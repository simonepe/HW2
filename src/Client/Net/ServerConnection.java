package Client.Net;

import Common.Command;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class ServerConnection implements Runnable {
 
    private static final int MAX_WORD_LENGTH = 8192;
    private SocketChannel socketChannel;
    private Selector selector;
    private InetSocketAddress serverAddress;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private volatile boolean timeToSend = false;
    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(MAX_WORD_LENGTH);
    private MessageListener listener;
    private boolean connected;

    @Override
    public void run() {
        try {
            initConnection();
            initSelector();
            
            while (connected || !messagesToSend.isEmpty()) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        recvFromServer(key);
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }
        } catch (Throwable connectionFailure) {
            System.err.println("CONNECTION FAILURE");
        }try{
            doDisconnect();
        }catch (Throwable connectionFailure){
            System.err.println("DISCONNECT FAILURE");
        }
    }
    
    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }
    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
    
    public void connect(String host, int port) {
        serverAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
    }
     private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }
    
    public void disconnect() throws IOException {
        connected = false;
        sendMsg(Command.DISCONNECT.toString());
    }

    private void doDisconnect() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }
    
     private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void recvFromServer(SelectionKey key) throws IOException {
        msgFromServer.clear();
        int numOfReadBytes = socketChannel.read(msgFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException("ERROR");
        }
        String recvdString = extractMessageFromBuffer();
        notifyMsgReceived(recvdString);
    }

    private void notifyMsgReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
        pool.execute(new Runnable() {
            @Override
            public void run() {
            listener.recvdMsg(msg);
           }
        });
    }
    
    private String extractMessageFromBuffer() {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        return new String(bytes);
    }
    
    private void sendMsg(String s) {
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(s.getBytes()));
        }
        timeToSend = true;
        selector.wakeup();
    }
    
    public void newConsoleOutput(MessageListener listener) {
        this.listener = listener;
    }
    
    public void newGame() {
        sendMsg(Command.NEWGAME.toString());
    }
    
    public void makeGuess(String guess) {
        sendMsg(guess);
    }

}

package Server.Net;

import Common.MessageException;
import Server.Controller.Controller;
import Server.Model.WordHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class Server {
    private final Controller contr = new Controller();
    public final WordHandler wordHandler = new WordHandler();
    private static final int LINGER_TIME = 5000;
    private int portNo = 3333;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;


    public static void main(String[] args) {
        Server server = new Server();
        System.out.println("Server running...");
        server.serve();
    }

    private void serve() {
        try {
            initSelector();
            initListeningSocketChannel();
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        startHandler(key);
                    } else if (key.isReadable()) {
                        recvFromClient(key);
                    } else if (key.isWritable()) {
                        sendToClient(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
    }

    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    private void recvFromClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.handler.recvMsg();
        } catch (IOException clientHasClosedConnection) {
            removeClient(key);
        }
    }
    
    private void sendToClient(SelectionKey key) throws IOException {
        Client client = (Client) key.attachment();
        try {
            client.sendAll();
            key.interestOps(SelectionKey.OP_READ);
        } catch (MessageException couldNotSendAllMessages) {
        } catch (IOException clientHasClosedConnection) {
            removeClient(key);
        }
    }
    private void removeClient(SelectionKey clientKey) throws IOException {
        Client client = (Client) clientKey.attachment();
        client.handler.disconnectClient();
        clientKey.cancel();
    }

    private ByteBuffer stringToByteBuffer(String msg) {
        return ByteBuffer.wrap(msg.getBytes());
    }
    
    private void startHandler(SelectionKey key) throws IOException, Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        ClientHandler handler = new ClientHandler(this, clientChannel);
        clientChannel.register(selector, SelectionKey.OP_WRITE, new Client(handler));
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }
    
    public void newGame(ClientHandler client) throws Exception {
        String msg = contr.newGame(client.scoreboard, wordHandler, client, this);
        ByteBuffer buff = stringToByteBuffer(msg);
        client.sendMsg(buff);
    }
    public void guess(String guess, ClientHandler client) throws IOException {
        String msg = contr.guess(guess, client.scoreboard.currentGame);
        ByteBuffer buff = stringToByteBuffer(msg);
        client.sendMsg(buff);  
    }
    public void newScore(ClientHandler client, String msg) throws IOException {
        ByteBuffer buff = stringToByteBuffer(msg);
        client.sendMsg(buff);
    }

    private class Client {
        private final ClientHandler handler;
        private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();

        private Client(ClientHandler handler) {
            this.handler = handler;
        }

        private void sendAll() throws IOException, MessageException {
            ByteBuffer msg = null;
            synchronized (messagesToSend) {
                while ((msg = messagesToSend.peek()) != null) {
                    handler.sendMsg(msg);
                    messagesToSend.remove();
                }
            }
        }
    }
    
}

package Server.Net;

import java.io.IOException;
import Common.Command;
import Common.MessageException;
import Server.Model.*;

import static Common.Command.DISCONNECT;
import static Common.Command.NEWGAME;
import static Common.Command.NO_COMMAND;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final int MAX_WORD_LENGTH = 8192;
    private final Server server;
    private final SocketChannel clientChannel;
    public Scoreboard scoreboard;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(MAX_WORD_LENGTH);
    private final ArrayDeque<String> messages = new ArrayDeque<>();
    private StringBuilder recvdChars = new StringBuilder();

    ClientHandler(Server server, SocketChannel clientChannel) throws Exception {
        this.server = server;
        this.clientChannel = clientChannel;
        scoreboard= new Scoreboard(server.wordHandler);
        server.newGame(this);
    }

    @Override
    public void run() {
        while (hasNext(messages)) {
            try {
                String s = messages.pop();
                InputType msg = new InputType(s);
                
                switch (msg.commandType) {

                    case NEWGAME:
                        server.newGame(this);
                        break;
                    case DISCONNECT:
                        disconnectClient();
                        break;
                    case NO_COMMAND:
                        server.guess(msg.receivedString.toUpperCase(), this);
                        break;
                    default:
                        throw new MessageException("Corrupt message: " + msg.receivedString);
                }
            } catch (IOException ioe) {
                try {
                    disconnectClient();
                } catch (IOException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                throw new MessageException(ioe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sendMsg(ByteBuffer msg) throws IOException {
        clientChannel.write(msg);
        if (msg.hasRemaining()) {
            throw new MessageException("Could not send message");
        }
    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }
    
    void recvMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Connection is closed");
        }
        String recvdString = extractMessageFromBuffer();
        messages.add(recvdString);
        ForkJoinPool.commonPool().execute(this);
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    private static class InputType {
        private Command commandType;
        private String receivedString;

        private InputType(String receivedString) {
            parse(receivedString);
            this.receivedString = receivedString;
        }

        private void parse(String strToParse) {
            try {
                String up = strToParse.toUpperCase();
                if(null != up)switch (up) {
                    case "NEWGAME":
                        commandType = Command.valueOf(up);
                        break;
                    case "DISCONNECT":
                        commandType = Command.valueOf(up);
                        break;
                    default:
                        commandType = Command.NO_COMMAND;
                        break;
                }
            } catch (Throwable throwable) {
                throw new MessageException(throwable);
            }
        }
    }

    public synchronized boolean hasNext(Queue queue) {
        return !queue.isEmpty();
    }
    
}

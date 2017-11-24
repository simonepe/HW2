package Client.View;

import java.util.Scanner;
import Client.Net.ServerConnection;
import Client.Net.MessageListener;

public class Interpreter implements Runnable{

    private final Scanner console = new Scanner(System.in);
    private boolean startGame = false;
    private ServerConnection server;
    private final ThreadSafeStdOut outP = new ThreadSafeStdOut();

    public Interpreter(){
    }
    public void start() {
        if (startGame) {
            return;
        }
        startGame = true;
        server = new ServerConnection();
        new Thread(this).start();
    }

        @Override
    public void run() {
        while (startGame) {
            try {
                String msg = readNextLine();
                switch (msg.toUpperCase()) {
                    case "QUIT":
                        startGame = false;
                        server.disconnect();
                        break;
                    case "CONNECT":
                        server.newConsoleOutput(new ConsoleOutput());
                        server.connect("localhost",3333);
                        break;
                    case "YES":
                        server.newGame();
                        break;
                    default:
                        server.makeGuess(msg);
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
            }
        }
    }
    
    private String readNextLine() {
        return console.nextLine();
    }

    private class ConsoleOutput implements MessageListener  {
        @Override
        public void recvdMsg(String msg) {
            printToConsole(msg);
        }

        private void printToConsole(String output) {
            outP.println(output);
        }
    }

}




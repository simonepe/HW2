package Server.Controller;

import Server.Model.HangManGame;
import Server.Model.Scoreboard;
import Server.Model.WordHandler;
import Server.Net.Server;
import Server.Net.ClientHandler;
import java.io.IOException;


public class Controller {
    private Scoreboard scoreboard;
    private Server server;
    private ClientHandler client;
    
    public Controller(){
    }

    public String newGame(Scoreboard scoreboard, WordHandler wh, 
                          ClientHandler client, Server server ) throws Exception {
        this.scoreboard = scoreboard;
        this.client = client;
        this.server = server;
        return scoreboard.newGame(wh);
    }


    public String guess(String guess, HangManGame currentGame) throws IOException {
        return currentGame.guess(guess);
    }
    
    public void positiveScore() throws IOException{
        String msg = scoreboard.positiveScore();
        server.newScore(client, msg);
    }
    
    public void negativeScore() throws IOException{
        String msg = scoreboard.negativeScore();
        server.newScore(client, msg);
    }
}

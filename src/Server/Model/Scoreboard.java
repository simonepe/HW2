package Server.Model;

public class Scoreboard {
    
    public HangManGame currentGame;
    int score;
    WordHandler wh;

    public Scoreboard(WordHandler wh) throws Exception {
        this.score = 0;
        this.wh = wh;
    }

    public String newGame(WordHandler wh) throws Exception {
        this.currentGame = new HangManGame(wh.randomWord(), this);
        String newGameBegins = currentGame.start();
        return newGameBegins;
    }

    public String positiveScore() {
        score++;
       return String.valueOf(score);
    }
    
    public String negativeScore() {
        score--;
        return String.valueOf(score);
    }

}

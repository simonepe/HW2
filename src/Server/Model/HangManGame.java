package Server.Model;

import Server.Controller.Controller;
import java.io.IOException;

public class HangManGame {

    int remainingGuesses;
    boolean correctGuess;
    String[] wordLetters;
    String[] output;
    String word;
    String won = "YOU WON!";
    String lost = "YOU LOST!";
    String nextGuess = "Enter your next guess";
    String gameBegins = "A new game has started, make your first guess.";
    String startOver = "type YES to start a new game";
    String youGuessed = "You guessed: ";
    String guessesRemaining = "Guesses remaining: ";
    String searchedWord = "The word we were looking for was: ";
    String wrongGuess = "WRONG GUESSES :";
    String currentScore = "Your current score is: ";
    String[] guessedLetters;
    String[] wrongGuessedLetters;
    int guessedLetterIndex;
    int wrongGuessedLetterIndex;
    String wrongGuesses;
    Scoreboard scoreboard;


    public HangManGame(String word, Scoreboard scoreboard){
        this.word = word;
        this.scoreboard = scoreboard;
        remainingGuesses = word.length();
        wordLetters = word.split("");
        guessedLetterIndex = 0;
        wrongGuessedLetterIndex = 0;
        wrongGuesses = "";
        start();   
    }

    public String start(){
        System.out.println(word);
        wrongGuessedLetters = new String[word.length()*2];
        guessedLetters = new String[word.length()*2];
        output = new String[word.length()*2];
        int i = 0;
        while (i < word.length()*2){
            output[i] = "_ ";
            i++;
        }
        String letterLines = stringArrayToString(wordLetters, output);

        return gameBegins + System.lineSeparator() + letterLines;
    }
    public String guess(String guess) throws IOException{
        boolean allLettersDone = true;
        boolean correctGuess = false;
        if (guess.length() > 1){
            if (guess.equals(word)) {
                                       
                return won();
            }
            else {                               
                return lost();
            }
        }
        
        for (String s : guessedLetters){
            if (guess.equals(s)){
                String currentWord = stringArrayToString(wordLetters, output);
                return "You already guessed " + guess + "! Try something else!"
                        + System.lineSeparator()
                        + currentWord;
            }   
        }

        guessedLetters[guessedLetterIndex] = guess;
        guessedLetterIndex++;
        
        int i = 0;
        for (String s : wordLetters){
            if (guess.equals(s)){         
                output[i] = guess;
                correctGuess = true;
            }
            else if (output[i*2].equals("_ ")){
                allLettersDone = false;        
            }
            i++;
        }
        if (allLettersDone) {
            return won();          
        }
      
        String currentWord = stringArrayToString(wordLetters, output);
        if(currentWord.equals(word)){
            return won();
        }
        if (correctGuess == false){
            wrongGuessedLetters[wrongGuessedLetterIndex] = guess;
            wrongGuessedLetterIndex++;
            String[] tom = new String[wrongGuessedLetterIndex];
            wrongGuesses = stringArrayToString(tom, wrongGuessedLetters);
            remainingGuesses--;
            if(remainingGuesses < 1){
                return lost();
            }
        }
        return youGuessed + guess + System.lineSeparator() 
                          + nextGuess + System.lineSeparator() 
                          + guessesRemaining + remainingGuesses + System.lineSeparator()
                          + wrongGuess + wrongGuesses + System.lineSeparator()
                          + currentWord;
    }
    
    private String won() throws IOException{
        String newScore = scoreboard.negativeScore(); 
        return won + System.lineSeparator()
                + currentScore + newScore + System.lineSeparator()
                + searchedWord + word + System.lineSeparator()
                + startOver;
    }
    
    private String lost() throws IOException{
        String newScore = scoreboard.negativeScore();
        return lost + System.lineSeparator()
                + currentScore + newScore + System.lineSeparator()
                + searchedWord + word + System.lineSeparator()
                + startOver;
    }
    
    private String stringArrayToString(String[] wordLetters, String[] output){
        int t = 0;
        StringBuffer sb = new StringBuffer("");
        for( String si : wordLetters){
            sb.append(output[t]);
            t++;
        }
        return sb.toString();
    }
}

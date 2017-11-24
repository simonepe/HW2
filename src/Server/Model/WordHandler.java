package Server.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class WordHandler {

    public WordHandler(){
        
    }
    public String randomWord() throws Exception {
         String newWord = "";
         try{
             FileInputStream fis = new FileInputStream("/Users/Simone/Downloads/words.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fis));
             
             String line;
             ArrayList<String> allWords = new ArrayList();
             
              try {
                while ((line = br.readLine()) != null) {
                    allWords.add(line);
                }
                
                Random r = new Random();
                int randomIndex = r.nextInt(allWords.size());
                newWord = allWords.get(randomIndex);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
         }catch(IOException ex){
         ex.printStackTrace();
     }
         return newWord.toUpperCase();
     }  
}





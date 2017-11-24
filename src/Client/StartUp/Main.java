package Client.StartUp;

import Client.View.Interpreter;

public class Main {

    public static void main(String[] args){
        System.out.println("Type connect to connect to server");
        new Interpreter().start();
    }
}

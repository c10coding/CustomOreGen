package net.dohaw.customoregen;

public class UnexpectedFileExists extends Exception{

    public UnexpectedFileExists(String fileName){
        super("There was an unexpected file that exists! It goes by the name " + fileName);
    }

}

package net.dohaw.customoregen.exception;

public class LargerMinYException extends Exception{

    public LargerMinYException(String customOreName){
        super("The Minimum Y level is larger than the Maximum Y Level for the custom ore " + customOreName + "");
    }

}

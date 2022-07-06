package src.BuildingBlocks.Values;

import src.Tokens.Token;


import java.util.ArrayList;

public class FunctionCall {

    private String name ;
    private ArrayList<Token> params ;
    private String desc ;
    private boolean isStatic ;

    public FunctionCall(String n , ArrayList<Token> p , String d , boolean S){
        name = n ;
        params = p ;
        desc = d ;
        isStatic = S ;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public ArrayList<Token> getParams() {
        return params;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "name='" + name + '\'' +
                ", params=" + params +
                ", desc='" + desc + '\'' +
                '}';
    }
}



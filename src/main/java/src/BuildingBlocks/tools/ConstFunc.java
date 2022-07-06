package src.BuildingBlocks.tools;

import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;

public class ConstFunc {

    private ArrayList<Token> pattern ;
    private ArrayList<Token> result ;

    public ConstFunc(ArrayList<Token> pat ,ArrayList<Token> res ){
        pattern = pat ;
        result  = res ;
    }

    public boolean startsWithVar(){
        return pattern.get(0).getToken() == TokenT.Var;
    }

    public ArrayList<Token> getPattern() {
        return pattern;
    }

    public ArrayList<Token> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ConstFunc{" +
                "pattern=" + pattern +
                ", result=" + result +
                '}';
    }
}

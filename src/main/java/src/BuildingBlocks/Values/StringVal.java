package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class StringVal {

    public ArrayList<Token> parts ;

    public StringVal(ArrayList<Token> p){
        parts = p ;
    }

    @Override
    public String toString() {
        return "StringVal{" +
                "parts=" + parts +
                '}';
    }
}

package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class ArrayDeclaration2 {

    private ArrayList<Token> values ;

    public ArrayDeclaration2(ArrayList<Token> tok ){
        values = tok ;
    }

    public ArrayList<Token> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "ArrayDeclaration2{" +
                "values=" + values +
                '}';
    }
}

package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class ArrayDeclaration {

    private String type ;
    private ArrayList<Token> values;

    public ArrayDeclaration(String s , ArrayList<Token> va){
        type = s ;
        values  = va ;
    }

    public ArrayList<Token> getValues() {
        return values;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ArrayDeclaration{" +
                "type='" + type + '\'' +
                ", values=" + values +
                '}';
    }
}

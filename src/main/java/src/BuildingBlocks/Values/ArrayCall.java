package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class ArrayCall {

    boolean isBasic;
    ArrayList<Token> Values = null ;
    String name ;
    String type ;

    public ArrayCall(ArrayList<Token> val, String n, String t , boolean b){
        Values = val ;
        name = n ;
        type = t ;
        isBasic = b ;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isBasic() {
        return isBasic;
    }

    public ArrayList<Token> getValues() {
        return Values;
    }

    @Override
    public String toString() {
        return "ArrayCall{" +
                "isBasic=" + isBasic +
                ", Values=" + Values +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

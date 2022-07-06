package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class ObjectDeclaration {

    private String address ;
    private String desc ;
    private ArrayList<Token> values ;

    public ObjectDeclaration(String a, String d, ArrayList<Token> val){
        address = a ;
        desc = d ;
        values = val ;
    }

    public ArrayList<Token> getValues() {
        return values;
    }

    public String getDesc() {
        return desc;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "ObjectDeclaration{" +
                "address='" + address + '\'' +
                ", desc='" + desc + '\'' +
                ", values=" + values +
                '}';
    }
}

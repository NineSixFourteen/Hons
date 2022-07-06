package src.BuildingBlocks.parserTypes;

import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.ToolKit;
import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;

public class ExlVariable {
    private final String name ;
    private final TokenT Type ;
    private String objName = "";
    private final boolean isArray ;
    private boolean isFlat = false;
    private int arraySize = 0 ;
    private int pos ;


    public ExlVariable(String n, TokenT t, int p){
        name  = n ;
        Type  = t ;
        pos   = p ;
        isArray = false;
    }

    public ExlVariable(String n , TokenT t , String obj , int p ){
        name  = n ;
        Type  = t ;
        objName = obj ;
        pos   = p ;
        isArray = false;
    }

    public ExlVariable(String n, TokenT t,  int p , boolean b , int si){
        name    = n ;
        Type    = t ;
        pos     = p ;
        isArray = b ;
        arraySize = si ;
    }

    public ExlVariable(String n, TokenT t,  int p , boolean b , boolean is , int si){
        name    = n ;
        Type    = t ;
        pos     = p ;
        isArray = b ;
        isFlat = is ;
        arraySize = si ;
    }
    public ExlVariable(String n, TokenT t, String ty ,  int p , boolean b , int si ){
        name    = n ;
        Type    = t ;
        objName = ty ;
        pos     = p ;
        isArray = b ;
        arraySize = si ;
    }
    public ExlVariable(String n, TokenT t, String ty ,  int p , boolean b , boolean d , int si ){
        name    = n ;
        Type    = t ;
        objName = ty ;
        pos     = p ;
        isArray = b ;
        isFlat = d ;
        arraySize = si ;
    }

    public String getString(ArrayList<ExlClassInfo> objects){
        String s = "";
        if(isArray) {
            for (int i = 0; i < arraySize; i++) {
                s += "[";
            }
        }
        switch (Type) {
            case Int:
            case String:
            case Char:
            case Boolean:
            case Float:
            case Long:
            case Double:
            case Void:
                return s + ToolKit.TokenToString(new Token(Type , null) ,objects);
            case Object:
                return s + "L" + ToolKit.getAddress(objName , objects) + ";";
        }
        return null ;
    }

    public int getArraySize() {
        return arraySize;
    }

    public String getName() {
        return name;
    }

    public TokenT getType() {
        return Type;
    }

    public int getPos() {
        return pos;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getObjName() {
        return objName;
    }

    public boolean isFlat() {
        return isFlat;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "ExlVariable{" +
                "name='" + name + '\'' +
                ", Type=" + Type +
                ", objName='" + objName + '\'' +
                ", isArray=" + isArray +
                ", arraySize=" + arraySize +
                ", pos=" + pos +
                '}';
    }
}
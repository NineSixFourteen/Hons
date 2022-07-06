package src.BuildingBlocks.Values;


import src.Tokens.Token;

import java.util.ArrayList;

public class ObjectRef {

    private boolean isStatic ;
    private boolean isLocal ;
    private boolean isFunc ;
    private String name ;
    private String Description ;
    private String Origin ;
    private String type ;
    private ArrayList<Token> param ;


    public ObjectRef(boolean i ,boolean il ,  boolean is , String n , String d, String O , String T , ArrayList<Token> p){
        isFunc = i ;
        isLocal = il ;
        isStatic = is ;
        name = n ;
        Description = d ;
        Origin = O ;
        type = T ;
        param = p ;
    }

    public boolean isFunc() {
        return isFunc;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return Origin;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Token> getParam() {
        return param;
    }

    public String getDescription() {
        return Description;
    }

    @Override
    public String toString() {
        return "ObjectRef{" +
                "isStatic=" + isStatic +
                ", isFunc=" + isFunc +
                ", name='" + name + '\'' +
                ", Origin='" + Origin + '\'' +
                ", type='" + type + '\'' +
                ", param=" + param +
                ", description " + Description +
                '}';
    }
}

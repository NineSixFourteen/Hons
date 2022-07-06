package src.BuildingBlocks.Values;

import src.Tokens.Token;

import java.util.ArrayList;

public class IfThenElse {

    private Token If ;
    private Token Then ;
    private Token Else ;
    private Token type ;


    public IfThenElse(Token i, Token t, Token e){
        If = i ;
        Then = t ;
        Else = e ;
    }


    public IfThenElse(Token i, Token t, Token e, Token ty){
        If = i ;
        Then = t ;
        Else = e ;
        type = ty ;
    }

    public Token getElse() {
        return Else;
    }

    public Token getIf() {
        return If;
    }

    public Token getThen() {
        return Then;
    }

    public Token getType() {
        return type;
    }

    @Override
    public String toString() {
        return "IfThenElse{" +
                "IF=" + If +
                ", Then=" + Then +
                ", Else=" + Else +
                '}';
    }
}

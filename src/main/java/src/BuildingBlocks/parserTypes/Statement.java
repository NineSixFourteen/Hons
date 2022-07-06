package src.BuildingBlocks.parserTypes;

import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;

public class Statement {

    private ArrayList<Token> Body;
    private ArrayList<Statement> innerStatements ;
    private StatementType type ;
    private boolean isLast ;

    public Statement(StatementType type, ArrayList<Token> tokens, ArrayList<Statement> innerStatements){
        this.Body = tokens;
        this.type = type;
        this.innerStatements = innerStatements ;
        this.isLast = false ;
    }

    public ArrayList<Statement> getInnerStatements() {
        return innerStatements;
    }

    public ArrayList<Token> getBody() {
        return Body;
    }

    public TokenT TokenType(){
        switch (type){
            case String: return TokenT.String;
            case Int: return TokenT.Int ;
            case Boolean: return TokenT.Boolean ;
            case Char: return TokenT.Char ;
            case Float: return TokenT.Float;
            case Long: return TokenT.Long ;
            case Double: return TokenT.Double ;
            default: return null ;
        }
    }

    public StatementType getType() {
        return type;
    }

    public void setLast() {
        isLast = true;
    }

    public boolean getLast (){
        return isLast ;
    }

    @Override
    public String toString() {
        return "Statement{" +
                "tokens=" + Body +
                ", innerStatements=" + innerStatements +
                ", type=" + type +
                '}';
    }
}


package src.BuildingBlocks.parserTypes;

import src.Tokens.Token;

import java.util.ArrayList;

public class Function {
    private final String name ;
    private Token Type;
    private final ArrayList<Statement> statements;
    private final ArrayList<ExlVariable> parameters;
    private Boolean hasError = false ;
    private ArrayList<ExlVariable> vars;
    private String desc ;
    private boolean isStatic  ;
    private boolean isPublic  ;
    private boolean isPrivate  ;
    private String Throws ;

    public Function(String Name, Token type, ArrayList<Statement> statements, ArrayList<ExlVariable> parameters , String desc, boolean p , boolean pr , boolean s ,String th ){
        this.name = Name ;
        this.Type = type ;
        this.statements = statements ;
        this.parameters = parameters ;
        this.vars = parameters ;
        this.desc = desc ;
        isStatic = s ;
        isPrivate = pr ;
        isPublic  = p ;
        Throws = th ;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ExlVariable> getVars() {
        return vars;
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    public ArrayList<ExlVariable> getParameters() {
        return parameters;
    }

    public Token getType() {
        return Type;
    }

    public String getThrows() {
        return Throws;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public String toString() {
        return "Function{" +
                "ReturnType=" + Type +
                ", name=" + name +
                ", body=" + statements +
                ", parameters=" + parameters +
                ", hasError=" + hasError +
                '}';
    }
}


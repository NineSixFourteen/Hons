package src.Parser;

import src.BuildingBlocks.ToolKit;
import src.BuildingBlocks.Values.*;
import src.BuildingBlocks.tools.Tuple;
import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;

public class PPT {

    public static String prettyPrintLine(ArrayList<Token> t){
        StringBuilder s = new StringBuilder();
        for (Token te : t) {
            s.append(prettyPrint(te)).append(" ");
        }
        return s.toString();
    }

    public static String prettyPrintExpression(Token T){
        StringBuilder s = new StringBuilder("( ");
        try {
            Expression e = (Expression) T.getValue();
            for (Token t : e.tokens) {
                s.append(prettyPrint(t)).append(" ");
            }
            s.append(")");
            return s.toString();
        }catch (Exception e){
            ArrayList<Token> et = (ArrayList<Token>) T.getValue();
            if(et != null) {
                for (Token t : et) {
                    s.append(prettyPrint(t)).append(" ");
                }
                s.append(")");
                return s.toString();
            } else {
                return "";
            }
        }
    }

    public static String prettyPrint(Token T){
        switch (T.getToken()){
            case Plus: return "+";
            case Minus: return "-";
            case Mul: return "*";
            case Div: return "/";
            case LThan: return "<";
            case LThanEq: return "<=";
            case GThan: return ">";
            case GThanEq:return ">=";
            case Int: return "int";
            case Float: return "float";
            case Double:return "double";
            case Case: return "case";
            case Char: return "char";
            case And: return "&&";
            case Void: return "void";
            case Or: return "||";
            case LBracket: return "(";
            case RBracket: return ")";
            case RBrace: return "}";
            case LBrace: return "{";
            case Return: return "return";
            case String: return "String";
            case Expression: return prettyPrintExpression(T);
            case Value: return "" + T.getValue() ;
            case Array: return PrettyPrintArray(T);
            case ArrayDeclaration: return PrettyPrintArrayDec(T);
            case ArrayDeclaration2: return PrettyPrintArrayDec2(T);
            case FunctionCall: return PrettyPrintFunctionCall(T);
            case If: return "if";
            case Print: return "Print";
            case Try: return "try";
            case LSquare:return "[";
            case RSquare:return "]";
            case Then:return "then";
            case Else:return "else";
            case Public:return "public";
            case Private:return "private";
            case Long:return "long";
            case Static:return "static";
            case IfThenElse:return PrettyPrintITE(T);
            case StringVal: return PrettyPrintStringVal(T);
            case ArrayCall: return PrettyPrintACall(T);
            case Object: return PrettyPrintObject(T);
            case Cast: return "cast(" + prettyPrint((Token) T.getValue()) + ")";
        }
        return "";
    }

    private static String PrettyPrintObject(Token t) {
        ArrayList<ObjectRef> ors = (ArrayList<ObjectRef>) t.getValue();
        StringBuilder s = new StringBuilder(ors.get(0).getOrigin() + ".");
        for(ObjectRef or : ors){
            s.append(PrettyPrintObjectRef(or)).append(".");
        }
        s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    private static String PrettyPrintObjectRef(ObjectRef or) {
        if(or.isLocal()){
            switch (or.getName()){
                case "Val":
                    return (String) or.getParam().get(0).getValue();
                case "FC":
                    return PrettyPrintFunctionCall(new Token(TokenT.FunctionCall , or.getParam().get(0).getValue(), or.getParam().get(0).getLine() ));
                case "OD":
                    return "TODO OD IN PPT";
                default:
                    return "";
            }
        } else if(or.isFunc()){
            StringBuilder s = new StringBuilder("");
            s.append("(");
            for(Token T : or.getParam()){
                s.append(prettyPrint(T)).append(", ");
            }
            s.append(")");
            return or.getName() + s ;
        } else {
            return or.getName() ;
        }
    }

    private static String PrettyPrintACall(Token t) {
        ArrayCall ac = (ArrayCall) t.getValue();
        String s = "";
        s += ac.getName();
        for(int i = 0 ; i < ac.getValues().size() ; i++){
            s += "[" + prettyPrint(ac.getValues().get(i)) + "]";
        }
        return s ;
    }

    private static String PrettyPrintStringVal(Token t) {
        StringVal sv = (StringVal) t.getValue();
        StringBuilder s = new StringBuilder("( ");
        for(Token tk : sv.parts){
            s.append(prettyPrint(tk) + " + ");
        }
        s.delete(s.length() - 2 , s.length());
        s.append(")");
        return s.toString();
    }

    private static String PrettyPrintITE(Token t) {
        IfThenElse ite = (IfThenElse) t.getValue() ;
        return "if " + prettyPrint(ite.getIf()) + " then " + prettyPrint(ite.getThen()) + " else " + prettyPrint(ite.getElse());
    }

    private static String PrettyPrintFunctionCall(Token t) {
        FunctionCall fc = (FunctionCall) t.getValue();
        StringBuilder s = new StringBuilder(fc.getName());
        s.append("(");
        for(Token T : fc.getParams()){
            s.append(prettyPrint(T)).append(", ");
        }
        s.append(")");
        return s.toString();
    }

    private static String PrettyPrintArrayDec(Token t) {
        ArrayDeclaration ad = (ArrayDeclaration) t.getValue();
        StringBuilder s = new StringBuilder("new " + prettyPrint(ToolKit.StringToToken(ad.getType(), t.getLine())));
        for(int i = 0 ; i < ad.getValues().size(); i++){
            s.deleteCharAt(s.length() - 1);
            s.deleteCharAt(s.length() - 1);
        }
        for(Token T : ad.getValues()){
            s.append("[").append(prettyPrint(T)).append("]");
        }
        return s.toString();
    }

    private static String PrettyPrintArrayDec2(Token t) {
        ArrayDeclaration2 ad2 = (ArrayDeclaration2) t.getValue();
        StringBuilder s = new StringBuilder("[ ");
        for(Token T : ad2.getValues() ){
            s.append(prettyPrint(T)).append(", ");
        }
        s.delete(s.length() - 2 , s.length() - 1);
        s.append(" ]");
        return s.toString();
    }

    private static String PrettyPrintArray(Token t) {
        Tuple<Token, Integer> array = (Tuple<Token, Integer>) t.getValue();
        StringBuilder s = new StringBuilder(prettyPrint(array.x));
        for(int i = 0 ; i < array.y;i++){
            s.append("[]");
        }
        return s.toString();
    }

    public static String prettyPrintList(ArrayList<Token> t){
        StringBuilder s = new StringBuilder();
        for (Token te : t) {
            s.append(prettyPrint(te)).append(", ");
        }
        if(s.length() > 0) {
            s.delete(s.length() - 2, s.length());
        }
        return s.toString();
    }
}

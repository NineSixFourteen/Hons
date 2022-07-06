package src.Parser;

import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.Info.MethodInfo;
import src.BuildingBlocks.ToolKit;
import src.BuildingBlocks.Values.*;
import src.BuildingBlocks.parserTypes.ExlVariable;
import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;
import java.util.Objects;

public class Validate {
    public static ArrayList<ExlError> validateBoolean(ArrayList<Token> tokens, ArrayList<ExlError> errors , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars) {
        ArrayList<Token> notBooleanParts = new ArrayList<>();
        for(int i = 0 ; i < tokens.size();i++) {
            Token t = tokens.get(i);
            switch (t.getToken()){
                case BoolExpression:
                    errors = validateBoolean((ArrayList<Token>) t.getValue(),errors ,objects ,vars);
                    break;
                case Expression:
                    try {
                        Expression e = (Expression) t.getValue();
                        errors = validateExpression(e.tokens, errors, objects, vars);
                    } catch (Exception er){
                        errors = validateExpression((ArrayList<Token>) t.getValue(), errors, objects, vars);
                    }
                    break;
                case Or:
                    if(i < 1 ||i + 1  > tokens.size() ){
                        errors.add(new ExlError(t.getLine(), "|| required a value on both sides of it"));
                    } else if(! (isBoolean(tokens.get(i - 1),vars) && isBoolean(tokens.get(i + 1),vars))){
                        errors.add(new ExlError(t.getLine(),"|| requires booleans expressions on either side"));
                    }
                    break;
                case And:
                    if(i < 1 ||i + 1  > tokens.size() ){
                        errors.add(new ExlError(t.getLine(), "&& required a value on both sides of it"));
                    } else if(! (isBoolean(tokens.get(i - 1),vars) && isBoolean(tokens.get(i + 1), vars))){
                        errors.add(new ExlError(t.getLine(),"&& requires booleans expressions on either side"));
                    }
                    break;
                case LThanEq:
                case LThan:
                case GThan:
                case GThanEq:
                case EqualTo:
                case NotEqualTo:
                    if(i < 1 ||i + 1  > tokens.size() ){
                        errors.add(new ExlError(t.getLine(), PPT.prettyPrint(t)  + " required a value on both sides of it"));
                    } else if(!isSameType(tokens.get(i - 1) , tokens.get(i + 1) , objects , vars)){
                        errors.add(new ExlError(t.getLine(),PPT.prettyPrint(t) + " required both values on each side to be of the same type"));
                    }
                    break;
                default:
                    if(!isBoolean(t,vars)){
                        notBooleanParts.add(t);
                    }
            }
        }
        if(notBooleanParts.size() > 0 ) {
            errors.add(new ExlError(tokens.get(0).getLine(), "Tokens " + PPT.prettyPrintList(notBooleanParts) + " should not be in boolean expression"));
        }
        return errors;
    }

    private static boolean isSameType(Token token, Token token1 , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars) {
        ArrayList<String> typesOne = ToolKit.possibleType(token, objects , vars);
        ArrayList<String> typesTwo = ToolKit.possibleType(token1, objects, vars);
        for(String x : typesOne){
            for(String y : typesTwo){
                if(x.equals(y) ){
                    return true ;
                }
            }
        }
        return false ;
    }
    private static boolean isSameTypePlus(Token token, Token token1 , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars) {
        ArrayList<String> typesOne = ToolKit.possibleType(token, objects , vars);
        ArrayList<String> typesTwo = ToolKit.possibleType(token1, objects, vars);
        for(String x : typesOne){
            for(String y : typesTwo){
                if(x.equals(y) || x.equals("Ljava/lang/String;") || y.equals("Ljava/lang/String;") ){
                    return true ;
                }
            }
        }
        return false ;
    }

    private static boolean isBoolean(Token token , ArrayList<ExlVariable> vars) {
        switch (token.getToken()){
            case BoolExpression:return true ;
            case Value:
                if( token.getValue().equals("true") || token.getValue().equals("false")) {
                    return true ;
                } else {
                    for(ExlVariable var : vars){
                        if(var.getName().equals(token.getValue())){
                            return true;
                        }
                    }
                    return false ;
                }
            case FunctionCall:
                FunctionCall fc = (FunctionCall) token.getValue();
                return fc.getDesc().endsWith("Z");
            case ArrayCall:
                ArrayCall ac = (ArrayCall) token.getValue();
                return ac.getType().equals("Z");
            case Object:
                ArrayList<ObjectRef> or = (ArrayList<ObjectRef>) token.getValue() ;
                ObjectRef oR = or.get(or.size() - 1);
                return oR.getType().equals("Z");
        }
        return false ;
    }

    public static ArrayList<ExlError> validateExpression(ArrayList<Token> tokens, ArrayList<ExlError> errors, ArrayList<ExlClassInfo> objects, ArrayList<ExlVariable> vars) {
        for(int i = 0 ; i < tokens.size();i++) {
            Token t = tokens.get(i);
            switch (t.getToken()){
                case Expression:
                    Expression e = (Expression) t.getValue();
                    errors = validateExpression(e.tokens ,errors, objects ,vars );
                    break;
                case Plus:
                    if(i < 1 || i + 2  > tokens.size()  ){
                        errors.add(new ExlError(t.getLine(), PPT.prettyPrint(t) + " required a value on both sides of it"));
                    } else if(!isSameTypePlus(tokens.get(i - 1) , tokens.get(i + 1) , objects , vars)){
                        errors.add(new ExlError(t.getLine(),PPT.prettyPrint(t) + " required both values on each side to be of the same type but " + PPT.prettyPrint(tokens.get(i - 1)) + " and " + PPT.prettyPrint(tokens.get(i + 1)) + " are not "));
                    }
                    break;
                case Minus:
                case Div:
                case Mul:
                    if(i < 1 || i + 2  > tokens.size()  ){
                        errors.add(new ExlError(t.getLine(), PPT.prettyPrint(t) + " required a value on both sides of it"));
                    } else if(!isSameType(tokens.get(i - 1) , tokens.get(i + 1) , objects , vars)){
                        errors.add(new ExlError(t.getLine(),PPT.prettyPrint(t) + " required both values on each side to be of the same type but " + PPT.prettyPrint(tokens.get(i - 1)) + " and " + PPT.prettyPrint(tokens.get(i + 1)) + " are not "));
                    }
                    break;
                case Value:
                    String s = (String) t.getValue();
                    if(!isValue(s)){
                        boolean b = true ;
                        for(ExlVariable var : vars){
                            if(var.getName().equals(s)){
                                b =  false ;
                                break;
                            }
                        }
                        if(b){
                            errors.add(new ExlError(tokens.get(0).getLine() , "Unable to find variable called " + s));
                        }
                    }
            }
        }
        return errors;
    }

    private static boolean isValue(String s) {
        try{
            Double.parseDouble(s);
            return true;
        } catch (Exception e){
            switch (s.charAt(0)){case '\'': case '\"': return true;}
        }
        return false ;
    }

    public static boolean validateArray(Token token, Expression exp , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars, ArrayList<MethodInfo> mi) {
        String s = ToolKit.TokenToString(token ,objects );
        Token t = exp.get(0);
        switch (t.getToken()){
            case ArrayDeclaration:
            case ArrayDeclaration2:
            case FunctionCall:
            case ArrayCall:
            case IfThenElse:
            case Object:
                return ToolKit.isType(t , s , objects,vars,mi);
        }
        return false ;
    }



}

package src.BuildingBlocks;

import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ConstructorInfo;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.Info.MethodInfo;
import src.BuildingBlocks.Values.*;
import src.BuildingBlocks.parserTypes.ExlVariable;
import src.BuildingBlocks.parserTypes.Statement;
import src.BuildingBlocks.parserTypes.StatementType;
import src.BuildingBlocks.tools.ConstFunc;
import src.BuildingBlocks.tools.Tuple;
import src.BuildingBlocks.tools.functionScope;
import src.Parser.PPT;
import src.Tokens.Token;
import src.Tokens.TokenT;
import java.util.ArrayList;
import java.util.HashMap;

public class ToolKit {

    //Can't make an object of it
    private ToolKit(){}

    public static Tuple<ArrayList<Token>,Integer> collectBracket(ArrayList<Token> tokens, int startAt, TokenT typeOfBrac) throws Exception {
        ArrayList<Token> tokens1 = new ArrayList<>();
        if(tokens.get(startAt).getToken() != typeOfBrac) {
            throw new java.lang.Exception("Collect Brackets should start with a LBracket");
        }
        int openBrackets = 0 ;
        startAt++ ;
        TokenT otherBrac = TokenT.RBracket;
        switch (typeOfBrac){
            case LBracket: break;
            case LSquare: otherBrac = TokenT.RSquare ;break;
            case LBrace: otherBrac = TokenT.RBrace ;break;
            default:
                throw new Error("collectBracket needs a bracket");
        }
        for(int i = startAt; i < tokens.size() ; i++ ) {
            Token t = tokens.get(i);
            if (t.getToken() == otherBrac) {
                if (openBrackets == 0) {
                    return new Tuple<>(tokens1, i);
                } else {
                    openBrackets -= 1;
                    tokens1.add(t);
                }
            } else if (t.getToken() == typeOfBrac) {
                openBrackets += 1;
                tokens1.add(t);
            } else {
                tokens1.add(t) ;
            }
        }
        throw new java.lang.Exception("Unable to find the closing bracket for " + PPT.prettyPrint(tokens.get(startAt - 1)) + " located at \n\t" + PPT.prettyPrintLine(new ArrayList<>(tokens.subList(0 , startAt))));
    }

    public static Tuple<ArrayList<Token>, Integer> collectLine(ArrayList<Token> tokens, int i) {
        ArrayList<Token> nList = new ArrayList<>();
        for(int j = i ; j < tokens.size(); j++){
            if(tokens.get(j).getToken() == TokenT.SemiColan){
                nList.add(tokens.get(j++));
                return new Tuple<>(nList , j);
            } else {
                nList.add(tokens.get(j));
            }
        }
        return null ;
    }

    public static Tuple<ArrayList<Token> , Integer> tokensUpTo(ArrayList<Token> tokens, int start, TokenT tT){
        ArrayList<Token> subList = new ArrayList<>();
        for(int i = start ; i < tokens.size(); i++){
            if (tokens.get(i).getToken() == tT){
                return new Tuple<>(subList , i);
            } else {
                subList.add(tokens.get(i));

            }
        }
        return new Tuple<>(subList , 0);
    }

    //Validate
    public static boolean containsReturn(ArrayList<Statement> innerStatements) {
        for(Statement s : innerStatements){
            if(s.getType() == StatementType.Return){
                return true ;
            }
        }
        return false ;
    }
    //To

    public static Token StringToToken(String s , int line){
        switch (s.charAt(0)){
            case 'I':
                return new Token(TokenT.Int , null,line);
            case 'Z':
                return new Token(TokenT.Boolean , null,line);
            case 'C':
                return new Token(TokenT.Char , null,line);
            case 'F':
                return new Token(TokenT.Float , null,line);
            case 'J':
                return new Token(TokenT.Long , null,line);
            case 'D':
                return new Token(TokenT.Double , null,line);
            case 'L':
                if(s.equals("ljava/lang/String;")){
                    return new Token(TokenT.String , null,line);
                }
                return new Token(TokenT.Object , s.substring(1 ,s.length() - 1), line);
            case '[':
                int i = 0 ;
                while(s.charAt(i) == '['){
                    i++ ;
                }
                return new Token(TokenT.Array , new Tuple<>(StringToToken(s.substring(i) , line), i));
        }
        return null ;
    }
    //Array
    public static Tuple<Token , Integer> getArrayInfo(ArrayCall ac, functionScope fs) {
        for(ExlVariable var : fs.getVars()){
            if(ac.getName().equals(var.getName())){
                for(int i = 0 ; i < ac.getType().length() ; i++){
                    if(ac.getType().charAt(i) != '[') {
                        if(var.getType() == TokenT.Object){
                            return new Tuple<>(new Token(var.getType(), var.getObjName(), ac.getValues().get(0).getLine()), var.getArraySize() - i);
                        }else {
                            return new Tuple<>(new Token(var.getType(), null , ac.getValues().get(0).getLine()), var.getArraySize() - i);
                        }
                    }
                }
            }
        }
        return null ;
    }

    //Is
    private static boolean isObject(String val, String s) {
        try{
            double d = Double.parseDouble(val);
            return false ;
        }catch (Exception e){
            if(val.startsWith("'")){
                return false ;
            }
            if(s.startsWith("Ljava/lang/Object;")){
                return true ;
            } else if(s.equals("Ljava/lang/String;")){
                return true ;
            }
        }
        return false ;
    }
    public static Boolean isSymbol(Token t) {
        return t.getToken() == TokenT.Plus || t.getToken() == TokenT.Minus || t.getToken() == TokenT.Div || t.getToken() == TokenT.Mul || t.getToken() == TokenT.Mod;
    }

    private static boolean isBool(String val) {
        return val.equals("true") || val.equals("false");
    }

    private static boolean isChar(String val) {
        boolean b = val.length() == 3 && val.charAt(0) == '\'' && val.charAt(2) == '\'';
        return b;
    }

    public static boolean isVar(String val, ArrayList<ExlVariable> vars) {
        if(validVarName(val)){
            for(ExlVariable var : vars){
                if(var.getName().equals(val)){
                    return true ;
                }
            }
        }
        return false ;
    }

    public static boolean isVarType(String val, ArrayList<ExlVariable> vars, String z) {
        for(ExlVariable var : vars){
            if(var.getName().equals(val)){
                switch (var.getType()){
                    case String: return z.equals("Ljava.lang.String;");
                    case Int: return z.equals("I");
                    case Float: return z.equals("F");
                    case Char: return z.equals("C");
                    case Boolean: return z.equals("Z");
                    case Long: return z.equals("J");
                    case Double: return z.equals("D");
                    case Object:
                        if(z.startsWith("L")){
                            return var.getObjName().equals(z);
                        }
                }
            }
        }
        return false ;
    }

    //GetInfo
    public static ArrayList<String> getInits(String type, ArrayList<ExlClassInfo> objects) {
        ArrayList<String> pdesc = new ArrayList<>();
        for(ExlClassInfo ec : objects ){
            if(ec.getName().equals(type)){
                for(ConstructorInfo ci : ec.getCons()){
                    pdesc.add(ci.getDescription());
                }
            }
        }
        return pdesc ;
    }

    public static String getAddress(String name, ArrayList<ExlClassInfo> objects){
        for(ExlClassInfo eci : objects){
            if(eci.getName().equals(name)){
                return eci.getAddress() ;
            }
        }
        return null;
    }

    public static String getDes(String type) {
        if(type.contains(".")){
            return type ;
        }
        if(type.length() == 1){
            return type ;
        }
        return type.substring(1 , type.length() - 1).replace('/', '.');
    }

    public static String TokenToString(Token t , ArrayList<ExlClassInfo> objects){
        switch (t.getToken()){
            case Int:return "I";
            case Float:return "F";
            case Char:return "C";
            case Double:return "D";
            case Long:return "J";
            case Boolean:return "Z";
            case String:return "Ljava/lang/String;";
            case Value:return "L" + getAddress((String) t.getValue(), objects) + ";";
            case Object:return "L" + t.getValue() + ";";
            case Void:return "V";
            case Array:
                Tuple<Token , Integer> array = (Tuple<Token, Integer>) t.getValue();
                StringBuilder L = new StringBuilder();
                for(int i = 0; i < array.y;i++){
                    L.append("[");
                }
                L.append(TokenToString(array.x, objects));
                return L.toString();
        }
        return "";
    }

    public static String getDesc(String type, String[] vars) {
        StringBuilder s = new StringBuilder("(");
        for(String r : vars){
            s.append(r);
        }
        s.append(")").append(type);
        return s.toString();
    }

    public static ArrayList<String> getDescP(ArrayList<MethodInfo> mIs) {
        ArrayList<String> s = new ArrayList<>() ;
        for(MethodInfo m : mIs){
            s.add(m.getDescription());
        }
        return sortpDesc(s);
    }

    private static ArrayList<String> sortpDesc(ArrayList<String> s) {
        if(allL(s)){
            return s ;
        }
        for(int i = 0 ; i < s.size() ; i++){
            if(s.get(0).charAt(1) == 'L'){
                s.add(s.get(0));
                s.remove(0);
                i -- ;
            }
        }
        return s ;
    }
    public static boolean isObject2(String value, ArrayList<ExlClassInfo> objects) {
        for(ExlClassInfo eci : objects){
            if(eci.getName().equals(value)){
                return true ;
            }
        }
        return false ;
    }

    public static Tuple<ArrayList<Token> , Integer> getValues(ArrayList<Token> tokens, int i) throws Exception {
        Tuple<ArrayList<Token> , Integer > x ;
        ArrayList<Token> values = new ArrayList<>();
        boolean b = true ;
        while(i < tokens.size() && b){
            switch (tokens.get(i).getToken()) {
                case LSquare:
                    x = collectBracket(tokens, i, TokenT.LSquare);
                    values.add(new Token(TokenT.Expression, x.x, tokens.get(0).getLine()));
                    i = x.y + 1;
                break;
                case Array:
                    throw new Exception("Missing size of new array should go between [ and ]  ");
                default:
                    b = false;


            }
        }
        return new Tuple<>(values , i);
    }

    public static String getTYpe(ExlVariable e, ArrayList<ExlClassInfo> objects, int line) {
        String s = "";
        if (e.isArray()) {
            for (int i = 0; i < e.getArraySize(); i++) {
                s += "[";
            }
        }
        switch (e.getType()) {
            case Int:
            case String:
            case Char:
            case Boolean:
            case Float:
            case Long:
            case Double:
            case Void:
                return s + TokenToString(new Token(e.getType() , null , line) , objects);
            case Object:
                return s + "L" + getAddress(e.getObjName() , objects) + ";";
        }
        return null ;
    }

    //Inference
    public static String calcDesc(ArrayList<String> pDesc, ArrayList<Token> x , ArrayList<ExlVariable> vars , boolean isStatic , String name , String funcName , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> mi) throws Exception {
        for (String s : pDesc) {
            String sr = getP(s);
            if (matches(sr, x , vars , isStatic , objects , mi)) {
                return s;
            }
        }
        throw new Exception("Can't find function " + funcName + " that fits desc " +  PPT.prettyPrint(new Token(TokenT.Expression , new Expression(x))) + " in " + name);
    }

    public static String calcDesc2(String Desc, Token x , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> mi) {
        if (matches(Desc, x , vars , isStatic , objects , mi)) {
            return Desc;
        }
        return "" ;
    }

    public static String getP(String s) {
        for(int i = 0 ; i < s.length() ; i++){
            if(s.charAt(i) == ')'){
                return s.substring(1 , i);
            }
        }
        return s;
    }

    //Is number with letter end
    private static boolean isNWL(String s){
        String s2 = s.substring(0 , s.length() -1);
        if(s2.length() == 0){
            return false ;
        }
        if(isNum(s2)){
            switch (s.charAt(s.length() - 1)){
                case 'F': case'D': case'I': case 'L':return true ;
                default:return false ;
            }
        }
        return false ;
    }

    private static boolean isNum(String s2) {
        boolean dot = false ;
        for(int i = 0 ; i < s2.length() ; i++){
            switch (s2.charAt(0)) {
                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case'7': case'8': case'9' :
                    break;
                case '.' :
                    if(!dot){
                        dot = true ;
                    } else {
                        return false ;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true ;
    }

    private static boolean matches(String s, ArrayList<Token> x, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> Mi) {
        ArrayList<String> params = getParam(s);
        if(params.size() != x.size()){
            return false ;
        }
        for(int i = 0 ; i < params.size() ; i++){
            if(! potType(params.get(i) , x.get(i) , vars , isStatic , objects , Mi)){
                return false ;
            }
        }
        return true ;
    }

    private static boolean matches(String s, Token x, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> Mi) {
        return potType(s , x , vars , isStatic , objects , Mi) ;
    }

    private static boolean expType(String s, Expression ex, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI){
        for(Token t : ex.tokens){
            switch (t.getToken()){
                case Expression:
                    Expression expression ;
                    try {
                        expression = (Expression) t.getValue();
                    } catch (Exception e){
                        //e.printStackTrace();
                        expression = new Expression((ArrayList<Token>) t.getValue());
                    }
                    if(!expType(s , expression, vars , isStatic, objects , MI)){
                        return false ;
                    }
                    break;
                case StringVal:
                case ObjectDeclaration:
                case ArrayDeclaration:
                case ArrayDeclaration2:
                case FunctionCall:
                case ArrayCall:
                case Value:
                case Object:
                case IfThenElse:
                    if(! potType(s , t , vars , isStatic , objects , MI)){
                        return false ;
                    }
                    break;
                case LThan:
                case LThanEq:
                case GThan:
                case GThanEq:
                case NotEqualTo:
                case EqualTo:
                    return s.equals("Z") ;
            }
        }
        return true;
    }

    public static boolean isString(Expression ex, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> MI) {
        for(Token t : ex.tokens){
            switch (t.getToken()){
                case Expression:
                case FunctionCall:
                case ArrayCall:
                case Object:
                case ObjectDeclaration:
                    if(potType("Ljava/lang/String;",t , vars , isStatic,objects,MI)){
                        return true ;
                    }
                    break;
                case StringVal:
                    return true ;
                case Value:
                    String s = (String) t.getValue() ;
                    if(s.startsWith("\"")){
                        return true ;
                    }
                    for(ExlVariable var : vars){
                        if(var.getName().equals(s)) {
                            if(var.getType() == TokenT.String || (var.getType() == TokenT.Object && var.getObjName().equals("Ljava/lang/String;"))){
                                return true;
                            }
                        }
                    }
                    break;
                case Minus:
                case Mul:
                case Div:
                    return false ;
            }
        }
        return false ;
    }

    public static String getType(Token t , ArrayList<ExlClassInfo>objects , ArrayList<ExlVariable> vars){
        try {
            return getP(inferType(t, objects, vars));
        }catch (Exception e){
            //e.printStackTrace();
            System.exit(2);
            return "";
        }
    }

    static boolean checkCast(Token t , StatementType st){
        switch (st){
            case String: return t.getToken() == TokenT.String;
            case Int: return t.getToken() == TokenT.Int;
            case Float: return t.getToken() == TokenT.Float;
            case Long: return t.getToken() == TokenT.Long;
            case Double: return t.getToken() == TokenT.Double;
            case Char: return t.getToken() == TokenT.Char;
            case Boolean: return t.getToken() == TokenT.Boolean;
        }
        return false ;
    }

    static boolean checkCast(Token t , TokenT tt){
        switch (tt){
            case String: return t.getToken() == TokenT.String;
            case Int: return t.getToken() == TokenT.Int;
            case Float: return t.getToken() == TokenT.Float;
            case Long: return t.getToken() == TokenT.Long;
            case Double: return t.getToken() == TokenT.Double;
            case Char: return t.getToken() == TokenT.Char;
            case Boolean: return t.getToken() == TokenT.Boolean;
        }
        return false ;
    }

    static boolean checkCast(Token t , String s){
        switch (s){
            case "Ljava/lang/String;" : return t.getToken() == TokenT.String;
            case "I": return t.getToken() == TokenT.Int;
            case "F": return t.getToken() == TokenT.Float;
            case "J": return t.getToken() == TokenT.Long;
            case "D" : return t.getToken() == TokenT.Double;
            case "C": return t.getToken() == TokenT.Char;
            case "Z": return t.getToken() == TokenT.Boolean;
        }
        return false ;
    }

    public static boolean isType(Token t , StatementType s , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars, ArrayList<MethodInfo> mi){
        if(t.getToken() == TokenT.Expression){
            try {
                Expression e = (Expression) t.getValue();
                if(e.size() == 2 && e.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) e.get(0).getValue();
                    if(checkCast(ts,s)){
                        return true ;
                    }
                }
            } catch (Exception e){
                ArrayList<Token> es = (ArrayList<Token>) t.getValue();
                if(es.size() == 2 && es.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) es.get(0).getValue();
                    if(checkCast(ts,s)){
                        return true ;
                    }
                }
            }
        }
        String st = "";
        switch (s){
            case Float:st = "F";break;
            case Char:st = "C";break;
            case Int:st = "I";break;
            case Long:st = "J";break;
            case Double:st = "D";break;
            case String:st = "Ljava/lang/String;";break;
        }
        String desc = calcDesc2(st , t , vars , true ,  objects , mi);
        return desc != "";
    }

    public static boolean isType(Token t , TokenT s , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars, ArrayList<MethodInfo> mi){
        if(t.getToken() == TokenT.Expression){
            try {
                Expression e = (Expression) t.getValue();
                if(e.size() == 2 && e.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) e.get(0).getValue();
                    if(checkCast(ts,s)){
                        return true ;
                    }
                }
            } catch (Exception e){
                ArrayList<Token> es = (ArrayList<Token>) t.getValue();
                if(es.size() == 2 && es.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) es.get(0).getValue();
                    if(checkCast(ts,s)){
                        return true ;
                    }
                }
            }
        }
        String st = "";
        switch (s){
            case Boolean:st = "Z";break;
            case Float:st = "F";break;
            case Char:st = "C";break;
            case Int:st = "I";break;
            case Long:st = "J";break;
            case Double:st = "D";break;
            case String:st = "Ljava/lang/String;";break;
            case Object:st = "Ljava/lang/Object;";
        }
        String desc = calcDesc2(st , t , vars , true ,  objects , mi);
        return desc != "";
    }

    public static boolean isType(Token t , String st, ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars, ArrayList<MethodInfo> mi){
        if(t.getToken() == TokenT.Expression){
            try {
                Expression e = (Expression) t.getValue();
                if(e.size() == 2 && e.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) e.get(0).getValue();
                    if(checkCast(ts,st)){
                        return true ;
                    }
                }
            } catch (Exception e){
                ArrayList<Token> es = (ArrayList<Token>) t.getValue();
                if(es.size() == 2 && es.get(0).getToken() == TokenT.Cast){
                    Token ts = (Token) es.get(0).getValue();
                    if(checkCast(ts,st)){
                        return true ;
                    }
                }
            }
        }
        String desc = calcDesc2(st , t , vars , true ,  objects , mi);
        return desc != "";
    }

    public static ArrayList<String> possibleType(Token t, ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars){
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<String> pDesc = new ArrayList<>();
        pDesc.add("I");
        pDesc.add("F");
        pDesc.add("J");
        pDesc.add("D");
        pDesc.add("Z");
        pDesc.add("C");
        pDesc.add("Ljava/lang/String;");
        pDesc.add("Ljava/lang/Object;");
        for(String des : pDesc){
            String desc = calcDesc2(des , t , vars , true ,  objects , null);
            if(!desc.equals("")){
                ret.add(desc) ;
            }
        }
        return ret ;
    }

    public static String inferType(Token t, ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars) throws Exception {
        if(t.getToken() == TokenT.ArrayCall){
            ArrayCall ac = (ArrayCall) t.getValue();
            if(!ac.isBasic()){
                if(ac.getType().equals("D")){
                    return "(D)V";
                } else {
                    return "(I)V";
                }
            }
        }
        ArrayList<Token> statement = new ArrayList<>();
        statement.add(t);
        Expression ex = new Expression(statement);
        statement = new ArrayList<>();
        ArrayList<String> pDesc = new ArrayList<>();
        pDesc.add("(Z)V");
        pDesc.add("(I)V");
        pDesc.add("(F)V");
        pDesc.add("(J)V");
        pDesc.add("(D)V");
        pDesc.add("(C)V");
        pDesc.add("(Ljava/lang/String;)V");
        pDesc.add("(Ljava/lang/Object;)V");
        statement.add(new Token(TokenT.Expression, ex, t.getLine()));
        String desc = calcDesc(pDesc, statement, vars, true, "Print", "Print", objects, null);
        return desc;
    }

    public static String inferType(Expression p , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> methods , ArrayList<ExlVariable> vars, boolean isStatic , ArrayList<ExlError> errors){
        ArrayList<Token> statement = new ArrayList<>();
        ArrayList<String> pDesc = new ArrayList<>();
        pDesc.add("(Z)V");
        pDesc.add("(I)V");
        pDesc.add("(F)V");
        pDesc.add("(J)V");
        pDesc.add("(D)V");
        pDesc.add("(C)V");
        pDesc.add("(Ljava/lang/String;)V");
        pDesc.add("(Ljava/lang/Object;)V");
        statement.add(new Token(TokenT.Expression , p, p.tokens.get(0).getLine()));
        String desc = calcDesc3(pDesc , statement , vars , isStatic , objects , methods , errors);
        return desc ;
    }

    private static String calcDesc3(ArrayList<String> pDesc, ArrayList<Token> statement, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> methods, ArrayList<ExlError> errors) {
        for (String s : pDesc) {
            String sr = getP(s);
            if (matches(sr, statement, vars, isStatic, objects, methods)) {
                return s;
            }
        }
        return null ;
    }
    private static String calcDesc(ArrayList<String> pDesc, ArrayList<Token> statement, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> methods, ArrayList<ExlError> errors) {
        for (String s : pDesc) {
            String sr = getP(s);
            if (matches(sr, statement , vars , isStatic , objects , methods)) {
                return s;
            }
        }
        errors.add(new ExlError(statement.get(0).getLine() ,"Cannot find a type for expression " + PPT.prettyPrint(statement.get(0))));
        return null ;
    }

    private static boolean potType(String s, Token token, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI) {
        String val ;
        if(s.equals("Ljava/lang/Object;")){
            return true ;
        }
        switch (token.getToken() ) {
            case Value:
                val = (String) token.getValue();
                if(isNWL(val)){
                    switch (val.charAt(val.length() -1) ){
                        case 'L' : return s.charAt(0) == 'J';
                        case 'D' : return s.charAt(0) == 'D';
                        case 'F' : return s.charAt(0) == 'F';
                        case 'I' : return s.charAt(0) == 'I';
                        default: return false ;
                    }
                }
                if (isVar(val, vars)) {
                    ExlVariable var = gVar(val, vars);
                    if(var.isArray() && s.equals("Ljava/lang/Object;")){
                        return true ;
                    }
                    if (var.getType() == TokenT.Object) {
                        if(s.equals("Ljava/lang/Object;")){
                            return true  ;
                        }
                        return s.equals("L" + getAddress(var.getObjName() , objects) + ";");
                    }
                    return s.equals(getTYpe(var , objects,token.getLine())) ;
                }
                switch (s.charAt(0)) {
                    case 'I':
                        try {
                            Integer.parseInt(val);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    case 'F':
                        try {
                            Float.parseFloat(val);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    case 'C':
                        return isChar(val);
                    case 'J':
                        try {
                            Long.parseLong(val);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    case 'D':
                        try {
                            Double.parseDouble(val);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    case 'Z':
                        return isBool(val);
                    case 'L':
                        return isObject(val , s);
                    case '[':
                        return false ;
                    default:
                        throw new Error("CONFUSED POT TYPE");
                }
            case Expression:
                return expType(s, (Expression) token.getValue(), vars , isStatic , objects , MI);
            case FunctionCall:
                FunctionCall fc = (FunctionCall) token.getValue();
                String type = getRType(fc.getDesc());
                if(type.startsWith("[") && s.equals("Ljava/lang/Object;")){
                    return true ;
                }
                return type.equals(s);
            case ArrayCall:
                ArrayCall ac = (ArrayCall) token.getValue() ;
                if(ac.getType() == null){
                    return false;
                }
                return ac.getType().equals(s) || s.equals("Ljava/lang/Object;") && (ac.getType().charAt(0) == 'L' || ac.getType().charAt(0) == '[') ;
            case ArrayDeclaration:
                ArrayDeclaration ad = (ArrayDeclaration) token.getValue() ;
                return ad.getType().equals(s);
            case ArrayDeclaration2:
                ArrayDeclaration2 ad2 = (ArrayDeclaration2) token.getValue() ;
                return checkAD2(ad2.getValues() , s , objects, vars,MI);
            case Object:
                ArrayList<ObjectRef> or = (ArrayList<ObjectRef>) token.getValue() ;
                ObjectRef oR = or.get(or.size() - 1);
                return oR.getType().equals(s);
            case StringVal:
                return s.equals("Ljava/lang/String;") || s.equals("Ljava/lang/Object;");
            case ObjectDeclaration:
                if(s.equals("Ljava/lang/Object;")){
                    return true ;
                }
                ObjectDeclaration od = (ObjectDeclaration) token.getValue() ;
                String s2 = "L" + od.getAddress() + ";" ;
                return s2.equals(s);
            case BoolExpression:
                return s.equals("Z");
            case IfThenElse:
                IfThenElse ite  = (IfThenElse) token.getValue();
                return isType(ite.getThen() , s , objects , vars, MI) && isType(ite.getElse() , s , objects , vars, MI) ;
        }
        return false ;
    }

    public static boolean checkAD2(ArrayList<Token> values, String s , ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars , ArrayList<MethodInfo> mi) {
        for (Token t : values) {
            if(s.length()  < 2){
                return false ;
            }
            if (s.charAt(1) == '[') {
                return isType(t , s.substring(1), objects , vars, mi);
            } else {
                if (!isType(t, s.substring(1), objects, vars, mi)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<String> getPDesc(String fName, boolean isStatic, ArrayList<MethodInfo> methods) {
        ArrayList<String> s = new ArrayList<>() ;
        for(MethodInfo m : methods){
            if(m.getName().equals(fName) && m.getIsStatic() == isStatic) {
                s.add(m.getDescription());
            }
        }
        return  sortDesc(s) ;
    }

    private static ArrayList<String> sortDesc(ArrayList<String> s) {
        if(allL(s)){
            return s ;
        }
        for(int i = 0 ; i < s.size() ; i++){
            if(s.get(0).charAt(1) == 'L'){
                s.add(s.get(0));
                s.remove(0);
                i -- ;
            }
        }
        return s ;
    }

    private static boolean allL(ArrayList<String> s) {
        for(String sr : s){
            if(! (sr.charAt(1) == 'L')){
                return false ;
            }
        }
        return true ;
    }

    public static boolean isStatic(String desc, ArrayList<MethodInfo> methods) {
        for(MethodInfo mi : methods){
            if(desc.equals(mi.getDescription())){
                return mi.getIsStatic() ;
            }
        }
        return false ;
    }

    private static String getRType(String s) {
        for(int i = 0 ; i < s.length() ; i++){
            if(s.charAt(i )  == ')'){
                return s.substring(i+1);
            }
        }
        return null ;
    }

    private static ExlVariable gVar(String val, ArrayList<ExlVariable> vars) {
        for(ExlVariable var : vars){
            if(var.getName().equals(val)){
                return var ;
            }
        }
        return null ;
    }

    public static ArrayList<Token> fixParam(ArrayList<Token> x, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> mi) throws Exception {
        ArrayList<Token> expr = new ArrayList<>() ;
        ArrayList<Token> nList = new ArrayList<>() ;
        for(int i = 0 ; i < x.size(); i++) {
            switch (x.get(i).getToken()) {
                case Comma:
                    Expression ex = new Expression(expr);
                    ex.simplfy(objects, vars, isStatic, mi);
                    nList.add(new Token(TokenT.Expression, ex, x.get(0).getLine()));
                    expr = new ArrayList<>();
                    break;
                case LBracket:
                case LSquare:
                    Tuple<ArrayList<Token>, Integer> s = collectBracket(x, i, x.get(i).getToken());
                    expr.add( x.get(i));
                    expr.addAll(s.x);
                    i = s.y;
                    expr.add( x.get(i));
                    break;
                default:
                    expr.add(x.get(i));

            }
        }
        if(expr.size() > 0 ){
            Expression ex = new Expression(expr);
            ex.simplfy(objects , vars ,isStatic , mi);
            nList.add(new Token(TokenT.Expression , ex , ex.tokens.get(0).getLine()));
        }
        return nList ;
    }

    public static ArrayList<Token> splitOnComma(ArrayList<Token> x) throws Exception {
        ArrayList<Token> expr = new ArrayList<>() ;
        ArrayList<Token> nList = new ArrayList<>() ;
        for(int i = 0 ; i < x.size(); i++) {
            switch (x.get(i).getToken()) {
                case Comma:
                    Expression ex = new Expression(expr);
                    nList.add(new Token(TokenT.Expression, ex , ex.tokens.get(0).getLine()));
                    expr = new ArrayList<>();
                    break;
                case LBracket:
                case LSquare:
                    Tuple<ArrayList<Token>, Integer> s = collectBracket(x, i, x.get(i).getToken());
                    expr.add( x.get(i));
                    expr.addAll(s.x);
                    i = s.y;
                    expr.add( x.get(i));
                    break;
                default:
                    expr.add(x.get(i));

            }
        }
        if(expr.size() > 0 ){
            Expression ex = new Expression(expr);
            nList.add(new Token(TokenT.Expression , ex, ex.tokens.get(0).getLine()));
        }
        return nList ;
    }

    public static Boolean validVarName(String name)  {
        char firstLetter = name.charAt(0);
        if(name.equals("true") || name.equals("false")){
            return false ;
        }
        return ! (firstLetter == '\'' || firstLetter == '\"' || (firstLetter > 47 && firstLetter < 58)  || firstLetter == '-') ;
    }

    private static ArrayList<String> getParam(String s) {
        ArrayList<String> sr = new ArrayList<>();
        String temp = "" ;
        int i = 0 ;
        while (i < s.length()){
            switch (s.charAt(i)){
                case 'I': case 'F': case 'C': case 'Z': case 'J':
                case 'D': sr.add(temp + s.charAt(i)); temp = "" ; break;
                case 'L':
                    String st = "";
                    while (i < s.length() && s.charAt(i) != ';'){
                        st += s.charAt(i++) ;
                    }
                    sr.add(temp + st + ";");
                    temp = "" ;
                    break;
                case '[':
                    temp += "[";
                    break;
                default:
                    throw new Error("CAN't work out type");
            }
            i++ ;
        }
        return sr;
    }

    public static ArrayList<Token> checkPattern(ConstFunc cf, int i, ArrayList<Token> tokens,HashMap<String , ConstFunc> cfs , HashMap<String , String> Const , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects ) throws Exception {
        HashMap<String , Token> swaps = new HashMap<>();
        if(cf.startsWithVar()){
            i-- ;
        }
        int y = i ;
        for(Token t : cf.getPattern()){
            if(t.getToken() != TokenT.Var){
                if(! t.getValue().equals(tokens.get(i).getValue())){
                    return tokens ;
                }
            } else{
                if(tokens.get(i).getToken() == TokenT.LBracket){
                    Tuple<ArrayList<Token> , Integer> das = collectBracket(tokens,i, TokenT.LBracket);
                    Expression p = new Expression(das.x);
                    p.tidy(cfs, Const, vars, isStatic, MI, objects);
                    swaps.put((String) t.getValue(), new Token(TokenT.Expression, p,tokens.get(0).getLine()));
                    i = das.y;
                }else if(tokens.get(i).getToken() == TokenT.RBracket){
                    Tuple<ArrayList<Token> , Integer> das = collectBracketRev(tokens,i, TokenT.RBracket);
                    y = das.y ;
                    Expression p = new Expression(das.x);
                    p.tidy(cfs, Const, vars, isStatic, MI, objects);
                    swaps.put((String) t.getValue(), new Token(TokenT.Expression, p , tokens.get(0).getLine()));
                }else {
                    swaps.put((String) t.getValue(), tokens.get(i));
                }
            }
            i++ ;
        }
        ArrayList<Token> temp1;
        if(y > 0) {
            temp1 = new ArrayList<>(tokens.subList(0, y ));
        } else {
            temp1 = new ArrayList<>();
        }
        ArrayList<Token> temp2 = new ArrayList<>(tokens.subList(i , tokens.size()));
        temp1.addAll(makeResult(cf.getResult() , swaps));
        temp1.addAll(temp2);

        return temp1;
    }

    public static Tuple<ArrayList<Token>,Integer> collectBracketRev(ArrayList<Token> tokens, int startAt, TokenT typeOfBrac) throws Exception {
        ArrayList<Token> tokens1 = new ArrayList<>();
        if(tokens.get(startAt).getToken() != typeOfBrac) {
            throw new java.lang.Exception("Collect Brackets should start with a LBracket");
        }
        int openBrackets = 0 ;
        startAt-- ;
        TokenT otherBrac = TokenT.LBracket;
        switch (typeOfBrac){
            case RBracket: break;
            case RSquare: otherBrac = TokenT.LSquare ;break;
            case RBrace: otherBrac = TokenT.LBrace ;break;
            default:
                throw new Error("collectBracketRev needs a bracket");
        }
        for(int i = startAt ; i > -1  ; i-- ) {
            Token t = tokens.get(i);
            if (t.getToken() == otherBrac) {
                if (openBrackets == 0) {
                    return new Tuple<>(tokens1, i);
                } else {
                    openBrackets -= 1;
                    tokens1.add(0,t) ;
                }
            } else if (t.getToken() == typeOfBrac) {
                openBrackets += 1;
                tokens1.add(0,t) ;
            } else {
                tokens1.add(0,t) ;
            }
        }
        throw new java.lang.Exception("Unable to find the closing bracket for " + PPT.prettyPrint(tokens.get(startAt - 1)) + " located at \n\t" + PPT.prettyPrintLine(new ArrayList<>(tokens.subList(0 , startAt))));
    }

    private static ArrayList<Token> makeResult(ArrayList<Token> result, HashMap<String, Token> swaps) {
        ArrayList<Token> newTokens = new ArrayList<>();
        boolean b = false;
        for(Token t : result){
            if(t.getToken() == TokenT.Value){
                for(String s : swaps.keySet()){
                    if(t.getValue().equals(s)){
                        newTokens.add(swaps.get(s));
                        b = true;
                        break;
                    }
                }
                if(!b){
                    newTokens.add(t);
                }
                b = false ;
            } else {
                newTokens.add(t);
            }
        }
        return newTokens;
    }

    public static TokenT getHighestType(TokenT type , TokenT type2){
        HashMap<TokenT , Integer> typeVal = new HashMap<>();
        typeVal.put(TokenT.Int , 1);
        typeVal.put(TokenT.Boolean , 1);
        typeVal.put(TokenT.Char, 1);
        typeVal.put(TokenT.Float, 2);
        typeVal.put(TokenT.Long, 3);
        typeVal.put(TokenT.Double, 4);
        typeVal.put(TokenT.String, 5);
        typeVal.put(TokenT.Object, 6);

        if(typeVal.get(type2) > typeVal.get(type)){
            return type2;
        }
        return type ;
    }

    public static String inferPrint(Expression p, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> methods, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlError> errors) {
        ArrayList<Token> statement = new ArrayList<>();
        ArrayList<String> pDesc = new ArrayList<>();
        pDesc.add("(Z)V");
        pDesc.add("(I)V");
        pDesc.add("(F)V");
        pDesc.add("(J)V");
        pDesc.add("(D)V");
        pDesc.add("(C)V");
        pDesc.add("(Ljava/lang/String;)V");
        pDesc.add("(Ljava/lang/Object;)V");
        statement.add(new Token(TokenT.Expression , p, p.tokens.get(0).getLine()));
        String desc = calcDesc3(pDesc , statement , vars , isStatic , objects , methods , errors);
        if(desc == null){
            return "(Ljava/lang/Object;)V" ;
        }
        return desc ;
    }
}

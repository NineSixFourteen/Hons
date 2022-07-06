package src.BuildingBlocks.Values;

import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.Info.FieldInfo;
import src.BuildingBlocks.Info.MethodInfo;
import src.BuildingBlocks.ToolKit;
import src.BuildingBlocks.parserTypes.ExlVariable;
import src.BuildingBlocks.tools.ConstFunc;
import src.BuildingBlocks.tools.Tuple;
import src.BuildingBlocks.tools.functionScope;
import src.Parser.PPT;
import src.Parser.Validate;
import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Expression {

     public ArrayList<Token> tokens ;

    public Expression(ArrayList<Token> tok){
        tokens = tok ;
    }

    public Expression(List<Token> tok){
        tokens = new ArrayList<>(tok) ;
    }

    public int size(){
        return tokens.size() ;
    }

    public Token get(int i){
        return tokens.get(i);
    }

    public void switchT(HashMap<String , ConstFunc> cfs , HashMap<String , String> Const , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects ) throws Exception {
        switchOut(Const);
        for(int i = 0 ; i < 8; i++){
            SwitchFunc(cfs, Const, vars, isStatic, MI, objects);
        }
        removeExpression();
    }

    public void tidy(HashMap<String , ConstFunc> cfs , HashMap<String , String> Const , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects ) throws Exception {
        switchOut(Const);
        for(int i = 0 ; i < 8; i++){
            SwitchFunc(cfs, Const, vars, isStatic, MI, objects);
        }
        removeExpression();
        simplfy(objects,vars,isStatic,MI);
    }

    private void removeExpression() {
        ArrayList<Token> toks = new ArrayList<>();
        for(Token t : tokens){
            if(t.getToken() == TokenT.Expression){
                Expression p = (Expression) t.getValue();
                if(p.size() == 1){
                    toks.add(p.get(0));
                } else {
                    toks.add(t);
                }
            } else {
                toks.add(t);
            }
        }
        tokens = toks ;
    }

    public void SwitchFunc(HashMap<String , ConstFunc> cfs , HashMap<String , String> Const , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects ) throws Exception {
        tokens = SwitchFun(cfs, Const, vars, isStatic, MI, objects);
    }

    public ArrayList<Token> SwitchFun(HashMap<String , ConstFunc> cfs , HashMap<String , String> Const , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects ) throws Exception {
        for(int i = 0 ; i < tokens.size() ; i++){
            Token t = tokens.get(i);
            if(t.getToken() == TokenT.Value ){
                String val = (String) t.getValue();
                for(String s : cfs.keySet()){
                    if(val.equals(s)){
                        return tokens = ToolKit.checkPattern(cfs.get(s) , i , tokens , cfs , Const , vars , isStatic , MI , objects);
                    }
                }
            }
        }
        return tokens;
    }

    public void switchOut(HashMap<String , String> Const){
        for(int i = 0 ; i < tokens.size() ; i++){
            Token t = tokens.get(i);
            if(t.getToken() == TokenT.Value ){
                String val = (String) t.getValue();
                for(String s : Const.keySet()){
                    if(val.equals(s)){
                        tokens.set(i , new Token(TokenT.Value , Const.get(s),tokens.get(0).getLine()));
                    }
                }
            }
        }
    }

    public void simplfy(ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> mi ) throws Exception {
        tokens = simplfyExpression(tokens  ,vars , isStatic , mi , objects );
    }

    public void simplfyNOBOOl(ArrayList<ExlClassInfo> objects , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI ) throws Exception {
        tokens = addCast(tokens, objects);
        tokens = addAD2Call(tokens , vars , isStatic , objects , MI);
        tokens = addFCall(tokens, vars, isStatic, objects, MI);
        tokens = addODCall(tokens, vars, isStatic , objects , MI);
        tokens = addACall(tokens, vars, isStatic , objects , MI);
        tokens = addOCall(tokens, vars, isStatic, objects, MI);
        tokens = removeBrackets(tokens);
        tokens = reduceToAdd(tokens);
        tokens = addITECall(tokens, vars, isStatic, objects, MI);
        tokens = addSCall(tokens, vars, isStatic, objects, MI);
    }

    private ArrayList<Token> addCast(ArrayList<Token> tokens, ArrayList<ExlClassInfo> objects) {
        if(tokens.size() > 2 && tokens.get(0).getToken() == TokenT.LBracket && tokens.get(2).getToken() == TokenT.RBracket){
            switch (tokens.get(1).getToken()){
                case String:
                case Boolean:
                case Char:
                case Long:
                case Array:
                case Double:
                case Float:
                case Int:
                    tokens.add(0 , new Token(TokenT.Cast , tokens.get(1) , tokens.get(0).getLine()) );
                    tokens.remove(1);
                    tokens.remove(1);
                    tokens.remove(1);
                    break;
                case Value:
                    String s = (String) tokens.get(1).getValue();
                    if(ToolKit.isObject2(s , objects)){
                        String l = "L" + ToolKit.getAddress(s, objects) + ";" ;
                        tokens.add(0 , new Token(TokenT.Cast ,new Token(TokenT.Value , l) , tokens.get(0).getLine()) );
                        tokens.remove(1);
                        tokens.remove(1);
                        tokens.remove(1);
                    }
                    break;
            }
        }
        return tokens;
    }

    private ArrayList<Token> addITECall(ArrayList<Token> tokens, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> mi) throws Exception {
        boolean b = false ;
        ArrayList<Token> newTokens = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++){
            if(tokens.get(i).getToken() == TokenT.If){
                Expression If ;
                Expression Then ;
                Tuple<ArrayList<Token> , Integer> IF = ToolKit.tokensUpTo(tokens , i + 1 , TokenT.Then );
                If = new Expression(IF.x);
                If.simplfy(objects,vars,isStatic,mi);
                i = IF.y + 1 ;
                Token type = null ;
                Tuple<ArrayList<Token> , Integer> THEN = ToolKit.tokensUpTo(tokens , i , TokenT.Else );
                Then = new Expression(THEN.x);
                Then.simplfy(objects,vars,isStatic,mi);
                switch (Then.tokens.get(0).getToken()){
                    case Object:
                        ArrayList<ObjectRef> ors = (ArrayList<ObjectRef>) Then.tokens.get(0).getValue();
                        type  = ToolKit.StringToToken(ors.get(ors.size() - 1).getType(), If.tokens.get(0).getLine());
                        break;
                    case ObjectDeclaration:
                        ObjectDeclaration od = (ObjectDeclaration) Then.tokens.get(0).getValue() ;
                        type = ToolKit.StringToToken("L" + od.getAddress() + ";", Then.tokens.get(0).getLine() );
                        break;
                    case ArrayDeclaration:
                        ArrayDeclaration ad = (ArrayDeclaration) Then.tokens.get(0).getValue() ;
                        type = ToolKit.StringToToken(ad.getType() , Then.tokens.get(0).getLine()) ;
                        break;
                    case Value:
                        for(ExlVariable var : vars){
                            if(var.getName().equals(Then.tokens.get(0).getValue())){
                                type = ToolKit.StringToToken(ToolKit.getTYpe(var , objects , Then.tokens.get(0).getLine()) , Then.tokens.get(0).getLine());
                            }
                        }
                }
                i = THEN.y + 1 ;
                Expression Else = new Expression(new ArrayList<>(tokens.subList(i,tokens.size())));
                Else.simplfy(objects,vars,isStatic,mi);
                IfThenElse ite ;
                if(type == null ) {
                    ite = new IfThenElse(new Token(TokenT.Expression, If, tokens.get(0).getLine()), new Token(TokenT.Expression, Then, tokens.get(0).getLine()), new Token(TokenT.Expression, Else, tokens.get(0).getLine()));
                } else {
                    ite = new IfThenElse(new Token(TokenT.Expression, If, tokens.get(0).getLine()), new Token(TokenT.Expression, Then, tokens.get(0).getLine()), new Token(TokenT.Expression, Else, tokens.get(0).getLine()), type);
                }
                b = true ;
                i = tokens.size();
                ArrayList<ExlError> errors = new ArrayList<>();
                errors = Validate.validateBoolean(If.tokens,errors,objects,vars);
                if(errors.size() > 0){
                    throw new Exception(errors.get(0).getReason());
                }
                newTokens.add(new Token(TokenT.IfThenElse , ite,tokens.get(0).getLine()));
            } else {
                newTokens.add(tokens.get(i));
            }
        }
        if(b && newTokens.size() > 1){
            throw new Exception("If _ Then _ Else _ cannot be used outside of being the only element in a variable assignment");
        }
        return newTokens ;
    }

    private ArrayList<Token> simplfyExpression(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<MethodInfo> MI , ArrayList<ExlClassInfo> objects) throws Exception {
        if(isBooleanExpr(tokens)){
            return simplifyBoolean(tokens , vars , isStatic, objects , MI );
        }else {
            if(tokens.size() > 0) {
                if(tokens.get(0).getToken() == TokenT.If){
                    tokens = addITECall(tokens, vars, isStatic, objects, MI);
                }
                tokens = addCast(tokens, objects);
                tokens = addAD2Call(tokens, vars, isStatic, objects, MI);
                tokens = addFCall(tokens, vars, isStatic, objects, MI);
                tokens = addODCall(tokens, vars, isStatic, objects, MI);
                tokens = addACall(tokens, vars, isStatic, objects, MI);
                tokens = addOCall(tokens, vars, isStatic, objects, MI);
                tokens = removeBrackets(tokens);
                tokens = reduceToAdd(tokens);
                tokens = addSCall(tokens, vars, isStatic, objects, MI);
                //tokens = optimise(tokens);
            }
            return tokens;
        }
    }
    //TODO IT
    private ArrayList<Token> addSCall(ArrayList<Token> tokens, ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> mi) {
        Expression ex = new Expression(tokens);
        if(ToolKit.isString(ex, vars,isStatic,objects,mi) && tokens.size() > 1 && tokens.get(0).getToken() != TokenT.Cast && tokens.get(tokens.size() - 1).getToken() != TokenT.SemiColan){
            StringVal sv = new StringVal(tokens);
            tokens = new ArrayList<>();
            tokens.add(new Token(TokenT.StringVal , sv , sv.parts.get(0).getLine()));
        }
        return tokens ;
    }

    private ArrayList<Token> optimise(ArrayList<Token> tokens) {
        double total = 0 ;
        ArrayList<Token> others = new ArrayList<>();
        for(int i = 0 ; i < tokens.size();i++){
            switch (tokens.get(i).getToken()){
                case Value:
                    if(isNotVar((String) tokens.get(i).getValue())){
                        String s = (String) tokens.get(i).getValue();
                        double d = Double.parseDouble(s);
                        if(i > 0){
                            switch(tokens.get(i - 1).getToken()){
                                case Plus:
                                    total += d ;
                                    break;
                                case Minus:
                                    total -= d ;
                                    break;
                                case Div:
                                    total /= d ;
                                    break;
                                case Mul:
                                    total *= d;
                                    break;
                            }
                        } else{
                            total += d ;
                        }
                    } else {
                        if(i > 0){
                            others.add(tokens.get(i - 1));
                        }
                        others.add(tokens.get(i));
                    }
                break;
                case Expression:
                    Expression ex = (Expression) tokens.get(i).getValue();
                    ex.tokens = optimise(ex.tokens);
                    if(ex.size() == 1){
                        Token t = ex.get(0);
                        others.add(tokens.get(i - 1));
                        others.add(t);
                    }
                    break;
                case ArrayCall:
                case FunctionCall:
                case Object:
                case IfThenElse:
                case ArrayLength:
                    if(i > 0){
                        others.add(tokens.get(i - 1));
                    }
                    others.add(tokens.get(i));
            }
        }
        if(total % 1 == 0 ){
            String sts = ""+ total;
            int i = Integer.parseInt(sts.substring(0 , sts.length() - 2));
            others.add(0, new Token(TokenT.Value, "" + i, tokens.get(0).getLine()));
        } else {
            others.add(0, new Token(TokenT.Value, "" + total,tokens.get(0).getLine()));
        }
        if(others.size() > 2) {
            switch (others.get(1).getToken()) {
                case Plus:
                case Minus:
                case Mul:
                case Div:
                    break;
                default:
                    tokens.add(1, new Token(TokenT.Plus, null,tokens.get(0).getLine()));
            }
        }
        return others ;
    }

    private boolean isNotVar(String value) {
        try{
            Double.parseDouble(value);
            return true ;
        } catch (Exception e){
            return false ;
        }
    }

    private Tuple<ArrayList<Token>, Integer> collectString(ArrayList<Token> tokens, int i) {
        if(i+ 1 < tokens.size() && tokens.get(i + 1).getToken() == TokenT.Plus){
            i++;
            ArrayList<Token> tok = new ArrayList<>();
            while(i < tokens.size() && tokens.get(i).getToken() == TokenT.Plus){
                tok.add(tokens.get(i - 1));
                i += 2 ;
            }
            tok.add(tokens.get(i - 1));
            return new Tuple<>(tok ,i);
        } else {
            return new Tuple<>(new ArrayList<>(tokens.subList(i,i+1))  ,i+ 1);
        }
    }

    //FunctionCall
    public ArrayList<Token> addFCall(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic2 , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++) {
            if(i == tokens.size()-1){
                newTokens.add(tokens.get(i));
                break;
            }
            Token t = tokens.get(i);
            Token t1 = tokens.get(i + 1) ;
            if(t.getToken() == TokenT.Value && t1.getToken() == TokenT.LBracket) {
                if(i > 0 && (tokens.get(i - 1).getToken() == TokenT.Dot || tokens.get( i - 1).getToken() == TokenT.New) ) {
                    newTokens.add(t);
                } else {
                    Tuple<ArrayList<Token>, Integer> x = ToolKit.collectBracket(tokens, i + 1, TokenT.LBracket);
                    ArrayList<Token> res = ToolKit.fixParam(x.x, vars, isStatic2 , objects , MI);
                    ArrayList<String> pDesc = ToolKit.getPDesc((String) t.getValue(), isStatic2 , MI);
                    String desc = ToolKit.calcDesc(pDesc, res, vars, isStatic2, "this " , (String) t.getValue() , objects , MI);
                    boolean isStatic = ToolKit.isStatic(desc , MI);
                    FunctionCall fc = new FunctionCall((String) t.getValue(), res, desc, isStatic);
                    newTokens.add(new Token(TokenT.FunctionCall, fc,tokens.get(0).getLine()));
                    i = x.y;
                }
            } else {
                newTokens.add(t);
            }
        }
        return newTokens ;
    }

    //ArrayDeclaration
    private ArrayList<Token> addAD2Call(ArrayList<Token> tokens, ArrayList<ExlVariable> vars, boolean isStatic , ArrayList<ExlClassInfo> Objects , ArrayList<MethodInfo> mi) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        ArrayList<Token> tokens1 = new ArrayList<>();
        ArrayList<Token> expr = new ArrayList<>();
        if(tokens.get(0).getToken() == TokenT.LSquare){
            Tuple<ArrayList<Token> , Integer> x = ToolKit.collectBracket(tokens , 0 , TokenT.LSquare);
            for(int i = 0 ; i < x.x.size(); i++){
                switch (x.x.get(i).getToken()){
                    case LBrace:
                    case LBracket:
                    case LSquare:
                        tokens1.add(x.x.get(i));
                        Tuple<ArrayList<Token> , Integer> y = ToolKit.collectBracket(x.x , i , x.x.get(i).getToken()) ;
                        tokens1.addAll(y.x);
                        i = y.y ;
                        tokens1.add(x.x.get(i));
                        break;
                    case Comma:
                        Expression ex = new Expression(tokens1);
                        ex.simplfy(Objects , vars , isStatic , mi);
                        expr.add(new Token(TokenT.Expression, ex,ex.get(0).getLine()));
                        tokens1 = new ArrayList<>();
                        break;
                    default:
                        tokens1.add(x.x.get(i));
                }
            }
            Expression ex = new Expression(tokens1);
            ex.simplfy(Objects , vars , isStatic , mi);
            expr.add(new Token(TokenT.Expression, ex, ex.get(0).getLine()));
            ArrayDeclaration2 ad2 = new ArrayDeclaration2(expr) ;
            newTokens.add(new Token(TokenT.ArrayDeclaration2 , ad2,ex.get(0).getLine()));
            newTokens.addAll(tokens.subList(x.y + 1 , tokens.size()));
            return newTokens ;
        } else {
            return tokens ;
        }
    }

    //ArrayCall
    private ArrayList<Token> addACall(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> mi) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++) {
            if(i == tokens.size()-1){
                newTokens.add(tokens.get(i));
                break;
            }
            Token t = tokens.get(i);
            Token t1 = tokens.get(i + 1) ;
            if(t.getToken() == TokenT.Value && t1.getToken() == TokenT.LSquare) {
                boolean d = true ;
                String name2 = (String) t.getValue();
                for(ExlVariable var : vars){
                    if(var.getName().equals(name2)){
                        d = !var.isFlat();
                        break;
                    }
                }
                String name =  (String) t.getValue() ;
                Tuple<ArrayList<Token> , Integer> x = getArrayCall(tokens , i + 1 , vars , isStatic ,objects , mi );
                String type = getArrayCallType(name , vars , x.x.size() , objects) ;
                ArrayCall ac = new ArrayCall(x.x , name , type , d);
                newTokens.add(new Token(TokenT.ArrayCall , ac, tokens.get(0).getLine()) );
                i = x.y - 1;
            } else {
                newTokens.add(t);
            }
        }
        return newTokens ;
    }
    private String getArrayCallType(String name, ArrayList<ExlVariable> vars , int size , ArrayList<ExlClassInfo> objects) throws Exception {
        for(ExlVariable var : vars){
            if(var.getName().equals(name)) {
                if (var.isArray()) {
                    if(var.isFlat()){
                        switch (var.getType()){
                            case Double: return "D";
                            case Int: return "I";
                        }
                    }
                    String s = "";
                    int diff = var.getArraySize() - size ;
                    for(int i = 0 ; i < diff ; i++){
                        s = s + "[";
                    }
                    switch (var.getType()) {
                        case Int:
                            return s + "I";
                        case Float:
                            return s + "F";
                        case Double:
                            return s + "D";
                        case Long:
                            return s + "J";
                        case Boolean:
                            return s + "Z";
                        case Char:
                            return s + "C";
                        case String:
                            return s + "Ljava/lang/String;";
                        case Object:
                            return s + "L" + ToolKit.getAddress(var.getObjName() , objects) + ";" ;
                        default:
                            throw new Exception("Can't work out array call type");
                    }
                }
            }
        }
        return null ;
    }

    private  Tuple<ArrayList<Token> , Integer> getArrayCall(ArrayList<Token> tokens, int i, ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI) throws Exception {
        ArrayList<Token> vals = new ArrayList<>();
        while(i < tokens.size() && tokens.get(i).getToken() == TokenT.LSquare){
            Tuple<ArrayList<Token> , Integer> x = ToolKit.collectBracket(tokens , i , TokenT.LSquare) ;
            Expression ex = new Expression(x.x);
            ex.simplfy(objects ,vars ,isStatic , MI);
            vals.add(new Token(TokenT.Expression , ex, ex.get(0).getLine()));
            i = x.y + 1;
        }
        return new Tuple<>(vals , i);
    }

    //Simplfy maths
    private ArrayList<Token> removeBrackets(ArrayList<Token> tokens) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        Expression Expr;
        for(int i = 0 ; i < tokens.size() ; i++) {
            Token t = tokens.get(i);
            if(t.getToken() == TokenT.LBracket) {
                Tuple<ArrayList<Token> , Integer> x = ToolKit.collectBracket(tokens , i , TokenT.LBracket);
                Expr = new Expression(x.x) ;
                i = x.y;
                Expr = new Expression(removeBrackets(Expr.tokens));
                newTokens.add(new Token(TokenT.Expression, Expr, Expr.get(0).getLine()));
            } else  {
                newTokens.add(t);
            }
        }
        return newTokens ;
    }

    public ArrayList<Token> reduceToAdd(ArrayList<Token> tokens){
        ArrayList<Token> newTokens = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++){
           switch (tokens.get(i).getToken()){
               case Mul:
               case Div:
                   newTokens.remove(newTokens.size() - 1);
                   Tuple<ArrayList<Token> , Integer> x = collectMul(tokens , i);
                   i = x.y;
                   newTokens.add(new Token(TokenT.Expression , new Expression(x.x), x.x.get(0).getLine()));
                   break;
               default:
                   newTokens.add(tokens.get(i));
           }
        }
        return newTokens;
    }

    private Tuple<ArrayList<Token> , Integer> collectMul(List<Token> tokens , int mdAT) {
        ArrayList<Token> newTokens = new ArrayList<>();
        newTokens.add(tokens.get(mdAT -1));
        for(int i = mdAT ; i < tokens.size() ; i++){
            switch (tokens.get(i).getToken()){
                case Mul:
                case Div:
                    newTokens.add(tokens.get(i++));
                    newTokens.add(tokens.get(i));
                    break;
                default:
                    return new Tuple<>(newTokens , i - 1);
            }
        }
        return new Tuple<>(newTokens , tokens.size());
    }


    //Booleans
    private boolean isBooleanExpr(ArrayList<Token> tokens) {
        for(int i = 0 ; i < tokens.size() ; i++){
            Token t = tokens.get(i);
            switch (t.getToken()){
                case LThanEq:
                case LThan:
                case GThan:
                case GThanEq:
                case EqualTo:
                case NotEqualTo:
                case Or:
                case And:
                    return true;
                case If:
                    while(tokens.get(i).getToken() != TokenT.Then){
                        i++;
                    }
                    break;
            }
        }
        return false ;
    }

    private ArrayList<Token> simplifyBoolean(ArrayList<Token> tokens , ArrayList<ExlVariable> vars, boolean isStatic, ArrayList<ExlClassInfo> objects, ArrayList<MethodInfo> MI) throws Exception {
        ArrayList<Token> returnList = new ArrayList<>();
        boolean b ;
        ArrayList<ArrayList<Token>> Bools = SplitBools(tokens);
        for(ArrayList<Token> bool : Bools) {
            b = false ;
            Expression ex = new Expression(bool);
            ex.simplfyNOBOOl(objects , vars, isStatic, MI);
            if(ex.size() == 1){
                returnList.add(ex.get(0));
            } else if (ex.size() == 2){
                returnList.add(ex.get(0));
                returnList.add(ex.get(1));
            } else {
                int BoolPos = getBopPos(ex.tokens);
                int i ;
                Token te = ex.get(BoolPos);
                Expression expr = new Expression(new ArrayList<>(ex.tokens.subList(0, BoolPos)));
                if(ex.get(ex.size() - 1 ).getToken() == TokenT.And || ex.get(ex.size() - 1 ).getToken() == TokenT.Or ) {
                    i = ex.size() - 1 ;
                    b = true ;
                } else {
                    i = ex.size() ;
                }
                Expression expr2 = new Expression(new ArrayList<>(ex.tokens.subList(BoolPos + 1, i)));
                ArrayList Boolexpr = new ArrayList<>();
                expr.simplfy(objects , vars , isStatic , MI);
                expr2.simplfy(objects , vars , isStatic , MI);
                Boolexpr.add(new Token(TokenT.Expression, expr,expr.get(0).getLine()));
                Boolexpr.add(te);
                Boolexpr.add(new Token(TokenT.Expression, expr2,expr2.get(0).getLine()));
                returnList.add(new Token(TokenT.BoolExpression , Boolexpr,expr.get(0).getLine()) );
                if(b) {
                    returnList.add(bool.get(bool.size() - 1));
                }
            }
        }
        return condenseBoolean(returnList) ;
    }

    private ArrayList<ArrayList<Token>> SplitBools(ArrayList<Token> tokens) {
        ArrayList<Token> bool = new ArrayList<>();
        ArrayList<ArrayList<Token>> bools = new ArrayList<>();
        for(Token t : tokens){
            switch (t.getToken()){
                case And:
                case Or:
                    bool.add(t);
                    bools.add(bool);
                    bool = new ArrayList<>();
                    break;
                default:
                    bool.add(t);
                    break;
            }
        }
        if( ! bool.isEmpty()){
            bools.add(bool);
        }
        return bools ;
    }

    private ArrayList<Token> condenseBoolean(ArrayList<Token> tokens) {
        ArrayList<Token> retList = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++){
            if(tokens.size() > i + 1 && tokens.get(i + 1).getToken() == TokenT.And){
                Tuple<ArrayList<Token> , Integer> x = collectAnds(tokens , i );
                retList.add(new Token(TokenT.BoolExpression , x.x , x.x.get(0).getLine()));
                i = x.y;
            } else if(tokens.size() > i + 1 ) {
                retList.add(tokens.get(i));
            } else {
                retList.add(tokens.get(tokens.size() - 1 )) ;
            }
        }
        return retList ;
    }

    private Tuple<ArrayList<Token>, Integer> collectAnds(ArrayList<Token> tokens, int i) {
        ArrayList<Token> newList = new ArrayList<>();
        newList.add(tokens.get(i++));
        while (i < tokens.size() && tokens.get(i).getToken() == TokenT.And){
            newList.add(tokens.get(i++));
            newList.add(tokens.get(i++));
        }
        if( i  < tokens.size() && tokens.get(i).getToken() == TokenT.Or){
            i -= 1 ;
        }
        return new Tuple<>(newList , i);
    }

    private int getBopPos(ArrayList<Token> tokens) throws Exception {
        TokenT[] Bop = {TokenT.LThan , TokenT.GThan , TokenT.LThanEq , TokenT.GThanEq , TokenT.EqualTo , TokenT.NotEqualTo } ;
        for(int i = 0 ; i < tokens.size(); i++){
            for(TokenT b : Bop) {
                if (tokens.get(i).getToken() == b){
                    return i;
                }
            }
        }
        throw new Exception("Can't find bool in expression " + PPT.prettyPrint(new Token(TokenT.Expression , new Expression(tokens), tokens.get(0).getLine())));
    }

    //Objects
    private ArrayList<Token> addODCall(ArrayList<Token> tokens, ArrayList<ExlVariable> vars, boolean isStatic , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        boolean b = false ;
        boolean a = false ;
        for(int i = 0 ; i < tokens.size() ; i++){
            if(tokens.get(i).getToken() == TokenT.New){
                i++ ;
                String type ;
                switch (tokens.get(i).getToken()){
                    case Value:
                        type = (String) tokens.get(i++).getValue() ;
                        b = true ;
                        break;
                    case Array:
                        throw new Exception("Missing size of new array should go between [ and ]  ");
                    default:
                        type = ToolKit.TokenToString(tokens.get(i++), objects) ;
                        break;
                }
                switch (tokens.get(i).getToken()){
                    case LBracket:
                        Tuple<ArrayList<Token> , Integer> x = ToolKit.collectBracket(tokens , i , TokenT.LBracket) ;
                        ArrayList<Token> res = ToolKit.fixParam(x.x , vars , isStatic , objects , MI);
                        ArrayList<String> pDesc = ToolKit.getInits(type , objects) ;
                        String desc = ToolKit.calcDesc(pDesc , res , vars , isStatic , type , "init" , objects , MI);
                        String des = ToolKit.getAddress(type , objects);
                        ObjectDeclaration OD = new ObjectDeclaration( des, desc, res);
                        newTokens.add(new Token(TokenT.ObjectDeclaration , OD , tokens.get(0).getLine()) );
                        i = x.y;
                        break;
                    case LSquare:
                        Tuple<ArrayList<Token> , Integer> values = ToolKit.getValues(tokens , i );
                        i = values.y - 1;
                        if(b){
                            type = ToolKit.getAddress(type , objects);
                            type = "L" + type + ";";
                        }
                        for(int j = 0 ; j < values.x.size() ; j++){
                            type = "[" + type ;
                        }
                        ArrayDeclaration AD = new ArrayDeclaration(type , values.x);
                        a = true ;
                        newTokens.add(new Token(TokenT.ArrayDeclaration , AD, tokens.get(0).getLine())) ;
                        break;
                }
            } else {
                newTokens.add(tokens.get(i));
            }
        }
        if(a && newTokens.size() > 1){
            ArrayList<Token> te = (ArrayList<Token>) newTokens.clone();
            for(Token t : newTokens ){
                if(t.getToken() == TokenT.ArrayDeclaration || t.getToken() == TokenT.ObjectDeclaration){
                    te.remove(t);
                    break;
                }
            }
            throw new Exception("Unexpected elements [ " + PPT.prettyPrintList(te) + " ] in expression " + PPT.prettyPrintLine(newTokens));
        }
        return newTokens ;
    }

    private ArrayList<ExlClassInfo> addClass(String name , String Des , ArrayList<ExlVariable> vars , ArrayList<ExlClassInfo> objects){
        for(ExlClassInfo n : objects) {
            if(n.getName().equals(name)){
                return objects;
            }
        }
        for(ExlVariable var : vars){
            if(var.getName().equals("name") && var.getType() == TokenT.Object){
                return objects;
            }
        }
        try {
            objects.add(new ExlClassInfo(name , Des));
        } catch (Exception e){}
        return objects ;
    }

    private ArrayList<Token> addOCall(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic ,  ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI ) throws Exception {
        ArrayList<Token> newTokens = new ArrayList<>();
        ArrayList<Token> before ;
        Tuple<Token, Integer> x ;
        for(int i = 0 ; i < tokens.size() ; i++) {
            if(i + 1 < tokens.size() && tokens.get(i + 1).getToken() == TokenT.Dot) {
                switch (tokens.get(i).getToken()) {
                    case FunctionCall:
                    case ArrayCall:
                    case ArrayDeclaration:
                    case ObjectDeclaration:
                        x = getObjects(tokens, i, vars, isStatic, objects, MI);
                        newTokens.add(x.x);
                        i = x.y - 1;
                        break;
                    case Value:
                        boolean b = false ;
                        String name = (String) tokens.get(i).getValue();
                        for(ExlVariable var : vars){
                            if(var.getName().equals(name)){
                                if(var.isArray() && i + 2 < tokens.size() && tokens.get(i + 2).getToken() == TokenT.Value){
                                    if(tokens.get(i + 2).getValue().equals("length")) {
                                        newTokens.add(new Token(TokenT.ArrayLength, name, tokens.get(0).getLine()));
                                        i += 2 ;
                                        b = true ;
                                    }
                                }
                            }
                        }
                        if(!b) {
                            x = getObjects(tokens, i, vars, isStatic, objects, MI);
                            newTokens.add(x.x);
                            i = x.y - 1;
                        }
                }
            } else if(i < tokens.size() - 1 && tokens.get(i).getToken() == TokenT.Value  && tokens.get(i + 1).getToken() == TokenT.LBracket) {
                Tuple<ArrayList<Token> , Integer> y = ToolKit.collectBracket(tokens , i+1 , TokenT.LBracket);
                if(y.y  + 1 < tokens.size() && tokens.get(y.y + 1).getToken() == TokenT.Dot){
                    if(i > 0){
                        before = new ArrayList<>(tokens.subList(0 , i ));
                    } else {
                        before = new ArrayList<>() ;
                    }
                    ArrayList<Token> fc = new ArrayList<>();
                    fc.add(tokens.get(i));
                    fc.add(new Token(TokenT.LBracket , null , tokens.get(0).getLine()));
                    fc.addAll(y.x);
                    fc.add(new Token(TokenT.RBracket , null, tokens.get(0).getLine()));
                    fc = addFCall(fc, vars , isStatic ,objects ,MI);
                    before.add(new Token(TokenT.FunctionCall , fc.get(0).getValue(), tokens.get(0).getLine()));
                    before.addAll(tokens.subList(y.y + 1 , tokens.size()));
                    tokens = before ;
                    Tuple<Token , Integer> xs = getObjects(tokens , i , vars , isStatic , objects , MI);
                    newTokens.add(xs.x);
                    i = xs.y - 1;
                } else {
                    newTokens.add(tokens.get(i));
                }
            }else{
                newTokens.add(tokens.get(i));
            }
        }
        return newTokens ;
    }

    private Tuple<Token, Integer> getObjects(ArrayList<Token> tokens, int i , ArrayList<ExlVariable> vars , boolean isStatic ,  ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MI) throws Exception {
        Tuple<ArrayList<ArrayList<Token>> , Integer> x = collectObject(tokens , i);
        Token t = objToken(x.x , vars , isStatic, objects , MI);
        return new Tuple<>(t , x.y);
    }

    private Token objToken(ArrayList<ArrayList<Token>> y , ArrayList<ExlVariable> vars , boolean isStatic , ArrayList<ExlClassInfo> objects , ArrayList<MethodInfo> MISS ) throws Exception {
        ArrayList<ObjectRef> refs = new ArrayList<>();
        String head = null ;
        ArrayList<Token> t = y.get(0) ;
        switch (t.get(0).getToken()){
            case Value:
                String s = (String) y.get(0).get(0).getValue();
                if(ToolKit.isVar(s , vars)){
                    ExlVariable exl  = getVar2(s , vars);
                    refs.add(new ObjectRef(false , true , false , "Val" , "()L" + exl.getName() + ";" , exl.getObjName() , "" , y.get(0))) ;
                    if(exl.getType() == TokenT.String){
                        head = "String" ;
                    }else {
                        head = fixType2(exl.getObjName());
                    }
                } else if(s.startsWith("\"")){
                    refs.add(new ObjectRef(false , true , false , "Val" , "()Ljava/lang/String;" , "java/lang/String" , "" , y.get(0))) ;
                    head = "String" ;
                } else{
                    head = s ;                 }
                break;
            case FunctionCall:
                FunctionCall fc = (FunctionCall) y.get(0).get(0).getValue();
                head = getOG(fc.getDesc());
                refs.add(new ObjectRef(false , true , false , "FC" , fc.getDesc() , head , "" , y.get(0))) ;
                break;
            case ArrayCall:
                System.out.println("TODO AC IN OD");
                break;
            case ObjectDeclaration:
                ObjectDeclaration od = (ObjectDeclaration) y.get(0).get(0).getValue();
                head = fixType2(od.getAddress());
                refs.add(new ObjectRef(false , true , false , "OD" , od.getDesc() , head , "" , y.get(0))) ;
                break;
        }
        objects = addClass(head , "" ,vars , objects);
        functionScope fr = new functionScope(null  , null , null , objects );
        head = fr.getClass(head).getName();
        for(int i = 1 ; i < y.size() ;i++){
            String name = (String) y.get(i).get(0).getValue();
            if(y.get(i).size() > 1){
                Expression param = (Expression) y.get(i).get(1).getValue() ;
                functionScope fs = new functionScope(null  , null , null , objects );
                ExlClassInfo ECI = fs.getClass(head);
                ArrayList<MethodInfo> MIs = ECI.getMethods(name);
                ArrayList<String> pDesc = ToolKit.getDescP(MIs) ;
                ArrayList<Token> res = ToolKit.fixParam(param.tokens , vars , isStatic , objects , MISS );
                String desc = ToolKit.calcDesc(pDesc , res , vars , isStatic , head , name , objects , MISS);
                MethodInfo MI = getMis(MIs , desc);
                param.simplfy(objects , vars,isStatic, MISS);
                refs.add(new ObjectRef(true , false , MI.getIsStatic() , name , desc, ECI.getAddress() , MI.getType(), param.tokens));
                if(MI.getType().startsWith("L") ) {
                    String type = fixType(MI.getType());
                    String des = ToolKit.getDes(MI.getType());
                    objects = addClass(type, des , vars, objects);
                    head = type ;
                }else {
                    head = MI.getType();
                }
            }  else {
                objects = addClass(head , "" , vars, objects);
                functionScope fs = new functionScope(null , null , null , objects );
                ExlClassInfo ECI = fs.getClass(head);
                FieldInfo FI = ECI.getField(name);
                try {
                    refs.add(new ObjectRef(false, false, FI.isStatic(), name, "", ECI.getAddress(), FI.getType(), null));
                    if(FI.getType().startsWith("L")) {
                        String type = fixType(FI.getType());
                        String des = ToolKit.getDes(FI.getType());
                        objects = addClass(type, des , vars, objects);
                        head = type ;
                    }else {
                        head = FI.getType();
                    }
                } catch (Exception e){
                    throw new Exception("Can't find field called " + name + " in " + ECI.getName());
                }

            }
        }
        return new Token(TokenT.Object , refs, tokens.get(0).getLine());
    }

    private MethodInfo getMis(ArrayList<MethodInfo> mIs, String desc) {
        for(MethodInfo mi : mIs){
            if(mi.getDescription().equals(desc)){
                return mi ;
            }
        }
        return null ;
    }


    private String getOG(String desc) {
        for(int i = 0 ; i < desc.length() ; i++){
            if(desc.charAt(i) == ')'){
                desc =  desc.substring(i+2 , desc.length() - 1 );
            }
        }
        return fixType2(desc) ;
    }

    private ExlVariable getVar2(String s, ArrayList<ExlVariable> vars) {
        for(ExlVariable var : vars){
            if(var.getName().equals(s)){
                return var ;
            }
        }
        return null ;
    }

    private String fixType(String type) {
        for(int i = type.length() - 1; i > 0  ; i--){
            if(type.charAt(i) == '/'){
                return type.substring(i + 1, type.length() - 1);
            }
        }
        return type ;
    }

    private String fixType2(String type) {
        for(int i = type.length() - 1; i > 0  ; i--){
            if(type.charAt(i) == '/'){
                return type.substring(i +1 );
            }
        }
        return type ;
    }

    private Tuple<ArrayList<ArrayList<Token>>, Integer> collectObject(ArrayList<Token> tokens, int j) throws Exception {
        ArrayList<ArrayList<Token>> newList = new ArrayList<>();
        newList.add(new ArrayList<>(tokens.subList(j , j+1)));
        j++ ;
        while(j < tokens.size() && tokens.get(j).getToken() == TokenT.Dot) {
            Tuple<ArrayList<Token> , Integer > o = getObject(tokens , j);
            newList.add(o.x);
            j = o.y;
        }
        return new Tuple<>(newList , j);
    }

    private Tuple<ArrayList<Token>, Integer> getObject(ArrayList<Token> tokens, int j) throws Exception {
        ArrayList<Token> t = new ArrayList<>();
        j += 2;
        if(j < tokens.size() && tokens.get(j).getToken() == TokenT.LBracket){
            Tuple<ArrayList<Token> , Integer> s = ToolKit.collectBracket(tokens , j  , TokenT.LBracket );
            t.add(tokens.get(j - 1));
            t.add(new Token(TokenT.Expression , new Expression(s.x), tokens.get(0).getLine())) ;
            j = s.y + 1;
      }else{
            t.add(tokens.get(j - 1));
        }
        return new Tuple<>(t,j);
    }

    @Override
    public String toString() {
        return "Expression{" +
                "tokens=" + tokens +
                '}';
    }
}

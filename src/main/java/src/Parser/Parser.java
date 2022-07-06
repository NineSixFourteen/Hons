package src.Parser;

import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.Info.MethodInfo;
import src.BuildingBlocks.ToolKit;
import src.BuildingBlocks.Values.*;
import src.BuildingBlocks.parserTypes.*;
import src.BuildingBlocks.tools.ConstFunc;
import src.BuildingBlocks.tools.Tuple;

import src.Tokens.Token;
import src.Tokens.TokenT;

import java.util.*;

public class Parser {

    private HashMap<String , String> constValues = new HashMap<>();
    private HashMap<String , ConstFunc> constFunc = new HashMap<>();
    private ArrayList<ExlClassInfo> objects = new ArrayList<>();
    private ArrayList<MethodInfo> methods = new ArrayList<>();
    private ArrayList<ExlError> errors = new ArrayList<>();

    public Parser(){
    }

    private Tuple<ArrayList<Function>, ArrayList<Statement>> getFieldsFunctions(ArrayList<Token> tokens , String Owner ) throws Exception {
        boolean isStatic  = false;
        boolean isPublic  = false;
        boolean isPrivate = false;
        ArrayList<Function> functions = new ArrayList<>();
        ArrayList<ArrayList<Token>> funcBefore = new ArrayList<>();
        ArrayList<Statement> fields = new ArrayList<>();
        ArrayList<ArrayList<Token>> fieldsBefore = new ArrayList<>();
        ArrayList<ExlVariable> vars ;
        Tuple<ArrayList<Token> , Integer> x;
        Tuple<ArrayList<ArrayList<Token>> , ArrayList<ArrayList<Token>> > temp ;
        int i = 0 ;
        int y;
        Tuple<Integer , Boolean> x2 ;
        while (i < tokens.size()) {
            y = i ;
            x2 = checkO(TokenT.Private, i, tokens);if(x2.y){isPrivate = true ; isPublic = false ; }i = x2.x ;
            x2 = checkO(TokenT.Public, i, tokens);if(x2.y){isPrivate = false ; isPublic = true ; }i = x2.x ;
            x2 = checkO(TokenT.Static, i, tokens);if(x2.y){isStatic = true ;}i = x2.x ;
            switch (tokens.get(i).getToken()) {
                case Int:
                case Boolean:
                case Long:
                case Double:
                case Float:
                case Char:
                case String:
                case Array:
                case Void:
                    i++ ;
                    switch (tokens.get(i).getToken()){
                        case Value:
                            i++ ;
                            switch (tokens.get(i).getToken()){
                                case LBracket:
                                    x = collectFunction(tokens , y ) ;
                                    registerFunc(x.x , Owner);
                                    funcBefore.add(x.x);
                                    i = x.y ;
                                    break;
                                case Equal:
                                    x = ToolKit.collectLine(tokens, y);
                                    fieldsBefore.add(x.x);
                                    i = x.y ;
                                    break;
                                default:
                                    throw new Exception("Not sure what this is " + PPT.prettyPrint(tokens.get(i)) + PPT.prettyPrint(tokens.get(i+ 1)) + PPT.prettyPrint(tokens.get(i - 1)));
                            }
                            break;
                    }
                    break;
                case Value:
                    String word = (String) tokens.get(i).getValue();
                    if(word.equals("main")){
                        x = getMain(tokens , y ) ;
                        funcBefore.add(x.x);
                        i = x.y ;
                    } else if(word.equals(Owner)){
                        x = getConstructor(tokens , y , Owner ) ;
                        registerFunc(x.x , Owner);
                        funcBefore.add(x.x);
                        i = x.y ;
                    } else {
                        i++ ;
                        check(TokenT.Value , i++ , tokens);
                        switch (tokens.get(i).getToken()){
                            case LBracket:
                                x = collectFunction(tokens , y ) ;
                                registerFunc(x.x , Owner);
                                funcBefore.add(x.x);
                                i = x.y ;
                                break;
                            case Equal:
                                x = ToolKit.collectLine(tokens, y);
                                fieldsBefore.add(x.x);
                                i = x.y ;
                                break;
                            default:
                                throw new Exception("Not sure what this is " + PPT.prettyPrint(tokens.get(i)));
                        }
                    } break;
                case LBrace:
                    x =  ToolKit.collectBracket(tokens , i , TokenT.LBrace);
                    temp = collectFieldsFunction(x.x , Owner , isPublic , isPrivate , isStatic , new Token(TokenT.Null , null, tokens.get(0).getLine()));
                    fieldsBefore.addAll(temp.x);
                    funcBefore.addAll(temp.y);
                    i = x.y + 1;
                    break;
                default:
                    throw new Exception("Unexpected getFieldFunction token " + PPT.prettyPrint(tokens.get(i)) + " " +  i );
            }
        }
        vars = new ArrayList<>();
        for(ArrayList<Token> fie : fieldsBefore ){
            try {
                vars = getVar(fie, vars, true);
            } catch (Exception e){
                errors.add(new ExlError(fie.get(0).getLine() , e.getMessage()));
            }
            Statement s = parseStatement(fie , vars , true, TokenT.Void);
            fields.add(s);
        }
        for(ArrayList<Token> func : funcBefore){
            functions.add(getFunction(func , (ArrayList<ExlVariable>) vars.clone(), Owner));
        }
        return new Tuple<>(functions ,fields);
    }

    private Tuple<ArrayList<ArrayList<Token>>, ArrayList<ArrayList<Token>>> collectFieldsFunction(ArrayList<Token> tokens, String Owner, boolean isPublic, boolean isPrivate, boolean isStatic, Token token) throws Exception {
        ArrayList<ArrayList<Token>> funcBefore = new ArrayList<>();
        ArrayList<ArrayList<Token>> fieldsBefore = new ArrayList<>();
        Tuple<ArrayList<Token> , Integer> x;
        int i = 0 ;
        int y ;
        while (i < tokens.size()) {
            y = i ;
            switch (tokens.get(i).getToken()){
                case Static:
                case Private:
                case Public:
                    i++ ;
                    break;
                case Array:
                    while (tokens.get(i).getToken() == TokenT.Array){
                        i++ ;
                    } i-- ;
                case Int:
                case Boolean:
                case Long:
                case Double:
                case Float:
                case Char:
                case String:
                case Void:
                case Value:
                    i++ ;
                    check(TokenT.Value , i++ , tokens);
                    switch (tokens.get(i).getToken()){
                        case LBracket:
                            x = collectFunction(tokens , y ) ;
                            ArrayList<Token> s = setTo(x.x , isPrivate , isPublic , isStatic) ;
                            registerFunc(s, Owner);
                            funcBefore.add(s);
                            i = x.y ;
                            break;
                        case Equal:
                            x = ToolKit.collectLine(tokens, y);
                            fieldsBefore.add(x.x);
                            i = x.y ;
                            break;
                        default:
                            throw new Exception("Not sure what this is collecting" + PPT.prettyPrint(tokens.get(i)));
                    }
                    break;
            }
        }
        return new Tuple<>(fieldsBefore , funcBefore);
    }

    private void registerFunc(ArrayList<Token> tokens , String Owner) throws Exception {
        int i = 0 ;
        boolean isPrivate ;
        boolean isPublic ;
        boolean isStatic ;
        ArrayList<ExlVariable> vars ;
        Tuple<ArrayList<Token> , Integer> x;
        ArrayList<Token> t = new ArrayList<>();
        Tuple<Integer , Boolean> x2 ;
        x2 = checkO(TokenT.Private, i, tokens);
        if(x2.y){isPrivate = true; i = x2.x ;} else {isPrivate = false ; }
        x2 = checkO(TokenT.Public, i, tokens);
        if(x2.y){isPublic = true; i = x2.x ;} else {isPublic = false ; }
        x2 = checkO(TokenT.Static, i, tokens);
        if(x2.y){isStatic = true; i = x2.x ;} else {isStatic = false ; }
        String ty = ToolKit.TokenToString(tokens.get(i++) ,(objects)) ;
        String name = (String) tokens.get(i++).getValue();
        check(TokenT.LBracket, i, tokens);
        x =  ToolKit.collectBracket(tokens, i, TokenT.LBracket);
        vars = getParameters(x.x, isStatic, Owner);
        String[] params = getParam(vars, isStatic);
        i = x.y + 1 ;
        switch (tokens.get(i).getToken()){
            case LBrace:
                methods.add(new MethodInfo(isPublic, isPrivate, isStatic, false, name, false, ty, params, ToolKit.getDesc(ty, params), ""));
                break;
            case Throws:
                i++ ;
                check(TokenT.Value, i,  tokens);
                String thr = (String) tokens.get(i++).getValue();
                check(TokenT.LBrace, i++, tokens);
                methods.add(new MethodInfo(isPublic, isPrivate, isStatic, false, name, false, ty, params, ToolKit.getDesc(ty, params), ToolKit.getAddress(thr , objects)));
        }

    }

    private ArrayList<Token> setTo(ArrayList<Token> x, boolean isPrivate, boolean isPublic, boolean isStatic) {
        ArrayList<Token> newT  = new ArrayList<>();
        if(isPrivate){
            newT.add(new Token(TokenT.Private , null,x.get(0).getLine()));
        } else if(isPublic) {
            newT.add(new Token(TokenT.Public , null,x.get(0).getLine()));
        }
        if(isStatic){newT.add(new Token(TokenT.Static , null,x.get(0).getLine()));}
        newT.addAll(x);
        return newT ;
    }

    private Tuple<ArrayList<Token> , Integer> getMain(ArrayList<Token> func , int i) throws Exception {
        int y = i;
        if(func.get(i++).getToken() != TokenT.Value){
            throw new Exception("Main function not optional and needs no keywords infront ");
        }
        Tuple<ArrayList<Token> , Integer> x;
        check(TokenT.LBracket , i++ ,  func);
        check(TokenT.RBracket , i++ ,  func);
        switch (func.get(i).getToken()){
            case LBrace:
                x =  ToolKit.collectBracket(func, i, TokenT.LBrace);
                break;
            case Throws:
                i++ ;
                check(TokenT.Value , i++ , func);
                check(TokenT.LBrace , i , func);
                x =  ToolKit.collectBracket(func, i, TokenT.LBrace);
                break;
            default:
                x = null ;
        }
        ArrayList<Token> t = new ArrayList<>();
        t.add(new Token(TokenT.Void , null,func.get(0).getLine()));
        t.addAll(func.subList(y, x.y + 1) ) ;
        return new Tuple<>(t, x.y + 1);
    }

    private Tuple<ArrayList<Token> , Integer> getConstructor(ArrayList<Token> tokens , int i , String Owner) throws Exception {
        boolean isPrivate ;
        boolean isPublic ;
        boolean isStatic ;
        ArrayList<ExlVariable> vars ;
        Tuple<ArrayList<Token> , Integer> x;
        ArrayList<Token> t = new ArrayList<>();
        Tuple<Integer , Boolean> x2 ;
        x2 = checkO(TokenT.Private, i, tokens);
        if(x2.y){isPrivate = true; i = x2.x ; t.add(tokens.get(i - 1)) ; } else {isPrivate = false ; }
        x2 = checkO(TokenT.Public, i, tokens);
        if(x2.y){isPublic = true; i = x2.x ; t.add(tokens.get(i - 1)) ; } else {isPublic = false ; }
        x2 = checkO(TokenT.Static, i, tokens);
        if(x2.y){isStatic = true; i = x2.x + 1 ; t.add(tokens.get(i - 1)) ; } else {isStatic = false ; i += 1 ; }
        int y = i - 1 ;
        check(TokenT.LBracket, i,  tokens);
        x =  ToolKit.collectBracket(tokens, i, TokenT.LBracket);
        vars = getParameters(x.x, isStatic, Owner);
        i = x.y + 1;
        check(TokenT.LBrace, i,  tokens);
        x =  ToolKit.collectBracket(tokens, i, TokenT.LBrace);
        String ty = "V";
        String[] pams = getParam(vars, isStatic);
        methods.add(new MethodInfo(isPublic, isPrivate, isStatic, false, Owner, false, ty, pams, ToolKit.getDesc(ty, pams) , ""));
        t.add(new Token(TokenT.Void , null,tokens.get(0).getLine()));
        t.addAll(tokens.subList(y, x.y + 1) ) ;
        return new Tuple<>(t, x.y + 1);
    }

    private Tuple<ArrayList<Token> , Integer> collectFunction(ArrayList<Token> tokens , int i ) throws Exception {
        Tuple<ArrayList<Token> , Integer> x;
        int y = i ;
        Tuple<Integer , Boolean> x2 ;
        x2 = checkO(TokenT.Private, i, tokens); i = x2.x ;
        x2 = checkO(TokenT.Public, i, tokens); i = x2.x ;
        x2 = checkO(TokenT.Static, i, tokens);i = x2.x ;
        switch (tokens.get(i).getToken()) {
            case Array:
            case Int:
            case Long:
            case Double:
            case Char:
            case Float:
            case Boolean:
            case String:
            case Void:
                i++ ;
                check(TokenT.Value, i++,  tokens);
                check(TokenT.LBracket, i,  tokens);
                x = ToolKit.collectBracket(tokens, i, TokenT.LBracket);
                i = x.y + 1;
                switch (tokens.get(i).getToken()){
                    case LBrace:
                        x = ToolKit.collectBracket(tokens, i, TokenT.LBrace);
                        return new Tuple<>(new ArrayList<>(tokens.subList(y, x.y + 1) ), x.y + 1);
                    case Throws:
                        i++ ;
                        check(TokenT.Value , i++ , tokens);
                        check(TokenT.LBrace , i , tokens);
                        x = ToolKit.collectBracket(tokens, i, TokenT.LBrace);
                        return new Tuple<>(new ArrayList<>(tokens.subList(y, x.y + 1) ), x.y + 1);
                }
            case Value:
                String typeF = (String) tokens.get(i++).getValue();
                if (ToolKit.isObject2(typeF, objects)) {
                    check(TokenT.Value, i++,  tokens);
                    check(TokenT.LBracket, i,  tokens);
                    x = ToolKit.collectBracket(tokens, i, TokenT.LBracket);
                    i = x.y + 1;
                    switch (tokens.get(i).getToken()){
                        case LBrace:
                            x = ToolKit.collectBracket(tokens, i, TokenT.LBrace);
                            return new Tuple<>(new ArrayList<>(tokens.subList(y, x.y + 1) ), x.y + 1);
                        case Throws:
                            i++ ;
                            check(TokenT.Value , i++ , tokens);
                            check(TokenT.LBrace , i , tokens);
                            x = ToolKit.collectBracket(tokens, i, TokenT.LBrace);
                            return new Tuple<>(new ArrayList<>(tokens.subList(y, x.y + 1) ), x.y + 1);
                    }
                } else {
                    throw new Exception("Don't know object " + typeF);
                }
                break;
            default:
               throw new Exception("Unexpected type of func " + PPT.prettyPrint(tokens.get(i)));
        }
        return null ;
    }
    
    private ArrayList<ExlVariable> getVar(ArrayList<Token> s, ArrayList<ExlVariable> vars , boolean isStatic) throws Exception {
        StatementType st = getStatementType(s);
        switch (st){
            case Float: case Long: case Boolean: case Char: case Int:
            case Double: checkForDupe((String) s.get(1).getValue() , vars) ; vars.add(new ExlVariable((String) s.get(1).getValue() , StoTT(st) , 0)); break;
            case String: vars.add(new ExlVariable((String) s.get(1).getValue() , TokenT.String , "java/lang/String", 0)); break;
            case Array:
                Tuple<Token , Integer> ti = (Tuple<Token, Integer>) s.get(0).getValue();
                if(ti.x.getToken() == TokenT.Value){
                    vars.add(new ExlVariable((String) s.get(1).getValue(), TokenT.Object, (String) ti.x.getValue(),  0, true, ti.y));
                }else {
                    vars.add(new ExlVariable((String) s.get(1).getValue(), ti.x.getToken(), 0, true, ti.y));
                } break;
            case OtherArray:
                Tuple<Token , Integer> tir = (Tuple<Token, Integer>) s.get(0).getValue();
                if(tir.x.getToken() == TokenT.Value){
                    vars.add(new ExlVariable((String) s.get(2).getValue(), TokenT.Object, (String) tir.x.getValue(),  0, true, true, tir.y));
                }else {
                    vars.add(new ExlVariable((String) s.get(2).getValue(), tir.x.getToken(), 0, true,true, tir.y));
                } break;
            case Object: vars.add(new ExlVariable((String) s.get(1).getValue() , TokenT.Object , (String) s.get(0).getValue() , 0));break;
            case For:
                if(s.get(2).getToken() == TokenT.Value){
                    vars.add(new ExlVariable((String) s.get(2).getValue() , TokenT.Int ,0 )) ;
                } else {
                    vars.add(new ExlVariable((String) s.get(3).getValue() , s.get(2).getToken() ,0 )) ;
                }break;
            case Try:
                vars.add(new ExlVariable((String) "e" , TokenT.Object , "java/lang/Exception" , 0));
        }
        return vars ;
    }

    private void checkForDupe(String name , ArrayList<ExlVariable> vars) throws Exception {
        for(String s : constValues.keySet()){
            if(s.equals(name)){
                throw new Exception("Variable  " + name + " already defined as a constant ");
            }
        }
        for(ExlVariable var : vars){
            if(var.getName().equals(name)){
                throw new Exception("Variable  " + name + " already defined as a variable ");
            }
        }
    }

    private TokenT StoTT(StatementType st){
        switch (st){
            case Int: return TokenT.Int ;
            case Boolean: return TokenT.Boolean ;
            case Char: return TokenT.Char ;
            case Float: return TokenT.Float;
            case Long: return TokenT.Long ;
            case Double: return TokenT.Double ;
            default: return null ;
        }
    }

    private Function getFunction(ArrayList<Token> func , ArrayList<ExlVariable> vars2 , String Owner) throws Exception {
        int i = 0;
        Token type ;
        Tuple<Integer , Boolean> x2 ;
        x2 = checkO(TokenT.Private, i, func);
        boolean isPrivate = x2.y ; i = x2.x ;
        x2 = checkO(TokenT.Public, i, func);
        boolean isPublic = x2.y ; i = x2.x ;
        x2 = checkO(TokenT.Static, i, func);
        boolean isStatic = x2.y ; i = x2.x ;
        if(func.get(i).getToken() == TokenT.Value){
            type = new Token(TokenT.Object , ToolKit.getAddress((String) func.get(i++).getValue() , objects) , func.get(0).getLine());
        }else {
            type = func.get(i++);
        }
        String name = (String) func.get(i++).getValue();
        check(TokenT.LBracket , i ,  func);
        Tuple<ArrayList<Token>, Integer> x = ToolKit.collectBracket(func, i, TokenT.LBracket);
        ArrayList<ExlVariable> vars = getParameters(x.x, isStatic, Owner);
        i = x.y + 1;
        String Throws = "";
        switch (func.get(i).getToken()){
            case LBrace:
                x = ToolKit.collectBracket(func, i, TokenT.LBrace);
                break;
            case Throws:
                i++ ;
                check(TokenT.Value , i , func);
                Throws = (String) func.get(i++).getValue();
                check(TokenT.LBrace , i , func);
                x = ToolKit.collectBracket(func, i, TokenT.LBrace);
        }
        vars2.addAll(vars);
        if (!name.equals("main")) {
            ArrayList<Statement> funcStatements = parseStatements(x.x, vars2 , isStatic , type.getToken());
            funcStatements = CompressStatements(funcStatements);
            String ty = ToolKit.TokenToString(type , objects);
            String[] pams = getParam(vars, isStatic);
            return new Function(name, type, funcStatements, vars, ToolKit.getDesc(ty, pams), isPublic, isPrivate, isStatic , ToolKit.getAddress(Throws , objects));
        } else {
            ArrayList<Statement> funcStatements = parseStatements(x.x, vars2, true , TokenT.Void);
            funcStatements = CompressStatements(funcStatements);
            vars = new ArrayList<>();
            vars.add(new ExlVariable("args", TokenT.String, 0, true , 1));
            return new Function(name, type, funcStatements, vars, "([Ljava/lang/String;)V", true, false, true , ToolKit.getAddress(Throws , objects));
        }
    }

    private String[] getParam(ArrayList<ExlVariable> vars , boolean s) {
        ArrayList<ExlVariable> vars2 = new ArrayList<>();
        if(!s){
            vars2.addAll(vars.subList(1 , vars.size()));
        } else{
            vars2.addAll(vars);
        }
        String[] ss = new String[vars2.size()] ;
        for(int i = 0 ; i < vars2.size() ; i++){
            ss[i] = vars2.get(i).getString(objects);
        }
        return ss ;
    }

    public Tuple<ArrayList<ExlClass>, ArrayList<ExlError> > parse(ArrayList<Token> tokens) throws Exception {
        ArrayList<ExlClass> classes = new ArrayList<>();
        String name ;
        ExlClass claus ;
        Tuple<ArrayList<Function>,ArrayList<Statement>> FandF ;
        Tuple<ArrayList<Token> , Integer > x = beforeClass(tokens);
        int i = x.y ;
        try {
            handlePreClass(x.x);
        } catch (Exception e){
            errors.add(new ExlError(x.x.get(0).getLine() , e.getMessage())) ;
        }
        tokens = new ArrayList<>(tokens.subList(i , tokens.size() ));
        i = 0 ;
        while ( i < tokens.size()){
            check(TokenT.Class  , i++ ,  tokens);
            check(TokenT.Value  , i , tokens);
            name = (String) tokens.get(i++).getValue();
            check(TokenT.LBrace , i ,  tokens);
            try {
                x = ToolKit.collectBracket(tokens, i, TokenT.LBrace);
            } catch (Exception e){
                errors.add(new ExlError(tokens.get(i).getLine() , e.getMessage()));
                return new Tuple<>(classes,errors ) ;
            }
            claus = new ExlClass(name);
            FandF = getFieldsFunctions(x.x , name );
            claus.addFunctions(FandF.x);
            for(Statement s : FandF.y){
                claus.addField(s);
            }
            claus.addObjects(objects);
            classes.add(claus);
            i = x.y + 1 ;
        }
        return new Tuple<>(classes,errors ) ;
    }

    private void handlePreClass(ArrayList<Token> x) throws Exception {
        ArrayList<ArrayList<Token>> y = splitIntoLines(x);
        for(ArrayList<Token> xy : y){
            switch (xy.get(0).getToken()){
                case Import:
                    doImport(xy);
                    break;
                case Const:
                    doConst(xy);
                    break;
                case ConstFunc:
                    doConstFunc(xy);
            }
        }
    }

    private void doConstFunc(ArrayList<Token> xy) {
        int i = 1 ;
        Tuple<ArrayList<Token> , Integer> x = ToolKit.tokensUpTo(xy, i, TokenT.Colan );
        i = x.y + 1;
        Tuple<ArrayList<Token>, Integer> y = ToolKit.tokensUpTo(xy, i, TokenT.Colan);
        i = y.y + 1;
        Tuple<ArrayList<Token>, Integer> z = ToolKit.tokensUpTo(xy, i,TokenT.SemiColan);
        if(x.x.size() > 0) {
            String name = (String) x.x.get(0).getValue();
            ArrayList<Token> tokens = makePattern(x.x, y.x);
            ConstFunc cf = new ConstFunc(tokens, z.x);
            constFunc.put(name, cf);
        }
    }

    private ArrayList<Token> makePattern(ArrayList<Token> x, ArrayList<Token> x1) {
        ArrayList<Token> newTokens = new ArrayList<>();
        for(int i = 0 ; i < x1.size() ; i++){
            Token t = x1.get(i);
            for(Token t1 : x) {
                if (t1.getValue().equals(t.getValue())) {
                    newTokens.add(t);
                    break;
                }
            }
            if(newTokens.size() != i + 1 ) {
                newTokens.add(new Token(TokenT.Var, t.getValue(),x.get(0).getLine()));
            }
        }
        return newTokens;
    }

    private void doConst(ArrayList<Token> xy) {
        int i = 1 ;
        String name = (String) xy.get(i++).getValue();
        check(TokenT.Equal, i++, xy);
        String Value = (String) xy.get(i++).getValue() ;
        check(TokenT.SemiColan, i,  xy);
        constValues.put(name , Value);
    }

    private void doImport(ArrayList<Token> xy) throws Exception {
        int i = 1 ;
        String s = "";
        while(i < xy.size() - 1 && xy.get(i + 1).getToken() == TokenT.Dot){
            s += xy.get(i).getValue() + ".";
            i += 2 ;
        }
        s += xy.get(i).getValue();
        try {
            objects.add(new ExlClassInfo((String) xy.get(i).getValue()  , s));
        } catch (Exception e) {
            throw new Exception("Cannot find " + xy.get(i).getValue());
        }
    }

    private Tuple<ArrayList<Token>, Integer> beforeClass(ArrayList<Token> tokens) {
        ArrayList<Token> nList = new ArrayList<>();
        for(int i = 0 ; i < tokens.size() ; i++){
            if(tokens.get(i).getToken() == TokenT.Class){
                return new Tuple<>(nList , i);
            } else {
                nList.add(tokens.get(i));
            }
        }
        return null ;
    }

    private ArrayList<Statement> CompressStatements(ArrayList<Statement> statements) throws Exception {
        ArrayList<Statement> innerStatements = new ArrayList<>();
        ArrayList<Statement> newStatements = new ArrayList<>() ;
        for(int i = 0 ; i < statements.size() ; i++){
            if(statements.get(i).getType() == StatementType.IF && i+1 < statements.size() && (statements.get(i+1).getType() == StatementType.Else || statements.get(i+1).getType() == StatementType.Else_If)){
                innerStatements.add(statements.get(i++) );
                while( i < statements.size() && (statements.get(i).getType() == StatementType.Else || statements.get(i).getType() == StatementType.Else_If)){
                    innerStatements.add(statements.get(i++));
                }
                validateIFBlock(innerStatements);
                newStatements.add(new Statement(StatementType.IF_Block, null , innerStatements));
                innerStatements = new ArrayList<>();
                i--;
            } else if(statements.get(i).getType() == StatementType.Try){
                if(i + 1 < statements.size() && statements.get(i + 1).getType() == StatementType.Catch){
                    ArrayList<Statement> s = new ArrayList<>() ;
                    s.add(statements.get(i));
                    s.add(statements.get(i + 1));
                    newStatements.add(new Statement(StatementType.TryCatch, null , s));
                    i++ ;
                } else {
                    //THROW ERROR
                }
            } else {
                newStatements.add(statements.get(i));
            }
        }
        return newStatements ;
    }

    private void validateIFBlock(ArrayList<Statement> innerStatements) throws Exception {
        for(int i = 1; i < innerStatements.size() -1 ;i++){
            if(innerStatements.get(i).getType() == StatementType.Else){
                throw new Exception("Else can only be the last else in an IF_Block");
            }
        }
    }

    private ArrayList<ExlVariable> getParameters(ArrayList<Token> tokens , boolean s , String owner) {
        int pos = 0 ;
        ArrayList<ExlVariable> params = new ArrayList<>() ;
        if(!s){
            params.add(new ExlVariable("this" , TokenT.Object , owner , pos++ ));
        }
        for(int i = 0 ; i < tokens.size() ; i++ ) {
            switch (tokens.get(i).getToken()){
                case Value:
                    params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), TokenT.Object ,  ToolKit.getAddress("" +tokens.get(i).getValue() , objects), pos++));  i += 2;
                    break;
                case Array:
                    Tuple<Token, Integer> ti = (Tuple<Token, Integer>) tokens.get(i).getValue();
                    if(tokens.get(i+1).getToken() != TokenT.Mul) {
                        if (ti.x.getToken() == TokenT.Value) {
                            params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), TokenT.Object, (String) ti.x.getValue(), pos++, true, ti.y));
                        } else {
                            params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), ti.x.getToken(), pos++, true, ti.y));
                        }
                        i += 2;
                    } else {
                        if (ti.x.getToken() == TokenT.Value) {
                            params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), TokenT.Object, (String) ti.x.getValue(), pos++, true,true, ti.y));
                        } else {
                            params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), ti.x.getToken(), pos++, true,true , ti.y));
                        }
                        i += 2;
                    }
                    break;
                default:
                    params.add(new ExlVariable((String) tokens.get(i + 1).getValue(), tokens.get(i).getToken(), "", pos++));  i += 2;
                    break;
            }
        }
        return params ;
    }

    private ArrayList<Statement> parseStatements(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic ,TokenT type) {
        ArrayList<Statement> statements = new ArrayList<>();
        ArrayList<ArrayList<Token>> statements2 = splitIntoLines(tokens);
        ArrayList<ExlVariable> varsCl = new ArrayList<>();
        for (ArrayList<Token> statement : statements2 ) {
            try {
                vars = getVar(statement, vars, isStatic);
                StatementType st = getStatementType(statement);
                switch (st){
                    case IF:
                    case For:
                    case Try:
                    case Catch:
                    case While:
                    case Switch:
                        varsCl = (ArrayList<ExlVariable>) vars.clone();
                        Statement s = parseStatement(statement, vars, isStatic, type);
                        vars = (ArrayList<ExlVariable>) varsCl.clone();
                        statements.add(s);
                        break;
                    default:
                        Statement srs = parseStatement(statement, vars, isStatic, type);
                        statements.add(srs);
                }
            } catch (Exception e){
                errors.add(new ExlError(statement.get(0).getLine(), e.getMessage()));
                statements.add(new Statement(StatementType.Fail, null,null));
            }
        }
        return statements ;
    }

    private Statement parseStatement(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic , TokenT type2 ) {
        int pos = 1 ;
        try {
            Expression exp ;
            exp = new Expression(tokens);
            exp.switchT(constFunc,constValues, vars, isStatic, methods,objects);
            tokens = exp.tokens ;
            StatementType type = getStatementType(tokens);
            ArrayList<Token> body ;
            ArrayList<Statement> inStatements;
            ArrayList<Token> expression ;
            ArrayList<Token> statement = new ArrayList<>() ;
            switch (type) {
                case Else_If:
                    pos++ ;
                case While:
                case IF:
                    check(TokenT.LBracket, pos, tokens);
                    Tuple<ArrayList<Token>, Integer> s = ToolKit.collectBracket(tokens, pos , TokenT.LBracket);
                    exp = new Expression(s.x);
                    exp.tidy(constFunc,constValues, vars, isStatic, methods,objects);
                    pos = s.y;
                    check(TokenT.RBracket, pos++, tokens);
                    check(TokenT.LBrace, pos++, tokens);
                    check(TokenT.RBrace, tokens.size() - 1, tokens);
                    body = new ArrayList<>(tokens.subList(pos, tokens.size() - 1));
                    inStatements = parseStatements(body, vars, isStatic, type2);
                    if (inStatements.size() > 0) {
                        inStatements.get(inStatements.size() - 1).setLast();
                    }
                    inStatements = CompressStatements(inStatements);
                    errors = Validate.validateBoolean(exp.tokens, errors, objects, vars);
                    return new Statement(type, exp.tokens, inStatements);
                case Else:
                    check(TokenT.LBrace , pos++ ,  tokens) ;
                    check(TokenT.RBrace , tokens.size()-1 ,  tokens);
                    body = new ArrayList<>(tokens.subList(pos , tokens.size()-1));
                    inStatements = parseStatements(body , vars , isStatic, type2);
                    if(inStatements.size() > 0) {
                        inStatements.get(inStatements.size() - 1).setLast();
                    }
                    return new Statement(type , null, inStatements);
                case For:
                    body = new ArrayList<>();
                    check(TokenT.LBracket , pos , tokens);
                    Tuple<ArrayList<Token> , Integer> t = ToolKit.collectBracket(tokens,pos,TokenT.LBracket);
                    ArrayList<Token> ses = ToolKit.splitOnComma(t.x);
                    if(ses.size() == 3){
                        Expression e = (Expression) ses.get(0).getValue();
                        pos = 0 ;
                        switch (e.get(pos).getToken()){
                            case Int:
                            case Float:
                            case Double:
                            case Long:
                                body.add(e.get(pos++));
                                check(TokenT.Value,pos,e.tokens);
                                body.add(e.get(pos++));
                                break;
                            case Value:
                                body.add(new Token(TokenT.Int, null,body.get(0).getLine()));
                                body.add(e.get(pos++));
                                break;
                            default:
                                throw new Exception("not acceptable type for variable in for ");
                        }
                        check(TokenT.Equal , pos++, e.tokens);
                        e = new Expression(e.tokens.subList(pos,e.size()));
                        e.simplfy(objects,vars,isStatic,methods);
                        body.add(new Token(TokenT.Expression,e,e.get(0).getLine()));
                        e = (Expression) ses.get(1).getValue();
                        e.simplfy(objects,vars,isStatic,methods);
                        errors = Validate.validateBoolean(e.tokens , errors , objects , vars) ;
                        body.add(new Token(TokenT.Expression , e,e.get(0).getLine()));
                        body.add(ses.get(2));
                    } else {
                        errors.add(new ExlError(body.get(0).getLine(), "Missing the three parts for the FOR statement only have " + ses.size() + " parts"));
                        return new Statement(StatementType.Fail , null , null);
                    }
                    pos = t.y;
                    check(TokenT.RBracket , pos++ ,  tokens);
                    check(TokenT.LBrace , pos ,  tokens);
                    t = ToolKit.collectBracket(tokens,pos,TokenT.LBrace);
                    check(TokenT.RBrace , t.y ,  tokens);
                    inStatements = parseStatements(t.x , vars , isStatic, type2);
                    if(inStatements.size() > 0) {
                        inStatements.get(inStatements.size() - 1).setLast();
                    }
                    return new Statement(type , body, inStatements);
                case Try:
                    check(TokenT.LBrace , pos++ ,  tokens);
                    check(TokenT.RBrace , tokens.size()-1 ,  tokens);
                    body = new ArrayList<>(tokens.subList(pos , tokens.size()-1));
                    inStatements = parseStatements(body , vars , isStatic, type2);
                    if(inStatements.size() > 0) {
                        pos++ ;
                        inStatements.get(inStatements.size() - 1).setLast();
                    }
                    return new Statement(type , null, inStatements);
                case Catch:
                    check(TokenT.LBracket , pos++ ,  tokens);
                    check(TokenT.Value, pos++ ,  tokens);
                    check(TokenT.Value, pos++ ,  tokens);
                    check(TokenT.RBracket, pos++ ,  tokens);
                    check(TokenT.LBrace , pos ,  tokens);
                    check(TokenT.RBrace , tokens.size()-1 ,  tokens);
                    body = new ArrayList<>(tokens.subList(pos + 1, tokens.size()-1));
                    tokens = new ArrayList<>(tokens.subList(2 , pos - 1));
                    inStatements = parseStatements(body , vars , isStatic, type2);
                    if(inStatements.size() > 0) {
                        inStatements.get(inStatements.size() - 1).setLast();
                    }
                    return new Statement(type , tokens, inStatements);
                case Print:
                    exp = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                    exp.tidy(constFunc,constValues,vars,isStatic,methods, objects);
                    errors = Validate.validateExpression(exp.tokens , errors,objects, vars);
                    String desc = ToolKit.inferPrint(exp, objects, methods, vars, isStatic, errors);
                    statement = new ArrayList<>();
                    statement.add(new Token(TokenT.Value, desc,exp.get(0).getLine()));
                    statement.add(new Token(TokenT.Expression, exp,exp.get(0).getLine()));
                    return new Statement(type , statement, null);
                case Overwrite:
                    if(tokens.get(1).getToken() == TokenT.Equal) {
                        expression = new ArrayList<>(tokens.subList(2, tokens.size() - 1));
                        exp = new Expression(expression);
                        exp.switchOut(constValues);
                        exp.simplfy(objects ,vars , isStatic ,methods);
                        statement.add(tokens.get(0));
                        statement.add(tokens.get(1));
                        statement.add(new Token(TokenT.Expression, exp,exp.get(0).getLine()));
                        return new Statement(type, statement, null);
                    } else if(tokens.get(1).getToken() == TokenT.LSquare){
                        statement.add(tokens.get(0));
                        Tuple<ArrayList<Token> , Integer> values = ToolKit.getValues(tokens , 1 );
                        statement.add(new Token(TokenT.Expression , new Expression(values.x),tokens.get(0).getLine())) ;
                        pos = values.y ;
                        check(TokenT.Equal , pos , tokens);
                        statement.add(tokens.get(pos));
                        exp = new Expression(new ArrayList<>(tokens.subList(pos + 1, tokens.size() - 1)));
                        exp.switchOut(constValues);
                        exp.simplfy(objects ,vars , isStatic ,methods);
                        statement.add(new Token(TokenT.Expression , exp,exp.get(0).getLine())) ;
                        return new Statement(type, statement, null);
                    } else {
                        errors.add(new ExlError(tokens.get(0).getLine() , "Unclear on what this line is meant to be "));
                        return new Statement(StatementType.Fail, null , null) ;
                    }
                case FunctionCall:
                    exp = new Expression(tokens);
                    exp.switchOut(constValues);
                    exp.simplfy(objects ,vars , isStatic ,methods);
                    return new Statement(type , exp.tokens , null);
                case Boolean:
                    check(TokenT.Value, pos++ ,  tokens);
                    check(TokenT.Equal, pos++ ,  tokens);
                    exp = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                    exp.switchOut(constValues);
                    exp.simplfy(objects ,vars , isStatic ,methods);
                    if(exp.size() == 1){
                        switch (exp.get(0).getToken()){
                            case BoolExpression:
                                statement.add(tokens.get(1));
                                statement.addAll(exp.tokens);
                                return new Statement(type , statement , null) ;
                            case Value:
                                String val = (String) exp.get(0).getValue();
                                switch (val){
                                    case "false":
                                    case "true" :
                                        statement.add(tokens.get(1));
                                        statement.addAll(exp.tokens);
                                        return new Statement(type , statement , null);
                                    default:
                                        if(ToolKit.isVar(val , vars)) {
                                            if (ToolKit.isVarType(val, vars, "Z")) {
                                                statement.add(tokens.get(1));
                                                statement.addAll(exp.tokens);
                                                return new Statement(type , statement , null);
                                            }
                                        } else {
                                            throw new Exception("Variable " + val + "not found" );
                                        }
                                }
                                break;
                            case FunctionCall:
                                FunctionCall fc = (FunctionCall) exp.get(0).getValue();
                                if(fc.getDesc().endsWith("Z")){
                                    statement.add(tokens.get(1));
                                    statement.addAll(exp.tokens);
                                    return new Statement(type, statement , null);
                                } else {
                                    throw new Exception("Function " +  fc.getName() + " doesn't return a boolean type");
                                }
                            case Object:
                                ArrayList<ObjectRef> or = (ArrayList<ObjectRef>) exp.get(0).getValue() ;
                                if(or.get(or.size() - 1).getType().equals( "Z")) {
                                    statement.add(tokens.get(1));
                                    statement.addAll(exp.tokens);
                                    return new Statement(type , statement , null);
                                }
                            case ArrayCall:
                                ArrayCall ac = (ArrayCall) exp.get(0).getValue() ;
                                if(ac.getType().equals("Z")){
                                    statement.add(tokens.get(1));
                                    statement.addAll(exp.tokens);
                                    return new Statement(type, statement , null);
                                } else {
                                    throw new Exception("Array  " +  ac.getName() + " doesn't isn't type ");
                                }
                            case IfThenElse:
                                IfThenElse ite = (IfThenElse) exp.get(0).getValue();
                                if(ToolKit.isType(ite.getThen() , TokenT.Boolean , objects,vars,methods) && ToolKit.isType(ite.getElse() , TokenT.Boolean , objects,vars,methods)){
                                    statement.add(tokens.get(1));
                                    statement.add(exp.get(0));
                                    return new Statement(type, statement , null);
                                } else {
                                    throw new Exception(PPT.prettyPrint(exp.get(0)) + " is not of type bool due to else or then");
                                }
                            default:
                                errors.add(new ExlError(tokens.get(0).getLine() , "Unknown expression given as a boolean " + PPT.prettyPrint(exp.get(0))));
                                return new Statement(StatementType.Fail , null , null);
                        }
                    } else {
                        exp.switchOut(constValues);
                        exp.simplfy(objects ,vars , isStatic ,methods);
                        statement.add(tokens.get(1));
                        statement.addAll(exp.tokens);
                        return new Statement(StatementType.Boolean, statement , null);
                    }
                case Char:
                case Int:
                case Double:
                case Float:
                case String:
                case Long:
                    check(TokenT.Value, pos++ ,  tokens);
                    check(TokenT.Equal, pos++ ,  tokens);
                    statement = new ArrayList<>(tokens.subList(1 , 2)) ;
                    exp  = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                    exp.tidy(constFunc,constValues,vars,isStatic,methods, objects);
                    if(ToolKit.isType(new Token(TokenT.Expression, exp,exp.get(0).getLine()), type ,objects , vars,methods)) {
                        statement.add(new Token(TokenT.Expression, exp,exp.get(0).getLine()));
                    } else {
                        errors.add(new ExlError(exp.get(0).getLine(),"The expression " + PPT.prettyPrint(new Token(TokenT.Expression, exp,exp.get(0).getLine())) + " is not of the type " + type));
                    }
                    return new Statement(type , statement , null);
                case Return:
                    exp  = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                    exp.switchOut(constValues);
                    exp.simplfy(objects ,vars , isStatic ,methods);
                    if(exp.size() > 0) {
                        if (ToolKit.isType(new Token(TokenT.Expression, exp, exp.get(0).getLine()), type2, objects, vars, methods) ) {
                            statement = new ArrayList<>();
                            statement.add(new Token(TokenT.Expression, exp, exp.get(0).getLine()));
                        } else if(type2 == TokenT.Array){
                            statement = new ArrayList<>();
                            statement.add(new Token(TokenT.Expression, exp, exp.get(0).getLine()));
                        } else {
                            errors.add(new ExlError(exp.get(0).getLine(), "The expression " + PPT.prettyPrint(new Token(TokenT.Expression, exp, exp.get(0).getLine())) + " is not of type " + type2));
                        }
                    }
                    return new Statement(type , statement , null) ;
                case Array:
                case OtherArray:
                    statement.add(tokens.get(0));
                    Tuple<Integer, Boolean> z = checkO(TokenT.Mul , pos ,tokens);
                    pos = z.x;
                    statement.add(tokens.get(pos++));
                    check(TokenT.Equal , pos++ ,  tokens);
                    if(tokens.get(pos).getToken() == TokenT.New){
                        exp = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                        exp.switchOut(constValues);
                        exp.simplfy(objects ,vars , isStatic ,methods);
                        if(!Validate.validateArray(tokens.get(0) , exp , objects ,vars , methods)){
                            errors.add(new ExlError(tokens.get(0).getLine(),PPT.prettyPrint(new Token(TokenT.Expression, exp,exp.get(0).getLine())) + "is not of type " + PPT.prettyPrint(tokens.get(0))));
                        }
                        statement.addAll(exp.tokens);
                    }else {
                        exp = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size() - 1)));
                        exp.switchOut(constValues);
                        exp.simplfy(objects ,vars , isStatic ,methods);
                        if(!Validate.validateArray(tokens.get(0) , exp , objects ,vars , methods)){
                            errors.add(new ExlError(tokens.get(0).getLine(),PPT.prettyPrint(new Token(TokenT.Expression, exp,exp.get(0).getLine())) + " is not of type " + PPT.prettyPrint(tokens.get(0))));
                        }
                        statement.addAll(exp.tokens) ;
                    }
                    return new Statement(type , statement , null );
                case Object:
                    checkImported(tokens.get(0) , objects) ;
                    check(TokenT.Value, pos++ ,  tokens);
                    check(TokenT.Equal, pos++ ,  tokens);
                    statement = new ArrayList<>(tokens.subList(0 , 2)) ;
                    exp = new Expression(new ArrayList<>(tokens.subList(pos , tokens.size()-1)));
                    exp.tidy(constFunc,constValues,vars,isStatic,methods,objects);
                    statement.addAll(exp.tokens);
                    return new Statement(type , statement , null);
                case Switch:
                    Tuple<ArrayList<Token> , Integer> x =  ToolKit.collectBracket(tokens , pos , TokenT.LBracket) ;
                    exp = new Expression(x.x) ;
                    pos = x.y  ;
                    x =  ToolKit.collectBracket(tokens , pos , TokenT.LBrace) ;
                    try {
                        inStatements = getSwitch(x.x , vars , isStatic, type2) ;
                        return new Statement(StatementType.Switch, exp.tokens , inStatements);
                    } catch (Exception e){
                        errors.add(new ExlError(tokens.get(0).getLine(), "Switch body is incorrect"));
                        return new Statement(StatementType.Fail, tokens , null);
                    }
                case Break:
                    return new Statement(StatementType.Break, null , null);
                default:
                    errors.add(new ExlError(tokens.get(0).getLine(), "unable to determine type of statement "));
                    return new Statement(StatementType.Fail, tokens , null);
            }
        } catch(Exception e) {
            //e.printStackTrace();
            errors.add(new ExlError(tokens.get(0).getLine(), e.getMessage()));
            return new Statement(StatementType.Fail, null,null) ;
        }
    }

    private void checkImported(Token token, ArrayList<ExlClassInfo> objects) throws Exception {
        String i = (String) token.getValue();
        for(ExlClassInfo object : objects){
            if(object.getName().equals(i)){
                return;
            }
        }
        try{
            Class.forName("java.lang." + i);
            return;
        } catch (Exception e) {
            throw new Exception("Unknown type " + i + ", suggestion check has been imported");
        }
    }

    private ArrayList<Statement> getSwitch(ArrayList<Token> tokens , ArrayList<ExlVariable> vars , boolean isStatic , TokenT type) throws Exception {
        ArrayList<Statement> statements = new ArrayList<>();
        ArrayList<ArrayList<Token>> statements2 = splitIntoLines(tokens);
        for(ArrayList<Token> t : statements2){
            if(t.get(0).getToken() == TokenT.Case ){
                Tuple<ArrayList<Token> , Integer> x =  getCaseStatement(t) ;
                if(x.x.size() == 1 && x.x.get(0).getToken() == TokenT.Value) {
                    statements.add(new Statement(StatementType.Case, x.x , null));
                } else {
                    throw new Exception("Case only one value" + x.x );
                }
                statements.add(parseStatement(new ArrayList<>(t.subList(x.y + 1, t.size())) , vars , isStatic, type ));
            } else if(t.get(0).getToken() == TokenT.Default){
                check(TokenT.Colan , 1 ,  t);
                statements.add(new Statement(StatementType.Case, null , null));
                statements.add(parseStatement(new ArrayList<>(t.subList(2, t.size())) , vars , isStatic, type));
            } else {
                statements.add(parseStatement(t , vars , isStatic, type));
            }
        }
        return statements ;
    }

    private Tuple<ArrayList<Token>, Integer> getCaseStatement(ArrayList<Token> tokens) {
        ArrayList<Token> newList = new ArrayList<>();
        int i = 1 ;
        while (tokens.get(i).getToken() != TokenT.Colan){
            newList.add(tokens.get(i));
            i++ ;
        }
        return new Tuple<>(newList , i) ;
    }


    private void check(TokenT type , int  pos , ArrayList<Token> tokens) {
        if (!(type == tokens.get(pos).getToken())) {
            if(type == TokenT.Value){
                errors.add(new ExlError(tokens.get(0).getLine(), "Expected a Value but was " + tokens.get(pos)));
            } else {
                errors.add(new ExlError(tokens.get(0).getLine(), "Expected " + new Token(type, type, tokens.get(0).getLine()) + " but was " + tokens.get(pos) + " " + tokens));
            }
        }
    }

    private Tuple<Integer , Boolean > checkO(TokenT type , int pos , ArrayList<Token> tokens){
        if(tokens.get(pos).getToken() == type){
            return new Tuple<>(pos + 1 , true )  ;
        }
        return new Tuple<>(pos , false) ;
    }


    private StatementType getStatementType(ArrayList<Token> tokens) {
        switch (tokens.get(0).getToken()) {
            case If: return StatementType.IF;
            case String: return StatementType.String;
            case Print: return StatementType.Print;
            case For: return StatementType.For;
            case Float: return StatementType.Float;
            case Int: return StatementType.Int;
            case Return: return StatementType.Return;
            case Char: return StatementType.Char;
            case Boolean: return StatementType.Boolean;
            case Long: return StatementType.Long;
            case Double: return StatementType.Double;
            case Switch: return StatementType.Switch;
            case While: return StatementType.While;
            case Try: return StatementType.Try;
            case Catch: return StatementType.Catch;
            case Else:
                if(tokens.get(1).getToken() == TokenT.If){
                    return StatementType.Else_If;
                }else{
                    return StatementType.Else;
                }
            case Value:
                try {
                    objects.add(new ExlClassInfo((String) tokens.get(0).getValue(), ""));
                } catch (Exception e){}
                Expression exp = new Expression(tokens);
                if(exp.get(1).getToken() == TokenT.Dot){
                    return StatementType.FunctionCall;
                }
                if(exp.get(1).getToken() == TokenT.LBracket){
                    return StatementType.FunctionCall;
                }
                if(exp.get(0).getToken() == TokenT.Value && exp.get(1).getToken() == TokenT.Value){
                    return StatementType.Object;
                }
                return StatementType.Overwrite;
            case Array: return tokens.get(1).getToken() == TokenT.Mul ? StatementType.OtherArray : StatementType.Array;
            case Break: return StatementType.Break;
            default:
                System.out.println("UNKNOWN STATEMENT TYPE - " + tokens.get(0) );
                return StatementType.Fail;
        }
    }



    static private ArrayList<ArrayList<Token>> splitIntoLines(ArrayList<Token> tokens) {
        ArrayList<ArrayList<Token>> Lines = new ArrayList<>();
        ArrayList<Token> Line = new ArrayList<>();
        for(int i = 0; i < tokens.size() ; i++){
            switch (tokens.get(i).getToken()){
                case SemiColan:
                    Line.add(tokens.get(i));
                    Lines.add(Line);
                    Line = new ArrayList<>();
                    break;
                case LBrace:
                    Line.add(tokens.get(i));
                    i++;
                    int count = 0 ;
                    while( ! (count == 0 && tokens.get(i).getToken() == TokenT.RBrace ) && i < tokens.size()-1){
                        if(tokens.get(i).getToken() == TokenT.LBrace){
                            count += 1;
                        }
                        if(tokens.get(i).getToken() == TokenT.RBrace){
                            count -= 1;
                        }
                        Line.add(tokens.get(i));
                        i++;
                    }
                    Line.add(tokens.get(i));
                    Lines.add(Line);
                    Line = new ArrayList<>();
                    break;
                default:
                    Line.add(tokens.get(i));
                    break;
            }
        }
        return Lines;
    }

}
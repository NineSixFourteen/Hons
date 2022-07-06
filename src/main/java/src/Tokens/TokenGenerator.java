package src.Tokens;

import src.BuildingBlocks.tools.Tuple;

import java.util.ArrayList;

public class TokenGenerator {

    private String code = "";

    public TokenGenerator(String code){
        this.code = code;
    }


    private String removeSpaces (String s){ return s.replace("//s++","");}

    private ArrayList<Token> addToken(String word, ArrayList<Token> ts,int line){
        Token t = matchToken(removeSpaces(word) , line);
        if ( ! (t.getToken() == TokenT.Value && t.getValue() == null) ) { ts.add(t); }
        return ts;
    }

    public ArrayList<Token> getTokens() throws Exception {
        ArrayList<Token> tokens = new ArrayList<Token>() ;
        char[] Code = code.toCharArray();
        String word = "";
        char k = ' ';
        boolean r = false;
        boolean collectingString = false ;
        int line = 1;
        for (char c : Code) {
            if(!collectingString) {
                switch (c) {
                    case '\n': line++ ;
                    case ' ' : tokens = addToken(word, tokens,line); word = ""; break;
                    case ';' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.SemiColan, null , line)); word = ""; break;
                    case ':' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.Colan, null, line )); word = ""; break;
                    case '(' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.LBracket, null,line)); word = ""; break;
                    case ')' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.RBracket, null,line)); word = ""; break;
                    case '{' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.LBrace, null,line)); word = ""; break;
                    case '}' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.RBrace, null,line)); word = ""; break;
                    case ',' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.Comma, null,line)); word = ""; break;
                    case '[' : tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.LSquare, null,line)); word = ""; break;
                    case ']' : tokens = addToken(word, tokens,line); tokens = check(tokens); word = ""; break;
                    case '.' :
                        if(! isNumber(word) ) {tokens = addToken(word, tokens,line); tokens.add(new Token(TokenT.Dot, null,line)); word = "" ; break;}
                        else { word += c ; break; }
                    case '\'':
                        r = true ;
                    case '\"': tokens = addToken(word, tokens,line); collectingString = true ; word = "" + c; k = c ; break;
                    default:
                        word += c;
                        break;
                }
            } else if(collectingString && c == k) {
                word += c ;
                r = false;
                collectingString = false ;
                tokens.add(new Token(TokenT.Value , word, line));
                word = "";
            }else {
                if(word.endsWith("\\")){
                    word = word.substring(0 , word.length() - 1);
                    switch (c){
                        case 'n':
                            word += "\n";
                            break;
                        case 't':
                            word += "\t";break;
                        case '\\':
                            word += "\\";break;
                        case '\"':
                            word += "\"";break;
                    }
                } else {
                    word += c ;
                }
            }
        }
        if(collectingString){
            throw new Exception("Open " + ( r ? "'" : "\"")  + " on line " + line);
        }
        return tokens ;
    }

    private boolean isNumber(String word) {
        try {
            Integer.parseInt(word);
            return true ;
        } catch (Exception e){
            return false ;
        }
    }

    private ArrayList<Token> check(ArrayList<Token> tokens) {
        if(tokens.get(tokens.size() - 1).getToken() == TokenT.LSquare) {
            tokens.remove(tokens.size() - 1);
                switch (tokens.get(tokens.size() - 1).getToken()) {
                    case Boolean:
                    case String:
                    case Char:
                    case Float:
                    case Int:
                    case Double:
                    case Long:
                    case Value:
                        tokens.add(new Token(TokenT.Array, new Tuple<>(tokens.get(tokens.size() - 1) , 1),tokens.get(tokens.size() - 1).getLine()));
                        tokens.remove(tokens.get(tokens.size() - 2));
                        break;
                    case Array:
                        Tuple <Token , Integer > ts = (Tuple) tokens.get(tokens.size() - 1).getValue();
                        tokens.add(new Token(TokenT.Array , new Tuple<>(ts.x , ts.y + 1), tokens.get(tokens.size() - 1).getLine()));
                        tokens.remove(tokens.get(tokens.size() - 2));
                        break;
                    default:
                        tokens.add(new Token(TokenT.Array, new Token(TokenT.Unknown, null , tokens.get(tokens.size() - 1).getLine())));
                }
            } else{
                tokens.add(new Token(TokenT.RSquare, null, tokens.get(tokens.size() - 1).getLine()));
            }

        return tokens;
    }

    private Token matchToken(String word, int line){
        while(word.startsWith("\n")){
            word = word.substring(1);
        }
        word = word.trim();
        switch (word){
            case "&&": return new Token(TokenT.And , null,line);
            case "||": return new Token(TokenT.Or , null,line) ;
            case "%": return new Token(TokenT.Mod , null,line);
            case "+": return new Token(TokenT.Plus, null,line);
            case "-": return new Token(TokenT.Minus, null,line);
            case "*": return new Token(TokenT.Mul, null,line);
            case "/": return new Token(TokenT.Div, null,line);
            case "for": return new Token(TokenT.For, null,line);
            case "if": return new Token(TokenT.If, null,line);
            case "then": return new Token(TokenT.Then , null,line);
            case "const": return new Token(TokenT.Const , null,line);
            case "constFunc": return new Token(TokenT.ConstFunc , null,line);
            case "else": return new Token(TokenT.Else , null,line);
            case "try": return new Token(TokenT.Try , null,line);
            case "catch": return new Token(TokenT.Catch , null,line);
            case "throws": return new Token(TokenT.Throws ,null,line);
            case "=": return new Token(TokenT.Equal, null,line);
            case "==": return new Token(TokenT.EqualTo, null,line);
            case "!": return new Token(TokenT.Not , null,line) ;
            case "!=": return new Token(TokenT.NotEqualTo, null,line);
            case ">": return new Token(TokenT.GThan, null,line);
            case ">=": return new Token(TokenT.GThanEq, null,line);
            case "<": return new Token(TokenT.LThan, null,line);
            case "<=": return new Token(TokenT.LThanEq, null,line);
            case "private": return new Token(TokenT.Private , null,line);
            case "public": return new Token(TokenT.Public , null,line);
            case "static": return new Token(TokenT.Static , null,line);
            case "new": return new Token(TokenT.New , null,line);
            case "int": return new Token(TokenT.Int, null,line);
            case "String": return new Token(TokenT.String , null,line);
            case "float": return new Token(TokenT.Float, null,line);
            case "char": return new Token(TokenT.Char , null,line);
            case "var": return new Token(TokenT.Var, null,line);
            case "while": return new Token(TokenT.While , null,line);
            case "boolean": return new Token(TokenT.Boolean , null,line);
            case "long": return new Token(TokenT.Long , null,line);
            case "double": return new Token(TokenT.Double , null,line);
            case "void": return new Token(TokenT.Void , null,line);
            case "Print": return new Token(TokenT.Print, null,line);
            case "return": return new Token(TokenT.Return , null,line);
            case "class": return new Token(TokenT.Class , null,line);
            case "import": return new Token(TokenT.Import , null,line) ;
            case "switch": return new Token(TokenT.Switch , null,line);
            case "case": return new Token(TokenT.Case , null,line);
            case "default": return new Token(TokenT.Default , null,line);
            case "break": return new Token(TokenT.Break , null,line);
            default:
                if (word.equals("") || word.charAt(0) == '\n' || word.trim().length() == 0 ){
                    word = null;
                }
                return new Token(TokenT.Value, word,line);
        }
    }

}

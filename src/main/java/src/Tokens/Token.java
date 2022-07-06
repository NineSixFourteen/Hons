package src.Tokens;

public class Token{
    TokenT token;
    Object value ;
    int line ;

    public Token(TokenT t, Object val, int l ) {
        this.token = t ;
        this.value = val;
        this.line = l ;
    }

    public Token(TokenT t, Object val) {
        this.token = t ;
        this.value = val;
        this.line = 99999999;
    }


    public int getLine() {
        return line;
    }

    public TokenT getToken() {
        return token;
    }

    public Object getValue(){
        return value;
    }

    public String toString() {
        return token + " " + (value == null ? "" : value) ;
    }
}


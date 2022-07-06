package src.BuildingBlocks.Info;


import src.BuildingBlocks.tools.Tuple;

import java.util.Arrays;

public class MethodInfo {

    private boolean isPublic = false ;
    private boolean isPrivate = false;
    private boolean isStatic = false ;
    private boolean isArray = false ;
    private String name = "";
    private boolean isFinal = false ;
    private String type = "";
    private String[] param ;
    private String Description ;
    private String Throws = "";

    MethodInfo(String Info){
        boolean b = false ;
        isPublic = true ;
        String[] temp = Info.split(" ");
        for(int i = 1 ; i < temp.length;i++){
            if(!b) {
                switch (temp[i]) {
                    case "final":
                        isFinal = true;
                        break;
                    case "static":
                        isStatic = true;
                        break;
                    case "abstract":
                    case "synchronized":
                    case "default":
                    case "native":
                        break;
                    case "":
                        break;
                    default:
                        b = true;
                        i -= 1 ;
                        break;
                }
            } else {
                type = getType(temp[i]);
                isArray = type.endsWith("[]") ;
                Tuple<String , String> x = split(temp[i + 1]);
                name = getN(x.x) ;
                param = x.y.split(",");
                Description = getDescriptions(type , param);
                break;
            }
        }

    }

    public String getType() {
        return type;
    }

    private String getDescriptions(String type, String[] param) {
        String ret = "(";
        for(String s : param){
            ret += getS(s);
        }
        ret += ")";
        ret += type;
        return ret;
    }

    private Tuple<String , String> split(String s) {
        int k = 0 ;
        for(int i = s.length() - 1 ; i > 0 ; i--){
            if(s.charAt(i) == '('){
                k = i ;
                break;
            }
        }
        return new Tuple<>(s.substring(0 , k),s.substring(k + 1 , s.length() - 1 ));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return Description;
    }

    private String getS(String s){
        String ret = "";
        if(s.endsWith("[]")){
            ret += "[";
            s = s.substring(0 , s.length() - 2);
        }
        switch (s){
            case "":
                ret += "";
                break;
            case "int":
                ret += "I";
                break;
            case "float":
                ret += "F";
                break;
            case "boolean":
                ret += "Z";
                break;
            case "char":
                ret += "C";
                break;
            case "void":
                ret+= "V";
                break;
            case "long":
                ret+= "J";
                break;
            case "double":
                ret+= "D";
                break;
            default:
                ret += "L";
                ret += s.replace('.' , '/') ;
                ret += ";";
                break;
        }
        return ret;
    }

    private String getType(String s){
        switch (s) {
            case "":
            case "int":
                return "I";
            case "float":
                return "F";
            case "boolean":
                return "Z";
            case "char":
                return "C";
            case "void":
                return "V";
            case "long":
                return "J";
            case "double":
                return "D";
            default:
                String ret = "";
                ret += "L";
                ret += s.replace('.', '/');
                ret += ";";
                return ret;
        }
    }

    private String getN(String s) {
        int k = 0 ;
        for(int i = s.length() - 1 ; i > 0 ; i--){
            if(s.charAt(i) == '.'){
                k = i ;
                break;
            }
        }
        return s.substring(k + 1);
    }


    public MethodInfo(boolean pb , boolean P , boolean S , boolean A , String n , boolean F , String t ,String[] p , String desc , String th ){
        isPublic = pb ;
        isPrivate = P ;
        isStatic = S ;
        isArray = A  ;
        name = n     ;
        isFinal = F  ;
        type = t     ;
        param = p ;
        Description = desc ;
        Throws  = th ;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "name='" + name + '\'' +
                ", isStatic=" + isStatic +
                ", isArray=" + isArray +
                ", isFinal=" + isFinal +
                ", type='" + type + '\'' +
                ", param=" + Arrays.toString(param) +
                ", desc=" + Description  +
                '}';
    }
}

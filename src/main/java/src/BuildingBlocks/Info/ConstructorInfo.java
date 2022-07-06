package src.BuildingBlocks.Info;

import java.util.Arrays;

public class ConstructorInfo {

    private String[] param ;
    private String Description ;

    ConstructorInfo(String info){
        int start = 0 ;
        int end = info.length() - 1 ;
        for(int i = 0 ; i < info.length() ; i++){
            if(info.charAt(i) == '('){
                start = i ;
            }
        }
        for(int i = end ; i > start ; i--){
            if(info.charAt(i) == ')'){
                end = i ;
            }
        }
        info = info.substring(start + 1, end );
        param = info.split(",");
        Description = getDescriptions(param);
    }

    private String getDescriptions(String[] param) {
        String ret = "(";
        for(String s : param){
            ret += getS(s);
        }
        ret += ")";
        ret += "V";
        return ret;
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

    public String getDescription() {
        return Description;
    }

    @Override
    public String toString() {
        return "ConstructorInfo{" +
                "param=" + Arrays.toString(param) +
                ", Description='" + Description + '\'' +
                '}';
    }
}

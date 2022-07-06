package src.BuildingBlocks.Info;


public class FieldInfo {

    private boolean isStatic = false ;
    private boolean isArray = false ;
    private String name = "";
    private boolean isFinal = false ;
    private String type = "";

    FieldInfo(String Info){
        boolean b = false ;
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
                name = getN(temp[i + 1]) ;
                break;
            }
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

    FieldInfo(boolean S , boolean A , String n , boolean F , String t ){
        isStatic = S ;
        isArray = A  ;
        name = n     ;
        isFinal = F  ;
        type = t     ;
    }

    private String getType(String word){
        StringBuilder s = new StringBuilder();
        while (word.endsWith("[]")){
            s.append("[");
            word = word.substring(0 , word.length()-2);
        }
        switch (word){
            case "int" :
                return s + "I";
            case "float":
                return s +"F";
            case "boolean":
                return s +"Z";
            case "char":
                return s +"C";
            case "double":
                return s +"D";
            case "long":
                return s + "J";
            default:
                return s + "L" + fixWord(word) + ";" ;
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "isStatic=" + isStatic +
                ", isArray=" + isArray +
                ", name='" + name + '\'' +
                ", isFinal=" + isFinal +
                ", type='" + type + '\'' +
                '}';
    }

    private String fixWord(String word){
        return word.replace('.' , '/') ;
    }
}

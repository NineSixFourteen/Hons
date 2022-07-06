package src.BuildingBlocks.tools;

import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.parserTypes.ExlVariable;

import java.util.ArrayList;

public class functionScope{

    private ArrayList<ExlVariable> vars ;
    private int pos = 0 ;
    private ArrayList<ExlVariable> fields ;
    private String owner ;
    private ArrayList<ExlClassInfo> objects ;
    private int maxVars;

    public functionScope(ArrayList<ExlVariable> v, ArrayList<ExlVariable> f , String o , ArrayList<ExlClassInfo> ob ) {
        if(v != null) {
            maxVars = v.size();
            vars = new ArrayList<>();
            for(int i = 0 ; i < v.size();i++){
                addVar(v.get(i));
            }
        } else {
            vars = new ArrayList<>();
        }
        this.fields = f ;
        this.owner = o ;
        this.objects = ob ;
    }


    public String getOwner() {
        if(owner == null){
            return "";
        }
        return owner;
    }

    public int getMaxVars() {
        return maxVars;
    }

    public ArrayList<ExlVariable> getFields() {
        if(fields ==null){
            return new ArrayList<>();
        }return fields;
    }


    public String getAddress(String ClassName) throws Exception {
        if(ClassName.equals("flatDouble")){
            return "flatDouble";
        }
        if(ClassName.equals("flatInteger")){
            return "flatInteger";
        }
        if(ClassName.startsWith("L") && ClassName.endsWith(";")){
            for(int i = ClassName.length() - 1 ; i > 0; i--){
                if(ClassName.charAt(i) == '/'){
                    ClassName = ClassName.substring(i + 1, ClassName.length() - 1);
                    break;
                }
            }
        }
        ExlClassInfo eci = getClass(ClassName);
        return eci.getAddress() ;
    }

    public ArrayList<ExlClassInfo> getObjects() {
        return objects;
    }

    public ExlClassInfo getClass(String ClassName) throws Exception {
        if(ClassName.contains("/")){
            ClassName = getName(ClassName);
        }
        for(ExlClassInfo ob : objects){
            if(ob.getName().equals(ClassName)){
                return ob ;
            }
        }
        objects.add(new ExlClassInfo(ClassName , ClassName));
        return objects.get(objects.size() -1);
    }

    private String getName(String className) {
        String s = "";
        for(int i = className.length() - 1; i > 0 ; i -- ){
            if(className.charAt(i) == '/'){
                String r = "";
                for(int j = s.length() - 1 ; j > 0 ; j--){
                    r += s.charAt(j);
                }
                return r ;
            } else {
                s += className.charAt(i);
            }
        }
        return s ;
    }

    public int getPos() {
        return pos - 1;
    }

    public ArrayList<ExlVariable> getVars() {
        if(vars == null) {
            return new ArrayList<>();
        }
        return vars;
    }

    public void addVar(ExlVariable newVar) {
        newVar.setPos(pos);
        vars.add(newVar);
        if(vars.size() > maxVars){
            maxVars = vars.size() ;
        }
        if(newVar.isArray()){
            pos++ ;
        } else {
            switch (newVar.getType()) {
                case Long:
                case Double:
                    pos += 2;
                    break;
                default:
                    pos++;
                    break;
            }
        }
    }

    public void setVars(ArrayList<ExlVariable> vars2){
        if(vars2 != null){
            vars = new ArrayList<>();
            pos = 0 ;
            for(ExlVariable var : vars2){
                addVar(var);
            }
            if(vars.size() > maxVars){
                maxVars = vars.size();
            }
        }

    }

    @Override
    public String toString() {
        return "functionScope{" +
                "vars=" + vars +
                '}';
    }

}

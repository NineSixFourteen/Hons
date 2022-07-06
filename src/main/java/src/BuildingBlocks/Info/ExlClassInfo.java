package src.BuildingBlocks.Info;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

public class ExlClassInfo {

    private String name ;
    private String address ;
    private Boolean isGeneric ;
    private int noOfGenerics ;
    private ArrayList<MethodInfo> Methods = new ArrayList<>();
    private ArrayList<ConstructorInfo> Cons = new ArrayList<>();
    private ArrayList<FieldInfo> Fields = new ArrayList<>();

    public ExlClassInfo(String name, String Des) throws Exception {
        this.name = name ;
        Class c;
        try {
            c = Class.forName("src.Output." + name);
            address = name ;
        } catch (Exception e){
            try {
                c = Class.forName("java.lang." + name);
                address = "java/lang/" + name ;
            }catch (Exception e2){
                try{
                    c = Class.forName(Des);
                    address = Des.replace('.', '/') ;
                }catch (Exception e3) {
                    throw new Exception("Can't find class " + Des);
                }
            }
        }
        TypeVariable[] b = c.getTypeParameters() ;
        if(b.length > 0){
            isGeneric = true ;
            noOfGenerics = b.length ;
        } else {
            isGeneric = false ;
            noOfGenerics = 0 ;
        }
        Constructor[] cons = c.getConstructors() ;
        Method[] methods = c.getMethods();
        Field[] fields = c.getFields();
        for(Constructor con : cons){
            Cons.add(new ConstructorInfo(con.toString()));
        }
        for(Method m : methods){
            Methods.add(new MethodInfo(m.toString()));
        }
        for(Field f : fields){
            Fields.add(new FieldInfo(f.toString()));
        }
    }

    public ArrayList<MethodInfo> getMethods(String name){
        ArrayList<MethodInfo> MIs = new ArrayList<>();
        for(MethodInfo m : Methods){
            if(m.getName().equals(name)){
                MIs.add(m);
            }
        }
        return MIs ;
    }

    public FieldInfo getField(String Field){
        for(FieldInfo f : Fields){
            if(f.getName().equals(Field)){
                return f ;
            }
        }
        return null ;
    }

    public ArrayList<ConstructorInfo> getCons() {
        return Cons;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public ArrayList<FieldInfo> getFields() {
        return Fields;
    }

    public ArrayList<MethodInfo> getMethods() {
        return Methods;
    }

    @Override
    public String toString() {
        return "ExlObject{" +
                "name='" + name + '\'' +
                "address='" + address + '\'' +
                ", Methods=" + Methods +
                ", Fields=" + Fields +
                '}';
    }
}

package src.BuildingBlocks.parserTypes;

import src.BuildingBlocks.Info.ExlClassInfo;

import java.util.ArrayList;

public class ExlClass{
    private final String Name;
    private ArrayList<Function> functions;
    private ArrayList<Statement> fields = new ArrayList<>();
    private ArrayList<ExlClassInfo> objects = new ArrayList<>();

    public ExlClass(String n){
        this.Name = n;
        this.functions = new ArrayList<>();
    }

    public void addObjects(ArrayList<ExlClassInfo> ob){
        for(ExlClassInfo o : ob){
            objects.add(o);
        }
    }

    public ArrayList<ExlClassInfo> getObjects() {
        return objects;
    }

    public String getName() {
        return Name;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void addField(Statement s){
        this.fields.add(s);
    }

    public void addFunctions(ArrayList<Function> f){
        functions = f ;
    }
    public ArrayList<Statement> getFields() {
        return fields;
    }


    @Override
    public String toString() {
        return "ExlClass{" +
                "name='" + Name + '\'' +
                '}';
    }

}
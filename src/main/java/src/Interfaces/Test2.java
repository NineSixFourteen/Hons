package src.Interfaces;
import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ConstructorInfo;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.Info.FieldInfo;
import src.BuildingBlocks.Info.MethodInfo;
import src.BuildingBlocks.parserTypes.ExlClass;
import src.BuildingBlocks.parserTypes.Function;
import src.BuildingBlocks.parserTypes.Statement;
import src.BuildingBlocks.tools.Tuple;
import src.Parser.*;
import src.Tokens.Token;
import src.Tokens.TokenGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test2 {

    private static void PrettyPrintClass(ExlClass Claus) {
        System.out.println("CLASS NAME - " + Claus.getName());
        System.out.println("");
        System.out.println("Fields" );
        ArrayList<Statement> stds = Claus.getFields();
        for (Statement s : stds) {
            PrettyPrintStatement(s , 0);
            System.out.println("");
        }
        System.out.println("Functions");
        ArrayList<Function> funcs = Claus.getFunctions();
        for (Function f : funcs) {
            System.out.println("");
            PrettyPrintFunction(f);
        }
        ArrayList<ExlClassInfo> s = Claus.getObjects();
        System.out.println("Objects ");
        for(ExlClassInfo ob : s){
            PrettyPrintObject(ob) ;
        }
    }

    private static void PrettyPrintObject(ExlClassInfo ob){
        System.out.println("Name : " + ob.getName() );
        ArrayList<MethodInfo> meths = ob.getMethods();
        System.out.println("Constructors");
        for(ConstructorInfo m : ob.getCons()){
            System.out.println(m.getDescription());
        }
        System.out.println("Method names");
        for(MethodInfo m : meths){
            System.out.println(m.getName() + " " + m.getDescription());
        }
        System.out.println("Field names");
        for(FieldInfo f : ob.getFields()){
            System.out.println(f.getName() + " " + f.getType());
        }
        System.out.println("");
    }


    private static void PrettyPrintFunction(Function fun) {
        System.out.println("Function Name - " + fun.getName());
        System.out.println("Type - " + fun.getType() + " Parameters " + fun.getParameters() + " Desc " + fun.getDesc() + " Public/Private/Static - " + fun.isPublic() + "/" + fun.isPrivate() + "/" + fun.isStatic() + " throws " + fun.getThrows() );

        System.out.println("Statements");
        ArrayList<Statement> states = fun.getStatements();

        for (Statement s : states) {
            System.out.println("");
            PrettyPrintStatement(s , 0);
        }

    }

    private static void PrettyPrintStatement(Statement S, int indent) {
        String ind = getIndent(indent) ;
        System.out.println( ind + "Type - " + S.getType() + " Body - " + S.getBody());
        ArrayList<Statement> states = S.getInnerStatements();
        if (states != null) {
            System.out.println(ind  + "Inner Statements");
            System.out.println(ind + "{");
            for (Statement s : states) {
                PrettyPrintStatement(s , indent + 1);
            }
            System.out.println(ind + "}");
        }

    }

    private static String getIndent(int indent) {
        String s = "";
        for(int i = 0 ; i < indent ; i++){
            s += "  ";
        }
        return s ;
    }


    public static void main(String[] args) throws FileNotFoundException {
        File ex = new File("src/main/java/Input/Example.exl");
        Scanner sc = new Scanner(ex);
        StringBuilder Code = new StringBuilder();
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            if(! s.trim().startsWith("//")){
                Code.append(s);
                Code.append('\n');
            } else {
                Code.append('\n');
            }
        }
        TokenGenerator t = new TokenGenerator(Code.toString());
        ArrayList<Token> ts = null;
        try {
            ts = t.getTokens();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Parser p = new Parser();
        try {
            Tuple<ArrayList<ExlClass>, ArrayList<ExlError>> classes = p.parse(ts);
            for(ExlError e : classes.y){
                System.out.println(e);
            }
            for(ExlClass clas : classes.x){
                PrettyPrintClass(clas);
            }
        } catch (Exception e){
            System.out.println("Failed to parse file example.exl  due to " );
            System.out.println(e.getMessage());
        }

    }
}

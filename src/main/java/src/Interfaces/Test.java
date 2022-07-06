package src.Interfaces;

import src.BuildingBlocks.Errors.ExlError;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.parserTypes.*;

import src.BuildingBlocks.tools.Tuple;
import src.Parser.Parser;

import src.Compiler.Compiler;

import src.Tokens.Token;
import src.Tokens.TokenGenerator;

import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            System.out.println(args[0]);
        }
        File ex = new File("src/main/java/Input/Example.exl");
        Scanner sc = new Scanner(ex);
        StringBuilder Code = new StringBuilder();
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            if(! s.trim().startsWith("//")){
                Code.append(s);
            }
            Code.append('\n');
        }
        TokenGenerator t = new TokenGenerator(Code.toString());
        ArrayList<Token> ts = null;
        try {
            ts = t.getTokens();
            Parser p = new Parser();
            Tuple<ArrayList<ExlClass>, ArrayList<ExlError> > Classes = null;
            try {
                Classes = p.parse(ts);
                Compiler c = new Compiler();
                if(Classes.y.size() > 0){
                    for(ExlError e : Classes.y){
                        System.out.println(e);
                    }
                }else {
                    for (ExlClass cl : Classes.x) {
                        ArrayList<Function> cr = (ArrayList<Function>) cl.getFunctions().clone();
                        ClassWriter cw = c.compile(cl);
                        String name = cl.getName();
                        cl.addFunctions(cr);
                        makeFile(name, cl);
                        FileOutputStream out = new FileOutputStream("src/main/java/src/Output/" + name + ".class");
                        out.write(cw.toByteArray());
                        out.close();
                    }
                    System.out.println("Success!!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("s " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(" d"+e.getMessage());
        }

    }


    private static void makeFile(String name , ExlClass xD) throws IOException {
        FileOutputStream out = new FileOutputStream("src/main/java/src/Output/" + name + ".java");
        StringBuilder s = new StringBuilder("package src.Output;\n" +
                "public class " + name + "  {\n" +
                "\n");
        ArrayList<Function> sr = xD.getFunctions() ;
        ArrayList<Statement> z = xD.getFields() ;
        for(var y : z){
            s.append("\n\n").append(WriteField(y , xD));
        }
        for(Function x : sr){
            s.append("\n\n").append(writeFunction(x, name, xD));
        }
        s.append("\n" + "}");
        out.write(s.toString().getBytes(StandardCharsets.UTF_8));
        out.close();
    }

    private static String WriteField(Statement y,ExlClass xD) {
        String s = "\tpublic static ";
        if(y.getType() != StatementType.Array) {
            String st = (String) y.getBody().get(0).getValue();
            switch (y.getType()) {
                case Int:
                    s = s + "int " + st + " = 0 ; ";
                    break;
                case Char:
                    s = s + "char " + st + " = 'a' ; ";
                    break;
                case Float:
                    s = s + "float " + st + " = 0 ; ";
                    break;
                case Boolean:
                    s = s + "boolean " + st + " = false ; ";
                    break;
                case String:
                    s = s + "String " + st + " = null ; ";
                    break;
                case Double:
                    s = s + "double " + st + " = 0 ; ";
                    break;
                case Long:
                    s = s + "long " + st + " = 0 ; ";
                    break;
                case Object:
                    s = s + getAddress((String) y.getBody().get(0).getValue(), xD).replace('/', '.') + " " + y.getBody().get(1).getValue() + ";";
                    break;
            }
        } else {
            Tuple<Token , Integer> r = (Tuple<Token, Integer>) y.getBody().get(0).getValue();
            StringBuilder R = new StringBuilder();
            for(int i = 0 ; i < r.y;i++){
                R.append("[]");
            }
            switch (r.x.getToken()) {
                case Int:
                    s = s + "int" + R + " "  + y.getBody().get(1).getValue() + " ;";
                    break;
                case Char:
                    s = s + "char " + R + " " + y.getBody().get(1).getValue() + "  ;";
                    break;
                case Float:
                    s = s + "float " + R + " " + y.getBody().get(1).getValue() + " ;";
                    break;
                case Boolean:
                    s = s + "boolean " + R + " " + y.getBody().get(1).getValue() + " ;";
                    break;
                case String:
                    s = s + "String " + R + " " + y.getBody().get(1).getValue() + " ;";
                    break;
                case Double:
                    s = s + "double " + R + " " + y.getBody().get(1).getValue() + " ;";
                    break;
                case Long:
                    s = s + "long " + R + " " + y.getBody().get(1).getValue() + " ;";
                    break;
                case Value:
                case Object:
                    s = s + getAddress((String) r.x.getValue(), xD).replace('/', '.') + R + " " + y.getBody().get(1).getValue() + ";";
                    break;
                default:
                    System.out.println("ERROR in makeFile in test" + r.x );
                    System.exit(2);
            }
        }
        return s;
    }

    private static String writeFunction(Function f , String cName , ExlClass xD) {
        if(f.getName().equals(cName)){
             return cName + "(" + addParameters(f.getParameters() , xD) + ") " + "{ " + getR(f.getType()) + " }";
        }else {
            return getStart(f) + getType(f.getType(), xD) + " " + f.getName() + "(" + addParameters(f.getParameters() , xD) + ") " + "{ " + getR(f.getType()) + " }";
        }
    }

    private static String getStart(Function f) {
        String s = "";
        if(f.isPublic()){
            s += "public " ;
        }else if(f.isPrivate()){
            s += "private " ;
        }
        if(f.isStatic()){
            s += "static " ;
        }
        return s ;
    }

    private static String getType(Token type , ExlClass xD) {
        switch (type.getToken()){
            case Char:
                return "char";
            case Float:
                return "float";
            case Long:
                return "long";
            case Double:
                return "double";
            case Int:
                return "int";
            case Boolean:
                return "boolean";
            case String:
                return "String";
            case Value:
                String s3 = getAddress((String) type.getValue() , xD);
                return s3.replace("/", ".");
            case Object:
                String s = (String) type.getValue();
                return s.replace("/", ".");
            case Array:
                Tuple<Token , Integer> ti = (Tuple<Token, Integer>) type.getValue();
                StringBuilder s2 = new StringBuilder();
                switch (ti.x.getToken()){
                    case Int:     s2 = new StringBuilder("int");    break;
                    case Float:   s2 = new StringBuilder("float");  break;
                    case Char:    s2 = new StringBuilder("char");   break;
                    case Boolean: s2 = new StringBuilder("boolean");break;
                    case Long:    s2 = new StringBuilder("long");   break;
                    case Double:  s2 = new StringBuilder("double"); break;
                    case String:  s2 = new StringBuilder("String"); break;
                    case Value:   s2 = new StringBuilder(getAddress((String) ti.x.getValue(), xD)); break;
                    case Object:  s2 = new StringBuilder((String) ti.x.getValue());
                }
                s2.append("[]".repeat(Math.max(0, ti.y)));
                return s2.toString().replace("/" , ".");
            case Void:
                return "void";
        }
        return null ;
    }

    private static String getAddress(String name, ExlClass xD){
        if(name.equals(xD.getName())){
            return name ;
        }
        for(ExlClassInfo eci : xD.getObjects()){
            if(eci.getName().equals(name)){
                return eci.getAddress() ;
            }
        }
        return null;
    }

    private static String getR(Token t) {
        switch (t.getToken()){
            case Float:
            case Double:
            case Long:
            case Int:
                return "return 0 ;" ;
            case Char:
                return "return '0';" ;
            case Boolean:
                return "return false;" ;
            case Void:
                return " " ;
            default:
                return "return null;" ;
        }
    }

    private static String addParameters(ArrayList<ExlVariable> param , ExlClass xD) {
        int i = 0 ;
        StringBuilder s = new StringBuilder();
        for(ExlVariable var : param){
            StringBuilder arrays = new StringBuilder();
            if(var.isArray()){
                arrays.append("[]".repeat(Math.max(0, var.getArraySize())));
            }
            switch (var.getType()){
                case Int: s.append(", int").append(arrays).append(" var").append(i++); break;
                case Float: s.append(", float").append(arrays).append(" var").append(i++); break;
                case Double: s.append(", double").append(arrays).append(" var").append(i++); break;
                case Long: s.append(", long").append(arrays).append(" var").append(i++); break;
                case String: s.append(", String").append(arrays).append(" var").append(i++); break;
                case Boolean: s.append(", boolean").append(arrays).append(" var").append(i++); break;
                case Object: s.append(", ").append(Objects.requireNonNull(getAddress(var.getObjName(), xD)).replace('/', '.')).append(arrays).append(" var").append(i++); break;
            }
        }
        if(s.length() > 1) {
            return s.substring(1);
        } else {
            return s.toString();
        }
    }

}



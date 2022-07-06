import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.lang.StringBuilder ;

public class testSuite {

    static String Loc = "src/main/java";

    public static void main(String[] args) throws IOException, InterruptedException {
        testFunction("Vars" ,"vars" , new String[]{"10","d","100.0","false","10.2","1000","sds","5"}); // 8
        testR("Vars" ,"vars" , new String[]{"10","d","100.0","false","10.2","1000","sds","5"});
        testFunction("Vars" ,"arrays" , new String[]{"11","0.0","5.4","0","poper","7"}); // 6
        testFunction("Vars" ,"arraysMD" , new String[]{"8","0.0","2.2","0","dsd","5","0","null","2.0"}); //9
        testFunction("Vars" ,"flatArrays" , new String[]{"10","1.4","14","4.4"}); //4
        testFunction("Expressions" , "basicExp" , new String[]{"0","41","30","30451","822177000","0.0","41.0","30.0","30451.0","8.22177E8","0","41","30","30451","822177000","0.0","41.0","30.0","30451.0","8.2217702E8"});//20
        testFunction("Expressions" , "fCalls" , new String[]{"222" , "224.0" , "222","224.0"});//4
        testFunction("Expressions" , "aCalls" , new String[]{"2","2"});//2
        testFunction("Expressions" , "data" , new String[]{}); //0
        testFunction("Expressions" , "usingObjects", new String[]{"10","2.5","2","4.0","2","2.3","1","2","0.0","0","8","2","4 2.5 2 4.02"}); // 12
        testFunction("Expressions" , "IfThenElse", new String[]{"10","11.0","6.0","false","5","2","null"}); // 7
        testFunction("Expressions" , "mixingUp" , new String[]{"16","27.4","26","26.0"}); // 4
        testFunction("Expressions" , "syntax" , new String[]{"25","43466557686937456435688527675040625802564660517371780402481729089536555417949051890403879840079255169295922593080322634775209689623239873322471161642996440906533187938298969649928516003704476137795166849228875","18.849556","8.0","Hello","10.0","5.0"}); // 4
        testFunction("Logic", "basicLogic", new String[]{"yes", "yes","0","1","2","3","4","true","false","true","false","false","true","true","true","false","false"} ); // 18
        testFunction("Logic", "moreLogic", new String[]{"yes","yes","0","1","2","3","4","true","false","false","false"});
        testFunction("Logic" , "nested" , new String[]{"10","10.0","4","3","2","11.0","4","3","2","12.0","4","3","2","13.0","4","3","2","14.0","4","3","2","10","10.0","4","3","2","11.0","4","3","2","12.0","4","3","2","13.0","4","3","2","14.0","4","3","2"});
        //BasicError
        testErrors("Errors" , "basicE" , new String[]{
                "There was and error on line 3 that states The expression ( 2 + 0.5 ) is not of the type Int",
                "There was and error on line 4 that states The expression ( d(( 2 ), ) ) is not of the type Int" ,
                "There was and error on line 5 that states The expression ( new int[( 2 )] ) is not of the type Int",
                "There was and error on line 6 that states ( new int[( 2 )] )is not of type int[][]",
                "There was and error on line 7 that states The expression ( \"5\" ) is not of the type Int",
                "There was and error on line 8 that states ( new int[( 2 )][( 2 )] )is not of type int[]",
                "There was and error on line 9 that states The expression ( a ) is not of the type Float",
                "There was and error on line 10 that states The expression ( a() ) is not of the type Float",
                "There was and error on line 11 that states The expression ( b() ) is not of the type Int",
                "There was and error on line 12 that states Variable  e already defined as a variable " ,
                "There was and error on line 12 that states Can't find function d that fits desc ( ( 2.4 ) ) in this "
        });
        //OpenBracket
        testErrors("Errors" , "openB" , new String[]{
                "There was and error on line 3 that states Unable to find the closing bracket for ( located at ",
                        "\t( " ,
                "There was and error on line 4 that states Unable to find the closing bracket for ( located at ",
                        "\tb ( ",
                "There was and error on line 5 that states Unable to find the closing bracket for [ located at ",
                        "\t9 + a [ ",
                "There was and error on line 6 that states Unable to find the closing bracket for [ located at ",
                        "\t[ ",
                "There was and error on line 15 that states Unable to find the closing bracket for ( located at ",
                        "\tb ( ",
                "There was and error on line 20 that states Unable to find the closing bracket for [ located at ",
                        "\tf [ "
        });
        //Open Quote
        testErrors("Errors" , "openQ" , new String[]{
                "Open \" on line 4"
        });
    }

    private static void testR(String fold , String file , String[] correctAnswers) throws IOException {
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", "java -cp EXL-V1.jar EXL tests/" + fold + "/" + file + " -R");
        processBuilder.directory(new File(Loc));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        line = reader.readLine();
        if(!line.equals("Success")){
            System.out.println("Compiling of " + file + " failed could not run");
        }
        ArrayList<String> answers = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            answers.add(line);
        }
        checkAnswers(answers , correctAnswers, file);
    }


    private static void testErrors(String fold , String file , String[] expectedErrors) throws IOException{
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", "java -cp EXL-V1.jar EXL tests/" + fold + "/" + file);
        processBuilder.directory(new File(Loc));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        ArrayList<String> errors = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            errors.add(line);
        }
        checkAnswers(errors , expectedErrors, file);
    }

    private static void testFunction(String fold , String file , String[] correctAnswers) throws IOException {
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", "java -cp EXL-V1.jar EXL tests/" + fold + "/" + file);
        processBuilder.directory(new File(Loc));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String lastLine = "";
        StringBuilder wholeMessage = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            wholeMessage.append(line + "\n");
            lastLine = line ;
        }
        if(lastLine.equals("Success")){
            System.out.println("Compiling of " + file + " passed");
        } else {
            System.out.println("Compiling of " + file + " failed message below ");
            System.out.println(wholeMessage);
        }
        processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", "java " + file);
        processBuilder.directory(new File(Loc + "/tests/" + fold));
        process = processBuilder.start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        ArrayList<String> answers = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            answers.add(line);
        }
        checkAnswers(answers , correctAnswers, file);
    }

    private static void checkAnswers(ArrayList<String> answers, String[] correctAnswers , String file) {
        if(answers.size() == correctAnswers.length){
            for(int i = 0 ; i < correctAnswers.length ; i++){
                check(answers.get(i) , correctAnswers[i] , file , i);
            }
            System.out.println("PASSED checking " + file + " output ");
        } else {
            System.out.println( file + " size no match " + answers.size() + " != " + correctAnswers.length);
            System.out.println(answers);
        }
    }

    static void check(String a , String b , String name , int i){
        if(!a.equals(b)){
            System.out.println("Issue in " + name + " test " + i + " does not pass check correct answer : "  + b + " exl answer " + a);
        }
    }

}

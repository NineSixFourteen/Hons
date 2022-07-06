package Benchmarks;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RunBench {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        System.out.println("Enter the number of the Prog you would like to run ");
        try {
            String s = br.readLine() ;
            int i = Integer.parseInt(s);
            switch (i){
                default:
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog4/Prog4e" ,"src/main/java" , "Success");
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog3/Prog3e" ,"src/main/java" , "Success");
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog2/Prog2e" ,"src/main/java" , "Success");
                    //runBenchMark(".\\Prog1CS.exe" , "src/main/java/Benchmarks/Prog1" , 800 , "src/main/java/Benchmarks/Prog1/Prog1CSData.txt") ;
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog1/Prog1e2" ,"src/main/java" , "Success");
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog1/Prog1e" ,"src/main/java" , "Success");
                    runBenchMark("java Prog1e2" , "src/main/java/Benchmarks/Prog1" , 25 , "src/main/java/Benchmarks/Prog1/Prog1e3Data.txt") ;
                    runBenchMark("java Prog1e" , "src/main/java/Benchmarks/Prog1" , 25 , "src/main/java/Benchmarks/Prog1/Prog1e3Data.txt") ;
                    //runBenchMark("java Prog1"  , "src/main/java/Benchmarks/Prog1" , 800 , "src/main/java/Benchmarks/Prog1/Prog1jData.txt") ;
                    runBenchMark("java Prog2e" , "src/main/java/Benchmarks/Prog2" , 25 , "src/main/java/Benchmarks/Prog2/Prog1eData.txt") ;
                    //runBenchMark("java Prog2"  , "src/main/java/Benchmarks/Prog2" , 800 , "src/main/java/Benchmarks/Prog2/Prog2jData.txt") ;
                    //runBenchMark("python Prog2.py" , "src/main/java/Benchmarks/Prog2" , 1500 , "src/main/java/Benchmarks/Prog2/Prog2pData.txt") ;
                    //runBenchMark("python Prog1.py" , "src/main/java/Benchmarks/Prog1" , 1500 , "src/main/java/Benchmarks/Prog1/Prog1pData.txt") ;
                    runBenchMark("java Prog3e" , "src/main/java/Benchmarks/Prog3" , 25 , "src/main/java/Benchmarks/Prog3/Prog5eData.txt") ;
                    runBenchMark("java Prog4e" , "src/main/java/Benchmarks/Prog4" , 25 , "src/main/java/Benchmarks/Prog4/Prog45Data.txt") ;
                    //runBenchMark("java Prog4"  , "src/main/java/Benchmarks/Prog4" , 200 , "src/main/java/Benchmarks/Prog4/Prog4jData.txt") ;
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exiting...");
        }
    }

    private static void runProg4(int noOfRuns) {
        int noOfRunsr = 200;
        System.out.println("Enter the number of the language you would like to run \n" +
                "1. Exl \n" +
                "2. Java \n" +
                "3. Numpy \n " +
                "5. All of the above");
        try {
            String s = br.readLine() ;
            int i = Integer.parseInt(s);
            br.close();
            switch (i){
                case 1 :
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog4/Prog4e" ,"src/main/java" , "Success");
                    runBenchMark("java Prog4e" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4eData.txt") ;
                    break;
                case 2 :
                    //compileFile("javac Prog2.java" ,"src/main/java/Benchmarks/Prog2" , "");
                    runBenchMark("java Prog4" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4jData.txt") ;
                    break;
                case 3 :
                    runBenchMark("python Prog4.py" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4pData.txt") ;
                    break;
                case 4 :
                    //runBenchMark("python Prog1NP" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1npData.txt") ;
                    break;
                case 5:
                    runBenchMark("java Prog4e" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4eData.txt") ;
                    runBenchMark("java Prog4" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4jData.txt") ;
                    runBenchMark("python Prog4.py" , "src/main/java/Benchmarks/Prog4" , noOfRuns , "src/main/java/Benchmarks/Prog4/Prog4pData.txt") ;
                default:
                    System.out.println("Exiting...");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exiting...");
        }
    }

    private static void runProg3(int noOfRuns) {
        int noOfRunsr = 1000;
        System.out.println("Enter the number of the language you would like to run \n" +
                "1. Exl \n" +
                "2. Java \n" +
                "3. C \n " +
                "5. All of the above");
        try {
            String s = br.readLine() ;
            int i = Integer.parseInt(s);
            br.close();
            switch (i){
                case 1 :
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog3/Prog3e" ,"src/main/java" , "Success");
                    runBenchMark("java Prog3e" , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3eData.txt") ;
                    break;
                case 2 :
                    //compileFile("javac Prog2.java" ,"src/main/java/Benchmarks/Prog2" , "");
                    runBenchMark("java Prog3" , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3jData.txt") ;
                    break;
                case 3 :
                    runBenchMark(".\\Prog3 " , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3cData.txt") ;
                    break;
                case 4 :
                    //runBenchMark("python Prog1NP" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1npData.txt") ;
                    break;
                case 5:
                    runBenchMark("java Prog3e" , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3eData.txt") ;
                    runBenchMark("java Prog3" , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3jData.txt") ;
                    runBenchMark(".\\Prog3 " , "src/main/java/Benchmarks/Prog3" , noOfRuns , "src/main/java/Benchmarks/Prog3/Prog3cData.txt") ;
                default:
                    System.out.println("Exiting...");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exiting...");
        }
    }

    private static void runProg2(int noOfRuns) {
        int noOfRunsr = 1500;
        System.out.println("Enter the number of the language you would like to run \n" +
                "1. Exl \n" +
                "2. Java \n" +
                "3. Python \n " +
                "5. All of the above");
        try {
            String s = br.readLine() ;
            int i = Integer.parseInt(s);
            br.close();
            switch (i){
                case 1 :
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog2/Prog2e" ,"src/main/java" , "Success");
                    runBenchMark("java Prog2e" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2eData.txt") ;
                    break;
                case 2 :
                    //compileFile("javac Prog2.java" ,"src/main/java/Benchmarks/Prog2" , "");
                    runBenchMark("java Prog2" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2jData.txt") ;
                    break;
                case 3 :
                    runBenchMark("python Prog2.py" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2pData.txt") ;
                    break;
                case 4 :
                    //runBenchMark("python Prog1NP" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1npData.txt") ;
                    break;
                case 5:
                    runBenchMark("java Prog2e" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2eData.txt") ;
                    runBenchMark("java Prog2" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2jData.txt") ;
                    runBenchMark("python Prog2.py" , "src/main/java/Benchmarks/Prog2" , noOfRuns , "src/main/java/Benchmarks/Prog2/Prog2pData.txt") ;
                default:
                    System.out.println("Exiting...");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exiting...");
        }
    }

    private static void runProg1(int noOfRuns) {
        int noOfRunsr = 1500;
        System.out.println("Enter the number of the language you would like to run \n" +
                "1. Exl \n" +
                "2. Java \n" +
                "3. Python \n " +
                "5. All of the above");
        try {
            String s = br.readLine() ;
            int i = Integer.parseInt(s);
            br.close();
            switch (i){
                case 1 :
                    compileFile("java -cp EXL-V1.jar EXL Benchmarks/Prog1/Prog1e" ,"src/main/java" , "Success");
                    runBenchMark("java Prog1e" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1eData.txt") ;
                    break;
                case 2 :
                    //compileFile("javac Prog1.java" ,"src/main/java/Benchmarks/Prog1" , "");
                    runBenchMark("java Prog1" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1jData.txt") ;
                    break;
                case 3 :
                    runBenchMark("python Prog1.py" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1pData.txt") ;
                    break;
                case 4 :
                    break;
                case 5:
                    runBenchMark("java Prog1e" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1eData.txt") ;
                    runBenchMark("java Prog1" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1jData.txt") ;
                    runBenchMark("python Prog1.py" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1pData.txt") ;
                    //runBenchMark("python Prog1NP" , "src/main/java/Benchmarks/Prog1" , noOfRuns , "src/main/java/Benchmarks/Prog1/Prog1npData.txt") ;
                default:
                    System.out.println("Exiting...");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exiting...");
        }
    }

    private static void runBenchMark(String cmd, String Loc, int noOfRuns, String outFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", cmd);
        processBuilder.directory(new File(Loc));
        Process process ;
        ArrayList<Long> times = new ArrayList<>();
        long total = 0 ;
        ArrayList<Long> top5Percent = new ArrayList<>();
        ArrayList<Long> bot5Percent = new ArrayList<>();
        for(int j = 0 ; j < noOfRuns; j++) {
            process = processBuilder.start();
            process.waitFor();
            String line = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String lastLine = "";
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            if(lastLine.contains(".")){
                for(int i = 0 ; i < lastLine.length() ; i++){
                    if(lastLine.charAt(i) == '.'){
                        lastLine = lastLine.substring(0 , i);
                        break;
                    }
                }
            }
            long elapsedTime = Long.parseLong(lastLine);
            times.add(elapsedTime);
            top5Percent = addTotop5Percent(elapsedTime , top5Percent , noOfRuns/20 );
            bot5Percent = addTobot5Percent(elapsedTime , bot5Percent , noOfRuns/20 );
            if(j %20 == 0 ){
                System.out.println("Completed " + j + " out of " + noOfRuns);
            }
        }
        ArrayList<Long> sorted = (ArrayList<Long>) times.clone();
        Collections.sort(sorted);
        ArrayList<Long> lsd = new ArrayList<>(sorted.subList(noOfRuns/20,sorted.size() - noOfRuns/20));
        StringBuilder sb = new StringBuilder("Total time: "  + total  + "\t Number of runs: " + noOfRuns +"\tAverage time : " + average(times) + "\n"
                + "quickest time : " + top5Percent.get(0) + "\t longest time : " + bot5Percent.get(0) + "\n"
                + "average of fastest 5% : " + average(top5Percent) + "\t average of slowest 5% : " + average(bot5Percent) + "\n"
                + "average excluding top 5% and bottom 5% : " + average(lsd) + "\n"
                );

        for(int j = 0 ; j < times.size(); j++){
            sb.append(j + ". " + times.get(j) + "\n");
        }
        sb .append("One Percent fastest times \n");
        for(int j = 0 ; j < top5Percent.size() ; j++ ){
            sb.append(j + ". " + top5Percent.get(j) + "\n");
        }
        sb .append("One Percent slowest times \n");
        for(int j = 0 ; j < bot5Percent.size() ; j++ ){
            sb.append(j + ". " + bot5Percent.get(j) + "\n");
        }
        FileOutputStream out = new FileOutputStream(outFile);
        out.write(sb.toString().getBytes());
        out.close();

    }

    static void compileFile(String cmd , String Loc, String expectedLine) throws IOException {
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", cmd);
        processBuilder.directory(new File(Loc));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String lastLine = "";
        while ((line = reader.readLine()) != null) {
            lastLine = line ;
        }
        if(!lastLine.equals(expectedLine)) {
            System.out.println("CompileFail " + cmd );
            System.out.println(lastLine);
        }
    }

    static ArrayList<Long> addTotop5Percent(Long a , ArrayList<Long> aa , int maxSize){
        if(aa.size() <  maxSize) {
            aa.add(a);
            aa.sort(null);
        } else if( aa.size() > 0 && aa.get(aa.size() - 1) > a ) {
            aa.remove(aa.size() - 1) ;
            aa.add(a);
            aa.sort(null);
        }
        return aa ;
    }

    static ArrayList<Long> addTobot5Percent(Long a , ArrayList<Long> aa , int maxSize){
        if(aa.size() < maxSize) {
            aa.add(a);
            aa.sort(null);
            Collections.reverse(aa);
        } else if( aa.size() > 0 && aa.get(aa.size() - 1) < a ) {
            aa.remove(aa.size() - 1) ;
            aa.add(a);
            aa.sort(null);
            Collections.reverse(aa);
        }
        return aa ;
    }

    static Long average(ArrayList<Long> l){
        long total = 0 ;
        for(Long ls : l){
            total += ls ;
        }
        return total / l.size() ;
    }

}

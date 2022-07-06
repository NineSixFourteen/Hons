package Benchmarks.Prog2;

import java.util.ArrayList;

public class Prog2 {

    public static void main(String[] args) {
        long start = System.currentTimeMillis() ;
        findPath("MI", "MUIU");
        findPath("MI" , "MIUIUIUIU");
        findPath("MI" , "MUIIU");
        findPath("MI" , "MIUIIIIUIUIIIIU");
        findPath("MI" , "MUIU");
        findPath("MI" , "MIUIUIUIU");
        findPath("MI" , "MUIIU");
        findPath("MI" , "MIIUUII");
        findPath("MI" , "Notpos");
        findPath("MI" , "Notpos");
        long end = System.currentTimeMillis() ;
        long diff = end - start ;
        System.out.println(diff);
    }

    private static void findPath(String start , String End) {
        ArrayList<String> paths = new ArrayList<>();
        paths.add(start);
        while(!paths.contains(End)){
            if(paths.size() > 30000){
                return;
            }
            String s = paths.get(0);
            paths.remove(0);
            ArrayList<String> newPaths = checkRules(s);
            paths.addAll(newPaths);
        }
    }

    private static ArrayList<String> checkRules(String start) {
        ArrayList<String> newPaths = new ArrayList<>();
        if(start.endsWith("I")){
            newPaths.add(start + "U");
        }
        newPaths.add( start + start.substring(1));
        for(int i = 0 ; i < start.length() - 3;i++){
            if(start.charAt(i) == 'I' && start.charAt(i + 1) == 'I' && start.charAt(i + 2) == 'I'){
                newPaths.add(start.substring(0 , i) + "U" + start.substring(i +3));
            }
        }
        for(int i = 0 ; i < start.length() - 2;i++){
            if(start.charAt(i) == 'U' && start.charAt(i + 1) == 'U' ){
                newPaths.add(start.substring(0 , i) + start.substring(i + 2));
            }
        }
        return newPaths ;
    }

}

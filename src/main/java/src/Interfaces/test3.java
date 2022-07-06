package src.Interfaces;

import java.io.FileOutputStream;

public class test3 {


    public static void main(String[] args) throws Exception {
        String name = "Dog";
        FileOutputStream out = new FileOutputStream("src/main/java/src/Output" + name + ".class");
        byte[] cw = testDump.dump() ;
        out.write(cw);
        out.close();
    }

}
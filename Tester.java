// Last update: Aug 4, 2023

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tester {
    public static void main (String[] args) throws FileNotFoundException, IOException {
        String filePath = "/Users/tonyliu/Desktop/BrokenCodeSample.java";
        FileTerminal ft = new FileTerminal(filePath);
        ft.fixFile("/Users/tonyliu/Desktop/BrokenCodeFixed.java");
    }
}

// Last update: Aug 4, 2023

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tester {
    public static void main (String[] args) throws FileNotFoundException, IOException {
        String filePath = "your_input_file_path.java";
        FileTerminal ft = new FileTerminal(filePath);
        ft.fixFile("your_output_file_path.java");
    }
}

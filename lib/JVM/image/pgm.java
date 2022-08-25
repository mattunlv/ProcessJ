package image;

import java.io.PrintWriter;
import java.util.*;

public class pgm {
    
    public static void writePGM(int[][] pic, String filename, int max) {
        try {
            int width = pic[0].length;
            int height = pic.length;
            
            PrintWriter write = new PrintWriter(filename, "UTF-8");
            write.println("P2");
            write.println(width + " " + height);
            write.println(max);
            
            for (int j = 0; j < height; ++j) {
                for (int i = 0; i < width; ++i)
                    write.print(pic[j][i] + " ");
                write.println("");
            }
            write.close();
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}

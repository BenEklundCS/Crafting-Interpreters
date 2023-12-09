import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Arithmetic {
    public static void main(String[] args) {
        ArrayList<Token> tokens_a = new Scanner("2+24").scanner();
        ArrayList<Token> tokens_b = new Scanner("10/2*3000-100").scanner();
        ArrayList<Token> tokens_c = new Scanner("(10.5+6.1=16.611111").scanner();
        System.out.println(tokens_a);
        System.out.println(tokens_b);
        System.out.println(tokens_c);
    }
    public static void error(String errorMsg) {
        System.out.println(errorMsg);
    }
    public static void log(String message) {
        try (FileWriter log = new FileWriter("arithmetic.log")){
            log.write(message);
            log.close();
        } catch (IOException e) { error(e.getMessage()); return; }
    }
} 
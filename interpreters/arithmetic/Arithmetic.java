import java.util.ArrayList;

class Arithmetic {
    public static void main(String[] args) {
        Interpreter i = new Interpreter("2+2");
        ArrayList<Token> tokens = i.scanner();
        System.out.println(tokens);
    }
    static public void error(String errorMsg) {
        System.out.println(errorMsg);
    }
}
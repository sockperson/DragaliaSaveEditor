import java.util.Locale;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InputUtils {

    public static final Scanner input = new Scanner(System.in);

    public static String input(String question) {
        System.out.print(question + ": ");
        return input.nextLine();
    }

    //for writing Options values
    public static void writeOptionsValue(String question, String fieldName) {
        String fieldVal = Options.getFieldAsString(fieldName);
        if (fieldVal.equals("true")) {
            fieldVal = "y";
        } else if (fieldVal.equals("false")) {
            fieldVal = "n";
        }

        System.out.print(question + " (y/n) (Current: " + fieldVal + "): ");
        String in = input.nextLine();
        in = in.toUpperCase();

        if(in.equals("Y") || in.equals("YES")){
            Options.editBooleanOption(fieldName, true);
        } else if (in.equals("N") || in.equals("NO")){
            Options.editBooleanOption(fieldName, false);
        } else {
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            writeOptionsValue(question, fieldName);
        }
    }

    public static boolean passYesNo(String question) {
        System.out.print(question + " (y/n): ");
        String in = input.nextLine();
        in = in.toUpperCase();

        if(in.equals("Y") || in.equals("YES")){
            return true;
        } else if (in.equals("N") || in.equals("NO")){
            return false;
        } else {
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            return passYesNo(question);
        }
    }

    public static void yesNoQuestion(String question, String response, Runnable func){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', print response and run the function
            System.out.println(response);
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, response, func);
        }
    }

    public static void yesNoQuestion(String question, Runnable func){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', run the function
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, func);
        }
    }

    public static void yesNoQuestion(String question, Runnable yesFunc, Runnable noFunc){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', run yesFunc
            yesFunc.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', run noFunc
            noFunc.run();
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            yesNoQuestion(question, yesFunc, noFunc);
        }
    }

    public static void maybeQuestion(String question, String response2, Runnable func, Runnable func2){
        System.out.print(question + " (y/n): ");        //print the question
        String in = input.nextLine();                   //take in the input
        in = in.toUpperCase();                          //make the input not case-sensitive
        if(in.equals("Y") || in.equals("YES")){         //if input is 'Y', print response and run the function
            func.run();
        } else if (in.equals("N") || in.equals("NO")){  //if input is 'N', do nothing
            return;
        } else if (in.equals("MITSUBAP")){              //do both of the funcs if secret input
            System.out.println(response2);
            func.run();
            func2.run();
        } else {                                        //if input invalid, ask the question again
            System.out.println("Invalid Input. Enter 'y' or 'n'");
            maybeQuestion(question, response2, func, func2);
        }
    }

    // takes in a String
    // validates something with that String
    // if validated, run a function with that String
    // if not, re-prompt input
    public static void validatedInputAndCall(String val, Predicate<String> pred, Consumer<String> func, String errMessage) {
        while (true) {
            System.out.print(val + " (Enter 'exit' to return): ");
            String in = input.nextLine().toUpperCase(Locale.ROOT);
            if (in.equals("EXIT")) {
                return;
            }
            if (pred.test(in)) {
                func.accept(in);
                return;
            }
            System.out.println(errMessage);
        }
    }

    public static void continuousInput(String val, Consumer<String> func){
        while(true){
            System.out.print(val + " (Enter 'exit' to return): ");
            String in = input.nextLine().toUpperCase();
            if(in.equals("EXIT")){
                return;
            }
            func.accept(in);
        }
    }

}

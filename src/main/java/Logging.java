import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logging {

    public static void print(String msg, String... vals) {
        String printMsg = msg;
        int count = 0;
        for (String val : vals) {
            String marker = "{" + count + "}";
            printMsg = printMsg.replace(marker, val);
            count++;
        }
        System.out.println(printMsg);
    }

    public static void print(String msg, int... vals) {
        String printMsg = msg;
        int count = 0;
        for (int val : vals) {
            String marker = "{" + count + "}";
            printMsg = printMsg.replace(marker, String.valueOf(val));
            count++;
        }
        System.out.println(printMsg);
    }

    private static List<List<String>> log = new ArrayList<>();
    private static List<String> logKindaStream = new ArrayList<>();

    public static String listPrettify(List<String> list){
        int size = list.size();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i++){
            sb.append(list.get(i));
            if(i != size - 1){
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static List<String> toList(List<String> list){
        int size = list.size();
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i++){
            sb.append(list.get(i));
            if(i != size - 1){
                sb.append(", ");
            }
            if(sb.length() > 110){
                out.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        if(sb.length() > 0){
            out.add(sb.toString());
        }
        return out;
    }

    public static void log(String message){
        log.add(Collections.singletonList(message));
    }

    public static void log(String message, String... vals) {
        String printMsg = message;
        int count = 0;
        for (String val : vals) {
            String marker = "{" + count + "}";
            printMsg = printMsg.replace(marker, val);
            count++;
        }
        log.add(Collections.singletonList(printMsg));
    }

    public static void write(String message, List<String> msgs) {
        for (String str : msgs) {
            write(str);
        }
        flushLog(message);
    }

    public static void write(String message){
        logKindaStream.add(message);
    }

    public static void flushLog(String message){
        log.add(Collections.singletonList(message + ": "));
        log.add(toList(logKindaStream));
        logKindaStream.clear();
    }

    public static void clearLogs(){
        log.clear();
    }

    public static void printLogs(){
        for (int i = 0; i < log.size(); i++){
            List<String> messages = log.get(i);
            for (String message : messages) {
                System.out.println("[" + (i+1) + "] " + message);
            }
        }
    }
}

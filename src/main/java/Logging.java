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

    // TODO... move logging stuff from JsonUtils to here
}

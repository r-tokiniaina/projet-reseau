package logging;


public class Log {

    /** Just to inform
     */
    public static void info(String message) {
        System.out.print("INFO: ");
        System.out.println(message);
    }

    /** Used for testing
     */
    public static void debug(String message) {
        System.out.print("DEBUG: ");
        System.out.println(message);
    }

    /** A problem, not serious, but still important to ignore
     */
    public static void warning(String message) {
        System.out.print("WARNING: ");
        System.out.println(message);
    }

    /** Critical error
     */
    public static void error(String message) {
        System.out.print("ERROR: ");
        System.out.println(message);
    }
}

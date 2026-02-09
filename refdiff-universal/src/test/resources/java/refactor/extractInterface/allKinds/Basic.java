package pkg;

public interface Basic {

    // Constant declaration
    int MAX_VALUE = 100;

    // Abstract method
    void doSomething();

    // Default method
    default String getDefaultMessage() {
        return "Default message";
    }

    // Static method
    static int calculateSum(int a, int b) {
        return a + b;
    }

    // Private method (Java 9+)
    private void log(String message) {
        System.out.println("Log: " + message);
    }

    // Public static method
    public static String formatMessage(String message, int value) {
        return String.format("Message: %s, Value: %d", message, value);
    }
}

package Task2;

public class Task2ConsoleDemo {
    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String mode = args[0].toLowerCase();

        switch (mode) {
            case "controller":
                runController(args);
                break;
            case "dispatcher":
                runDispatcher(args);
                break;
            default:
                printHelp();
                break;
        }
    }

    private static void runController(String[] args) {
        int port = 12345;

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, будет использован 12345");
            }
        }

        new Task2Controller(port).start();
    }

    private static void runDispatcher(String[] args) {
        String host = "localhost";
        int port = 12345;

        if (args.length > 1) {
            host = args[1];
        }

        if (args.length > 2) {
            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, будет использован 12345");
            }
        }

        new Task2Dispatcher(host, port, new ConsoleDispatcherView()).start();
    }

    private static void printHelp() {
        System.out.println("Использование:");
        System.out.println("  java Task2.Task2ConsoleDemo controller [port]");
        System.out.println("  java Task2.Task2ConsoleDemo dispatcher [host] [port]");
    }
}
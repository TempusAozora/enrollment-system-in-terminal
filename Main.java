import java.util.Scanner;

public class Main {
    // Main class contains the loop to keep the program running. Most of the code is in "ProcessCommands" class.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProcessCommands processCommands = new ProcessCommands();
        
        handleInterrupts();
        System.out.println("\nWelcome to smart enrollment system. Type 'help' for commands\n");
        System.out.print("smart-enrollment-system> ");
        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            if (cmd.isBlank()) {
                System.out.print("smart-enrollment-system> ");
                continue;
            }

            String res = processCommands.process(cmd);
            System.out.println(res);

            System.out.print("smart-enrollment-system> ");
        }

        System.out.println("Closing");
        scanner.close();
    }

    private static void handleInterrupts() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.err.println(); // prints extra line when the program is interrupted.
            }
        });
    }
}
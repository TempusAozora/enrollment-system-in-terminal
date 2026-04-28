import java.util.Scanner;

public class Main {

    public static void lines(int lines, String lineType) {
        for (int i = 0; i < lines; i++) {
            System.out.print(lineType);
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProcessCommands processCommands = new ProcessCommands();

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

        scanner.close();
    }
}
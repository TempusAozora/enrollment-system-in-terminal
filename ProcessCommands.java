import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProcessCommands {
    private Map<String, Method> commands = new HashMap<>();

    public ProcessCommands() {
        for (Method method : Command.class.getDeclaredMethods()) {
            this.commands.put(method.getName(), method);
        }
    }

    public String process(String str) {
        String[] parts = str.split(" ", 2);
        String cmd = parts[0].toUpperCase(), args = (parts.length > 1) ? parts[1] : "";

        if (this.commands.containsKey(cmd)) {
             try {
                Method method = commands.get(cmd);
                Object res = method.invoke(null, cmd, args);
                return (res instanceof String) ? (String) res : cmd + " Success";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Command not found!";
    }
}

class Command {
    public static void ADD(String cmd, String args) {
        // Add logic here
    }
    public static void REMOVE(String cmd, String args) {
        // Remove logic here
    }

    public static String HELP(String cmd, String args) {
        return "\n" +
        "ADD id, full_name, course, year_level, gender - adds a row." + "\n" +
        "REMOVE [id=? || full_name=? || ...] - removes a row with given conditions. To clear the table, type 'REMOVE *'" + "\n" +
        "DISPLAY [id=? || full_name=? || ...] - displays the table with given conditions. To show the full table, type 'DISPLAY *'" + "\n"
        ;
    }

    public static void DISPLAY(String cmd, String args) {
        System.out.println("");
        System.out.printf("%-10s | %-10s | %-5s%n", "Name", "Position", "Age");
        System.out.println("------------------------------------");
        System.out.printf("%-10s | %-10s | %-5d%n", "Alice", "Dev", 28);
        System.out.println("");
    }
}


import java.lang.reflect.Method;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class ProcessCommands {
    private Map<String, Method> commands = new HashMap<>();
    
    static String createRowLine(String str1, String str2, int[] lengths) { // for formatting row line
        int excess = 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lengths.length; i++) {
            sb.append(str2);
            for (int j = 0; j < lengths[i] + excess; j++) {
                sb.append(str1);
            }
        }
        sb.append(str2);
        sb.append("\n");

        return sb.toString();
    }

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
    static void ADD(String cmd, String args) {
        // Add logic here
    }
    static void REMOVE(String cmd, String args) {
        // Remove logic here
    }

    static void HELP(String cmd, String args) {
        System.out.println(
            "ADD id, full_name, course, year_level, gender - adds a row." + "\n" +
            "REMOVE [id=? || full_name=? || ...] - removes a row with given conditions. To clear the table, type 'REMOVE *'" + "\n" +
            "DISPLAY [id=? || full_name=? || ...] - displays the table with given conditions. To show the full table, type 'DISPLAY *'"
        );
    }

    static void DISPLAY(String cmd, String args) {
        String DELIMITER = ",";
        List<String[]> data = new ArrayList<>();
        int[] maxColumnLengths = null;

        // read CSV file
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("./saved/TableData.csv"));
        } catch(IOException e) { // if TableData.csv is not found, use TableDataSample.csv
            System.out.print("\n\nPrimary table data file not found. Using example file.\n\n");
            try {
                br = new BufferedReader(new FileReader("./saved/TableDataSample.csv"));
            } catch(IOException exception) {
                System.out.print("\n\nTable data file not found." + " " + exception + "\n\n");
                return;
            }
        }

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                if (maxColumnLengths == null) {
                    maxColumnLengths = new int[values.length];
                }
                for (int i=0; i<values.length; i++) {
                    maxColumnLengths[i] = values[i].length() > maxColumnLengths[i] ? values[i].length() : maxColumnLengths[i];
                }
    
                data.add(values);
            }
        } catch(IOException e) {
            System.out.print("\n\n" + e + "\n\n");
        }

        // Create row line
        String rowSeparator = ProcessCommands.createRowLine("-", "+", maxColumnLengths);

        // Display data
        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            System.out.format(rowSeparator);

            for (int j = 0; j < row.length; j++) {
                System.out.format("| %-" + maxColumnLengths[j] + "s ", row[j]);
            }
            System.out.format("|%n");
        }
        System.out.format(rowSeparator);
        System.out.println();
    }

    static String MODIFY(String cmd, String args) {
        return cmd + " " + args + " (Work in progress)";
    }
}


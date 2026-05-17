import java.lang.reflect.Method;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class ProcessCommands {
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
    
    public String process(String str) {
        String[] parts = str.split("\\s+", 2); // get whitespace
        String cmd = parts[0].toUpperCase(), args = (parts.length > 1) ? parts[1] : "";

        try {
            Method method = Command.class.getDeclaredMethod(cmd, String.class, String[].class);
            Object res = method.invoke(null, cmd, Parser.parse(args));
            return (res instanceof String) ? (String) res : cmd + " Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Command not found! ";
        }
    }
}

// Work in progress
class Parser {
    static String[] parse(String args) {
        if (args == null || args.trim().isEmpty()) {
            return new String[0];
        }
        
        String[] com_part = args.split(",");

        for(int i = 0; i<com_part.length; i++) {
            com_part[i] = com_part[i].trim();
        }

        return com_part; 
    }
}

// handle CSV logic here e.g reading, adding, removing data
class CSV {
    public List<String[]> data;
    public String[] header;

    CSV(String filePath) { // initialize CSV
        String DELIMITER = ",";

        this.data = new ArrayList<>();
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(filePath));
            this.header = br.readLine().split(DELIMITER);

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                data.add(values);                
            }
        } catch(IOException e) {
            System.out.print(e);
        }
    }

    public int getHeaderIdx(String headerName) {
        for (int i = 0; i < this.header.length; i++) {
            if (this.header[i].equals(headerName)) return i; 
        }
        return -1;
    }

    public int[] getColumnLengths(int[] headerIdx) {
        int[] lengths = new int[headerIdx.length];
        for (int i = 0; i < headerIdx.length; i++) {
            int idx = headerIdx[i];
            lengths[i] = this.header[idx].length();
        }
        return lengths;
    }
}


class Command {
    static void ENROLL(String cmd, String[] args) {
        // Add logic here
    }
    static void UNENROLL(String cmd, String[] args) {
        // Remove logic here
    }

    static void HELP(String cmd, String[] args) {
        System.out.println(
            "ADD id, full_name, course, year_level, gender - adds a row." + "\n" +
            "REMOVE [id=? || full_name=? || ...] - removes a row with given conditions. To clear the table, type 'REMOVE *'" + "\n" +
            "DISPLAY [id=? || full_name=? || ...] - displays the table with given conditions. To show the full table, type 'DISPLAY *'"
        );
    }

    static String DISPLAY(String cmd, String[] args) {
        if (args.length == 0) {
            return "Argument/s needed for display";
        }

        // Parse arguments
        CSV csvData = new CSV("./saved/TableDataSample.csv");
        
        List<Integer> headerList = new ArrayList<Integer>();
        List<Object[]> filterRow = new ArrayList<Object[]>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            int idx = csvData.getHeaderIdx(arg);
            if (arg.charAt(0) == '*') {
                for (int j = 0; j < csvData.header.length; j++) {
                    headerList.add(j);
                }
            } else if (idx != -1) {
                headerList.add(idx);
            } else if (arg.contains("=")) {
                String[] parts = arg.split("=");
                String key = parts[0];
                String value = parts[1];

                idx = csvData.getHeaderIdx(key);

                if (idx == -1) {
                    System.out.println("header \"" + key + "\" not found.");
                    continue;
                }

                // [index, value]
                Object[] filterValues = new Object[2];
                filterValues[0] = idx;
                filterValues[1] = value;

                filterRow.add(filterValues);
            } else {
                System.out.println("header \"" + arg + "\" not found.");
            }
        }

        if (headerList.size() == 0) {
            return "No valid header data";
        }
        
        // convert Integer list to int array
        int[] header = headerList.stream().mapToInt(Integer::valueOf).toArray();
        int[] maxColumnLengths = csvData.getColumnLengths(header);

        // Create row line
        String rowSeparator = ProcessCommands.createRowLine("-", "+", maxColumnLengths);

        // Display header
        System.out.format(rowSeparator);
        for (int i = 0; i < header.length; i++) {
            String h = csvData.header[header[i]];
            System.out.format("| %-" + maxColumnLengths[i] + "s ", h);
        }
        System.out.format("|%n");

        // Display data
        for (int i = 0; i < csvData.data.size(); i++) {
            String[] row = csvData.data.get(i);
            
            boolean match = true;
            for (int j = 0; j < filterRow.size(); j++) {
                int idx = (int) filterRow.get(j)[0];
                String value = (String) filterRow.get(j)[1];

                if (!value.equals("*") && !row[idx].equals(value)) {
                    match = false;
                    break;
                }
            }
            
            if (!match) continue;
            System.out.format(rowSeparator);

            for (int j = 0; j < header.length; j++) {
                String data = row[header[j]];
                System.out.format("| %-" + maxColumnLengths[j] + "s ", data);
            }
            System.out.format("|%n");
        }
        System.out.format(rowSeparator);
        return "";
    }

    static String MODIFY(String cmd, String args) {
        return cmd + " " + args + " (Work in progress)";
    }
}
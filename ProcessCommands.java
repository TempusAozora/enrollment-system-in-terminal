import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class ProcessCommands {
    public String process(String str) {
        Token[] tokens;
        try { // tokenize the string
            tokens = Token.tokenize(str);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        
        String cmd = tokens[0].value.toUpperCase();

        try { // run the method from input command.
            Method method = Command.class.getDeclaredMethod(cmd, String.class, Token[].class);
            Object res = method.invoke(null, cmd, tokens);
            return (res instanceof String) ? (String) res : cmd + " Success";
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace(); 
            return "Command not found!";
        }
    }
}

// getColumnLengths(): For display command. From array of indices, get the length of each column for display.
class Formatter {
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

    public static int[] getColumnLengths(String[][] data) {
        int[] lengths = new int[data[0].length];
        for (int i = 0; i < data.length; i++) {
            String[] row = data[i];
            for (int j = 0; j < row.length; j++) {
                lengths[j] = row[j].length() > lengths[j] ? row[j].length() : lengths[j];
            }
        }

        return lengths;
    }
}

// Update: class Parser -> class Token
// Break command into chunks or tokens to parse like a language
// Also detects syntax errors in user input
// Example input: DISPLAY student_id, full_name WHERE full_name=foo"
// Output: [
//     Token("COMMAND", "DISPLAY"),
//     Token("IDENTIFIER", "student_id"),
//     Token("COMMA", ","),
//     Token("IDENTIFIER", "full_name"),
//     Token("WHERE", "WHERE"),
//     Token("IDENTIFIER", "student_id"),
//     Token("OPERATOR", "="),
//     Token("STRING", "foo") or TOKEN("NUMBER", 123) if input is number 
// ]

// COMMAND is the first word in the input.
// IDENTIFIER is a keyword that represents the column name.
// OPERATOR are operators such as the equal sign
class Token {
    public String type;
    public String value;

    Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    static Token[] tokenize(String input) {
        int i = 0;
        ArrayList<Token> output = new ArrayList<>(); // ArrayList first since the data will be dynamic

        while (i < input.length()) {
            char inpChar = input.charAt(i);
            if (Character.isWhitespace(inpChar)) { // Handle whitespace
                i++;
                continue; // ignore white space characters.
            }

            if (Character.isAlphabetic(inpChar)) { // Handle alphabetic characters
                StringBuilder value = new StringBuilder(); // using string builder for optimization in appending char to string.
                while (i < input.length() && (Character.isLetter(input.charAt(i)) || input.charAt(i) == '_')) {
                    value.append(input.charAt(i));
                    i++;
                }

                if (output.size() == 0) {
                    output.add(new Token("COMMAND", value.toString())); // Treat first word as command.
                } else if (value.toString().toUpperCase().equals("WHERE")) {
                    output.add(new Token("WHERE", value.toString()));
                } else {
                    output.add(new Token("IDENTIFIER", value.toString())); // Treat the rest as identifiers.
                }
                continue;
            }

            if (inpChar == ',') { // Handle comma
                output.add(new Token("COMMA", Character.toString(inpChar)));
                i++;
                continue;
            }

            if (inpChar == '"' || inpChar == '\'') { // Handle strings
                i++;
                if (i < input.length()) {
                    StringBuilder value = new StringBuilder(); // string builder to append
                    while (i < input.length() && input.charAt(i) != inpChar) { // only stop when it goes beyond input or it detects quotation marks.
                        value.append(input.charAt(i));
                        i++;
                    }

                    if (i >= input.length()) { // detect unterminated string literal
                        throw new RuntimeException("Unterminated string literal");
                    }
                    
                    output.add(new Token("STRING", value.toString()));
                    i++;
                }

                continue;
            }

            if (Character.isDigit(inpChar)) { // Handle digits
                StringBuilder value = new StringBuilder(); // string builder to append
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    value.append(input.charAt(i));
                    i++;
                }
                output.add(new Token("NUMBER", value.toString()));
                continue;
            }

            if (inpChar == '=') { // operators also include arithmetic but for now, equal sign is only used.
                output.add(new Token("OPERATOR", Character.toString(inpChar)));
                i++;
                continue;
            }

            if (inpChar == '*') {
                output.add(new Token("STAR", Character.toString(inpChar)));
                i++;
                continue;
            }

            throw new RuntimeException("Command not found or invalid syntax!"); // if it does not pass to any conditional statements. Return error.
        }

        return output.toArray(new Token[output.size()]);
    }
}

// handle TSV logic here e.g reading, adding, removing data
// getHeaderIdx(): For display command. Get index from header name. Return -1 if not found.
class TSV {
    public List<String[]> data;
    public String[] header;

    TSV(String filePath) { // initialize TSV
        this.data = new ArrayList<>();
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(filePath));
            this.header = br.readLine().split("\t", -1);

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t", -1);
                this.data.add(values);                
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
}


class Command {
    static void ENROLL(String cmd, Token[] tokens) {
        // Add logic here
    }
    static void UNENROLL(String cmd, Token[] tokens) {
        // Remove logic here
    }

    static String HELP(String cmd, Token[] tokens) {
        return "\n" +
            "DISPLAY [header_name, ... || *] WHERE [header_name = \"value\", ...] - displays the table with given conditions. To show the full table, type 'DISPLAY *'" + "\n" +
            "ENROLL (work in progress)" + "\n" +
            "UNENROLL (work in progress)" + "\n";
    }

    static String DISPLAY(String cmd, Token[] tokens) {
        if (tokens.length == 0) return "Argument/s needed for DISPLAY command";

        // Parse token and check for syntax errors.
        TSV tsvData = new TSV("./saved/TableDataSample.tsv");
        
        List<Integer> headerList = new ArrayList<Integer>();
        List<Object[]> filterRow = new ArrayList<Object[]>();

        int i_parser = 1; // start at 1
        while ( // Parse identifiers and star and commas
                i_parser < tokens.length && 
                (tokens[i_parser].type == "IDENTIFIER" || tokens[i_parser].type == "STAR")
            ) {
            int idx = tsvData.getHeaderIdx(tokens[i_parser].value); // get index of identifier

            if (tokens[i_parser].type == "STAR")
                for (int j = 0; j < tsvData.header.length; j++) headerList.add(j); // add all headers
            else if (idx != -1) headerList.add(idx); // add header

            i_parser++;
            if (i_parser < tokens.length && tokens[i_parser].type == "COMMA") { // ignore comma
                i_parser++;
                if ( // detect invalid syntax after comma
                    i_parser < tokens.length &&
                    !(tokens[i_parser].type == "IDENTIFIER" || tokens[i_parser].type == "STAR")
                ) return "Invalid syntax near DISPLAY. Invalid input after a comma.";
            } else break;
        }

        if (headerList.size() == 0) return "No valid header data";
        if (i_parser < tokens.length) { 
            if (!tokens[i_parser].type.equals("WHERE")) 
                return "Invalid syntax after DISPLAY. Use WHERE to filter rows.";
            i_parser++;
            if (i_parser >= tokens.length) return "Invalid syntax after WHERE. Empty field.";

            while (i_parser < tokens.length && tokens[i_parser].type == "IDENTIFIER") { // Parse WHERE clause
                if (i_parser+2 >= tokens.length) return "Invalid syntax near WHERE. Incomplete input";

                // example: student_id = 1
                String identifier = tokens[i_parser].value; 
                String equalSign = tokens[i_parser+1].value;
                String value = tokens[i_parser+2].value;

                if (!equalSign.equals("=")) return "Invalid syntax near WHERE. Use equal sign.";
                int idx = tsvData.getHeaderIdx(identifier);

                if (!( // catch invalid type
                    tokens[i_parser+2].type.equals("STRING") || 
                    tokens[i_parser+2].type.equals("NUMBER")
                )) return "Invalid syntax near WHERE. Invalid input. Make sure to use numbers or enclose characters in quotation marks.";
                
                if (idx == -1) System.out.println("header \"" + identifier + "\" not found. Skipping.");
                else { // check first if value is a string or number. Then add.
                    Object[] filterValues = new Object[2];
                    filterValues[0] = idx;
                    filterValues[1] = value;

                    filterRow.add(filterValues);
                }

                i_parser += 3;
                if (i_parser < tokens.length && tokens[i_parser].type == "COMMA") {
                    i_parser++;
                    if ( // detect invalid syntax after comma
                        i_parser < tokens.length &&
                        !(tokens[i_parser].type == "IDENTIFIER")
                    ) return "Invalid syntax near WHERE. Invalid input after a comma.";
                } else break;
            }
        }
        
        // convert Integer list to int array
        int[] header = headerList.stream().mapToInt(Integer::valueOf).toArray();
        
        // Create filtered data
        ArrayList<String[]> dataFiltered = new ArrayList<>();

        dataFiltered.add(new String[header.length]);
        for (int i = 0; i < header.length; i++) {
            dataFiltered.get(0)[i] = tsvData.header[header[i]];
        }

        for (int i = 0; i < tsvData.data.size(); i++) {
            String[] row = tsvData.data.get(i);
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

            dataFiltered.add(new String[header.length]);
            for (int j = 0; j < header.length; j++) {
                String data  = row[header[j]];
                dataFiltered.get(dataFiltered.size()-1)[j] = data;
            }
        }

        // Formatting
        int[] maxColumnLengths = Formatter.getColumnLengths(dataFiltered.toArray(new String[0][]));
        String rowSeparator = Formatter.createRowLine("-", "+", maxColumnLengths);

        // Display table
        for (int i = 0; i < dataFiltered.size(); i++) {
            System.out.format(rowSeparator);
            String[] row = dataFiltered.get(i);
            for (int j = 0; j < row.length; j++) {
                System.out.format("| %-" + maxColumnLengths[j] + "s ", row[j]);
            }
            System.out.format("|%n"); // newline
        }
        System.out.format(rowSeparator);
        return "";
    }

    static String MODIFY(String cmd, Token[] tokens) {
        return "(Work in progress)";
    }
}
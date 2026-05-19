
import java.util.ArrayList;

public class Command {
    static void ENROLL(Token[] tokens, TSV tsvData) {
        // Add logic here
    }
    static void UNENROLL(Token[] tokens, TSV tsvData) {
        // Remove logic here
    }

    static String HELP(Token[] tokens, TSV tsvData) {
        return "\n" +
            "DISPLAY [header_name, ... || *] WHERE [header_name = \"value\", ...] - displays the table with given conditions. To show the full table, type 'DISPLAY *'" + "\n" +
            "ENROLL (work in progress)" + "\n" +
            "UNENROLL (work in progress)" + "\n";
    }

    static String DISPLAY(Token[] tokens, TSV tsvData) {
        if (tokens.length == 1) return "Argument/s needed for DISPLAY command";

        // Parse token and check for syntax errors.
        ArrayList<Integer> headerList = new ArrayList<Integer>();
        ArrayList<Object[]> filterRow = new ArrayList<Object[]>();

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
                ) return "Invalid syntax near DISPLAY. Trailing comma.";
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
                    ) return "Invalid syntax near WHERE. Trailing comma.";
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

    static String MODIFY(Token[] tokens, TSV tsvData) {
        if (tokens.length == 1) return "Argument/s needed for MODIFY command";
        // Parse token and check for syntax errors.
        int i_parser = 1; // start at 1

        ArrayList<Object[]> Values = new ArrayList<Object[]>(); // [[columnIdx, value]] for setting values
        ArrayList<Object[]> filterRow = new ArrayList<Object[]>(); // [[columnIdx, value]] for filtering rows

        // MODIFY command uses while loop just like WHERE, but slightly modified
        while (i_parser < tokens.length && tokens[i_parser].type == "IDENTIFIER") { // Parse identifiers and commas
            if (i_parser+2 >= tokens.length) return "Invalid syntax near MODIFY. Incomplete input";

            // example: student_id = 1
            String identifier = tokens[i_parser].value; 
            String equalSign = tokens[i_parser+1].value;
            String value = tokens[i_parser+2].value;

            if (!equalSign.equals("=")) return "Invalid syntax near MODIFY. Use equal sign.";
            int idx = tsvData.getHeaderIdx(identifier);

            if (!( // catch invalid type
                tokens[i_parser+2].type.equals("STRING") || 
                tokens[i_parser+2].type.equals("NUMBER")
            )) return "Invalid syntax near MODIFY. Invalid input. Make sure to use numbers or enclose characters in quotation marks.";
            
            if (idx == -1) System.out.println("header \"" + identifier + "\" not found. Skipping.");
            else { // check first if value is a string or number. Then add.
                Object[] setValues = new Object[2];
                setValues[0] = idx;
                setValues[1] = value;

                Values.add(setValues);
            }

            i_parser += 3;
            if (i_parser < tokens.length && tokens[i_parser].type == "COMMA") {
                i_parser++;
                if ( // detect invalid syntax after comma
                    i_parser < tokens.length &&
                    !(tokens[i_parser].type == "IDENTIFIER")
                ) return "Invalid syntax near WHERE. Trailing comma.";
            } else break;
        }

        if (Values.size() == 0) return "No valid header data";
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
                    ) return "Invalid syntax near WHERE. Trailing comma.";
                } else break;
            }
        }

        // Construct object table
        ArrayList<Object[]> modifiedRows = new ArrayList<>();

        for (int i = 0; i < tsvData.data.size(); i++) {
            String[] row = tsvData.data.get(i);
            boolean match = true;

            for (int j = 0; j < filterRow.size(); j++) {
                int idx = (int) filterRow.get(j)[0];
                String value = (String) filterRow.get(j)[1];

                if (!row[idx].equals(value)) {
                    match = false;
                    break;
                }
            }
            
            if (!match) continue;
            int[] columnIndices = new int[Values.size()];
            String[] values = new String[Values.size()];

            for (int j = 0; j < Values.size(); j++) {
                columnIndices[j]  = (int) Values.get(j)[0];
                values[j] = (String) Values.get(j)[1];
            }

            Object[] newObj = new Object[]{i, columnIndices, values};
            modifiedRows.add(newObj);
        }

        if (modifiedRows.size() == 0) return "No data modified.";

        tsvData.modify(modifiedRows.toArray(new Object[0][]));// modify 


        return "MODIFY success.";
    }
}

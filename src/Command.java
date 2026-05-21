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
            "DISPLAY [header_name, ... || *] WHERE [header_name = \"value\", ...] - displays the table with given conditions. To show the full table, type \"DISPLAY *\"" + "\n" +
            "MODIFY [header_name = \"value\" WHERE [header_name = \"value\", ...] - change value of a column data. WHERE is used to filter rows." + "\n" +
            "ENROLL (work in progress)" + "\n" +
            "UNENROLL (work in progress)" + "\n";
    }

    static String DISPLAY(Token[] tokens, TSV tsvData) {
        if (tokens.length == 1) return "Argument/s needed for DISPLAY command";

        // Parse token and check for syntax errors.
        ArrayList<Integer> headerList = new ArrayList<Integer>();
        ConditionChecker conditionChecker = new ConditionChecker();
        ArrayList<Integer> sortIdx = new ArrayList<Integer>();
        Boolean isAscending = true; // ascending by default

        Parser parser = new Parser(tokens);
        parser.consume("COMMAND", "Expected Display Command");

        while (true) { // parse identifiers
            Token input = parser.consumeAny(new String[]{"IDENTIFIER", "STAR"}, "Invalid Syntax. DISPLAY expects column name or *");
            if (input.type == "STAR") {
                for (int j = 0; j < tsvData.header.length; j++) headerList.add(j);
            } else {
                int idx = tsvData.getHeaderIdx(input.value);
                if (idx != -1) headerList.add(idx);
                else System.out.println("Header name not found. Skipping");
            }
            if (!parser.match("COMMA")) break;
            parser.consume("COMMA", "Invalid Syntax. Comma expected near DISPLAY.");
        }

        if (headerList.size() == 0) return "No valid header column data.";

        Boolean whereConsumed = false; // avoid duplicate use of WHERE
        Boolean sortConsumed = false; // avoid duplicate use of SORT BY

        while (!parser.finished() && (!whereConsumed || !sortConsumed)) {
            if (parser.match("WHERE")) {
                parser.consume("WHERE", "Invalid Syntax. Expects WHERE or SORT BY");
                while (true) { // parse where
                    Token identifier = parser.consume("IDENTIFIER", "Invalid Syntax. Header column name expected near WHERE.");
                    Token relOperator = parser.consumeAny(new String[]{"REL_OPERATOR", "LIKE"}, "Invalid Syntax. Operator expected expected near WHERE.");
                    Token stringToken = parser.consumeAny(new String[]{"STRING", "NUMBER"}, "Invalid Syntax. String or number expected. Make sure to enclose string in quotation marks \"\".");
                    
                    int idx = tsvData.getHeaderIdx(identifier.value);
                    Boolean match = parser.match("LOG_OPERATOR");
                    String logOperator = match ? parser.getCurrentToken().value : "NONE";
                    
                    if (idx == -1) System.out.println("header \"" + identifier + "\" not found for WHERE. Skipping.");
                    else conditionChecker.add(idx, relOperator.value, stringToken.value, logOperator);
                    
                    if (!match) break;
                    parser.consume("LOG_OPERATOR", "Invalid Syntax. Expects AND OR");
                }
                whereConsumed = true;
            } else if (parser.match("SORT")) {
                parser.consume("SORT", "Invalid Syntax. Expects WHERE or SORT BY");
                parser.consume("BY", "Invalid Syntax. Expects WHERE or SORT BY");
                
                while (true) { // parse sort by
                    Token identifier = parser.consume("IDENTIFIER", "Invalid Syntax. Header column name expected near SORT BY.");
                    int idx = tsvData.getHeaderIdx(identifier.value);

                    if (idx == -1) System.out.println("header \"" + identifier + "\" not found for SORT BY. Skipping.");
                    else sortIdx.add(idx);

                    if (!parser.match("COMMA")) break;
                    parser.consume("COMMA", "Invalid Syntax. Comma expected near SORT BY.");   
                }
                if (parser.match("SORT_ORDER")) { // parse order. Ascending by default
                    Token order = parser.consume("SORT_ORDER", "Invalid Syntax. Expects ASCENDING or DESCENDING");
                    isAscending = order.value.equals("ASCENDING");
                }
                sortConsumed = true;
            }
        }
                
        int[] header = headerList.stream().mapToInt(Integer::valueOf).toArray();    // convert Integer list to int array        
        
        ArrayList<String[]> rowFiltered = new ArrayList<String[]>();                        // Filter rows then sort
        ArrayList<String[]> dataFiltered = new ArrayList<String[]>();                       // Filter columns

        for (int i = 0; i < tsvData.data.size(); i++) {
            String[] row = tsvData.data.get(i);
            if (!conditionChecker.checkRow(row)) continue;
            
            rowFiltered.add(row);
        }

        final Boolean isAscendingFinal = isAscending; // declare final variable whether it is ascending or not.
        if (!sortIdx.isEmpty()) {
            rowFiltered.sort((rowA, rowB) -> { // sort rows
                for (int i = 0; i < sortIdx.size(); i++) {
                    int idx = sortIdx.get(i);
                    int order = isAscendingFinal ? 1 : -1;
                    
                    String a = rowA[idx];
                    String b = rowB[idx];

                    int result = 0;

                    // check if numeric using regex
                    boolean aIsNumeric = a.matches("-?\\d+");
                    boolean bIsNumeric = b.matches("-?\\d+");

                    if (aIsNumeric && bIsNumeric) result = order * Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                    else result = order * rowA[idx].compareToIgnoreCase(rowB[idx]);
                            
                    if (result != 0) return result;
                }
                return 0;
            });
        }

        // Finalize table data for display
        dataFiltered.add(new String[header.length]); // header
        for (int i = 0; i < header.length; i++) {
            dataFiltered.get(0)[i] = tsvData.header[header[i]];
        }

        for (int i = 0; i < rowFiltered.size(); i++) { // data
            String[] row = rowFiltered.get(i);
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
        ArrayList<Object[]> Values = new ArrayList<Object[]>();
        ConditionChecker conditionChecker = new ConditionChecker();

        Parser parser = new Parser(tokens);
        parser.consume("COMMAND", "Expected Modify Command");        // MODIFY command uses while loop just like WHERE, but slightly modified
        while (true) { // parse MODIFY
            Token identifier = parser.consume("IDENTIFIER", "Invalid Syntax. Header column name expected near MODIFY.");
            Token relOperator = parser.consumeAny(new String[]{"REL_OPERATOR", "LIKE"}, "Invalid Syntax. Operator expected expected near MODIFY.");
            Token stringToken = parser.consumeAny(new String[]{"STRING", "NUMBER"}, "Invalid Syntax. String or number expected. Make sure to enclose string in quotation marks \"\".");

            int idx = tsvData.getHeaderIdx(identifier.value);
            if (idx == -1) System.out.println("header \"" + identifier + "\" not found MODIFY. Skipping.");
            else Values.add(new Object[]{idx, stringToken.value});

            if (!parser.match("COMMA")) break;
            parser.consume("COMMA", "Invalid Syntax. Expects comma near MODIFY.");
        }

        if (!parser.finished()) {
            parser.consume("WHERE", "Invalid Syntax. Expects WHERE or SORT BY");
             while (true) { // parse WHERE 
                Token identifier = parser.consume("IDENTIFIER", "Invalid Syntax. Header column name expected near WHERE.");
                Token relOperator = parser.consumeAny(new String[]{"REL_OPERATOR", "LIKE"}, "Invalid Syntax. Operator expected expected near WHERE.");
                Token stringToken = parser.consumeAny(new String[]{"STRING", "NUMBER"}, "Invalid Syntax. String or number expected. Make sure to enclose string in quotation marks \"\".");
    
                int idx = tsvData.getHeaderIdx(identifier.value);
                Boolean match = parser.match("LOG_OPERATOR");
                String logOperator = match ? parser.getCurrentToken().value : "NONE";
                
                if (idx == -1) System.out.println("header \"" + identifier + "\" not found WHERE. Skipping.");
                else conditionChecker.add(idx, relOperator.value, stringToken.value, logOperator);
    
                if (!match) break;
                parser.consume("LOG_OPERATOR", "Invalid Syntax. Expects AND or OR");
             }
        }

        // Construct object table
        ArrayList<Object[]> modifiedRows = new ArrayList<>();
        for (int i = 0; i < tsvData.data.size(); i++) {
            String[] row = tsvData.data.get(i);
            if (!conditionChecker.checkRow(row)) continue;

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

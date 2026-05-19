

import java.util.ArrayList;

// Breaks command into chunks or tokens to parse like a language
// Array of tokens are used to properly detect syntax errors and be more flexible.
// Example input: DISPLAY student_id, full_name WHERE full_name=foo"
// Output from Token.tokenize(input): [
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
// STRINGS are enclosed by quotation marks "" or ''

public class Token {
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
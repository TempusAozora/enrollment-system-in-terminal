

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Entry point of command processing
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
            Method method = Command.class.getDeclaredMethod(cmd,Token[].class, TSV.class);
            TSV tsvData = new TSV("../saved/TableDataSample.tsv");
            Object res = method.invoke(null, tokens, tsvData);
            return (res instanceof String) ? (String) res : cmd + " Success";
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // e.printStackTrace();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                return e.getCause().getMessage(); // catch runtime expections
            } 
            return "Command not found!";
        }
    }
}
// Exclusively for display command.
public class Formatter {
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
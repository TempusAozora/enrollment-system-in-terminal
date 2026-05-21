import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// For where clause
public class ConditionChecker {
	ArrayList<Condition> conditions = new ArrayList<Condition>();

	public void add(int idx, String relOp, String value, String logOp) {
		Condition condition = new Condition(idx, relOp, value, logOp);
		conditions.add(condition);
	}
	private Boolean check(Condition condition, String rowValue) {
		switch (condition.relOperator) {
			case "=":
				return rowValue.equals(condition.value);
			case ">":
				try {return Integer.parseInt(rowValue) > Integer.parseInt(condition.value);} 
				catch (NumberFormatException e) {return false;}
			case "<":
				try {return Integer.parseInt(rowValue) < Integer.parseInt(condition.value);} 
				catch (NumberFormatException e) {return false;}
			case ">=":
				try {return Integer.parseInt(rowValue) >= Integer.parseInt(condition.value);} 
				catch (NumberFormatException e) {return false;}
			case "<=":
				try {return Integer.parseInt(rowValue) <= Integer.parseInt(condition.value);} 
				catch (NumberFormatException e) {return false;}
			case "LIKE":
				Pattern pattern = Pattern.compile(condition.value, Pattern.LITERAL);
				Matcher matcher = pattern.matcher(rowValue);
				return matcher.find();
			default:
				throw new RuntimeException("invalid relation operator");
		}
	}

	public Boolean checkRow(String[] row) {
		if (conditions.isEmpty()) return true;
		Boolean currBool = check(conditions.get(0), row[conditions.get(0).idx]);

		for (int i = 0; i<conditions.size()-1; i++) {
			Condition con = conditions.get(i);
			Condition nextCon = conditions.get(i+1);

			Boolean boolVal = check(nextCon, row[nextCon.idx]);

			if (con.logOperator.equals("AND")) {
				currBool = currBool && boolVal;
			} else if (con.logOperator.equals("OR")) {
				currBool = currBool || boolVal;
			}
		}

		return currBool;
	} 
}

class Condition {
	int idx; // row idx
	String relOperator; // =, LIKE, >, <, etc.
	String value; // string or number
	String logOperator; // AND, OR

	Condition(int idx, String relOp, String value, String logOp) {
		this.idx = idx;
		this.relOperator = relOp;
		this.value = value;
		this.logOperator = logOp;
	}
}

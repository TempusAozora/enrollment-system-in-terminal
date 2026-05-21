public class Parser {
	private Token[] tokens;
	private int current = 0;

	Parser(Token[] tokens) {
		this.tokens = tokens;
	}

	public Token getCurrentToken() {
		return tokens[current];
	}

	public boolean finished() {
		return current >= tokens.length;
	}

	public Token proceed() {
		if (!finished()) current++;
		return tokens[current - 1];
	}

	public boolean match(String... types) {
		if (finished()) return false;
		for (String type : types) {
			if (getCurrentToken().type.equals(type)) {
				return true;
			}
		}
		return false;
	}

	public Token consumeAny(String[] types, String exceptionMsg) {
		if (match(types)) {
			Token prevToken = proceed();
			return prevToken;
		}
		
		throw new RuntimeException(exceptionMsg);
	}

	public Token consume(String type, String exceptionMsg) {
		if (match(type)) {
			Token prevToken = proceed();
			return prevToken;
		}
		throw new RuntimeException(exceptionMsg);
	}
}

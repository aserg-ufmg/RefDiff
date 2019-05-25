package refdiff.core.cst;

import java.util.List;

public class TokenizedSource {
	
	public static final int START = 0;
	public static final int END = 1;
	private final String file;
	private final int[][] tokens;
	
	public TokenizedSource(String file, List<TokenPosition> tokens) {
		this.file = file;
		this.tokens = new int[tokens.size()][2];
		for (int i = 0; i < tokens.size(); i++) {
			TokenPosition token = tokens.get(i);
			this.tokens[i][START] = token.getStart();
			this.tokens[i][END] = token.getEnd();
		}
	}
	
	public String getFile() {
		return file;
	}
	
	public int[][] getTokens() {
		return tokens;
	}
	
}

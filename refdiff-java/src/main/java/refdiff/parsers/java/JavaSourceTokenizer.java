package refdiff.parsers.java;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import refdiff.core.cst.TokenPosition;

public class JavaSourceTokenizer {
	
	private IScanner scanner = ToolFactory.createScanner(true, true, false, "1.8");
	
	public List<TokenPosition> tokenize(char[] charArray) {
		try {
			scanner.setSource(charArray);
			List<TokenPosition> tokens = new ArrayList<>();
			int token;
			while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				int tokenStart = scanner.getCurrentTokenStartPosition();
				int tokenEnd = scanner.getCurrentTokenEndPosition();
				if (token == ITerminalSymbols.TokenNameCOMMENT_LINE || token == ITerminalSymbols.TokenNameCOMMENT_BLOCK || token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
					tokenizeComment(charArray, tokenStart, tokenEnd + 1, tokens);
				} else if (isSignificantToken(token)) {
					tokens.add(new TokenPosition(tokenStart, tokenEnd + 1));
				}
			}
			return tokens;
		} catch (InvalidInputException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean isSignificantToken(int token) {
		return token != ITerminalSymbols.TokenNameWHITESPACE/* &&
			token != ITerminalSymbols.TokenNameSEMICOLON &&
			token != ITerminalSymbols.TokenNameCOMMA &&
			token != ITerminalSymbols.TokenNameLBRACKET &&
			token != ITerminalSymbols.TokenNameRBRACKET &&
			token != ITerminalSymbols.TokenNameRBRACE &&
			token != ITerminalSymbols.TokenNameLBRACE &&
			token != ITerminalSymbols.TokenNameDOT*/;
	}
	
	private static Pattern pattern = Pattern.compile("\\S+");
	
	private static void tokenizeComment(char[] charArray, int start, int end, List<TokenPosition> tokens) {
		CharBuffer comment = CharBuffer.wrap(charArray, start, end - start);
		Matcher matcher = pattern.matcher(comment);
		
		while (matcher.find()) {
			String token = matcher.group();
			if (!token.equals("*")) {
				tokens.add(new TokenPosition(start + matcher.start(), start + matcher.end()));
			}
		}
	}
}

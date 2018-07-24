package refdiff.parsers.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import refdiff.core.rast.TokenPosition;

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
				if (token != ITerminalSymbols.TokenNameWHITESPACE) {
					tokens.add(new TokenPosition(tokenStart, tokenEnd + 1));
				}
			}
			return tokens;
		} catch (InvalidInputException e) {
			throw new RuntimeException(e);
		}
	}
	
}

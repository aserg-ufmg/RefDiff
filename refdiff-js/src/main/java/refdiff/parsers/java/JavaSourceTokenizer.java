package refdiff.parsers.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import refdiff.parsers.SourceTokenizer;

public class JavaSourceTokenizer implements SourceTokenizer {

	private IScanner scanner = ToolFactory.createScanner(true, true, false, "1.8");
	
	@Override
	public List<String> tokenize(String source) {
		try {
			char[] charArray = source.toCharArray();
            scanner.setSource(source.toCharArray());
            List<String> tokens = new ArrayList<>();
            int token;
            while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
                int tokenStart = scanner.getCurrentTokenStartPosition();
                int tokenEnd = scanner.getCurrentTokenEndPosition();
                if (token != ITerminalSymbols.TokenNameWHITESPACE) {
                	tokens.add(new String(charArray, tokenStart, tokenEnd - tokenStart + 1));
                }
            }
            return tokens;
        } catch (InvalidInputException e) {
            throw new RuntimeException(e);
        }
	}
	
}

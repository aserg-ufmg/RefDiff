package refdiff.parsers.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.IToken;

import refdiff.core.cst.TokenPosition;

public class TokenVisitor extends ASTGenericVisitor {

	private List<TokenPosition> tokenList;
	
	public TokenVisitor(List<TokenPosition> tokenList) {
		super(true);
		this.tokenList = tokenList;
	}
	
	@Override
	protected int genericVisit(IASTNode iastNode) {
		if (!this.tokenList.isEmpty()) {
			return PROCESS_ABORT;
		}
		
		IToken token = null;
		try {
			token = iastNode.getSyntax();
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException(e);
		} catch (ExpansionOverlapsBoundaryException e) {
			throw new RuntimeException(e);
		}
		
		if (this.tokenList.isEmpty() && token != null) {
			while (token != null) {
				this.tokenList.add(new TokenPosition(token.getOffset(), token.getEndOffset()));
				token = token.getNext();
			}
		}
		
		return PROCESS_CONTINUE;
	}
	
}

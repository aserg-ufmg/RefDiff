package refdiff.parsers.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;

public class TokenVisitor extends ASTGenericVisitor {

	private List<String> tokenList;
	
	public TokenVisitor(List<String> tokenList) {
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
				this.tokenList.add(token.getImage());
				token = token.getNext();
			}
		}
		
		return PROCESS_CONTINUE;
	}
	
}

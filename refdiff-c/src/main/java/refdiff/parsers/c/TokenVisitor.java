package refdiff.parsers.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.IToken;

public class TokenVisitor extends ASTGenericVisitor {

	private List<String> tokenList;
	
	public TokenVisitor(List<String> tokenList) {
		super(true);
		this.tokenList = tokenList;
	}
	
	@Override
	protected int genericVisit(IASTNode iastNode) {
		IToken token = null;
		try {
			token = iastNode.getSyntax();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (ExpansionOverlapsBoundaryException e) {
			e.printStackTrace();
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

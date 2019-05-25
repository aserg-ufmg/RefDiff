package refdiff.core.cst;

import java.util.List;

public interface HasChildrenNodes {
	
	List<CstNode> getNodes();
	
	void addNode(CstNode node);
}

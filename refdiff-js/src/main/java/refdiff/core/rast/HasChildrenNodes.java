package refdiff.core.rast;

import java.util.List;

public interface HasChildrenNodes {
	
	List<RastNode> getNodes();
	
	void addNode(RastNode node);
}

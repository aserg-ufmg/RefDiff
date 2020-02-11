package refdiff.core.cst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The Code Structure Tree (CST) root object.
 */
public class CstRoot implements HasChildrenNodes {
	
	private List<CstNode> nodes = new ArrayList<>();
	
	private Set<CstNodeRelationship> relationships = new HashSet<>();
	
	private Map<String, TokenizedSource> tokenizedSource = new HashMap<>();
	
	/**
	 * The top-level nodes of the CST.
	 */
	public List<CstNode> getNodes() {
		return nodes;
	}
	
	@Override
	public void addNode(CstNode node) {
		nodes.add(node);
	}
	
	public void addTokenizedFile(TokenizedSource tokenizedSource) {
		this.tokenizedSource.put(tokenizedSource.getFile(), tokenizedSource);
	}
	
	/**
	 * @return The relationships between nodes within the CST.
	 */
	public Set<CstNodeRelationship> getRelationships() {
		return relationships;
	}
	
	public void forEachNode(BiConsumer<CstNode, Integer> consumer) {
		forEachNodeInList(nodes, consumer, 0);
	}
	
	private void forEachNodeInList(List<CstNode> list, BiConsumer<CstNode, Integer> consumer, int depth) {
		for (CstNode node : list) {
			consumer.accept(node, depth);
			forEachNodeInList(node.getNodes(), consumer, depth + 1);
		}
	}

	/**
	 * @return A map of source file paths to tokenized source code.
	 */
	public Map<String, TokenizedSource> getTokenizedSource() {
		return tokenizedSource;
	}

}

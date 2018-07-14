package refdiff.core.rast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class RastRoot implements HasChildrenNodes {
	
	private List<RastNode> nodes = new ArrayList<>();
	
	private Set<RastNodeRelationship> relationships = new HashSet<>();
	
	private Map<String, TokenizedSource> tokenizedSource = new HashMap<>();
	
	public List<RastNode> getNodes() {
		return nodes;
	}
	
	@Override
	public void addNode(RastNode node) {
		nodes.add(node);
	}
	
	public void addTokenizedFile(TokenizedSource tokenizedSource) {
		this.tokenizedSource.put(tokenizedSource.getFile(), tokenizedSource);
	}
	
	public Set<RastNodeRelationship> getRelationships() {
		return relationships;
	}
	
	public void forEachNode(BiConsumer<RastNode, Integer> consumer) {
		forEachNodeInList(nodes, consumer, 0);
	}
	
	private void forEachNodeInList(List<RastNode> list, BiConsumer<RastNode, Integer> consumer, int depth) {
		for (RastNode node : list) {
			consumer.accept(node, depth);
			forEachNodeInList(node.getNodes(), consumer, depth + 1);
		}
	}

	public Map<String, TokenizedSource> getTokenizedSource() {
		return tokenizedSource;
	}

}

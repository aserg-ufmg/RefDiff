package refdiff.core.rast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class RastRoot implements HasChildrenNodes {
	
	private List<RastNode> nodes = new ArrayList<>();
	
	private Set<RastNodeRelationship> relationships = new HashSet<>();
	
	public List<RastNode> getNodes() {
		return nodes;
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
	
	public Optional<RastNode> findByNamePath(String... namePath) {
		return findByNamePathRecursive(nodes, 0, namePath);
	}
	
	private Optional<RastNode> findByNamePathRecursive(List<RastNode> list, int depth, String[] namePath) {
		if (depth >= namePath.length) {
			throw new IllegalArgumentException(String.format("depth should be less than namePath.length"));
		}
		String name = namePath[depth];
		for (RastNode node : list) {
			if (node.getLocalName().equals(name)) {
				if (depth == namePath.length - 1) {
					return Optional.of(node);
				} else {
					return findByNamePathRecursive(node.getNodes(), depth + 1, namePath);
				}
			}
		}
		return Optional.empty();
	}

}

package refdiff.core.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;

public class RastRootHelper<T> {
	
	private final RastRoot rastRoot;
	private final Map<Integer, RastNode> idMap = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> edges = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> reverseEdges = new HashMap<>();
	private final Map<RastNode, Integer> depthMap = new HashMap<>();
	
	public RastRootHelper(RastRoot rastRoot) {
		this.rastRoot = rastRoot;
		
		rastRoot.forEachNode((node, depth) -> {
			idMap.put(node.getId(), node);
			depthMap.put(node, depth);
		});
		
		for (RastNodeRelationship relationship : rastRoot.getRelationships()) {
			edges.compute(relationship.getN1(), (key, oldValue) -> {
				if (oldValue == null) {
					ArrayList<RastNodeRelationship> list = new ArrayList<>();
					list.add(relationship);
					return list;
				} else {
					oldValue.add(relationship);
					return oldValue;
				}
			});
			reverseEdges.compute(relationship.getN2(), (key, oldValue) -> {
				if (oldValue == null) {
					ArrayList<RastNodeRelationship> list = new ArrayList<>();
					list.add(relationship);
					return list;
				} else {
					oldValue.add(relationship);
					return oldValue;
				}
			});
		}
	}
	
	public int depth(RastNode node) {
		return depthMap.get(node);
	}
	
	public Collection<RastNode> findRelationships(RastNodeRelationshipType type, RastNode node) {
		return this.findRelationships(edges, type, node, rel -> idMap.get(rel.getN2()));
	}
	
	public Collection<RastNode> findReverseRelationships(RastNodeRelationshipType type, RastNode node) {
		return this.findRelationships(reverseEdges, type, node, rel -> idMap.get(rel.getN1()));
	}
	
	private Collection<RastNode> findRelationships(Map<Integer, List<RastNodeRelationship>> map, RastNodeRelationshipType type, RastNode node, Function<RastNodeRelationship, RastNode> mappingFn) {
		return map.getOrDefault(node.getId(), Collections.emptyList()).stream().map(mappingFn).collect(Collectors.toList());
	}
	
	public Optional<RastNode> findByNamePath(String... namePath) {
		return findByNamePath(rastRoot, namePath);
	}
	
	public static Optional<RastNode> findByNamePath(RastRoot rastRoot, String... namePath) {
		return findByNamePathRecursive(rastRoot.getNodes(), 0, namePath);
	}
	
	public static Optional<RastNode> findByFullName(HasChildrenNodes parent, String name) {
		for (RastNode node : parent.getNodes()) {
			if (fullName(node).equals(name)) {
				return Optional.of(node);
			}
		}
		return Optional.empty();
	}
	
	private static Optional<RastNode> findByNamePathRecursive(List<RastNode> list, int depth, String[] namePath) {
		if (depth >= namePath.length) {
			throw new IllegalArgumentException(String.format("depth should be less than namePath.length"));
		}
		String name = namePath[depth];
		for (RastNode node : list) {
			if (fullName(node).equals(name)) {
				if (depth == namePath.length - 1) {
					return Optional.of(node);
				} else {
					return findByNamePathRecursive(node.getNodes(), depth + 1, namePath);
				}
			}
		}
		return Optional.empty();
	}
	
	public static boolean sameName(RastNode n1, RastNode n2) {
		return !n1.getSimpleName().isEmpty() && n1.getSimpleName().equals(n2.getSimpleName());
	}
	
	public static boolean sameSignature(RastNode n1, RastNode n2) {
		return signature(n1).equals(signature(n2));
	}
	
	public static boolean sameNamespace(RastNode n1, RastNode n2) {
		return Objects.equals(n1.getNamespace(), n2.getNamespace());
	}
	
	public static String signature(RastNode n) {
		return n.getLocalName();
	}
	
	public static String fullName(RastNode n) {
		return n.getNamespace() != null ? n.getNamespace() + n.getLocalName() : n.getLocalName();
	}
	
	public static boolean anonymous(RastNode n) {
		return n.getSimpleName().isEmpty();
	}
	
	public static boolean sameType(RastNode n1, RastNode n2) {
		return n1.getType().equals(n2.getType());
	}
	
}

package refdiff.core.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;

public class RastRootHelper<T> {

	private final Map<Integer, RastNode> idMap = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> edges = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> reverseEdges = new HashMap<>();
	private final Map<RastNode, Integer> depthMap = new HashMap<>();
	
	public RastRootHelper(RastRoot rastRoot) {
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
}

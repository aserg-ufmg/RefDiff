package refdiff.core.diff;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import refdiff.core.diff.similarity.SourceRepresentationBuilder;
import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.Parameter;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.TokenizedSource;

public class RastRootHelper<T> {
	
	private final RastRoot rastRoot;
	private final Map<Integer, RastNode> idMap = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> edges = new HashMap<>();
	private final Map<Integer, List<RastNodeRelationship>> reverseEdges = new HashMap<>();
	private final Map<RastNode, Integer> depthMap = new HashMap<>();
	private final Map<String, String> fileMap = new HashMap<>();
	private final SourceRepresentationBuilder<T> srb;
	private final Map<RastNode, T> srMap = new HashMap<>();
	private final Map<RastNode, T> srBodyMap = new HashMap<>();
	private final boolean isBefore;
	
	public RastRootHelper(RastRoot rastRoot, SourceFileSet sources, SourceRepresentationBuilder<T> srb, boolean isBefore) throws IOException {
		this.rastRoot = rastRoot;
		this.srb = srb;
		this.isBefore = isBefore;
		
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
		
		for (SourceFile file : sources.getSourceFiles()) {
			fileMap.put(file.getPath(), sources.readContent(file));
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
		return map.getOrDefault(node.getId(), Collections.emptyList()).stream()
			.filter(rel -> rel.getType().equals(type))
			.map(mappingFn).collect(Collectors.toList());
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
	
	public static boolean leaf(RastNode n) {
		return n.getNodes().isEmpty();
	}
	
	public static boolean sameType(RastNode n1, RastNode n2) {
		return n1.getType().equals(n2.getType());
	}
	
	public void printRelationships(PrintStream out) {
		out.print("Relationships:\n");
		for (RastNodeRelationship rel : rastRoot.getRelationships()) {
			RastNode n1 = idMap.get(rel.getN1());
			RastNode n2 = idMap.get(rel.getN2());
			out.print(String.format("%s %s %s\n", n1.getLocalName(), rel.getType(), n2.getLocalName()));
		}
	}
	
	public void computeSourceRepresentation(RastNode node) {
		if (!srMap.containsKey(node)) {
			String sourceCode = fileMap.get(node.getLocation().getFile());
			List<String> nodeTokens = retrieveTokens(sourceCode, node, false);
			srMap.put(node, srb.buildForNode(node, isBefore, nodeTokens));
			
			if (node.getLocation().getBegin() != node.getLocation().getBodyBegin()) {
				List<String> nodeBodyTokens = retrieveTokens(sourceCode, node, true);
				T body = srb.buildForFragment(nodeBodyTokens);
				List<String> tokensToIgnore = new ArrayList<>();
				for (Parameter parameter : node.getParameters()) {
					tokensToIgnore.add(parameter.getName());
				}
				tokensToIgnore.addAll(getTokensToIgnoreInNodeBody(node));
				T normalizedBody = srb.minus(body, tokensToIgnore);
				srBodyMap.put(node, normalizedBody);
				
			} else {
				srBodyMap.put(node, srMap.get(node));
			}
		}
	}
	
	private List<String> retrieveTokens(String sourceCode, RastNode node, boolean bodyOnly) {
		return retrieveTokens(rastRoot, sourceCode, node, bodyOnly);
	}
	
	public static List<String> retrieveTokens(RastRoot rastRoot, String sourceCode, RastNode node, boolean bodyOnly) {
		Location location = node.getLocation();
		int nodeStart;
		int nodeEnd;
		if (bodyOnly) {
			nodeStart = location.getBodyBegin();
			nodeEnd = location.getBodyEnd();
		} else {
			nodeStart = location.getBegin();
			nodeEnd = location.getEnd();
		}
		TokenizedSource tokenizedSourceCode = rastRoot.getTokenizedSource().get(location.getFile());
		List<String> tokens = new ArrayList<>();
		for (int[] tokenPositon : tokenizedSourceCode.getTokens()) {
			int tokenStart = tokenPositon[TokenizedSource.START];
			int tokenEnd = tokenPositon[TokenizedSource.END];
			if (tokenStart < nodeStart) {
				continue;
			}
			if (tokenStart >= nodeEnd) {
				break;
			}
			tokens.add(sourceCode.substring(tokenStart, tokenEnd));
		}
		return tokens;
	}
	
	private Collection<String> getTokensToIgnoreInNodeBody(RastNode node) {
		return Arrays.asList("return");
	}
	
	public T sourceRep(RastNode n) {
		if (!srMap.containsKey(n)) {
			throw new RuntimeException("Source representation not computed");
		}
		return srMap.get(n);
	}
	
	public T bodySourceRep(RastNode n) {
		if (!srBodyMap.containsKey(n)) {
			throw new RuntimeException("Source representation not computed");
		}
		return srBodyMap.get(n);
	}
	
	public static List<String> getNodePath(RastNode node) {
		LinkedList<String> path = new LinkedList<>();
		computeNodePath(path, node);
		return path;
	}
	
	private static void computeNodePath(LinkedList<String> path, RastNode node) {
		String nodeName;
		if (node.getNamespace() != null) {
			nodeName = node.getNamespace() + node.getLocalName();
		} else {
			nodeName = node.getLocalName();
		}
		path.addFirst(nodeName);
		if (node.getParent().isPresent()) {
			computeNodePath(path, node.getParent().get());
		}
	}
}

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
import refdiff.core.cst.HasChildrenNodes;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.TokenizedSource;

public class CstRootHelper<T> {
	
	private final CstRoot cstRoot;
	private final Map<Integer, CstNode> idMap = new HashMap<>();
	private final Map<Integer, List<CstNodeRelationship>> edges = new HashMap<>();
	private final Map<Integer, List<CstNodeRelationship>> reverseEdges = new HashMap<>();
	private final Map<CstNode, Integer> depthMap = new HashMap<>();
	private final Map<String, String> fileMap = new HashMap<>();
	private final SourceRepresentationBuilder<T> srb;
	private final Map<CstNode, T> srMap = new HashMap<>();
	private final Map<CstNode, T> srBodyMap = new HashMap<>();
	private final Map<CstNode, T> srNameMap = new HashMap<>();
	private final Map<String, List<CstNode>> nameIndex = new HashMap<>();
	private final boolean isBefore;
	
	public CstRootHelper(CstRoot cstRoot, SourceFileSet sources, SourceRepresentationBuilder<T> srb, boolean isBefore) throws IOException {
		this.cstRoot = cstRoot;
		this.srb = srb;
		this.isBefore = isBefore;
		
		cstRoot.forEachNode((node, depth) -> {
			idMap.put(node.getId(), node);
			depthMap.put(node, depth);
			nameIndex.computeIfAbsent(node.getLocalName(), k -> new ArrayList<>()).add(node);
		});
		
		for (CstNodeRelationship relationship : cstRoot.getRelationships()) {
			edges.compute(relationship.getN1(), (key, oldValue) -> {
				if (oldValue == null) {
					ArrayList<CstNodeRelationship> list = new ArrayList<>();
					list.add(relationship);
					return list;
				} else {
					oldValue.add(relationship);
					return oldValue;
				}
			});
			reverseEdges.compute(relationship.getN2(), (key, oldValue) -> {
				if (oldValue == null) {
					ArrayList<CstNodeRelationship> list = new ArrayList<>();
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
	
	public int depth(CstNode node) {
		return depthMap.get(node);
	}
	
	public List<CstNode> findByLocalName(String localName) {
		return nameIndex.getOrDefault(localName, Collections.emptyList());
	}
	
	public Collection<CstNode> findRelationships(CstNodeRelationshipType type, CstNode node) {
		return this.findRelationships(edges, type, node, rel -> idMap.get(rel.getN2()));
	}
	
	public boolean hasRelationship(CstNodeRelationshipType type, Optional<CstNode> optN1, CstNode n2) {
		return hasRelationship(type, optN1, Optional.of(n2));
	}
	
	public boolean hasRelationship(CstNodeRelationshipType type, Optional<CstNode> optN1, Optional<CstNode> optN2) {
		if (optN1.isPresent() && optN2.isPresent()) {
			CstNode n1 = optN1.get();
			CstNode n2 = optN2.get();
			return edges.getOrDefault(n1.getId(), Collections.emptyList()).stream()
				.filter(rel -> rel.getType().equals(type) && idMap.get(rel.getN2()).equals(n2))
				.findFirst().isPresent();
		}
		return false;
	}
	
	public Collection<CstNode> findReverseRelationships(CstNodeRelationshipType type, CstNode node) {
		return this.findRelationships(reverseEdges, type, node, rel -> idMap.get(rel.getN1()));
	}
	
	private Collection<CstNode> findRelationships(Map<Integer, List<CstNodeRelationship>> map, CstNodeRelationshipType type, CstNode node, Function<CstNodeRelationship, CstNode> mappingFn) {
		return map.getOrDefault(node.getId(), Collections.emptyList()).stream()
			.filter(rel -> rel.getType().equals(type))
			.map(mappingFn).collect(Collectors.toList());
	}
	
	public Optional<CstNode> findByNamePath(String... namePath) {
		return findByNamePath(cstRoot, namePath);
	}
	
	public static Optional<CstNode> findByNamePath(CstRoot cstRoot, String... namePath) {
		return findByNamePathRecursive(cstRoot.getNodes(), 0, namePath);
	}
	
	public static Optional<CstNode> findByFullName(HasChildrenNodes parent, String name) {
		for (CstNode node : parent.getNodes()) {
			if (fullName(node).equals(name)) {
				return Optional.of(node);
			}
		}
		return Optional.empty();
	}
	
	private static Optional<CstNode> findByNamePathRecursive(List<CstNode> list, int depth, String[] namePath) {
		if (depth >= namePath.length) {
			throw new IllegalArgumentException(String.format("depth should be less than namePath.length"));
		}
		String name = namePath[depth];
		for (CstNode node : list) {
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
	
	public static boolean sameName(CstNode n1, CstNode n2) {
		return !n1.getSimpleName().isEmpty() && n1.getSimpleName().equals(n2.getSimpleName());
	}
	
	public static boolean sameSignature(CstNode n1, CstNode n2) {
		return signature(n1).equals(signature(n2));
	}
	
	public static boolean sameNamespace(CstNode n1, CstNode n2) {
		return Objects.equals(n1.getNamespace(), n2.getNamespace());
	}
	
	public static String signature(CstNode n) {
		return n.getLocalName();
	}
	
	public static String fullName(CstNode n) {
		return n.getNamespace() != null ? n.getNamespace() + n.getLocalName() : n.getLocalName();
	}
	
	public static boolean anonymous(CstNode n) {
		return n.getSimpleName().isEmpty();
	}
	
	public static boolean leaf(CstNode n) {
		return n.getNodes().isEmpty();
	}
	
	public static boolean sameType(CstNode n1, CstNode n2) {
		return n1.getType().equals(n2.getType());
	}
	
	public static boolean childOf(CstNode n1, CstNode n2) {
		return n1.getParent().isPresent() && n1.getParent().get().equals(n2);
	}
	
	public void printRelationships(PrintStream out) {
		out.print("Relationships:\n");
		for (CstNodeRelationship rel : cstRoot.getRelationships()) {
			CstNode n1 = idMap.get(rel.getN1());
			CstNode n2 = idMap.get(rel.getN2());
			out.print(String.format("%s %s %s\n", n1.getLocalName(), rel.getType(), n2.getLocalName()));
		}
	}
	
	public void computeSourceRepresentation(CstNode node) {
		if (!srMap.containsKey(node)) {
			String sourceCode = fileMap.get(node.getLocation().getFile());
			List<String> nodeTokens = retrieveTokens(sourceCode, node, false);
			srMap.put(node, srb.buildForNode(node, isBefore, nodeTokens));
			srNameMap.put(node, srb.buildForName(node, isBefore));
			
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
	
	private List<String> retrieveTokens(String sourceCode, CstNode node, boolean bodyOnly) {
		return retrieveTokens(cstRoot, sourceCode, node, bodyOnly);
	}
	
	public static List<String> retrieveTokens(CstRoot cstRoot, String sourceCode, CstNode node, boolean bodyOnly) {
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
		TokenizedSource tokenizedSourceCode = cstRoot.getTokenizedSource().get(location.getFile());
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
	
	private Collection<String> getTokensToIgnoreInNodeBody(CstNode node) {
		return Arrays.asList("return");
	}
	
	public T sourceRep(CstNode n) {
		if (!srMap.containsKey(n)) {
			throw new RuntimeException("Source representation not computed");
		}
		return srMap.get(n);
	}
	
	public T bodySourceRep(CstNode n) {
		if (!srBodyMap.containsKey(n)) {
			throw new RuntimeException("Source representation not computed");
		}
		return srBodyMap.get(n);
	}
	
	public T nameSourceRep(CstNode n) {
		if (!srNameMap.containsKey(n)) {
			throw new RuntimeException("Source representation not computed");
		}
		return srNameMap.get(n);
	}
	
	public static List<String> getNodePath(CstNode node) {
		LinkedList<String> path = new LinkedList<>();
		computeNodePath(path, node);
		return path;
	}
	
	private static void computeNodePath(LinkedList<String> path, CstNode node) {
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

	public boolean isNameUnique(CstNode n2) {
		return findByLocalName(n2.getLocalName()).size() == 1;
	}
}

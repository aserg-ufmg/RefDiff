package refdiff.parsers.universal.common;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.diff.CstRootHelper;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InheritanceTreeGenerator {
    /**
     * Builds the inheritance tree by analyzing the provided CST root and source code map.
     * It identifies inheritance relationships (e.g., subclass-superclass) among the specified node types.
     * @param root The CstRoot containing the nodes and to which relationships will be added.
     * @param sourceCodeMap A map where keys are file paths and values are the source code content of those files.
     * @param inheritableNodeTypes The CST node types that can participate in inheritance relationships.
     */
    public static void generate(CstRoot cstRoot, Map<String, String> sourceCodeMap, String... inheritableNodeTypes) {
        Map<String, List<CstNode>> inheritableNodeMap = new HashMap<>(); // class name -> CstNode list
        cstRoot.forEachNode((node, depth) -> {
            String nodeType = node.getType();
            if (List.of(inheritableNodeTypes).contains(nodeType)) {
                inheritableNodeMap.computeIfAbsent(node.getSimpleName(), k -> new ArrayList<>()).add(node); // Handle duplicated class names
            }
        });

        inheritableNodeMap.values().stream().flatMap(List::stream).forEach(node -> {
            String sourceCode = sourceCodeMap.get(node.getLocation().getFile());
            List<String> classHeaderTokens = extractClassHeaderTokens(node, cstRoot, sourceCode);
            for (String token : classHeaderTokens) {
                if (inheritableNodeMap.containsKey(token) && !token.equals(node.getSimpleName())) { // Avoid self-inheritance
                    for (CstNode superNode : inheritableNodeMap.get(token)) {
                        cstRoot.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.SUBTYPE, node.getId(), superNode.getId()));
                    }
                }
            }
        });
    }

    /**
     * Extracts the class header tokens from the given class node.
     * @param classNode The CST node representing the class.
     * @param cstRoot The CST root containing the nodes.
     * @param sourceCode The source code as a string.
     * @return A list of tokens representing the class header. e.g. ["public", "class", "A", "extends", "B", "implements", "C", ",", "D"]
     */
    private static List<String> extractClassHeaderTokens(CstNode classNode, CstRoot cstRoot, String sourceCode) {
        CstNode classHeader = new CstNode(0);
        Location location = new Location();
        location.setBegin(classNode.getLocation().getBegin());
        location.setEnd(classNode.getLocation().getBodyBegin());
        location.setFile(classNode.getLocation().getFile());
        classHeader.setLocation(location);
        List<String> tokens = CstRootHelper.retrieveTokens(cstRoot, sourceCode, classHeader, false);
        return tokens;
    }
}
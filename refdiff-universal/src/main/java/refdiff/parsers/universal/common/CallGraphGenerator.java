package refdiff.parsers.universal.common;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.diff.CstRootHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallGraphGenerator {

    private final Set<String> callableNodeTypes;
    private static final List<Integer> EMPTY_LIST = List.of();

    public CallGraphGenerator(String... callableNodeTypes) {
        this.callableNodeTypes = new HashSet<>(Arrays.asList(callableNodeTypes));
    }

    /**
     * Generates call relationships (e.g., method calls) between callable nodes within the provided CST root.
     * It first identifies all nodes of the specified {@code callableNodeTypes} (e.g., method declarations)
     * and then analyzes the tokens within each callable node to find references to other callable nodes.
     * <p>
     * Note: This implementation assumes that the CST root contains tokens for each node,
     * typically populated by {@link refdiff.core.diff.CstRootHelper#retrieveTokens(CstRoot, String, CstNode, boolean)}.
     * @param root The CstRoot containing the nodes and to which relationships will be added.
     * @param sourceCodeMap A map where keys are file paths and values are the source code content of those files.
     */
    public void generateCallGraph(CstRoot root, Map<String, String> sourceCodeMap) {
        Map<String, List<Integer>> calleeCandidatesMap = new HashMap<>(); // メソッドがオーバーロードされている場合、同名メソッドが複数存在するためValueはListにしている
        root.forEachNode((node, depth) -> {
            if (this.callableNodeTypes.contains(node.getType())) {
                calleeCandidatesMap.computeIfAbsent(node.getSimpleName(), k -> new ArrayList<>()).add(node.getId());
            }
        });
        root.forEachNode((node, depth) -> {
            if (this.callableNodeTypes.contains(node.getType())) {
                try {
                    addCallRelationship(node, root, sourceCodeMap, calleeCandidatesMap);
                } catch (Exception e) {
                    // Log the exception and continue processing other nodes
                    System.err.println("Error occured in call graph generation at " + node.getLocation().getFile() + ":" + node.getLocation().getBeginLine() + ", Message: " + e.getMessage());
                }
            }
        });
    }

    private void addCallRelationship(CstNode callerNode, CstRoot root, Map<String, String> sourceCodeMap, Map<String, List<Integer>> calleeCandidatesMap) {
        String path = callerNode.getLocation().getFile();
        String sourceCode = sourceCodeMap.get(path);
        List<String> tokens = CstRootHelper.retrieveTokens(root, sourceCode, callerNode, true);

        final int batchSize = 1000;
        int totalTokens = tokens.size();

        // Process tokens in batches to limit memory usage
        for (int i = 0; i < totalTokens; i += batchSize) {
            int end = Math.min(i + batchSize, totalTokens);
            List<String> tokenBatch = tokens.subList(i, end);

            for (String token : tokenBatch) {
                List<Integer> calleeIds = calleeCandidatesMap.getOrDefault(token, EMPTY_LIST);
                for (Integer calleeId : calleeIds) {
                    root.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.USE, callerNode.getId(), calleeId));
                }
            }
        }
    }
}
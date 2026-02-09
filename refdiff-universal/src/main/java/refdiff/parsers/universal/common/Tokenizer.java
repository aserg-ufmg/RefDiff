package refdiff.parsers.universal.common;

import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSTree;
import refdiff.core.cst.TokenPosition;
import refdiff.core.cst.TokenizedSource;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public static TokenizedSource tokenize(TSTree tree, TSLanguage tsLang, String path) {
        TSNode rootNode = tree.getRootNode();
        List<TokenPosition> tokens = new ArrayList<>();
        
        collectLeafNodes(rootNode, tokens);
        
        return new TokenizedSource(path, tokens);
    }

    private static void collectLeafNodes(TSNode node, List<TokenPosition> tokens) {
        if (node.getChildCount() == 0) {
            tokens.add(new TokenPosition(node.getStartByte(), node.getEndByte()));
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                collectLeafNodes(node.getChild(i), tokens);
            }
        }
    }
}
package refdiff.rast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RastRoot {
    public List<RastNode> nodes = new ArrayList<>();

    public void forEachNode(BiConsumer<RastNode, Integer> consumer) {
        forEachNodeInList(nodes, consumer, 0);
    }

    private void forEachNodeInList(List<RastNode> list, BiConsumer<RastNode, Integer> consumer, int depth) {
        for (RastNode node : list) {
            consumer.accept(node, depth);
            forEachNodeInList(node.nodes, consumer, depth + 1);
        }
    }
}

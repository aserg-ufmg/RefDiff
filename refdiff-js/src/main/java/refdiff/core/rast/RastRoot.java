package refdiff.core.rast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

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

    public Optional<RastNode> findByName(String nameBefore) {
        return findBy(nodes, n -> n.logicalName.equals(nameBefore));
    }

    private Optional<RastNode> findBy(List<RastNode> list, Predicate<RastNode> predicate) {
        for (RastNode node : list) {
            if (predicate.test(node)) {
                return Optional.of(node);
            }
            Optional<RastNode> n = findBy(node.nodes, predicate);
            if (n.isPresent()) {
                return n;
            }
        }
        return Optional.empty();
    }
}

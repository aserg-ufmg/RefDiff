package refdiff.parsers.universal.c;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestWithTags {

    private final LanguagePlugin parser = new CPlugin();

    @Test
    public void shouldParseFunctionsAndCompareWithTags() throws Exception {
        Path basePath = Paths.get("src/test/resources/c/syntax");
        SourceFolder sources = SourceFolder.from(basePath, Paths.get("src/func.c"));
        CstRoot root = parser.parse(sources);

        Path tagsPath = basePath.resolve("tags/tags-func.json");
        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader reader = new BufferedReader(new FileReader(tagsPath.toFile()))) {
            List<String> expectedNames = reader.lines()
                    .map(line -> {
                        try {
                            return mapper.readTree(line);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(json -> json.get("name").asText())
                    .collect(Collectors.toList());

            Set<String> cstSimpleNames = root.getNodes().stream()
                    .flatMap(node -> flatten(node).stream())
                    .map(CstNode::getSimpleName)
                    .collect(Collectors.toSet());

            for (String expectedName : expectedNames) {
                assertThat(cstSimpleNames, hasItem(expectedName));
            }
        }
    }

    private Set<CstNode> flatten(CstNode node) {
        return java.util.stream.Stream.concat(
                java.util.stream.Stream.of(node),
                node.getNodes().stream().flatMap(child -> flatten(child).stream()))
                .collect(Collectors.toSet());
    }
}

package validation;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.Optional;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;

public class Match {
  private Tag tag;
  private List<CstNode> cstNodeCandidates;
  private Map<Integer, Map<String, Boolean>> matchedFields; // cstNodeId -> (fieldName -> isMatched)
  private Boolean exactMatch;
  private String[] notes;

  Match(Tag tag) {
    this.tag = tag;
    this.cstNodeCandidates = new ArrayList<>();
    this.matchedFields = new HashMap<>();
    this.exactMatch = false;
    this.notes = new String[]{};
  }

  public Tag getTag() {
    return this.tag;
  }
  public List<CstNode> getCstNodeCandidates() {
    return this.cstNodeCandidates;
  }
  public Map<Integer, Map<String, Boolean>> getMatchedFields() {
    return this.matchedFields;
  }
  public Boolean getExactMatch() {
    return this.exactMatch;
  }
  public String[] getNotes() {
    return this.notes;
  }

  private void addNote(String note) {
    String[] newNotes = new String[this.notes.length + 1];
    System.arraycopy(this.notes, 0, newNotes, 0, this.notes.length);
    newNotes[this.notes.length] = note;
    this.notes = newNotes;
  }

  /**
   * Create a Match for the given tag using nodes from the CST root.
   * isTypeEqual: function that returns true when the tag kind and node type should be considered equal.
   */
  public static Match create(Tag tag, CstRoot root, BiPredicate<Tag, CstRoot> shouldIgnore, BiPredicate<Integer, Integer> isLineEqual, BiPredicate<Tag, CstNode> isNameEqual, BiPredicate<String, String> isTypeEqual) {
    Match match = new Match(tag);
    if (shouldIgnore.test(tag, root)) {
      match.addNote("To be ignored");
      return match;
    }

    root.forEachNode((node, d) -> {
      Map<String, Boolean> fieldMatches = new HashMap<>();
      fieldMatches.put("file", false);
      fieldMatches.put("line", false);
      fieldMatches.put("name", false);
      fieldMatches.put("type", false);

      String tagFile = Optional.ofNullable(tag.getPath()).orElse("");
      String nodeFile = Optional.ofNullable(node.getLocation()).map(l -> l.getFile()).orElse("");
      if (!tagFile.equals(nodeFile)) {
        return; // skip nodes in different files
      }
      fieldMatches.put("file", true);

      Integer tagLine = tag.getLine();
      Integer nodeLine = node.getLocation().getBeginLine();
      if (tagLine != null && nodeLine != null && tagLine.equals(nodeLine)) {
        fieldMatches.put("line", true);
      } else if (isLineEqual.test(tagLine, nodeLine)) {
        fieldMatches.put("line", true);
      }

      if (tag.getName() != null && tag.getName().equals(node.getSimpleName())) {
        fieldMatches.put("name", true);
      } else if (isNameEqual.test(tag, node)) {
        fieldMatches.put("name", true);
      } else {
        return;
      }

      if (isTypeEqual.test(tag.getKind(), node.getType())) {
        fieldMatches.put("type", true);
      }


      boolean allMatched = fieldMatches.values().stream().allMatch(b -> b.booleanValue());
      if (allMatched) {
        match.exactMatch = true;
        match.cstNodeCandidates.clear(); // store only exact match
        match.cstNodeCandidates.add(node);
        match.matchedFields.put(node.getId(), fieldMatches);
        return;
      }
      // if any field matched, record this node as a candidate
      boolean anyMatched = fieldMatches.values().stream().anyMatch(b -> b.booleanValue());
      if (anyMatched) {
        match.cstNodeCandidates.add(node);
        match.matchedFields.put(node.getId(), fieldMatches);
      }
    });

    if (match.cstNodeCandidates.isEmpty()) {
      match.addNote("No matching CST nodes found");
    } else if (!match.exactMatch) {
      match.addNote("Partial matches found");
    }

    return match;
  }
}

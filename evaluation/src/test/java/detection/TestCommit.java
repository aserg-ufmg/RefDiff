package detection;

import org.junit.Test;

import evaluation.CommitUrl;

import static org.junit.Assert.assertEquals;;

public class TestCommit {
  @Test
  public void testExtract() {
    String url = "https://github.com/owner/name/commit/abcdefghijklmnopqrstuvwxyz123456";
    String owner = CommitUrl.extractOwner(url);
    String repoName = CommitUrl.extractRepoName(url);
    String sha1 = CommitUrl.extractSha1(url);

    assertEquals("owner", owner);
    assertEquals("name", repoName);
    assertEquals("abcdefghijklmnopqrstuvwxyz123456", sha1);
  }

  @Test
  public void testGenerateCommitDiffUrl() {
    String owner = "owner";
    String repoName = "name";
    String sha1 = "abcdefghijklmnopqrstuvwxyz123456";
    String filePath = "refdiff-universal/src/main/java/refdiff/parsers/universal/java/JavaParser.java";

    String expectedUrl = "https://github.com/owner/name/commit/abcdefghijklmnopqrstuvwxyz123456#diff-323575ade1cabd9ea442023b67f1469cdd0661d8e6e92866b7b0dd6ebf50fd2a";
    String actualUrl = CommitUrl.generateCommitDiffUrl(owner, repoName, sha1, filePath);
    System.out.println(actualUrl);

    assertEquals(expectedUrl, actualUrl);
  }
}
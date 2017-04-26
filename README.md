# RefDiff

RefDiff is a tool to detect refactorings applied to Java code in git repositories.



## Usage

```java
RefDiff refDiff = new RefDiff();
GitService gitService = new GitServiceImpl(); 
try (Repository repository = gitService.openRepository("path/to/git/repo")) {
    List<SDRefactoring> refactorings = refDiff.detectAtCommit(repository, "commitSHA");
    for (SDRefactoring refactoring : refactorings) {
        System.out.println(refactoring.toString());
    }
}
```

# RefDiff

RefDiff is a tool to detect refactorings applied to Java code in git repositories.



## Usage

The easiest way to get RefDiff is from the Mavel Central. Declare it as a dependency in your build system (Maven, Gradle, etc). For example:

```
<dependency>
  <groupId>com.github.aserg-ufmg</groupId>
  <artifactId>refdiff-core</artifactId>
  <version>0.1.0</version>
</dependency>
```

Then, you can detect refactoring in a certain repository/commit using the following code:

```java
RefDiff refDiff = new RefDiff();
GitService gitService = new GitServiceImpl(); 
try (Repository repository = gitService.cloneIfNotExists("C:/tmp/clojure", "https://github.com/refdiff-data/clojure.git")) {
    List<SDRefactoring> refactorings = refDiff.detectAtCommit(repository, "17217a1");
    for (SDRefactoring refactoring : refactorings) {
        System.out.println(refactoring.toString());
    }
}
```

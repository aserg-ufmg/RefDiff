# RefDiff

RefDiff is a tool to detect refactorings applied to Java code in git repositories.

The following types of refactoring are supported:

* Rename Type
* Move Type
* Move and Rename Type 
* Extract Supertype
* Rename Method 
* Change Method Signature 
* Pull Up Method 
* Push Down Method 
* Move Method 
* Extract Method 
* Inline Method 
* Pull Up Field 
* Push Down Field 
* Move Field 


## Usage

The easiest way to get RefDiff is from the Mavel Central. Declare it as a dependency in your build system (Maven, Gradle, etc). For example:

```
<dependency>
  <groupId>com.github.aserg-ufmg</groupId>
  <artifactId>refdiff-core</artifactId>
  <version>0.1.1</version>
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

See more details in [this tutorial](doc/Tutorial1.md)


## Building from the source code

RefDiff uses Gradle as the build system. If you would like to work with the source code of RefDiff, clone the repository and run the following command inside the folder:

    ./gradlew eclipse

This will create Eclipse metadata for the project. Now, you can import the project refdiff-core into Eclipse IDE and work with the source code.


## Publications

The algorithm RefDiff uses is described in details in the following paper:

* Danilo Silva, Marco Tulio Valente. [RefDiff: Detecting Refactorings in Version Histories](http://www.dcc.ufmg.br/~mtov/pub/2017-msr.pdf). In 14th International Conference on Mining Software Repositories (MSR), 2017.

The data used in the evaluation is available in [this spreadsheet](refdiff-evaluation/data/evaluation-oracle.xlsx).

Learn more about our research group at http://aserg.labsoft.dcc.ufmg.br/

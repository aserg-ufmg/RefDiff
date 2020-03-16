# RefDiff

RefDiff is a tool to mine refactorings in the commit history of git repositories.
Currently, three programming languages are supported: Java, JavaScript, and C.

RefDiff finds relationships between code elements of two given revisions of the
project. Relationships indicate that both elements are the same, or that a refactoring
operation involving them was applied. The following relationship types are supported:

* Same
* Convert Type
* Change Signature of Method/Function
* Pull Up Method
* Push Down Method
* Rename
* Move
* Move and Rename
* Extract Supertype (e.g., Class/Interface)
* Extract Method/Function
* Inline Method/Function


## Getting started

Before building the project, make sure you have git and a Java Development Kit (JDK) version 8 installed in your system. Also, set the JAVA_HOME environment variable to point to the installation directory of the desired JDK.

```
git clone https://github.com/aserg-ufmg/RefDiff.git
```

Use gradle to create the Eclipse IDE project metadata. For example, in Windows systems:

```
cd RefDiff
gradlew eclipse
```

Note that in Linux or Mac you should run `./gradlew eclipse` to run the gradle wrapper.

Import all projects within `RefDiff` folder to Eclipse. Then, see the examples 
in `RefDiffExample.java` from `refdiff-example`.

You can detect refactorings in a certain repository/commit using the following code:

```java
private static void runExamples() throws Exception {
	// This is a temp folder to clone or checkout git repositories.
	File tempFolder = new File("temp");

	// Creates a RefDiff instance configured with the JavaScript plugin.
	JsPlugin jsPlugin = new JsPlugin();
	RefDiff refDiffJs = new RefDiff(jsPlugin);

	// Clone the angular.js GitHub repo.
	File angularJsRepo = refDiffJs.cloneGitRepository(
		new File(tempFolder, "angular.js"),
		"https://github.com/refdiff-study/angular.js.git");

	// You can compute the relationships between the code elements in a commit with
	// its previous commit. The result of this operation is a CstDiff object, which
	// contains all relationships between CstNodes. Relationships whose type is different
	// from RelationshipType.SAME are refactorings.
	CstDiff diffForCommit = refDiffJs.computeDiffForCommit(angularJsRepo, "2636105");
	printRefactorings("Refactorings found in angular.js 2636105", diffForCommit);
}
private static void printRefactorings(String headLine, CstDiff diff) {
	System.out.println(headLine);
	for (Relationship rel : diff.getRefactoringRelationships()) {
		System.out.println(rel.getStandardDescription());
	}
}
```

You can also mine refactorings from the commit history:

```java
// You can also mine refactoring from the commit history. In this example we navigate
// the commit graph backwards up to 5 commits. Merge commits are skipped.
refDiffJs.computeDiffForCommitHistory(angularJsRepo, 5, (commit, diff) -> {
	printRefactorings("Refactorings found in angular.js " + commit.getId().name(), diff);
});
```

You can use different language plugins to mine refactorings in other programming languages:

```java
// In this example, we use the plugin for C.
CPlugin cPlugin = new CPlugin();
RefDiff refDiffC = new RefDiff(cPlugin);

File gitRepo = refDiffC.cloneGitRepository(
	new File(tempFolder, "git"),
	"https://github.com/refdiff-study/git.git");

printRefactorings(
	"Refactorings found in git ba97aea",
	refDiffC.computeDiffForCommit(gitRepo, "ba97aea1659e249a3a58ecc5f583ee2056a90ad8"));


// Now, we use the plugin for Java.
JavaPlugin javaPlugin = new JavaPlugin(tempFolder);
RefDiff refDiffJava = new RefDiff(javaPlugin);

File eclipseThemesRepo = refDiffC.cloneGitRepository(
	new File(tempFolder, "eclipse-themes"),
	"https://github.com/icse18-refactorings/eclipse-themes.git");

printRefactorings(
	"Refactorings found in eclipse-themes 72f61ec",
	refDiffJava.computeDiffForCommit(eclipseThemesRepo, "72f61ec"));
```

## Extending RefDiff to support other programming languages

You can implement the `LanguagePlugin` interface to support other programming languages.
Soon, we will provide a detailed tutorial on how to do this.

## Evaluation

Our evaluation with an oracle containing 3,248 real refactoring instances from public Java repositories shows that RefDiff’s precision is 96% and recall is 80%:

![Java evaluation results](https://github.com/aserg-ufmg/RefDiff/blob/master/java-eval.png)

The data used in the evaluation is available in the following links:
- [Java evaluation data](refdiff-evaluation/data/java-evaluation/evaluation-data-public.xlsx)
- [C evaluation data](https://docs.google.com/spreadsheets/d/1uAMLA47u8k1C5p1vJ8hbovHpxRrlVRinpm8LmDKpVfU/edit?usp=sharing)
- [JavaScript evaluation data](https://docs.google.com/spreadsheets/d/1gTcga3gG0oWvqEm0iUv1_6y5Epc9T6a-KSONpQ8z9ss/edit?usp=sharing)


## Publications

The algorithm RefDiff uses is described in details in the following paper:

* Danilo Silva, Marco Tulio Valente. [RefDiff: Detecting Refactorings in Version Histories](http://www.dcc.ufmg.br/~mtov/pub/2017-msr.pdf). In 14th International Conference on Mining Software Repositories (MSR), 2017.

Learn more about our research group at http://aserg.labsoft.dcc.ufmg.br/

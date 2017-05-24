# Tutorial

In this tutorial, we will walk through each step to use RefDiff to mine refactorings from a git repository. Before starting, make sure you have the following software installed on your system:

* Java (version 1.8 or higher)
* Eclipse IDE (Neon release or higher)


## 1. Create a new project

In Eclipse, click in

    File > New > Project ...

Then, select *Maven Project* in the *New project wizard*.
Proceed with the default options by clicking *Next*, until you are asked to enter a group and artifact Id. Fill the fields with the desired names, for example:

**Group Id**: refdiff 
**Artifact Id**: tutorial1

and then click *Finish*.

Now, edit the `pom.xml` file in the root of the project and add the following entry to the `dependencies` tag:

```
<dependency>
  <groupId>com.github.aserg-ufmg</groupId>
  <artifactId>refdiff-core</artifactId>
  <version>0.1.1</version>
</dependency>
```

When you save the file, all the necessary dependencies to run refdiff will be downloaded from the maven central and configured in the classpath automatically.

Tha last step to configure the project is to define the compiler compliance level. Right click on the project you just created and click in *Properties*. Then, open the *Java Compiler* tab and set the value of the *Compiler compliance level* to 1.8.
Now, you are ready to use RefDiff in your project.


## 2. Detect refactorings in a specific commit

Supose we want the find refactorings in a specific commit of interest. To take a real example, lets consider the following commit from the Jersey repository at GitHub:

https://github.com/jersey/jersey/commit/d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606


Create a class `Example1` in your project with the content below:

```java
package refdiff.tutorial;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.RefDiff;
import refdiff.core.api.GitService;
import refdiff.core.rm2.model.refactoring.SDRefactoring;
import refdiff.core.util.GitServiceImpl;

public class Example1 {
    public static void main(String[] args) throws Exception {
        RefDiff refDiff = new RefDiff();
        GitService gitService = new GitServiceImpl();
        try (Repository repository = gitService.cloneIfNotExists("C:/tmp/jersey", "https://github.com/jersey/jersey.git")) {
            List<SDRefactoring> refactorings = refDiff.detectAtCommit(repository, "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606");
            for (SDRefactoring r : refactorings) {
                System.out.printf("%s\t%s\t%s\n", r.getRefactoringType().getDisplayName(), r.getEntityBefore().key(), r.getEntityAfter().key());
            }
        }
    }
}
```

# Tutorial

In this tutorial, we will walk through each step to use RefDiff to mine refactorings from a git repository. Before starting, make sure you have the following software installed on your system:

* Java (version 1.8 or higher)
* Eclipse IDE (Neon release or higher)


## 1. Create a new project

In Eclipse, click in **File > New > Project**.
Then, select *Maven Project* in the *New project wizard*.
Proceed with the default options by clicking **Next**, until you are asked to enter a group and artifact Id. Fill the fields with the desired names (for example, *Group Id*: refdiff and *Artifact Id*: tutorial1) and then click **Finish**.

Now, edit the `pom.xml` file in the root of the project and add the following entry to the `dependencies` tag:

```
<dependency>
  <groupId>com.github.aserg-ufmg</groupId>
  <artifactId>refdiff-core</artifactId>
  <version>0.1.1</version>
</dependency>
```

When you save the file, all the necessary dependencies to run RefDiff will be downloaded from the Maven central repository and configured in the classpath automatically.

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

In this code snippet, we first instantiate two objects: `RefDiff` and `GitServiceImpl`. The first one is the main entry point of RefDiff's API. While the second is a utility class to manipulate git repositories.
Before detecting refactorings, we most create a local clone of the repository, which is done by the statement below:

```java
Repository repository = gitService.cloneIfNotExists("C:/tmp/jersey", "https://github.com/jersey/jersey.git")
```

Note that the first parameter to the method `cloneIfNotExists` is the path of a folder to store the local clone. If the folder already exists, it is assumed to be a git repository and the cloning process is skiped. The method returns a `Repository` object from the jgit API. Such object is instantiated in a `try` block to be automatically closed using the [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) feature of Java 7.


Later, we call `refDiff.detectAtCommit`, passing the repository and the ID of the commit as arguments, to get a list of the refactorings detected in such commit.

```java
List<SDRefactoring> refactorings = refDiff.detectAtCommit(repository, "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606");
```

This is the most straightforward way to use RefDiff's API, in which the source code is compared before and after the commit to find refactorings. Specifically, RefDiff compares the specified commit with its parent commit, assuming the commit has only one parent. Commits with more than one parent (merges) and with no parent (first commit) will produce an empty list of refactorings.

The last part o the code iterates on the list of refactorings and print their data in the console, yielding the following output:

```
Move Class  core-client/src/main/java/org.glassfish.jersey.client.HttpUrlConnector  core-client/src/main/java/org.glassfish.jersey.client.internal.HttpUrlConnector
Extract Method  core-client/src/main/java/org.glassfish.jersey.client.HttpUrlConnectorProvider#getConnector(Client, Configuration)  core-client/src/main/java/org.glassfish.jersey.client.HttpUrlConnectorProvider#createHttpUrlConnector(Client, ConnectionFactory, int, boolean, boolean)
Extract Method  core-client/src/main/java/org.glassfish.jersey.client.HttpUrlConnector#_apply(ClientRequest)    core-client/src/main/java/org.glassfish.jersey.client.internal.HttpUrlConnector#secureConnection(Client, HttpURLConnection)
```

In this example, RefDiff finds that:

* the class `HttpUrlConnector` is moved from `org.glassfish.jersey.client` to `org.glassfish.jersey.client.internal`;
* the method `createHttpUrlConnector(Client, ConnectionFactory, int, boolean, boolean)` is extracted from `HttpUrlConnectorProvider#getConnector(Client, Configuration)`; and
* the method `secureConnection(Client, HttpURLConnection)` is extracted from `HttpUrlConnector#_apply(ClientRequest)`.

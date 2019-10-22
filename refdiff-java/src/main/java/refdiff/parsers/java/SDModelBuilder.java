package refdiff.parsers.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.internal.compiler.util.Util;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.TokenizedSource;

public class SDModelBuilder {
	
	private static final String systemFileSeparator = Matcher.quoteReplacement(File.separator);
	
	private Map<CstNode, List<String>> postProcessReferences;
	private Map<CstNode, List<String>> postProcessSupertypes;
	
	private void postProcessReferences(SDModel model, Map<CstNode, List<String>> referencesMap) {
		for (Map.Entry<CstNode, List<String>> entry : referencesMap.entrySet()) {
			final CstNode entity = entry.getKey();
			List<String> references = entry.getValue();
			for (String referencedKey : references) {
				Optional<CstNode> referenced = model.findByKey(referencedKey);
				if (referenced.isPresent()) {
					model.addReference(entity, referenced.get());
				}
			}
		}
	}
	
	private void postProcessSupertypes(SDModel model) {
		for (Map.Entry<CstNode, List<String>> entry : postProcessSupertypes.entrySet()) {
			final CstNode type = entry.getKey();
			List<String> supertypes = entry.getValue();
			for (String supertypeKey : supertypes) {
				Optional<CstNode> supertype = model.findByKey(supertypeKey);
				if (supertype.isPresent()) {
					model.addSubtype(supertype.get(), type);
				}
			}
		}
	}
	
	public void analyze(File rootFolder, List<String> javaFiles, final SDModel model, JavaSourceTokenizer tokenizer) {
		postProcessReferences = new HashMap<CstNode, List<String>>();
		postProcessSupertypes = new HashMap<CstNode, List<String>>();
		final String projectRoot = rootFolder.getPath();
		final String[] emptyArray = new String[0];
		
		String encoding = StandardCharsets.UTF_8.name();
		String[] filesArray = new String[javaFiles.size()];
		String[] encodings = new String[javaFiles.size()];
		for (int i = 0; i < filesArray.length; i++) {
			filesArray[i] = rootFolder + File.separator + javaFiles.get(i).replaceAll("/", systemFileSeparator);
			encodings[i] = encoding;
		}
		final String[] sourceFolders = this.inferSourceFolders(filesArray);
		final ASTParser parser = buildAstParser(sourceFolders);
		
		FileASTRequestor fileASTRequestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				String relativePath = sourceFilePath.substring(projectRoot.length() + 1).replaceAll(systemFileSeparator, "/");
				// IProblem[] problems = ast.getProblems();
				// if (problems.length > 0) {
				// System.out.println("problems");
				// }
				//
				try {
					//char[] charArray = Util.getFileCharContent(new File(sourceFilePath), StandardCharsets.UTF_8.name());
					char[] charArray = Util.getFileCharContent(new File(sourceFilePath), encoding);
					processCompilationUnit(relativePath, charArray, ast, model);
					TokenizedSource tokenizedSource = new TokenizedSource(relativePath, tokenizer.tokenize(charArray));
					model.getRoot().addTokenizedFile(tokenizedSource);
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		parser.createASTs((String[]) filesArray, encodings, emptyArray, fileASTRequestor, null);
		
		postProcessReferences(model, postProcessReferences);
		postProcessReferences = null;
		postProcessSupertypes(model);
		postProcessSupertypes = null;
	}
	
	private static ASTParser buildAstParser(String[] sourceFolders) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setEnvironment(new String[0], sourceFolders, null, true);
		// parser.setEnvironment(new String[0], new String[]{"tmp\\refactoring-toy-example\\src"}, null, false);
		return parser;
	}
	
	private void processCompilationUnit(String sourceFilePath, char[] fileContent, CompilationUnit compilationUnit, SDModel model) {
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		String packageName = "";
		if (packageDeclaration != null) {
			packageName = packageDeclaration.getName().getFullyQualifiedName();
		}
		int flags = compilationUnit.getFlags();
		if ((flags & ASTNode.RECOVERED) > 1 || (flags & ASTNode.MALFORMED) > 1) {
			// syntax error
		} else {			
			BindingsRecoveryAstVisitor visitor = new BindingsRecoveryAstVisitor(model, compilationUnit, sourceFilePath, fileContent, packageName, postProcessReferences, postProcessSupertypes);
			compilationUnit.accept(visitor);
		}
	}
	
	private String[] inferSourceFolders(String[] filesArray) {
		Set<String> sourceFolders = new TreeSet<String>();
		nextFile:
		for (String file : filesArray) {
			for (String sourceFolder : sourceFolders) {
				if (file.startsWith(sourceFolder)) {
					continue nextFile;
				}
			}
			String otherSourceFolder = extractSourceFolderFromPath(file);
			if (otherSourceFolder != null) {
				sourceFolders.add(otherSourceFolder);
				// System.out.print("source folder: ");
				// System.out.println(otherSourceFolder);
			}
		}
		return sourceFolders.toArray(new String[sourceFolders.size()]);
	}
	
	private String extractSourceFolderFromPath(String sourceFilePath) {
		try (BufferedReader scanner = new BufferedReader(new FileReader(sourceFilePath))) {
			String lineFromFile;
			while ((lineFromFile = scanner.readLine()) != null) {
				if (lineFromFile.startsWith("package ")) {
					// a match!
					// System.out.print("package declaration: ");
					String packageName = lineFromFile.substring(8, lineFromFile.indexOf(';'));
					// System.out.println(packageName);
					
					String packagePath = packageName.replace('.', File.separator.charAt(0));
					int indexOfPackagePath = sourceFilePath.lastIndexOf(packagePath + File.separator);
					if (indexOfPackagePath >= 0) {
						return sourceFilePath.substring(0, indexOfPackagePath - 1);
					}
					return null;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	/////////////////
	
}

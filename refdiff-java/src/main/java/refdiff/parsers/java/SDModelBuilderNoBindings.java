package refdiff.parsers.java;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.TokenizedSource;

public class SDModelBuilderNoBindings {
	
	private BindingResolver bindingResolver;
	
	private void postProcessReferences(SDModel model) {
		for (Map.Entry<RastNode, List<String>> entry : bindingResolver.getReferencesMap().entrySet()) {
			final RastNode entity = entry.getKey();
			List<String> references = entry.getValue();
			for (String referencedKey : references) {
				Optional<RastNode> referenced = model.findByKey(referencedKey);
				if (referenced.isPresent()) {
					model.addReference(entity, referenced.get());
				}
			}
		}
	}
	
	private void postProcessSupertypes(SDModel model) {
		for (Map.Entry<RastNode, List<String>> entry : bindingResolver.getSupertypesMap().entrySet()) {
			final RastNode type = entry.getKey();
			List<String> supertypes = entry.getValue();
			for (String supertypeKey : supertypes) {
				Optional<RastNode> supertype = model.findByKey(supertypeKey);
				if (supertype.isPresent()) {
					model.addSubtype(supertype.get(), type);
				}
			}
		}
	}
	
	public void analyze(SourceFileSet sources, final SDModel model, JavaSourceTokenizer tokenizer) {
		bindingResolver = new BindingResolver();
		
		final ASTParser parser = buildAstParser();
		
		try {
			for (SourceFile sourceFile : sources.getSourceFiles()) {
				parser.setUnitName(sourceFile.getPath());
				char[] charArray = sources.readContent(sourceFile).toCharArray();
				parser.setSource(charArray);
				CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
				processCompilationUnit(sourceFile.getPath(), charArray, astRoot, model);
				
				TokenizedSource tokenizedSource = new TokenizedSource(sourceFile.getPath(), tokenizer.tokenize(charArray));
				model.getRoot().addTokenizedFile(tokenizedSource);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		postProcessReferences(model);
		postProcessSupertypes(model);
		bindingResolver = null;
	}
	
	private static ASTParser buildAstParser() {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(false);
		return parser;
	}
	
	protected void processCompilationUnit(String sourceFilePath, char[] fileContent, CompilationUnit compilationUnit, SDModel model) {
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		String packageName = "";
		if (packageDeclaration != null) {
			packageName = packageDeclaration.getName().getFullyQualifiedName();
		}
		AstVisitorNoBindings visitor = new AstVisitorNoBindings(model, compilationUnit, sourceFilePath, fileContent, packageName, bindingResolver);
		compilationUnit.accept(visitor);
	}
	
}

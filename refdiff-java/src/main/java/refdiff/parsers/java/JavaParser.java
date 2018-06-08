package refdiff.parsers.java;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.Stereotype;
import refdiff.parsers.RastParser;

public class JavaParser implements RastParser {

	@Override
	public RastRoot parse(SourceFileSet sources) throws Exception {
		List<String> javaFiles = new ArrayList<>();
		Optional<Path> optBasePath = sources.getBasePath();
		if (!optBasePath.isPresent()) {
			throw new RuntimeException("The Java parser requires a SourceFileSet that support a basePath");
		}
		for (SourceFile sourceFile : sources.getSourceFiles()) {
			javaFiles.add(sourceFile.getPath());
		}
		
		File rootFolder = optBasePath.get().toFile();
		
		SDModel sdModel = new SDModel();
		SDModelBuilder mb = new SDModelBuilder();
		mb.analyze(rootFolder, javaFiles, sdModel);
		
		return sdModel.getRoot();
	}

	public static String getKey(RastNode node) {
		String parentName;
		if (node.getParent().isPresent()) {
			parentName = getKey(node.getParent().get()) + ".";
		} else if (node.getNamespace() != null) {
			parentName = node.getNamespace();
		} else {
			parentName = "";
		}
		if (node.getType().equals(NodeTypes.METHOD_DECLARATION)) {
			String key = parentName + AstUtils.normalizeMethodSignature(node.getLocalName());
			
			// convert org.MyClass.new() to org.MyClass.MyClass()
			if (node.hasStereotype(Stereotype.TYPE_CONSTRUCTOR)) {
				key = key.replace(".new(", "." + node.getParent().get().getSimpleName() + "(");
			}
			return key;
		}
		
		return parentName + node.getLocalName();
	}
	
	@Override
	public List<String> getAllowedFileExtensions() {
		return Arrays.asList(".java");
	}
}

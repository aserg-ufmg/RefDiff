package refdiff.parsers.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;
import refdiff.parsers.RastParser;

public class JavaParser implements RastParser {

	@Override
	public RastRoot parse(List<SourceFile> sourceFiles) throws Exception {
		List<String> javaFiles = new ArrayList<>();
		for (SourceFile sourceFile : sourceFiles) {
			sourceFile.getPath();
			if (sourceFile instanceof FileSystemSourceFile) {
				javaFiles.add(sourceFile.getPath());
			} else {
				throw new RuntimeException("The Java parser only works with FileSystemSourceFile");
			}
		}
		File rootFolder = ((FileSystemSourceFile) sourceFiles.stream().findFirst().get()).getBasePath().toFile();
		
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
			return parentName + AstUtils.normalizeMethodSignature(node.getLocalName());
		}
		return parentName + node.getLocalName();
	}
}

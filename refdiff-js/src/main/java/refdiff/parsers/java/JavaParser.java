package refdiff.parsers.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
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

}

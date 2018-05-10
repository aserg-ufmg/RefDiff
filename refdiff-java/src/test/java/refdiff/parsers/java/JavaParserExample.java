package refdiff.parsers.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;

public class JavaParserExample {
	
	public static void main(String[] args) throws Exception {
		JavaParser parser = new JavaParser();
		
		Path basePath = Paths.get("test-data/parser/java");
		List<SourceFile> sourceFiles = new ArrayList<>();
		sourceFiles.add(new FileSystemSourceFile(basePath, Paths.get("p2/Foo.java")));
		sourceFiles.add(new FileSystemSourceFile(basePath, Paths.get("p1/Bar.java")));
		
		RastRoot rastRoot = parser.parse(sourceFiles);
		
		ObjectMapper jacksonObjectMapper = new ObjectMapper();
		ObjectWriter jsonWriter = jacksonObjectMapper.writerWithDefaultPrettyPrinter();
		
		jsonWriter.writeValue(System.out, rastRoot);
	}
	
}

package refdiff.parsers.java;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import refdiff.core.io.SourceFolder;
import refdiff.core.cst.CstRoot;

public class JavaParserExample {
	
	public static void main(String[] args) throws Exception {
		JavaPlugin parser = new JavaPlugin();
		
		Path basePath = Paths.get("test-data/parser/java");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("p2/Foo.java"), Paths.get("p1/Bar.java"));
		
		CstRoot cstRoot = parser.parse(sources);
		
		ObjectMapper jacksonObjectMapper = new ObjectMapper();
		ObjectWriter jsonWriter = jacksonObjectMapper.writerWithDefaultPrettyPrinter();
		
		jsonWriter.writeValue(System.out, cstRoot);
	}
	
}

package refdiff.parsers.c;

import java.util.ArrayList;
import java.util.List;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class CParser implements RastParser, SourceTokenizer {
	
	@Override
	public RastRoot parse(List<SourceFile> filesOfInterest) throws Exception {
		RastRoot root = new RastRoot();
		// TODO
		return root;
	}
	
	@Override
	public List<String> tokenize(String source) {
		List<String> tokens = new ArrayList<>();
		// TODO
		return tokens;
	}

}

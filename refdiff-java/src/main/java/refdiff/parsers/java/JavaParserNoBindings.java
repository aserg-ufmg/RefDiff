package refdiff.parsers.java;

import java.util.Arrays;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.Stereotype;
import refdiff.parsers.RastParser;

public class JavaParserNoBindings implements RastParser {

	private final JavaSourceTokenizer tokenizer = new JavaSourceTokenizer();
	
	@Override
	public RastRoot parse(SourceFileSet sources) throws Exception {
		SDModel sdModel = new SDModel();
		SDModelBuilderNoBindings mb = new SDModelBuilderNoBindings();
		mb.analyze(sources, sdModel, tokenizer);
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
	public FilePathFilter getAllowedFilesFilter() {
		return new FilePathFilter(Arrays.asList(".java"));
	}
}

package refdiff.parsers.c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;

import refdiff.core.cst.CstRoot;
import refdiff.core.cst.TokenPosition;
import refdiff.core.cst.TokenizedSource;
import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;
import refdiff.parsers.LanguagePlugin;

public class CPlugin implements LanguagePlugin {
	
	@Override
	public CstRoot parse(SourceFileSet sources) throws Exception {
		CstRoot root = new CstRoot();
		
		AtomicInteger id = new AtomicInteger(1);
		
		for (SourceFile sourceFile : sources.getSourceFiles()) {
			String sourceCode = sources.readContent(sourceFile);
			FileContent fileContent = FileContent.create("temp.source", sourceCode.toCharArray());
			IASTTranslationUnit translationUnit = parseAST(fileContent);
			
//			System.out.println(sourceFile.getPath());
//			ASTPrinter.print(translationUnit);

			ASTVisitor cCstVisitor = new CCstVisitor(root, sourceFile.getPath(), sourceCode, id);
			translationUnit.accept(cCstVisitor);
			
			root.addTokenizedFile(new TokenizedSource(sourceFile.getPath(), tokenize(sourceCode)));
		}
		
//		ObjectMapper jacksonObjectMapper = new ObjectMapper();
//		String jsonInString = jacksonObjectMapper.writeValueAsString(root);
//		System.out.println(jsonInString);
		
		return root;
	}
	
	private static IASTTranslationUnit parseAST(FileContent fileContent) throws CoreException {
		GCCLanguage gccLanguage = GCCLanguage.getDefault();
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = 0;
		IParserLogService log = new DefaultLogService();
		return gccLanguage.getASTTranslationUnit(fileContent, si, ifcp, idx, options, log);
	}
	
	public List<TokenPosition> tokenize(String source) {
		List<TokenPosition> tokens = new ArrayList<>();
		
		ASTVisitor tokenVisitor = new TokenVisitor(tokens);
		FileContent fileContent = FileContent.create("temp.source", source.toCharArray());
		IASTTranslationUnit translationUnit = null;
		try {
			translationUnit = parseAST(fileContent);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		translationUnit.accept(tokenVisitor);
		
		return tokens;
	}

	@Override
	public FilePathFilter getAllowedFilesFilter() {
		return new FilePathFilter(Arrays.asList(".c", ".h"));
	}

}

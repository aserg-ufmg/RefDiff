package refdiff.parsers.c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class CParser implements RastParser, SourceTokenizer {
	
	@Override
	public RastRoot parse(List<SourceFile> filesOfInterest) throws Exception {
		RastRoot root = new RastRoot();
		
		for (SourceFile sourceFile : filesOfInterest) {
			FileContent fileContent = FileContent.create("temp.source", sourceFile.getContent().toCharArray());
			ASTVisitor cRastVisitor = new CRastVisitor(root, sourceFile.getPath());
			IASTTranslationUnit translationUnit = parseAST(fileContent);	
			
//			ASTPrinter.print(translationUnit);
			
			translationUnit.accept(cRastVisitor);
		}
		
		ObjectMapper jacksonObjectMapper = new ObjectMapper();
		
		String jsonInString = jacksonObjectMapper.writeValueAsString(root);
		System.out.println(jsonInString);
		
		return root;
	}
	
	private static IASTTranslationUnit parseAST(FileContent fileContent) throws CoreException {
		GCCLanguage gccLanguage = GCCLanguage.getDefault();
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		return gccLanguage.getASTTranslationUnit(fileContent, si, ifcp, idx, options, log);
	}
	
	@Override
	public List<String> tokenize(String source) {
		List<String> tokens = new ArrayList<>();
		
		ASTVisitor tokenVisitor = new TokenVisitor(tokens);
		FileContent fileContent = FileContent.create("temp.source", source.toCharArray());
		IASTTranslationUnit translationUnit = null;
		try {
			translationUnit = parseAST(fileContent);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		translationUnit.accept(tokenVisitor);
		
		return tokens;
	}

}

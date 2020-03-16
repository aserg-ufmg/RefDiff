package refdiff.parsers;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.CstRoot;

/**
 * A LanguagePlugin is responsible for generating the Code Structure Tree (CST) for
 * a particular programming language.
 */
public interface LanguagePlugin {

	/**
	 * Analyze the given source files and build a Code Structure Tree (CST), represented by 
	 * the {@code CstRoot} object.
	 * 
	 * @param sources A set of source files that should be analyzed. Note that RefDiff does not pass all
	 * files of the project for analysis. Usually, only the files that changed between the revisions under 
	 * examination are passed to this method. Additionally, RefDiff filters files using the {@code getAllowedFilesFilter}
	 * method.
	 * @return The CST.
	 */
	CstRoot parse(SourceFileSet sources) throws Exception;
	
	/**
	 * @return A {@code FilePathFilter} object, which contains a list of file extensions supported by this
	 * LanguagePlugin. Additionally, a list of excluded file extensions may be provided. For example, the
	 * JavaScript language plugin analyzes .js files, but ignores .min.js files.
	 */
	FilePathFilter getAllowedFilesFilter();
	
}

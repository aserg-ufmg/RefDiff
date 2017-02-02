package tyRuBa.applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tests.PerformanceTest;

/**
 * @noToString This tag indicates the developer explicitly checked this class and
 *             wants to exclude it from the "implementToString" design rule.
 */
public class CommandLine {

	FrontEnd frontend = null;
	boolean loadInitFile = true; // the default
	File dbDir = null;
	int cachesize = FrontEnd.defaultPagerCacheSize;
	private boolean backgroundPageCleaning = false;

	void ensureFrontEnd()
		throws java.io.IOException, tyRuBa.parser.ParseException, TypeModeError {
		if (frontend == null) {
			if (dbDir==null) 
				frontend = new FrontEnd(loadInitFile,new File("./fdb/"),true,null,true,backgroundPageCleaning);
			else	
				frontend = new FrontEnd(loadInitFile,dbDir,true,null,false,backgroundPageCleaning);
		}
		frontend.setCacheSize(this.cachesize);
	}

	public static void main(String args[]) {
		new CommandLine().realmain(args);
	}

	/**
	 * @codegroup metadata
	 */
	public void realmain(String args[]) {
		if (args.length == 0) {
			System.err.println("ERROR: no commandline arguments where given");
			return;
		}
		try {
			int start = 0;
			for (int i = start; i < args.length; i++) {
				if (args[i].charAt(0) == '-') {
					if (args[i].equals("-noinit")) {
						System.err.println("Option -noinit seen...");
						if (frontend != null)
							throw new Error("The -noinit option must occur before any file names");
						loadInitFile = false;
					} else if (args[i].equals("-bgpager")) {
						if (frontend != null)
							throw new Error("The -bgpager option must occur before any file names");
						this.backgroundPageCleaning = true;
					} else if (args[i].equals("-cachesize")) {
						this.cachesize = Integer.parseInt(args[++i]);
						if (frontend!=null)
							frontend.setCacheSize(this.cachesize);
					} else if (args[i].equals("-dbdir")) {
						if (frontend != null)
							throw new Error("The -dbdir option must occur before any file names");
						if (dbDir!=null) 
							throw new Error("The -dbdir option can only be set once");
						this.dbDir = new File(args[++i]);
					} else if (args[i].equals("-o")) {
						ensureFrontEnd();
						frontend.redirectOutput(
							new PrintStream(new FileOutputStream(args[++i])));
					} else if (args[i].equals("-i")) {
						System.err.println("Option -i seen...");
						ensureFrontEnd();
						boolean keepGoing = false;
						do {
							try {
								System.err.println(
									"\n--- Interactive mode... type queries!");
								System.err.println("end with CTRL-D");
								frontend.load(System.in);
								keepGoing = false;
							} catch (ParseException e) {
								keepGoing = true;
								System.err.println(
									"TyRuBaParser:" + e.getMessage());
							} catch (TypeModeError e) {
								keepGoing = true;
								System.err.println(
									"Type or Mode Error: " + e.getMessage());
							}
						} while (keepGoing);
					} else if (args[i].equals("-silent")) {
						RuleBase.silent = true;
					} else if (args[i].equals("-nocache")) {
						if (frontend != null)
							throw new Error("The -nocache option must occur before any file names");
						RuleBase.useCache = false;
					} else if (args[i].equals("-classpath")) {
						ensureFrontEnd();
						frontend.parse("classpath(\"" + args[++i] + "\").");
					} else if (args[i].equals("-parse")) {
						String command = args[++i];
						while (!(args[i].endsWith(".")))
							command += " " + args[++i];
						System.err.println("-parse " + command);
						frontend.parse(command);
					} else if (args[i].equals("-benchmark")) {
						ensureFrontEnd();
						String queryfile = args[++i];
						PerformanceTest test = PerformanceTest.make(frontend,queryfile);
						frontend.output().println("----- results for tests in "+queryfile+" -------");						frontend.output().println(test);
						//frontend.output().println(PoormansProfiler.profile());
					} else if (args[i].equals("-metadata")) {
						ensureFrontEnd();
						frontend.enableMetaData();
					} else {
						System.err.println(
							"*** Error: unkown commandline option: " + args[i]);
						System.exit(-1);
					}
				} else {
					ensureFrontEnd();
					System.err.println("Loading file: " + args[i]);
					frontend.load(args[i]);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("TyRuBaParser:" + e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("TyRuBaParser:" + e.getMessage());
			System.exit(-2);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(-3);
		} catch (TypeModeError e) {
			e.printStackTrace();
			System.exit(-4);
		}
		frontend.shutdown();
		System.exit(0);
	}

}

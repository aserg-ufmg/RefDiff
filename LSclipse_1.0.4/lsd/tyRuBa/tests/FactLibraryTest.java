///*
// * Created on Jul 8, 2004
// */
//package tyRuBa.tests;
//
//import java.io.File;
//import java.io.IOException;
//
//import tyRuBa.engine.FrontEnd;
//import tyRuBa.engine.RuleBaseBucket;
//import tyRuBa.modes.TypeModeError;
//import tyRuBa.parser.ParseException;
//import tyRuBa.util.ElementSource;
//
///**
// * @author riecken
// */
//public class FactLibraryTest extends TyrubaTest {
//
//	private RubFileBucket bucket;
//	
//	/**
//	 * @param arg0
//	 */
//	public FactLibraryTest(String arg0) {
//		super(arg0);
//	}
//	
//	protected void setUp() throws Exception {
//		frontend = new FrontEnd(initfile);
//		File f = new File("fdb/test.jar");
//		if (!f.exists()) {
//			fail("test not setup properly, fdb/test.jar does not exist");
//		}
//		f = new File("fdb/test.rub");
//		if (!f.exists()) {
//			fail("test not setup properly, fdb/test.rub does not exist");
//		}
//		
//		bucket = new RubFileBucket(frontend, "fdb/test.rub");
//		frontend.getFactLibraryManager().addLibraryJarFile("fdb/test.jar");
//		frontend.parse("TYPE Package AS String " +
//					   "TYPE CU AS String " +
//				       "TYPE Field AS String " + 
//                       "TYPE Class AS String " +
//                       "TYPE Interface AS String " +
//                       "TYPE RefType = Class | Interface " +
//                       "TYPE Primitive AS String " +
//                       "TYPE Type = RefType | Primitive " +
//                       "TYPE Method AS String " +
//                       "TYPE Constructor AS String " +
//                       "TYPE Callable = Method | Constructor " +
//                       "TYPE Initializer AS String " +
//                       "TYPE Block = Callable | Initializer " +
//                       "TYPE Element = Package | CU | Field | Type | Block");
//		frontend.parse("package :: Package PERSISTENT MODES (F) IS NONDET END");
//		frontend.parse("class :: Class PERSISTENT MODES (F) IS NONDET END");
//        frontend.parse("child :: Element, Element PERSISTENT MODES (F,F) IS NONDET (B,F) IS NONDET (F,B) IS SEMIDET END");
//	}
//	
//	public void testFactLibrary() throws Exception {
//	}
//
//	public void tstFactLibrary() throws Exception {
//        ElementSource result = bucket.frameQuery("package(?P),child(?P,?CU),child(?CU,?T)");
//		while (result.hasMoreElements()) {
//		    System.err.println(result.nextElement());
//        }
//	}
//	
//	class RubFileBucket extends RuleBaseBucket {
//		
//		String myfile;
//		
//		RubFileBucket(FrontEnd fe,String filename) {
//			super(fe,filename);
//			myfile = filename;
//		}
//
//		public void update() throws ParseException, TypeModeError {
//			try {
//				load(myfile);
//			}
//			catch (IOException e) {
//				throw new Error("IOError for file "+myfile+": "+e.getMessage());
//			}
//		}
//		
//		public String toString() {
//			return "RubFileBucket("+myfile+")";
//		}
//
//	}
//
//}

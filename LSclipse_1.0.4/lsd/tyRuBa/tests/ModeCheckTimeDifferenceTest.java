//package tyRuBa.tests;
//
///*
// * Created on June 27, 2003
// */
//import tyRuBa.engine.Frame;
//import tyRuBa.engine.FrontEnd;
//import tyRuBa.engine.RBExpression;
//import tyRuBa.engine.RuleBase;
//import tyRuBa.engine.SimpleRuleBaseBucket;
//import tyRuBa.modes.TypeModeError;
//import tyRuBa.parser.ParseException;
//import tyRuBa.tdbc.PreparedQuery;
//import tyRuBa.util.ElementSource;
//
//public class ModeCheckTimeDifferenceTest extends TyrubaTest {
//
//	public ModeCheckTimeDifferenceTest(String arg0) {
//		super(arg0);
//	}
//
//	protected void setUp() throws Exception {
//		TyrubaTest.initfile = true;
//		RuleBase.useCache = false;
//		RuleBase.silent = true;
//		super.setUp();
//	}
//	
//	public void tstTypeCheckTime() throws ParseException, TypeModeError {
//		SimpleRuleBaseBucket bucket = new SimpleRuleBaseBucket(frontend);
//		RBExpression e = bucket.makeExpression("string_append(abc,def,?a)");//		long runtime;
//
//		for (int loop = 0; loop <= 100; loop++) {
//			runtime = System.currentTimeMillis();
//			for (int i = 0; i < 1000; i++) {
//				ElementSource results = bucket.frameQuery(e);
//				Frame f = (Frame) results.nextElement();
//				f.get(FrontEnd.makeVar("?a"));
//			}
//			runtime = System.currentTimeMillis() - runtime;
//			if (loop % 25 == 0)
//				System.err.println(loop + ": Slow running time = " + runtime / 1000.0);
//
//			PreparedQuery runnable = bucket.prepareForRunning(e);
//			runtime = System.currentTimeMillis();
//			for (int i = 0; i < 1000; i++) {
//				ElementSource results = runnable.start();
//				Frame f = (Frame) results.nextElement();
//				f.get(FrontEnd.makeVar("?a"));
//			}
//			runtime = System.currentTimeMillis() - runtime;
//			if (loop % 25 == 0)
//				System.err.println(loop + ": Fast running time = " + runtime / 1000.0);
//		}
//
//	}
//}

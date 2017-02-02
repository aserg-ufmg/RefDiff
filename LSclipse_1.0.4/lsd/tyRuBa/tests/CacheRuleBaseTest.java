package tyRuBa.tests;

import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class CacheRuleBaseTest extends TyrubaTest {
	
	public void setUp() throws Exception {
		RuleBase.useCache = true;
		TyrubaTest.initfile = false;
		super.setUp();
	}

	public CacheRuleBaseTest(String arg0) {
		super(arg0);
	}
	
	public void test() throws ParseException, TypeModeError {
		frontend.parse("foo, bar, goo :: String\n" +			"MODES (F) IS NONDET END");
			
		test_must_fail("foo(?x)");
		frontend.parse("foo(?x) :- bar(?x).");
		test_must_fail("foo(?x)");
		frontend.parse("bar(bar).");
		test_must_succeed("foo(bar)");
		
		frontend.parse("goo(goo).");
		frontend.parse("bar(?x) :- goo(?x).");
		test_must_succeed("foo(goo)");
		test_resultcount("foo(?x)", 2);
	}
	
	public void testMinnieBug() throws ParseException, TypeModeError {
		frontend.parse("married :: String, String\n" +			"MODES (F,F) IS NONDET END\n");
		
		frontend.parse("married(Minnie,Mickey).");
		frontend.parse("married(?x,?y) :- married(?y,?x).");

		test_resultcount("married(?a,?b)", 2);
		test_must_succeed("married(Minnie,Mickey)");
		test_must_equal("married(Minnie,?x)", "?x", "Mickey");
	}
	
}

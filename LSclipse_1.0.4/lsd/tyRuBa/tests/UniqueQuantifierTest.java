package tyRuBa.tests;

import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class UniqueQuantifierTest extends TyrubaTest {

	public UniqueQuantifierTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		RuleBase.silent = false;
		super.setUp();
	}

	public void testSimpleUnique() throws ParseException, TypeModeError {
		frontend.parse("unique_member :: ?t, [?t]\n" +			"MODES (F,B) IS SEMIDET END");
		frontend.parse("unique_member(?x,?lst) :- UNIQUE ?x : member(?x,?lst).");

		test_must_succeed("unique_member(1,[1])");
		test_must_fail("unique_member(?x,[1,2])");
		test_must_fail("unique_member(?x,[])");
		test_must_equal("unique_member(?x,[hello])", "?x", "hello");
	}
	
	public void testUniqueWithVariableBoundBefore() throws ParseException, TypeModeError {
		frontend.parse("foo :: String, [Object]\n" +			"MODES (F,B) IS SEMIDET END");
		frontend.parse("foo(?x,?lst) :- " +
			"equals(?x,bar), (UNIQUE ?x : member(?x,?lst)).");

		test_must_succeed("foo(?x,[bar])");
		test_must_succeed("foo(?x,[bar,bar])");
		test_must_fail("foo(?x,[foo])");
		test_must_fail("foo(?x,[foo,bar])");
		test_must_fail("foo(?x,[])");
	}

	public void testUniqueWithVariableUsedAfter() throws ParseException, TypeModeError {
		frontend.parse("foo :: String, [Object]\n" +			"MODES (F,B) IS SEMIDET END");
		frontend.parse("foo(?x,?lst) :- " +			"(UNIQUE ?x : member(?x,?lst)), equals(?x,foo).");
		
		test_must_succeed("foo(?x,[foo])");
		test_must_succeed("foo(?x,[foo,foo])");
		test_must_fail("foo(?x,[bar])");
		test_must_fail("foo(?x,[foo,bar])");
		test_must_fail("foo(?x,[])");
	}
	
	public void testUniqueWithMultipleQuantifiedVariables() throws ParseException, TypeModeError {
		test_must_succeed("UNIQUE ?x,?y: append(?x,?y,[])");
		test_must_fail("UNIQUE ?x,?y: equals(?x,1), member(?y,[])");
		test_must_fail("UNIQUE ?x,?y: member(?x,[]), equals(?y,a)");
		test_must_succeed("UNIQUE ?x,?y: member(?x,[1]), member(?y,[a])");
		test_must_succeed("UNIQUE ?x,?y: member(?x,[1,1]), member(?y,[a,a])");
		test_must_fail("UNIQUE ?x,?y: equals(?x,1), member(?y,[a,b])");
		test_must_fail("UNIQUE ?x,?y: member(?x,[1,2]), equals(?y,a)");
		test_must_fail("UNIQUE ?x,?y: append(?x,?y,[1,2,3])");
	}
}

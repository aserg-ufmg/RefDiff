package tyRuBa.tests;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class TyniNativePredicateTest extends TyrubaTest {

	public TyniNativePredicateTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		super.setUp();
	}

	public void testString() throws ParseException, TypeModeError {
		test_must_succeed("String(abcd)");
		test_must_fail("equals([?x|?],[1,a]), String(?x)");
		test_resultcount("member(?x,[1,a,2,b]), String(?x)", 2);
//		test_must_fail("String(abc<def>)");
//		test_must_fail("string(?a)");
	}
	
	public void testInteger() throws ParseException, TypeModeError {
		test_must_succeed("Integer(123)");
		test_must_fail("equals([?x|?],[a,1]), Integer(?x)");
		test_resultcount("member(?x,[1,a,2,b]), Integer(?x)", 2);
//		test_must_fail("Integer(<abc,def>)");
//		test_must_fail("integer(?a)");
	}
	
	public void testRange() throws ParseException, TypeModeError {
		test_must_findall("range(23,27,?x)", "?x",
			new String[] { "23", "24", "25", "26" });
	}
	
	public void testGreater() throws ParseException, TypeModeError {
		test_must_succeed("greater(10,5)");
		test_must_fail("greater(5,10)");
	}
	
	public void testSum() throws ParseException, TypeModeError {
		test_must_succeed("sum(1,2,3)");
		test_must_equal("sum(1,2,?x)", "?x", "3");
		test_must_equal("sum(1,?x,3)", "?x", "2");
		test_must_equal("sum(?x,2,3)", "?x", "1");
	}
	
	public void testMul() throws ParseException, TypeModeError {
		test_must_succeed("mul(2,3,6)");
		test_must_fail("mul(2,3,7)");
		test_must_equal("mul(2,3,?x)", "?x", "6");
	}
	
	public void testHashValue() throws ParseException, TypeModeError {
		test_must_succeed("hash_value(foobar,?v)");
	}
	
	public void testLength() throws ParseException, TypeModeError {
		test_must_equal("length([a,b,c],?x)", "?x", "3");
		test_must_succeed("length([a,b,c],3)");
		test_must_fail("length([a,b,c],2)");
	}
	
	public void testDebugPrint() throws ParseException, TypeModeError {
		test_must_succeed("equals(?x,partner), debug_print({Howdy ?x, how ya doin??})");
	}

	public void testWriteOutput() throws ParseException, TypeModeError {
		test_must_succeed("equals(?x,partner), write_output({Howdy ?x, how ya doin??})");
	}

}

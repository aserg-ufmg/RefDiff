package tyRuBa.tests;

import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class PrologNativePredicateTest extends TyrubaTest {

	public PrologNativePredicateTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		RuleBase.useCache = true;
		RuleBase.silent = true;
		super.setUp();
	}

	public void testListRef() throws ParseException, TypeModeError {
		test_must_equal("list_ref(?n,[a,b,c],b)", "?n", "1");
		test_must_equal("list_ref(2,[a,b,c],?x)", "?x", "c");
		test_must_findall("list_ref(?n,[a,b,a],a)", "?n",
			new String[] { "0", "2" });
	}
	
	public void testAppend() throws ParseException, TypeModeError {
		test_must_succeed("append([1,2,3],[4,5],[1,2,3,4,5])");
		test_must_equal("append(?x,[4,5],[1,2,3,4,5])", "?x", "[1,2,3]");
		test_must_fail("append(?x,[3,5],[1,2,3,4,5])");
		test_must_findall("append(?x,?y,[1,2,3,4])",
			new String[] {"?x", "?y"},
			new String[][] {
				new String[] { "[]", "[1,2,3,4]" },
				new String[] { "[1]", "[2,3,4]" },
				new String[] { "[1,2]", "[3,4]" },
				new String[] { "[1,2,3]", "[4]" },
				new String[] { "[1,2,3,4]", "[]" } 
			});
	}
	
	public void testMember() throws ParseException, TypeModeError {
		test_must_findall("member(?x,[1,2,3,4])", "?x",
			new String[] { "1", "2", "3", "4" });
	}
	
	public void testPermutation() throws ParseException, TypeModeError {
		test_must_succeed("permutation([1,2,3],[1,2,3])");
		test_must_succeed("permutation([1,2,3],[1,3,2])");
		test_must_succeed("permutation([1,2,3],[2,1,3])");
		test_must_succeed("permutation([1,2,3],[2,3,1])");
		test_must_succeed("permutation([1,2,3],[3,1,2])");
		test_must_succeed("permutation([1,2,3],[3,2,1])");
		test_must_fail("permutation([1,2,3],[1,2,4])");
		test_must_succeed("permutation([1,2,3], ?x)");
		test_must_succeed("permutation(?x, [1,2,3])");
	}
	
	public void testReverse() throws ParseException, TypeModeError {
		test_must_equal("reverse([1,2,3],?x)", "?x", "[3,2,1]");
		test_must_equal("reverse(?x,[1,2,3])", "?x", "[3,2,1]");
	}
	
//	public void testList() throws ParseException, TypeModeError {
//		test_must_succeed("list([1,2,3])");
//		test_must_succeed("list([])");
//		test_must_fail("list(1)");
//		test_must_fail("list(abc)");
//	}
	
	public void testEqualOrUnify() throws ParseException, TypeModeError {
		frontend.parse("TYPE foo<?x> AS <String>");
		test_must_succeed("equals([1,2,3],[1,2,3])");		
		test_must_fail("equals(a,b)");
		test_must_succeed("equals(foo<abc>,foo<abc>)");
		test_must_equal("equals(foo<abc>,?x)", "?x", "foo<abc>");
		test_must_equal("equals(a,?x)", "?x", "a");
		test_must_equal("equals(?x,a)", "?x", "a");
	}
	
	public void testZip() throws ParseException, TypeModeError {
		test_must_succeed(
			"zip([1,2,3],[a,b,c],?x), equals(?x,[<1,a>,<2,b>,<3,c>])");
		test_must_succeed("zip([1,2,3],[a,b,c],[<1,a>,<2,b>,<3,c>])");
		test_must_fail("zip([1,2],[a,b,c],[<1,a>,<2,b>,<3,c>])");
		
//		try {
//			test_must_fail("zip([1,2],[a,b],[<1,2>,<3,4>])");
//			fail("Shoud have thrown a TypeError");
//		}
//		catch (TypeModeError e){
//		}
	}
	
	public void testSumList() throws ParseException, TypeModeError {
		test_must_equal("sumList([1,2,3],?x)", "?x", "6");
	}
	
	public void testTrueFalse() throws ParseException, TypeModeError {
		test_must_fail("false()");
		test_must_succeed("true()");
		test_must_succeed("true();false()");
		test_must_succeed("false();true()");
		test_must_fail("true(),false()");
		test_must_fail("false(),true()");
	}

}

package tyRuBa.tests;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class TestFilterTest extends TyrubaTest {

	public TestFilterTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		super.setUp();
	}

	public void testTestFilter() throws ParseException, TypeModeError {
		frontend.parse("test_append_ffb :: [?t]\n" +			"MODES (B) IS DET END\n");
		frontend.parse("test_append_ffb(?z) :- " +			"TEST(EXISTS ?x,?y : append(?x, ?y, ?z)).");
		
		test_must_succeed("test_append_ffb([1,2,3])");
		
		frontend.parse("test_append_bbf :: [?t],[?t]\n" +			"MODES (B,B) IS DET END\n");
		frontend.parse("test_append_bbf(?x, ?y) :- " +			"TEST(EXISTS ?z : append(?x,?y,?z)).");
		
		test_must_succeed("test_append_bbf([1],[2])");

		frontend.parse("test_list_ref_bbf :: =Integer, [?t]\n");
		frontend.parse("test_list_ref_bbf(?x,?y) :- " +			"TEST(EXISTS ?z : list_ref(?x,?y,?z)).");
		
		test_must_succeed("test_list_ref_bbf(0,[1,2])");
		test_must_fail("test_list_ref_bbf(4,[1])");

		frontend.parse("test_list_ref_fbb :: [?t], ?t\n");
		frontend.parse("test_list_ref_fbb(?y,?z) :- " +			"TEST(EXISTS ?x : list_ref(?x,?y,?z)).");
		
		test_must_succeed("test_list_ref_fbb([1,2,3],2)");
		test_must_fail("test_list_ref_fbb([1],2)");
		
		test_must_succeed("TEST(append(?,?,[1,2,3]))");
		test_must_fail("TEST(member(?,[]))");
	}

}

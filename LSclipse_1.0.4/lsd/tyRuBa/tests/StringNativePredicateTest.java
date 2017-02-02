package tyRuBa.tests;

import tyRuBa.engine.FrontEnd;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;

public class StringNativePredicateTest extends TyrubaTest {

	public StringNativePredicateTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		super.setUp();
	}
		
	public void testEqual() throws ParseException, TypeModeError {
		test_must_succeed("equals(abc,\"abc\")");
	}
	
	public void testStringAppend() throws ParseException, TypeModeError {
		test_must_succeed("string_append(abc,def,abcdef)");
		test_must_fail("string_append(abc,def,quiwe)");
		test_must_equal("string_append(abc,def,?x)","?x","abcdef");
		test_must_equal("string_append(abc,?x,abcdef)", "?x", "def");
		test_must_fail("string_append(abc,?x,abdef)");
		test_must_equal("string_append(?x,def,abcdef)", "?x", "abc");
		test_must_fail("string_append(?x,def,abcddf)");
		test_must_findall("string_append(?x,?y,abcdef)", "?x",
			new String[] { "\"\"", "a", "ab", "abc", "abcd", "abcde", "abcdef" });
		test_must_equal("string_append(?x,?x,abcabc)", "?x", "abc");
		
		test_must_equal("string_append(ab,cd,ef,?x)", "?x", "abcdef");
	}

	public void testStringAppendTypeCheck() throws ParseException, TypeModeError {
		FrontEnd frontend = new FrontEnd(false);
		try {
			ElementSource results =
				frontend.varQuery("string_append(1,2,?x)", "?x");
			fail("this should have thrown type error");
		} catch (TypeModeError e) {
//			System.err.println(e.getMessage());
		}
	}

	public void testStringLength() throws ParseException, TypeModeError {
		test_must_succeed("string_length(abc,3)");
		test_must_equal("string_length(abc,?x)", "?x", "3");
	}

	public void testStringIndexSplit() throws ParseException, TypeModeError {
		test_must_equal("string_index_split(0,abcdef,?x,?y)",
			new String[] {"?x", "?y"},
			new String[] {"\"\"", "abcdef"});

		test_must_equal("string_index_split(6,abcdef,?x,?y)",
			new String[] {"?x", "?y"},
			new String[] {"abcdef", "\"\""});
			
		test_must_equal("string_index_split(3,abcdef,?x,?y)",
			new String[] {"?x", "?y" },
			new String[] {"abc", "def"});
		
		test_must_succeed("string_index_split(0,abcdef,\"\",abcdef)");
		test_must_succeed("string_index_split(6,abcdef,abcdef,\"\")");
		test_must_succeed("string_index_split(3,abcdef,abc,def)");
		test_must_fail("string_index_split(2,abcdef,abc,def)");

		test_must_fail("string_index_split(7,abcdef,?x,?y)");
	}

	public void testStringReplace() throws ParseException, TypeModeError {
		test_must_succeed("string_replace(\".\",\"/\",abc.def.ghi,\"abc/def/ghi\")");
		test_must_succeed("string_replace(\".\",\"/\",abc.def.ghi.,\"abc/def/ghi/\")");
		test_must_succeed(
			"string_replace(\".\",\"/\",\".abc.def.ghi.\",\"/abc/def/ghi/\")");
		test_must_succeed(
			"string_replace(\".\",\"/\",\".abc.def.ghi\",\"/abc/def/ghi\")");
		
		test_must_equal("string_replace(\".\",\"/\",\".abc.def.ghi\",?x)",
			"?x", "\"/abc/def/ghi\"");
			
		test_must_equal("string_replace(\".\",\"/\",?x,\"/abc/def/ghi\")",
			"?x", "\".abc.def.ghi\"");
	}
	
	public void testStringSplitAtLast()	throws ParseException, TypeModeError {
		test_must_succeed("string_split_at_last(l,hello,hel,o)");
		test_must_succeed("string_split_at_last(ll,hello,he,o)");
		
		test_must_equal("string_split_at_last(l,hello,?start,?end)", "?start", "hel");
		test_must_equal("string_split_at_last(l,hello,?start,?end)", "?end", "o");
		test_must_equal("string_split_at_last(l,?x,hel,o)",	"?x", "hello");
		test_must_equal("string_split_at_last(hel,?x,\"\",lo)", "?x", "hello");
		test_must_equal("string_split_at_last(ll,?x,he,o)", "?x", "hello");
		test_must_fail("string_split_at_last(\".\",?x,foo,faa.Foo)");
		test_must_equal("string_split_at_last(\".\",?x,foo.faa,Foo)","?x","foo.faa.Foo");
	}

	public void testToUpperCase() throws ParseException, TypeModeError {
		test_must_equal("to_upper_case(abcde,?x)", "?x", "ABCDE");
		test_must_succeed("to_upper_case(abcde,ABCDE)");
		test_must_fail("to_upper_case(abcde,AbcDE)");
	}
	
	public void testToLowerCase() throws ParseException, TypeModeError {
		test_must_equal("to_lower_case(abCde,?x)", "?x", "abcde");
		test_must_succeed("to_lower_case(abcde,abcde)");
		test_must_fail("to_lower_case(abCDe,AbCDE)");
	}
	
	public void testCapitalize() throws ParseException, TypeModeError {
		test_must_equal("capitalize(abcd,?x)","?x","Abcd");
		test_must_succeed("capitalize(abcd,Abcd)");
		test_must_fail("capitalize(abcd,abcd)");
		test_must_fail("capitalize(\"\",?)");
	}
	
	public void testDecapitalize() throws ParseException, TypeModeError {
		test_must_equal("decapitalize(ABCD,?x)", "?x", "aBCD");
		test_must_succeed("decapitalize(abcd,abcd)");
		test_must_fail("decapitalize(ABCD,ABCD)");
		test_must_fail("decapitalize(\"\",?)");
	}
	
	public void testStringRepeat() throws ParseException, TypeModeError {
		test_must_equal("string_repeat(3,foo,?x)", "?x", "foofoofoo");
		test_must_equal("string_repeat(0,foo,?x)", "?x", "\"\"");
		test_must_fail("string_repeat(-1,foo,?x)");
	}
}

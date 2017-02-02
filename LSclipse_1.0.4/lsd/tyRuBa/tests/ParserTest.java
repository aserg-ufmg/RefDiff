package tyRuBa.tests;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class ParserTest extends TyrubaTest {

	public ParserTest(String arg0) {
		super(arg0);
	}

	public void testParseExpression() throws ParseException, TypeModeError {
		try {
			test_must_fail("string_append(?x,?y,abc) string_append(?y,?z,abc)");
			fail("Should throw a parse exception");
		}
		catch (ParseException e) {
		}
	}

}

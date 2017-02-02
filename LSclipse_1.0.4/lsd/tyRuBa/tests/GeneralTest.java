/*
 * Created on Oct 4, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package tyRuBa.tests;

import java.util.Collection;

import tyRuBa.engine.RBExpression;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;

/**
 * @author kdvolder
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GeneralTest extends TyrubaTest {

	/**
	 * Constructor for GeneralTest.
	 * @param arg0
	 */
	public GeneralTest(String arg0) {
		super(arg0);
	}

	public void setUp() throws Exception {
		TyrubaTest.initfile = true;
		super.setUp();
	}

	public void testAskForMoreAgain() throws ParseException, TypeModeError {
		ElementSource result = frontend.frameQuery("string_append(abc,def,?x)");
		int ctr = 0;
		while (result.hasMoreElements()) {
			ctr++;
			result.nextElement();
		}
		assertEquals(1,ctr);
		assertFalse(result.hasMoreElements());
	}		

	public void testPersistentRBQuotedInFact() throws ParseException, TypeModeError {
		frontend.parse("test :: String PERSISTENT MODES (F) IS NONDET END");
		frontend.parse("test({Hola Pola!}).");
		test_must_succeed("test({Hola Pola!})");
		test_must_succeed("test(\"Hola Pola!\")");
	}
	
	public void testGetVars() throws ParseException, TypeModeError {
		RBExpression exp = frontend.makeExpression("string_append(?x,?a,abc);string_append(?x,?b,def).");
		Collection vars = exp.getVariables();
		assertEquals(vars.size(),1);
		assertEquals(vars.toString(),"[?x]");
	}
}

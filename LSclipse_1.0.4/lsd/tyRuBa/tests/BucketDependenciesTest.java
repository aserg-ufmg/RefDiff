/*
 * Created on Apr 3, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tests;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RuleBase;
import tyRuBa.engine.RuleBaseBucket;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

/**
 * @author kdvolder
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BucketDependenciesTest extends TyrubaTest {

	public BucketDependenciesTest(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception {
		RuleBase.silent = true;
		super.setUp();
		frontend.parse(
				"bucket :: Integer " +
				"MODES (F) IS NONDET END ");
		frontend.parse(
				"child :: Integer, Integer " +
				"MODES " +
				"    (B,F) IS NONDET " +
				"    (F,B) IS SEMIDET " +
				"    (F,F) IS NONDET " +
				"END ");
	}
	
	private class TestBucket extends RuleBaseBucket {
		
		static final int num_buckets = 128;
		
		private int myID;
		
		public TestBucket(FrontEnd frontend, int bucketID) {
			super(frontend, ""+bucketID);
			myID = bucketID;
		}

		protected void update() throws TypeModeError, ParseException {
			parse("bucket("+myID+").");
			assertChild(2*myID+1);
			assertChild(2*myID+2);
		}

		private void assertChild(int childID) throws ParseException, TypeModeError {
			if (childID<num_buckets) {
				new TestBucket(frontend(),childID); // creates new bucket while updating!
				parse("child("+myID+","+childID+").");
			}
		}
	}

    public void testBucketUpdateWithDependencies() throws ParseException, TypeModeError {
    		new TestBucket(frontend,0);
    		test_resultcount("bucket(?id)",TestBucket.num_buckets);
    }

    public void testBucketUpdateWithDependenciesDouble() throws ParseException, TypeModeError {
		new TestBucket(frontend,1);
		new TestBucket(frontend,2);
		test_resultcount("bucket(?id)",TestBucket.num_buckets-1); // -1 root node missing.
    }
    
}

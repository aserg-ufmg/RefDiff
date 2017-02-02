package tyRuBa.tests;

import tyRuBa.engine.RuleBase;
import tyRuBa.engine.SimpleRuleBaseBucket;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class RuleBaseBucketTest extends TyrubaTest {
	
	SimpleRuleBaseBucket bucket;
	SimpleRuleBaseBucket otherBucket;

	public void setUp() throws Exception {
		RuleBase.silent = true;
		super.setUp();
		bucket = new SimpleRuleBaseBucket(frontend);
		otherBucket = new SimpleRuleBaseBucket(frontend);
	}

	public void testOutdateSemiDetPersistentFact() throws ParseException, TypeModeError {
		frontend.parse("foo :: String, String \n" +
			"PERSISTENT MODES (B,F) IS SEMIDET END \n");
		bucket.addStuff("foo(bucket,buck).");
		bucket.addStuff("foo(target,targ).");
		test_resultcount("foo(bucket,?x)",1);
		test_resultcount("foo(bucket,buck)",1);

		bucket.setOutdated();
	
		test_resultcount("foo(bucket,?x)",1);
		test_resultcount("foo(bucket,buck)",1);		
	}

    public void testOutdateSemiDetPersistentFact2() throws ParseException, TypeModeError {
        frontend.parse("foo :: String, String \n" +
            "PERSISTENT MODES (B,F) IS SEMIDET END \n");
        bucket.addStuff("foo(bucket,buck).");
        bucket.addStuff("foo(target,targ).");
        test_resultcount("foo(bucket,?x)",1);
        test_resultcount("foo(bucket,buck)",1);

        bucket.clearStuff();
        bucket.addStuff("foo(bucket,buck2).");
        bucket.addStuff("foo(target,targ2).");
        test_resultcount("foo(bucket,?x)",1);
        test_resultcount("foo(bucket,buck2)",1);
            
    }

    public void testOutdateSemiDetPersistentFact3() throws ParseException, TypeModeError {
        frontend.parse("foo :: String, String \n" +
            "PERSISTENT MODES (B,F) IS SEMIDET END \n");
        
        
        
        bucket.addStuff("foo(a#bucket,buck).");
        bucket.addStuff("foo(a#target,targ).");
        test_resultcount("foo(a#bucket,?x)",1);
        test_resultcount("foo(a#bucket,buck)",1);

        bucket.clearStuff();
        bucket.addStuff("foo(b#bucket,buck).");
        bucket.addStuff("foo(b#target,targ).");
        
        test_resultcount("foo(b#bucket,?x)",1);
        test_resultcount("foo(b#bucket,buck)",1);
        
        bucket.clearStuff();
        bucket.addStuff("foo(a#bucket,buck1).");
        bucket.addStuff("foo(a#target,targ1).");
        
        test_resultcount("foo(a#bucket,?x)",1);
        test_resultcount("foo(a#bucket,buck1)",1);
        
    }
    
    
	public void testGlobalFact() throws ParseException, TypeModeError {
		frontend.parse("foo :: String \n" +			"MODES (F) IS NONDET END \n");
		bucket.addStuff("foo(bucket).");
		frontend.parse("foo(frontend).");
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(frontend)");
		test_must_succeed("foo(bucket)", bucket);
		test_must_succeed("foo(frontend)", bucket);
		test_must_fail("foo(bad)");
		
		bucket.clearStuff();
		
		test_must_fail("foo(bucket)");
		test_must_fail("foo(bucket)", bucket);
		test_must_succeed("foo(frontend)");
		test_must_succeed("foo(frontend)", bucket);
		test_must_fail("foo(c)");		
	}
	
	public void testAllCleared() throws ParseException, TypeModeError {
		frontend.parse("foo :: String \n" +
			"MODES (F) IS NONDET END \n");
		
		bucket.addStuff("foo(bucket).");
		bucket.addStuff("foo(bucket2).");
		bucket.addStuff("foo(bucket3).");

		test_must_succeed("foo(bucket).");

		bucket.clearStuff();

		// This test assumes that bucket will update and lose
		// parsed stuff when it runs the query.
		
		test_must_fail("foo(?x).");		
	}
	
	public void testLocalFact() throws ParseException, TypeModeError {
		bucket.addStuff("bar :: String");
		bucket.addStuff("bar(bucket).");
		test_must_succeed("bar(bucket)", bucket);
		try {
			test_must_fail("bar(bucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to the frontend.");
		} catch (TypeModeError e) {
		}		
		
		try {
			frontend.parse("bar(frontend).");
			fail ("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to the frontend.");
		} catch (TypeModeError e) {
		}
		
		bucket.clearStuff();
		try {
			test_must_fail("bar(bucket).", bucket);
			fail ("This should have thrown a TypeModeError because bucket has been " +				"cleared and the predicate bar is no longer declared in bucket.");
		} catch (TypeModeError e) {
		}
	}
	
	public void testDuplicate() throws ParseException, TypeModeError {
		frontend.parse("foobar :: String");
		try {
			bucket.parse("foobar :: String");
			fail("This should have thrown a TypeModeError since foobar is declared " +				"in the frontend, bucket cannot declare it again.");
		} catch (TypeModeError e) {
		}
	}
	
	public void testGlobalRule() throws ParseException, TypeModeError {
		frontend.parse("foo :: Object");
		bucket.addStuff("foo(?x) :- equals(?x, bucket).");
		frontend.parse("foo(?x) :- equals(?x, 1).");
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(1)");
		test_must_succeed("foo(bucket)", bucket);
		test_must_succeed("foo(1)", bucket);
		test_must_fail("foo(bad)");
		
		bucket.clearStuff();
		
		test_must_fail("foo(bucket)");
		test_must_fail("foo(bucket)", bucket);
		test_must_succeed("foo(1)");
		test_must_succeed("foo(1)", bucket);
		test_must_fail("foo(c)");		
	}
	
	public void testLocalRule() throws ParseException, TypeModeError {
		bucket.addStuff("bar :: String");
		bucket.addStuff("bar(?x) :- equals(?x, bucket).");
		test_must_succeed("bar(bucket)", bucket);
		try {
			test_must_fail("bar(bucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}		
		
		try {
			frontend.parse("bar(?x) :- equals(?x, frontend).");
			fail ("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}
		
		bucket.clearStuff();
		try {
			test_must_fail("bar(bucket).", bucket);
			fail ("This should have thrown a TypeModeError because bucket has been " +				"cleared and the predicate bar is no longer declared in bucket.");
		} catch (TypeModeError e) {			System.err.println(e.getMessage());
		}
	}
	
	public void testGlobalFactWithMultipleBuckets() throws ParseException, TypeModeError {
		frontend.parse("foo :: String");
		frontend.parse("foo(frontend).");
		otherBucket.addStuff("foo(otherBucket).");
		bucket.addStuff("foo(bucket).");
		
		test_must_succeed("foo(frontend)");
		test_must_succeed("foo(frontend)", otherBucket);
		test_must_succeed("foo(frontend)", bucket);
		
		test_must_succeed("foo(otherBucket)");
		test_must_succeed("foo(otherBucket)", otherBucket);
		test_must_succeed("foo(otherBucket)", bucket);
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(bucket)", otherBucket);
		test_must_succeed("foo(bucket)", bucket);
		
		otherBucket.clearStuff();
		
		test_must_succeed("foo(frontend)");
		test_must_succeed("foo(frontend)", otherBucket);
		test_must_succeed("foo(frontend)", bucket);
		
		test_must_fail("foo(otherBucket)");
		test_must_fail("foo(otherBucket)", otherBucket);
		test_must_fail("foo(otherBucket)", bucket);
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(bucket)", otherBucket);
		test_must_succeed("foo(bucket)", bucket);	
	}
	
	public void testLocalFactWithMultipleBuckets() throws ParseException, TypeModeError {
		bucket.addStuff("bar :: String");
		bucket.addStuff("bar(bucket).");
		test_must_succeed("bar(bucket)", bucket);
		
		try {
			test_must_fail("bar(bucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}
		
		try {
			test_must_fail("bar(bucket)", otherBucket);
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to otherBucket.");
		} catch (TypeModeError e) {
		}
		
		otherBucket.addStuff("bar :: String");
		otherBucket.addStuff("bar(otherBucket).");
		test_must_succeed("bar(otherBucket)", otherBucket);
		test_must_fail("bar(otherBucket)", bucket);
		
		try {
			test_must_fail("bar(otherBucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}
		
		test_must_fail("bar(bucket)", otherBucket);
	}

	public void testGlobalRuleWithMultipleBuckets() throws ParseException, TypeModeError {
		frontend.parse("foo :: Object");
		frontend.parse("foo(?x) :- equals(?x,1).");
		otherBucket.addStuff("foo(?x) :- equals(?x,10.1).");
		bucket.addStuff("foo(?x) :- equals(?x,bucket).");
		
		test_must_succeed("foo(1)");
		test_must_succeed("foo(1)", otherBucket);
		test_must_succeed("foo(1)", bucket);
		
		test_must_succeed("foo(10.1)");
		test_must_succeed("foo(10.1)", otherBucket);
		test_must_succeed("foo(10.1)", bucket);
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(bucket)", otherBucket);
		test_must_succeed("foo(bucket)", bucket);
		
		otherBucket.clearStuff();
		
		test_must_succeed("foo(1)");
		test_must_succeed("foo(1)", otherBucket);
		test_must_succeed("foo(1)", bucket);
		
		test_must_fail("foo(10.1)");
		test_must_fail("foo(10.1)", otherBucket);
		test_must_fail("foo(10.1)", bucket);
		
		test_must_succeed("foo(bucket)");
		test_must_succeed("foo(bucket)", otherBucket);
		test_must_succeed("foo(bucket)", bucket);	
	}

	public void testLocalRuleWithMultipleBuckets() throws ParseException, TypeModeError {
		bucket.addStuff("bar :: String");
		bucket.addStuff("bar(?x) :- equals(?x,bucket).");
		test_must_succeed("bar(bucket)", bucket);
		
		try {
			test_must_fail("bar(bucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}
		
		try {
			test_must_fail("bar(bucket)", otherBucket);
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to otherBucket.");
		} catch (TypeModeError e) {
		}
		
		otherBucket.addStuff("bar :: String");
		otherBucket.addStuff("bar(?x) :- equals(?x,otherBucket).");
		test_must_succeed("bar(otherBucket)", otherBucket);
		test_must_fail("bar(otherBucket)", bucket);
		
		try {
			test_must_fail("bar(otherBucket)");
			fail("This should have thrown a TypeModeError because the predicate bar " +				"is unknown to frontend.");
		} catch (TypeModeError e) {
		}
		
		test_must_fail("bar(bucket)", otherBucket);
	}

	/**
	 * A test designed to catch a particular bug in the ModedRuleBaseCollection.
	 * The bug surfaces when a new moded rulebase gets created on the fly after
	 * some of the rules that were inserted ot the collection have been
	 * outdated.
	 *
	 * @vsf+ RuleBaseCollectionBug
	 */
	public void testRuleBaseCollectionBug() throws ParseException, TypeModeError {
		frontend.parse("bar :: String,String " +
				"MODES (F,F) IS NONDET END");
//		front.addStuff("foo :: String,String " +
//				"MODES (F,F) IS NONDET END");
//		bucket.addStuff("foo(?x,?y) :- equals(?x,bucket), bar(?x,?y), bar(?y,?x).");
		bucket.addStuff("bar(?x,?z) :- bar(?x,?y), bar(?y,?z).");
		frontend.updateBuckets();
		bucket.setOutdated();
		bucket.addStuff("bar(?x,?z) :- bar(?x,?y), bar(?y,?z), bar(?x,?z).");
		frontend.updateBuckets(); // with bug... expected to crash here
		bucket.setOutdated();
		frontend.updateBuckets(); // with bug... expected to crash here
	}	
	
	public void testRuleBaseDestroy() throws ParseException, TypeModeError {
		frontend.parse("factFrom :: String " +
				"MODES (F) IS NONDET END");
		bucket.addStuff("factFrom(bucket).");
		otherBucket.addStuff("factFrom(otherBucket).");
		test_resultcount("factFrom(?buck)",2);
		otherBucket.destroy();
		test_resultcount("factFrom(?buck)",1);
		bucket.destroy();
		test_resultcount("factFrom(?buck)",0);
	}
	
	public RuleBaseBucketTest(String arg0) {
		super(arg0);
	}
}

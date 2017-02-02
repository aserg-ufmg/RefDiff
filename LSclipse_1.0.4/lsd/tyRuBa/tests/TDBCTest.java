/*
 * Created on Feb 16, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package tyRuBa.tests;

import tyRuBa.engine.FrontEnd;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.Insert;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.Query;
import tyRuBa.tdbc.TyrubaException;
import junit.framework.TestCase;

/**
 * @author kdvolder
 */
public class TDBCTest extends TestCase {

	Connection conn;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		FrontEnd fe = new FrontEnd(true);

		fe.parse("TYPE Method  AS String");
		fe.parse("TYPE Field   AS String");
		fe.parse("TYPE Member = Method | Field");
		fe.parse("foo :: Method,String \n" +
				"MODES (F,F) IS NONDET END");
		fe.parse("foo(booh::Method,booh).");

		fe.parse("fooMem :: Member,String \n" +
				 "MODES (F,F) IS NONDET END");
		fe.parse("fooMem(f_booh::Field,f_booh).");
		fe.parse("fooMem(m_booh::Method,m_booh).");

		conn = new Connection(fe);
	}


//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}

	public void testQuery() throws Exception {
		Query stat = conn.createQuery();
		ResultSet results = stat.executeQuery("string_append(?x,?y,abcde)");
		int count = 0;
		while (results.next()) {
			count++;
			String x = results.getString("?x");
			String y = results.getString("?y");
			assertEquals(x+y,"abcde");
		}
		assertEquals(count,6);
	}

	public void testNoColsQuery() throws Exception {
		Query stat = conn.createQuery();
		ResultSet results = stat.executeQuery("string_append(ab,cde,abcde)");
		assertTrue(results.next());
		assertFalse(results.next());

		results = stat.executeQuery("string_append(ab,cd,abcde)");
		assertFalse(results.next());
	}
	
	public void testPreparedQuery() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		String x = "a b c";
		String y = " d e";
		stat.put("!x",x);
		stat.put("!y",y);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String xy = results.getString("?xy");
			assertEquals(x+y,xy);
		}
		assertEquals(count,1);
	}

	public void testPreparedQueryMissingVar() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		String x = "a b c";
		String y = " d e";
		stat.put("!x",x);
		try {
			ResultSet results = stat.executeQuery();
			fail("Should have detected the problem that !y has not been put.");
		}
		catch (TyrubaException e) {
			// ok 
		}
	}

	public void testPreparedQueryBadType() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		try {
			stat.put("!x",123);
			fail("This should have thrown an exception. !m MUST be a string");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage()); 
		}
	}

	public void testPreparedQueryBadVar() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		try {
			stat.put("!m","abc");
			fail("This should have thrown an exception. !m is not defined");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage()); 
		}
	}

	public void testPreparedQueryBadType2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(!m::Method,?n)");
		try {
			stat.put("!m",123);
			fail("This should have thrown an exception. !m MUST be a string");
		} catch (TyrubaException e) {
			// good! 
		}
	}
	
	public void testPreparedQueryUDTypeOut() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(?m,!n)");
		String n = "booh";
		stat.put("!n",n);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String m = results.getString("?m");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}

	public void testPreparedQueryUDTypeOut2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("fooMem(?m,!n)");
		String n = "m_booh";
		stat.put("!n",n);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String m = results.getString("?m");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}
	
	public void testPreparedQueryUDTypeIn() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(!m::Method,?n)");
		String m = "booh";
		stat.put("!m",m);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String n = results.getString("?n");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}
	
	public void testInsert() throws Exception {
		Insert ins = conn.createInsert();
		ins.executeInsert("foo(bih::Method,bah).");
		
		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(bih::Method,?bah).");

		int count = 0;
		while (results.next()) {
			count++;
			String bah = results.getString("?bah");
			assertEquals(bah,"bah");
		}
		assertEquals(count,1);
	}

	public void testPreparedInsert() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh).");

		ins.put("!duh","bim");
		ins.executeInsert();
		
		ins.put("!duh","bam");
		ins.executeInsert();

		ins.put("!duh","bom");
		ins.executeInsert();
		
		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(clock::Method,?sound).");

		int count = 0;
		while (results.next()) {
			count++;
			String sound = results.getString("?sound");
			assertTrue(sound.length()==3 && sound.startsWith("b") && sound.endsWith("m"));
		}
		assertEquals(count,3);
	}

	public void testPreparedInsertMissingVar() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(!dah::Method,!duh).");

		ins.put("!duh","abc");
		try {
			ins.executeInsert();
			fail("Should have made an error: the variable !dah has not been put");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertBadType() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh).");

		try {
			ins.put("!duh",1);
			fail("Should have made an error: he variable !duh should be a string.");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertBadVar() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh).");

		try {
			ins.put("!dah","abc");
			fail("Should have made an error: the variable !dah is unknown");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertUDType() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(!dah::Method,!duh).");

		ins.put("!dah","abc");
		ins.put("!duh","abc");
		
		ins.executeInsert();

		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(?out,abc).");

		int count = 0;
		while (results.next()) {
			count++;
			String out = results.getString("?out");
			assertEquals(out,"abc");
		}
		assertEquals(count,1);
	}

}

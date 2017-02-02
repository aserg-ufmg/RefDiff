package tyRuBa.tests;

import java.io.Serializable;

import tyRuBa.engine.FunctorIdentifier;
import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.TyrubaException;

public class TypeTest extends TyrubaTest implements Serializable {

	public TypeTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		TyrubaTest.initfile = true;
		RuleBase.useCache = true;
		RuleBase.silent = true;
		super.setUp();
	}

	public void testIllegalCastToAbstractType() throws Exception {
		frontend.parse(
			"TYPE RefType AS String " +
			"TYPE PrimType AS String " +
			"TYPE Type = RefType | PrimType");

		frontend.parse(
			"type :: Type " +
			"MODES (F) IS NONDET END ");
		
		frontend.parse(
		     "type(foo.Bar::RefType).");

		try {
			test_must_equal("type(?x),equals(?x,?s::Type)","?s","foo.Bar");
			fail("Should throw a TypeModeError");
		}
		catch (TypeModeError e) {
			assertTrue("Wrong error message:"+e.getMessage(), e.getMessage().indexOf("Illegal cast")>=0);
		}
		
	}
	
    public void testMixingCompositeAndAtomicTypes() throws Exception {

		frontend.parse(
		"TYPE Element AS String ");

		frontend.parse(
		"name :: Element, String " +
		"MODES (B,F) IS SEMIDET END ");
		
		frontend.parse(
		"re_name :: Element, org.apache.regexp.RE ");

		frontend.parse(
		"TYPE Pattern     = RegExpPat" +
		"	             | Name " +
		"             	| StarPat " +
		"             	| SubtypePat ");

		frontend.parse(
				"TYPE RegExpPat AS RegExp " +
				"TYPE Name AS String " +
				"TYPE StarPat AS <> " +
				"TYPE SubtypePat AS Pattern ");

		frontend.parse(
		"match :: Pattern, Element " +
		"match(?re::RegExpPat, ?X)   :- re_name(?X, ?re). " +
		"match(?S::Name, ?X)         :- name(?X, ?S). " +
		"match(StarPat<>, ?X)        :- Element(?X). ");
		frontend.parse(
		"match(?P::SubtypePat, ?X)   :- match(?P,?X). ");
    }
	
    public void testCompositeSubTypes() throws Exception {

		frontend.parse(
			"TYPE Element AS String " +

		"name :: Element, String " +
		"MODES (B,F) IS SEMIDET END " +
		
		"re_name :: Element, org.apache.regexp.RE " +

		"TYPE Pattern<>   = RegExpPat<>" +
			"             | Name<> " +
			"             | StarPat<> " +
			"             | SubtypePat<> " +			
			"TYPE RegExpPat<> AS <org.apache.regexp.RE>" +
			"TYPE Name<> AS <String> " +
			"TYPE StarPat<> AS <> " +
			"TYPE SubtypePat<> AS <Pattern<>> ");

		frontend.parse(
		"match :: Pattern<>, Element " +
		"match(RegExpPat<?re>, ?X)   :- re_name(?X, ?re). " +
		"match(Name<?S>, ?X)         :- name(?X, ?S). " +
		"match(StarPat<>, ?X)        :- Element(?X). ");
		frontend.parse(
		"match(SubtypePat<?P>, ?X)   :- match(?P,?X). ");
    }
    
	public void testBadAppend1() throws ParseException, TypeModeError {
		try {
			frontend.parse("append(?x,abc,?x).");
			fail("This should have thrown a TypeModeError because \"abc\" does not" +				"have type [?t]");
		} catch (TypeModeError e) {
		}
	}

	public void testBadAppend2() throws ParseException, TypeModeError {
		try {
			frontend.parse("append([?x|?xs],?ys,[?x|?zs]) :- append(?x,?ys,?zs).");
			fail("This should have thrown a TypeModeError because ?x is not a list");
		} catch (TypeModeError e) {
		}
	}

	public void testEmptyList()throws ParseException, TypeModeError {
		frontend.parse("list :: [?x]\n" 
			+ "MODES (FREE) IS NONDET END");
		frontend.parse("list([]).");
	}

	public void testDeclarationArity() throws ParseException, TypeModeError {
		try {
			frontend.parse("planet :: String\n"
				+ "MODES\n"
				+ "(BOUND, BOUND, FREE) IS DET\n"
				+ "(FREE, FREE, BOUND) IS MULTI\n"
				+ "END\n");
					
			fail("This should have thrown a TypeModeError because planet has arity 1" +				"but the mode declarations have arities 3");
		} catch (TypeModeError e) {
		}
	}

	public void testUndefinedPredError() throws ParseException, TypeModeError {
		try {
			frontend.parse("plannet(Earth).");
			fail("This should have thrown a TypeModeError because plannet is not a declared" +				"predicate");
		} catch (TypeModeError e) {
		}
	}

	public void testStrictType() throws ParseException, TypeModeError {
		try {
			frontend.parse("isSum :: Integer, Integer, Integer");
			frontend.parse("isSum(?x,?y,?z) :-  sum(?x,?y,?z).");
			fail("This should fail because isSum must have strict(=) types");
		} catch (TypeModeError e) {
		}
		
		try {
			frontend.parse("foooo :: [Integer]");
			frontend.parse("foooo([?x,?y,?z]) :-  sum(?x,?y,?z).");
			fail("This should fail because foooo must have strict(=) types");
		} catch (TypeModeError e) {
		}

		try {
			frontend.parse("sumLost :: [Integer], Integer");
			frontend.parse("sumLost([],0).");
			frontend.parse("sumLost([?x|?xs],?sum) " +				":-  sumLost(?xs,?rest), sum(?x,?rest,?sum).");
			fail("This should fail because isSum must have strict(=) types");
		} catch (TypeModeError e) {
		}
		
		frontend.parse("sameObject :: =Object, =Object "+
		    "MODES (F,B) IS DET " +			"      (B,F) IS DET " +			"END");
		frontend.parse("sameObject(?x,?x).");
		test_must_succeed("equals(?x,foo),sameObject(?x,?x)");
	}
	
	public void testEmptyListRule1() throws ParseException, TypeModeError {
		frontend.parse("foo :: Integer, [Integer] \n" +			"MODES (B,F) IS NONDET END");
		frontend.parse("foo(?T,?X) :- equals(?X,[]), Integer(?T), " +			"NOT(equals(?T,1)); equals(?X,[1]), Integer(?T).");
	}

	public void testEmptyListRule2() throws ParseException, TypeModeError {
		frontend.parse("foo :: Integer, [Integer] \n" +
			"MODES (B,F) IS MULTI END");
		frontend.parse("foo(?T,[]) :- Integer(?T), NOT(equals(?T,1)).");
		frontend.parse("foo(?T,[?T]) :- Integer(?T).");
	}
	
	public void testStrictTypesInRules() throws ParseException, TypeModeError {
		frontend.parse("inEither :: ?a,[?a],[?a]\n" +			"MODES (F,B,B) IS NONDET END");
		frontend.parse("inEither(?x,?l1,?l2) :- member(?x,?l1); member(?x,?l2).");		

		frontend.parse("inEitherOne :: ?a,[?a],[?a]\n" +
			"MODES (F,B,B) IS NONDET END\n");
		frontend.parse("inEitherOne(?x,?l1,?l2) :- member(?x,?l1).");
		frontend.parse("inEitherOne(?x,?l1,?l2) :- member(?x,?l2).");

		frontend.parse("inAny :: ?a,[[?a]]\n" +
			"MODES (F,B) IS NONDET END");
		frontend.parse("inAny(?x,[?l1|?lmore]) :- member(?x,?l1); inAny(?x,?lmore).");
		
		test_must_findall("inEither(?x,[1,2,3],[4,5,6])", "?x", 
			new String[] { "1", "2", "3", "4", "5", "6" });
		test_must_findall("inEitherOne(?x,[1,2,3],[4,5,6])", "?x", 
			new String[] { "1", "2", "3", "4", "5", "6" });
		test_must_findall("inAny(?x,[[1],[2,3],[4,5,6]])", "?x", 
			new String[] { "1", "2", "3", "4", "5", "6" });
	}
	
	public void testStrictTypesWithShrinkingTypes() throws ParseException, TypeModeError {
		try {
			test_must_fail("sum(?x,?x,?x), member(?x,[0,1,a]), Integer(?x)");
			fail("This should have thrown a TypeModeError since sum requires ?x to be " +				"strictly Integer");
		} catch (TypeModeError e) {
			System.err.println(e.getMessage()); 
		}

		try {
			test_must_fail("member(?x,[0,1,a]), Integer(?x), sum(?x,?x,?x)");
			fail("This should have thrown a TypeModeError since sum requires ?x to be " +
				"strictly Integer");
		} catch (TypeModeError e) {
			System.err.println(e.getMessage()); 
		}

		try {
			test_must_fail("Integer(?x), sum(?x,?x,?x), member(?x,[0,1,a])");
			fail("This should have thrown a TypeModeError since sum requires ?x to be " +
				"strictly Integer");
		} catch (TypeModeError e) {
//			System.err.println(e.getMessage()); 
		}
		
		test_must_succeed("Integer(?x), sum(?x,?x,?x), member(?x,[0,1,2])");
	}
	
	public void testTypeTests() throws ParseException, TypeModeError {
		test_must_succeed("Integer(1)");
		test_must_succeed("Number(1.1)");
		test_must_fail("list_ref(0,[0,a],?x), String(?x)");
		
		frontend.parse("TYPE Sno AS String");
		frontend.parse("TYPE Bol AS String");
		frontend.parse("TYPE Snobol = Sno | Bol");
		test_must_succeed("Sno(sno::Sno)");
		test_must_succeed("Snobol(bol::Bol)");
		test_must_succeed("Snobol(sno::Sno)");
		test_must_fail("list_ref(0,[sno::Sno,bol::Bol],?x), Bol(?x)");
		
		frontend.parse("TYPE Bar AS String");
	}
	
	public void testListType() throws ParseException, TypeModeError {
		frontend.parse(
			"descriptorImg :: [String], String "+
			"MODES "+
			"   (B,F) IS SEMIDET "+
			"END ");
		frontend.parse(
			"descriptorImg([compilationUnit], DESC_OBJS_CUNIT).");
	}

	/** Test added because of bug (undesirable type error) found combining tvars 
	 * with actual types in lists.
	 */
	public void testListType2() throws ParseException, TypeModeError {
		frontend.parse(
			"abc :: [String] "+
			"MODES "+
			"   (F) IS MULTI "+
			"END ");
		frontend.parse(
			"abc([a,b,?c]) :- equals(?c,c).");
		frontend.parse(
			"abc(?x) :- equals(?x,[a,b,?c]), equals(?c,c).");
		frontend.parse(
			"abc(?x) :- equals(?c,c), equals(?x,[a,b,?c]).");
	}

	public void testUserListType() throws ParseException, TypeModeError {
		
		frontend.parse("TYPE Package AS String ");
		frontend.parse("TYPE CU AS String ");
		frontend.parse("TYPE Element = Package | CU ");

		frontend.parse(
			"child :: Element, Element " +			"MODES " +
			"  (F,F) IS NONDET " +
			"  (B,F) IS NONDET " +
			"  (F,B) IS SEMIDET " +
			"END");
		
		frontend.parse(
			"viewFromHere :: Element, [Element] "+
			"MODES " +
			"	(B,F) IS NONDET " +
			"END");
		frontend.parse(
			"viewFromHere(?X,?ViewOfChild) :- " +			"    child(?X,?Child), viewFromHere(?Child,?ChildsView), " +			"    equals(?ViewOfChild, [?Child | ?ChildsView]). ");
		frontend.parse(
			"viewFromHere(?X, []) :- Element(?X), NOT(child(?X,?)).");
	}
	
	public void testCompositeType() throws ParseException, TypeModeError {
		frontend.parse("TYPE Tree<?key,?value> = Node<?key,?value>" +
			"| Leaf<?value>" +
			"| EmptyTree<>" +			"| WeirdTree<?value>");
		frontend.parse(
			"TYPE Node<?key,?value> AS <?key, Tree<?key,?value>,Tree<?key,?value>>");
		frontend.parse("TYPE Leaf<?value> AS <?value>");
		frontend.parse("TYPE EmptyTree<> AS <>");
		frontend.parse("TYPE WeirdTree<?value> AS [?value]");
		
		frontend.parse("sumLeaf :: Tree<?x,=Integer>, =Integer \n" +			"MODES (B,F) IS SEMIDET END");
		frontend.parse("sumLeaf(Leaf<?value>,?value).");
		frontend.parse("sumLeaf(EmptyTree<>,0).");
		frontend.parse("sumLeaf(WeirdTree[],0).");
		frontend.parse("sumLeaf(WeirdTree[?l|?ist],?sum) " +			":- sumList(?ist,?istSum), sum(?l,?istSum,?sum).");
		frontend.parse("sumLeaf(Node<?key,?left,?right>,?sum)" +			":- sum(?leftSum,?rightSum,?sum), sumLeaf(?left,?leftSum)," +			"sumLeaf(?right,?rightSum).");
			
		frontend.parse("fringe :: Tree<?k,?v>, [=Leaf<?v>]\n" +			"MODES (B,F) IS DET END");

		frontend.parse("fringe(EmptyTree<>,[]).");
		frontend.parse("fringe(Leaf<?v>,[Leaf<?v>]).");
		frontend.parse("fringe(Node<?k,?t1,?t2>,?fringe) :-" +			"fringe(?t1,?f1),fringe(?t2,?f2),append(?f1,?f2,?fringe).");
			
		
		try {
			test_must_fail("sumLeaf(Node<1,1>,0)");
			fail("This should have thrown a TypeModeError since Node<?key,?value> " +				"has constructor with arity 3");
		} catch (TypeModeError e) {
//			System.err.println(e.getMessage());
		}
		test_must_succeed("sumLeaf(Leaf<1>,1)");
		test_must_fail("sumLeaf(Leaf<1>,2)");
		test_must_succeed("sumLeaf(EmptyTree<>,0)");
		test_must_fail("sumLeaf(EmptyTree<>,1)");
		test_must_succeed("sumLeaf(WeirdTree[],0)");
		test_must_succeed("sumLeaf(WeirdTree[1],1)");
		test_must_succeed("sumLeaf(WeirdTree[1,2,3],6)");
		test_must_fail("sumLeaf(WeirdTree[1,2,3],7)");
		test_must_succeed("sumLeaf(Node<1,EmptyTree<>,EmptyTree<>>,0)");
		test_must_succeed("sumLeaf(Node<1,EmptyTree<>,Leaf<2>>,2)");
		test_must_succeed("sumLeaf(Node<1,Leaf<3>,EmptyTree<>>,3)");
		test_must_succeed("sumLeaf(Node<1,Leaf<1>,Leaf<3>>,4)");
		try {
			test_must_fail("sumLeaf(Leaf<abc>,0)");
			fail("This should have thrown a TypeModeError since Leaf<abc> " +				"is of type Tree<?x,String> which is incompatible with " +				"Tree<Integer,Integer>");
		} catch (TypeModeError e) {
//			System.err.println(e.getMessage());
		}
	}
	
	public void testUserDefinedListType2() throws ParseException, TypeModeError {
		frontend.parse(
		"TYPE Type AS String " +		"TYPE Method AS String " +		"TYPE Element = Type | Method " +		"method :: Type, Method " +		"MODES" +		"(F,F) IS NONDET " +		"(B,F) IS NONDET " +		"(F,B) IS SEMIDET " +		"END");

		frontend.parse(
		"signature :: Method, String " +		"MODES " +		"(F,F) IS NONDET " +		"(B,F) IS SEMIDET " +		"(F,B) IS NONDET " +		"END ");
		
		frontend.parse(
		"methodizeHierarchy :: String, [Type], [Element] " +		"MODES " +		"	(B,B,F) REALLY IS DET " +		"END "); 		
		frontend.parse(
		"methodizeHierarchy(?sig,[],[]) :- String(?sig). "); 

		frontend.parse(
		"methodizeHierarchy(?sig,[?C1|?CH],?mH) :- " +		"   Type(?C1)," +		"   NOT( EXISTS ?m : method(?C1,?m), signature(?m,?sig) )," +		"   methodizeHierarchy(?sig,?CH,?mH). ");   

		frontend.parse(
		"methodizeHierarchy(?sig,[?C1|?CH],[?C1,?m|?mH]) :- " +		"  method(?C1,?m), signature(?m,?sig), " +		"  methodizeHierarchy(?sig,?CH,?mH).");   

	}
	
	public void testUserDefinedListType() throws TypeModeError, ParseException {
		frontend.parse("TYPE List<?element> = NonEmptyList<?element> | EmptyList<>");
		frontend.parse("TYPE NonEmptyList<?element> AS <?element,List<?element>>");
		frontend.parse("TYPE EmptyList<> AS <>");
		
		frontend.parse("append1 :: List<?x>, List<?x>, List<?x>\n" +			"MODES\n" +			"(B,B,F) IS DET\n" +			"(F,F,B) IS NONDET\n" +			"END");
		
		frontend.parse("append1(EmptyList<>,?x,?x).");
		frontend.parse("append1(NonEmptyList<?x,?xs>,?y,NonEmptyList<?x,?xys>)" +			":- append1(?xs,?y,?xys).");
		
		test_must_succeed("append1(EmptyList<>,EmptyList<>,EmptyList<>)");
		test_must_succeed("append1(EmptyList<>,NonEmptyList<1,EmptyList<>>," +			"NonEmptyList<1,EmptyList<>>)");
		test_must_succeed("append1(NonEmptyList<a,EmptyList<>>," +			"NonEmptyList<1,EmptyList<>>,NonEmptyList<a,NonEmptyList<1,EmptyList<>>>)");
		test_must_findall("append1(?x,?y,NonEmptyList<1,NonEmptyList<2,EmptyList<>>>)",			"?x", new String[] {
				"EmptyList<>",
				"NonEmptyList<1,EmptyList<>>",
				"NonEmptyList<1,NonEmptyList<2,EmptyList<>>>"
			});
			
		frontend.parse("sumList1 :: List<=Integer>, =Integer\n" +			"MODES (B,F) IS DET END");
		frontend.parse("sumList1(EmptyList<>,0).");
		frontend.parse("sumList1(NonEmptyList<?l,?ist>,?sum)" +			":- sumList1(?ist,?sumIst), sum(?l,?sumIst,?sum).");
		
		test_must_equal("sumList1(EmptyList<>,?x)", "?x", "0");
		test_must_equal("sumList1(NonEmptyList<1,EmptyList<>>,?x)", "?x", "1");
		test_must_equal("append1(NonEmptyList<1,EmptyList<>>," +			"NonEmptyList<2,EmptyList<>>,?list), sumList1(?list,?x)", "?x", "3");
	}
	
    public static class SourceLocation implements Serializable {
    	
		private static final long serialVersionUID = 1L;

			public SourceLocation(String parseFrom) {
    			// parseFrom looks like:  f1(f2,f3,f4)
    			int lparAt = parseFrom.indexOf('(');
    			int comma1 = parseFrom.indexOf(',',lparAt);
    			int comma2 = parseFrom.indexOf(',',comma1+1);
    			int rparAt = parseFrom.indexOf(')',comma2);

    			this.f1 = parseFrom.substring(0,lparAt);
    			this.f2 = Integer.parseInt(parseFrom.substring(lparAt+1,comma1));
    			this.f3 = Integer.parseInt(parseFrom.substring(comma1+1,comma2));
    			this.f4 = Integer.parseInt(parseFrom.substring(comma2+1,rparAt));
    		}

        public SourceLocation(String f1, int f2, int f3, int f4) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
            this.f4 = f4;
         }
        
        protected String f1;
        protected int f2;
        protected int f3;
        protected int f4;

        public boolean equals(Object _other) {
            if (!this.getClass().equals(_other.getClass()))
                return false;
            SourceLocation other = (SourceLocation)_other; 
            return  f1.equals(other.f1) &&
                    f2 == other.f2 &&
                    f3 == other.f3 &&
            		f4 == other.f4;
        }
    }
	
	public void testMappedCompositeType() throws ParseException, TypeModeError, TyrubaException {
	    
	    frontend.parse("TYPE SourceLocation<> AS <String,Integer,Integer,Integer> " +
	    		"TYPE SourceRange<>    AS <String,Integer,Integer,Integer,String> " +
	    		"TYPE SourceLink<> = SourceLocation<> | SourceRange<>  " +
	    		"" +
	    		"sourceLoc :: String, SourceLocation<> " +
	    		"PERSISTENT MODES (F,F) IS NONDET END " +
	    		"backLoc :: Object, String " +
	    		"MODES (F,F) IS NONDET END " +
	    		"backLoc(?X,?Y) :- sourceLoc(?Y,?X). " +
	    		"label :: Object, String");
	    frontend.addTypeMapping(new FunctorIdentifier("SourceLocation", 0), new TypeMapping() {

	        public Class getMappedClass() {
	            return SourceLocation.class;
	        }

            public Object toTyRuBa(Object obj) {
                SourceLocation sl_obj = (SourceLocation) obj;
                return new Object[] {
                        sl_obj.f1,
                        new Integer(sl_obj.f2),
                        new Integer(sl_obj.f3),
                        new Integer(sl_obj.f4)
                	};
            }

            public Object toJava(Object _parts) {
            		Object[] parts = (Object[])_parts;
            		return new SourceLocation(
            				(String)parts[0],
						((Integer)parts[1]).intValue(),
                        	((Integer)parts[2]).intValue(),
						((Integer)parts[3]).intValue());
            }
	    });
	    
	    frontend.parse("label(SourceLocation<?,?,?,?>,tisASourceLocation).");
	    frontend.parse("label(SourceLocation<foo,1,2,3>,niceOne).");
	    
	    SourceLocation sl1 = new SourceLocation("foo",1,2,3);
	    SourceLocation sl2 = new SourceLocation("bar",2,3,4);
	    
	    PreparedInsert insertIt = frontend.prepareForInsertion("sourceLoc(!name,!loc)");
	    insertIt.put("!name","foo");
	    insertIt.put("!loc",sl1);
	    insertIt.executeInsert();

	    insertIt.put("!name","bar");
	    insertIt.put("!loc",sl2);
	    insertIt.executeInsert();
	    
	    assertEquals(frontend.getProperty("foo","sourceLoc").up(),sl1);
	    assertEquals(frontend.getProperty("bar","sourceLoc").up(),sl2);
	    assertFalse(frontend.getProperty("foo","sourceLoc").up().equals(sl2));
	    assertFalse(frontend.getProperty("bar","sourceLoc").up().equals(sl1));
	    
	    assertEquals(frontend.getProperty(sl1,"backLoc").up(),"foo");
	    
	}

	public void testJavaTypeConstructor() throws Exception {
		frontend.parse(
				"sourceLoc :: String, tyRuBa.tests.TypeTest$SourceLocation " +
				"PERSISTENT MODES (F,F) IS NONDET END ");

		frontend.parse(
				"sourceLoc(foo,\"foo(1,2,3)\"::tyRuBa.tests.TypeTest$SourceLocation).");

		SourceLocation sl = new SourceLocation("foo",1,2,3);

		assertEquals(frontend.getProperty("foo","sourceLoc").up(),sl);
		
	}


    public void testSameStringUserDefinedTypes() throws Exception {
		String[] vehics = new String[] {
				"Bike","Car","Automobile","Big#Automobile","#Car","Big#"
		};
		String typeDec = "TYPE Vehicle = ";
		for (int i = 0; i < vehics.length; i++) {
			frontend.parse("TYPE "+vehics[i]+" AS String");
			if (i != 0) typeDec += " | "; 
			typeDec += vehics[i];
		}
		frontend.parse(typeDec);
		
		frontend.parse(	"vehicle :: Vehicle " +
			"PERSISTENT MODES " +
			"  (F) IS NONDET " +
			"END ");
		
		for (int i = 0; i < vehics.length; i++) {
			frontend.parse("vehicle(Foo::"+vehics[i]+").");
			frontend.parse("vehicle("+vehics[i]+"::"+vehics[i]+").");
		}
    		
		test_resultcount("vehicle(?x)",2*vehics.length);
    		
    		for (int i = 0; i < vehics.length; i++) {
        		for (int j = 0; j < vehics.length; j++) {
        			String query = "vehicle("+vehics[i]+"::"+vehics[j]+")";
        			if (i==j)
        				test_must_succeed(query);
        			else
        				test_must_fail(query);
    			}
		}
    }
    
}

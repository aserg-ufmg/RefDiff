package tyRuBa.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.regexp.RE;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/** 
 * A NativePredicate is a predicate that uses java methods in its evaluation.
 * There is an Implementation for each predicate mode that this predicate can
 * be evaluated in. NativePredicates should disappear before they are inserted
 * into a Rulebase since they should have been "convertedToMode" into Implementations
 * before insertion.
 */
public class NativePredicate extends RBComponent {

	private PredInfo predinfo;
	private ArrayList implementations = new ArrayList();
	private PredicateIdentifier predId;
	
	public Mode getMode() {
		return null; // Not yet converted, only specific Implementations have a mode
	}

	/** Constructor */	
	public NativePredicate(String name, TupleType argtypes) {
		predId = new PredicateIdentifier(name, argtypes.size());
		predinfo = Factory.makePredInfo(null, name, argtypes);
	}
	
	public NativePredicate(String name, Type t) {
		this(name, Factory.makeTupleType(t));
	}

	public NativePredicate(String name, Type t1, Type t2) {
		this(name, Factory.makeTupleType(t1, t2));
	}

	public NativePredicate(String name, Type t1, Type t2, Type t3) {
		this(name, Factory.makeTupleType(t1, t2, t3));
	}
	
	public NativePredicate(String name, Type t1, Type t2, Type t3, Type t4) {
		this(name, Factory.makeTupleType(t1, t2, t3, t4));
	}

	/** add a predicate mode to this native predicate along with the
	 *  implementation for this mode */
	public void addMode(Implementation imp) {
		predinfo.addPredicateMode(imp.getPredicateMode());
		implementations.add(imp);
	}

	/** add this predicate to rb */
	private void addToRuleBase(ModedRuleBaseIndex rb) throws TypeModeError {
		rb.insert(predinfo);
		rb.insert(this);
	}

	public Compiled compile(CompilationContext c) {
		throw new Error("Compilation only works after this has been converted "
			+ "to a proper mode.");
	}

	public TupleType typecheck(PredInfoProvider predinfo) throws TypeModeError {
		return getPredType();
	}
	
	public int getNumImplementation() {
		return implementations.size();
	}
	
	public Implementation getImplementationAt(int pos) {
		return (Implementation)implementations.get(pos);
	}

	public TupleType getPredType() {
		return predinfo.getTypeList();
	}
	
	public PredicateIdentifier getPredId() {
		return predId; 
	}
	
	public RBTuple getArgs() {
		throw new Error("getArgs cannot be called until an implementation has been selected");
	}
	
	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context) 
	throws TypeModeError {
		BindingList targetBindingList = mode.getParamModes();
		Implementation result = null;
		for (int i = 0; i < getNumImplementation(); i++) {
			Implementation candidate = getImplementationAt(i);
			if (targetBindingList.satisfyBinding(candidate.getBindingList())) {
				if (result == null 
				 || candidate.getMode().isBetterThan(result.getMode())) {
					result = candidate;
				}
			}
		}
		if (result == null) {
			throw new TypeModeError("Cannot find an implementation for "
					+ getPredName() + " :: " + mode);
		} else {
			return result; 
		}
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(predinfo.getPredId().toString());
		for (Iterator iter = implementations.iterator(); iter.hasNext();) {
			Implementation element = (Implementation) iter.next();
			result.append("\n" + element);
		}
		return result.toString();
	}
	
	public static void defineStringAppend(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_append =	new NativePredicate("string_append", 
			Type.string, Type.string, Type.string);

		string_append.addMode(new Implementation("BBF", "DET") {
			public void doit(RBTerm[] args) {
				String s1 = (String) args[0].up();
				String s2 = (String) args[1].up();
				addSolution(s1.concat(s2));
			}
		}); 

		string_append.addMode(new Implementation("FFB", "MULTI") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				int len = s.length();
				for (int i = 0; i <= len; i++) {
					addSolution(s.substring(0, i), s.substring(i));
				}
			}
		}); 

		string_append.addMode(new Implementation("BFB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				String s1 = (String) args[0].up();
				String s3 = (String) args[1].up();
				if (s3.startsWith(s1))
					addSolution(s3.substring(s1.length()));
			}
		}); 

		string_append.addMode(new Implementation("FBB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				String s2 = (String) args[0].up();
				String s3 = (String) args[1].up();
				if (s3.endsWith(s2))
					addSolution(s3.substring(0, s3.length() - s2.length())); 
			}
		}); 

		string_append.addToRuleBase(rb);
	}
	
	public static void defineStringLength(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_length =	new NativePredicate("string_length",
			Type.string, Type.integer);

		string_length.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				addSolution(new Integer(s.length()));
			}
		}); 

		string_length.addToRuleBase(rb);
	}

	public static void defineStringIndexSplit(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_index_split = new NativePredicate("string_index_split",
			Type.integer, Type.string, Type.string, Type.string);

		string_index_split.addMode(new Implementation("BBFF", "SEMIDET") {
			public void doit(RBTerm[] args) {
				int where = ((Integer) args[0].up()).intValue();
				String to_split = (String) args[1].up();
				if (where >= 0 && where <= to_split.length()) {
					addSolution(to_split.substring(0, where),
						to_split.substring(where));
				}
			}
		});

		string_index_split.addToRuleBase(rb);
	}

	public static void defineStringSplitAtLast(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_split_at_last = new NativePredicate("string_split_at_last", 
			Type.string, Type.string, Type.string, Type.string);

		string_split_at_last.addMode(new Implementation("BBFF", "DET") {
			public void doit(RBTerm[] args) {
				String separator = (String) args[0].up();
				String to_split = (String) args[1].up();
				int where = to_split.lastIndexOf(separator);
				if (where >= 0) {
					addSolution(to_split.substring(0, where),
						to_split.substring(where + separator.length()));
				} else {
					addSolution("", to_split);
				}
			}
		});

		string_split_at_last.addMode(new Implementation("BFBB", "DET") {
			public void doit(RBTerm[] args) {
				String separator = (String) args[0].up();
				String start = (String) args[1].up();
				String end = (String) args[2].up();
				if (end.indexOf(separator)<0)
					addSolution(start.concat(separator.concat(end)));
			}
		});

		string_split_at_last.addToRuleBase(rb);
	}

	public static String stringReplace(String r1, String r2, String s) {
		StringBuffer buf = new StringBuffer();
		int start = 0;
		do {
			int end = s.indexOf(r1, start);
			if (end == -1) {
				// no more matches
				buf.append(s.substring(start));
				return buf.toString();
			}
			buf.append(s.substring(start, end));
			buf.append(r2);
			start = end + r1.length();
		} while (start < s.length());
		return buf.toString();
	}

	public static void defineStringReplace(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_replace = new NativePredicate("string_replace",
			Type.string, Type.string, Type.string, Type.string);

		string_replace.addMode(new Implementation("BBBF", "DET") {
			public void doit(RBTerm[] args) {
				String r1 = (String) args[0].up();
				String r2 = (String) args[1].up();
				String s = (String) args[2].up();
				addSolution(stringReplace(r1, r2, s));
			}
		}); 

		string_replace.addMode(new Implementation("BBFB", "DET") {
			public void doit(RBTerm[] args) {
				String r1 = (String) args[0].up();
				String r2 = (String) args[1].up();
				String s = (String) args[2].up();
				addSolution(stringReplace(r2, r1, s));
			}
		});

		string_replace.addToRuleBase(rb);
	}

	public static void defineToLowerCase(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate to_lower_case = new NativePredicate("to_lower_case",
			Type.string, Type.string);

		to_lower_case.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				addSolution(s.toLowerCase());
			}
		}); 

		to_lower_case.addToRuleBase(rb);
	}

	public static void defineToUpperCase(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate to_upper_case =	new NativePredicate("to_upper_case",
			Type.string, Type.string);

		to_upper_case.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				addSolution(s.toUpperCase());
			}
		}); 

		to_upper_case.addToRuleBase(rb);
	}
	
	public static void defineCapitalize(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate capitalize = new NativePredicate("capitalize", 
			Type.string, Type.string);

		capitalize.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				if (s.length() > 0)
					addSolution(s.substring(0, 1).toUpperCase().concat(s.substring(1)));
			}
		}); 

		capitalize.addToRuleBase(rb);
	}	

	public static void defineDecapitalize(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate decapitalize = new NativePredicate("decapitalize",
			Type.string, Type.string);

		decapitalize.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				String s = (String) args[0].up();
				if (s.length() > 0)
					addSolution(s.substring(0, 1).toLowerCase().concat(s.substring(1)));
			}
		}); 

		decapitalize.addToRuleBase(rb);
	}	

	public static void defineStringRepeat(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate string_repeat = new NativePredicate("string_repeat",
			Type.integer, Type.string, Type.string);

		string_repeat.addMode(new Implementation("BBF", "DET") {
			public void doit(RBTerm[] args) {
				int num = ((Integer) args[0].up()).intValue();
				String rep = (String) args[1].up();
				if (num > -1) {
					String result = "";
					for (int i = 0; i < num; i++) 
						result += rep;
					addSolution(result);
				}
			}
		}); 

		string_repeat.addToRuleBase(rb);
	}	

	public static void defineTypeTest(ModedRuleBaseIndex rb, PredicateIdentifier id, final TypeConstructor t)
	throws TypeModeError {
		Assert.assertEquals(1,id.getArity());
		String javaName = t.getName();
		NativePredicate type_test = new NativePredicate(id.getName(), Factory.makeAtomicType(t));
		
		type_test.addMode(new Implementation("B", "SEMIDET") {
			public void doit(RBTerm[] args) {
                
				if (args[0].isOfType(t)) {
				    addSolution();
    			}
			}
		});
		
		type_test.addToRuleBase(rb);
	}

	public static void defineRange(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate range = new NativePredicate("range",
			Type.integer, Type.integer, Type.integer);

		range.addMode(new Implementation("BBF", "NONDET") {
			public void doit(RBTerm[] args) {
				int lo = ((Integer) args[0].up()).intValue();
				int high = ((Integer) args[1].up()).intValue();
				if (high > lo) {
					for (int i = lo; i < high; i++)
						addSolution(new Integer(i));
				}
			}
		}); 

		range.addToRuleBase(rb);
	}	

	public static void defineGreater(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate greater = new NativePredicate("greater", Type.integer, Type.integer);

		greater.addMode(new Implementation("BB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				int high = ((Integer) args[0].up()).intValue();
				int lo = ((Integer) args[1].up()).intValue();
				if (high > lo)
					addSolution();
			}
		}); 

		greater.addToRuleBase(rb);
	}	

	public static void defineSum(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate sum = new NativePredicate("sum",
			Type.integer, Type.integer, Type.integer);

		sum.addMode(new Implementation("BBF", "DET") {
			public void doit(RBTerm[] args) {
				int v1 = ((Integer) args[0].up()).intValue();
				int v2 = ((Integer) args[1].up()).intValue();
				addSolution(new Integer(v1 + v2));
			}
		}); 

		sum.addMode(new Implementation("BFB", "DET") {
			public void doit(RBTerm[] args) {
				int v1 = ((Integer) args[0].up()).intValue();
				int v2 = ((Integer) args[1].up()).intValue();
				addSolution(new Integer(v2 - v1));
			}
		}); 

		sum.addMode(new Implementation("FBB", "DET") {
			public void doit(RBTerm[] args) {
				int v1 = ((Integer) args[0].up()).intValue();
				int v2 = ((Integer) args[1].up()).intValue();
				addSolution(new Integer(v2 - v1));
			}
		}); 

		sum.addToRuleBase(rb);
	}	

	public static void defineMul(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate mul = new NativePredicate("mul", 
			Type.integer, Type.integer, Type.integer);

		mul.addMode(new Implementation("BBF", "DET") {
			public void doit(RBTerm[] args) {
				int v1 = ((Integer) args[0].up()).intValue();
				int v2 = ((Integer) args[1].up()).intValue();
				addSolution(new Integer(v1 * v2));
			}
		}); 

		mul.addMode(new Implementation("FBB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				int v2 = ((Integer) args[0].up()).intValue();
				int v3 = ((Integer) args[1].up()).intValue();
				if (v2 == 0) {
					if (v3 == 0) {
						addSolution(new Integer(0));
					}
				} else if (v3 % v2 == 0) {
					addSolution(new Integer(v3 / v2));
				}
			}
		}); 

		mul.addMode(new Implementation("BFB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				int v1 = ((Integer) args[0].up()).intValue();
				int v3 = ((Integer) args[1].up()).intValue();
				if (v1 == 0) {
					if (v3 == 0) {
						addSolution(new Integer(0));
					}
				} else if (v3 % v1 == 0) {
					addSolution(new Integer(v3 / v1));
				}
			}
		}); 
	
		mul.addToRuleBase(rb);
	}	
	
	public static void debug_print(Object ob) {
		System.err.println(ob.toString());
	}
	
	public static void defineDebugPrint(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate debug_print = new NativePredicate("debug_print", Type.object);

		debug_print.addMode(new Implementation("B", "DET") {
			public void doit(RBTerm[] args) {
				String msg = args[0].up().toString();
				debug_print(msg);
				addSolution();
			}
		}); 
		
		debug_print.addToRuleBase(rb);
	}	

	public static void defineThrowError(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate throw_error = new NativePredicate("throw_error", Type.string);

		throw_error.addMode(new Implementation("B", "FAIL") {
			public void doit(RBTerm[] args) {
				String msg = (String)args[0].up();
				throw new Error(msg);
			}
		}); 
		
		throw_error.addToRuleBase(rb);
	}	

	public static void defineWriteFile(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate write_file = new NativePredicate("write_file",
			Type.string, Type.string);

		write_file.addMode(new Implementation("BB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				String filename = (String)args[0].up();
				String contents = (String)args[1].up();
				debug_print("writing file: " + filename);
				try {
					File f = new File(filename);
					File p = f.getParentFile();
					if (p != null) {
						p.mkdirs();
					}
					PrintStream os = new PrintStream(new FileOutputStream(f));
					os.print(contents);
					os.close();
					addSolution();						
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
		}); 
		
		write_file.addToRuleBase(rb);
	}	

	public static void defineWriteOutput(final QueryEngine qe,ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate write_output = new NativePredicate("write_output", Type.string);

		write_output.addMode(new Implementation("B", "DET") {
			public void doit(RBTerm[] args) {
				String contents = (String)args[0].up();
				qe.output().print(contents);
				addSolution();
			}
		}); 
		
		write_output.addToRuleBase(rb);
	}	

	public static void defineFileseparator(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate fileseparator = new NativePredicate("fileseparator", Type.string);

		fileseparator.addMode(new Implementation("F", "DET") {
			public void doit(RBTerm[] args) {
				addSolution(System.getProperty("file.separator"));
			}
		}); 
		
		fileseparator.addToRuleBase(rb);
	}	

	public static void defineHashValue(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate hash_value = new NativePredicate("hash_value",
			Type.object, Type.integer);

		hash_value.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				addSolution(new Integer(args[0].up().hashCode()));
			}
		}); 
		
		hash_value.addToRuleBase(rb);
	}	

	public static void defineLength(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate length = new NativePredicate("length", 
			Factory.makeListType(Factory.makeTVar("element")), 
			Type.integer);

		length.addMode(new Implementation("BF", "DET") {
			public void doit(RBTerm[] args) {
				Object arg = args[0].up();
				if (arg instanceof String && arg.equals("[]")) {
					addSolution(new Integer(0));
				} else {
					addSolution(new Integer(((Object[])arg).length));
				}
			}
		}); 
		
		length.addToRuleBase(rb);
	}	

//	public static void defineRegexp(ModedRuleBaseIndex rb) throws TypeModeError {
//		NativePredicate regexp = new NativePredicate("regexp",
//			Factory.makeAtomicType(Factory.makeTypeConstructor(org.apache.regexp.RE.class)));
//
//		regexp.addMode(new Implementation("B", "SEMIDET") {
//			public void doit(RBTerm[] args) {
//				if (args[0].up() instanceof org.apache.regexp.RE)
//					addSolution();
//			}
//		}); 
//		
//		regexp.addToRuleBase(rb);
//	}	

	public static void defineReMatch(ModedRuleBaseIndex rb) throws TypeModeError {
		NativePredicate re_match = new NativePredicate("re_match", 
			Factory.makeStrictAtomicType(Factory.makeTypeConstructor(org.apache.regexp.RE.class)),
			Type.string);

		re_match.addMode(new Implementation("BB", "SEMIDET") {
			public void doit(RBTerm[] args) {
				RE re = (RE) args[0].up();
				String s = (String) args[1].up();
				if (re.match(s)) 
					addSolution();
			}
		}); 
		
		re_match.addToRuleBase(rb);
	}
	
	public static void defineConvertTo(ModedRuleBaseIndex rb, final TypeConstructor t)
	throws TypeModeError {
		NativePredicate convertTo = new NativePredicate("convertTo" + t.getName(),
			Factory.makeAtomicType(t), Factory.makeStrictAtomicType(t));
			
		convertTo.addMode(new Implementation("BF", "SEMIDET") {
			public void doit(RBTerm[] args) {
                
                if (args[0].isOfType(t)) {
                   addSolution(args[0].up());
                }
			}
		});
		 
		convertTo.addMode(new Implementation("FB", "DET") {
			public void doit(RBTerm[] args) {
				addSolution(args[0].up());
			}
		}); 

		convertTo.addToRuleBase(rb);
	}

//	public static void defineUnpack(ModedRuleBaseIndex rb, final TypeConstructor t) 
//	throws TypeModeError 
//	{
//		
//		final Type packedType = Factory.makeStrictAtomicType(t);
//		final Type unpackedType = Factory.makeStrictAtomicType(t.getRepresentationType());
//		
//		NativePredicate unpack = new NativePredicate("unpack" + t.getName(),
//			packedType, unpackedType);
//			
//		unpack.addMode(new Implementation("BF", "DET") {
//			public void doit(RBTerm[] args) {
//				RBUserDefinedObject arg = (RBUserDefinedObject) args[0];
//				addSolution(arg.getValue());
//			}
//		});
//		 
//		unpack.addMode(new Implementation("FB", "DET") {
//			public void doit(RBTerm[] args) {
//				try {
//					addTermSolution(new RBUserDefinedObject(t,(RBJavaObject)args[0]));
//				} catch (TypeModeError e) {
//					throw new Error("If this happens there must be a bug in the type system");
//				}
//			}
//		}); 
//
//		unpack.addToRuleBase(rb);
//	}

	
	public static void defineNativePredicates(QueryEngine qe) 
	throws TypeModeError {

		ModedRuleBaseIndex rules = qe.rulebase();
		
		// from string.rub 
		defineStringAppend(rules);
		defineStringLength(rules);
		defineStringSplitAtLast(rules);
		defineStringIndexSplit(rules);
		defineStringReplace(rules);
		defineToLowerCase(rules);
		defineToUpperCase(rules);
		defineCapitalize(rules);
		defineDecapitalize(rules);
		defineStringRepeat(rules);
		
		// from tyni.rub
//		defineTypeTest(rules, Factory.makeTypeConstructor(String.class));
//		defineTypeTest(rules, Factory.makeTypeConstructor(Integer.class));
//		defineTypeTest(rules, Factory.makeTypeConstructor(Object.class));
		defineRange(rules);
		defineGreater(rules);
		defineSum(rules);
		defineMul(rules);
		defineDebugPrint(rules);
		defineThrowError(rules);
		defineWriteFile(rules);
		defineWriteOutput(qe,rules);
		defineHashValue(rules);
		defineLength(rules);
		defineFileseparator(rules);
				
		// from regexp.rub
//		defineRegexp(rules);
		defineReMatch(rules);
	}

}

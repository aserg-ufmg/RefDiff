/*
 * Created on May 2, 2005
 */
package tyRuBa.engine;

import tyRuBa.modes.CompositeType;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.TyrubaException;

/**
 * A MetaBase is a wrapper around a QueryEngine providing facilities for storing
 * metaData into that queryEngine. 
 * 
 * @author kdvolder
 * @codegroup metadata
 */
public class MetaBase {

	private QueryEngine engine;
	
	private PreparedInsert typeConstructorFact = null;
	private PreparedInsert nameFact = null;
	private PreparedInsert subtypeFact = null;
	private PreparedInsert representationFact = null;
	private PreparedInsert arityFact = null;

	public static String declarations = 
		"TYPE meta.Type " +
		"	= meta.TupleType " +
		"	| meta.ListType " +
		"	| meta.CompositeType " +
		
		"TYPE meta.TupleType AS [meta.Type] " +
		"TYPE meta.ListType AS meta.Type " +
		"TYPE meta.CompositeType AS <tyRuBa.modes.TypeConstructor,meta.Type> " +
		
		"meta.typeConstructor :: tyRuBa.modes.TypeConstructor " +
		"MODES (F) IS NONDET END " +

		"meta.name :: Object, String " +
		"MODES (F,F) IS NONDET END " +

		"meta.arity :: tyRuBa.modes.TypeConstructor, Integer " +
		"MODES (B,F) IS DET " +
		"      (F,F) IS NONDET END " +

		"meta.subtype :: tyRuBa.modes.TypeConstructor, tyRuBa.modes.TypeConstructor " +
		"MODES (F,F) IS NONDET " +
		"      (B,F) IS NONDET" +
		"      (F,B) IS SEMIDET " +
		"END " +
		
		"meta.representation :: tyRuBa.modes.TypeConstructor, meta.Type " +
		"MODES (B,F) IS SEMIDET " +
		"      (F,F) IS NONDET " +
		"END ";

	MetaBase(QueryEngine engine) {
		this.engine = engine;
	}

	/**
	 * @rule 
	 * 
	 * 1) This method must be at the beginning of any method that
	 *       dereferences one of the XXXFact variables.
	 * 
	 *   field(?thisClass,?f),re_name(?f,/Fact$),reads(?M,?f,?accessF)
	 *     => calls(?M,?thisMethod,?callToMe), isBefore(?callToMe, ?accessF).
	 * 
	 *    msg: The method ?M must call ?thisMethod before accessing
	 *           ?f
	 * 
	 * 2) This method must initialize all the XXXFact variables.
	 * 
	 *   field(?thisClass,?f),re_name(?f,/Fact$),writes(?thisMethod,?f,?)
	 */
	private void lazyInitialize() {
		if (typeConstructorFact==null) 
			try {
				typeConstructorFact = engine.prepareForInsertion(
						"meta.typeConstructor(!t)");
				arityFact = engine.prepareForInsertion(
						"meta.arity(!t,!n)");
				nameFact = engine.prepareForInsertion(
						"meta.name(!t,!n)");
				subtypeFact = engine.prepareForInsertion(
						"meta.subtype(!super,!sub)");
				representationFact = engine.prepareForInsertion(
						"meta.representation(!type,!repType)");
			} catch (ParseException e) {
				e.printStackTrace();
				throw new Error(e);
			} catch (TypeModeError e) {
				e.printStackTrace();
				throw new Error(e);
			}
	}
	
	public void assertTypeConstructor(TypeConstructor type) {
		lazyInitialize();
		try {
			typeConstructorFact.put("!t",type);
			typeConstructorFact.executeInsert();
			
			nameFact.put("!t",type);
			nameFact.put("!n",type.getName());
			nameFact.executeInsert();
			
			arityFact.put("!t",type);
			arityFact.put("!n",type.getTypeArity());
			arityFact.executeInsert();
			
			type.setMetaBase(this);
		} catch (TyrubaException e) {
			throw new Error(e);
		}
	}

	public void assertSubtype(TypeConstructor superConst, TypeConstructor subConst) {
		lazyInitialize();
		try {
			subtypeFact.put("!super",superConst);
			subtypeFact.put("!sub",subConst);
			subtypeFact.executeInsert();
		} catch (TyrubaException e) {
			throw new Error(e);
		}
	}

	public void assertRepresentation(TypeConstructor constructor, Type repType) {
		lazyInitialize();
		try {
			representationFact.put("!type",constructor);
			representationFact.put("!repType",repType);
			representationFact.executeInsert();
		} catch (TyrubaException e) {
			throw new Error(e);
		}
	}

	/**
	 * Add the type mappings that allos smooth conversion from java representations
	 * for types into TyRuBa terms of type meta.Type.
	 * @throws TypeModeError
	 */
	public static void addTypeMappings(final FrontEnd frontend) throws TypeModeError {
		frontend.addTypeMapping(
				new FunctorIdentifier("meta.Type", 0), 
				new TypeMapping() {

					public Class getMappedClass() {
						return Type.class;
					}

					public Object toTyRuBa(Object obj) {
						throw new Error("This method cannot be caled because the class Type is abstract");
					}

					public Object toJava(Object parts) {
						throw new Error("This method cannot be called because meta.Type is abstract");
					}
				});
		frontend.addTypeMapping(
				new FunctorIdentifier("meta.CompositeType", 0), 
				new TypeMapping() {

					public Class getMappedClass() {
						return CompositeType.class;
					}

					public Object toTyRuBa(Object obj) {
						CompositeType compType = (CompositeType)obj;
						return new Object[] {
							compType.getTypeConstructor(),
							compType.getArgs()
						};
					}

					public Object toJava(Object _parts) {
						Object[] parts = (Object[])_parts;
						TypeConstructor constructor = (TypeConstructor)parts[0];
						TupleType args = (TupleType)parts[1];
						return constructor.apply(args,false);
					}
				});
		frontend.addTypeMapping(
				new FunctorIdentifier("meta.TupleType",0),
				new TypeMapping() {

					public Class getMappedClass() {
						return TupleType.class;
					}

					public Object toTyRuBa(Object obj) {
						return ((TupleType)obj).getTypes();
					}

					public Object toJava(Object obj) {
						Object[] objs = (Object[]) obj;
						Type[] types = new Type[objs.length];
						for (int i = 0; i < types.length; i++) {
							types[i] = (Type)objs[i];
						}
						return new TupleType(types);
					}
					
				});
	}

}

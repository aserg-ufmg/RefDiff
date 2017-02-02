package changetypes;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FactBase extends HashSet<Fact> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FactBase() {
		super();
	}

	public FactBase(FactBase f) {
		super(f);
	}

	public void print(PrintStream out) {
		if (this.size() > 0) {
			out.println("~~~Facts~~~");
			for (Fact f : this) {
				out.println(f.toString());
			}
		} else {
			out.println("No facts");
		}
	}

	private static String getParentFromFullName(String name) {
		// to get package from type, remove last dot
		int lastdot = name.lastIndexOf('.');
		if (lastdot == -1)
			return ""; // this is a short name
		return name.substring(0, lastdot);
	}

	private static String getChildFromFullName(String name) {
		int lastdot = name.lastIndexOf('.');
		return name.substring(lastdot + 1);
	}

	private void makeChangeFromFact(ChangeSet res, Fact f, char typ) {
		switch (f.type) {
		case PACKAGE:
			res.add(AtomicChange.makePackageChange(typ, f.params.get(0)));
			res.changecount[AtomicChange.ChangeTypes.ADD_PACKAGE.ordinal()]++;
			break;
		case TYPE:
			res.add(AtomicChange.makeTypeChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			// package is also modified
			res.add(AtomicChange.makePackageChange('M', f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.MOD_PACKAGE.ordinal()]++;
			break;
		case METHOD:
			res.add(AtomicChange.makeMethodChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_METHOD.ordinal()]++;
			// type is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(2),
					getChildFromFullName(f.params.get(2)),
					getParentFromFullName(f.params.get(2))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case FIELD:
			res.add(AtomicChange.makeFieldChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_FIELD.ordinal()]++;
			// type is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(2),
					getChildFromFullName(f.params.get(2)),
					getParentFromFullName(f.params.get(2))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case RETURN:
			res.add(AtomicChange.makeReturnsChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_RETURN.ordinal()]++;
			// method is also modified
			res.add(AtomicChange.makeMethodChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_METHOD.ordinal()]++;
			// type is also modified
			res.add(AtomicChange.makeTypeChange(
					'M',
					getParentFromFullName(f.params.get(0)),
					getChildFromFullName(getParentFromFullName(f.params.get(0))),
					getParentFromFullName(getParentFromFullName(f.params.get(0)))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case FIELDOFTYPE:
			res.add(AtomicChange.makeFieldTypeChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_FIELDOFTYPE.ordinal()]++;
			// field is also modified
			res.add(AtomicChange.makeFieldChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_FIELD.ordinal()]++;
			// type is also modified
			res.add(AtomicChange.makeTypeChange(
					'M',
					getParentFromFullName(f.params.get(0)),
					getChildFromFullName(getParentFromFullName(f.params.get(0))),
					getParentFromFullName(getParentFromFullName(f.params.get(0)))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case TYPEINTYPE:
			res.add(AtomicChange.makeTypeInTypeChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPEINTYPE.ordinal()]++;
			// child types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			// parent types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(1),
					getChildFromFullName(f.params.get(1)),
					getParentFromFullName(f.params.get(1))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case SUBTYPE:
			res.add(AtomicChange.makeSubtypeChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_SUBTYPE.ordinal()]++;
			// parent types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			// child types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(1),
					getChildFromFullName(f.params.get(1)),
					getParentFromFullName(f.params.get(1))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case IMPLEMENTS:
			res.add(AtomicChange.makeImplementsChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_IMPLEMENTS.ordinal()]++;
			// parent types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			// child types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(1),
					getChildFromFullName(f.params.get(1)),
					getParentFromFullName(f.params.get(1))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case EXTENDS:
			res.add(AtomicChange.makeExtendsChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_EXTENDS.ordinal()]++;
			// parent types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(0),
					getChildFromFullName(f.params.get(0)),
					getParentFromFullName(f.params.get(0))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			// child types is also modified
			res.add(AtomicChange.makeTypeChange('M', f.params.get(1),
					getChildFromFullName(f.params.get(1)),
					getParentFromFullName(f.params.get(1))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case INHERITEDFIELD:
			res.add(AtomicChange.makeInheritedFieldChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_INHERITEDFIELD
					.ordinal()]++;
			// child type is also modified (parent is modified elsewhere)
			res.add(AtomicChange.makeTypeChange('M', f.params.get(2),
					getChildFromFullName(f.params.get(2)),
					getParentFromFullName(f.params.get(2))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case INHERITEDMETHOD:
			res.add(AtomicChange.makeInheritedMethodChange(typ,
					f.params.get(0), f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_INHERITEDMETHOD
					.ordinal()]++;
			// child type is also modified (parent is modified elsewhere)
			res.add(AtomicChange.makeTypeChange('M', f.params.get(2),
					getChildFromFullName(f.params.get(2)),
					getParentFromFullName(f.params.get(2))));
			res.changecount[AtomicChange.ChangeTypes.ADD_TYPE.ordinal()]++;
			break;
		case ACCESSES:
			res.add(AtomicChange.makeAccessesChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_ACCESSES.ordinal()]++;
			break;
		case CALLS:
			res.add(AtomicChange.makeCallsChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_CALLS.ordinal()]++;
			break;

		case METHODBODY:
			res.add(AtomicChange.makeMethodBodyChange(typ, f.params.get(0),
					f.params.get(1)));// Niki's edit
			res.changecount[AtomicChange.ChangeTypes.ADD_METHODBODY.ordinal()]++; // Niki's
																					// edit
			// TODO: what else is modified?
			break;
		case METHODSIGNATURE:
			res.add(AtomicChange.makeMethodArgsChange(f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.CHANGE_METHODSIGNATURE
					.ordinal()]++;
			// TODO: what else is modified?
			break;
		case CONDITIONAL:
			res.add(AtomicChange.makeConditionalChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2), f.params.get(3)));
			res.changecount[AtomicChange.ChangeTypes.ADD_CONDITIONAL.ordinal()]++;
			break;
		case PARAMETER:
			res.add(AtomicChange.makeParameterChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_PARAMETER.ordinal()]++;
			break;
		case THROWN:
			res.add(AtomicChange.makeThrownExceptionChange(typ,
					f.params.get(0), f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_THROWN.ordinal()]++;
			break;
		case GETTER:
			res.add(AtomicChange.makeGetterChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_GETTER.ordinal()]++;
			break;
		case SETTER:
			res.add(AtomicChange.makeSetterChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_SETTER.ordinal()]++;
			break;
		case METHODMODIFIER:
			res.add(AtomicChange.makeMethodModifierChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_METHODMODIFIER
					.ordinal()]++;
			break;
		case FIELDMODIFIER:
			res.add(AtomicChange.makeFieldModifierChange(typ, f.params.get(0),
					f.params.get(1)));
			res.changecount[AtomicChange.ChangeTypes.ADD_FIELDMODIFIER
					.ordinal()]++;
			break;
		case CAST:
			res.add(AtomicChange.makeCastChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2)));
			res.changecount[AtomicChange.ChangeTypes.ADD_CAST.ordinal()]++;
			break;
		case TRYCATCH:
			res.add(AtomicChange.makeTryCatchChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2), f.params.get(3)));
			res.changecount[AtomicChange.ChangeTypes.ADD_TRYCATCH.ordinal()]++;
			break;
		case LOCALVAR:
			res.add(AtomicChange.makeLocalVarChange(typ, f.params.get(0),
					f.params.get(1), f.params.get(2), f.params.get(3)));
			res.changecount[AtomicChange.ChangeTypes.ADD_LOCALVAR.ordinal()]++;
			break;
		default:
			// catastrophic show-stopping disaster!
			break;
		}
	}

	// this is the new factbase, input is old factbase
	public ChangeSet diff(FactBase oldfacts) {

		ChangeSet res = new ChangeSet();
		FactBase added = new FactBase(this);
		added.removeAll(oldfacts);
		FactBase deleted = new FactBase(oldfacts);
		deleted.removeAll(this);
		// Set<Fact> addedMethodDeclarations = new HashSet<Fact>();
		Set<Fact> before_parameters = new HashSet<Fact>();
		Set<Fact> after_parameters = new HashSet<Fact>();

		// scan for additive changes (and modifications)
		for (Fact f : added) {
			if (f.type == Fact.FactTypes.PARAMETER) {
				before_parameters.add(f);
			} else {
				makeChangeFromFact(res, f, 'A');
			}
		}
		// scan for depletive changes
		for (Fact f : deleted) {
			if (f.type == Fact.FactTypes.PARAMETER) {
				after_parameters.add(f);
			} else {
				makeChangeFromFact(res, f, 'D');
			}
		}

		for (Fact f : before_parameters) {
			// Loop thru deleted; find matching method decl by name; compare
			// params - actually diff = add/delete param fact.
			for (Fact fd : after_parameters) {
				// Compare method names
				if (!fd.params.get(0).equals(f.params.get(0))) {
					continue;
				}
				// Found a match, compare parameter lists
				String[] new_params = f.params.get(1).split(",");
				String[] old_params = fd.params.get(1).split(",");

				Set<String> tempNew = new HashSet<String>();
				Set<String> tempOld = new HashSet<String>();

				for (String s : new_params)
					if (!s.equals(""))
						tempNew.add(s);
				for (String s : old_params)
					if (!s.equals(""))
						tempOld.add(s);

				if (tempNew.equals(tempOld))
					break;

				Set<String> addedParams = new HashSet<String>(tempNew);
				Set<String> deletedParams = new HashSet<String>(tempOld);

				addedParams.removeAll(tempOld);
				deletedParams.removeAll(tempNew);

				for (String add : addedParams) {
					Fact fNew = Fact.makeParameterFact(f.params.get(0),
							f.params.get(1), add);
					makeChangeFromFact(res, fNew, 'A');
				}

				for (String rem : deletedParams) {
					Fact fNew = Fact.makeParameterFact(f.params.get(0),
							f.params.get(1), rem);
					makeChangeFromFact(res, fNew, 'D');
				}
			}
		}

		res.normalize();
		return res;
	}

	// generate derived facts
	public void deriveFacts() {
		// deriveRemoveExternalMethodsCalls();
		deriveInheritedMembers();
		deriveDefaultConstructors();
	}

	public void deriveRemoveExternalMethodsCalls() {
		System.out.print("Deriving remove external methods... ");

		// grab all bad method call facts
		FactBase badMethodCalls = new FactBase();
		for (Fact f : this) {
			if (f.type != Fact.FactTypes.CALLS)
				continue;
			if (f.params.get(1).startsWith("junit.framework"))
				badMethodCalls.add(f);
		}
		// remove method call facts
		this.removeAll(badMethodCalls);

		System.out.println("OK");
	}

	public void deriveDefaultConstructors() {
		System.out.print("Deriving default constructors... ");

		// for all classes without a constructor, create a default constructor
		// and return

		// Optimize by using small sets instead of whole factbase
		Set<Fact> typefacts = new HashSet<Fact>();
		Set<Fact> methodfacts = new HashSet<Fact>();
		for (Fact f : this) {
			if (f.type == Fact.FactTypes.TYPE)
				typefacts.add(f);
			else if (f.type == Fact.FactTypes.METHOD)
				methodfacts.add(f);
		}

		// iterate over types
		for (Fact f : typefacts) {
			boolean found = false;
			if (f.params.get(3).equals(Fact.INTERFACE))
				continue;
			for (Fact f2 : methodfacts) {
				if (f2.params.get(1).startsWith("<init>(")
						&& f2.params.get(2).equals(f.params.get(0))) {
					found = true;
					break;
				}
			}
			if (!found) {
				Fact constfact = Fact
						.makeMethodFact(f.params.get(0) + "#<init>()",
								"<init>()", f.params.get(0), Fact.PUBLIC);
				Fact returnfact = Fact.makeReturnsFact(f.params.get(0)
						+ "#<init>()", "void");
				this.add(constfact);
				this.add(returnfact);
			}
		}
		System.out.println("OK");
	}

	public void deriveInheritedMembers() {

		// iterate through factbase to find the following:
		// 1. method(m, ms, t) & subtype(t, t2) & !method(m2, ms, t2) ~~>
		// inheritedmethod(ms, t, t2)
		// 2. inheritedmethod(ms, t, t2) & subtype(t2, t3) & !method(m3, ms, t3)
		// ~~> inheritedmethod(ms, t, t3)
		// 3. field(f, fs, t) & subtype(t, t2) & [!field(f2, fs, t2)] ~~>
		// inheritedfield(fs, t, t2)
		// 4. inheritedfield(fs, t, t2) & subtype(t2, t3) & [!field(f3, fs, t3)]
		// ~~> inheritedfield(fs, t, t3)
		System.out.println("Deriving inheritance members... ");

		// Optimize by using small sets instead of whole factbase
		Set<Fact> subtypefacts = new HashSet<Fact>();
		Set<Fact> methodfacts = new HashSet<Fact>();
		Set<Fact> fieldfacts = new HashSet<Fact>();
		Set<Fact> inheritedmethodfacts = new HashSet<Fact>();
		Set<Fact> inheritedfieldfacts = new HashSet<Fact>();
		for (Fact f : this) {
			if (f.type == Fact.FactTypes.SUBTYPE)
				subtypefacts.add(f);
			else if (f.type == Fact.FactTypes.METHOD)
				methodfacts.add(f);
			else if (f.type == Fact.FactTypes.FIELD)
				fieldfacts.add(f);
		}

		Queue<Fact> worklist = new LinkedList<Fact>();
		// check 1. method(m, ms, t, pb|pt) & subtype(t, t2) & !method(m2, ms,
		// t2) ~~> inheritedmethod(ms, t, t2)
		System.out.print("  Checking for directly inherited methods... ");
		for (Fact a1 : methodfacts) {
			if (a1.params.get(3) == Fact.PRIVATE)
				continue;
			if (a1.params.get(1).startsWith("<init>("))
				continue;
			for (Fact a2 : subtypefacts) {
				if (!a2.params.get(0).equals(a1.params.get(2)))
					continue;
				Fact b1 = Fact.makeMethodFact("*", a1.params.get(1),
						a2.params.get(1), "*");
				if (methodfacts.contains(b1))
					continue;
				// success!
				Fact newfact = Fact.makeInheritedMethodFact(a1.params.get(1),
						a2.params.get(0), a2.params.get(1));
				inheritedmethodfacts.add(newfact);
			}
		}
		System.out.println("OK");
		// check 3. field(f, fs, t, pb|pt) & subtype(t, t2) & !field(f2, fs, t2)
		// ~~> inheritedfield(fs, t, t2)
		System.out.print("  Checking for directly inherited fields... ");
		for (Fact a1 : fieldfacts) {
			if (a1.params.get(3) == Fact.PRIVATE)
				continue;
			for (Fact a2 : subtypefacts) {
				if (!a2.params.get(0).equals(a1.params.get(2)))
					continue;
				// Fact b1 = Fact.makeFieldFact("*", a1.params.get(1),
				// a2.params.get(1), "*");
				// if (fieldfacts.contains(b1)) continue;
				// success!
				Fact newfact = Fact.makeInheritedFieldFact(a1.params.get(1),
						a2.params.get(0), a2.params.get(1));
				inheritedfieldfacts.add(newfact);
			}
		}
		System.out.println("OK");
		// check 2. inheritedmethod(ms, t, t2) & subtype(t2, t3) & !method(*,
		// ms, t3, *) & !inheritedmethod(ms, t, t3) ~~> inheritedmethod(ms, t,
		// t3)
		System.out.print("  Checking for indirectly inherited methods... ");
		worklist.clear();
		for (Fact f : inheritedmethodfacts) {
			worklist.add(f);
		}
		while (worklist.size() > 0) {
			Fact a1 = worklist.poll();
			for (Fact a2 : subtypefacts) {
				if (!a2.params.get(0).equals(a1.params.get(2)))
					continue;
				Fact b1 = Fact.makeMethodFact("*", a1.params.get(0),
						a2.params.get(1), "*");
				if (methodfacts.contains(b1))
					continue;
				Fact b2 = Fact.makeInheritedMethodFact(a1.params.get(0),
						a1.params.get(1), a2.params.get(1));
				if (inheritedmethodfacts.contains(b2))
					continue;
				// success!
				Fact newfact = Fact.makeInheritedMethodFact(a1.params.get(0),
						a1.params.get(1), a2.params.get(1));
				worklist.add(newfact);
				inheritedmethodfacts.add(newfact);
			}
		}
		this.addAll(inheritedmethodfacts);
		System.out.println("OK");
		// check 4. inheritedfield(fs, t, t2) & subtype(t2, t3) & !field(*, fs,
		// t3, *) & !inheritedfield(fs, t, t3) ~~> inheritedfield(fs, t, t3)
		System.out.print("  Checking for indirectly inherited fields... ");
		worklist.clear();
		for (Fact f : inheritedfieldfacts) {
			worklist.add(f);
		}
		while (worklist.size() > 0) {
			Fact a1 = worklist.poll();
			for (Fact a2 : subtypefacts) {
				if (!a2.params.get(0).equals(a1.params.get(2)))
					continue;
				Fact b1 = Fact.makeMethodFact("*", a1.params.get(0),
						a2.params.get(1), "*");
				if (methodfacts.contains(b1))
					continue;
				// Fact b2 = Fact.makeInheritedFieldFact(a1.params.get(0),
				// a1.params.get(1), a2.params.get(1));
				// if (inheritedfieldfacts.contains(b2)) continue;
				// success!
				Fact newfact = Fact.makeInheritedFieldFact(a1.params.get(0),
						a1.params.get(1), a2.params.get(1));
				worklist.add(newfact);
				inheritedfieldfacts.add(newfact);
			}
		}
		this.addAll(inheritedfieldfacts);
		System.out.println("OK");
	}

}

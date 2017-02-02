package tyRuBa.modes;

import java.util.HashMap;
import java.util.Map;

public class CompositeType extends BoundaryType {

	private TypeConstructor typeConst = null;
	boolean strict = false;
	private TupleType args;

	public CompositeType(TypeConstructor typeConst, boolean strict, TupleType args) {
		this.typeConst = typeConst;
		this.strict = strict;
		this.args = args;
	}
	
	public TypeConstructor getTypeConstructor() {
		return typeConst;
	}

	public int hashCode() {
		return getTypeConstructor().hashCode() + 13 * (args.hashCode());
	}

	public void checkEqualTypes(Type other, boolean grow) throws TypeModeError {
		if (other instanceof TVar || other instanceof GrowableType) {
			other.checkEqualTypes(this, grow);
		} else {
			check(other instanceof CompositeType, this, other);
			CompositeType cother = (CompositeType) other;
			check(getTypeConstructor().equals(cother.getTypeConstructor()), this, other);
			try {
				args.checkEqualTypes(cother.args, grow);
			} catch (TypeModeError e) {
				throw new TypeModeError(e, this);
			}
			boolean newStrict = this.strict || cother.strict;
			cother.strict = newStrict;
			this.strict = newStrict;
		}
	}
	
	public boolean isSubTypeOf(Type other, Map renamings) {
		if (other instanceof TVar)
			other = ((TVar)other).getContents();
		if (other == null) // Was a free TVar
			return false;
		if (! (other instanceof CompositeType)) {
			return false;
		} else {
			CompositeType declared = (CompositeType) other;
			TypeConstructor declaredTypeConst = declared.getTypeConstructor();
			if (isStrict()) {
				return typeConst.equals(declaredTypeConst)
					&& declared.isStrict()
					&& args.isSubTypeOf(declared.args, renamings);
			} else if (typeConst.equals(declaredTypeConst)) {
				return args.isSubTypeOf(declared.args, renamings);
			} else if (declaredTypeConst.isSuperTypeOf(typeConst)) {
				Map params = new HashMap();
				for (int i = 0; i < typeConst.getTypeArity(); i++) {
					String currName = typeConst.getParameterName(i);
					params.put(currName, args.getParamType(i, typeConst));
				}
				for (int i = 0; i < declaredTypeConst.getTypeArity(); i++) {
					String currName = declaredTypeConst.getParameterName(i);
					Type paramType = (Type) params.get(currName);
					if (paramType != null) {
						Type declaredType = declared.args.getParamType(i, declaredTypeConst);
						if (declaredType == null) {
							return false;
						} else if (! paramType.isSubTypeOf(declaredType, renamings)) {
							return false;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean equals(Object other) {
		if (! (other instanceof CompositeType)) {
			return false;
		} else {
			CompositeType cother = (CompositeType) other;
			return getTypeConstructor().equals(cother.getTypeConstructor())
				&& isStrict() == cother.isStrict()
				&& args.equals(cother.args);
		}
	}

	public String toString() {
		String constName = typeConst.getName() + args;
		if (isStrict()) {
			return "=" + constName;
		} else {
			return constName;
		}
	}

	public boolean isFreeFor(TVar var) {
		return args.isFreeFor(var);
	}

	public Type clone(Map tfact) {
		return new CompositeType(typeConst, strict, (TupleType)args.clone(tfact));
	}

	public String getName() {
		return getTypeConstructor().getName();
	}

	public TupleType getArgs() {
		return args;
	}

	public Type union(Type other) throws TypeModeError {
		if (other instanceof TVar || other instanceof GrowableType) {
			return other.union(this);
		} else {
			check(other instanceof CompositeType, this, other);
			CompositeType cother = (CompositeType) other;
			TypeConstructor otherTypeConst = cother.typeConst;
			if (this.equals(other)) {
				return this;
			} else if (typeConst.equals(otherTypeConst)) {
				TupleType resultArg = (TupleType) args.union(cother.args);
				if (strict || cother.strict) {
					return typeConst.applyStrict(resultArg, false);
				} else {
					return typeConst.apply(resultArg, false);
				}
			} else if (otherTypeConst.isSuperTypeOf(typeConst)) {
				check(!isStrict(), this, other);
				Map params = new HashMap();
				for (int i = 0; i < typeConst.getTypeArity(); i++) {
					params.put(typeConst.getParameterName(i), args.get(i));
				}
				TupleType resultArg = Factory.makeTupleType();
				for (int i = 0; i < cother.typeConst.getTypeArity(); i++) {
					String currName = cother.typeConst.getParameterName(i);
					Type paramValue = (Type) params.get(currName);
					if (paramValue != null) {
						resultArg.add(paramValue.union(cother.args.get(i)));
					} else {
						resultArg.add(cother.args.get(i));
					}
				}
				if (cother.strict) {
					return otherTypeConst.applyStrict(resultArg, false);
				} else {
					return otherTypeConst.apply(resultArg, false);
				}
			} else if (typeConst.isSuperTypeOf(otherTypeConst)) {
				return cother.intersect(this);
			} else {
				check(!isStrict(), this, other);
				check(!cother.isStrict(), this, other);
				TypeConstructor superTypeConst = typeConst.lowerBound(otherTypeConst);
				Map params = new HashMap();
				for (int i = 0; i < typeConst.getTypeArity(); i++) {
					params.put(typeConst.getParameterName(i), args.get(i));
				}
				for (int i = 0; i < otherTypeConst.getTypeArity(); i++) {
					String currName = otherTypeConst.getParameterName(i);
					Type paramValue = (Type) params.get(currName);
					if (paramValue == null) {
						params.put(currName, cother.args.get(i));
					} else {
						params.put(currName, paramValue.union(cother.args.get(i)));
					}
				}
				TupleType resultArg = Factory.makeTupleType();
				for (int i = 0; i < superTypeConst.getTypeArity(); i++) {
					String currName = superTypeConst.getParameterName(i);
					resultArg.add((Type) params.get(currName));
				}
				return superTypeConst.apply(resultArg, false);
			}
		}
	}

	public Type intersect(Type other) throws TypeModeError {
		if (other instanceof TVar || other instanceof GrowableType) {
			return other.intersect(this);
		} else {
			check(other instanceof CompositeType, this, other);
			CompositeType cother = (CompositeType) other;
			TypeConstructor otherTypeConst = cother.typeConst;
			if (this.equals(other)) {
				return this;
			} else if (typeConst.equals(otherTypeConst)) {
				TupleType resultArg = (TupleType) args.intersect(cother.args);
				if (strict || cother.strict) {
					return typeConst.applyStrict(resultArg, false);
				} else {
					return typeConst.apply(resultArg, false);
				}
			} else if (typeConst.isSuperTypeOf(otherTypeConst)) {
				check(! cother.isStrict(), this, other);
				Map params = new HashMap();
				for (int i = 0; i < typeConst.getTypeArity(); i++) {
					params.put(typeConst.getParameterName(i), args.get(i));
				}
				TupleType resultArg = Factory.makeTupleType();
				for (int i = 0; i < cother.typeConst.getTypeArity(); i++) {
					String currName = cother.typeConst.getParameterName(i);
					Type paramValue = (Type) params.get(currName);
					check(paramValue != null, this, other);
					resultArg.add(paramValue.intersect(cother.args.get(i)));
				}
				return cother.typeConst.apply(resultArg, false);
			} else if (otherTypeConst.isSuperTypeOf(typeConst)) {
				return cother.intersect(this);
			} else {
				throw new TypeModeError("Incompatible types: " + this + ", " + other);
			}
		}
	}
	
	public boolean hasOverlapWith(Type other) {
		if (other instanceof TVar || other instanceof GrowableType) {
			return other.hasOverlapWith(this);
		} else if (! (other instanceof CompositeType)) {
			return false;
		} else {
			CompositeType cother = (CompositeType) other;
			TypeConstructor otherTypeConst = cother.getTypeConstructor();
			if (typeConst.equals(otherTypeConst)) {
				return args.hasOverlapWith(cother.args);
			} else if (typeConst.isSuperTypeOf(otherTypeConst)) {
				Map params = new HashMap();
				for (int i = 0; i < typeConst.getTypeArity(); i++) {
					params.put(typeConst.getParameterName(i), args.get(i));
				}
				for (int i = 0; i < otherTypeConst.getTypeArity(); i++) {
					Type paramValue = (Type) params.get(otherTypeConst.getParameterName(i));
					if (paramValue != null) {
						if (cother.args.get(i).hasOverlapWith(paramValue)) {
							return true;
						}
					}
				}
				return false;
			} else if (otherTypeConst.isSuperTypeOf(typeConst)) {
				return other.hasOverlapWith(this);
			} else {
				return false;
			}
		}
	}
	
	boolean isStrict() {
		return strict;
	}

	public Type copyStrictPart() {
		if (isStrict()) {
			return typeConst.applyStrict((TupleType)args.copyStrictPart(), false);
		} else {
			TypeConstructor resultTypeConst =
				typeConst.getSuperestTypeConstructor();
			Map params = new HashMap();
			for (int i = 0; i < typeConst.getTypeArity(); i++) {
				params.put(typeConst.getParameterName(i), args.get(i).copyStrictPart());
			}
			TupleType resultArg = Factory.makeTupleType();
			for (int i = 0; i < resultTypeConst.getTypeArity(); i++) {
				String currName = resultTypeConst.getParameterName(i);
				Type paramType = (Type) params.get(currName);
				if (paramType == null) {
					resultArg.add(Factory.makeTVar(currName));
				} else {
					resultArg.add(paramType);
				}
			}
			return resultTypeConst.apply(resultArg, false);
		}
	}
	
	public void makeStrict() {
		this.strict = true;
	}
	
	public void addSubType(Type subType) throws TypeModeError {
		if (! (subType instanceof CompositeType)) {
			throw new TypeModeError(subType + " is an illegal subtype for " + this);
		} else {
			typeConst.addSubTypeConst(((CompositeType) subType).getTypeConstructor());
		}
	}
	
	public void setRepresentationType(Type repBy) {
		typeConst.setRepresentationType(repBy);
	}

//	Type lowerBound(Type other) throws TypeModeError {
//		if (other instanceof GrowableType || other instanceof TVar) {
//			return other.lowerBound(this);
//		} else {
//			check(other instanceof CompositeType, this, other);
//			TypeConstructor otherTypeConst = ((CompositeType)other).getTypeConstructor();
//			TupleType otherArg = ((CompositeType) other).getArgs();
//			int thisArity = typeConst.getTypeArity();
//			int otherArity = otherTypeConst.getTypeArity();
//			if (typeConst.equals(otherTypeConst)) {
//				return typeConst.apply((TupleType)args.lowerBound(otherArg), false);
//			} else if (typeConst.isSuperTypeOf(otherTypeConst)) {
//				Map params = new HashMap();
//				Type paramValue;
//				String currName;
//				TupleType resultArg = Factory.makeTupleType();
//				for (int i = 0; i < otherArity; i++) {
//					currName = otherTypeConst.getParameterName(i);
//					params.put(currName, otherArg.getParamType(i, otherTypeConst));	
//				}
//				for (int i = 0; i < thisArity; i++) {
//					currName = typeConst.getParameterName(i);
//					paramValue = (Type) params.get(currName);
//					Type currType = args.getParamType(i, typeConst);
//					if (paramValue == null) {
//						resultArg.add(currType);					
//					} else if (currType != null) {
//						resultArg.add(paramValue.lowerBound(currType));
//					}
//				}
//                if (((CompositeType)other).isStrict()) {
//                    throw new TypeModeError("Incompatible types: "+this + " lowerbound "+other);
//                }
//				return typeConst.apply(resultArg, false);
//			} else if (otherTypeConst.isSuperTypeOf(typeConst)) {
//				return other.lowerBound(this);
//			} else {
//				Map params = new HashMap();
//				Type paramValue;
//				String currName;
//				for (int i = 0; i < thisArity; i++) {
//					currName = typeConst.getParameterName(i);
//					params.put(currName, args.getParamType(i, typeConst));
//				}
//				for (int i = 0; i < otherArity; i++) {
//					currName = otherTypeConst.getParameterName(i);
//					Type currType = otherArg.getParamType(i, otherTypeConst);
//					paramValue = (Type) params.get(currName);
//					if (paramValue == null) {
//						params.put(currName, currType);
//					} else if (currType != null) {
//						params.put(currName, paramValue.lowerBound(currType));
//					}
//				}
//				TypeConstructor thisSuperConst = typeConst.getSuperTypeConstructor();
//				check(thisSuperConst != null, this, other);
//				TupleType resultArg = Factory.makeTupleType();
//				for (int i = 0; i < thisSuperConst.getTypeArity(); i++) {
//					currName = thisSuperConst.getParameterName(i);
//					resultArg.add((Type) params.get(currName));
//				}
//
//				return thisSuperConst.apply(resultArg, false)
//					.lowerBound(other);
//			}
//        }
//	}

	public Type getParamType(String currName, Type repAs) {
		if (repAs instanceof TVar) {
			if (currName.equals(((TVar)repAs).getName())) {
				return this;
			} else {
				return null;
			}
		} else if (! (repAs instanceof CompositeType)) {
			return null;
		} else {
			CompositeType compositeRepAs = (CompositeType) repAs;
			if (compositeRepAs.getTypeConstructor().equals(typeConst)) {
				return args.getParamType(currName, compositeRepAs.args);
			} else {
				return null;
			}
		}
	}
	
    public Class javaEquivalent() throws TypeModeError {
    		return typeConst.javaEquivalent();
    }
    
    public boolean isJavaType() {
        return getTypeConstructor().isJavaTypeConstructor();
    }
}

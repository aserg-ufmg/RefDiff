package refdiff.parsers.js;

import java.util.function.Function;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

class JsValue {
	
	private final Object value;
	private final ScriptObjectMirror o;
	private final Function<Object, String> toJsonFunction;
	
	public JsValue(Object value, Function<Object, String> toJsonFunction) {
		this.value = value;
		if (value instanceof ScriptObjectMirror) {
			this.o = (ScriptObjectMirror) value;
		} else {
			this.o = null;
		}
		this.toJsonFunction = toJsonFunction;
	}
	
	public boolean has(String member) {
		return o != null && o.hasMember(member);
	}
	
	public JsValue get(String member) {
		if (o != null) {
			if (o.hasMember(member)) {
				return new JsValue(o.getMember(member), toJsonFunction);
			} else {
				throw error("Object has no member '" + member + "'");
			}
		} else {
			throw error("Not an object");
		}
	}
	
	public JsValue get(int pos) {
		if (o != null) {
			if (o.isArray()) {
				return new JsValue(o.getSlot(pos), toJsonFunction);
			} else {
				throw error("Not an array");
			}
		} else {
			throw error("Not an array");
		}
	}
	
	public String asString() {
		if (value instanceof String) {
			return (String) value;
		} else {
			throw error("Not a string");
		}
	}
	
	public int asInt() {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			throw error("Not a number");
		}
	}
	
	public String[] getOwnKeys() {
		if (o != null) {
			return o.getOwnKeys(true);
		} else {
			throw error("Not an object");
		}
	}
	
	public boolean isObject() {
		return o != null && !o.isArray();
	}
	
	public boolean isArray() {
		return o != null && o.isArray();
	}
	
	public int size() {
		if (o != null) {
			return o.size();
		} else {
			throw error("Not an array");
		}
	}
	
	@Override
	public String toString() {
		return value == null ? "null" : toJsonFunction.apply(value);
	}
	
	private RuntimeException error(String string) {
		return new RuntimeException(string + ":\n" + toString());
	}
	
}

package refdiff.parsers.js;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

class JsValueV8 implements Closeable {
	
	private final Object value;
	private final V8Object o;
	private final boolean defined;
	private final Function<Object, String> toJsonFunction;
	private final List<JsValueV8> children = new ArrayList<>();
	
	public JsValueV8(Object value, Function<Object, String> toJsonFunction) {
		this.value = value;
		if (value instanceof V8Object) {
			V8Object v8Object = (V8Object) value;
			if (!v8Object.isUndefined()) {
				this.o = v8Object;
				this.defined = true;
			} else {
				this.o = null;
				this.defined = false;
			}
		} else {
			this.defined = value != null;
			this.o = null;
		}
		this.toJsonFunction = toJsonFunction;
	}
	
	public boolean has(String member) {
		return o != null && o.contains(member) && get(member).isDefined();
	}
	
	public JsValueV8 get(String member) {
		if (o != null) {
			if (o.contains(member)) {
				return addChild(new JsValueV8(o.get(member), toJsonFunction));
			} else {
				throw error("Object has no member '" + member + "'");
			}
		} else {
			throw error("Not an object");
		}
	}
	
	public JsValueV8 get(int pos) {
		if (o != null) {
			if (o instanceof V8Array) {
				V8Array array = (V8Array) o;
				return addChild(new JsValueV8(array.get(pos), toJsonFunction));
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
			return o.getKeys();
		} else {
			throw error("Not an object");
		}
	}
	
	public boolean isObject() {
		return o != null && !(o instanceof V8Array);
	}
	
	public boolean isArray() {
		return o instanceof V8Array;
	}
	
	public int size() {
		if (o instanceof V8Array) {
			return ((V8Array) o).length();
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
	
	private JsValueV8 addChild(JsValueV8 value) {
		if (value.o != null) {
			this.children.add(value);
		}
		return value;
	}
	
	@Override
	public void close() throws IOException {
		for (JsValueV8 child : children) {
			child.close();
		}
		if (o != null) {
			o.release();
		}
	}
	
	public boolean isDefined() {
		return defined;
	}
	
}

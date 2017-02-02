/**
 * RuleBase stores a collection of Logic inference rules.
 */

package tyRuBa.engine;

import java.util.HashMap;
import java.util.Map;

import serp.util.SoftValueMap;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.util.Action;
import tyRuBa.util.DelayedElementSource;
import tyRuBa.util.ElementCollector;
import tyRuBa.util.ElementSetCollector;
import tyRuBa.util.ElementSource;

public class CachedRuleBase extends Compiled {
	
	Compiled compiledContents;
	
	SemiDetCachedRuleBase mySemiDetCompanion = null;

	/** Cache of already performed (simple)queries. */
	private Map cache = null;

	public CachedRuleBase(Compiled compiledRuleBase) {
		super(compiledRuleBase.getMode());
		compiledContents = compiledRuleBase;
		initCache();
	}

//	public void update() {
//		clearCache();
//		super.update();
//	}

	private void initCache() {
		cache = RuleBase.softCache ? (Map) new SoftValueMap() : new HashMap();
	}

	/** Unification, check cache first */
	public ElementSource runNonDet(Object input, final RBContext context) {
		final RBTuple other = (RBTuple)input;
		FormKey k = new FormKey(other);
		CacheEntry entry = (CacheEntry) cache.get(k);
		ElementCollector cachedResult;
		if (entry == null
			|| (cachedResult = entry.getCachedResult()) == null) {
			/* Not found in the cache */
			ElementCollector result = new ElementSetCollector();
			if (!RuleBase.silent)
				if (entry == null)
					System.err.print(".");
				else
					System.err.print("@");
			entry = new CacheEntry(k, result);
			cache.put(k, entry);
//			result.setSource(contents.unify(other, context));
			result.setSource(new DelayedElementSource() {
				public ElementSource produce() {
					return compiledContents.runNonDet(other, context);
				}
				public String produceString() {
					return other.toString();
				}
			});
//			System.err.println("Precached : " + other);
//			System.err.println(" hashcode = " + k.hashCode());
//			System.err.println("   Cached : " + other);
			return result.elements();
		} else { /* Found in the Cache */
			if (!RuleBase.silent)
				System.err.print("H");
//			System.err.println("     f0 = " + f0);
//			System.err.println("  query : " + other);
			final Frame call = new Frame();
			if (other.sameForm(entry.key.theKey, call, new Frame())) {
//				System.err.println("    cache : " + entry.key.theKey);
//				System.err.println("callframe = " + call);
				/* previous call to sameform determines call frame! */
				return cachedResult.elements().map(new Action() {
					public Object compute(Object f) {
//						System.err.println("cache frame = " + f);
						Frame callres = call.callResult((Frame) f);
//						System.err.println(" callresult = " + callres);
						return callres;
					}
				});
			} else {
				//System.err.println("not sameform: " + other + " & " + entry.key.theKey);
				throw new Error("Should never happen");
			}
		}
	}

	/* To make better use of memory use this "Soft" 
	   CacheEntry instead of CacheEntry seems to
	   have only small impact on runtime performance! 
	private class CacheEntry extends java.lang.ref.SoftReference {
		FormKey key;
		CacheEntry(FormKey k,ElementCollector r) { super(r); key = k; }
		ElementCollector getCachedResult() { return (ElementCollector)get(); }
	}
	*/

	private class CacheEntry {
		FormKey key;
		ElementCollector result;
		CacheEntry(FormKey k, ElementCollector r) {
			result = r;
			key = k;
		}
		ElementCollector getCachedResult() {
			return result;
		}
	}

	public SemiDetCompiled first() {
		if (mySemiDetCompanion == null) {
			mySemiDetCompanion = new SemiDetCachedRuleBase(compiledContents.first());
		}
		return mySemiDetCompanion;
	}

	public String toString() {
		return "CACHED RULEBASE(...)";
	}
}
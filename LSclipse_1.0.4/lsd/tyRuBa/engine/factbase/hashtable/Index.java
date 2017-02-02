/*
 * Created on Jun 11, 2004
 */
package tyRuBa.engine.factbase.hashtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.factbase.NamePersistenceManager;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.PredicateMode;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;
import tyRuBa.util.pager.Location;
import tyRuBa.util.pager.Pager;
import tyRuBa.util.pager.Pager.Resource;
import tyRuBa.util.pager.Pager.ResourceId;

/**
 * An index allows facts to be efficiently looked up for a specific predicate
 * mode.
 * @category FactBase
 * @author riecken
 */
public final class Index {

    /**
     * Wraps a HashMap in a Pager resource. This wrapper also knows how to clean
     * itself.
     */
    static class HashMapResource extends HashMap implements Pager.Resource {

        private long myLastCleanTime = System.currentTimeMillis();

        public boolean isClean(ValidatorManager vm) {
            long lastDirty = vm.getLastInvalidatedTime();
            return myLastCleanTime > lastDirty;
        }

        public void clean(ValidatorManager vm) {
            myLastCleanTime = System.currentTimeMillis();
            for (Iterator iter = entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object whatIsThere = entry.getValue();
                if (whatIsThere instanceof ArrayList) {
                    ArrayList lstWhatIsThere = (ArrayList) whatIsThere;
                    for (Iterator iterator = lstWhatIsThere.iterator(); iterator.hasNext();) {
                        IndexValue element = (IndexValue) iterator.next();
                        if (!element.isValid(vm)) {
                            iterator.remove();
                        }
                    }

                    int size = lstWhatIsThere.size();
                    if (size == 0) {
                        iter.remove();
                    } else if (size == 1) {
                        entry.setValue(lstWhatIsThere.get(0));
                    }

                } else { //It's an indexvalue
                    IndexValue idxWhatIsThere = (IndexValue) whatIsThere;
                    if (!idxWhatIsThere.isValid(vm)) {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Wraps a HashSet in a Pager resource.
     */
    static class HashSetResource extends HashSet implements Pager.Resource {
    }

    /** Locations in the mode that are free. */
    private final int[] freePlaces;

    /** Locations in the mode that are bound. */
    private final int[] boundPlaces;

    /** Name of the predicate this index is for. */
    private String predicateName;

    /** Whether we wish to check for duplicates. */
    private boolean checkDet;

    /** Query engine that this index is in. */
    private QueryEngine engine;

    /** ValidatorManager to validate facts. */
    private ValidatorManager validatorManager;

    /** NamePersistenceManager to get filenames from. */
    private NamePersistenceManager nameManager;

    /** Location that this index is located at. */
    private Location storageLocation;
    
    public String toString() {
    		String result = "Index(" + predicateName + " ";
    		int arity = boundPlaces.length + freePlaces.length;
    		char[] boundMap = new char[arity];
    		for (int i = 0; i < boundMap.length; i++) {
    			boundMap[i] = 'F';
    		}
    		for (int i = 0; i < boundPlaces.length; i++) {
    			boundMap[boundPlaces[i]] = 'B';
    		}
    		return result + new String(boundMap) + ")";
    }

    /**
     * Creates a new Index
     * @param mode mode that the index is for
     * @param storageLocation location at which the index is stored
     * @param engine query engine that the index resides in
     * @param predicateName name of the predicate that the index is for.
     */
    Index(PredicateMode mode, Location storageLocation, QueryEngine engine, String predicateName) {
        this.validatorManager = engine.getFrontEndValidatorManager();
        this.engine = engine;
        this.storageLocation = storageLocation;
        this.predicateName = predicateName;
        this.nameManager = engine.getFrontendNamePersistenceManager();
        this.checkDet = (mode.getMode().isDet() || mode.getMode().isSemiDet());
        BindingList bl = mode.getParamModes();
        boundPlaces = new int[bl.getNumBound()];
        int boundPos = 0;
        freePlaces = new int[bl.getNumFree()];
        int freePos = 0;
        for (int i = 0; i < bl.size(); i++) {
            if (bl.get(i).isBound()) {
                boundPlaces[boundPos++] = i;
            } else {
                freePlaces[freePos++] = i;
            }
        }
    }

    /**
     * Creates an Index with a specific NamePersistenceManager and
     * ValidatorManager (used for fact libraries).
     */
    Index(PredicateMode mode, Location storageLocation, QueryEngine engine, String predicateName,
            NamePersistenceManager nameManager, ValidatorManager validatorManager) {
        this(mode, storageLocation, engine, predicateName);
        this.nameManager = nameManager;
        this.validatorManager = validatorManager;
    }

    /**
     * Retrieves the Pager from the QueryEngine.
     */
    private Pager getPager() {
        return engine.getFrontEndPager();
    }

    /**
     * Extracts a subset of an RBTuple into a new RBTuple.
     * @param toExtract parts of tuple to extract.
     * @param from tuple to extract from
     */
    private RBTuple extract(int[] toExtract, RBTuple from) {
        RBTerm[] extracted = new RBTerm[toExtract.length];
        for (int i = 0; i < extracted.length; i++) {
            extracted[i] = from.getSubterm(toExtract[i]);
        }
        return FrontEnd.makeTuple(extracted);
    }

    /**
     * Extract the bound parts.
     * @param goal RBTuple to extract the bound parts from.
     */
    public RBTuple extractBound(RBTuple goal) {
        return extract(boundPlaces, goal);
    }

    /**
     * Extract the free parts.
     * @param goal RBTuple to extract the free parts from.
     */
    public RBTuple extractFree(RBTuple goal) {
        return extract(freePlaces, goal);
    }

    /**
     * Adds a fact into the index.
     * @param fact fact to insert.
     */
    public void addFact(IndexValue fact) {
        RBTuple parts = fact.getParts();
        RBTuple whole_key = extractBound(parts);
        RBTuple free = extractFree(parts);

        //Special Behaviour for the all free index
        if (whole_key == RBTuple.theEmpty) {
            whole_key = free;
        }
        final Object key = whole_key.getSecond();
        final String topLevelKey = whole_key.getFirst();
        final IndexValue value = IndexValue.make(fact.getValidatorHandle(), free);

        getPager().asynchDoTask(getResourceFromKey(whole_key), new Pager.Task(true) {

            public Object doIt(Resource map_rsrc) {
                HashMapResource map = (HashMapResource) map_rsrc;

                if (map != null && !map.isClean(validatorManager)) {
                    map.clean(validatorManager);
                }

                if (map == null)
                    map = new HashMapResource(); //new resource is
                // automatically clean

                Object whatIsThere = map.get(key);
                if (whatIsThere == null) {
                    map.put(key, value);
                } else if (whatIsThere instanceof ArrayList) {
                    ArrayList lstWhatIsThere = (ArrayList) whatIsThere;
                    if (checkDet) { //SemiDet/Det uniqueness check
                        for (Iterator iter = lstWhatIsThere.iterator(); iter.hasNext();) {
                            IndexValue element = (IndexValue) iter.next();
                            if (!element.getParts().equals(value.getParts())) {
                                throw new Error(
                                        "OOPS!! More than one fact has been inserted into a Det/SemiDet predicate ("+predicateName+") present = "
                                                + element.getParts() + " ||| new = " + value.getParts() + key);
                            }
                        }
                    }
                    lstWhatIsThere.add(value);
                } else { //It's an indexValue
                    IndexValue idxWhatIsThere = (IndexValue) whatIsThere;
                    if (checkDet) { //SemiDet/Det uniqueness check
                        if (!idxWhatIsThere.getParts().equals(value.getParts())) {
                            throw new Error(
                            		"OOPS!! More than one fact has been inserted into a Det/SemiDet predicate ("+predicateName+") present = "
                                            + idxWhatIsThere.getParts() + " ||| new = " + value.getParts() + key);
                        }
                    }
                    ArrayList lstWhatIsThere = new ArrayList(2);
                    lstWhatIsThere.add(whatIsThere);
                    lstWhatIsThere.add(value);
                    map.put(key, lstWhatIsThere);
                }

                this.changedResource(map);
                return null;
            }

        });
        getPager().asynchDoTask(storageLocation.getResourceID("keys.data"), new Pager.Task(true) {

            public Object doIt(Resource rsrc) {
                HashSetResource toplevelKeys = (HashSetResource) rsrc;
                if (toplevelKeys == null)
                    toplevelKeys = new HashSetResource();
                if (toplevelKeys.add(topLevelKey))
                    changedResource(toplevelKeys);
                return null;
            }
        });
    }

    /**
     * Creates a resourceId from a RBTuple.
     */
    private ResourceId getResourceFromKey(RBTuple whole_key) {
        return storageLocation.getResourceID(nameManager.getPersistentName(whole_key.getFirst()));
    }

    /** Returns a match for a NonDet / Multi exectution. */
    public ElementSource getMatchElementSource(RBTuple inputPars) {

        //special case for all free index
        if (inputPars == RBTuple.theEmpty) {
            return convertIndexValuesToRBTuples(values());
        }

        final Object key = inputPars.getSecond();

        return (ElementSource) getPager().synchDoTask(getResourceFromKey(inputPars), new Pager.Task(false) {

            public Object doIt(Resource rsrc) {
                HashMapResource map_resource = (HashMapResource) rsrc;
                if (map_resource == null) {
                    return ElementSource.theEmpty;
                } else {
                    return convertIndexValuesToRBTuples(removeInvalids(map_resource.get(key)));
                }
            }
        });

    }

    /** Returns a match for a SemiDet / Det exectution. */
    public RBTuple getMatchSingle(RBTuple inputPars) {
        return (RBTuple) getMatchElementSource(inputPars).firstElementOrNull();
    }

    /** Returns all of the IndexValues in this index. */
    public ElementSource values() {
        return getTopLevelKeys().map(new Action() {

            public Object compute(Object arg) {
                String topkey = (String) arg;
                return getTopKeyValues(topkey);
            }

        }).flatten();
    }

    /**
     * Returns an ElementSource containing all of the top-level keys (as
     * Strings).
     */
    private ElementSource getTopLevelKeys() {
        HashSetResource topLevelKeys = (HashSetResource) getPager().synchDoTask(
                storageLocation.getResourceID("keys.data"), new Pager.Task(false) {
                    public Object doIt(Resource rsrc) {
                        HashSetResource toplevelKeys = (HashSetResource) rsrc;
                        return toplevelKeys;
                    }
                });
        return ElementSource.with(topLevelKeys.iterator());
    }

    /**
     * Returns an ElementSource containing all of the IndexValues for a given
     * top-level key.
     */
    private ElementSource getTopKeyValues(String topkey) {
        ElementSource valid_values = (ElementSource) getPager().synchDoTask(
                storageLocation.getResourceID(nameManager.getPersistentName(topkey)), new Pager.Task(false) {
                    public Object doIt(Resource rsrc) {
                        HashMapResource map = (HashMapResource) rsrc;
                        return ElementSource.with(map.values().iterator()).map(new Action() {
                            public Object compute(Object arg) {
                                return removeInvalids(arg);
                            }
                        }).flatten();
                    }
                });
        return valid_values;
    }

    /**
     * When given an ElementSource full of IndexValues, will convert it into an
     * ElementSource of the RBTuples that are in the IndexValues.
     */
    public ElementSource convertIndexValuesToRBTuples(ElementSource source) {
        return source.map(new Action() {
            public Object compute(Object arg) {
                return ((IndexValue) arg).getParts();
            }
        });
    }

    /**
     * Filters out the invalid values. "values" must either be an IndexValue or
     * an ArrayList of IndexValues.
     */
    public ElementSource removeInvalids(Object values) {
        if (values == null)
            return ElementSource.theEmpty;
        else if (values instanceof ArrayList) {
            return ElementSource.with((ArrayList) values).map(new Action() {
                public Object compute(Object arg) {
                    if (((IndexValue) arg).isValid(validatorManager))
                        return arg;
                    else
                        return null;
                }
            });
        } else {
            if (((IndexValue) values).isValid(validatorManager)) {
                return ElementSource.singleton(values);
            } else {
                return ElementSource.theEmpty;
            }
        }
    }

    /**
     * Persists the index to disk.
     */
    public void backup() {
        //this is currently done by the Pager.
    }

}
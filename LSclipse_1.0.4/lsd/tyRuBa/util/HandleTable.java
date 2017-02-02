package tyRuBa.util;

import java.io.Serializable;

/**
 * @author cburns
 *
 * Class for generating handles for Object references. Designed for highly efficient,
 * low fragmentation storage with high performance insertion and look up operations.
 * 
 * However, unlike a HashTable, there is no containsValue() type operation. Such an operation
 * would be horribly expensive, since there is no special association between an object
 * reference and its assigned indirection value. The values are assigned at the convenience
 * of the IndirectionTable, and the Objects involved have no say whatsoever.
 * 
 * The HandleTable is implemented essentially as a generational array lookup table with
 * a built in free list.
 * 
 * No indirection code will ever be returned twice by the add() operation, even when the
 * same object is added more than once. Since the codes are 64 bit, this is not likely
 * to be a problem in the foreseeable future.
 * 
 * This implementation should be capable of holding ~4 billion generations of ~4 billion
 * objects each. This should far exceed our needs.
 * 
 * In terms of memory efficiency, this implementation uses ~12 bytes per object, while
 * a HashTable uses over 30 bytes per object.
 * 
 * Note that handle 0 always maps to null. The zeroith element will never 
 * be available for use, so this mapping cannot be overriden.
 */

public class HandleTable implements Serializable {
	private static final int DEFAULTSIZE = 32; //how big a table to start
	private static final double GROWTHFACTOR = 1.33;
	
	private int freeHead;
	private int free[];
	private int update[];
	private Object references[];
	
	public HandleTable() {
		freeHead = 1;
		
		free = new int[DEFAULTSIZE];
		update = new int[DEFAULTSIZE];
		references = new Object[DEFAULTSIZE];
		
		for(int i = 1; i < DEFAULTSIZE; i++)
			free[i] = i + 1;
	}
	
	public long add(Object reference) {
		if(freeHead != references.length) { //We have room
			long indirection = ((long)update[freeHead] << 32) + (long)freeHead;
			
			references[freeHead] = reference;
			
			freeHead = free[freeHead];
			
			return indirection;
		} else {
			int tmpFree[] = new int[(int)((double)free.length * GROWTHFACTOR)];
			int tmpUpdate[] = new int[(int)((double)update.length * GROWTHFACTOR)];
			Object tmpReferences[] = new Object[(int)((double)references.length * GROWTHFACTOR)];
			
			System.arraycopy(free, 0, tmpFree, 0, free.length);
			System.arraycopy(update, 0, tmpUpdate, 0, update.length);
			System.arraycopy(references, 0, tmpReferences, 0, references.length);
			
			for(int i = free.length; i < tmpFree.length; i++)
				tmpFree[i] = i + 1;
				
			free = tmpFree;
			update = tmpUpdate;
			references = tmpReferences;
				
			return add(reference);
		}
	}
	
	public Object get(long handle) {
		int index = (int)(handle & 0xFFFFFFFF);
		if(update[index] == (handle >> 32))
			return references[index];
		else
			return null;
	}
	
	public void remove(long handle) {
		int index = (int)(handle & 0xFFFFFFFF);
		if(update[index] == (handle >> 32)) {
			free[index] = freeHead;
			update[index]++;
			references[index] = null;
			freeHead = index;
		}
	}
}

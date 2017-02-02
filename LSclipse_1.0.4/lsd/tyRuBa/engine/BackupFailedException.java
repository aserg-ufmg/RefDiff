/*
 * Created on Oct 12, 2003
 */
package tyRuBa.engine;

import java.io.IOException;

/**
 * Because JVM does not guarnatee it will run finalizers, infortunatrly,
 * TyRuBa cannot guarantee it can make a backup of your factbase. 
 * 
 * Tyruba depends on some finalizers to release locks on the objects that
 * need to be stored to disk. If they have not been released tyruba
 * will refuse to store them to disk and then a backupFailedException
 * will result.
 * 
 * If you don;t like this, complain to sun and ask them to provide
 * as part of the JVM spec some better guarantees that finalizers
 * will do anything at all.
 * 
 * In a better future version of TyRuBa, TyRuBa will try to implement
 * a backup routine that saves the factbase anyway.
 * 
 * In the mean time, you can call ElementSource.release whenever you
 * stop reading the source before it reaches the end. This will hasten
 * release of locks and thus depend much less on running finalizers.
 * 
 * However, remaining in the spirit of Sun JVM spec. Calling this method
 * is not *guarnateed* to do anything at all :-)
 */
public class BackupFailedException extends IOException {

	public BackupFailedException(String s) {
		super(s);
	}

}

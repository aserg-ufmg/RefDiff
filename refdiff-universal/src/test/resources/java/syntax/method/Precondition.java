package java.syntax.method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jgit.annotations.Nullable;

public class Precondition {
  	  public static void checkArgument(
       boolean expression,
       String errorMessageTemplate,
       @Nullable Object @Nullable ... errorMessageArgs) {
     if (!expression) {
       throw new IllegalArgumentException(
           Platform.lenientFormat(errorMessageTemplate, errorMessageArgs));
     }
   }
}

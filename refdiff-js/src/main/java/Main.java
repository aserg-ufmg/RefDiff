import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.NodeVisitor;

public class Main {
    
    public static void main(String[] args) throws IOException {
        
        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        Parser parser = new Parser(compilerEnv);
        
        try (FileReader reader = new FileReader(new File("hello.js"))) {
            
            AstRoot root = parser.parse(reader, "hello.js", 1);
            root.visitAll(new NodeVisitor() {
                @Override
                public boolean visit(AstNode node) {
                    System.out.println(node.getClass().getName() + " " + node.getAbsolutePosition() + ":" + node.getLength());
                    System.out.println(node.toSource());
                    System.out.println();
                    return true;
                }
            });
        }
        
        
        
    }
    
}
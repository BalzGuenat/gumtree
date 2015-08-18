
package gen.envision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Stack;

import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

@Register(id = "envision", accept = "\\$")
public class EnvisionTreeGenerator extends TreeGenerator {

	private static final String LINE_NUM = "lineNum";
	private static final int nodeTypeIdx = 1; // The type is the second thing on a node line.
	private final HashMap<String, Integer> typeMap = new HashMap<>();
	private int nextTypeId = 0;
	
	@Override
	protected TreeContext generate(Reader src) throws IOException {
        try {
        	BufferedReader r = new BufferedReader(src);
        	TreeContext context = new TreeContext();
            Stack<ITree> treeStack = new Stack<>();
            int lineNum = 0;
            if (r.ready()) {
            	ITree t = createTree(context, r.readLine(), ++lineNum);
            	context.setRoot(t);
            	treeStack.push(t);
            }
            while (r.ready()) {
            	String nodeLine = r.readLine();
            	ITree t = createTree(context, nodeLine, ++lineNum);
            	
            	// This "walks up" the tree up to the parent of the current node
            	int popCount = treeStack.size() - depth(nodeLine);
            	for (int i = 0; i < popCount; i++) {
            		treeStack.pop();
            	}
            	
        		t.setParentAndUpdateChildren(treeStack.peek());
        		treeStack.push(t);
            }
            context.validate();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	private ITree createTree(TreeContext context, String nodeLine, int lineNum) {
		String[] node = nodeLine.split(" ");
    	if (!typeMap.containsKey(node[nodeTypeIdx])) {
    		typeMap.put(node[nodeTypeIdx], nextTypeId++);
    	}
    	int type = typeMap.get(node[nodeTypeIdx]); // This is the integer identifying the AST node-type
    	int dotIdx = nodeLine.indexOf('.');
    	String label; // This is a string relating the node to the code.
    	// e.g. in an assignment "p = 12" the left-hand-side AST node would have label "p")
    	if (dotIdx > 0)
    		label = nodeLine.substring(dotIdx + 1, nodeLine.length());
    	else
    		label = ITree.NO_LABEL;
    	String typeLabel = node[nodeTypeIdx]; // This is the name of the type. In our example "Ident".
    	
    	ITree t = context.createTree(type, label, typeLabel);
    	t.setMetadata(LINE_NUM, lineNum);
    	return t;
	}
	
	private static int depth(String nodeLine) {
		int i = 0;
		for (; nodeLine.charAt(i) == '\t'; i++);
		return i;
	}
}

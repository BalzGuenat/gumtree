
package gen.envision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.github.gumtreediff.tree.TreeUtils.TreeVisitor;

@Register(id = "envision", accept = "\\$")
public class EnvisionTreeGenerator extends TreeGenerator {

	private static final String LINE_NUM = "lineNum";
	private static final String LABEL = "label";
	private static final String NODE_ID = "nodeId";
	private static final String PARENT_ID = "parentId";
	private final HashMap<String, Integer> typeMap = new HashMap<>();
	private int nextTypeId = 0;
	
	private static class LabelComparator implements Comparator<ITree> {

		@Override
		public int compare(ITree a, ITree b) {
			String labelA = (String) a.getMetadata(LABEL);
			String labelB = (String) b.getMetadata(LABEL);
			if (labelA.length() == labelB.length())
				return labelA.compareTo(labelB);
			else
				return labelA.length() - labelB.length();
		}
		
	}
	
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
            
            TreeUtils.visitTree(context.getRoot(), new TreeVisitor() {
				
				@Override
				public void startTree(ITree tree) {}
				
				@Override
				public void endTree(ITree tree) {
					tree.getChildren().sort(new LabelComparator());;
				}
			});
            
            context.validate();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	private class EnvisionNode {
		String label;
		String type;
		String id;
		String parentId;
		String value;
		public EnvisionNode(String nodeLine) {
			Pattern p = Pattern.compile("\\t*(\\S+) (\\S+) \\{(\\S+)\\} \\{(\\S+)\\}(\\. [SID]_(.*))?");
			Matcher m = p.matcher(nodeLine);
			label = m.group(1);
			type = m.group(2);
			id = m.group(3);
			parentId = m.group(4);
			value = m.group(6);
		}
	}
	
	private ITree createTree(TreeContext context, String nodeLine, int lineNum) {
		EnvisionNode node = new EnvisionNode(nodeLine);
    	if (!typeMap.containsKey(node.type)) {
    		typeMap.put(node.type, nextTypeId++);
    	}
    	int type = typeMap.get(node.type); // This is the integer identifying the AST node-type
    	String label = (node.value != null) ? node.value : ITree.NO_LABEL; // This is a string relating the node to the code.
    	// e.g. in an assignment "p = 12" the left-hand-side AST node would have label "p")
    	String typeLabel = node.type; // This is the name of the type. In our example "Ident".
    	
    	ITree t = context.createTree(type, label, typeLabel);
    	t.setMetadata(LINE_NUM, lineNum);
    	t.setMetadata(LABEL, node.label);
    	t.setMetadata(NODE_ID, node.id);
    	t.setMetadata(PARENT_ID, node.parentId);
    	return t;
	}
	
	private static int depth(String nodeLine) {
		int i = 0;
		for (; nodeLine.charAt(i) == '\t'; i++);
		return i;
	}
}

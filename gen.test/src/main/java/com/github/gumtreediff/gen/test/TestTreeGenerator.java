
package com.github.gumtreediff.gen.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.Integer;
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

@Register(id = "testgen", accept = "\\.test$")
public class TestTreeGenerator extends TreeGenerator {
	
	private static final int INNER_NODE = 0;
	private static final int LEAF_NODE = 1;
	private static final int NAMED_NODE = 2;

	private static final String LINE_NUM = "lineNumber";
	private static final String DEPTH = "depth";
	private static final String TYPE = "type";
	private static final String LABEL = "label";
	private static final String PARENT_ID = "parentId";
	private static final HashMap<String, Integer> typeMap = new HashMap<>();
	private static int nextTypeId = 0;
	
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
            int pos = 0;
            if (r.ready()) {
            	String line = r.readLine();
            	ITree t = createTree(context, line, ++lineNum);
            	t.setPos(pos);
            	context.setRoot(t);
            	treeStack.push(t);
            	pos += line.length() + 1; // + 1 for newlines
            }
            while (r.ready()) {
            	String nodeLine = r.readLine();
            	ITree t = createTree(context, nodeLine, ++lineNum);
            	t.setPos(pos);
            	pos += nodeLine.length() + 1;
            	
            	// This "walks up" the tree up to the parent of the current node
				int depth = (Integer) t.getMetadata(DEPTH);
            	int popCount = treeStack.size() - depth;
            	for (int i = 0; i < popCount; i++) {
            		ITree closedTree = treeStack.pop();
            		closedTree.setLength(pos - closedTree.getPos());
            	}
            	
        		t.setParentAndUpdateChildren(treeStack.peek());
        		/*
            	if (t.getMetadata(TYPE).equals("NameText")) {
            		// if we find a NameText node, we update the label of its parent
            		ITree parent = treeStack.peek();
            		parent.setType(NAMED_NODE);
            		parent.setLabel(parent.getLabel() + ":" + t.getLabel());
            	}*/
            	
        		treeStack.push(t);
            }
            ++lineNum; // close off remaining trees.
        	while (!treeStack.isEmpty()) {
        		ITree closedTree = treeStack.pop();
        		closedTree.setLength(pos - closedTree.getPos());
        	}
            /*
            TreeUtils.visitTree(context.getRoot(), new TreeVisitor() {
				
				@Override
				public void startTree(ITree tree) {}
				
				@Override
				public void endTree(ITree tree) {
					tree.getChildren().sort(new LabelComparator());
				}
			});
            */
            context.validate();
			System.out.println(String.format("Tree generated:\t%d", System.currentTimeMillis()));
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

	static final Pattern p = Pattern.compile("^(\\s*)(\\d+):(.*)$");
	private class TestNode {
		int depth;
		String typeLabel;
		int type;
		String label;
		public TestNode(String nodeLine) {
			Matcher m = p.matcher(nodeLine);
			boolean matched = m.matches();
			assert matched;
			if (!matched)
				System.err.println("Cannot match line: " + nodeLine);
			depth = m.group(1).length();
			typeLabel = m.group(2);
			type = Integer.parseInt(typeLabel);
			label = m.group(3);
		}
	}
	
	private ITree createTree(TreeContext context, String nodeLine, int lineNum) {
		TestNode node = new TestNode(nodeLine);
    	ITree t = context.createTree(node.type, node.label, node.typeLabel);
		t.setMetadata(DEPTH, node.depth);
    	t.setMetadata(LINE_NUM, lineNum);
    	return t;
	}
}

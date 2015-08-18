package com.github.gumtreediff.client.diff;

import com.github.gumtreediff.client.Register;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

@Register(name = "envdmp", description = "Dump line matching",
        experimental = true, options = AbstractDiffClient.Options.class)
public class EnvisionMatchingDump extends AbstractDiffClient<AbstractDiffClient.Options> {

	private static final String LINE_NUM = "lineNum";
	private static final String NODE_ID = "nodeId";
	private static final String PARENT_ID = "parentId";
	
    public EnvisionMatchingDump(String[] args) {
        super(args);
    }

    @Override
    protected Options newOptions() {
        return new Options();
    }

    @Override
    public void run() {
        Matcher m = matchTrees();
        final MappingStore map = m.getMappings();
        TreeContext ctx = getDstTreeContext();
    	TreeUtils.visitTree(ctx.getRoot(), new TreeUtils.TreeVisitor() {
            public void startTree(ITree tree) {
            	ITree match = map.getSrc(tree);
            	if (match != null)
            	{
            		String outputLine = String.format("%d -> %s, %s", 
            				tree.getMetadata(LINE_NUM), 
            				match.getMetadata(NODE_ID), 
            				match.getMetadata(PARENT_ID) );
            		System.out.println(outputLine);
            	}
            }
            public void endTree(ITree tree) {}
        });
    }
}

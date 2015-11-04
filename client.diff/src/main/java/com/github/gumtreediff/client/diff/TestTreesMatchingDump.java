package com.github.gumtreediff.client.diff;

import com.github.gumtreediff.client.Register;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

@Register(name = "testdump", description = "Dump line matching",
        experimental = true, options = AbstractDiffClient.Options.class)
public class TestTreesMatchingDump extends AbstractDiffClient<AbstractDiffClient.Options> {

	private static final String LINE_NUMBER = "lineNumber";
	private static final String OUTPUT_FILE_SUFFIX = ".mtch";

    public TestTreesMatchingDump(String[] args) {
        super(args);
    }

    @Override
    protected Options newOptions() {
        return new Options();
    }

	private static class LineComparator implements Comparator<Mapping> {

		@Override
		public int compare(Mapping a, Mapping b) {
			int lineA = (Integer) a.getFirst().getMetadata(LINE_NUMBER);
			int lineB = (Integer) b.getFirst().getMetadata(LINE_NUMBER);
			return lineA - lineB;
		}

	}

    @Override
    public void run() {
        Matcher matcher = matchTrees();
        ArrayList<Mapping> mappings = new ArrayList<>(matcher.getMappingSet());
		mappings.sort(new LineComparator());
		try {
	        File dstFile = new File(opts.dst);
	        File outputFile = new File(dstFile.getParentFile(), dstFile.getName() + OUTPUT_FILE_SUFFIX);
			PrintWriter writer = new PrintWriter(outputFile);
	        for (Mapping m : mappings) {
	    		String outputLine = String.format("%s -> %s", 
	    				m.getFirst().getMetadata(LINE_NUMBER),
	    				m.getSecond().getMetadata(LINE_NUMBER) );
	    		writer.println(outputLine);
	        }
	        writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not write patch file.");
			e.printStackTrace();
		}
    }
}

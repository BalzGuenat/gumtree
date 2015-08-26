package com.github.gumtreediff.client.diff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import com.github.gumtreediff.client.Register;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.Matcher;

@Register(name = "envdmp", description = "Dump line matching",
        experimental = true, options = AbstractDiffClient.Options.class)
public class EnvisionMatchingDump extends AbstractDiffClient<AbstractDiffClient.Options> {

	private static final String NODE_ID = "nodeId";	
	private static final String OUTPUT_FILE_SUFFIX = ".idpatch";
	
    public EnvisionMatchingDump(String[] args) {
        super(args);
    }

    @Override
    protected Options newOptions() {
        return new Options();
    }

    @Override
    public void run() {
        Matcher matcher = matchTrees();
        ArrayList<Mapping> mappings = new ArrayList<>(matcher.getMappingSet());        
		try {
	        File dstFile = new File(opts.dst);
	        File outputFile = new File(dstFile.getParentFile(), dstFile.getName() + OUTPUT_FILE_SUFFIX);
			PrintWriter writer = new PrintWriter(outputFile);
	        for (Mapping m : mappings) {
	    		String outputLine = String.format("%s -> %s", 
	    				m.getSecond().getMetadata(NODE_ID), 
	    				m.getFirst().getMetadata(NODE_ID) );
	    		writer.println(outputLine);
	        }
	        writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not write patch file.");
			e.printStackTrace();
		}
    }
}

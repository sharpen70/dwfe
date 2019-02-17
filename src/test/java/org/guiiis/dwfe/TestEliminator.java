package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;

import org.guiiis.dwfe.vareliminate.BehaveChecker;
import org.guiiis.dwfe.vareliminate.Eliminator;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestEliminator extends TestCase {
	private static String rootDir = "./input/";
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestEliminator( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestEliminator.class );
    }
    
    public void test1() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex2")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	Eliminator eliminator = new Eliminator(rs);
    	
    //	System.out.println(r + "\n");
    	eliminator.elim();
    //	checker.printMark();
    	RuleSet re = eliminator.getRuleSet();
    	
    	for(Rule r : re) System.out.println(r);
    	
    	Assert.assertTrue(eliminator.getRuleSet().size() == 12);
    }
    
    public void test2() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex1")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	Eliminator eliminator = new Eliminator(rs);
    	
    //	System.out.println(r + "\n");
    	eliminator.elim();
    //	checker.printMark();
    	RuleSet re = eliminator.getRuleSet();
    	
    	for(Rule r : re) System.out.println(r);
    	
    	Assert.assertTrue(eliminator.getRuleSet().size() == 9);
    }
}

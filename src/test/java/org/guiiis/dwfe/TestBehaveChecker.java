package org.guiiis.dwfe;

import java.io.File;

import org.guiiis.dwfe.vareliminate.BehaveChecker;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestBehaveChecker extends TestCase {
	private static String rootDir = "./input/";
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestBehaveChecker( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestBehaveChecker.class );
    }
    
    public void test1() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex1")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	Rule r = kb.getRule("2");
    	
    	BehaveChecker checker = new BehaveChecker(rs);
    	
    //	System.out.println(r + "\n");
    	boolean re = checker.isBehaveRule(r);
    //	checker.printMark();
    	
    	Assert.assertTrue(re);
    }
    
    public void test2() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex1")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	Rule r = kb.getRule("1");
    	
    	BehaveChecker checker = new BehaveChecker(rs);
    	
 //   	System.out.println("\n" + r + "\n");
    	
    	boolean re = checker.isBehaveRule(r);
 //   	checker.printMark();
    	
    	Assert.assertFalse(re);
    }
    
    public void test3() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex2")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	Rule r = kb.getRule("1");
    	
    	BehaveChecker checker = new BehaveChecker(rs);
    	
 //   	System.out.println("\n" + r + "\n");
    	
    	boolean re = checker.isBehaveRule(r);
 //   	checker.printMark();
    	
    	Assert.assertTrue(re);
    }
    
}

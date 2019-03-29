package org.guiiis.dwfe;

import java.io.File;
import java.util.List;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.core.DatalogRule;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDatalogRewriting extends TestCase {
	private static String rootDir = "./input/";
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDatalogRewriting( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestDatalogRewriting.class );
    }
    
    public void test1() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex1")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery("?() :- e(X,Y).");
    	
    	DatalogRewriting dr = new DatalogRewriting(rs);
    	
    	List<DatalogRule> result = dr.pexec(query);
    	
    	if(result != null)
    		for(DatalogRule r : result) System.out.println(r);
    	
    	Assert.assertTrue(result == null);
    }
    
    public void test2() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex2")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery("?() :- p(X,Y).");
    	
    	DatalogRewriting dr = new DatalogRewriting(rs);
    	
    	List<DatalogRule> result = dr.pexec(query);
    	
    	if(result != null)
    		for(DatalogRule r : result) System.out.println(r);
    	
    	Assert.assertTrue(result.size() == 14);
    }
}

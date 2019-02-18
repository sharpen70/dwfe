package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;

import org.guiiis.dwfe.core.graal.PureQuery;
import org.guiiis.dwfe.opt.Eliminator;
import org.guiiis.dwfe.opt.Optimizier;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestOptimizier extends TestCase {
	private static String rootDir = "./input/";
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestOptimizier( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestOptimizier.class );
    }
    
    public void test1() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex1")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery("?() :- e(X,Y).");
    	
    	Optimizier opt = new Optimizier(new PureQuery(query), rs);
    	
    	IndexedByHeadPredicatesRuleSet irs = opt.getUnsolved();
    	
    	for(Rule r : irs) System.out.println(r);
    	
    	Assert.assertTrue(irs.size() == 5);
    }
}

package org.guiiis.dwfe;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.opt.Optimizier;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
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
    	
		List<Term> ansVar = new LinkedList<>();
		Predicate G = new Predicate(DatalogRewriting.ANSPredicateIdentifier, 0);
		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
		Rule H = DefaultRuleFactory.instance().create(query.getAtomSet(), DefaultAtomSetFactory.instance().create(Ghead));
		H.setLabel(String.valueOf(rs.size() + 1));
		rs.add(H);
		
    	Optimizier opt = new Optimizier(rs);
    	
    	IndexedByHeadPredicatesRuleSet irs = opt.getUnsolved();
    	
    	for(Rule r : irs) System.out.println(r);
    	
    	Assert.assertTrue(irs.size() == 10);
    }
    
    public void test2() throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new DlgpParser(new File(rootDir, "ex2")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery("?() :- p(X,Y).");
    	
		List<Term> ansVar = new LinkedList<>();
		Predicate G = new Predicate(DatalogRewriting.ANSPredicateIdentifier, 0);
		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
		Rule H = DefaultRuleFactory.instance().create(query.getAtomSet(), DefaultAtomSetFactory.instance().create(Ghead));
		H.setLabel(String.valueOf(rs.size() + 1));
		rs.add(H);
		
    	Optimizier opt = new Optimizier(rs);
    	
    	IndexedByHeadPredicatesRuleSet irs = opt.getUnsolved();
    	
    	List<Rule> s = opt.getSolved();
    	
    	for(Rule r : s) System.out.println(r);
    	System.out.println(s.size());
    	
    	Assert.assertTrue(irs.size() == 0);
    }
}

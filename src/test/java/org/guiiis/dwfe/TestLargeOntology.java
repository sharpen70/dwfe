package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.core.DatalogRule;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestLargeOntology extends TestCase {
	private static String rootDir = "./AGOSUV-bench";
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestLargeOntology( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestLargeOntology.class );
    }
    
//    public void test1() throws Exception {
//    	KBBuilder builder = new KBBuilder();
//    	builder.addRules(new DlgpParser(new File(rootDir, "O.dlp")));
//    	KnowledgeBase kb = builder.build();
//    	RuleSet rs = kb.getOntology();
//    	
//    	ConjunctiveQuery query = DlgpParser.parseQuery(
//    			"@prefix : <http://purl.obolibrary.org/obo/pr#>\n"
//    			+ "?(X) :- :has_part(X, Y), :CHEBI_23367(Y) , :lacks_part(X, Z), :SO_0000418(Z).");
//    	
//    	DatalogRewriting dr = new DatalogRewriting(rs);
//    	
//    	List<DatalogRule> result = dr.pexec(query);
//    	
//    	if(result != null)
//    		for(DatalogRule r : result) System.out.println(r);
//    	
//    	Assert.assertTrue(true);
//    }
    
    public void test2() throws Exception {
    	KBBuilder builder = new KBBuilder();
//    	builder.addRules(new OWL2Parser(new File("./pagoda-bench/Reactome/Reactome.owl")));
    	builder.addRules(new DlgpParser(new File(rootDir, "G/G.dlp")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
//    	ConjunctiveQuery query = DlgpParser.parseQuery(
//    			"@prefix biopax3: <http://www.biopax.org/release/biopax-level3.owl#>\n"
//    			+ "?(X, Y) :- biopax3:displayName(X, Y), biopax3:Pathway(X).");
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery(
    			"@prefix : <file:///c:/tmp/OpenGALEN2_FULL_WithPropertyChains.owl#>\n"
    			+ "?(X0) :- :isConsequenceOf(X0, X1), :Hypertension(X1).");

    	DatalogRewriting dr = new DatalogRewriting(rs);
    	
    	Collection<DatalogRule> result = dr.exec(query);
    	
    	System.out.println(result.size());
//    	if(result != null)
//    		for(DatalogRule r : result) System.out.println(r);
    	
    	Assert.assertTrue(true);
    }
}

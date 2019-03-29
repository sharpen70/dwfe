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

public class TestDatalogRewritingWithCompilation extends TestCase {
	private static String rootDir = "./AGOSUV-bench/";
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDatalogRewritingWithCompilation( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestDatalogRewritingWithCompilation.class );
    }
    
	public void test1() throws Exception {
		KBBuilder builder = new KBBuilder();
		builder.addRules(new DlgpParser(new File(rootDir, "A/A.dlp")));
		KnowledgeBase kb = builder.build();
		RuleSet rs = kb.getOntology();
		
		ConjunctiveQuery query = DlgpParser.parseQuery(
				"@prefix : <http://ksg.meraka.co.za/adolena.owl#>\n"
				+ "@prefix NAP: <file:///home/aurona/0AlleWerk/Navorsing/Ontologies/NAP/NAP#>\n"
				+ "?(X0) :- NAP:Device(X0), :assistsWith(X0, X1), NAP:PhysicalAbility(X1), :affects(X2, X1), NAP:Quadriplegia(X2).");
		
		DatalogRewriting dr = new DatalogRewriting(rs);
		
		List<DatalogRule> result = dr.pexec(query);
		
		System.out.println(result.size());
		
		Assert.assertTrue(true);
	}
	
	public void test2() throws Exception {
		KBBuilder builder = new KBBuilder();
		builder.addRules(new DlgpParser(new File(rootDir, "U/U.dlp")));
		KnowledgeBase kb = builder.build();
		RuleSet rs = kb.getOntology();
		
		ConjunctiveQuery query = DlgpParser.parseQuery(
				"@prefix : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
				+ "?(A,B,C):- :Student(A), :Advisor(A,B), :FacultyStaff(B), :takesCourse(A,C), :teacherOf(B,C), :Course(C).");
		
		DatalogRewriting dr = new DatalogRewriting(rs);
		
		List<DatalogRule> result = dr.pexec(query);
		
		System.out.println(result.size());
		
		Assert.assertTrue(true);
	}
}

package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.core.DatalogRule;
import org.guiiis.dwfe.core.DlgKnowledgeBase;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
	
	private static String rootDir = "./input/";
	
	private static String[] dlgs = {"dlg1", "dlg2", "dlg3", "dlg4", "dlg_o"};
	private static String[] queries = {
			"?(X) :- a(X), c(X), e(X).",
			"?() :- p(Y,Z),p(Z,Y).",
			"?() :- q(U,V),r(V,W),q(T,W).",
			"?() :- r(U,V),r(V,W),p(U,Z),p(V,Z),p(V,T),p(W,T),p1(U),p2(W).",
			"?() :- a(X,Y),b(Y)."};
	
	private static int caseNum = 5;

	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception
    {	   	
//    	for(int i = 4; i < caseNum; i++) {
//    		test(dlgs[i], queries[i], writer);
//    	}
    	
    	newtest(dlgs[4], queries[4]);

        assertTrue( true );
    }
    
//    public void test1() throws Exception {
//    	test(dlgs[1], queries[1]);
//    }
    
    private void newtest(String dlgFile, String q) throws Exception {
    	DlgKnowledgeBase kb = new DlgKnowledgeBase(new DlgpParser(new File(rootDir, dlgFile)));
    	
    	ConjunctiveQuery query = DlgpParser.parseQuery(q);
    	
    	kb.rewriteToDlg(query, System.out);
    }
}

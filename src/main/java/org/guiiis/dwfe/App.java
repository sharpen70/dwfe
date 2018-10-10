package org.guiiis.dwfe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.guiiis.dwfe.core.RewritingAlgorithm;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.backward_chaining.pure.AggregSingleRuleOperator;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final String DLGFLAG = "dlg";
	private static final String UCQFLAG = "ucq";
	
	public static void main(String[] args) throws Exception {
		PrintStream output = System.out;
		
		String ontologyfile = null;
		String queriesfile = null;
		String outputfile = null;
		
		String mode = DLGFLAG;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				if(args[i].equals("-o")) outputfile = args[++i];
				else mode = args[i].substring(1);
			}
			else {
				if(ontologyfile == null) ontologyfile = args[i];
				else queriesfile = args[i];
			}
		}
		
		if(ontologyfile == null) { System.out.println("Missing Ontology File !!"); return; }
		if(queriesfile == null) { System.out.println("Missing Queries File !!"); return; }
		if(outputfile != null) { 
			File o = new File(outputfile);
			o.getParentFile().mkdirs();
			o.createNewFile();
			output = new PrintStream(new FileOutputStream(o, false)); 
		}
		
    	// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
	
		kbb.addAll(new DlgpParser(new File(ontologyfile)));
		
		KnowledgeBase kb = kbb.build();
		
		RuleSet ontology = kb.getOntology();
		
		ArrayList<String> qs = SimpleQueryFileReader.read(new File(queriesfile));

		
		for(String s : qs) {
//			System.out.println(s);
			ConjunctiveQuery query = DlgpParser.parseQuery(s);
			if(mode.equals(DLGFLAG)) {
				DatalogRewriting dr = new DatalogRewriting();			
				dr.setProfiler(new RealTimeProfiler(output));
				output.println(query.toString());
				dr.exec(query, ontology);	
			}
			if(mode.equals(UCQFLAG)) {
				IndexedByHeadPredicatesRuleSet indexedRuleSet = new IndexedByHeadPredicatesRuleSet(ontology);

				// rewriting
				RulesCompilation compilation = new IDCompilation();
//				compilation.compile(ontology.iterator());
//				
				AggregSingleRuleOperator operator = new AggregSingleRuleOperator();
				Profiler p = new RealTimeProfiler(output);
				RewritingAlgorithm algo = new RewritingAlgorithm(operator);
				operator.setProfiler(p);
				algo.setProfiler(p);
				algo.execute(query, indexedRuleSet, NoCompilation.instance());
			}
		}
		
//		ConjunctiveQuery query = DlgpParser.parseQuery(qs.get(3));
//		output.println(query.toString());
//		dr.exec(query, ontology);			
		// 8 - Close resources
		kb.close();
	}
}

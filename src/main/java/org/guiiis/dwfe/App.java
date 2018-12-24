package org.guiiis.dwfe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.core.DlgKnowledgeBase;
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
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;

/**
 * Hello world!
 *
 */
public class App 
{	
	public static void main(String[] args) throws Exception {
		PrintStream output = System.out;
		
		String ontologyfile = null;
		
		String queriesfile = null;
		String outputfile = null;
		
		int mode = 0;		/* 0: Rewriting to Datalog;  1: Rewriting to UCQ */
		int syntax = 0; 	/* 0: DlgP syntax;  1: OWL syntax */
		
		DlgKnowledgeBase kb;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				String flag = args[i].substring(1);
				if(flag.equals("i")) {
					syntax = Integer.parseInt(args[++i]);
					ontologyfile = args[++i];
				}
				if(flag.equals("m")) mode = Integer.parseInt(args[++i]);
				if(flag.equals("o")) outputfile = args[++i];
				if(flag.equals("q")) queriesfile = args[++i];
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
		
//    	// 0 - Create a KBBuilder
//		KBBuilder kbb = new KBBuilder();
//	
//		kbb.addAll(new DlgpParser(new File(ontologyfile)));
//		
//		KnowledgeBase kb = kbb.build();
//		
//		RuleSet ontology = kb.getOntology();
		
		if(syntax == 1) kb = new DlgKnowledgeBase(new OWL2Parser(new File(ontologyfile)));
		else kb = new DlgKnowledgeBase(new DlgpParser(new File(ontologyfile)));
		
		kb.setProfiling(output);

		ArrayList<String> qs = SimpleQueryFileReader.read(new File(queriesfile));
		
		for(String s : qs) {
			ConjunctiveQuery query = DlgpParser.parseQuery(s);
			
			if(mode == 0) kb.rewriteToDlg(query);
			if(mode == 1) kb.rewriteToUCQ(query);
		}
		
		kb.close();
	}
}

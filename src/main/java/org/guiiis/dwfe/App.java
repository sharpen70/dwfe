package org.guiiis.dwfe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) throws Exception {
		PrintStream output = System.out;
		
		if(args.length < 1) { System.out.println("Missing Ontology File !!"); return; }
		if(args.length < 2) { System.out.println("Missing Queries File !!"); return; }
		if(args.length >= 3) { 
			File o = new File(args[2]);
			o.getParentFile().mkdirs();
			o.createNewFile();
			output = new PrintStream(new FileOutputStream(o, false)); 
		}
		
		String ontologyfile = args[0];
		String queriesfile = args[1];
		
		output.println("Evaluating ontology " + ontologyfile + " and queries " + queriesfile);
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
	
		kbb.addAll(new DlgpParser(new File(ontologyfile)));
		
		KnowledgeBase kb = kbb.build();
		
		RuleSet ontology = kb.getOntology();
		
		Collection<String> qs = SimpleQueryFileReader.read(new File(queriesfile));
		
		DatalogRewriting dr = new DatalogRewriting();
		
		dr.setProfiler(new RealTimeProfiler(output));
		
		for(String s : qs) {
			System.out.println(s);
			ConjunctiveQuery query = DlgpParser.parseQuery(s);
			output.println(query.toString());
			dr.exec(query, ontology);			
		}
		// 8 - Close resources
		kb.close();
	}
}

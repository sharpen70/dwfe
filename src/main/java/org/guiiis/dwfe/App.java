package org.guiiis.dwfe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.DefaultKnowledgeBase;

/**
 * Hello world!
 *
 */
public class App 
{	
	public static void main(String[] args) throws Exception {		
		String ontologyfile = null;		
		String queriesfile = null;
		String outputDir = "./";
		
		int mode = 0;			/* 0: Rewriting to Datalog;  1: Rewriting to UCQ */
		int input_syntax = 0; 	/* 0: DlgP syntax;  1: OWL syntax */
		int query_syntax = 0;	/* 0: DlgP syntax;  1: Sparql syntax */
		
		DlgKnowledgeBase kb;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				String flag = args[i].substring(1);
				if(flag.equals("i")) {
					input_syntax = Integer.parseInt(args[++i]);
					ontologyfile = args[++i];
				}
				if(flag.equals("m")) mode = Integer.parseInt(args[++i]);
				if(flag.equals("o")) outputDir = args[++i];
				if(flag.equals("q")) {
					query_syntax = Integer.parseInt(args[++i]);
					queriesfile = args[++i];
				}
			}
		}
		
		if(ontologyfile == null) { System.out.println("Missing Ontology File !!"); return; }
		if(queriesfile == null) { System.out.println("Missing Queries File !!"); return; }

		File fdir = new File(outputDir);
		fdir.getParentFile().mkdirs();
		fdir.mkdir();
		
		File ontofile = new File(ontologyfile);
		if(input_syntax == 1) kb = new DlgKnowledgeBase(new OWL2Parser(ontofile));
		else kb = new DlgKnowledgeBase(new DlgpParser(ontofile));

		ArrayList<String> qs;
		
		if(query_syntax == 1) qs = SimpleQueryFileReader.read2(new File(queriesfile));
		else qs = SimpleQueryFileReader.read(new File(queriesfile));
		
		for(int i = 0; i < qs.size(); i++) {
			ConjunctiveQuery query;
			String s = qs.get(i);
			
			
			File outputfile = new File(fdir.getAbsolutePath() + "/" + ontofile.getName() + "_q" + i + "_" + mode);
			System.out.println(outputfile);
			outputfile.createNewFile();

			if(query_syntax == 1) query =  new SparqlConjunctiveQueryParser(s).getConjunctiveQuery();  
			else query = DlgpParser.parseQuery(s);
			
//			if(mode == 0) {
//				File outputQueryFile = new File(fdir.getAbsolutePath() + "/" + ontofile.getName() + "_q" + i + "_" 
//						+ mode + "-q");
//				outputQueryFile.createNewFile();
//				kb.rewriteToDlg(query, new PrintStream(outputfile), new PrintStream(outputQueryFile));
//			}
//			if(mode == 1) kb.rewriteToUCQ(query, new PrintStream(outputfile));
			if(mode == 0) kb.rewriteToDlg(query, new PrintStream(outputfile));
			if(mode == 1) kb.rewriteToUCQ(query, new PrintStream(outputfile));			
		}
		
		kb.close();
	}
}

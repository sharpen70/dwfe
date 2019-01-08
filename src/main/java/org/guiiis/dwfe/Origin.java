package org.guiiis.dwfe;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.store.triplestore.rdf4j.RDF4jStore;

public class Origin {
	public static void main(String[] args) throws Exception {		
		String ontologyfile = null;		
		String queriesfile = null;
		String datafile = null;
		
		int input_syntax = 0; 	/* 0: DlgP syntax;  1: OWL syntax */
		int query_syntax = 0;	/* 0: DlgP syntax;  1: Sparql syntax */
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				String flag = args[i].substring(1);
				if(flag.equals("i")) {
					input_syntax = Integer.parseInt(args[++i]);
					ontologyfile = args[++i];
				}
				if(flag.equals("q")) {
					query_syntax = Integer.parseInt(args[++i]);
					queriesfile = args[++i];
				}
			}
		}
		
		KBBuilder kbb = new KBBuilder();
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		
		//Configuring RDF4J Sail Store
		InputStream datastream = new FileInputStream(datafile);
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);

	//	kbb.setStore(new RDF4jStore(a));
		
		DlgKnowledgeBase kb;
		
		if(ontologyfile == null) { System.out.println("Missing Ontology File !!"); return; }
		if(queriesfile == null) { System.out.println("Missing Queries File !!"); return; }

		ArrayList<String> qs = SimpleQueryFileReader.read(new File(queriesfile));
		
		for(int i = 0; i < qs.size(); i++) {
			ConjunctiveQuery query;
			String s = qs.get(i);
			

			if(query_syntax == 1) query =  new SparqlConjunctiveQueryParser(s).getConjunctiveQuery();  
			else query = DlgpParser.parseQuery(s);
			
//			if(mode == 0) {
//				File outputQueryFile = new File(fdir.getAbsolutePath() + "/" + ontofile.getName() + "_q" + i + "_" 
//						+ mode + "-q");
//				outputQueryFile.createNewFile();
//				kb.rewriteToDlg(query, new PrintStream(outputfile), new PrintStream(outputQueryFile));
//			}
		
		}
		
	}
}

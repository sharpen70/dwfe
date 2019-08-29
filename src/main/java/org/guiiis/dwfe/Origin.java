package org.guiiis.dwfe;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.store.triplestore.rdf4j.RDF4jStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;

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
				if(flag.equals("d")) datafile = args[++i];
			}
		}		
		
		List<String> qs = SimpleQueryFileReader.read(new File(queriesfile), query_syntax);

		String baseURI = "";
		
		long t1 = System.currentTimeMillis();
		
		File file = new File(datafile);
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		
		try {
			RepositoryConnection con = repo.getConnection();
			try {
				con.add(file, baseURI, RDFFormat.TURTLE);
				
				System.out.println("Size of the Repo: " + con.size());
			}
			finally {
				con.close();
			}
		}
		catch (RDF4JException e) {
		   // handle exception. This catch-clause is
		   // optional since RDF4JException is an unchecked exception
		}
		catch (IOException e) {
		   // handle io exception
		}
		
		KBBuilder kbb = new KBBuilder();
		
		try {
			kbb.setStore(new RDF4jStore(repo));
		} catch (AtomSetException e) {
			e.printStackTrace();
		}
		
		if(input_syntax == 0) kbb.addRules(new DlgpParser(new File(ontologyfile)));
		else kbb.addRules(new OWL2Parser(new File(ontologyfile)));
		
		kbb.setApproach(Approach.REWRITING_ONLY);
		
		KnowledgeBase kb = kbb.build();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("BuildTime cost: " + (t2 - t1) + "ms");		

		for(int i = 0; i < qs.size(); i++) {
			String query = qs.get(i);
			ConjunctiveQuery q;
			
			System.out.println("Executing query " + (i + 1));
			
			if(query_syntax == 0) q = DlgpParser.parseQuery(query);
			else q = new SparqlConjunctiveQueryParser(query).getConjunctiveQuery();
			
//			writer.write("\n= Answers =\n");
			CloseableIterator<Substitution> results = kb.query(q);
			
			int ansNum = 0;
			
			if (results.hasNext()) {
				do {
					results.next();
			  		ansNum++;
			  	} while (results.hasNext());
			} 
			
			System.out.println("AnsNum: " + ansNum);
			
			long t3 = System.currentTimeMillis();
			
			System.out.println("QueryTime cost: " + (t3 - t2) + "ms");
			
			t2 = System.currentTimeMillis();
			
			System.out.println("================================");
			results.close();
		}	
		
		kb.close();
	}
}

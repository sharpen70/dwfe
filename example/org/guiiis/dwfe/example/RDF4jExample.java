package org.guiiis.dwfe.example;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.store.Store;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.store.rdbms.driver.SqliteDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;
import fr.lirmm.graphik.graal.store.triplestore.rdf4j.RDF4jStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class RDF4jExample {
	
	public static void main(String[] args) throws Exception {
		//String data = "/home/sharpen/projects/evaluations/dwfe/data/Reactome/reactome010.ttl";
		//String owl = "/home/sharpen/projects/evaluations/dwfe/pagoda-bench/Reactome/Reactome_m.dlp";
		String data = "/home/sharpen/projects/evaluations/dwfe/data/U/lubm1.ttl";
		String owl = "/home/sharpen/projects/evaluations/dwfe/AGOSUV-bench/U/U.dlp";
		
//		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
//					+ " SELECT ?X "
//					+ " WHERE { "
//					+ " ?X rdf:type <http://www.biopax.org/release/biopax-level3.owl#DnaReference>"
//					+ "}";
		
		String query = "@prefix : <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>"
					+ "?(A,B) :- :Person(A), :teacherOf(A,B), :Course(B).";
		String baseURI = "";
		
		long t1 = System.currentTimeMillis();
		
		File file = new File(data);
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
	//		kbb.setStore(new NaturalRDBMSStore(new SqliteDriver(file)));
		} catch (AtomSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		kbb.addRules(new DlgpParser(new File(owl)));
		kbb.setApproach(Approach.REWRITING_ONLY);
		
		KnowledgeBase kb = kbb.build();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("BuildTime cost: " + (t2 - t1) + "ms");
		
		ConjunctiveQuery q = DlgpParser.parseQuery(query);
		
		CloseableIterator<Substitution> results = kb.query(q);
		
		long t3 = System.currentTimeMillis();
		
		int ans = 0;

		if (results.hasNext()) {
			do {
				ans++;
				results.next();
		  	} while (results.hasNext());
		}
		
		System.out.println("QueryTime cost: " + (t3 - t2) + "ms, ANS num: " + ans);
		
		results.close();
		kb.close();
	}
}

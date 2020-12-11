package org.guiiis.dwfe;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.OFF);
		
		String ontologyfile = args[0];		
		String queriesfile = args[1];
		String dataDir = args[2];						
		
		long t1 = System.currentTimeMillis();
		
		File file = new File(dataDir);
		
		KBBuilder kbb = new KBBuilder();				
		
		kbb.addRules(new DlgpParser(new File(ontologyfile)));
		
		if(file.isDirectory()) {
			for(File data : file.listFiles()) {
				kbb.addAtoms(new DlgpParser(data));
			}
		}
		else kbb.addAtoms(new DlgpParser(file));
		
		KnowledgeBase kb = kbb.build();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("BuildTime cost: " + (t2 - t1) + "ms");		
		
		Scanner scanner = new Scanner(new File(queriesfile));
		int i = 0;
		while(scanner.hasNextLine()) {
			String query = scanner.nextLine();
			
			ConjunctiveQuery q = DlgpParser.parseQuery(query);;
			
			System.out.println("Executing query " + (i + 1));
			
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
			
			System.out.println("================================");
			results.close();
		}	
		scanner.close();
		kb.close();
	}
}

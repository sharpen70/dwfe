package org.guiiis.dwfe;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.guiiis.dwfe.core.graal.NaiveMapper;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.factory.AtomFactory;
import fr.lirmm.graphik.graal.api.kb.Approach;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.store.Store;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultPredicateFactory;
import fr.lirmm.graphik.graal.core.mapper.MappedStore;
import fr.lirmm.graphik.graal.core.stream.filter.AtomFilterIterator;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.store.rdbms.driver.SqliteDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class Origin_rdbms {
	public static void main(String[] args) throws Exception {		
		String ontologyfile = null;		
		String queriesfile = null;
		String datafile = null;
		
		String rdbmsDir = "RDBMS/";
		
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
		
		long t1 = System.currentTimeMillis();		
		
		KBBuilder kbb = new KBBuilder();
		
		String dataName = new File(datafile).getName();
		String dname = dataName.indexOf(".") != -1 ? dataName.substring(0, dataName.indexOf(".")) : dataName;
		String dbpath = rdbmsDir + dname + ".db";
		
		if(!(new File(dbpath).exists())) init(datafile, dbpath);
		
		Store naturalRDBMSStore = new NaturalRDBMSStore(new SqliteDriver(new File(dbpath)));
		
		kbb.setStore(naturalRDBMSStore);
		
		if(input_syntax == 0) kbb.addRules(new DlgpParser(new File(ontologyfile)));
		else kbb.addRules(new OWL2Parser(new File(ontologyfile)));
		
		kbb.setApproach(Approach.REWRITING_ONLY);
		
		KnowledgeBase kb = kbb.build();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("BuildTime cost: " + (t2 - t1) + "ms");
		
		for(String query : qs) {
			ConjunctiveQuery q;
			
			if(query_syntax == 0) q = DlgpParser.parseQuery(query);
			else q = new SparqlConjunctiveQueryParser(query).getConjunctiveQuery();
			
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
	
	private static void init(String datafile, String dbfile) throws Exception {
		System.out.println("initializing db for " + datafile + " ...");
		Store naturalRDBMSStore = new NaturalRDBMSStore(new SqliteDriver(new File(dbfile)));
		naturalRDBMSStore = new MappedStore(naturalRDBMSStore, new NaiveMapper().inverse());
		
		DlgpParser parser = new DlgpParser(new File(datafile));
		naturalRDBMSStore.addAll(new AtomFilterIterator(parser));
		parser.close();
		naturalRDBMSStore.close();
		System.out.println("finished.");
	}
}

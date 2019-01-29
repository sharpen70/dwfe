package org.guiiis.dwfe.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.factory.AtomFactory;
import fr.lirmm.graphik.graal.api.store.Store;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultPredicateFactory;
import fr.lirmm.graphik.graal.core.mapper.MappedStore;
import fr.lirmm.graphik.graal.core.mapper.PrefixMapper;
import fr.lirmm.graphik.graal.core.stream.filter.AtomFilterIterator;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.store.rdbms.driver.RdbmsDriver;
import fr.lirmm.graphik.graal.store.rdbms.driver.SqliteDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;

public class GenerateRDBMSData {
	public static String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";	
	
	public static void main(String[] args) throws Exception {
		String db = "/home/sharpen/projects/evaluations/dwfe/data/";

		
		File dbdir = new File(db);
		
		for(File b : dbdir.listFiles()) {
			for(File ttl : b.listFiles()) {					
				String dbfile = ttl.getAbsolutePath().replaceAll(".ttl", ".db");
				RdbmsDriver driver = new PostgreSQLDriver(new File(db));
				gen(driver, ttl);
			}
		}
		

	}
	
	private static void gen(RdbmsDriver driver, File datafile) throws Exception {
		
		System.out.println("Creating db for " + datafile + " ...");
		Store naturalRDBMSStore = new NaturalRDBMSStore(driver);
	//	naturalRDBMSStore = new MappedStore(naturalRDBMSStore, new NaiveMapper().inverse());
		naturalRDBMSStore = new MappedStore(naturalRDBMSStore, new PrefixMapper("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#").inverse());
		
		Model results = Rio.parse(new FileInputStream(datafile), "", RDFFormat.TURTLE);
		
		AtomFactory factory = DefaultAtomFactory.instance();
		
//		results.stream().forEach((statement) -> {
//			Atom a;
//			if(statement.getPredicate().toString().equals(type)) {	
//				Term t = DefaultTermFactory.instance().createConstant(statement.getSubject());
//				Predicate p = DefaultPredicateFactory.instance().create(statement.getObject().toString(), 1);
//				a = factory.create(p, t);
//			}
//			else {
//				Term t1 = DefaultTermFactory.instance().createConstant(statement.getSubject());
//				Term t2 = DefaultTermFactory.instance().createConstant(statement.getObject());
//				Predicate p = DefaultPredicateFactory.instance().create(statement.getPredicate().toString(), 2);
//				a = DefaultAtomFactory.instance().create(p, t1, t2);
//
//			}
//			try {
//				naturalRDBMSStore.add(a);
//			} catch (AtomSetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});

		naturalRDBMSStore.close();
		System.out.println("finished.");
	}
	
	


	
	
	
	
}

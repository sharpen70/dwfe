package org.guiiis.dwfe.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.factory.AtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultPredicateFactory;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;

public class TurtleDataToDlgp {
	public static void main(String[] args) throws Exception {
		
		
		String db = "/home/sharpen/projects/evaluations/dwfe/data/";
		String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";		

		AtomFactory factory = DefaultAtomFactory.instance();
		
		File dbdir = new File(db);
		File ttl = new File("/home/sharpen/projects/dwfe_evaluation/data/Uniprot/big.ttl");
//		for(File b : dbdir.listFiles()) {
//			for(File ttl : b.listFiles()) {
				if(ttl.getName().endsWith(".ttl")) {
					System.out.println("Converting turtle data " + ttl + " ...");
					String out = ttl.getAbsolutePath().replaceAll(".ttl", ".dlp");
					File outfile = new File(out);
					DlgpWriter writer = new DlgpWriter(outfile);
					
					Model results = Rio.parse(new FileInputStream(ttl), "", RDFFormat.TURTLE);
					
					results.stream().forEach((statement) -> {
						Atom a;
						if(statement.getPredicate().toString().equals(type)) {	
							Term t = DefaultTermFactory.instance().createConstant(statement.getSubject());
							Predicate p = DefaultPredicateFactory.instance().create(statement.getObject().toString(), 1);
							a = factory.create(p, t);
						}
						else {
							Term t1 = DefaultTermFactory.instance().createConstant(statement.getSubject());
							Term t2 = DefaultTermFactory.instance().createConstant(statement.getObject());
							Predicate p = DefaultPredicateFactory.instance().create(statement.getPredicate().toString(), 2);
							a = DefaultAtomFactory.instance().create(p, t1, t2);

						}
						try {
							writer.write(a);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});			
					writer.close();
				}
			}
//		}		
//	}
}

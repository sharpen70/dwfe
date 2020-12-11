package org.guiiis.dwfe.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.owl.OWL2ParserException;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import fr.lirmm.graphik.util.stream.CloseableIterator;

public class GenToCSVSQL {
	public static void main(String[] args) throws KBBuilderException, OWL2ParserException, IOException {
		String dir = "/home/peng/projects/tractable-abd/tboxfiles/";
		String abox_dir = "/home/peng/projects/tractable-abd/aboxdump_csv/";
		
		File tboxs = new File(dir);
		
		for(File tbox : tboxs.listFiles()) {
			KBBuilder kbb = new KBBuilder();
			kbb.addRules(new OWL2Parser(tbox));
			KnowledgeBase kb = kbb.build();
			
			String tbox_name = tbox.getName().split("\\.")[0];
			String name = abox_dir + tbox_name;
			
			File abox_dir_f = new File(name);
			abox_dir_f.mkdirs();
			
			Set<String> predicates = new HashSet<>();
			
			RuleSet rs = kb.getOntology();
			for(Rule r : rs) {
				CloseableIterator<Atom> it = r.getHead().iterator();
				while(it.hasNext()) {
					Atom a = it.next();
					String id = a.getPredicate().getIdentifier().toString();
					int i = id.indexOf('#');
					if(i != -1) id = id.substring(id.indexOf('#') + 1);
					predicates.add(id);
				}
				it = r.getBody().iterator();
				while(it.hasNext()) {
					Atom a = it.next();
					String id = a.getPredicate().getIdentifier().toString();
					int i = id.indexOf('#');
					if(i != -1) id = id.substring(id.indexOf('#') + 1);
					predicates.add(id);
				}
			}
			
			PrintStream print = new PrintStream(new File(name + ".sql"));
			
			for(String s : predicates) {
				String sql = "SELECT *\n"
						+ "FROM `" + s + "`\n"
//						+ "INTO OUTFILE '" + name + "/" + s + ".csv'\n"
						+  "INTO OUTFILE '/tmp/" + tbox_name + "/" + s + ".csv'\n"
						+ "FIELDS TERMINATED BY ','\n"
						+ "LINES TERMINATED BY '\\n';\n";
				
				print.print(sql);
			}
			
			print.close();
		}
	}
}

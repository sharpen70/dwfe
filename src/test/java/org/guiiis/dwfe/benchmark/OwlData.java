package org.guiiis.dwfe.benchmark;

import java.io.File;
import java.util.List;

import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;

public class OwlData {
	private static final String rootDir = "/home/sharpen/projects/evaluations/dwfe/";
	private static final String reactome_o = "Reactome/Reactome.owl";
	private static final String uniprot_o = "original-tboxes/Uniprot.owl";
	private static final String reactome_c = "normalised-tboxes-with-chains/tbox-00485-reactome.owl";
	private static final String uniprot_c = "normalised-tboxes-with-chains/tbox-00304-uniprot.owl";
	
	private static final String reactome_sparql = "Reactome/test_s.sparql";
	private static final String uniprot_sparql = "Uniprot/test.sparql";
	
	public static void main(String[] args) throws Exception {
//		KBBuilder builder = new KBBuilder();
//		
//		builder.addAll(new OWL2Parser(rootDir + reactome_o));
//		
//		KnowledgeBase kb = builder.build();
		
		List<String> qs = SimpleQueryFileReader.read2(new File(rootDir + reactome_sparql));
		
		for(String s : qs) {
			ConjunctiveQuery q = new SparqlConjunctiveQueryParser(s).getConjunctiveQuery();
			System.out.println(q);
			
			
			DlgKnowledgeBase kb = new DlgKnowledgeBase(new OWL2Parser(new File(rootDir + reactome_o)));
			
			kb.rewriteToDlg(q);
		}		
	}
}

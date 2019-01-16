package org.guiiis.dwfe.example;

import java.io.File;
import java.util.List;

import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;

public class OwlData {
	private static final String rootDir = "/home/sharpen/projects/evaluations/dwfe/";
	private static final String reactome_o = "Reactome/Reactome.owl";
	private static final String uniprot_o = "Uniprot/Uniprot.owl";
	private static final String reactome_c = "normalised-tboxes-with-chains/tbox-00485-reactome.owl";
	private static final String uniprot_c = "normalised-tboxes-with-chains/tbox-00304-uniprot.owl";
	
	private static final String reactome_sparql = "Reactome/test_s.sparql";
	private static final String uniprot_sparql = "Uniprot/test.sparql";
	
	public static void main(String[] args) throws Exception {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.OFF);
		
		List<String> qs = SimpleQueryFileReader.read2(new File(rootDir + reactome_sparql));
		DlgKnowledgeBase kb = new DlgKnowledgeBase(new OWL2Parser(new File(rootDir + reactome_o)));
		
//		AnalyserRuleSet ars = new AnalyserRuleSet(kb.getOntology());
//		FUSAnalyser analyzer = new FUSAnalyser(ars);
//		RuleSet rs = analyzer.maximalFusComponent();
//		
//		System.out.println(rs.toString());
		
		
		for(String s : qs) {
			ConjunctiveQuery q = new SparqlConjunctiveQueryParser(s).getConjunctiveQuery();
			
			System.out.println("UCQ rewriting:");
			kb.rewriteToUCQ(q, System.out);			
			System.out.println("Datalog rewriting:");
	//		kb.rewriteToDlg(q, System.out);
		}		
	}
}

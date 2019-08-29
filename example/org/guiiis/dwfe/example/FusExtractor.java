package org.guiiis.dwfe.example;

import java.io.File;
import java.io.PrintStream;

import org.guiiis.dwfe.core.DlgKnowledgeBase;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.kb.KBBuilder;

// Fus is undecidable . This class just extract the maximal acyclic component of the ontology

public class FusExtractor {
	
	public static void main(String[] args) throws Exception {
		File bench = new File("/home/sharpen/projects/evaluations/dwfe/pagoda-bench/");
	//	File bench = new File("/home/sharpen/projects/evaluations/dwfe/AGOSUV-bench/");
		
		for(File f : bench.listFiles()) {
			File origin = new File(f.getAbsolutePath() + "/" + f.getName() + ".owl");
			File modified = new File(f.getAbsolutePath() + "/" + f.getName() + "_m.dlp");
			
			DlgpWriter writer = new DlgpWriter(new PrintStream(modified));
			
			KBBuilder kbb = new KBBuilder();
			kbb.addRules(new OWL2Parser(origin));
			KnowledgeBase _kb = kbb.build();
			DlgKnowledgeBase kb = new DlgKnowledgeBase(_kb.getOntology());
			
			RuleSet fus = kb.getFusComponent();
			
			for(Rule r : fus) {
				writer.write(r);
				
			}
			
			writer.close();
		}
	}
}

package org.guiiis.dwfe.example;

import java.io.File;
import java.io.PrintStream;

import org.guiiis.dwfe.core.DlgKnowledgeBase;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;

// Fus is undecidable . This class just extract the maximal acyclic component of the ontology

public class FusExtractor {
	
	public static void main(String[] args) throws Exception {
		File bench = new File("/home/sharpen/projects/evaluations/dwfe/pagoda-bench/");
		
		for(File f : bench.listFiles()) {
			File origin = new File(f.getAbsolutePath() + "/" + f.getName() + ".owl");
			File modified = new File(f.getAbsolutePath() + "/" + f.getName() + "_m.dlp");
			
			DlgpWriter writer = new DlgpWriter(new PrintStream(modified));
			
			DlgKnowledgeBase kb = new DlgKnowledgeBase(new OWL2Parser(origin));
			
			RuleSet fus = kb.getFusComponent();
			
			for(Rule r : fus) {
				writer.write(r);
			}
			
			writer.close();
		}
	}
}

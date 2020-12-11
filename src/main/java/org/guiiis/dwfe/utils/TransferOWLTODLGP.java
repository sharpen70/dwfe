package org.guiiis.dwfe.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.owl.OWL2ParserException;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;

public class TransferOWLTODLGP {
	public static void main(String[] args) throws KBBuilderException, OWL2ParserException, IOException {
		String dir = "/home/peng/projects/tractable-abd/tboxfiles/";
		
		File tboxs = new File(dir);
		
		for(File tbox : tboxs.listFiles()) {
			KBBuilder kbb = new KBBuilder();
			kbb.addRules(new OWL2Parser(tbox));
			KnowledgeBase kb = kbb.build();
			
			String name = dir + tbox.getName().split("\\.")[0] + ".dlp";
			OutputStream out = new PrintStream(name);
			DlgpWriter writer = new DlgpWriter(out);
			for(Rule r : kb.getOntology()) {
				writer.write(r);
			}
			writer.close();
		}
	}
}

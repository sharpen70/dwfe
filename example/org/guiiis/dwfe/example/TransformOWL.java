package org.guiiis.dwfe.example;

import java.io.File;
import java.io.FileNotFoundException;

import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.io.OWL2Parser2;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.io.owl.OWL2ParserException;

public class TransformOWL {
	public static void main(String[] args) throws FileNotFoundException, AtomSetException, OWL2ParserException {
		String O = "/home/sharpen/projects/evaluations/benchmarks/owl/Uniprot/Uniprot.owl";
		
		DlgKnowledgeBase kb = new DlgKnowledgeBase(new OWL2Parser2(new File(O)));
	}
}

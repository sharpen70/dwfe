package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;

import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.RuleSetException;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.owl.OWL2ParserException;
import fr.lirmm.graphik.graal.rulesetanalyser.property.AGRDProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.DomainRestrictedProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.LinearProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.StickyProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.WeaklyAcyclicProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;

public class CheckProperties {
	public static void main(String[] args) throws FileNotFoundException, RuleSetException, OWL2ParserException {
		String ontoDirPath = "/home/sharpen/projects/evaluations/benchmarks/new/chembl";
		
//		File ontoDir = new File(ontoDirPath);
		File onto = new File(ontoDirPath);
//		for(File onto : ontoDir.listFiles()) {
			String name = onto.getName();
//			String path = ontoDirPath + name + "/" + name + ".owl";
			String path = ontoDirPath + "/" + name + ".dlp";			
//		    Ontology o = new DefaultOntology(new OWL2Parser(new File(path)));
		    Ontology o = new DefaultOntology(new DlgpParser(new File(path)));
	    	
		    System.out.println(o.size());
		    
			AnalyserRuleSet analyserruleset = new AnalyserRuleSet(o);
			
			RuleSetProperty sticky = StickyProperty.instance();
			RuleSetProperty weaklyAC = WeaklyAcyclicProperty.instance();
			RuleSetProperty linear = LinearProperty.instance();
			RuleSetProperty AGRD = AGRDProperty.instance();
			RuleSetProperty domainRestrcited = DomainRestrictedProperty.instance();
			
			System.out.println(name + 
					" Sticky: " + sticky.check(analyserruleset) + 
					" Weakly Acyclic: " + weaklyAC.check(analyserruleset) +
					" Linear: " + linear.check(analyserruleset) +
					" AGRD: " + AGRD.check(analyserruleset) +
					" Domain Restricted: " + domainRestrcited.check(analyserruleset));
		}
//	}
}

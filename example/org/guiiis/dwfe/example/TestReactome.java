package org.guiiis.dwfe.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.guiiis.dwfe.core.DatalogRewriting;
import org.guiiis.dwfe.opt.Optimizier;
import org.guiiis.dwfe.utils.SimpleQueryFileReader;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.io.owl.OWL2Parser;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;

public class TestReactome {
	public static void main(String[] argv) throws Exception {
    	KBBuilder builder = new KBBuilder();
    	builder.addRules(new OWL2Parser(new File("./pagoda-bench/Reactome/Reactome.owl")));
    	KnowledgeBase kb = builder.build();
    	RuleSet rs = kb.getOntology();
    	
    	List<String> qs = SimpleQueryFileReader.read2(new File("./pagoda-bench/Reactome/test.sparql"));
    	
    	DatalogRewriting dr = new DatalogRewriting(rs);
    	
    	for(String q : qs) {
    		ConjunctiveQuery query =  new SparqlConjunctiveQueryParser(q).getConjunctiveQuery();
    		
    		List<Term> ansVar = new LinkedList<>();
    		Predicate G = new Predicate(DatalogRewriting.ANSPredicateIdentifier, 0);
    		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
    		Rule H = DefaultRuleFactory.instance().create(query.getAtomSet(), DefaultAtomSetFactory.instance().create(Ghead));
    		H.setLabel(String.valueOf(rs.size() + 1));
    		rs.add(H);
    		
        	Optimizier opt = new Optimizier(rs);
        	
        	IndexedByHeadPredicatesRuleSet irs = opt.getUnsolved();
        	
        	System.out.println(query + ": unsolved size " + irs.size());
    	}	
    	

	}
}

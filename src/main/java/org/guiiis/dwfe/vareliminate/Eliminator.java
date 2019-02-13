package org.guiiis.dwfe.vareliminate;

import java.util.HashSet;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByBodyPredicatesRuleSet;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Eliminator {
	private RuleSet rs = null;
	private IndexedByBodyPredicatesRuleSet ruleset = null;
	private BehaveChecker checker = null;
	
	public Eliminator(RuleSet _rs) throws IteratorException {
		this.rs = _rs;
		this.ruleset = new IndexedByBodyPredicatesRuleSet(rs);
		this.checker = new BehaveChecker(_rs);
	}
	
	public void elim() throws IteratorException, AtomSetException {
		for(Rule r : this.ruleset) {
			if(this.checker.isBehaveRule(r)) elim(r);
		}
	}
	
	public void elim(Rule r) {
		Rule tr = r;
		
		Set<Term> terms = new HashSet<>();
		For(Term t : r.getHead().getTerms()) {
			
		}
		
		for(Variable v : r.getExistentials()) {
			tr = elim(tr, v);
		}
	}
	
	private Rule elim(Rule r, Variable v) {
		
		return null;
	}
	
	public RuleSet getRuleSet() {
		return this.ruleset;
	}
}

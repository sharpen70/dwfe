package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.atomset.AtomSetUtils;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;
import fr.lirmm.graphik.util.Partition;

/**
 * Datalog rewriting Operator (DR)
 * Datalog rule rewriting engine that get rules by
 * applying one-step datalog rewriting according to
 * provided piece-based unification
 * 
 * @author sharpen
 */
public class DatalogRewritingOperator {
	
	public DatalogRewritingOperator() {
	
	}
	
	public RuleRewPair getRewriteFrom(DatalogRule r, QueryUnifier u) {
		Rule _r = u.getRule();
		InMemoryAtomSet piece = u.getPiece();
		Partition<Term> partition = u.getPartition();
		
		DefaultRuleFactory rulefactory = DefaultRuleFactory.instance();
		DefaultAtomFactory atomfactory = DefaultAtomFactory.instance();
		
		if(_r.getExistentials().isEmpty()) {
			DatalogRule dr = new DefaultDatalogRule(_r);
			return new RuleRewPair(dr);
		}
		else {
			Object identifier = _r.getLabel();
			int arity = _r.getFrontier().size();
			Predicate predicate = new Predicate(identifier, arity);
			List<Term> terms = new ArrayList<>(_r.getFrontier());
			
			InMemoryAtomSet head = new LinkedListAtomSet(atomfactory.create(predicate, terms));
			InMemoryAtomSet mbody = u.getImageOf(_r.getBody());
			InMemoryAtomSet mhead = u.getImageOf(head);
			
			DatalogRule r2 = new DefaultDatalogRule(mbody, mhead);
			r2.makeSafe();
			
			InMemoryAtomSet nbody = u.getImageOf(AtomSetUtils.minus(r.getBody(), u.getPiece()));
			InMemoryAtomSet nhead = u.getImageOf(r.getHead());
			
			DatalogRule r1 = new DefaultDatalogRule(nbody, nhead);
			r1.makeSafe();
		
			return new RuleRewPair(r1, r2);
		}
	}
	
	
}

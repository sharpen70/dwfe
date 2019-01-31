package org.guiiis.dwfe.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Utils {
	
	public static List<PredicatePosition> getPositionByVar(AtomSet atomset, Variable v) throws IteratorException {
		List<PredicatePosition> pos = new ArrayList<>();
		
		CloseableIterator<Atom> it = atomset.iterator();
		
		while(it.hasNext()) {
			Atom a = it.next();
			List<Term> terms = a.getTerms();
			
			for(int i = 0; i < terms.size(); i++) {
				if(terms.get(i).equals(v)) pos.add(new PredicatePosition(a.getPredicate(), i));
			}
		}
		
		return pos;
	}
	
	public static Set<Variable> getVarByPosition(AtomSet atomset, PredicatePosition p) throws IteratorException {
		Set<Variable> vs = new HashSet<>();
		
		CloseableIterator<Atom> it = atomset.iterator();
		
		while(it.hasNext()) {
			Atom a = it.next();
			if(a.getPredicate().equals(p.predicate)) {
				Term t = a.getTerm(p.position);
				if(t.isVariable()) vs.add((Variable)t);					
			}
		}
		return vs;
	}
	
	public static List<PredicatePosition> getAffectedPosition(Rule r, PredicatePosition p) throws IteratorException {
		AtomSet body = r.getBody();
		
		CloseableIterator<Atom> it = body.iterator();
		Set<Variable> affectedVariable = new HashSet<>();
		
		while(it.hasNext()) {
			Atom a = it.next();
			if(a.getPredicate().equals(p.predicate)) {
				Term t = a.getTerm(p.position);
				if(t.isVariable()) affectedVariable.add((Variable)t);
			}
		}
		
		List<PredicatePosition> affectedPosition = new ArrayList<>();
		for(Variable v : affectedVariable) affectedPosition.addAll(getPositionByVar(r.getHead(), v));
		
		return affectedPosition;
	}
}

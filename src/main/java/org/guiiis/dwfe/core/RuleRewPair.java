package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.core.atomset.AtomSetUtils;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;

public class RuleRewPair {
	private DatalogRule r_tail;
	private DatalogRule r_up;
	private boolean origin;
	
	public RuleRewPair(DatalogRule r_tail, DatalogRule r_up, boolean origin) {
		this.r_tail = r_tail;
		this.r_up = r_up;
		this.origin = origin;
	}
	
	public Collection<DatalogRule> getRules() {
		List<DatalogRule> rs = new ArrayList<>();
		
		if(!this.origin) rs.add(this.r_up);
		rs.add(this.r_tail);
		
		return rs;
	}
	
	public String toString() {
		String s = "";
		
		if(this.r_up != null) s = this.r_up.toString() + "  ";
		
		s += this.r_tail.toString();
		
		return s;
	}
	
	public void replace(DatalogRule r) {
		this.r_tail = r;
	}

	public DatalogRule suits(QueryUnifier u) {
		InMemoryAtomSet b = u.getImageOf(u.getPiece());
		
		if(AtomSetUtils.contains(u.getImageOf(this.r_tail.getBody()), b)) return this.r_tail;
		else {
			DatalogRule ur = unfold();
			if(AtomSetUtils.contains(u.getImageOf(ur.getBody()), b)) return ur;
			else return null;
		}
	}

	public DatalogRule unfold() {
		InMemoryAtomSet mbody = this.r_up.getBody();
		mbody = AtomSetUtils.minus(mbody, this.r_tail.getHead());
		mbody = AtomSetUtils.union(mbody, this.r_tail.getBody());
		
		return new DefaultDatalogRule(mbody, this.r_up.getHead());
	}
}

package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.core.atomset.AtomSetUtils;

enum Type {
	ORIGIN, SEP
}

public class RuleRewPair {
	private DatalogRule r1;
	private DatalogRule r2;
	
	private Type type;
	
	private boolean origin;
	
	public RuleRewPair(DatalogRule r) {
		r1 = r;
		origin = true;
	}
	
	public RuleRewPair(DatalogRule r1, DatalogRule r2, Type type) {
		this.r1 = r1;
		this.r2 = r2;
		this.origin = false;
		this.type = type;
	}
	
	public boolean origin() {
		return true;
	}
	
	public Collection<DatalogRule> getRules() {
		List<DatalogRule> rs = new ArrayList<>();
		rs.add(r1);
		if(!origin) rs.add(r2);
		
		return rs;
	}
	
	public String toString() {
		String s = r1.toString();
		if(!origin) s = "\n" + r2.toString();
		
		return s;
	}
	
	public void replace(DatalogRule r) {
		this.r1 = r;
	}

	public DatalogRule contains(InMemoryAtomSet b) {
		if(AtomSetUtils.contains(r1.getBody(), b)) return r1;
		if(!this.origin)
			if(AtomSetUtils.contains(r2.getBody(), b)) return r2;
		
		return null;
	}

	public DatalogRule unfold() {
		if(this.type == Type.ORIGIN) return r1;
		
		InMemoryAtomSet mbody = r1.getBody();
		mbody = AtomSetUtils.minus(mbody, r2.getHead());
		mbody = AtomSetUtils.union(mbody, r2.getBody());
		
		return new DefaultDatalogRule(r1.getHead(), r2.getBody());
	}
}

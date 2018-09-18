package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;

public class RuleRewPair {
	private DatalogRule r1;
	private DatalogRule r2;
	
	private boolean origin;
	
	public RuleRewPair(DatalogRule r) {
		r1 = r;
		origin = true;
	}
	
	public RuleRewPair(DatalogRule r1, DatalogRule r2) {
		this.r1 = r1;
		this.r2 = r2;
		origin = false;
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

	public void replace(DatalogRule r) {
		// TODO Auto-generated method stub
		
	}

	public DatalogRule contains(InMemoryAtomSet b) {
		// TODO Auto-generated method stub
		return null;
	}

	public DatalogRule unfold() {
		// TODO Auto-generated method stub
		return null;
	}
}

package org.guiiis.dwfe.core;

import java.io.IOException;
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
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.util.Partition;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;

/**
 * Datalog rewriting Operator (DR)
 * Datalog rule rewriting engine that get rules by
 * applying one-step datalog rewriting according to
 * provided piece-based unification
 * 
 * @author sharpen
 */
public class DatalogRewritingOperator implements Profilable {
	private Profiler profiler;
	
	public DatalogRewritingOperator() {
	
	}
	
	public RuleRewPair getRewriteFrom(DatalogRule r, ExtendedQueryUnifier eu) {
		QueryUnifier u = eu.getUnifier();
		Rule _r = u.getRule();
		
		DefaultAtomFactory atomfactory = DefaultAtomFactory.instance();
		
		if(_r.getExistentials().isEmpty()) {
			InMemoryAtomSet nbody = u.getImageOf(_r.getBody());
			InMemoryAtomSet nhead = u.getImageOf(_r.getHead());
			DatalogRule dr = new DefaultDatalogRule(nbody, nhead);	
			return new RuleRewPair(dr);
		}
		else {
			String identifier = eu.getRuleLabel() + "_AGG" + eu.getAgg();
			int arity = _r.getFrontier().size();
			Predicate predicate = new Predicate(identifier, arity);
			List<Term> terms = new ArrayList<>(_r.getFrontier());
			
			InMemoryAtomSet head = new LinkedListAtomSet(atomfactory.create(predicate, terms));
			InMemoryAtomSet mbody = u.getImageOf(_r.getBody());
			InMemoryAtomSet mhead = u.getImageOf(head);
			
			DatalogRule r2 = new DefaultDatalogRule(mbody, mhead);
			r2.makeSafe();
			
			InMemoryAtomSet nbody = u.getImageOf(r.getBody());
			InMemoryAtomSet npiece = u.getImageOf(u.getPiece());
			
//			DlgpWriter writer = new DlgpWriter();
//			try {
//				writer.write(nbody);
//				writer.write(u.getPiece());
//				writer.write(npiece);
//				writer.flush();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			nbody = AtomSetUtils.minus(nbody, npiece);
			nbody = AtomSetUtils.union(nbody, mhead);
			
			InMemoryAtomSet nhead = u.getImageOf(r.getHead());
			
			DatalogRule r1 = new DefaultDatalogRule(nbody, nhead);
			r1.makeSafe();
		
			return new RuleRewPair(r1, r2);
		}
	}

	@Override
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	@Override
	public Profiler getProfiler() {
		return this.profiler;
	}
}

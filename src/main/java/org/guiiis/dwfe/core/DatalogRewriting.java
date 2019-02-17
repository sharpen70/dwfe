package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.guiiis.dwfe.core.graal.PureQuery;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.util.profiler.NoProfiler;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

/**
 * Non-recursive datalog rewriting for existential rules
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public class DatalogRewriting implements Profilable {
	private Profiler profiler = NoProfiler.instance();
	
	private ExtendedSRA           		operator;
	private DatalogRewritingOperator    dlgoperator;
	private RuleSet onto;
	private IndexedByHeadPredicatesRuleSet focus;
	
	public DatalogRewriting(RuleSet _onto) {
		this.operator = new ExtendedSRA();
		this.onto = _onto;
		this.dlgoperator = new DatalogRewritingOperator();
		this.focus = new IndexedByHeadPredicatesRuleSet();
		
		setLabel(this.onto);
	}
	
	public Collection<DatalogRule> exec(ConjunctiveQuery q) {
		if (this.getProfiler() != null && this.getProfiler().isProfilingEnabled()) {
			this.getProfiler().trace(q.getLabel());
		}
		
		// rewriting
		DatalogRewritingAlgorithm algo = new DatalogRewritingAlgorithm(this.dlgoperator, this.operator);

		this.operator.setProfiler(this.profiler);
		this.dlgoperator.setProfiler(this.profiler);
		
		algo.setProfiler(this.getProfiler());

		return algo.exec(q, this.focus);
	}
	
	private IndexedByHeadPredicatesRuleSet focus(PureQuery q) {
		IndexedByHeadPredicatesRuleSet indexedRuleSet = new IndexedByHeadPredicatesRuleSet(onto);
		IndexedByHeadPredicatesRuleSet re = new IndexedByHeadPredicatesRuleSet();
		Set<Integer> labels = new HashSet<>();
		
		CloseableIteratorWithoutException<Atom> it = q.getAtomSet().iterator();
		Queue<CloseableIteratorWithoutException<Atom>> queue = new LinkedList<>();
		queue.add(it);
		
		while(!queue.isEmpty()) {
			CloseableIteratorWithoutException<Atom> _it = queue.poll();
			
			while(_it.hasNext()) {
				Atom a = it.next();
				for(Rule r : indexedRuleSet.getRulesByHeadPredicate(a.getPredicate())) {
					if(labels.add(Integer.valueOf(r.getLabel()))) {
						re.add(r);
						queue.add(r.getBody().iterator());
					}
				}
			}
		}
		
		return re;
	}
	
	/** Add label to each rule in the ontology if not exist **/
	private void setLabel(RuleSet rs) {
		int i = 0;
		
		for(Rule r: rs) {
			r.setLabel(String.valueOf(i));
			i++;
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

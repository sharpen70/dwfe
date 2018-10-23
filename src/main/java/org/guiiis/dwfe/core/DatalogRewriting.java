package org.guiiis.dwfe.core;

import java.util.Collection;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.profiler.NoProfiler;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;

/**
 * Non-recursive datalog rewriting for existential rules
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public class DatalogRewriting implements Profilable {
	private Profiler profiler = NoProfiler.instance();
	
	private ExtendedSRA           		operator;
	private DatalogRewritingOperator    dlgoperator;
	
	public DatalogRewriting() {
		this.operator = new ExtendedSRA();
		this.dlgoperator = new DatalogRewritingOperator();
	}
	
	public Collection<DatalogRule> exec(ConjunctiveQuery q, RuleSet onto) {
		if (this.getProfiler() != null && this.getProfiler().isProfilingEnabled()) {
			this.getProfiler().trace(q.getLabel());
		}
		
		RuleSet rs = new LinkedListRuleSet(Rules.computeSinglePiece(onto.iterator()));
		addLabel(rs);
		
		IndexedByHeadPredicatesRuleSet indexedRuleSet = new IndexedByHeadPredicatesRuleSet(rs);

		// rewriting
		DatalogRewritingAlgorithm algo = new DatalogRewritingAlgorithm(this.dlgoperator, this.operator);

		this.operator.setProfiler(this.profiler);
		this.dlgoperator.setProfiler(this.profiler);
		
		algo.setProfiler(this.getProfiler());

		return algo.exec(q, indexedRuleSet);
	}
	
	/** Add label to each rule in the ontology if not exist **/
	private void addLabel(RuleSet rs) {
		int i = 0;
		
		for(Rule r: rs) {
			r.setLabel("R" + i);
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

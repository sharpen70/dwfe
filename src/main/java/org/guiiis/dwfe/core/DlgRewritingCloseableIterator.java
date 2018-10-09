package org.guiiis.dwfe.core;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.profiler.NoProfiler;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorAdapter;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class DlgRewritingCloseableIterator implements CloseableIterator<DatalogRule>, Profilable {
	private PureQuery                   pquery;
	private LinkedListRuleSet           ruleset;
	private CloseableIteratorWithoutException<DatalogRule> rewrites = null;

	private ExtendedSRA           		operator;
	private DatalogRewritingOperator    dlgoperator;
	private Profiler                    profiler  = NoProfiler.instance();
	
	public DlgRewritingCloseableIterator(ConjunctiveQuery q, Iterable<Rule> ruleset) {
		this.pquery = new PureQuery(q);
		this.ruleset = new LinkedListRuleSet(ruleset.iterator());
		this.operator = new ExtendedSRA();
		this.dlgoperator = new DatalogRewritingOperator();
	}
	
	@Override
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	@Override
	public Profiler getProfiler() {
		return this.profiler;
	}

	@Override
	public boolean hasNext() throws IteratorException {
		if (this.rewrites == null) {
			this.compute();
		}
		return this.rewrites.hasNext();
	}

	@Override
	public DatalogRule next() throws IteratorException {
		if (this.rewrites == null) {
			this.compute();
		}
		DatalogRule r = this.rewrites.next();
		return r;
	}

	@Override
	public void close() {
	}
	
	private void compute() {
		if (this.getProfiler() != null && this.getProfiler().isProfilingEnabled()) {
			this.getProfiler().trace(this.pquery.getLabel());
		}
		IndexedByHeadPredicatesRuleSet indexedRuleSet = new IndexedByHeadPredicatesRuleSet(this.ruleset);

		// rewriting
		DatalogRewritingAlgorithm algo = new DatalogRewritingAlgorithm(this.dlgoperator, this.operator);

		operator.setProfiler(this.getProfiler());
		algo.setProfiler(this.getProfiler());

		Iterable<DatalogRule> dlgP = algo.exec(pquery, indexedRuleSet);
	
		this.rewrites = new CloseableIteratorAdapter<DatalogRule>(dlgP.iterator());
	}
}

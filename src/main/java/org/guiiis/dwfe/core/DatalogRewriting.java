package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.guiiis.dwfe.core.graal.PureQuery;
import org.guiiis.dwfe.opt.Optimizier;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
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
	
	public final static String ANSPredicateIdentifier = "guiiis.dwfe:ANS";
	
	private ExtendedSRA           		operator;
	private DatalogRewritingOperator    dlgoperator;
	private RuleSet onto;
	
	public DatalogRewriting(RuleSet _onto) {
		this.operator = new ExtendedSRA();
		this.onto = _onto;
		this.dlgoperator = new DatalogRewritingOperator();
		
		setLabel(this.onto);
	}
	
	public List<DatalogRule> pexec(ConjunctiveQuery q) throws Exception {
		if (this.getProfiler() != null && this.getProfiler().isProfilingEnabled()) {
			this.getProfiler().trace(q.getLabel());
		}
		
		PureQuery pquery = new PureQuery(q);
		
		RuleSet re = this.focus(pquery);
		
		List<Term> ansVar = new LinkedList<>();
		Predicate G = new Predicate(ANSPredicateIdentifier, 0);
		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
		InMemoryAtomSet Gheads = DefaultAtomSetFactory.instance().create(Ghead);
		Rule H = DefaultRuleFactory.instance().create(q.getAtomSet(), Gheads);
		H.setLabel(String.valueOf(re.size() + 1));
		re.add(H);
		
		Optimizier opt = new Optimizier(re);
		
		IndexedByHeadPredicatesRuleSet unsolved = opt.getUnsolved();
		
		for(Rule r :unsolved) System.out.println(r);
		
		IDCompilation ruleCompilation = new IDCompilation();
		List<Rule> compiledrule = new LinkedList<>();
		
		for(Rule r : unsolved) {
			if(ruleCompilation.isCompilable(r)) compiledrule.add(r);
		}
		
		ruleCompilation.compile(unsolved.iterator());
		
		// checking
		AnalyserRuleSet analyserruleset = new AnalyserRuleSet(unsolved);
		Analyser analyser = new Analyser(analyserruleset);
		
		Map<String, RuleSetProperty> properties = new HashMap<>();
		properties.putAll(RuleSetPropertyHierarchy.generatePropertyMapSpecializationOf(FUSProperty.instance()));
		analyser.setProperties(properties.values());
		if(!analyser.isDecidable())	{
			System.out.println("Not Rewritable!");
			return null;
		}
		
		PureQuery newq = new PureQuery(Gheads, new LinkedList<>());
		
		// further rewriting
		DatalogRewritingAlgorithm algo = new DatalogRewritingAlgorithm(this.dlgoperator, this.operator, ruleCompilation);
		
		algo.setProfiler(this.getProfiler());

		Set<DatalogRule> ba =  algo.pexec(newq, unsolved);
		
		List<DatalogRule> result = new LinkedList<>();
		
		result.addAll(ba);
		
		for(Rule r : opt.getSolved()) result.add(new DefaultDatalogRule(r));
		for(Rule r : compiledrule) result.add(new DefaultDatalogRule(r));
		
		return result;
	}
	
	public Collection<DatalogRule> exec(ConjunctiveQuery q) throws Exception {
		if (this.getProfiler() != null && this.getProfiler().isProfilingEnabled()) {
			this.getProfiler().trace(q.getLabel());
		}		
		
		// rewriting
		DatalogRewritingAlgorithm algo = new DatalogRewritingAlgorithm(this.dlgoperator, this.operator);
		
		algo.setProfiler(this.getProfiler());
		
		return algo.exec(q, new IndexedByHeadPredicatesRuleSet(this.onto));
	}
	
	private RuleSet focus(PureQuery q) {
		IndexedByHeadPredicatesRuleSet indexedRuleSet = new IndexedByHeadPredicatesRuleSet(onto);
		RuleSet re = new LinkedListRuleSet();
		
		Set<Integer> labels = new HashSet<>();
		
		CloseableIteratorWithoutException<Atom> it = q.getAtomSet().iterator();
		Queue<CloseableIteratorWithoutException<Atom>> queue = new LinkedList<>();
		queue.add(it);
		
		while(!queue.isEmpty()) {
			CloseableIteratorWithoutException<Atom> _it = queue.poll();
			
			while(_it.hasNext()) {
				Atom a = _it.next();
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
		int i = 1;
		
		for(Rule r: rs) {
			r.setLabel(String.valueOf(i));  
			i++;
		}
	}

	@Override
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
		this.operator.setProfiler(this.profiler);
		this.dlgoperator.setProfiler(this.profiler);
	}

	@Override
	public Profiler getProfiler() {
		return this.profiler;
	}	
}

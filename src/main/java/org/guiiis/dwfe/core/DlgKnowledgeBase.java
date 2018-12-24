package org.guiiis.dwfe.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.guiiis.dwfe.utils.FUSAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.backward_chaining.pure.AggregSingleRuleOperator;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import fr.lirmm.graphik.util.stream.IteratorException;


public class DlgKnowledgeBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(DlgKnowledgeBase.class);
	
	private PrintStream profileSteam = System.out;
	private Boolean decidable = null;
	
	private final RuleSet ruleset;
	private final AtomSet store;
	
	private AnalyserRuleSet analyzerRuleSet = null;
	private Analyser analyzer = null;
	private FUSAnalyser fusAnalyzer = null;
	
	private RuleSet fusComponent = null;
	private boolean force_rewriting = true;
	
	public DlgKnowledgeBase(Parser<Object> parser) throws AtomSetException {
		this.store = new DefaultInMemoryGraphStore();
		
		RuleSet rs = new LinkedListRuleSet();
		
		Object o;
		try {
			while (parser.hasNext()) {
				o = parser.next();
				if (o instanceof Rule) {
					rs.add((Rule) o);
				} else if (o instanceof Atom) {
					this.store.add((Atom) o);
				}
			}
		} catch (IteratorException e) {
			throw new AtomSetException(e);
		}
		
		this.ruleset = new LinkedListRuleSet(Rules.computeSinglePiece(rs.iterator()));
		this.analyzerRuleSet = new AnalyserRuleSet(this.ruleset);
		this.analyzer = new Analyser(this.analyzerRuleSet);
		this.fusAnalyzer = new FUSAnalyser(this.analyzerRuleSet);
	}
	
	public Collection<DatalogRule> rewriteToDlg(ConjunctiveQuery q) {
		DatalogRewriting dr = new DatalogRewriting();			
		dr.setProfiler(new RealTimeProfiler(profileSteam));
		
		if(this.isDecidable()) {
			return dr.exec(q, this.ruleset);	
		}		
		else if(this.force_rewriting) {
			if(this.fusComponent == null) {
				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
			//	System.out.println(this.fusComponent);
			}
			return dr.exec(q, this.fusComponent);
		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
			return null;
		}		
	}
	
	public Collection<ConjunctiveQuery> rewriteToUCQ(ConjunctiveQuery q) {
//		RulesCompilation compilation = new IDCompilation();
//		compilation.compile(ontology.iterator());
//		
		AggregSingleRuleOperator operator = new AggregSingleRuleOperator();
		Profiler p = new RealTimeProfiler(profileSteam);
		RewritingAlgorithm algo = new RewritingAlgorithm(operator);
		operator.setProfiler(p);
		algo.setProfiler(p);
		
		if(this.isDecidable()) {
			return algo.execute(q, new IndexedByHeadPredicatesRuleSet(this.ruleset), NoCompilation.instance());		
		}		
		else if(this.force_rewriting) {
			if(this.fusComponent == null) {
				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
			//	System.out.println(this.fusComponent);
			}
			return algo.execute(q, new IndexedByHeadPredicatesRuleSet(this.fusComponent), NoCompilation.instance());
		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
			return null;
		}
	}
	
	protected Boolean isDecidable() {
		if(this.decidable != null) return decidable;
		else {					
			Map<String, RuleSetProperty> properties = new HashMap<>();
			properties.putAll(RuleSetPropertyHierarchy.generatePropertyMapSpecializationOf(FUSProperty.instance()));
			this.analyzer.setProperties(properties.values());
			this.decidable = this.analyzer.isDecidable();			
			return decidable;
		}
	}
	
	public RuleSet getOntology() {
		return this.ruleset;
	}
	
	public void setProfiling(PrintStream o) {
		profileSteam = o;

	}
	
	public void close() {
		if (this.store instanceof Closeable) {
			try {
				((Closeable) this.store).close();
			} catch (IOException e) {
				LOGGER.warn("Error while closing KnowledgeBase: ", e);
			}
		}
	}
}

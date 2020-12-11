package org.guiiis.dwfe.core;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.guiiis.dwfe.core.graal.RewritingAlgorithm;
import org.guiiis.dwfe.io.SparqlUnionOfConjunctiveQueryWriter;
import org.guiiis.dwfe.utils.FUSAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Constant;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Literal;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.TermGenerator;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.api.core.Term.Type;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.backward_chaining.pure.AggregAllRulesOperator;
import fr.lirmm.graphik.graal.backward_chaining.pure.AggregSingleRuleOperator;
import fr.lirmm.graphik.graal.backward_chaining.pure.RewritingOperator;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.core.DefaultUnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.profiler.Profiler;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;


public class DlgKnowledgeBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(DlgKnowledgeBase.class);
	
	private PrintStream profileSteam = System.out;
	
	private Boolean decidable = null;
	
	private RuleSet ruleset;
//	private final AtomSet store;
	
	private AnalyserRuleSet analyzerRuleSet = null;
	private Analyser analyzer = null;
	private FUSAnalyser fusAnalyzer = null;
	
	private RuleSet fusComponent = null;
	private boolean force_rewriting = true;
	
	private IndexedByHeadPredicatesRuleSet compiledrule;
	private RulesCompilation rulesCompilation = NoCompilation.instance();
	private boolean compiled = false;
	
	public DlgKnowledgeBase(Parser<Object> parser) throws AtomSetException {
//		this.store = new DefaultInMemoryGraphStore();
		this.ruleset = new LinkedListRuleSet();
		Object o;
		try {
			while (parser.hasNext()) {
				o = parser.next();
				if (o instanceof Rule) {
					this.ruleset.add((Rule) o);
				} 
//				else if (o instanceof Atom) {
//					this.store.add((Atom) o);
//				}
			}
		} catch (IteratorException e) {
			throw new AtomSetException(e);
		}
		
		this.init();
	}
	
	public DlgKnowledgeBase(RuleSet rs) {
		this.ruleset = rs;
		this.init();
	}
	
	public void init() {
		this.ruleset = new LinkedListRuleSet(Rules.computeSinglePiece(this.ruleset.iterator()));
		this.compiledrule = new IndexedByHeadPredicatesRuleSet();
	}
	
	public void rewriteToDlg(ConjunctiveQuery q, PrintStream outputStream) throws Exception {
		DatalogRewriting dr = new DatalogRewriting(this.ruleset);			
		dr.setProfiler(new RealTimeProfiler(profileSteam));
		
	//	outputStream.println("Query:\n" + q);
	//	outputStream.println("\nRewriting Result:\n");
		
		if(this.isDecidable()) {
			Collection<DatalogRule> re = dr.oexec(q);
			for(DatalogRule r : re) outputStream.println(r.toRDFox());
		}		
//		else if(this.force_rewriting) {
//			if(this.fusComponent == null) {
//				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
//			//	System.out.println(this.fusComponent);
//			}
//			Collection<DatalogRule> re = dr.exec(q, this.fusComponent);
//			for(DatalogRule r : re) outputStream.println(r.toRDFox());
//		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
		}
	}
	
//	public void rewriteToDlg(ConjunctiveQuery q, PrintStream outputStream, PrintStream queryStream) throws IOException {
//		DatalogRewriting dr = new DatalogRewriting();			
//		dr.setProfiler(new RealTimeProfiler(profileSteam));
//		
//	//	outputStream.println("Query:\n" + q);
//	//	outputStream.println("\nRewriting Result:\n");
//		
//		List<Term> ansVar = q.getAnswerVariables();
//		Predicate ANS = new Predicate(DatalogRewritingAlgorithm.ANSPredicateIdentifier, ansVar.size());
//		Atom a = DefaultAtomFactory.instance().create(ANS, ansVar);
//		ConjunctiveQuery ansQuery = new DefaultConjunctiveQuery(DefaultAtomSetFactory.instance().create(a));
//		
//		SparqlConjunctiveQueryWriter writer = new SparqlConjunctiveQueryWriter(queryStream);
//		
//		if(this.isDecidable()) {
//			Collection<DatalogRule> re = dr.exec(q, this.ruleset);
//			for(DatalogRule r : re) outputStream.println(r.toRDFox());
//			writer.write(ansQuery);
//		}		
//		else if(this.force_rewriting) {
//			if(this.fusComponent == null) {
//				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
//			//	System.out.println(this.fusComponent);
//			}
//			Collection<DatalogRule> re = dr.exec(q, this.fusComponent);
//			for(DatalogRule r : re) outputStream.println(r);
//			writer.write(ansQuery);
//		}
//		else {
//			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
//		}
//		
//		writer.close();
//	}
	
	public void rewriteToUCQ(ConjunctiveQuery q, PrintStream outputStream) throws IOException {
//		RulesCompilation compilation = new IDCompilation();
//		compilation.compile(ontology.iterator());
//		
		AggregSingleRuleOperator operator = new AggregSingleRuleOperator();
//		RewritingOperator operator = new AggregAllRulesOperator();

		Profiler p = new RealTimeProfiler(profileSteam);
		RewritingAlgorithm algo = new RewritingAlgorithm(operator);
		operator.setProfiler(p);
		algo.setProfiler(p);
		
		SparqlUnionOfConjunctiveQueryWriter writer = new SparqlUnionOfConjunctiveQueryWriter(outputStream);
		
//		outputStream.println("Query:\n" + q);
//		outputStream.println("\nRewriting Result:\n");
		
		if(this.isDecidable()) {	
			UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(q.getAnswerVariables(), 
					algo.execute(q, new IndexedByHeadPredicatesRuleSet(this.ruleset), NoCompilation.instance()));
			
		//	writer.write(ucq);
		}		
		else if(this.force_rewriting) {
			if(this.fusComponent == null) {
				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
			//	System.out.println(this.fusComponent);
			}
			UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(q.getAnswerVariables(), 
					algo.execute(q, new IndexedByHeadPredicatesRuleSet(this.fusComponent), NoCompilation.instance()));
			
		//	writer.write(ucq);	
		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
		}
		
		writer.close();
	}
	
	public Collection<ConjunctiveQuery> rewriteToUCQWithComp(ConjunctiveQuery q) throws IOException {
//		this.complileRule();
//		
		AggregSingleRuleOperator operator = new AggregSingleRuleOperator();
//		RewritingOperator operator = new AggregAllRulesOperator();

		Profiler p = new RealTimeProfiler(profileSteam);
		RewritingAlgorithm algo = new RewritingAlgorithm(operator);
		operator.setProfiler(p);
		algo.setProfiler(p);
		
	
		Collection<ConjunctiveQuery> qs = algo.execute(q, 
					new IndexedByHeadPredicatesRuleSet(this.ruleset), this.rulesCompilation);
			
//			for(ConjunctiveQuery cq : qs) {
//				Predicate hp = new Predicate("ANS", cq.getAnswerVariables().size());
//				Atom h = new DefaultAtom(hp, cq.getAnswerVariables());
//				InMemoryAtomSet head = new LinkedListAtomSet();
//				head.add(h);
//				Rule r = new DefaultRule(cq.getAtomSet(), head);
//				
//				rewriting.add(r);
//			}
//			rewriting.addAll(this.compiledrule);
//			
//			return rewriting;		
		return qs;

	}
	
	public Boolean isDecidable() {
		if(this.decidable != null) return decidable;
		else {				
			this.analyzerRuleSet = new AnalyserRuleSet(this.ruleset);
			this.analyzer = new Analyser(this.analyzerRuleSet);
//			this.fusAnalyzer = new FUSAnalyser(this.analyzerRuleSet);
			
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
	
	public RuleSet getFusComponent() {
		if(this.isDecidable()) return this.ruleset;
		
		if(this.fusComponent == null) {
			this.fusComponent = this.fusAnalyzer.maximalFusComponent();
		}
		
		return this.fusComponent;
	}
	
	public void setProfiling(PrintStream o) {
		this.profileSteam = o;

	}
	
	public void complileRule() {
		if(this.compiled) return;
		else {
			this.compiled = true;
			this.rulesCompilation = new IDCompilation();
			
			for(Rule r : this.ruleset) {
				if(this.rulesCompilation.isCompilable(r)) this.compiledrule.add(r);
			}		
			
			this.rulesCompilation.compile(this.ruleset.iterator());
		}
	}
	
	public IndexedByHeadPredicatesRuleSet getCompliedRule() {
		return this.compiledrule;
	}
//	public void close() {
//		if (this.store instanceof Closeable) {
//			try {
//				((Closeable) this.store).close();
//			} catch (IOException e) {
//				LOGGER.warn("Error while closing KnowledgeBase: ", e);
//			}
//		}
//	}
}

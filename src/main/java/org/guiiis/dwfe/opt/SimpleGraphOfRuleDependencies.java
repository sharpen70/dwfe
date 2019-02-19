package org.guiiis.dwfe.opt;

import java.util.Collections;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.unifier.DependencyChecker;
import fr.lirmm.graphik.graal.core.Substitutions;
import fr.lirmm.graphik.graal.core.grd.DefaultGraphOfRuleDependencies;

public class SimpleGraphOfRuleDependencies extends DefaultGraphOfRuleDependencies {
	public SimpleGraphOfRuleDependencies(Iterable<Rule> rules) {
		super(rules);
	}
	
	@Override
	protected Set<Substitution> computeDependency(Rule r1, Rule r2, DependencyChecker... checkers) {
		return Collections.<Substitution>singleton(Substitutions.emptySubstitution());
	}
}

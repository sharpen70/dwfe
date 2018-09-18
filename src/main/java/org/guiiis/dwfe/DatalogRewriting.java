package org.guiiis.dwfe;

import java.util.Set;

import org.guiiis.dwfe.core.DatalogRule;
import org.guiiis.dwfe.core.DlgRewritingCloseableIterator;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.core.Rules;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;

/**
 * Non-recursive datalog rewriting for existential rules
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public class DatalogRewriting {
	public DlgRewritingCloseableIterator exec(ConjunctiveQuery q, KnowledgeBase kb) {
		RuleSet rs = new LinkedListRuleSet(Rules.computeSinglePiece(kb.getOntology().iterator()));
		addLabel(rs);
		
		DlgRewritingCloseableIterator it = new DlgRewritingCloseableIterator(q, rs);

		return it;
	}
	
	/** Add label to each rule in the ontology if not exist **/
	private void addLabel(RuleSet rs) {
		int i = 0;
		
		for(Rule r: rs) {
			r.setLabel("rule_" + i);
			i++;
		}
	}
	
	
}

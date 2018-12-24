package org.guiiis.dwfe.io;

import java.util.Collection;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryWriter;

public class SparqlUnionOfConjunctiveQueryWriter extends SparqlConjunctiveQueryWriter {
	public SparqlUnionOfConjunctiveQueryWriter write(Collection<ConjunctiveQuery> ucq) {
		
		return this;
	}
}

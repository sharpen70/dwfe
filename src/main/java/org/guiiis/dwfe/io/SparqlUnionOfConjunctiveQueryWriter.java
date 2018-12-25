package org.guiiis.dwfe.io;

import java.io.IOException;
import java.io.OutputStream;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryWriter;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

public class SparqlUnionOfConjunctiveQueryWriter extends SparqlConjunctiveQueryWriter {
	public SparqlUnionOfConjunctiveQueryWriter(OutputStream o) {
		super(o);
	}
	
	public SparqlUnionOfConjunctiveQueryWriter write(UnionOfConjunctiveQueries ucq) throws IOException {
		this.write("SELECT DISTINCT ");
		
		for(Term t : ucq.getAnswerVariables()) {
			this.write(t);
			this.write(' ');
		}

		this.write("\nWHERE {\n");
		
		CloseableIterator<ConjunctiveQuery> uit = ucq.iterator();
		
		boolean ufirst = true;
		while(uit.hasNext()) {
			ConjunctiveQuery query = uit.next();
			
			if(!ufirst) this.write("\nUNION\n");
			else ufirst = false;
			
			this.write("{ ");
			
			boolean isFirst = true;
			CloseableIteratorWithoutException<Atom> it = query.getAtomSet().iterator();
			
			while (it.hasNext()) {
				Atom a = it.next();
				if(!isFirst) this.write(" . ");
				else isFirst = false;

				this.writeAtom(a);
			}
			this.write(" }");
		}
	
		this.write("\n}\n");

		return this;
	}
}

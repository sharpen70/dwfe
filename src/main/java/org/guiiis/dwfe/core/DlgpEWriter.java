package org.guiiis.dwfe.core;

import java.io.IOException;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;

public class DlgpEWriter extends DlgpWriter {
	public DlgpWriter write(DatalogRule rule) throws IOException {
		this.writeLabel(rule.getLabel());

		this.writeAtomSet(rule.getHead().iterator(), false);
		this.write(" :- ");
		this.writeAtomSet(rule.getBody().iterator(), false);
		this.write(".\n");

		return this;
	}
}

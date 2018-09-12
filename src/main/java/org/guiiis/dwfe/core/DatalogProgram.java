package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The class for datalog program
 * Consists of datalog rules
 * 
 * @author sharpen
 */
public class DatalogProgram {
	private List<DatalogRule> rules;
	
	public DatalogProgram() {
		rules = new ArrayList<DatalogRule>();
	}
	
	public void add(DatalogRule r) {
		
	}
	
	boolean isRecursive() {
		return false;
	}
}

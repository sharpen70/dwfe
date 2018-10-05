package org.guiiis.dwfe.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;

public class SimpleQueryFileReader {
	public static Collection<String> read(File f) throws FileNotFoundException {
		Scanner scanner = new Scanner(f);
		Collection<String> sep = new ArrayList<>();
		Collection<String> re = new ArrayList<>();
		String prefix = "";
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			if(line.startsWith("@")) {
				prefix += line + "\n";
			} else if(!line.isEmpty()) {
				sep.add(line);
			}
		}
		
		for(String s : sep) {
			re.add(prefix + "\n" + s); 			
		}
		scanner.close();
		
		return re;
	}
}

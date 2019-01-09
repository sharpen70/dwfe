package org.guiiis.dwfe.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class SimpleQueryFileReader {
	public static List<String> read(File f, int syntax) throws FileNotFoundException {
		return syntax == 0 ? read(f) : read2(f);
	}
	
	public static ArrayList<String> read(File f) throws FileNotFoundException {
		Scanner scanner = new Scanner(f);
		Collection<String> sep = new ArrayList<>();
		ArrayList<String> re = new ArrayList<>();
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
	
	public static ArrayList<String> read2(File f) throws FileNotFoundException {
		Scanner scanner = new Scanner(f);
		ArrayList<String> re = new ArrayList<>();
		String s = "";
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			if(line.isEmpty()) continue;
			if(line.startsWith("^")) {
				if(!s.isEmpty()) re.add(s);
				s = "";
			}
			else {
				s += line + "\n";
			}
		}
		
		re.add(s);
		
		scanner.close();	
		return re;
	}
}

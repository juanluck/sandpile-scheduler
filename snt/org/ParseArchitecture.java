package org;

import org.config.Configuration;
import org.config.LoadProperties;

public class ParseArchitecture {

	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args);
		Configuration.setConfiguration(lp);
		
		
		
	}
}

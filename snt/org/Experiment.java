package org;

import org.config.Configuration;
import org.config.LoadProperties;
import org.factories.factoryExperiment;

public class Experiment{

	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args);
		Configuration.setConfiguration(lp);
		
		factoryExperiment.createExperiment().run();
	}	
 
}

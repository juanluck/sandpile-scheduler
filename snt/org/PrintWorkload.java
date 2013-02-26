package org;

import java.util.List;
import java.util.Map;

import org.config.Configuration;
import org.config.LoadProperties;

public class PrintWorkload {
	
	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args);
		Configuration.setConfiguration(lp);
		
		
		for(int clock=0;!Configuration.V.isempty();clock++){
			int ntask=0;
			List<Map<String, Double>> tasks = Configuration.V.tasksarriving(clock);
			while(tasks!=null && tasks.size()>0){
				tasks.remove(0);
				ntask++;
			}
			if (ntask!=0)
				System.out.println(clock+" "+ntask);
			else
				System.out.println(clock+" "+0);
		}
		
		
		
		
	}

}

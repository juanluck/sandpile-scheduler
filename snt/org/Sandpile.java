package org;

import java.util.List;
import java.util.Map;

import org.config.Configuration;
import org.graphs.WattsStrogatz;
import org.sandpile.Grain;

import random.CommonState;

public class Sandpile extends Thread{

	WattsStrogatz sn;
	
	
	
	public void setSn(WattsStrogatz sn) {
		this.sn = sn;
	}



	public void run() {

		int clock=0;
		
		
		boolean emptypile=false;
		
		while (!Configuration.V.isempty() || !emptypile){
			
			List<Map<String, Double>> tasks = Configuration.V.tasksarriving(clock);
			
			//Assigning task to processors. You can follow three different policies: "frontend", "random" and "roundrobin"
			int proc=0;
			while(tasks!=null && tasks.size()>0){
				Map<String,Double> task= tasks.remove(0);
				Double runtime  = task.get("runtime");
				Double codesize = task.get("codesize");
				Grain g = new Grain(runtime.doubleValue(),codesize.doubleValue());
				int middle=sn.size()/2;
				if(Configuration.assignation.equals("random"))
					sn.getProcessor(CommonState.r.nextInt(sn.size())).get_pile().push(g); // Random
				else if(Configuration.assignation.equals("frontend"))
					sn.getProcessor(middle).get_pile().push(g); // Front-end
				else if(Configuration.assignation.equals("roundrobin"))
					sn.getProcessor(++proc%sn.size()).get_pile().push(g); // Round-robin
				
			}
			
			
			
			int max=0;
			int totaltopple=0;
			for(int i = 0;i<sn.size();i++){
				totaltopple+=sn.getProcessor(i).get_pile().get_topple();
				if (sn.getProcessor(i).get_pile().size()>max)
					max=sn.getProcessor(i).get_pile().size();
			}
			
			
			System.out.println("-----------------------------------------");
			for(int i=0;i<sn.size();i++)
				System.out.print("\t"+sn.getProcessor(i).get_pile().size_transfer());
			/*for(int j=1;j<=max;j++){
				for(int i = 0;i<sn.size();i++){
					if(sn.getProcessor(i).get_pile().size()>=j)
						System.out.print("X");
					else
						System.out.print(" ");
				}	
				System.out.println();
			}*/
			System.out.println();
			System.out.println("-----------------------------------------");
			
			
			if (Configuration.sandpile) // If is false, there is no sandpile
				for(int i = 0;i<sn.size();i++){	sn.getProcessor(i).executeUpdate();	}
				//sn.getProcessor(sn.size()/2).executeUpdate();
			
			//Tic transfer n bits/time from source to destiny. After completeness, task are pushed in the pile.
			for(int i = 0;i<sn.size();i++){
				sn.getProcessor(i).get_pile().tic();
			}

			//Tac consumes tasks from the pile in a fifo style
			emptypile=true;
			for(int i = 0;i<sn.size();i++){
				sn.getProcessor(i).get_pile().tac();
				if(!sn.getProcessor(i).get_pile().isempty()) // For termination
					emptypile=false;
			}
			
			System.out.println("Clock: "+clock+" Topple: "+totaltopple);
			clock++;
		}
		
		
	}
}

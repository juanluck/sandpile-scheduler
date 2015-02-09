package org;
import org.config.Configuration;
import org.config.LoadProperties;
import org.config.Logger;
import org.config.Tasks;
import org.utils.SinusoidalArrival;

import random.CommonState;


public class CreateWorkload {
	static int testarrival=1; //for the testing sandpile method
	static int testarrivaltime=0;
	
	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args,null);
		Configuration.setConfiguration(lp);

		
		
		// + WORKLOAD
		// --+ FIXED: Creating a Workload where every BoT has Configuration.tasksperbot tasks
		if(Configuration.tasksperbotmethod.equals("fixed")){
			for(int i=0;i<Configuration.b;i++){
				String N=runtime();
				String D=codesize();
				String A=arrival();
				
				for (int j=1;j<Configuration.tasksperbot;j++){
					N+=" "+runtime();
					D+=" "+codesize();
					A+=" "+arrival();
				}
				Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_N_"+Configuration.methodruntime+"_"+Configuration.runtimeavg, N);
				Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_D_"+Configuration.methodcodesize+"_"+Configuration.codesizeavg, D);
				Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_A_"+Configuration.methodarrival+"_"+Configuration.arrivalavg, A);
			}
			Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_N_"+Configuration.methodruntime+"_"+Configuration.runtimeavg, "");
			Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_D_"+Configuration.methodcodesize+"_"+Configuration.codesizeavg, "");
			Logger.append("b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_A_"+Configuration.methodarrival+"_"+Configuration.arrivalavg, "");
		}
	
	}
	
	public static Tasks[] createOnLineWorkload(){
		
		Tasks [] NDA = new Tasks[3];
		NDA[0] = new Tasks(); // Runtime N
		NDA[1] = new Tasks(); // Data D
		NDA[2] = new Tasks(); // Arriving N
		// + ONLINE WORKLOAD
		// --+ FIXED: Creating a Workload where every BoT has Configuration.tasksperbot tasks
		if(Configuration.tasksperbotmethod.equals("fixed")){
			for(int i=0;i<Configuration.b;i++){
		
				NDA[0].add(i,Double.parseDouble(runtime()));
				NDA[1].add(i,Double.parseDouble(codesize()));
				NDA[2].add(i,Double.parseDouble(arrival()));
				for (int j=1;j<Configuration.tasksperbot;j++){
					
					NDA[0].add(i,Double.parseDouble(runtime()));
					NDA[1].add(i,Double.parseDouble(codesize()));
					NDA[2].add(i,Double.parseDouble(arrival()));
				}

			}
		}
		// --+ Sinusoidal: Creating a Workload with a sinusoidal arrival
		else if(Configuration.tasksperbotmethod.equals("sinusoidal")){
			
						
			for (int i=0;i<Configuration.b;i++){
				int nr_of_tasks = SinusoidalArrival.nr_of_tasks(i);
				for(int j=0;j<nr_of_tasks;j++){
					
					NDA[0].add(i,Double.parseDouble(runtime()));
					NDA[1].add(i,Double.parseDouble(codesize()));
					NDA[2].add(i,i);
			}

			}
			
		}
		
		return NDA;
	}
	
	public static String runtime(){
		if (Configuration.methodruntime.equals("homogeneous")){
			return Configuration.runtimeavg+"";
		}else if (Configuration.methodruntime.equals("normal")){
			double val=0;
			do{
				val = CommonState.r.nextGaussian()*Math.sqrt(Configuration.runtimeavg/3.0)+Configuration.runtimeavg;
			}while(val<=0);
			
			return val+"";
		}else if (Configuration.methodruntime.equals("exponential")){
			double val=0;
			do{
				double U = CommonState.r.nextDouble();
				double lambda = 1.0/Configuration.runtimeavg;
				val = (-Math.log(U))/lambda;
			}while(val<=0);
			
			return val+"";
		}else
			return "";
	}
	
	public static String codesize(){
		if (Configuration.methodcodesize.equals("homogeneous")){
			return Configuration.codesizeavg+"";
		}else if(Configuration.methodcodesize.equals("normal")){
			double val=0;
			do{
				val = CommonState.r.nextGaussian()*Math.sqrt(Configuration.codesizeavg/3.0)+Configuration.codesizeavg;
			}while(val<=0);
			
			return val+"";
		}else
			return "";
	}
	
	public static String arrival(){
		if (Configuration.methodarrival.equals("homogeneous")){
			return Configuration.arrivalavg+"";
		}else if (Configuration.methodarrival.equals("testingsandpile")){
			int aux= testarrivaltime;
			if ((testarrival%Configuration.tasksperbot)==0)
				testarrivaltime+=Configuration.arrivalavg;
			testarrival++;
			return aux+"";
		}else if (Configuration.methodarrival.equals("uniform")){
			int aux= testarrivaltime;
			if ((testarrival%Configuration.tasksperbot)==0)
				testarrivaltime = CommonState.r.nextInt(Configuration.arrivalavg);
			testarrival++;
			return aux+"";
		}else if(Configuration.methodarrival.equals("increasing")){
			int aux= testarrivaltime;
			if (testarrival> Configuration.tasksperbot){
				aux += CommonState.r.nextInt(Configuration.arrivalavg);
				if ((testarrival%Configuration.tasksperbot)==0)
					testarrivaltime += Configuration.arrivalavg;
			}
				
			testarrival++;
			return aux+"";
		}else
			return "";
	}
}

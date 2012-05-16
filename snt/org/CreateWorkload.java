package org;
import org.config.Configuration;
import org.config.LoadProperties;
import org.config.Logger;


public class CreateWorkload {
	static int testarrival=1; //for the testing sandpile method
	static int testarrivaltime=0;
	
	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args);
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
	
	public static String runtime(){
		if (Configuration.methodruntime.equals("homogeneous")){
			return Configuration.runtimeavg+"";
		}else
			return "";
	}
	
	public static String codesize(){
		if (Configuration.methodcodesize.equals("homogeneous")){
			return Configuration.codesizeavg+"";
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
		}else
			return "";
	}
}

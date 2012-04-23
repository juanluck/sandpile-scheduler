package org;

import org.config.Configuration;
import org.config.LoadProperties;
import org.config.Logger;


public class CreateArchitecture {
	
	public static void main(String[] args) {
		LoadProperties lp = new LoadProperties(args);
		Configuration.setConfiguration(lp);
	
		double [] p = new double[Configuration.q];
		double [][] c =new double[Configuration.q][Configuration.q];
		
		
		// + PROCESSORS
		// --+ HOMOGENEOUS: Creating an Architecture of Q homogeneous processors
		if(Configuration.processortype.equals("homogeneous")){
			String cpu="1";
			for (int i=0;i<Configuration.q-1;i++)
					cpu+=" 1";
			Logger.append("homogeneousQ"+Configuration.q, cpu+"\n");

		}
		// --+  HETEROGENEOUS. Creating an Architecture of Q heterogeneous computers
		//   --+ METHOD INCREASING processors to max rho
		else if(Configuration.processortype.equals("increasing")){
			float increasingfactor = (float) (((Configuration.rho-1)*1.0)/(Configuration.q-1)); 
			
			float value=1;
			String cpu=""+value;
			for (int i=0;i<Configuration.q-1;i++){
					value+=increasingfactor;
					cpu+=" "+value;
			}
			Logger.append("increasingQ"+Configuration.q, cpu+"\n");
			
		}
		//   --+ METHOD RANDOM processors: U from 1 to rho
		// TODO: Implement the random - and other- methods


		// + NETWORK
		// --+ HOMOGENEOUS: Creating an Architecture of homogeneous network links
		if(Configuration.networktype.equals("homogeneous")){

			for (int i=0;i<Configuration.q;i++){
				String link="";
				for (int j=0;j<Configuration.q;j++){
					if (j==0){
						if(i==j)
							link+="0";
						else
							link+="1";
					}else{
						if(i==j)
							link+=" 0";
						else
							link+=" 1";	
					}
				}
				Logger.append("homogeneousC"+Configuration.q, link);
			}
			Logger.append("homogeneousC"+Configuration.q, "");
		}
		// --+ HETEROGENEOUS: Creating an Architecture of heterogeneous network links
		//   --+ METHOD INCREASING links to max tau
		
		//   --+ METHOD RANDOM links: U from 1 to tau
		// TODO: Implement the increasing, random  and other methods

		
		
	}
}

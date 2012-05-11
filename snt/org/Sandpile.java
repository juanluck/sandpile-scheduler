package org;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.config.Configuration;
import org.config.Logger;
import org.graphs.WattsStrogatz;
import org.sandpile.Grain;

import random.CommonState;

public class Sandpile extends Thread{

	WattsStrogatz sn;
	
	SortedMap<Integer, Integer> frec;
	
	public Sandpile() {
		frec = new TreeMap<Integer, Integer>();
	}
	
	public void setSn(WattsStrogatz sn) {
		this.sn = sn;
	}



	public void run() {

		int clock=0;
		int throughput=0;

		
		boolean emptypile=false;
		
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
		
		while (!Configuration.V.isempty() || !emptypile){
			
			// Log files
			logs(clock,throughput);
			
			
			
			/*int max=0;
			int totaltopple=0;
			for(int i = 0;i<sn.size();i++){
				totaltopple+=sn.getProcessor(i).get_pile().get_topple();
				if (sn.getProcessor(i).get_pile().size()>max)
					max=sn.getProcessor(i).get_pile().size();
			}*/
			//System.out.println("-----------------------------------------");
			//for(int i=0;i<sn.size();i++)
			//	System.out.print("\t"+sn.getProcessor(i).get_pile().size_transfer());
			/*for(int j=1;j<=max;j++){
				for(int i = 0;i<sn.size();i++){
					if(sn.getProcessor(i).get_pile().size()>=j)
						System.out.print("X");
					else
						System.out.print(" ");
				}	
				System.out.println();
			}*/
			//System.out.println();
			//System.out.println("-----------------------------------------");
			
			
			if (Configuration.sandpile) // If is false, there is no sandpile
				for(int i = 0;i<sn.size();i++){	sn.getProcessor(i).executeUpdate();	}
				//sn.getProcessor(sn.size()/2).executeUpdate();
			
			//Tic transfer n bits/time from source to destiny. After completeness, task are pushed in the pile.
			for(int i = 0;i<sn.size();i++){
				sn.getProcessor(i).get_pile().tic();
			}

			//Tac consumes tasks from the pile in a fifo style
			emptypile=true;
			throughput=0;
			for(int i = 0;i<sn.size();i++){
				throughput += sn.getProcessor(i).get_pile().tac();
				if(!sn.getProcessor(i).get_pile().isempty()) // For termination
					emptypile=false;
			}
			
			//System.out.println("Clock: "+clock+" Topple: "+totaltopple);
			clock++;
			
			
			tasks = Configuration.V.tasksarriving(clock);
			
			//Assigning task to processors. You can follow three different policies: "frontend", "random" and "roundrobin"
			proc=0;
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
		}
		
		logs(clock,throughput);
		
		finallog(clock);
		
		
	}
	
	public void add_topple_frequencies(SortedMap<Integer, Integer> localfreq){
		
		Integer key;
		Iterator<Integer> it = localfreq.keySet().iterator();
		
		
		while(it.hasNext()){
			key=it.next();
			if(frec.containsKey(key)){
				int aux = frec.get(key).intValue();
				aux += localfreq.get(key).intValue();
				frec.put(key, new Integer(aux));
			}else{
				frec.put(key, localfreq.get(key));
			}
		}
				
	}
	
	public void logs(int clock,int throughput){
		
		//------------Logs
		int topplecycle=0;
		int abortcycle=0;
		int totaltopple=0;
		for(int i=0;i<sn.size();i++){
			totaltopple+=sn.getProcessor(i).get_pile().get_topple();
			int toppletransaction = sn.getProcessor(i).get_pile().get_topple_transaction();
			int aborttransaction =  sn.getProcessor(i).get_pile().get_abort();
			sn.getProcessor(i).get_pile().add_frectopple(toppletransaction);
			topplecycle+=toppletransaction;
			abortcycle+=aborttransaction;
		}
		
		
		
		String pile=clock+"";
		String transfer=clock+"";
		String total=clock+"";
		String topple=clock+" "+topplecycle;
		String abort=clock+" "+abortcycle;
		String stats=clock+" "+totaltopple+" "+throughput;
		for(int i=0;i<sn.size();i++){
			pile+=" "+sn.getProcessor(i).get_pile().size();
			transfer+=" "+(sn.getProcessor(i).get_pile().size_transfer()-sn.getProcessor(i).get_pile().size());
			total+=" "+sn.getProcessor(i).get_pile().size_transfer();
		}
		
		Logger.append(Configuration.exper+"/statuspiles.txt", pile);
		Logger.append(Configuration.exper+"/statustransfer.txt", transfer);
		Logger.append(Configuration.exper+"/statustotal.txt", total);
		Logger.append(Configuration.exper+"/topplecycle.txt", topple);
		Logger.append(Configuration.exper+"/abort.txt", abort);
		Logger.append(Configuration.exper+"/stats.txt", stats);
		
		//------------End Logs
		
	}
	
	
	public void finallog(int clock){
		for(int i=0;i<sn.size();i++){
			SortedMap<Integer, Integer> localfreq =	sn.getProcessor(i).get_pile().get_freqtopple();
			add_topple_frequencies(localfreq);
		}
		Logger.append(Configuration.exper+"/topplefrequencies.txt", get_topple_frequencies(frec));
		
		double avgflow=0;
		int countgrains=0;
		for(int i=0;i<sn.size();i++){
			List<Grain> processed = sn.getProcessor(i).get_pile().get_processed();
			for(int j=0;j<processed.size();j++){
				avgflow+=processed.get(j).get_flowtime();
				countgrains++;
			}
		}
		
		avgflow/=(countgrains*1.0);
		
		double stdflow=0;
		countgrains=0;
		for(int i=0;i<sn.size();i++){
			List<Grain> processed = sn.getProcessor(i).get_pile().get_processed();
			for(int j=0;j<processed.size();j++){
				double std = avgflow - processed.get(j).get_flowtime();
				std *= std;
				stdflow += std;
				countgrains++;
			}
		}
		
		stdflow/=(countgrains*1.0); stdflow = Math.sqrt(stdflow);
		
		String flowtime = avgflow+" "+stdflow+"\n";
		
		Logger.append(Configuration.exper+"/flowtime.txt", flowtime);
		
	}
	
	public String get_topple_frequencies(SortedMap<Integer, Integer> frectopple){
		
		Integer key;
		String frequencies="";
		Iterator<Integer> it = frectopple.keySet().iterator();
		
		boolean newcount=true;
		int keyvalue = 0;
		int freqvalue = 0;
		boolean last=false;
		while(it.hasNext()){
			key=it.next();
			if(key.intValue()!=0){
				if(newcount){
					newcount=false;
					keyvalue=key.intValue();
					freqvalue=frectopple.get(key).intValue();
				}else if ((key.intValue()<keyvalue+5)){
					freqvalue+=frectopple.get(key).intValue();
				}else{
					frequencies+=keyvalue+" "+freqvalue+"\n";
					keyvalue=key.intValue();
					freqvalue=frectopple.get(key).intValue();
				}
				
				//frequencies+=key.toString()+" "+frectopple.get(key).toString()+"\n";
			}
		}
		frequencies+=keyvalue+" "+freqvalue+"\n";
		return frequencies;
	}
}

package org;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.config.Configuration;
import org.config.Logger;
import org.graphs.Topology;
import org.sandpile.Grain;
import org.sandpile.Node;
import org.sandpile.PileInNode;

import random.CommonState;

public class Sandpile extends Thread{

	Topology sn;
	
	SortedMap<Integer, Integer> frec;
	
	SortedMap<Integer, Integer> _throughput;
		
	public Sandpile() {
		frec = new TreeMap<Integer, Integer>();
		_throughput = new TreeMap<Integer, Integer>();
	}
	
	public void setSn(Topology sn) {
		this.sn = sn;
	}



	public void run() {

		int clock=0;
		int throughput=0;
		double current_flow_time=0;
		int new_workload = 0;
		
		
		boolean emptypile=false;
		
		List<Map<String, Double>> tasks = Configuration.V.tasksarriving(clock);
		if(tasks!=null)
			new_workload = tasks.size();
		else
			new_workload = 0;
		//Assigning task to processors. You can follow three different policies: "frontend", "random" and "roundrobin"
		int proc=0;
		while(tasks!=null && tasks.size()>0){
			Map<String,Double> task= tasks.remove(0);
			Double runtime  = task.get("runtime");
			Double codesize = task.get("codesize");
			Grain g = new Grain(runtime.doubleValue(),codesize.doubleValue());
			int middle=(int)( sn.size()/2.0);
			if(Configuration.assignation.equals("random"))
				sn.getProcessor(CommonState.r.nextInt(sn.size())).get_pile().push(g); // Random
			else if(Configuration.assignation.equals("frontend"))
				sn.getProcessor(middle).get_pile().push(g); // Front-end
			else if(Configuration.assignation.equals("roundrobin"))
				sn.getProcessor(++proc%sn.size()).get_pile().push(g); // Round-robin
			
		}

		//Initial paint
		repaint();
		if(Configuration.verbosity>0){
			Logger.append("","Cycle,New Workload,Resources,Utilization,Avalanche,Accumulated Energy,Current Flow Time\n");
		}
		
		int size_of_the_avalanche = 0;
		long accumulated_energy = 0;
		
		while (!Configuration.V.isempty() || !emptypile){
			
			// Log files
			logs(clock,throughput);
			
			// Log in the Standard output
			if(Configuration.verbosity>0){
				int nr_resources = 0;
				for(int i = 0;i<sn.size();i++){
					if (!sn.getProcessor(i).get_pile().isempty())
						nr_resources++;
				}
				accumulated_energy +=nr_resources;
				
				Logger.append("", clock+","+new_workload+","+nr_resources+","+((nr_resources*1.0)/Configuration.q)+","+size_of_the_avalanche+","+accumulated_energy+","+current_flow_time+"\n");
				size_of_the_avalanche = 0;
				new_workload = 0;
				
			}
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
			
			if (Configuration.liquid)
				for(int i = 0;i<sn.size();i++){	sn.getProcessor(i).executeUpdateLiquid();	}
			else if (Configuration.sandpile) // If is false, there is no sandpile
				if (Configuration.topology.equals("grid") || Configuration.topology.equals("gridtorus")){
						for(int i = 0;i<sn.size();i++){	size_of_the_avalanche += sn.getProcessor(i).executegridUpdate(sn.getProcessor(i),true);	}
				}else if (Configuration.clairvoyance)
					for(int i = 0;i<sn.size();i++){	sn.getProcessor(i).executeUpdateClairvoyant();	}
				else
					for(int i = 0;i<sn.size();i++){	sn.getProcessor(i).executeUpdate();}
				//sn.getProcessor(sn.size()/2).executeUpdate();
			
			//Tic transfer n bits/time from source to destiny. After completeness, task are pushed in the pile.
			for(int i = 0;i<sn.size();i++){
				sn.getProcessor(i).get_pile().tic();
			}

			// After tic()
			repaint();
			
			//Tac consumes tasks from the pile in a fifo style
			emptypile=true;
			throughput=0;
			current_flow_time = 0;
			int nr_of_tasks = 0;
			for(int i = 0;i<sn.size();i++){
				throughput += sn.getProcessor(i).get_pile().tac();
				double aux = sn.getProcessor(i).get_pile().get_flowtime_of_tasks_in_current_cycle();
				if (aux != -1){
					current_flow_time += aux;
					nr_of_tasks ++;
				}
				if(!sn.getProcessor(i).get_pile().isempty()) // For termination
					emptypile=false;
			}
			
			if(nr_of_tasks != 0)
				current_flow_time /= (nr_of_tasks*1.0);
			else
				current_flow_time = -1;
			
			// After tac()
			repaint();
			
			
			if (_throughput.containsKey(new Integer(throughput))){
				int aux = _throughput.get(new Integer(throughput)).intValue();
				aux++;
				_throughput.remove(new Integer(throughput));
				_throughput.put(new Integer(throughput), new Integer(aux));
			}else{
				_throughput.put(new Integer(throughput), new Integer(1));
			}
			
			//System.out.println("Clock: "+clock+" Topple: "+totaltopple);
			clock++;
			
			
			tasks = Configuration.V.tasksarriving(clock);
			if(tasks!=null)
				new_workload = tasks.size();
			else
				new_workload = 0;
			
			//Assigning task to processors. You can follow three different policies: "frontend", "random" and "roundrobin"
			//proc=0;
			while(tasks!=null && tasks.size()>0){
				Map<String,Double> task= tasks.remove(0);
				Double runtime  = task.get("runtime");
				Double codesize = task.get("codesize");
				Grain g = new Grain(runtime.doubleValue(),codesize.doubleValue());
				int middle=(int)( sn.size()/2.0);
				if(Configuration.assignation.equals("random"))
					sn.getProcessor(CommonState.r.nextInt(sn.size())).get_pile().push(g); // Random
				else if(Configuration.assignation.equals("frontend"))
					sn.getProcessor(middle).get_pile().push(g); // Front-end
				else if(Configuration.assignation.equals("roundrobin"))
					sn.getProcessor(++proc%sn.size()).get_pile().push(g); // Round-robin
				
			}
			
			
			//Hack for getting a canonical behavior: [Only for canonical abelian sandpile (tac=false)] Check for determining if the system is stable
			boolean stable = false;
			if (Configuration.notac==true && (Configuration.topology.equals("grid") || Configuration.topology.equals("gridtorus"))){
				for(int i = 0;i<sn.size();i++){	
					if (sn.getProcessor(i).get_pile().size() >= Configuration.threshold){
						stable = false;
					}
				}
			}
		   if(stable)
				break;
		}
		

		logs(clock,throughput);
		
		if(Configuration.verbosity > 1)
			finallog(clock);
		int numberofprocessors = 0;
		for(int i=0;i<sn.size();i++){
			if(!sn.getProcessor(i).get_pile().isempty())
				numberofprocessors++;
		}
			
		//Logger.append("","Nr.task: "+Configuration.b*Configuration.tasksperbot+" Nr.nodes: "+numberofprocessors+" Density: "+(Configuration.b*Configuration.tasksperbot*1.0)/numberofprocessors+" "+ get_flowtime_avg_std()+" "+get_throughput_avg_std(clock)+" "+clock+" "+get_total_topples(frec)+"\n");		
		
	}
	
	private void repaint(){
		if (Configuration.display){
			for(int i = 0;i<sn.size();i++){
				double load = (Configuration.threshold < sn.getProcessor(i).get_pile().size())? Configuration.threshold : sn.getProcessor(i).get_pile().size();
				if (sn.getProcessor(i).get_pile().isempty()){
					Topology.get_gs().getGSnodeById(""+sn.getProcessor(i).get_pile().get_indexpile()).addAttribute("ui.hide");
				}else{
					Topology.get_gs().getGSnodeById(""+sn.getProcessor(i).get_pile().get_indexpile()).removeAttribute("ui.hide");
					Topology.get_gs().changeGSColorByLoad(""+sn.getProcessor(i).get_pile().get_indexpile(), load/(Configuration.threshold*1.0) );
				}
				
			}		
			Topology.get_gs().sleep(Configuration.pause);
		}	
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
		int global_avalanche_per_cycle =0;
		for(int i=0;i<sn.size();i++){
			totaltopple+=sn.getProcessor(i).get_pile().get_topple();
			global_avalanche_per_cycle += sn.getProcessor(i).get_pile().get_topple_transaction();
			int aborttransaction =  sn.getProcessor(i).get_pile().get_abort();
			topplecycle+=sn.getProcessor(i).get_pile().get_topple_transaction();
			abortcycle+=aborttransaction;
		}
		sn.getProcessor(0).get_pile().add_frectopple(global_avalanche_per_cycle);
		
		if(Configuration.verbosity>2){
			double avgloadcycle=0;
			
			for(int i=0;i<sn.size();i++){
				avgloadcycle+=sn.getProcessor(i).get_pile().size_transfer();
			}
			
			avgloadcycle/=sn.size();

			double stdloadcycle=0;
			for(int i=0;i<sn.size();i++){
				double std = avgloadcycle - sn.getProcessor(i).get_pile().size_transfer();
				std *= std;
				stdloadcycle += std;
			}

			stdloadcycle/=(sn.size()*1.0); stdloadcycle = Math.sqrt(stdloadcycle);
			
			
			String pile=clock+"";
			String transfer=clock+"";
			String total=clock+"";
			String topple=clock+" "+topplecycle;
			String abort=clock+" "+abortcycle;
			String stats=clock+" "+totaltopple+" "+throughput;
			String estimate=clock+"";
			for(int i=0;i<sn.size();i++){
				pile+=" "+sn.getProcessor(i).get_pile().size();
				transfer+=" "+(sn.getProcessor(i).get_pile().size_transfer()-sn.getProcessor(i).get_pile().size());
				total+=" "+(int)(sn.getProcessor(i).get_pile().size_transfer());///sn.getProcessor(i).get_pile().get_task_per_cycle_estimate());
				estimate+=" "+sn.getProcessor(i).get_pile().get_task_per_cycle_estimate();
			}
			
			Logger.append(Configuration.exper+"/statuspiles"+Configuration.seed+".txt", pile);
			Logger.append(Configuration.exper+"/statustransfer"+Configuration.seed+".txt", transfer);
			Logger.append(Configuration.exper+"/statustotal"+Configuration.seed+".txt", total);
			if (topplecycle>0)
				Logger.append(Configuration.exper+"/topplecycle"+Configuration.seed+".txt", topple);
			Logger.append(Configuration.exper+"/abort"+Configuration.seed+".txt", abort);
			Logger.append(Configuration.exper+"/dynamics"+Configuration.seed+".txt", stats);
			Logger.append(Configuration.exper+"/workload"+Configuration.seed+".txt", clock+" "+avgloadcycle+" "+stdloadcycle);
			Logger.append(Configuration.exper+"/TperCest"+Configuration.seed+".txt", estimate);
			//------------End Logs
			
		}
		
	
	}
	
	
	public void finallog(int clock){
		for(int i=0;i<sn.size();i++){
			SortedMap<Integer, Integer> localfreq =	sn.getProcessor(i).get_pile().get_freqtopple();
			add_topple_frequencies(localfreq);
		}
		Logger.append(Configuration.exper+"/topplefrequencies"+Configuration.seed+".txt", get_topple_frequencies(frec));
		
		
		
		Logger.append(Configuration.exper+"/stats"+Configuration.seed+".txt", get_flowtime_avg_std()+"\n"+get_throughput_avg_std(clock)+"\n"+"Makespan "+clock+"\n");
		
		Logger.append(Configuration.exper+"/throughput"+Configuration.seed+".txt", get_throughput_frequencies(clock));
		
		
	}
	
	public String get_flowtime_avg_std(){
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
		
		String flowtime = avgflow+" "+stdflow;
		
		return "Flowtime "+flowtime;		
	}
	
	
	public String get_throughput_avg_std(int clock){
		Integer key;
		Iterator<Integer> it = _throughput.keySet().iterator();
		
		double avgthroughput=0;
		int count=0;
		
		while(it.hasNext()){
			key=it.next();
			int times = _throughput.get(key).intValue();
			count += times;
			avgthroughput += (key.intValue() * times);
		}
		avgthroughput /= (count*1.0);
		
		
		it = _throughput.keySet().iterator();
		double stdthroughput=0;
		double std = 0;
		count=0;
		
		while(it.hasNext()){
			key=it.next();
			int times = _throughput.get(key).intValue();
			std = avgthroughput - key.intValue();
			std *= std;
			count += times;
			stdthroughput += (std * times);
		}
		
		stdthroughput /= (count*1.0); stdthroughput = Math.sqrt(stdthroughput);
		
		return "Throughput "+avgthroughput+" "+stdthroughput;
	}
	
	public String get_throughput_frequencies(int clock){
		Integer key;
		String frequencies="";
		Iterator<Integer> it = _throughput.keySet().iterator();
		
		int keyvalue = 0;
		double freqvalue = 0;

		while(it.hasNext()){
			key=it.next();
			keyvalue = key.intValue();
			freqvalue=_throughput.get(key).intValue()/(clock*1.0);
			frequencies+=keyvalue+" "+freqvalue+"\n";
		}
		return frequencies;
	}
	
	public int get_total_topples(SortedMap<Integer, Integer> frectopple){
		Integer key;
		Iterator<Integer> it = frectopple.keySet().iterator();
		
		int totaltopples = 0;
		while(it.hasNext()){
			key=it.next();
			totaltopples += (key.intValue() * frectopple.get(key).intValue());
		}
		return totaltopples;
	}
	
	public String get_topple_frequencies(SortedMap<Integer, Integer> frectopple){
		
		Integer key;
		String frequencies="";
		Iterator<Integer> it = frectopple.keySet().iterator();
		
		//boolean newcount=true;
		//int keyvalue = 0;
		//int freqvalue = 0;
		//boolean last=false;
		while(it.hasNext()){
			key=it.next();
			if(key.intValue()!=0){
				frequencies+=key.intValue()+" "+frectopple.get(key).intValue()+"\n";
				/*if(newcount){
					newcount=false;
					keyvalue=key.intValue();
					freqvalue=frectopple.get(key).intValue();
				}else if ((key.intValue()<keyvalue+5)){
					freqvalue+=frectopple.get(key).intValue();
				}else{
					frequencies+=keyvalue+" "+freqvalue+"\n";
					keyvalue=key.intValue();
					freqvalue=frectopple.get(key).intValue();
				}*/
				
				//frequencies+=key.toString()+" "+frectopple.get(key).toString()+"\n";
			}
		}
		//frequencies+=keyvalue+" "+freqvalue+"\n";
		return frequencies;
	}
	
	public static void main(String[] args) {
		//Setting the seed
				CommonState.setSeed(Configuration.seed);
				
				//Creating the directory for the experiment
				//Configuration.exper += "/"+Configuration.seed;
				
				(new File(Configuration.exper)).mkdirs();
				
				//List of Nodes
				Node[] proc = new Node[Configuration.q];
				for (int i=0;i<Configuration.q;i++){
					PileInNode pile = new PileInNode(Configuration.p[i], i);
					proc[i] = new Node(pile);
				}
				
				//Creating the topology
				Topology sn = new Topology(proc);
				
				for (int i=0;i<Configuration.q;i++){
					proc[i].set_neighbours(sn.getNeighborhoodforNode(i));
				}
				
				// Creating the algorithm
				Sandpile sp = new Sandpile();
				
				// Adding the WS topology to the Sandpile
				sp.setSn(sn);
	}
}

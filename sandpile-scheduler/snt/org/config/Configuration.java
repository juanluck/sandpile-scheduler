
package org.config;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.CreateWorkload;






public class Configuration {
	

	//Creating the architecture
	//-------------------------
	public static int q=0; // Number of Computing Nodes
	public static String processortype=null; // Can be "homogeneous", or two types of heterogeneous "increasing", or "random".
	public static int rho=0; // Maximum CPU speed factors
	public static String networktype=null; // Can be "homogeneous", or two types of heterogeneous "increasing", or "random".
	public static int tau=0; // Maximum Network Link speed factors
	
	//Loading Architectures
	//-------------------------	
	public static String fileprocessors="";
	public static String filenetwork="";
	public static double []   p;// Vector of size "q" with the speeding factors of different processors (reference processor = 1)
	public static double [][] C;// Matrix of size "qxq" with the speeding factors of network links (reference link = 1)
	
	
	//Creating Workload
	//-------------------------	
	public static int b=0; // Number of bags of tasks
	// Number of tasks per BoT
	// "fixed" method assigns "tasksperbot" to every task
	public static String tasksperbotmethod="" ; 
	public static int tasksperbot=0;
	// Runtime per task n_i
	// "homogeneous" method assigns "runtimeavg" to every task
	public static String methodruntime="";
	public static int runtimeavg;
	// Code size per task, d_i
	// "homogeneous" method assigns "codesizeavg" to every task
	public static String methodcodesize="";
	public static int codesizeavg;
	// Arrival time of task a_i
	// "homogeneous" method assigns "arrivalavg" to every task
	public static String methodarrival="";
	public static int arrivalavg;
	
	//Loading Workload
	//-------------------------
	public static boolean parsing=false; // To parse the workload it has to be set to true
	public static String fileworkloadN="";
	public static String fileworkloadA="";
	public static String fileworkloadD="";	
	
	private static Tasks N;
	private static Tasks A;
	private static Tasks D;
	
	public static OrderedTasks V;
	
	// Random seed
	//-------------------------
	public static long seed;
	
	//Topology: By the moment only "ring", "gridtorus" and "wattsstrogatz" are implemented
	//TODO: implement grid topology 
	public static String topology;
	//Rewiring probability for the Watts-Strogatz graph
	//-------------------------
	public static double rewiring;
	
	
	//Initial Workload assignation: It can be "random", "frontend" or "roundrobin"
	public static String assignation = "frontend"; 
	
	//It disables the sandpile when is set to false; default is true
	public static boolean sandpile=true;
	//If true It disables a processor to pull a task for processing (Used for canonical sandpile)
	public static boolean notac=false;
	
	//It implements a simplistic version of the liquid approach when is set to true; default is false
	public static boolean liquid=false;
	
	//It selects the type of transition policy that enables an avalanche to happen
	//order: it selects the two neighbors beta with less load
	//random: it selects randomly the two neighbors beta
	public static String transition;
	
	//It identifies the experiment with the following values: architecture/workload/initialization/seed
	public static String workload;
	public static String exper;
	
	//Clairvoyance
	public static boolean clairvoyance;
	
	//Gossip forwarding
	public static boolean forwarding=true;
	
	// Classic sandpile (this settings are activated for grid/gridtorus topologies)
	//The following variables must be set if you want a classical grid/threshold sandpile
	// neighborhood stands for the neighborhood configuration only for grid or gridtorus topologies
	// TODO: implement vonneumann and moore neighborhoods.
		public static String neighborhood="vonneumann";
	//e.g. if neighborhood=vonneumann, threshold=8 and migrating_tasks_to_neighbor=1 then a pile of 8 grains will topple 4 grains to N W S E
		public static int threshold;
		public static int migrating_tasks_to_neighbor;
		
		
	//Displaying with GraphStream
		public static boolean display=true;
		public static int pause=10;
		
	//Verbosity
		//+ 0 do not display anything (to be used together with display=true)
		//+ 1 print information in the standard output
		//+ 2 in addition to 1, it also reports the final stats in the log directory
		//+ 3 in addition to 2, it reports everything in the log directory
		public static int verbosity=2;
	

	
	public static void setConfiguration(LoadProperties lp){

		// + Architecture
		// --+ Creating the Architecture
		q = Integer.valueOf(lp.getProperty("q","0"));
		processortype = lp.getProperty("processortype","homogeneous");
		rho = Integer.valueOf(lp.getProperty("rho","1"));
		networktype = lp.getProperty("networktype","homogeneous");
		tau = Integer.valueOf(lp.getProperty("tau","1"));
		
		// --+ Parsing an Architecture from files
		fileprocessors = lp.getProperty("fileprocessors", "");
		filenetwork = lp.getProperty("filenetwork", "");
		
		if (!filenetwork.equals("") && !fileprocessors.equals("")) 
			setuparchitecture(); // Vector p and Matrix C contain the architecture after setup
		
		// + Workload
		// --+ Creating the Workload
		
		b=Integer.valueOf(lp.getProperty("b","0"));
		tasksperbotmethod=lp.getProperty("tasksperbotmethod","fixed");
		tasksperbot=Integer.valueOf(lp.getProperty("tasksperbot","0"));
		methodruntime=lp.getProperty("methodruntime","homogeneous");
		runtimeavg= Integer.valueOf(lp.getProperty("runtimeavg","1"));
		methodcodesize= lp.getProperty("methodcodesize","homogeneous");
		codesizeavg=Integer.valueOf(lp.getProperty("codesizeavg","0"));
		methodarrival=lp.getProperty("methodarrival","homogeneous");
		arrivalavg=Integer.valueOf(lp.getProperty("arrivalavg","0"));
		parsing=Boolean.valueOf(lp.getProperty("parsing","false"));
		
		// --+ Parsing a Workload from files
		fileworkloadN = "b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_N_"+Configuration.methodruntime+"_"+Configuration.runtimeavg;
		fileworkloadA = "b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_A_"+Configuration.methodarrival+"_"+Configuration.arrivalavg;
		fileworkloadD = "b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_D_"+Configuration.methodcodesize+"_"+Configuration.codesizeavg;
		workload = "b_"+Configuration.b+"_tpbot_"+Configuration.tasksperbotmethod+"_"+Configuration.tasksperbot+"_N_"+Configuration.methodruntime+"_"+Configuration.runtimeavg+"_A_"+Configuration.methodarrival+"_"+Configuration.arrivalavg+"_D_"+Configuration.methodcodesize+"_"+Configuration.codesizeavg;
		
		if (Configuration.parsing && Configuration.b!=0){
			setupworkload();
		}
		
		// Seed
		seed = (lp.getProperty("seed") == null) ? System.currentTimeMillis() : Long.valueOf(lp.getProperty("seed")).longValue();
		
		//Topology
		topology = lp.getProperty("topology", "wattsstrogatz");
		//WS rewiring
		rewiring= Double.valueOf(lp.getProperty("rewiring","0.1"));
		
		//Init. assignation
		assignation = lp.getProperty("assignation", "frontend");
		
		//Activate sandpile
		sandpile= Boolean.valueOf(lp.getProperty("sandpile", "true"));
		notac= Boolean.valueOf(lp.getProperty("notac", "false"));
		
		//To Activate liquid
		liquid= Boolean.valueOf(lp.getProperty("liquid", "false"));

		//Select transition policy: order or random
		transition = lp.getProperty("transition", "order");
		
		//Activate clairvoyance
		clairvoyance= Boolean.valueOf(lp.getProperty("clairvoyance", "false"));
		
		//Activate forwarding
		forwarding= Boolean.valueOf(lp.getProperty("forwarding", "true"));

		
		//Exper
		exper=fileprocessors+filenetwork+"/"+workload+"/"+assignation;
		
		if (liquid)
			exper+="LIQ";
		else if (sandpile)
			exper+="SP";
		
		exper+=topology;
		
		if (clairvoyance)
			exper+="CL";
		
		if (!forwarding)
			exper+="NOFWD";
		
		if (transition.equals("order"))
			exper+="order";
		else
			exper+="random";
		
		//Classic sandpile
		neighborhood=lp.getProperty("neighborhood", "vonneumann");
		threshold = Integer.parseInt(lp.getProperty("threshold", "8"));
		migrating_tasks_to_neighbor = Integer.parseInt(lp.getProperty("migratingtasksperneighbor", "1"));;
		
		//Display in Graphstream
		display = Boolean.parseBoolean(lp.getProperty("display","false"));
		pause = Integer.parseInt(lp.getProperty("pause","100"));
		
		verbosity=Integer.valueOf(lp.getProperty("verbosity","2"));
	}
	
	
	private static void setupOnLineworkload(){
		
		Tasks[] NDA = CreateWorkload.createOnLineWorkload();
		
		V = new OrderedTasks();
		
		for(int k=0;k<Configuration.b;k++){
			ArrayList<Double> n = NDA[0].get(k);
			ArrayList<Double> d = NDA[1].get(k);
			ArrayList<Double> a = NDA[2].get(k);
	
			
			for (int i=0;i<n.size();i++){
				V.add(a.get(i), n.get(i), d.get(i));
			}
			
		}
	
	}
	
	private static void setupworkload(){
		
		File f = new File(Configuration.fileworkloadN);
		if(f.exists() && !f.isDirectory()) {
			// Parsing runtimes
			N = parsefile(Configuration.fileworkloadN);
				
			// Parsing Arrival times
			A = parsefile(Configuration.fileworkloadA);
			
			// Parsing Code Size
			D = parsefile(Configuration.fileworkloadD);
			
			V = new OrderedTasks();
			
			for(int k=0;k<Configuration.b;k++){
				ArrayList<Double> n = N.get(k);
				ArrayList<Double> a = A.get(k);
				ArrayList<Double> d = D.get(k);
				
				for (int i=0;i<n.size();i++){
					V.add(a.get(i), n.get(i), d.get(i));
				}
			}
		}else{//That is in case the workload has not been yet setup in a file. If that is the case we create it on-line
			setupOnLineworkload();
		}
		
		
	
	}
	
	private static Tasks parsefile(String file){
		Tasks B = new Tasks();
		
		String content = getContents(new File(file));
		BufferedReader allcontent = new BufferedReader(new StringReader(content));
		String line=null;
		
		 try {
			 for (int k=0;k<Configuration.b;k++){
				 line = allcontent.readLine();
				 decodeWorkload(line,B,k); 
			 }
			} catch (IOException e) {}
			
			try {
				allcontent.close();
			} catch (IOException e) {
			}	
			
		return B;
		
	}
	
	private static void decodeWorkload(String str, Tasks B, int bot){
		
		int firstindex=0;
		int nextindex;
		while((nextindex= str.indexOf(" ", firstindex+1))!=-1){			
			B.add(bot, Double.parseDouble(str.substring(firstindex, nextindex).trim()));
			firstindex=nextindex;
		}
		B.add(bot, Double.parseDouble(str.substring(firstindex).trim()));

	}

	
	
	private static void setuparchitecture(){

		// Parsing Processors
		String content = getContents(new File(Configuration.fileprocessors));
		BufferedReader allcontent = new BufferedReader(new StringReader(content));
		String line=null;
		try {
			line = allcontent.readLine();
		} catch (IOException e1) {
		} //not declared within while loop

		p= decodeprocessors(line);

		try {
			allcontent.close();
		} catch (IOException e) {
		}

		// Parsing network links
		C = new double[p.length][p.length];
		content = getContents(new File(Configuration.filenetwork));
		allcontent = new BufferedReader(new StringReader(content));
		line=null;
		
		 try {
			 for (int i=0;i<p.length;i++){
				 line = allcontent.readLine();
				 decodenetwork(line,i); 
			 }
			} catch (IOException e) {}
			
			try {
				allcontent.close();
			} catch (IOException e) {
			}
		
		
	}
	
	private static double[] decodeprocessors(String str){
		
		int count=2;
		int firstindex=str.indexOf(" ");
		int nextindex=0;
		
		while ((nextindex=str.indexOf(" ",firstindex+1))!=-1){
			firstindex=nextindex;
			count++;			
		}
		
		double [] auxp = new double[count];
		
		firstindex=0;
		
		for (int i=0;i<count;i++){
			nextindex= str.indexOf(" ", firstindex+1);
			if (nextindex!=-1)
				auxp[i]=Double.parseDouble(str.substring(firstindex, nextindex).trim());
			else
				auxp[i]=Double.parseDouble(str.substring(firstindex).trim());
			firstindex=nextindex;
		}
 		
		return auxp;
	}
	
	private static void decodenetwork(String str, int index){
		
		int firstindex=0;
		int nextindex;
		for (int j=0;j<p.length;j++){
			nextindex= str.indexOf(" ", firstindex+1);
			if (nextindex!=-1)
				C[index][j]=Double.parseDouble(str.substring(firstindex, nextindex).trim());
			else
				C[index][j]=Double.parseDouble(str.substring(firstindex).trim());
			
			firstindex=nextindex;
		}
	}

	
	
	private static String getContents(File aFile) {
	    //...checks on aFile are elided
	    StringBuffer contents = new StringBuffer();

	    //declared here only to make visible to finally clause
	    BufferedReader input = null;
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      input = new BufferedReader( new FileReader(aFile) );
	      String line = null; //not declared within while loop
	      /*
	      * readLine is a bit quirky :
	      * it returns the content of a line MINUS the newline.
	      * it returns null only for the END of the stream.
	      * it returns an empty String if two newlines appear in a row.
	      */
	      while (( line = input.readLine()) != null){
	        contents.append(line);
	        contents.append(System.getProperty("line.separator"));
	      }
	    }
	    catch (FileNotFoundException ex) {
	      ex.printStackTrace();
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    finally {
	      try {
	        if (input!= null) {
	          //flush and close both "input" and its underlying FileReader
	          input.close();
	        }
	      }
	      catch (IOException ex) {
	        ex.printStackTrace();
	      }
	    }
	    return contents.toString();
	  }

	
	

}

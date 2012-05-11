
package org.config;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;






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
	
	//Rewiring probability for the Watts-Strogatz graph
	//-------------------------
	public static double rewiring;
	
	
	//Initial Workload assignation: It can be "random", "frontend" or "roundrobin"
	public static String assignation = "frontend"; 
	
	//It disables the sandpile when is set to false; default is true
	public static boolean sandpile=true;
	
	//It identifies the experiment with the following values: architecture/workload/initialization/seed
	public static String workload;
	public static String exper;
	
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
		
		//WS rewiring
		rewiring= Double.valueOf(lp.getProperty("rewiring","0.1"));
		
		//Init. assignation
		assignation = lp.getProperty("assignation", "frontend");
		
		//Activate sandpile
		sandpile= Boolean.valueOf(lp.getProperty("sandpile", "true"));
		
		//Exper
		exper=fileprocessors+filenetwork+"/"+workload+"/"+assignation;
		
		
	}
	
	private static void setupworkload(){
		
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

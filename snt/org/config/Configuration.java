
package org.config;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;






public class Configuration {
	

	//Creating the architecture
	public static int q=0; // Number of Computing Nodes
	public static String processortype=null; // Can be "homogeneous", or two types of heterogeneous "increasing", or "random".
	public static int rho=0; // Maximum CPU speed factors
	public static String networktype=null; // Can be "homogeneous", or two types of heterogeneous "increasing", or "random".
	public static int tau=0; // Maximum Network Link speed factors
	
	//Loading Architectures
	public static String fileprocessors="";
	public static String filenetwork="";
	public static double []   p;// Vector of size "q" with the speeding factors of different processors (reference processor = 1)
	public static double [][] C;// Matrix of size "qxq" with the speeding factors of network links (reference link = 1)
	
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
			setup(); // Vector p and Matrix C contain the architecture after setup
		
		
		
	}
	
	private static void setup(){

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

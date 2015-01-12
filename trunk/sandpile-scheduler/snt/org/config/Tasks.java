package org.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.jmx.remote.util.OrderClassLoaders;

public class Tasks {
	
	List<Double> [] B;
	
	
	
	public Tasks() {
		B = new List[Configuration.b];
		for (int k=0;k<Configuration.b;k++){
			B[k] = new ArrayList<Double>();
		}
	}
	
	public void add(int k, double value){
		B[k].add(new Double(value));
	}
	
	public double get(int k, int j){
		return ((Double)B[k].get(j)).doubleValue();
	}
	
	public ArrayList<Double> get(int k){
		return (ArrayList)B[k];
	}
	
	public static void main(String[] args) {
		HashMap<Integer, List<Double>> a = new HashMap<Integer, List<Double>>();

		List<Double> x = new ArrayList<Double>();
		x.add(new Double(25));
		x.add(new Double(30));
		a.put(new Integer(1), x);
		
	
		
		a.get(new Integer(1)).add(new Double(50));
	
		
		Set h = a.keySet();
		
	
		
		System.out.println(a.get(new Integer(1)).toString()+" "+a.size());
		//for (Iterator it=h.iterator();it.hasNext();Object ob=it.next())
		
		
	}
	
	

}

package org.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class OrderedTasks {
	
	SortedMap<Double, List<Map<String,Double>>> orderedTasks;

	public OrderedTasks() {
		orderedTasks = new TreeMap<Double, List<Map<String,Double>>>();
	}
	
	public void add(Double a, Double n, Double d){
		Map<String, Double> v = new HashMap<String, Double>();
		v.put("runtime", n);
		v.put("codesize", d);
		
		if(orderedTasks.containsKey(a)){
			orderedTasks.get(a).add(v);
		}else{
			List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
			list.add(v);
			orderedTasks.put(a, list);
		}			
	}

	public boolean isempty(){
		return (orderedTasks.size()==0)?true:false;
	}
	
	public List<Map<String, Double>> tasksarriving(int clock){
		List<Map<String, Double>> toreturn;
		
		if (orderedTasks.containsKey(new Double(clock))){
			toreturn = orderedTasks.remove(new Double(clock));
		}else
			toreturn = null;
		
		return toreturn;
	}
	
}

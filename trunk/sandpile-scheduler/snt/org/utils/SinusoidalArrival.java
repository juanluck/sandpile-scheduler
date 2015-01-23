package org.utils;

public class SinusoidalArrival {
	
	public static void main(String[] args) {
		int period = 1000;
		int amplitude = 100 ;
		int average_workload = 50;
		int nr_cycles=10000;
		
		double total_tasks = 0;
		
		for(int i=0;i<nr_cycles;i++){
			double pi = Math.PI;
			int nr_of_tasks = (int) ((Math.sin(i*2*pi/period)/2.0)*amplitude + average_workload);
			total_tasks += nr_of_tasks;
			System.out.println(i+" "+nr_of_tasks);
		}
		System.out.println("Total Nr. of tasks: "+total_tasks);
	}
	
	public static int nr_of_tasks(int cycle){
		int period = 1000;
		int amplitude = 100 ;
		int average_workload = 50;
		
		double pi = Math.PI;
		int nr_of_tasks = (int) ((Math.sin(cycle*2*pi/period)/2.0)*amplitude + average_workload);
		return nr_of_tasks;
	}

}

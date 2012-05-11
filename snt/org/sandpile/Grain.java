package org.sandpile;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import random.CommonState;

public class Grain{
	HashMap<Double, List<HashMap<String,Double>>> _grain;
	
	private double _runtime=1;
	private double _codesize=0;
	private double _transferring_time=0;
	private long _identifier;
	private boolean _transferring=false;
	protected PileInNode _from,_to;
	private int _flowtime=0;
	
	public Grain() {
		_identifier = CommonState.r.nextLong();
	}
	
	public Grain(double runtime, double codesize) {
		_runtime = runtime;
		_codesize = codesize;
		_identifier = CommonState.r.nextLong();
	}

	
	
	public boolean is_transferring() {
		return _transferring;
	}
	
	
	public void set_transferring(boolean transferring, PileInNode from, PileInNode to) {
		_transferring = transferring;
		_from=from;
		_to=to;
	}
	
	


	public double get_transferring_time() {
		return _transferring_time;
	}



	public void set_transferring_time(double transferringTime) {
		_transferring_time = transferringTime;
	}
	
	public void decrease_transferring_time(){
		_transferring_time--;
	}

	public void increase_flowtime(){
		_flowtime++;
	}

	public double get_runtime() {
		return _runtime;
	}

	public double get_codesize() {
		return _codesize;
	}

	public void abort_transfer(){
		_to.abort_transfer(this);
	}
	
	public void resetvaluesingrain(){
		_transferring = false;
		_transferring_time = 0;
	}

	public int get_flowtime(){
		return _flowtime;
	}
	
	public boolean equals(Object obj) {
		if (((Grain)obj)._identifier== this._identifier)
			return true;
		else 
			return false;
	}
	
	public static void main(String[] args) {
		Random r = new Random();
		System.out.println(r.nextLong());
		
	}
	
}

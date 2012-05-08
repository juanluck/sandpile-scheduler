package org.sandpile;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.config.Configuration;

public class PileInNode {
	private List<Grain> _grains;
	private List<Grain> _transfer;
	private int _countgrains=0;
	private double _proc_time=0;
	private double _speedup;
	private int _indexpile;
	
	private int topple=0;
	
	boolean _triggered = false; // Any change in the pile activates the updatePile process
	
	
	
	
	public PileInNode(double speedup, int indexpile) {
		
		_indexpile = indexpile;
		_speedup = speedup;
		_grains = new ArrayList<Grain>();
		_transfer = new ArrayList<Grain>();
	}
	
	public void push(Grain grain){
		
		_grains.add(grain);
		_countgrains++;
		_triggered =  true;
		
	}
	
	public Grain pull(){
		Grain g = _grains.get(_grains.size());
		_grains.remove(_grains.size());		
		_countgrains--;
		
		_triggered = true;
		
		return g;
	}
	
	// Grain taken in a FIFO style for processing
	public Grain fifoget(){
		
		Grain g = _grains.get(0);
		
		if (g.is_transferring()){
			g.abort_transfer();
		}else{
			_countgrains--;
		}
		
		_grains.remove(0);	
		
		_triggered = true;
		
		return g;
	}
	
	public void cleansource(Grain g){
		_grains.remove(_grains.indexOf(g));
	}
	
	public void tic(){
		_triggered=false;
		
		if (_transfer.size()>0){
			for (int i=0;i<_transfer.size();i++){
				_transfer.get(i).decrease_transferring_time();
				if (_transfer.get(i).get_transferring_time()<=0){
					push(_transfer.get(i));
					_transfer.get(i)._from.cleansource(_transfer.get(i));
					_transfer.get(i).resetvaluesingrain();
					_transfer.remove(i);
					i--;
				}
			}
		}
	}
	
	public void tac(){

		if (_grains.size()>0 || _proc_time>0){ // If there are grains in the pile or a process to process, otherwise it waits
			while (_proc_time/(_speedup*1.0)<1.0 && _grains.size()>0){
				_proc_time+=fifoget().get_runtime();
			}
			_proc_time-=_speedup;
			if(_proc_time<0)
				_proc_time=0;
		}
	}
	
	public void graintopple(PileInNode to){
		
		topple++;
		
		Grain g=null;
		int last = _grains.size()-1;
		boolean found=false;
		
		g = _grains.get(last);
		while((g==null || g.is_transferring()) && last>=0){
			g = _grains.get(last);
			last--;	
		}
		
		if (g!=null && !g.is_transferring()){
			found=true;
		}
		
		if (found){
			_countgrains--;
			g.set_transferring(true,this,to);
			to.transfer(g);
		}		
		
		_triggered = true;
	}
	
	public void transfer(Grain g){
		_triggered=true;
		double transferringtime = (g.get_codesize()*1.0)/Configuration.C[g._from._indexpile][g._to._indexpile];
		g.set_transferring_time(transferringtime);
		_transfer.add(g);
	}
	
	
	public int size(){
		return _countgrains;
	}
	
	public int size_transfer(){
		return _countgrains+_transfer.size();
	}
	
		
	public boolean isempty(){
		return (_transfer.size() > 0 || _grains.size() > 0)?false:true; 
	}
	
	protected void abort_transfer(Grain g){
		_triggered=true;
		
		g._to._transfer.remove(g._to._transfer.indexOf(g));
		g.resetvaluesingrain();
	}
	
	public int get_indexpile(){
		return _indexpile;
	}
	
	
	
	public int get_topple() {
		return topple;
	}

	public static void main(String[] args) {
		
		Configuration.C = new double[2][2];
		
		Configuration.C[0][1]=1;
		Configuration.C[1][0]=1;
		
		
		Grain g1 = new Grain();
		Grain g2 = new Grain();
		Grain g3 = new Grain();
		Grain g4 = new Grain();
		Grain g5 = new Grain();
		
		PileInNode p1 = new PileInNode(1, 0);
		PileInNode p2 = new PileInNode(1, 1);
		
		p1.push(g1);
		p1.push(g2);
		p1.push(g3);
		p1.push(g4);

		p1.graintopple(p2);
		
		p1.push(g5);
		
		p1.tic();
		p2.tic();		
		p1.tac();
		p2.tac();
		
		System.err.println();
		
		p1.tic();
		p2.tic();		
		p1.tac();
		p2.tac();
		
		System.err.println();
		
		p1.tic();
		p2.tic();		
		p1.tac();
		p2.tac();
		
		System.err.println();
		
		p1.tic();
		p2.tic();		
		p1.tac();
		p2.tac();
		
		System.err.println();
		
		p1.tic();
		p2.tic();		
		p1.tac();
		p2.tac();
		
		System.err.println();
		
		
	}
}

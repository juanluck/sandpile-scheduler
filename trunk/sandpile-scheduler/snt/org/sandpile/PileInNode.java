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
import org.config.OrderedTasks;

public class PileInNode {
	private List<Grain> _grains;
	private List<Grain> _transfer;
	private List<Grain> _processed;
	private int _countgrains=0;
	private double _proc_time=0;
	private double _speedup;
	private int _indexpile;
	private boolean _proc_empty=true;
	
	private int _localtime=0;
	private double _task_per_cycle_estimate = 1;
	
	
	public double get_task_per_cycle_estimate() {
		return _task_per_cycle_estimate;
	}



	private int topple=0;
	private int toppletransaction=0;
	private SortedMap<Integer, Integer> _frectopple;
	
	private int _abort=0;
	private int _proc_tasks=0;
	
	boolean _triggered = false; // Any change in the pile activates the updatePile process
	
	
	
	
	public PileInNode(double speedup, int indexpile) {
		
		_indexpile = indexpile;
		_speedup = speedup;
		_grains = new ArrayList<Grain>();
		_transfer = new ArrayList<Grain>();
		_processed = new ArrayList<Grain>();
		_frectopple = new TreeMap<Integer, Integer>();
		
		
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
	
	//TIC for network transactions
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
	
	// TAC for processors
	//It returns the number of processed tasks to compute the throughput
	public int tac(){

		//if (Configuration.notac)
		//	return 0;
		
		int processedtask=0;
		if (_grains.size()>0 || _proc_time>0){ // If there are grains in the pile or a process to process, otherwise it waits
			while (_proc_time/(_speedup*1.0)<1.0 && _grains.size()>0){
				Grain g = fifoget();
				processedtask++;
				_proc_time+=g.get_runtime();
				_processed.add(g);
			}
			
			_localtime ++;
			_task_per_cycle_estimate = _processed.size()/(_localtime*1.0);
 
			
			_proc_time-=_speedup;
			
			for(int i=0;i<_grains.size();i++){
				_grains.get(i).increase_flowtime();
			}
			
			if(_proc_time<0)
				_proc_time=0;
			if (_proc_time>0){
				_proc_empty=false;
			}else
				_proc_empty=true;
		}
		
		return processedtask;
	}
	
	public void addgrainsintransaction(int numbergrains){
		toppletransaction+=numbergrains;
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
		return (_transfer.size() > 0 || _grains.size() > 0 || !_proc_empty)?false:true; 
	}
	
	protected void abort_transfer(Grain g){
		_triggered=true;
		_abort++;
		g._to._transfer.remove(g._to._transfer.indexOf(g));
		g.resetvaluesingrain();
	}
	
	public int get_indexpile(){
		return _indexpile;
	}
	
	
	
	public int get_topple() {
		return topple;
	}
	
	public int get_topple_transaction() {
		int aux = toppletransaction;
		toppletransaction = 0;
		return aux;
	}
	
	public void add_frectopple(int value){
		

		
		if (_frectopple.containsKey(new Integer(value))){
			int frec = _frectopple.get(new Integer(value)).intValue();
			frec++;
			_frectopple.put(new Integer(value), new Integer(frec));
		
		}else{
			_frectopple.put(new Integer(value), new Integer(1));
		}

		//print_topple_frequencies();
		
	}
	
	
  public void print_topple_frequencies(){
		
		Integer key;
		String frequencies="";
		Iterator<Integer> it = _frectopple.keySet().iterator();
		System.out.println("<-------Pile "+_indexpile);
		while(it.hasNext()){
			key=it.next();
			System.out.println(key.toString()+" "+_frectopple.get(key).toString());
		}
		System.out.println("------->");

	}
	
	public SortedMap<Integer, Integer> get_freqtopple(){
		return _frectopple;
	}
	
	public int get_abort(){
		return _abort;
	}

	public List<Grain> get_processed(){
		return _processed;
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

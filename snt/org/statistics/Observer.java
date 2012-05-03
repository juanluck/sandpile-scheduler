package org.statistics;


import java.util.ArrayList;
import java.util.Iterator;

public class Observer {

	protected ArrayList<IObservable> _observable;
	protected ArrayList<ITermination> _termination;
	protected static long _time;
	protected long _counttime;
	protected long _sample;
	protected long _prints = 1;
	protected boolean _finish = false;
	
	
	public Observer(long sample) {
		_observable = new ArrayList<IObservable>();
		_termination = new ArrayList<ITermination>();
		_time = System.currentTimeMillis();
		_sample = sample;
	}
	
	public static void restart(){
		_time = System.currentTimeMillis();
		Termination.reset();
	}
	
	public synchronized void Update(){
 
		if (!_finish && shouldprint()){
			_counttime = System.currentTimeMillis() - _time;
			String output = ""+_counttime+" ";
			
			for (Iterator<IObservable> it =_observable.iterator();it.hasNext();){
				output += it.next().getStateAsString()+" ";
			}
			output += "\n";
			if (Configuration.logfile.equals(""))
				System.out.print(output);
			else{
				Logger.append(Configuration.logfile, output);
			}
		}
	}
	
	
	public void registerObservable(IObservable obs){
		_observable.add(obs);
		if (obs instanceof ITermination) {
			_termination.add((ITermination)obs);
		}
		
	}
	
	private boolean shouldprint(){
		boolean print = false;
		 
		if (Termination.get_n_evaluation() >=  _sample * _prints){
			print = true;
			_prints++;
			
		}else if(Configuration.termination_max_cycles != 1000){
			if(Termination.get_n_generation() > _prints ){
				_prints++;
				print=true;
			}
		}
		
		boolean finish = false;
		for(Iterator<ITermination> ev=_termination.iterator();ev.hasNext() && !finish;){
			finish = ev.next().isFinish();
		}
		_finish = finish;
		return (finish || print);
	}
}

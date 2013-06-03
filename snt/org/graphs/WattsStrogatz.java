package org.graphs;


import java.util.ArrayList;

import org.config.Configuration;
import org.sandpile.Node;

import random.CommonState;

public class WattsStrogatz{
	private Node[] _proc;
	private boolean[][] _graph;
	
	
	public WattsStrogatz(Node[] nodes){
		_proc = nodes;
		_graph = new boolean[Configuration.q][Configuration.q];
		
		int simetric_degree =4;// Node degree is k=4
		

		if(Configuration.topology.equals("ring")){
			//Building a regular ring NON toroidal
			for(int i=0;i<Configuration.q;i++)
				for (int j=0;j<simetric_degree;j++){
					if (i+(j+1)<Configuration.q)
						_graph[i][i+(j+1)] = true;
					if(i-(j+1)>-1)
						_graph[i][i-(j+1)] = true;
				}			
		}else if(Configuration.topology.equals("vonneumann")){//Grid lattice
			//Building a toroidal grid implementing a von Neumann neighborhood
			//WARNING: In the current implementation we only considered a squared topology lxl, 
			//therefore, Configuration.q should be the square of a number l.
			
			//TODO: To allow different topologies lxh, new configuration lines should define either l and h in config.Configuration
			int l = (int)Math.sqrt(Configuration.q);
			int h = l;
			
			for(int i=0;i<Configuration.q;i++){
				if (i%l!=0)
					_graph[i][i-1] = true;
				if (i%l!=l-1)
					_graph[i][i+1] = true;
				if (i>=l)
					_graph[i][i-l] = true;
				if (i < l*(h-1))
					_graph[i][i+l] = true;
					
				//_graph[i][  (i%l==0)     ?  i+l-1    : i-1  ] = true; //left
				//_graph[i][ (i%l==l-1)    ?  i-l+1    : i+1  ] = true; //right
				//_graph[i][   (i < l)     ? i+l*(h-1) : i-l  ] = true; //up
				//_graph[i][ (i >= l*(h-1)) ? i-l*(h-1) : i+l  ] = true; //down
				}
			
		}else{//WS
			//Building a regular ring TOROIDAL
			for(int i=0;i<Configuration.q;i++)
				for (int j=0;j<simetric_degree;j++){
					_graph[i][(i+(j+1))%Configuration.q] = true;
					_graph[i][((i-(j+1))%Configuration.q > -1) ? (i-(j+1))%Configuration.q : Configuration.q+((i-(j+1))%Configuration.q)] = true;
				}
			
			// A hack for setting a random topology if topology=random
			if (Configuration.topology.equals("random")) 
				Configuration.rewiring = 1;
			
			//Rewiring
			for(int i=0;i<Configuration.q;i++){
				for(int j=0;j<i;j++){
					if(_graph[i][j] && CommonState.r.nextDouble()<Configuration.rewiring){ //We initialize with a watts strogatz graph
						int node = CommonState.r.nextInt(Configuration.q);
						while(_graph[i][node] || node==i){
							node = (node+1)%Configuration.q; 
						}
						_graph[i][j] = false;
						_graph[j][i] = false;
						_graph[i][node] =true;
						_graph[node][i] = true;
					}
				}
			}
			
		}
			
			
		
		//To paint the connection matrix
//		System.out.println();
//		for(int i=0;i<Configuration.q;i++){
//			for(int j=0;j<Configuration.q;j++){
//				if (i==j){
//					System.out.print(" -");
//				}else if (_graph[i][j]){
//					System.out.print(" X");
//				}else
//					System.out.print(" 0");
//			}
//			System.out.println();
//		}
		
		
		
	}
	
	
	public ArrayList<Node> getNeighborhoodforNode(int index){
		
		ArrayList<Node> nn = new ArrayList<Node>();
		
		for(int i=0;i<Configuration.q;i++){
			if(_graph[index][i])
				nn.add(_proc[i]);
		}
		
		//System.err.println(nn.size());
		return nn;
	}
	
	public Node getProcessor(int index) {
		return _proc[index];
	}

	public Node getRandomProc() {
		return _proc[CommonState.r.nextInt(_proc.length)];
	}
	
	public String getStateAsString(){
	
		return null;
	}
	
	public int size(){
		return _proc.length;
	}
		
}
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
		
		//Building a regular ring
		for(int i=0;i<Configuration.q;i++)
			for (int j=0;j<simetric_degree;j++){
				_graph[i][(i+(j+1))%Configuration.q] = true;
				_graph[i][((i-(j+1))%Configuration.q > -1) ? (i-(j+1))%Configuration.q : Configuration.q+((i-(j+1))%Configuration.q)] = true;
			}
		
		
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
	
	
	public ArrayList<Node> getNeighborhoodforNode(int index){
		
		ArrayList<Node> nn = new ArrayList<Node>();
		
		for(int i=0;i<Configuration.q;i++){
			if(_graph[index][i])
				nn.add(_proc[i]);
		}
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
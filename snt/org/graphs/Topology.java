package org.graphs;


import java.util.ArrayList;

import org.config.Configuration;
import org.graphics.GraphStreamWrapper;
import org.sandpile.Node;

import random.CommonState;

public class Topology{
	private Node[] _proc;
	private boolean[][] _graph;
	private static GraphStreamWrapper _gs;
	



	public Topology(Node[] nodes){
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
		}else if(Configuration.topology.equals("grid")){
			if (Configuration.neighborhood.equals("vonneumann") || Configuration.neighborhood.equals("vnratio1")){
				//Building a toroidal grid implementing a von Neumann neighborhood
				//WARNING: In the current implementation we only considered a squared topology lxl, 
				//therefore, Configuration.q should be the square of a number l.
				
				//TODO: To allow different topologies lxh, the settings should define either l and h in config.Configuration
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
			}else if (Configuration.neighborhood.equals("vnratio2")){
				//Building a toroidal grid implementing a von Neumann neighborhood
				//WARNING: In the current implementation we only considered a squared topology lxl, 
				//therefore, Configuration.q should be the square of a number l.
				
				//TODO: To allow different topologies lxh, the settings should define either l and h in config.Configuration
				int l = (int)Math.sqrt(Configuration.q);
				int h = l;
				
				for(int i=0;i<Configuration.q;i++){
					if (i%l!=0)//W
						_graph[i][i-1] = true;
					if (i%l!=l-1)//E
						_graph[i][i+1] = true;
					if (i>=l)//N 
						_graph[i][i-l] = true;
					if (i < l*(h-1))//S
						_graph[i][i+l] = true;
					
					if (i>l && i%l!=0) //Diagonal NW
						_graph[i][i-l-1] = true;
					
					if (i>l && i%l!=l-1) //Diagonal NE
						_graph[i][i-l+1] = true;
					
					if (i<l*(h-1) && i%l!=0) //Diagonal SW
						_graph[i][i+l-1] = true;
					
					if (i<l*(h-1) && i%l!=l-1) //Diagonal SE
						_graph[i][i+l+1] = true;
					
					if (i%l>1)//W 2
						_graph[i][i-2] = true;
					if (i%l<l-2)//E 2
						_graph[i][i+2] = true;
					if (i>=(2*l))//N 2
						_graph[i][i-(2*l)] = true;
					if (i < l*(h-2))//S 2
						_graph[i][i+(2*l)] = true;
						
					//_graph[i][  (i%l==0)     ?  i+l-1    : i-1  ] = true; //left
					//_graph[i][ (i%l==l-1)    ?  i-l+1    : i+1  ] = true; //right
					//_graph[i][   (i < l)     ? i+l*(h-1) : i-l  ] = true; //up
					//_graph[i][ (i >= l*(h-1)) ? i-l*(h-1) : i+l  ] = true; //down
					}
			}else if (Configuration.neighborhood.equals("vnratio3")){
				//Building a toroidal grid implementing a von Neumann neighborhood with ratio=3
				//WARNING: In the current implementation we only considered a squared topology lxl, 
				//therefore, Configuration.q should be the square of a number l.
				
				//TODO: To allow different topologies lxh, the settings should define either l and h in config.Configuration
				int l = (int)Math.sqrt(Configuration.q);
			
				for(int i=0;i<Configuration.q;i++){
					recursiveVNneighborhood(l, i, i, 3);
				}
				//recursiveVNneighborhood(l, Configuration.q-1, Configuration.q-1, 15);
			}else if (Configuration.neighborhood.equals("moore")){
				//Building a toroidal grid implementing a Moore neighborhood
				//WARNING: In the current implementation we only considered a squared topology lxl, 
				//therefore, Configuration.q should be the square of a number l.
				
				//TODO: To allow different topologies lxh, the settings should define either l and h in config.Configuration
				int l = (int)Math.sqrt(Configuration.q);
				int h = l;
				
				for(int i=0;i<Configuration.q;i++){
					if (i%l!=0) //W
						_graph[i][i-1] = true;
					if (i%l!=l-1) //E
						_graph[i][i+1] = true;
					if (i>=l) //N 
						_graph[i][i-l] = true;
					if (i < l*(h-1)) //S
						_graph[i][i+l] = true;
					
					if (i>l && i%l!=0) //Diagonal NW
						_graph[i][i-l-1] = true;
					
					if (i>l && i%l!=l-1) //Diagonal NE
						_graph[i][i-l+1] = true;
					
					if (i<l*(h-1) && i%l!=0) //Diagonal SW
						_graph[i][i+l-1] = true;
					
					if (i<l*(h-1) && i%l!=l-1) //Diagonal SE
						_graph[i][i+l+1] = true;
					
					
					
					//_graph[i][  (i%l==0)     ?  i+l-1    : i-1  ] = true; //left
					//_graph[i][ (i%l==l-1)    ?  i-l+1    : i+1  ] = true; //right
					//_graph[i][   (i < l)     ? i+l*(h-1) : i-l  ] = true; //up
					//_graph[i][ (i >= l*(h-1)) ? i-l*(h-1) : i+l  ] = true; //down
					
				}
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
			
			
		if (Configuration.display){
			initialgraph();
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
	
	/*
	 * Creates a grid lattice with a von Neumann neighborhood of ratio @param ratio
	 * @param l The grid is a lxl square grid
	 * @param index the absolute index in the l^2 list of processors
	 * @param ref When calling this function a pre-condition is that index==ref 
	 * @param radius the radius of the neighborhood. 
	 */
	private void recursiveVNneighborhood(int l, int index, int ref, int radius){
		if (radius>0){
			boolean w=false;
			if (ref%l > 0){//W
				//=if(!_graph[index][ref-1]){
				if(index!=ref-1){
					_graph[index][ref-1] = true;
					w=true;
				}
			}

			boolean n=false;
			if (ref >= l){//N 
				//if(!_graph[index][ref-l]){
				if(index!=ref-l){
					_graph[index][ref-l] = true;
					n=true;
				}
				//}
				
			}
			
			boolean e=false;
			if (ref%l < l-1){//E
				//if(!_graph[index][ref+1]){
				if(index!=ref+1){
					_graph[index][ref+1] = true;
					e=true;	
				}
				//}
			}
			
			
			boolean s=false;
			if (ref < l*(l-1)){//S
				//if (!_graph[index][ref+l]){
				if(index!=ref+l){
					_graph[index][ref+l] = true;
					s=true;
				}
				//}
			}
			
			if(w) recursiveVNneighborhood(l, index, ref-1, radius-1);
			if(n) recursiveVNneighborhood(l, index, ref-l, radius-1);
			if(e) recursiveVNneighborhood(l, index, ref+1, radius-1);
			if(s) recursiveVNneighborhood(l, index, ref+l, radius-1);
		}
	}
	
	public ArrayList<Node> getNeighborhoodforNode(int index){
		
		ArrayList<Node> nn = new ArrayList<Node>();
		
		for(int i=0;i<Configuration.q;i++){
			if(_graph[index][i]){
				nn.add(_proc[i]);
			}
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
	
	/*-------------------------------------------------------
	 * 
	 *               Displaying the graph
	 * 
	 * ------------------------------------------------------*/
	
	 private void initialgraph(){
		 _gs = new GraphStreamWrapper();
		 
		 // Vertices
		 if(Configuration.topology.equals("grid")){
			 int l = (int)Math.sqrt(Configuration.q);
				for(int i=0;i<Configuration.q;i++){
	   				_gs.addGSnode(""+i, (int)(i/l), i%l);
	   				//_gs.getGSnodeById(""+i).addAttribute("ui.hide");
				}
		 }//TODO: paint other types of topologies... ring, sw,...
		 
		 // Edges
//		 for(int i=0;i<Configuration.q;i++){
//			 for(int j=i;j<Configuration.q;j++){
//				 if (_graph[i][j])
//					 _gs.addGSedge(i+"_"+j, i, j);
//			 }
//		 }
	        sleep(Configuration.pause);		 

	 }
	 
	 private void sleep(int i) {
	        try {
	            Thread.sleep(i);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	 }

		
	public static GraphStreamWrapper get_gs() {
			return _gs;
		}

}
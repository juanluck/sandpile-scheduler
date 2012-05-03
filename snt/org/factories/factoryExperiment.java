package org.factories;


import org.Sandpile;
import org.config.Configuration;
import org.graphs.WattsStrogatz;
import org.sandpile.Node;
import org.sandpile.PileInNode;

import random.CommonState;

public class factoryExperiment {
	
	public static Sandpile createExperiment(){
		
		//Setting the seed
		CommonState.setSeed(Configuration.seed);
		
		//List of Nodes
		Node[] proc = new Node[Configuration.q];
		for (int i=0;i<Configuration.q;i++){
			PileInNode pile = new PileInNode(Configuration.p[i], i);
			proc[i] = new Node(pile);
		}
		
		//Creating the topology
		WattsStrogatz sn = new WattsStrogatz(proc);
		
		for (int i=0;i<Configuration.q;i++){
			proc[i].set_neighbours(sn.getNeighborhoodforNode(i));
		}
		
		// Creating the algorithm
		Sandpile sp = new Sandpile();
		
		// Adding the WS topology to the Sandpile
		sp.setSn(sn);
		
		return sp;
	}

}

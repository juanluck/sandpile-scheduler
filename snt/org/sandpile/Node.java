package org.sandpile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.config.Configuration;

import random.CommonState;

public class Node {

	ArrayList<Node> _neighbours;
	PileInNode _pile;
	boolean selected = false;
	private HashMap<Integer, Node> _recursivequeue = new HashMap<>();
	
	
	
	public Node(PileInNode pile) {
		_pile = pile;
	}

	public ArrayList<Node> get_neighbours() {
		return _neighbours;
	}

	public void set_neighbours(ArrayList<Node> neighbours) {
		_neighbours = neighbours;
	}

	public PileInNode get_pile() {
		return _pile;
	}
	
	public void executeUpdate(){
		if (_pile._triggered){
			selected = true;// To avoid cycles in the recursive call
			int localgrains = _pile.size();
			

			ArrayList<Node> selectedneighbours;
			if(Configuration.transition.equals("order"))
				selectedneighbours = selectTwoLessLoadedNeighbours();
			else
				selectedneighbours = selectTwoRandomNeighbours();

			int grainsneighbours=0;
			for(int i=0; i<selectedneighbours.size();i++){
				grainsneighbours+=selectedneighbours.get(i).get_pile().size_transfer();
			}
			
			if (localgrains > grainsneighbours && localgrains > 1){
				
				int avggrains = (int)((localgrains+grainsneighbours)/(selectedneighbours.size()+1));  // Number of grains transmitted to selected neighbors for routing
				int mingrains = (int)((localgrains+grainsneighbours)/10.0); ; // The avalanche stops when the number of grains are 1/8 of the initial topple
																			 // e.g. if there are 1000 task the avalanche stops for routings < 125 grains; 
																			 // The purpose is reducing the number of lookups done and then reducing communications
																			 // in the example above (1000 task) the worst-case would lead to 6 lookups, while without a limit would suppose ~126 lookups
				if (mingrains<1)
					mingrains=1;
				
				mingrains=1;
			
				
				for(int i=0; i<selectedneighbours.size();i++){
					int neighboursgrains = selectedneighbours.get(i)._pile.size_transfer();
					selectedneighbours.get(i).routingRequest(_pile, _pile.get_indexpile(), avggrains-neighboursgrains, mingrains);
					//System.out.println("GRANOS:"+(avggrains-neighboursgrains));
				}
					
			}else{
				for(int i=0; i<selectedneighbours.size();i++){
					selectedneighbours.get(i).selected=false;
				}
			}
			
			selected = false;			
		}else{
			//System.out.println("No");
		}
	
	}
	
	private void push_recursive_queue (Node node){
		if (!_recursivequeue.containsKey(new Integer (node.get_pile().get_indexpile()))){
			_recursivequeue.put(new Integer (node.get_pile().get_indexpile()), node);
		}
	}
	
	private void update_from_recursive_queue(Node origin){
		while(!_recursivequeue.isEmpty()){
			ArrayList<Integer> list = new ArrayList<>();
			for (Iterator<Integer> keys = _recursivequeue.keySet().iterator(); keys.hasNext();){list.add(keys.next());}
			
			for(int i=0;i<list.size();i++){
				Node toupdate = _recursivequeue.get(list.get(i));
				_recursivequeue.remove(new Integer(list.get(i)));
				toupdate.executevonneumannUpdate(origin, false);
			}
		}

	}
	
	
	public void executevonneumannUpdate(Node origin, boolean first){
			//selected = true;// To avoid cycles in the recursive call
			int localgrains = _pile.size();
			
			if(localgrains >= Configuration.threshold){
				
				ArrayList<Node> selectedneighbours = new ArrayList<Node>();
				for(int i = 0; i < _neighbours.size(); i++){
					//if(!_neighbours.get(i).selected){
						//_neighbours.get(i).selected = true;
						selectedneighbours.add(_neighbours.get(i));
					//}
				}
				
				for(int i = 0; i < selectedneighbours.size(); i++){
					selectedneighbours.get(i).routingClassicRequest(_pile, _pile.get_indexpile(), Configuration.migrating_tasks_to_neighbor, 1);
				}

				for(int i = 0; i < selectedneighbours.size(); i++){
					_neighbours.get(i).get_pile().tic(); // Hack for getting a classic-abelian sandpile: communications are instantaneous	
				}

				for(int i = 0; i < selectedneighbours.size(); i++){
					origin.push_recursive_queue(_neighbours.get(i));					
				}

			}

			if(this.get_pile().get_indexpile() == origin.get_pile().get_indexpile() && first){
				update_from_recursive_queue(origin);
			}
				
			//selected = false;	
	}

	
	public void executeUpdateClairvoyant(){
		if (_pile._triggered){
			selected = true;// To avoid cycles in the recursive call

			int h_alfa = _pile.size();
			double h_scale_alfa = h_alfa/_pile.get_task_per_cycle_estimate();
			double s_alfa = _pile.get_task_per_cycle_estimate();

			ArrayList<Node> selectedneighbours;
			if(Configuration.transition.equals("order"))
				selectedneighbours = selectTwoLessLoadedNeighbours();
			else
				selectedneighbours = selectTwoRandomNeighbours();


			int h_beta = 0;
			int h_gamma = 0;
			double h_scale_beta = 0;
			double h_scale_gamma = 0;
			double s_beta = 0;
			double s_gamma = 0;

			
			if(selectedneighbours.size()==2){
				h_beta = selectedneighbours.get(1)._pile.size_transfer();
				h_gamma = selectedneighbours.get(0)._pile.size_transfer();
				s_beta = selectedneighbours.get(1)._pile.get_task_per_cycle_estimate();
				s_gamma = selectedneighbours.get(0)._pile.get_task_per_cycle_estimate();
				h_scale_beta = h_beta / s_beta;
				h_scale_gamma = h_gamma / s_gamma;
				
			}else{//To make sure that no avalanche happens if there are less than 2 available neighbors
				h_scale_beta = Double.MAX_VALUE;
				h_scale_gamma = Double.MAX_VALUE;
			}

			int h_i_beta =0;
			int h_i_gamma =0;
			int h_i_alfa=0;
			if (h_scale_alfa > (h_scale_beta + h_scale_gamma) && h_alfa> 1){
			//if (h_scale_alfa > h_scale_gamma && h_alfa> 1){	
				double a = s_beta *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
				h_i_beta = (int)Math.floor(a);
				
				a = s_gamma *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
				h_i_gamma = (int)Math.floor(a);

				//a = s_gamma *((h_alfa+h_gamma)/(s_alfa+s_gamma));
				//h_i_gamma = (int)Math.floor(a);
				
				a = s_alfa *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
				h_i_alfa = (int)Math.ceil(a);
			}
			
			if ((h_i_beta >= h_beta) && (h_i_gamma >= h_gamma) && h_i_alfa> 0){
			//if ( (h_i_gamma > h_gamma) && h_alfa> 1){
				
				int remaining = h_alfa - h_i_alfa;
				int migr_beta = h_i_beta - h_beta;
				remaining -= migr_beta;
				int migr_gamma = remaining;
				
				//System.err.println("-"+h_alfa+" "+migr_beta+" "+migr_gamma);
				
				//int minimum_migration = Math.min(migr_alfa_to_beta, migr_alfa_to_gamma);
				selectedneighbours.get(1).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_beta, 1);
				selectedneighbours.get(0).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_gamma, 1);
				
//				if(migr_beta>=0 && migr_gamma>=0){
//					selectedneighbours.get(1).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_beta, 1);
//					selectedneighbours.get(0).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_gamma, 1);
//				}else if(migr_beta>=0){
//					while(migr_alfa+migr_beta+migr_gamma<0){
//						migr_gamma++;
//					}
//					selectedneighbours.get(1).routingRequestClairvoyant(_pile, _pile.get_indexpile(), Math.abs(migr_alfa), 1);
//					selectedneighbours.get(1).routingRequestClairvoyant(selectedneighbours.get(0)._pile, selectedneighbours.get(0)._pile.get_indexpile(), Math.abs(migr_gamma), 1);
//				}else if(migr_gamma>=0){
//					while(migr_alfa+migr_beta+migr_gamma<0){
//						migr_beta++;
//					}
//					selectedneighbours.get(0).routingRequestClairvoyant(_pile, _pile.get_indexpile(), Math.abs(migr_alfa), 1);
//					selectedneighbours.get(0).routingRequestClairvoyant(selectedneighbours.get(1)._pile, selectedneighbours.get(1)._pile.get_indexpile(), Math.abs(migr_beta), 1);
//				}
				
//				selectedneighbours.get(1).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_beta, 1);
//				selectedneighbours.get(0).routingRequestClairvoyant(_pile, _pile.get_indexpile(), migr_gamma, 1);
				
				for(int i=0; i<selectedneighbours.size();i++){
					selectedneighbours.get(i).selected=false;
				}
									
			}else{
				for(int i=0; i<selectedneighbours.size();i++){
					selectedneighbours.get(i).selected=false;
				}
			}
			
			selected = false;			
		}else{
			//System.out.println("No");
		}
	
	}
	
	
	public void executeUpdateLiquid(){
		if (_pile._triggered){
			selected = true;// To avoid cycles in the recursive call
			int localgrains = _pile.size();
			

			ArrayList<Node> selectedneighbours;
			selectedneighbours = selectLessLoadedNeighbour();

			int grainsneighbours=0;
			for(int i=0; i<selectedneighbours.size();i++){
				grainsneighbours+=selectedneighbours.get(i).get_pile().size_transfer();
			}
			
			if (localgrains > grainsneighbours && localgrains > 1){
				
				int avggrains = (int)((localgrains+grainsneighbours)/(selectedneighbours.size()+1));  // Number of grains transmitted to selected neighbors for routing
				int mingrains = (int)((localgrains+grainsneighbours)/10.0); ; // The avalanche stops when the number of grains are 1/8 of the initial topple
																			 // e.g. if there are 1000 task the avalanche stops for routings < 125 grains; 
																			 // The purpose is reducing the number of lookups done and then reducing communications
																			 // in the example above (1000 task) the worst-case would lead to 6 lookups, while without a limit would suppose ~126 lookups
				if (mingrains<1)
					mingrains=1;
				
				mingrains=1;
			
				
				for(int i=0; i<selectedneighbours.size();i++){
					int neighboursgrains = selectedneighbours.get(i)._pile.size_transfer();
					selectedneighbours.get(i).routingRequestLiquid(_pile, _pile.get_indexpile(), avggrains-neighboursgrains, mingrains);
					//System.out.println("GRANOS:"+(avggrains-neighboursgrains));
				}
					
			}else{
				for(int i=0; i<selectedneighbours.size();i++){
					selectedneighbours.get(i).selected=false;
				}
			}
			
			selected = false;			
		}else{
			//System.out.println("No");
		}
	
	}
	
	public void routingRequestClairvoyant(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
		selected = true;
		
		if (virtualgrains > 0){
			if (!Configuration.forwarding || virtualgrains <= minimumnumberofgrains){ // In case that the number of grains to root is smaller (or equal) to the pre-established minimum
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
				if(virtualgrains>0)
					origin.addgrainsintransaction(virtualgrains);
			}else{
				
				int h_alfa = _pile.size_transfer();
				int v_h_alfa= h_alfa+virtualgrains;
				double h_scale_alfa = v_h_alfa/_pile.get_task_per_cycle_estimate();
				double s_alfa = _pile.get_task_per_cycle_estimate();

				ArrayList<Node> selectedneighbours;
				if(Configuration.transition.equals("order"))
					selectedneighbours = selectTwoLessLoadedNeighbours();
				else
					selectedneighbours = selectTwoRandomNeighbours();

				
				int h_beta = 0;
				int h_gamma = 0;
				double h_scale_beta = 0;
				double h_scale_gamma = 0;
				double s_beta = 0;
				double s_gamma = 0;

				
				if(selectedneighbours.size()==2){
					h_beta = selectedneighbours.get(1)._pile.size_transfer();
					h_gamma = selectedneighbours.get(0)._pile.size_transfer();
					s_beta = selectedneighbours.get(1)._pile.get_task_per_cycle_estimate();
					s_gamma = selectedneighbours.get(0)._pile.get_task_per_cycle_estimate();
					h_scale_beta = h_beta / s_beta;
					h_scale_gamma = h_gamma / s_gamma;
					
				}else{//To make sure that no avalanche happens if there are less than 2 available neighbors
					System.err.println("NOO");
					h_scale_beta = Double.MAX_VALUE;
					h_scale_gamma = Double.MAX_VALUE;
				}

				int h_i_beta = 0;
				int h_i_gamma = 0;
				int h_i_alfa = 0;
				if (h_scale_alfa > (h_scale_gamma+h_scale_beta) && h_alfa> 1){	
					double a = s_beta *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
					h_i_beta = (int)Math.floor(a);
					
					a = s_gamma *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
					h_i_gamma = (int)Math.floor(a);

					//a = s_gamma *((h_alfa+h_gamma)/(s_alfa+s_gamma));
					//h_i_gamma = (int)Math.floor(a);
					
					a = s_alfa *((h_alfa+h_gamma+h_beta)/(s_alfa+s_gamma+s_beta));
					h_i_alfa = (int)Math.ceil(a);
				}

				
				if ((h_i_beta >= h_beta) && (h_i_gamma >= h_gamma) && h_alfa> 1){
					

					

					int migr_alfa_to_beta = h_i_beta - h_beta;
					int migr_alfa_to_gamma = h_i_gamma - h_gamma;
					int migr_alfa = h_i_alfa - h_alfa;
					
					//System.err.println(migr_alfa +" "+migr_beta+" "+migr_gamma+" "+(migr_alfa+migr_beta+migr_gamma));

					
					// Here we deal with the restriction of the maximum nr. of virtualgrains we have.
					// migrations to beta or gamma can not surpass such a treshold
					// remaining stands for the number of virtualgrains to which no decision has been yet taken
					// We preferentially submit the request to gamma since it is the less loaded node
					int remaining = virtualgrains;

					//Routing to Gamma
					if (remaining >= migr_alfa_to_gamma){
						//System.err.println(""+remaining+" "+migr_alfa_to_gamma);
						remaining -= migr_alfa_to_gamma;
						selectedneighbours.get(0).routingRequestClairvoyant(origin, origin.get_indexpile(), migr_alfa_to_gamma, 1);	
					}else{
						selectedneighbours.get(0).routingRequestClairvoyant(origin, origin.get_indexpile(), remaining, 1);
						remaining = 0;
					}
						
					//Routing to Beta
					if (remaining >= migr_alfa_to_beta){
						remaining -= migr_alfa_to_beta;
						selectedneighbours.get(1).routingRequestClairvoyant(origin, origin.get_indexpile(), migr_alfa_to_beta, 1);	
					}else{
						selectedneighbours.get(1).routingRequestClairvoyant(origin, origin.get_indexpile(), remaining, 1);
						remaining = 0;
					}
						
					//Remaining grains/tasks are computed locally
					if (remaining >0){
						for(int i=0;i<remaining;i++)
							origin.graintopple(_pile);
						if(virtualgrains>0)
							origin.addgrainsintransaction(remaining);
					}
						
					for(int i=0; i<selectedneighbours.size();i++){
						selectedneighbours.get(i).selected=false;
					}
					
										
				}else{// If we do not migrate to the neighbors, we compute grains locally
					for(int i=0;i<virtualgrains;i++)
						origin.graintopple(_pile);
					if(virtualgrains>0)
						origin.addgrainsintransaction(virtualgrains);
					
					for(int i=0; i<selectedneighbours.size();i++){
						selectedneighbours.get(i).selected=false;
					}
				}
			}
		}	
		selected = false;
	}

	

	
	public void routingClassicRequest(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
	
		if (virtualgrains > 0){
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
				if(virtualgrains>0)
					origin.addgrainsintransaction(virtualgrains);
		}
		
	}

	
	public void routingRequest(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
		selected = true;
		
		if (virtualgrains > 0){
			if (!Configuration.forwarding || virtualgrains <= minimumnumberofgrains){ // In case that the number of grains to root is smaller (or equal) to the pre-established minimum
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
				if(virtualgrains>0)
					origin.addgrainsintransaction(virtualgrains);
			}else{
				int localgrains= _pile.size_transfer();
				int vlocalgrains= localgrains+virtualgrains;
				
				ArrayList<Node> selectedneighbours;
				if(Configuration.transition.equals("order"))
					selectedneighbours = selectTwoLessLoadedNeighbours();
				else
					selectedneighbours = selectTwoRandomNeighbours();
				
				if(selectedneighbours.size()==0){ // In case that all selectedneighbors already belong to the avalanche
					for(int i=0;i<virtualgrains;i++)
						origin.graintopple(_pile);
					if(virtualgrains>0)
						origin.addgrainsintransaction(virtualgrains);
				}else{
					int grainsneighbours=0;
					for(int i=0; i<selectedneighbours.size();i++){
						grainsneighbours+=selectedneighbours.get(i).get_pile().size_transfer();
					}
					
					if(vlocalgrains > grainsneighbours){
						double avggrains = (double)(((vlocalgrains+grainsneighbours)/(selectedneighbours.size()+1.0)));
						
						int grainsforlocal = ((int)(avggrains+0.99)-localgrains); 
						if(virtualgrains >=  grainsforlocal){
							for(int i=0;i<grainsforlocal;i++){ // We compute in the current node those grains that correspond to him
								origin.graintopple(_pile);
							}
							if(grainsforlocal>0)
								origin.addgrainsintransaction(grainsforlocal);
						}else{
							System.err.println("ESTO NO DEBERIA DE DARSE");
						}
						
						int remaining = virtualgrains - grainsforlocal;
							
						int enviados=0;
						for(int i=0; i<selectedneighbours.size() ; i++){
							int neighboursgrains = selectedneighbours.get(i)._pile.size_transfer();
							int grainsforneighbour = (int) avggrains - neighboursgrains;
							if (remaining > 0){
								if(grainsforneighbour < remaining){
									selectedneighbours.get(i).routingRequest(origin, origin.get_indexpile(), grainsforneighbour, minimumnumberofgrains);
									remaining-=grainsforneighbour;
									enviados+=grainsforneighbour;
								}else{
									selectedneighbours.get(i).routingRequest(origin, origin.get_indexpile(), remaining, minimumnumberofgrains);
									remaining = 0;
									enviados+=remaining;
								}
							}else{
								selectedneighbours.get(i).selected=false;
							}
						}
						//System.out.println("Envio: "+enviados);
						
						
						
					}else{
						for(int i=0; i<selectedneighbours.size();i++){
							selectedneighbours.get(i).selected=false;
						}
						for(int i=0;i<virtualgrains;i++){ // We compute all virtualgrains in the current node
							origin.graintopple(_pile);
						}
						if(virtualgrains>0)
							origin.addgrainsintransaction(virtualgrains);
					}
				}	
			}
		}
		
		
		selected = false;
	}
	
	
	public void routingRequestLiquid(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
		selected = true;
		
		if (virtualgrains > 0){
			if (!Configuration.forwarding || virtualgrains <= minimumnumberofgrains){ // In case that the number of grains to root is smaller (or equal) to the pre-established minimum
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
				if(virtualgrains>0)
					origin.addgrainsintransaction(virtualgrains);
			}else{
				int localgrains= _pile.size_transfer();
				int vlocalgrains= localgrains+virtualgrains;
				
				ArrayList<Node> selectedneighbours;
				selectedneighbours = selectLessLoadedNeighbour();
				
				if(selectedneighbours.size()==0){ // In case that all selectedneighbors already belong to the avalanche
					for(int i=0;i<virtualgrains;i++)
						origin.graintopple(_pile);
					if(virtualgrains>0)
						origin.addgrainsintransaction(virtualgrains);
				}else{
					int grainsneighbours=0;
					for(int i=0; i<selectedneighbours.size();i++){
						grainsneighbours+=selectedneighbours.get(i).get_pile().size_transfer();
					}
					
					if(vlocalgrains > grainsneighbours){
						double avggrains = (double)(((vlocalgrains+grainsneighbours)/(selectedneighbours.size()+1.0)));
						
						int grainsforlocal = ((int)(avggrains+0.99)-localgrains); 
						if(virtualgrains >=  grainsforlocal){
							for(int i=0;i<grainsforlocal;i++){ // We compute in the current node those grains that correspond to him
								origin.graintopple(_pile);
							}
							if(grainsforlocal>0)
								origin.addgrainsintransaction(grainsforlocal);
						}else{
							System.err.println("ESTO NO DEBERIA DE DARSE");
						}
						
						int remaining = virtualgrains - grainsforlocal;
							
						int enviados=0;
						for(int i=0; i<selectedneighbours.size() ; i++){
							int neighboursgrains = selectedneighbours.get(i)._pile.size_transfer();
							int grainsforneighbour = (int) avggrains - neighboursgrains;
							if (remaining > 0){
								if(grainsforneighbour < remaining){
									selectedneighbours.get(i).routingRequestLiquid(origin, origin.get_indexpile(), grainsforneighbour, minimumnumberofgrains);
									remaining-=grainsforneighbour;
									enviados+=grainsforneighbour;
								}else{
									selectedneighbours.get(i).routingRequestLiquid(origin, origin.get_indexpile(), remaining, minimumnumberofgrains);
									remaining = 0;
									enviados+=remaining;
								}
							}else{
								selectedneighbours.get(i).selected=false;
							}
						}
						//System.out.println("Envio: "+enviados);
						
						
						
					}else{
						for(int i=0; i<selectedneighbours.size();i++){
							selectedneighbours.get(i).selected=false;
						}
						for(int i=0;i<virtualgrains;i++){ // We compute all virtualgrains in the current node
							origin.graintopple(_pile);
						}
						if(virtualgrains>0)
							origin.addgrainsintransaction(virtualgrains);
					}
				}	
			}
		}
		
		
		selected = false;
	}
				
	public void executeUpdate2(){
		
		selected = true;// To avoid cycles in the recursive call
		int localgrains = _pile.size();
		

		ArrayList<Node> selectedneighbours = selectTwoRandomNeighbours();

		int grainsneighbours=0;
		Node minimumsize=null;
		for(int i=0; i<selectedneighbours.size();i++){
			grainsneighbours+=selectedneighbours.get(i).get_pile().size();
			if(minimumsize==null || selectedneighbours.get(i).get_pile().size() < minimumsize.get_pile().size() )
				minimumsize = selectedneighbours.get(i);
		}
		
		if (localgrains > grainsneighbours && localgrains > 1){
			
			int avggrains = (int)((localgrains+minimumsize.get_pile().size())/(2));  // Number of grains transmitted to selected neighbors for routing
			int mingrains = (int)((localgrains+minimumsize.get_pile().size())/10.0); ; // The avalanche stops when the number of grains are 1/8 of the initial topple
																		 // e.g. if there are 1000 task the avalanche stops for routings < 125 grains; 
																		 // The purpose is reducing the number of lookups done and then reducing communications
																		 // in the example above (1000 task) the worst-case would lead to 6 lookups, while without a limit would suppose ~126 lookups
			if (mingrains<1)
				mingrains=1;
		
			int neighboursgrains = minimumsize.get_pile().size();
			minimumsize.routingRequest2(_pile, _pile.get_indexpile(), avggrains-neighboursgrains, mingrains);
							
		}// Otherwise do nothing
		
		selected = false;
	}
	
	public void routingRequest2(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
		selected = true;
		
		if (virtualgrains > 0){
			if (virtualgrains <= minimumnumberofgrains){ // In case that the number of grains to root is smaller (or equal) to the pre-established minimum
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
			}else{
				int localgrains= _pile.size();
				int vlocalgrains= localgrains+virtualgrains;
				
				ArrayList<Node> selectedneighbours = selectTwoRandomNeighbours();
				
				if(selectedneighbours.size()==0){ // In case that all selectedneighbors already belong to the avalanche
					for(int i=0;i<virtualgrains;i++)
						origin.graintopple(_pile);
				}else{
					int grainsneighbours=0;
					Node minimumsize=null;
					
					for(int i=0; i<selectedneighbours.size();i++){
						grainsneighbours+=selectedneighbours.get(i).get_pile().size();
						if(minimumsize==null || selectedneighbours.get(i).get_pile().size() < minimumsize.get_pile().size() )
							minimumsize = selectedneighbours.get(i);
					}
					
					if(vlocalgrains > grainsneighbours){
						int avggrains = (int)(((vlocalgrains+minimumsize.get_pile().size())/(2)));
						int maxtotransmit=0;
						if ((avggrains) > virtualgrains){
							maxtotransmit = virtualgrains;
						}else{
							maxtotransmit = avggrains;
						}
						
						int totaltransmitted = 0;
						
						int neighboursgrains = minimumsize._pile.size();
						if (neighboursgrains < maxtotransmit){
							minimumsize.routingRequest2(origin, _pile.get_indexpile(), maxtotransmit-neighboursgrains, minimumnumberofgrains);
							totaltransmitted = maxtotransmit-neighboursgrains;
						}
						
						
						if((virtualgrains-totaltransmitted) < 0){
							System.err.println("ALGO VA MUY MUY MAL!!");
						}
						
						for(int i=0;i<(virtualgrains-totaltransmitted);i++){ // We compute in the current node those grains that correspond to him
							origin.graintopple(_pile);
						}
						
					}else{
						for(int i=0;i<virtualgrains;i++){ // We compute all virtualgrains in the current node
							origin.graintopple(_pile);
						}
					}
				}	
			}
		}
		
		
		selected = false;
	}
	
	
	private ArrayList<Node> selectTwoRandomNeighbours(){

		ArrayList<Node> toreturn = new ArrayList<Node>();
		
		int freeneighbors =0;
				

		//We order in a SortedMap the workloads of the different neighbours.
		ArrayList<Integer> tochoose = new ArrayList<Integer>();
		for(int i=0;i<_neighbours.size();i++){
			if(!_neighbours.get(i).selected){
				freeneighbors++;
				tochoose.add(new Integer(i));	
				
			}
		}
		
		int index1=-1;
		int index2=-1;
		if (tochoose.size()>=2){
			index1 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();
			while((index2 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue()) == index1);
		}
		
		
		if (index1!=-1 && index2!=-1){
			_neighbours.get(index1).selected=true;
			toreturn.add(_neighbours.get(index1));
			_neighbours.get(index2).selected=true;
			toreturn.add(_neighbours.get(index2));
		}
		
		return toreturn;
	}
	
	private ArrayList<Node> selectTwoLessLoadedNeighbours(){

		ArrayList<Node> toreturn = new ArrayList<Node>();
		
		int freeneighbors =0;
				

		//We order in a SortedMap the workloads of the different neighbours.
		SortedMap<Double, ArrayList<Integer>> wloads= new TreeMap<Double, ArrayList<Integer>>();
		for(int i=0;i<_neighbours.size();i++){
			if(!_neighbours.get(i).selected){
				freeneighbors++;
				double pilesize =0;
				if (_neighbours.get(i)._pile.size_transfer()==0)
					pilesize = 0.1;
				else
					pilesize = _neighbours.get(i)._pile.size_transfer();
				Double wl;
				if (Configuration.clairvoyance)
					wl = new Double(pilesize/(_neighbours.get(i)._pile.get_task_per_cycle_estimate()));
				else
					wl = new Double(pilesize);
				
				if (wloads.containsKey(wl)){//We add in a list the neighbours with the same workloads
					wloads.get(wl).add(new Integer(i));
				}else{
					ArrayList<Integer> aux =new ArrayList<Integer>();
					aux.add(new Integer(i));
					wloads.put(wl, aux);
				}	
			}
		}
		
		//We select the two neighbours with smaller workloads (if equal we choose randomly)
		int index1 = -1;
		int index2 = -1;
		Set<Double> workloads = wloads.keySet();
		
		for(Iterator<Double> it = workloads.iterator();it.hasNext() && (index1==-1 || index2==-1);){
			
			Double key =it.next();
			ArrayList<Integer> tochoose = wloads.get(key);
			
			
			if (tochoose.size()==1){
				if (index1==-1)
					index1 = tochoose.get(0).intValue();
				else if (index2==-1)
					index2 = tochoose.get(0).intValue();
			}else if (tochoose.size()==2){
				if (index1==-1 && index2==-1){
					index1 = tochoose.get(0).intValue();
					index2 = tochoose.get(1).intValue();
				}else if (index2==-1){
					index2 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();
				}
			}else  if (tochoose.size()>2){
				if (index1==-1 && index2==-1){
					index1 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();
					while((index2 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue()) == index1);
				}else if (index2==-1){
					while((index2 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue()) == index1);
				}
			}
			
		}
		if(freeneighbors>=2){
			_neighbours.get(index1).selected=true;
			toreturn.add(_neighbours.get(index1));

			_neighbours.get(index2).selected=true;
			toreturn.add(_neighbours.get(index2));
		}
		
		
		//We select the neighbors beta and gamma with the larger and smaller workloads respectively (if equal we choose randomly)
//		int index1 = -1;
//		int index2 = -1;
//
//		if(freeneighbors>=2){
//			while(index1==index2){
//				//Minimun Workloaded resource
//				ArrayList<Integer> tochoose = wloads.get(wloads.firstKey());
//				index1 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();// We do it like this: in case of several resources have the same WL we choose one of them randomly
//				
//				//Maximum Workloaded resource
//				//tochoose.clear();
//				ArrayList<Integer> tochoose2 = wloads.get(wloads.lastKey());
//				index2 = tochoose2.get(CommonState.r.nextInt(tochoose2.size())).intValue();// We do it like this: in case of several resources have the same WL we choose one of them randomly
//			}
//			_neighbours.get(index1).selected=true;
//			toreturn.add(_neighbours.get(index1));
//
//			_neighbours.get(index2).selected=true;
//			toreturn.add(_neighbours.get(index2));
//		}
		
		return toreturn;
	}
	
	
	
	private ArrayList<Node> selectLessLoadedNeighbour(){

		ArrayList<Node> toreturn = new ArrayList<Node>();
		
		int freeneighbors =0;
				

		//We order in a SortedMap the workloads of the different neighbours.
		SortedMap<Double, ArrayList<Integer>> wloads= new TreeMap<Double, ArrayList<Integer>>();
		for(int i=0;i<_neighbours.size();i++){
			if(!_neighbours.get(i).selected){
				freeneighbors++;
				double pilesize =0;
				if (_neighbours.get(i)._pile.size_transfer()==0)
					pilesize = 0.1;
				else
					pilesize = _neighbours.get(i)._pile.size_transfer();
				Double wl;
				if (Configuration.clairvoyance)
					wl = new Double(pilesize/(_neighbours.get(i)._pile.get_task_per_cycle_estimate()));
				else
					wl = new Double(pilesize);
				
				if (wloads.containsKey(wl)){//We add in a list the neighbours with the same workloads
					wloads.get(wl).add(new Integer(i));
				}else{
					ArrayList<Integer> aux =new ArrayList<Integer>();
					aux.add(new Integer(i));
					wloads.put(wl, aux);
				}	
			}
		}
		
		//We select the neighbour with smaller workloads (if equal we choose randomly)
		int index1 = -1;
		Set<Double> workloads = wloads.keySet();
		
		for(Iterator<Double> it = workloads.iterator();it.hasNext() && (index1==-1);){
			
			Double key =it.next();
			ArrayList<Integer> tochoose = wloads.get(key);
			
			
			if (tochoose.size()==1){
				if (index1==-1)
					index1 = tochoose.get(0).intValue();
			}else if (tochoose.size()>1){
				if (index1==-1){
					index1 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();
				}
			}
			
		}
		if(freeneighbors>=1){
			_neighbours.get(index1).selected=true;
			toreturn.add(_neighbours.get(index1));
		}
		
		
		//We select the neighbors beta and gamma with the larger and smaller workloads respectively (if equal we choose randomly)
//		int index1 = -1;
//		int index2 = -1;
//
//		if(freeneighbors>=2){
//			while(index1==index2){
//				//Minimun Workloaded resource
//				ArrayList<Integer> tochoose = wloads.get(wloads.firstKey());
//				index1 = tochoose.get(CommonState.r.nextInt(tochoose.size())).intValue();// We do it like this: in case of several resources have the same WL we choose one of them randomly
//				
//				//Maximum Workloaded resource
//				//tochoose.clear();
//				ArrayList<Integer> tochoose2 = wloads.get(wloads.lastKey());
//				index2 = tochoose2.get(CommonState.r.nextInt(tochoose2.size())).intValue();// We do it like this: in case of several resources have the same WL we choose one of them randomly
//			}
//			_neighbours.get(index1).selected=true;
//			toreturn.add(_neighbours.get(index1));
//
//			_neighbours.get(index2).selected=true;
//			toreturn.add(_neighbours.get(index2));
//		}
		
		return toreturn;
	}
	
	

	
	
	
	
}

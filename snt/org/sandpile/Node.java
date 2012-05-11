package org.sandpile;

import java.util.ArrayList;

import random.CommonState;

public class Node {

	ArrayList<Node> _neighbours;
	PileInNode _pile;
	boolean selected = false;
	

	
	
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
			

			ArrayList<Node> selectedneighbours = selectTwoRandomNeighbours();

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
	

	
	public void routingRequest(PileInNode origin, int sender, int virtualgrains, int minimumnumberofgrains){
		selected = true;
		
		if (virtualgrains > 0){
			if (virtualgrains <= minimumnumberofgrains){ // In case that the number of grains to root is smaller (or equal) to the pre-established minimum
				for(int i=0;i<virtualgrains;i++)
					origin.graintopple(_pile);
				if(virtualgrains>0)
					origin.addgrainsintransaction(virtualgrains);
			}else{
				int localgrains= _pile.size_transfer();
				int vlocalgrains= localgrains+virtualgrains;
				
				ArrayList<Node> selectedneighbours = selectTwoRandomNeighboursWOSender(sender);
				
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
									selectedneighbours.get(i).routingRequest(origin, _pile.get_indexpile(), grainsforneighbour, minimumnumberofgrains);
									remaining-=grainsforneighbour;
									enviados+=grainsforneighbour;
								}else{
									selectedneighbours.get(i).routingRequest(origin, _pile.get_indexpile(), remaining, minimumnumberofgrains);
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
				
				ArrayList<Node> selectedneighbours = selectTwoRandomNeighboursWOSender(sender);
				
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
				
		int index1 = -1;
		if (_neighbours.size()>0){
			 index1 = CommonState.r.nextInt(_neighbours.size());	
		}
		
		int index2 = -1;
		if (_neighbours.size()>1){
			while ((index2 = CommonState.r.nextInt(_neighbours.size()))==index1){
			}	
		}
		
		
		
		if (index1!=-1 && !_neighbours.get(index1).selected){
			_neighbours.get(index1).selected=true;
			toreturn.add(_neighbours.get(index1));
		}
		if (index2!=-1 && !_neighbours.get(index2).selected){
			_neighbours.get(index2).selected=true;
			toreturn.add(_neighbours.get(index2));
		}
		
		return toreturn;
	}
	
	
private ArrayList<Node> selectTwoRandomNeighboursWOSender(int sender){
		
		ArrayList<Node> toreturn = new ArrayList<Node>();
		int index1=-1;
		int index2=-1;
		
		if (_neighbours.size()>1){
			while((index1 = CommonState.r.nextInt(_neighbours.size())) == sender){	
			}	
		}
		
		
		if (_neighbours.size()>2){
			while ((index2 = CommonState.r.nextInt(_neighbours.size()))==index1 || index2 == sender){
			}	
		}
		
		
		
		if (index1!=-1 && !_neighbours.get(index1).selected){
			_neighbours.get(index1).selected=true;
			toreturn.add(_neighbours.get(index1));
		}
		if (index2!=-1 && !_neighbours.get(index2).selected){
			_neighbours.get(index2).selected=true;
			toreturn.add(_neighbours.get(index2));
		}
		
		return toreturn;
	}
	

	
	
	
	
}

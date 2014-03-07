package org.graphics;

import static org.graphstream.algorithm.Toolkit.randomNode;

import java.awt.Color;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;

import org.config.Configuration;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer.CloseFramePolicy;

public class GraphStreamWrapper {
	
	SingleGraph ca;
	Hashtable<String,String> params;
	Random alea;

	// default parameters
	int pause = 50; // for visualization purpose 
	boolean trace = false; // for debug
	boolean interactive = true; // allow stopping execution at some particular points
	int randomSeed; // random seed 
	boolean persistant = true; // at the end of the execution prevent the windows to close
	
	int size = 5; 
	protected String stylesheet = 
			"graph { fill-mode: none; padding: 40px;}" 
					+ "node { size: 10px; shape: box; fill-mode: dyn-plain; fill-color: white, black;}"
					+ "edge { size : 1px; fill-color: grey;}";
		
	public GraphStreamWrapper() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		
		ca = new SingleGraph("Sandpile");
        
        initialisation(); 
        if(Configuration.display) ca.display(false);
	}
	
	   /**
     * Initialization method. This method reads the parameter file and
     * other command-line arguments
     * @param the parameters
     */
    private void initialisation() {
        ca.addAttribute("ui.quality");
        ca.addAttribute("ui.antialias");                
        ca.addAttribute("ui.stylesheet", stylesheet);
    }
    
    /* ==================================================================
    *
    *               Cellular Automata Graph Creation 
    *
    * ================================================================== */
    
    public void addGSnode(String id){
    	String nd = new String(id);
		org.graphstream.graph.Node node = ca.addNode(nd);
    }

    public void addGSnode(String id,int x, int y){
    	String nd = new String(id);
		org.graphstream.graph.Node node = ca.addNode(nd);
		node.addAttribute("xy",x,y);
    }
    
    public void addGSedge(String id, int index1, int index2){
    	ca.addEdge(id, index1, index2);
    }
    
    public org.graphstream.graph.Node getGSnodeById(String id){
    	return ca.getNode(id);
    }
    
    /**
     * 
     * @param id The node id
     * @param ratio A value between 0 and 1... 0 for a white node, 1 for a black one
     */
    public void changeGSColorByLoad(String id, double ratio){
    	ca.getNode(id).setAttribute("ui.color", ratio);
    }
    
    public void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close(){
    	if (Configuration.display){
    		sleep(Configuration.pause);
    		ca.display().setCloseFramePolicy(CloseFramePolicy.EXIT);
        	ca.display().close();	
    	}
    }
 
	
	

}

package fi.ni;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fi.ni.nodenamer.datastructure.Connection;
import fi.ni.nodenamer.datastructure.Node;
import fi.ni.nodenamer.datastructure.Path;

public class ShowNodeInfo {
    Node bn;
    
    public ShowNodeInfo(Node blank_node) {
	bn = blank_node;
    }

    public void run(int steps,int bittarget) {	
	    traverseGraph(bn, steps,bittarget);
    }

    double node_pathsprob = 1;
    int    node_prob_steps_taken = 0;

   
    private void traverseGraph(Node bn, int maxpath,int bittarget) {
	Queue<Path> q = new LinkedList<Path>();
	System.out.println();	
        System.out.println("BN: "+ bn+" litcount:"+bn.getEdges_literals().size()+ " lprob:"+bn.getLiteral_prob()+"  in: "+bn.getEdges_in().size()+" out:"+bn.getEdges_out().size());
	Path p0 = new Path(bn);
	boolean limit_set = false;
	int path_length_limit = maxpath;
	
	q.add(p0);
	while (!q.isEmpty()) {
	    Path p1 = q.poll();

	    int bits = getBits(node_pathsprob);

	    if (bits > bittarget){
		if (!limit_set) {
		    path_length_limit=node_prob_steps_taken;
		    if (path_length_limit > maxpath)
			path_length_limit = maxpath;
		    limit_set = true;
		}
	    }

	    //yhtäsuusuus ei toimi.. pitää olla > !!
	    if (p1.getSteps_taken() > path_length_limit) {
		continue;
	    }

	    handleCandidateLinks(q, p1, p1.getLast_node().getEdges_in());

	    if(p1.getLast_node().isEndbcksum())   
	    {
		 //todennäköisyys summattu jo nodeen.
		 p1.update(p1.getLast_node().getEndbranch_chksum());
	   	 continue;  
	    }
	    

	    handleCandidateLinks(q, p1, p1.getLast_node().getEdges_out());
	}

    }
    Map<Node, Integer> nodes = new HashMap<Node, Integer>();
    Set<String> nodeCheksums = new HashSet<String>();
    
    public int getBits(double probability) {
  	return (int) ((-Math.log(probability) / Math.log(2)) + 0.5);
      }
    private int handleCandidateLinks(Queue<Path> q, Path p1,List<Connection> edges) {
   	if ((edges == null) || (edges.size() == 0)) {
   	    return 0;
   	}
   	int max_number=50; // karsitaan patologiset pois  //TODO huomaa tämä
   	if (edges.size() > max_number) {
   	   return 0;
   	}
   	
   	for (Connection e : edges) {
   	    Node u = e.getPointedNode();
   	    Path p2 = new Path(p1, e);
   	    
   	    Integer i = nodes.get(u);
	    if (i == null) {
		i = new Integer(p2.getSteps_taken());
		nodes.put(u, i);
		// Ei toistoja
		if(nodeCheksums.add(u.getRDFClass_name()+"."+u.getLiteralChksum()))
		{
	   	  if(u.getLiteral_prob()<1)
	     	      System.out.println(u+" litcount:"+u.getEdges_literals().size()+ " lprob:"+u.getLiteral_prob()+"  in: "+u.getEdges_in().size()+" out:"+u.getEdges_out().size());
		  node_prob_steps_taken=p1.getSteps_taken();
		  node_pathsprob *= u.getLiteral_prob();
		}
	    }

	    if (p2.addEdge(e)) {
		p2.updateProbability(u.getLiteral_prob());
		if (p2.getSteps_taken() == i) 
		{
		    q.add(p2);
		} 
	    }
	 }
   	return 1;
       }
    
    
   
  
}

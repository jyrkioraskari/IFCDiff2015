package fi.ni.model.datastructure;

import java.util.LinkedList;
import java.util.List;

public class Path {
	final LinkedList<Node> nodelist = new LinkedList<Node>();
	String checksum;
	
	public Path(Path old,Node in, String checksum)
	{
		nodelist.addAll(old.nodelist);
		nodelist.add(in);
		this.checksum=checksum;
	}
	public Path(Node n)
	{
		nodelist.add(n);
		checksum=n.getLiteralChksum();
	}

   public boolean contains(Node n)
   {
	   return nodelist.contains(n);
   }
public LinkedList<Node> getNodelist() {
	return nodelist;
}
public String getChecksum() {
	return checksum;
}
	
}

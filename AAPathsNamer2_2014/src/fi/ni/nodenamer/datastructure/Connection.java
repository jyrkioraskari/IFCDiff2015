package fi.ni.nodenamer.datastructure;

public class Connection {
    String property;
    Node points_to;
    int propertyCount = 0;

    public Connection(String property, Node points_to) {
	super();
	this.property = property;
	this.points_to = points_to;
    }

    public String getProperty() {
	return property;
    }

    public void setProperty(String property) {
	this.property = property;
    }

    public Node s() {
	return points_to;
    }

    public void setPointedNode(Node pointed) {
	this.points_to = pointed;
    }

    public Node getPointedNode() {
	return points_to;
    }

    public String toString() {
	return "Connection [property=" + property + ", pointed=" + points_to + "]";
    }

    public int getPropertyCount() {
	return propertyCount;
    }

    public void setPropertyCount(int propertyCount) {
	this.propertyCount = propertyCount;
    }

}

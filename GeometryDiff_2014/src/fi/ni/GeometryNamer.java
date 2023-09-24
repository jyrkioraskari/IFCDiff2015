package fi.ni;



public class GeometryNamer {
    
    static public void testrun() {
	GNamer gs1 = new GNamer(5);
	gs1.printPlacementGraph("c:/2014_testdata/SMC_Rakennus.ifc", "IFC");
	    }

    
    public static void main(String[] args) {
	testrun();
    }

}

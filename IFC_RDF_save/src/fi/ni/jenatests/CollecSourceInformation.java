package fi.ni.jenatests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import softhema.system.toolkits.ToolkitString;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import fi.ni.ExpressReader;
import fi.ni.IFC_CLassModelConstants;
import fi.ni.IFC_ClassModel;
import fi.ni.Thing;
import fi.ni.ifc2x3.IfcApplication;
import fi.ni.ifc2x3.IfcOwnerHistory;
import fi.ni.vo.Link;
import fi.ni.vo.ValuePair;


public class CollecSourceInformation {

    
    private Model drum_statistics_model;
    static String ns = "http://drum/diff#";

    static String rdf_filename="c:\\jo/DRUM/general_information_tbasic.ttl";

    Property p_short_name;
    Property p_created_by;
    Property p_www;
    Property p_type;

    
    public void properties()
    {
	drum_statistics_model.setNsPrefix( "ns", ns);
	drum_statistics_model.setNsPrefix( "xml", "http://www.w3.org/2001/XMLSchema#");
	p_created_by = drum_statistics_model.createProperty( ns + "created_by" );
	p_www = drum_statistics_model.createProperty( ns + "www" );
	p_short_name = drum_statistics_model.createProperty( ns + "short_name" );
	p_type = drum_statistics_model.createProperty( ns + "type" );
    } 
    
    public CollecSourceInformation() {

	this.drum_statistics_model = ModelFactory.createDefaultModel();
	try {
		drum_statistics_model.read(FileManager.get().open(rdf_filename), "","TTL");
	} catch (Exception e) {
	}

	properties();	
	addInfo("Duplex Apartment Model","Duplex_A_20110907_optimized.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles");
	//addInfo("Duplex Apartment Model","Duplex_MEP_20110907.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles");
	addInfo("Office Building Model","Office_A_20110811.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","A");
	addInfo("Office Building Model","Office_A_20110811_optimized.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","A");
	addInfo("Office Building Model","Office_S_20110811.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","S");
	//addInfo("Office Building Model","Office_MEP_20110811.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","MEP");
	addInfo("Medical/Dental Clinic Building Model","Clinic_A_20110906.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","A");
	//addInfo("Medical/Dental Clinic Building Model","Clinic_S_20110715.ifc","Building Smart Alliance","http://www.buildingsmartalliance.org/index.php/projects/commonbimfiles","S");
	//addInfo("SMC Building","SMC_Building.ifc","Solibri Modelchecker","http://www.solibri.com/solibri-model-checker.html");
	
	addInfo("Demo Structure","drum_10.ifc","Aalto University, Department of Computer Science and Engineering","http://cse.aalto.fi/");
	
	addInfo("BIEN-ZENKER Jasmin-Sun","Bien-Zenker_Jasmin-Sun-AC14-V2.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Smiley West","Smiley-West-5-Buildings-14-10-2005.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Smiley West","AC-11-Smiley-West-04-07-2007.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Office Building","Allplan-2008-Institute-Var-2-IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Office Building","AC11-Institute-Var-2-IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");	
	addInfo("Ettenheim Town", "Ettenheim-GIS-05-11-2006_optimized.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","PART02_Wilfer_200302_20070209_IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","PART03_Buderus_200406_20070209_ifc.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","PART06_Kermi_200405_20070401_IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");	
	addInfo("FZK-House","ADT-FZK-Haus-2005-2006.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("FZK-House","AC14-FZK-Haus.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("FJK-House","FJK-Project-Final.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	
	addInfo("D.C. Riverside Office Building","DC_Riverside_Bldg-LOD_100.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php");
	addInfo("D.C. Riverside Office Building","DC_Riverside-STRUC-LOD_300.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php","Structural");
	addInfo("D.C. Riverside Office Building","DC_Riverside-LOD_300-HVAC.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php","HVAC");
	
	
	FileOutputStream fstream;
	try {
	    fstream = new FileOutputStream(rdf_filename);
	    drum_statistics_model.write(fstream,"TTL");
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    /*
     * 	addInfo("BIEN-ZENKER Jasmin-Sun","C:\\jo\\models\\2\\Bien-Zenker_Jasmin-Sun-AC14-V2.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Smiley West","C:\\jo\\models2\\Smiley-West-5-Buildings-14-10-2005.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Smiley West","C:\\jo\\models2\\AC-11-Smiley-West-04-07-2007.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Office Building","C:\\jo\\models2\\Allplan-2008-Institute-Var-2-IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("Office Building","C:\\jo\\models\\2\\AC11-Institute-Var-2-IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");	
	addInfo("Ettenheim Town", "C:\\jo\\models2\\Ettenheim-GIS-05-11-2006_optimized.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","C:\\jo\\models\\2\\PART02_Wilfer_200302_20070209_IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","C:\\jo\\models\\2\\PART03_Buderus_200406_20070209_ifc.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("HVAC-Components","C:\\jo\\models\\2\\PART06_Kermi_200405_20070401_IFC.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");	
	addInfo("FZK-House","C:\\jo\\models\\ADT-FZK-Haus-2005-2006.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("FZK-House","C:\\jo\\models2\\AC14-FZK-Haus.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	addInfo("FJK-House","C:\\jo\\models\\2\\FJK-Project-Final.ifc","Karlsruhe Institute of Technology","http://www.iai.fzk.de/www-extern/index.php?id=1135&L=1");
	
	addInfo("D.C. Riverside Office Building","C:\\jo\\models\\DC_Riverside_Bldg-LOD_100.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php");
	addInfo("D.C. Riverside Office Building","C:\\jo\\models2\\DC_Riverside-STRUC-LOD_300.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php","Structural");
	addInfo("D.C. Riverside Office Building","C:\\jo\\models2\\DC_Riverside-LOD_300-HVAC.ifc","Nemetschek Vectorworks, Inc.","http://www.vectorworks.net/bim/projects.php","HVAC");
	addInfo("Tekla BIMsigh demo","C:\\jo\\IFCtest_data\\inp3.ifc","Tekla BIMsigh","http://www.teklabimsight.com/");
	addInfo("Helmisimpukka","C:\\jo\\models2\\ARK_Helmisimpukka_20100919.ifc","Skanska","http://kodit.skanska.fi/Kohteet-ja-asunnot/Kuopion-Helmisimpukka/ http://www.cs.hut.fi/~sto/ifc/","Architectural");
	addInfo("Helmisimpukka","C:\\jo\\models2\\RAK_Helmisimpukka_20111220.ifc","Skanska","http://kodit.skanska.fi/Kohteet-ja-asunnot/Kuopion-Helmisimpukka/ http://www.cs.hut.fi/~sto/ifc/","Structural");
	
	addInfo("Crane","C:\\jo\\models2\\Crane.ifc","Aalto university collection","http://www.cs.hut.fi/~sto/ifc/");
	addInfo("First Floor Vent","C:\\jo\\models2\\First_Floor_Vent.ifc","Aalto university collection","http://www.cs.hut.fi/~sto/ifc/");
	addInfo("Second Floor Vent","C:\\jo\\models2\\Second_Floor_Vent.ifc","Aalto university collection","http://www.cs.hut.fi/~sto/ifc/");
	addInfo("Ground Floor Vent","C:\\jo\\models2\\Ground_Floor_Vent.ifc","Aalto university collection","http://www.cs.hut.fi/~sto/ifc/");
	addInfo("Office Building","C:\\jo\\models2\\OfficeBuilding.ifc","Aalto university collection","http://www.cs.hut.fi/~sto/ifc/");
	
	addInfo("SMC Building","C:\\jo\\models2\\SMC Building - modified.ifc","Solibri Modelchecker","http://www.cs.hut.fi/~sto/ifc/");
	addInfo("SMC Building","C:\\jo\\models2\\SMC Building.ifc","Solibri Modelchecker","http://www.cs.hut.fi/~sto/ifc/");

     */
    
    public void addInfo(String short_name,String filename,String created_by,String www)
    {
	    filename=ToolkitString.strReplaceLike(filename, "\\","/");
	    int last=filename.lastIndexOf("/");
	    if(last>0)
	       filename=filename.substring(last+1);
	    Resource test_file;
	    test_file
		  = drum_statistics_model.createResource(filename);
	    drum_statistics_model.add(test_file,p_created_by,created_by);
	    drum_statistics_model.add(test_file,p_www,www);
	    drum_statistics_model.add(test_file,p_short_name,short_name);
	
    }

    public void addInfo(String short_name,String filename,String created_by,String www,String type)
    {
	    filename=ToolkitString.strReplaceLike(filename, "\\","/");
	    int last=filename.lastIndexOf("/");
	    if(last>0)
	       filename=filename.substring(last+1);
	    Resource test_file;
	    test_file
		  = drum_statistics_model.createResource(filename);
	    drum_statistics_model.add(test_file,p_created_by,created_by);
	    drum_statistics_model.add(test_file,p_www,www);
	    drum_statistics_model.add(test_file,p_short_name,short_name);
	    drum_statistics_model.add(test_file,p_type,type);
	
    }

    public static void main(String[] args) {
	new CollecSourceInformation();

    }

}

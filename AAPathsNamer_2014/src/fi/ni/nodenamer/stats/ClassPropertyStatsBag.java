package fi.ni.nodenamer.stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClassPropertyStatsBag {
  public long total_class_instances=0; 
  
  public class ClassData
  {
	    long total_class_property_count=0; 
	    Map<String,Long> property_map=new TreeMap<String,Long>();  // property name, instance count 
	    public long class_instance_count=0;

		public void add(String property_name) {
			Long  property_value=property_map.get(property_name);			   
			property_map.put(property_name, property_value == null ? 1L : ++property_value);
            total_class_property_count++;			
		}
  }

   Map<String,ClassData> class_map=new TreeMap<String,ClassData>();  // Class name, ClassData 
   
   public void add(String class_name)
   {
	   ClassData  class_value=class_map.get(class_name);
	   if(class_value==null)
	   {
		   class_value=new ClassData();
		   class_map.put(class_name, class_value);
	   }
	   class_value.class_instance_count++;
	   total_class_instances++;
   }
   
   public void add(String class_name, String property_name)
   {
	   ClassData  class_value=class_map.get(class_name);
	   if(class_value==null)
	   {
		   class_value=new ClassData();
		   class_map.put(class_name, class_value);
	   }
	   	   
	   class_value.add(property_name);
   }
   
   public double test(String class_name, String property_name)
   {
	   ClassData  class_value=class_map.get(class_name);
	   if(class_value==null)
	   {
		   class_value=new ClassData();
		   class_map.put(class_name, class_value);
	   }
	   	   
	   Long count=class_value.property_map.get(property_name);
	   if(count==null)
		   return 1;
	   double avg= ((double)count)/((double)class_value.class_instance_count);
	   //if(class_name.equals("RDF:List"))
	   //    System.out.println("list value test:"+avg+ " prop:"+property_name);
	   if(avg>1)
	   	   return 1;
	   return avg;
   }

 
   public double test(String class_name)
   {
	   ClassData  cd=class_map.get(class_name);
	   if(cd==null)
		   return 1f;
	   
	   double cpers=((double)cd.class_instance_count)/((double)total_class_instances);
	   return cpers;
	}


   public void print()
   {
	   NumberFormat formatter = new DecimalFormat("0.000");
	   for (Map.Entry<String,ClassData> centry : class_map.entrySet()) {
		    String class_name = centry.getKey();
		    ClassData cd = centry.getValue();
		    double cpers=((double)cd.class_instance_count*100)/((double)total_class_instances);
		    System.out.println(class_name+": : "+formatter.format(cpers)+"%");
			   for (Map.Entry<String,Long> pentry : cd.property_map.entrySet()) {
				    String property = pentry.getKey();
				    Long count=pentry.getValue();
				    double ppers=((double)count*100)/((double)cd.total_class_property_count);
				    double avgcount=((double)count)/((double)cd.class_instance_count);
				    //System.out.println("--  "+property+": "+formatter.format(ppers)+"% avg: "+avgcount);
				    System.out.println("--  "+property+":  avg: "+avgcount);
			   }		    
		}
   }
   
   public List<String> getCommonPVClasses()
   {
	   List<String> ret=new ArrayList<String>();
	   for (Map.Entry<String,ClassData> centry : class_map.entrySet()) {
		    String class_name = centry.getKey();
		    ClassData cd = centry.getValue();
		    double cpers=((double)cd.class_instance_count*100)/((double)total_class_instances);
		    boolean suitable=false;
		    int scount=0;
		    for (Map.Entry<String,Long> pentry : cd.property_map.entrySet()) {
			    Long count=pentry.getValue();
			    double ppers=((double)count*100)/((double)cd.total_class_property_count);
			    double avgcount=((double)count)/((double)cd.class_instance_count);
			    if(ppers>3)
			    if(avgcount<1)
			    	scount++;
			    if(scount>1)
			    {
			    	suitable=true;
			    	break;
			    }
		   }		
		    if(suitable)
		      if(cpers>1)
		    	ret.add(class_name);
		}
	   return ret;
   }
   
   public List<String> getSuitableProperties(String class_name)
   {
	   List<String> ret=new ArrayList<String>();
	   ClassData cd=class_map.get(class_name);
	   if(cd==null)
		   return null;
	    for (Map.Entry<String,Long> pentry : cd.property_map.entrySet()) {
		    Long count=pentry.getValue();
		    double ppers=((double)count*100)/((double)cd.total_class_property_count);
		    double avgcount=((double)count)/((double)cd.class_instance_count);
		    if(ppers>3)
		    if(avgcount<1)
	    	   ret.add(pentry.getKey());
	   }		
	   return ret;
   }
   public static void main(String[] args) {
	   ClassPropertyStatsBag c=new ClassPropertyStatsBag();
	   c.add("A");
	   c.add("A","1");
	   c.add("A","2");
	   c.add("A");
	   c.add("A","1");
	   c.add("A","2");
	   c.add("B");
	   c.add("B","7");
	   c.add("C");
	   c.add("C","9");
	   c.add("C","9");
	   c.add("C");
	   c.add("C","9");
	   c.add("C","9");
	   c.print();
	   System.out.println("test: "+c.test("A"));
	   System.out.println("test: "+c.test("A","1"));
	   System.out.println("test: "+c.test("A","0"));
   }
   
}

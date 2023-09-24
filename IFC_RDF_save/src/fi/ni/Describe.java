package fi.ni;

import java.util.HashSet;
import java.util.Set;

public class Describe {
    
  static Set<String> outs=new HashSet<String>();  
  static void out(String txt)
  {
      if(outs.contains(txt))
	  return;
      outs.add(txt);
      System.out.println("+ "+txt);
  }
}

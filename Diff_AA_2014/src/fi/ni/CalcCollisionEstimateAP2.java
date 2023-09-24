package fi.ni;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.Frequency;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class CalcCollisionEstimateAP2 {

    
    Frequency counts = new Frequency();

    
    public CalcCollisionEstimateAP2()
    {
    }

    public double calc(Frequency histogram)
    {
	counts=histogram;
	Apfloat sum = new Apfloat("0", Apfloat.INFINITE);
	for(int n=1;n<350;n++)	    
	    sum=sum.add(calcsum(counts.getCount(n),n));    
	return sum.doubleValue();
    }

    public void test()
    {
	System.out.println("Start testsamples");
	testSamples();
	System.out.println("Testsamples done");
	Apfloat sum = new Apfloat("0", Apfloat.INFINITE);
	sampledata();
	for(int n=0;n<350;n++)	    
	    sum=sum.add(calcsum(counts.getCount(n),n));    
	System.out.println("summa: "+sum.floatValue());
	

    }

    final static Apfloat one=new Apfloat(1, Apfloat.INFINITE);
    private Apfloat calcsum(long count_in,int value)
    {
	if(count_in==0)
	    return new Apfloat("0", Apfloat.INFINITE);
	Apfloat count=new Apfloat(count_in, Apfloat.INFINITE);

	Apfloat	 p4all=one;
	Apfloat ret=new Apfloat("0", Apfloat.INFINITE);
        List<Apfloat> levels_smaller=new ArrayList<Apfloat>();
	for(int n=0;n<1000;n++)	 
	{
	   if(counts.getCount(n)>0)
	   {
            if(n<value)
	    {
        	levels_smaller.add((new Apfloat(Math.pow(2, n), 100000)).divide((new Apfloat(Math.pow(2, value), 100000))));
	    }
	    if(n==value)
            {
	    	       p4all=p4all.multiply(ApfloatMath.pow((one.subtract(one.divide(new Apfloat(Math.pow(2, n))))),counts.getCount(n)-1));
	    }
	    if(n>value)
	    {
	               p4all=p4all.multiply(ApfloatMath.pow((one.subtract(one.divide(new Apfloat(Math.pow(2, n))))),counts.getCount(n)));	       
	    }
	  }	    
	}

	if(levels_smaller.size()==0)
	{
	    ret=ret.add(count.multiply(one.subtract(p4all)));
	}
	else
	{
	 List<Apfloat> kertoimet=laskeKertoimet(levels_smaller);   
	    
	 int startinx=0;
	 for(Apfloat d:kertoimet)
	 {	    
	     Apfloat plevel=one;
		int inx=0;
		for(int n=0;n<value;n++)	 
		{
		   if(counts.getCount(n)>0)
		   {
		    if(inx>=startinx)   
	            if(n<value)
		    {
		         //plevel*=Math.pow((1-1/Math.pow(2, n)),counts.getCount(n));
	        	plevel=plevel.multiply(ApfloatMath.pow((one.subtract(one.divide(new Apfloat(Math.pow(2, n))))),counts.getCount(n)));
		    }
		    inx++;
		  }	    
		}
            //ret+=count*d*(1-(plevel*p4all));
            ret=ret.add(count.multiply(d.multiply(one.subtract(plevel.multiply(p4all)))));
            startinx++;
	 }
	 
	}
	return ret;
    }
   
    
   private List<Apfloat> laskeKertoimet(List<Apfloat>  levels_smaller) {
       List<Apfloat> kertoimet=new ArrayList<Apfloat>();
        Collections.reverse(levels_smaller);
        Apfloat current=one;
        for(Apfloat d:levels_smaller)
        {
            kertoimet.add(current.subtract(d));
            current=d;
        }
        kertoimet.add(current);
        Collections.reverse(kertoimet);
        return kertoimet;
    }


class Num
    {
	boolean collision=false;
	int number;
	public Num(int number) {
	    super();
	    this.number = number;
	}
	
    }
    
    List<Num> gennumbers=new ArrayList<Num>();
    public void sampledata()
    {
	adddata(10,2);
	adddata(10000,3);
	adddata(2000,4);
	adddata(500,5);
	adddata(20001,12);
	adddata(20001,16);
	adddata(20,20);
	adddata(2500,30);
	adddata(1200,50);
    }
    public void testSamples()
    {
	double total=0;
	for(int n=0;n<100000;n++)
	    total+=testSampleset();
	System.out.println("avg collision count: "+total/100000);
    }
    
    private double testSampleset()
    {
	gennumbers.clear();
	Map<Integer,Num> colmap=new HashMap<Integer,Num>();
	gendata(10,2);
	gendata(10000,3);
	gendata(2000,4);
	gendata(500,5);
	gendata(20001,12);
	gendata(20001,16);
	gendata(20,20);
	gendata(2500,30);
	gendata(1200,50);

	for(Num i:gennumbers)
	{
	    Num ed=colmap.get(i.number);
	    if(ed!=null)
	    {
		ed.collision=true;
		i.collision=true;
		
	    }
	    else
		colmap.put(i.number, i);
		    
	}
	double collisions=0;
	for(Num i:gennumbers)
	{
		if(i.collision)
		    collisions++;
		    
	}
	return collisions;

    }
    Random randomGenerator = new Random(System.currentTimeMillis());
    
    private void gendata(int count,int bits)
    {
	 int max=(int)(Math.pow(2, bits));
	 for (int n = 0; n < count; n++){
	      int randomInt = randomGenerator.nextInt(max);
	      gennumbers.add(new Num(randomInt));
	 }

    }
    
    public void adddata(int count,int val)
    {
	for(int n=0;n<count;n++)
	    counts.addValue(val);
    }
    
     
    
    public static void main(String[] args) {
       new CalcCollisionEstimateAP2();
    }

}

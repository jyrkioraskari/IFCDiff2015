package fi.ni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import fi.util.StringChecksum;

public class GenerateBlankNodeRDF {

	final int MAXNUM = 4;
	class Person
	{		
		final String id;
		String name=null;
		String street=null;
		String phone=null;
		Set<Person> friends= new HashSet<Person>();
		boolean removed=false;

		public Person(String id) {
			this.id=id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}
		
		

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public Set<Person> getFriends() {
			return friends;
		}

		public void setFriends(Set<Person> friends) {
			this.friends = friends;
		}

		public String getId() {
			return id;
		}

		public boolean isRemoved() {
			return removed;
		}

		public void setRemoved(boolean removed) {
			this.removed = removed;
		}
		
	}
	
	public GenerateBlankNodeRDF()
	{
		Random randomGenerator = new Random(System.currentTimeMillis());
		List<Person> persons=new ArrayList<Person>();
		for(int n=0;n<MAXNUM;n++)
		{
			persons.add(new Person("_:p"+n));
		}
		
		for(int n=0;n<MAXNUM;n++)
		{
			  Person p=persons.get(n);
			  int friendcount = randomGenerator.nextInt(5);
			  for(int i=0;i<friendcount;i++)
			  {
				  int f = randomGenerator.nextInt(MAXNUM);
				  
				  if(f!=i)
				    p.friends.add(persons.get(f));
			  }
			  int hasName = randomGenerator.nextInt(100);
			  if(hasName>30)
			  {
				  int name = randomGenerator.nextInt(10000);
				  StringChecksum s=new  StringChecksum();
				  s.update(""+name);
				  p.setName("N"+s.getChecksumValue());
			  }
			  int hasStreet = randomGenerator.nextInt(100);
			  if(hasStreet>30)
			  {
				  int name = randomGenerator.nextInt(10000);
				  StringChecksum s=new  StringChecksum();
				  s.update(""+name);
				  p.setStreet("S"+s.getChecksumValue());
			  }
			  int hasPhone = randomGenerator.nextInt(100);
			  if(hasPhone>30)
			  {
				  int number = randomGenerator.nextInt(10000);
				  p.setPhone(""+number);
			  }
		      
		}
		outputRDF(persons);
		System.out.println("-------------------------------------");

		int MAXREMOVE=1;
		for(int i=0;i<MAXREMOVE;i++)
		{
		// Remove a person
		int f = randomGenerator.nextInt(MAXNUM);
		Person p=persons.get(f);
		p.setRemoved(true);
		}
		int removed_persons_count=0;
		for(int n=0;n<MAXNUM;n++)
		{
			if(persons.get(n).isRemoved())
				removed_persons_count++;
		}

		
		int removed=outputRDF(persons);

		System.out.println("-------------------------------------");
		System.out.println("removed persons: "+removed_persons_count);
		System.out.println("removed triples: "+removed);
		
	}
	
	private int outputRDF(List<Person> persons)
	{
		int removed=0;
		
		System.out.println("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
		System.out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
		System.out.println("@prefix s:  <http://www.example.org/sample#> .");
		System.out.println("s:Person a rdfs:Class.");
		System.out.println("s:name a rdf:Propertyi .");
		System.out.println("s:address a rdf:Propertyi .");
		System.out.println("s:phone a rdf:Propertyi .");
		System.out.println("s:knows a rdf:Property .");
		for(int n=0;n<MAXNUM;n++)
		{
			  Person p=persons.get(n);
			  if(p.isRemoved())
			  {
				  removed++;	  
				  if(p.getName()!=null)
				     removed++;
				  if(p.getStreet()!=null)
					     removed++;
				  if(p.getPhone()!=null)
					     removed++;
				  for(Person f:p.getFriends())
				  {
					  if(!f.isRemoved())
					    removed++;
				  }
				  continue;
			  }
			  System.out.println(p.getId()+" a s:Person.");			  
			  if(p.getName()!=null)
			     System.out.println("_:p"+n+" s:name \""+p.getName()+"\".");
			  if(p.getStreet()!=null)
				     System.out.println("_:p"+n+" s:address \""+p.getStreet()+"\".");
			  if(p.getPhone()!=null)
				     System.out.println("_:p"+n+" s:phone \""+p.getPhone()+"\".");
			  for(Person f:p.getFriends())
			  {
				  if(!f.isRemoved())
				    System.out.println(p.getId()+" s:knows "+f.getId()+".");
				  else
					removed++;
			  }
				  
		}
		return removed;
	}

	

	public static void main(String[] args) {
		new GenerateBlankNodeRDF();

	}

}

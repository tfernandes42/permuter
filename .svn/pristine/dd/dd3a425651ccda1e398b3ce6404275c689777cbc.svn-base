package com.newsbank.permuter.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class Relations {
	
	Map <String, List<Person>> primaries;
	Map <String, List<Person>> relatives;
	Map <String, List<Terms>> Terms;
	StringBuilder xmlForm;
	//might not need coupleRefs to be included in Relations
	//List<String> coupleRefs;

	public Relations()
	{
	this(null);
	}
	
	public Relations(Person primary)
	{
	setPrimaries(new HashMap<String, List<Person>>());
	setRelatives(new HashMap<String, List<Person>>());
	setTerms(new HashMap<String, List<Terms>>());
	setXmlForm(new StringBuilder());
	}
	
	public StringBuilder getXmlForm() 
	{
		return xmlForm;
	}

	public void setXmlForm(StringBuilder xmlForm) 
	{
		this.xmlForm = xmlForm;
	}
	
	public Map<String, List<Person>> getPrimaries() 
	{
		return primaries;
	}

	//Primary getter~~~
	public List<Person> getPrimaries(String key) 
	{
		
			if(this.primaries != null)
		return this.primaries.get(key);
			else
				return  (List<Person>) primaries;
	 }

	//getter to only get back one primary at a time
	public Person getPrimary(String key) 
	{	
		if(this.getPrimaries().containsKey(key))
	return this.getPrimary(key);
		else
			return  (Person) relatives;
	}

	public void setPrimaries(Map <String, List<Person>> primaries) 
	{
		this.primaries = primaries;

		}
		
	//~~~~~Primary Setter
	public void setPrimaries(String key, Person thePerson) 
	{
		List <Person> theList = null;
		
		if(primaries != null && primaries.containsKey(key)  ) 
		{
			theList = primaries.get(key);
			//theList.add(thePerson);
		}
		else {
			theList = new ArrayList<Person>();
			//theList.add(thePerson);
			
			if(primaries != null )
				primaries.put(key, theList);
		}
		theList.add(thePerson);
	}

	public Map<String, List<Person>> getRelatives() 
	{
		return relatives;
	}
	
	//Relatives getter~~~
	public List<Person> getRelatives(String key) 
	{
			if(this.relatives != null)
		return this.relatives.get(key);
			else
				return  (List<Person>) relatives;
	 }

	//getter to only get back one relative at a time
	public Person getRelative(String key) 
	{	
		if(this.getRelatives().containsKey(key))
	return this.getRelative(key);
		else
			return  (Person) relatives;
	}

	public void setRelatives(Map <String, List<Person>> relatives) 
	{
		this.relatives = relatives;
	}
		
	//~~~~~Relatives setter
	public void setRelatives(String key, Person thePerson) 
	{
		List <Person> theList = null;
		
		if(relatives != null && relatives.containsKey(key)  ) 
		{
			theList = relatives.get(key);
			//theList.add(thePerson);
		}
		else {
			theList = new ArrayList<Person>();
			//theList.add(thePerson);
			
			if(relatives != null )
				relatives.put(key, theList);
		}
		theList.add(thePerson);
	}
	
	
	//Terms getter~~~
	public List<Terms> getTerms(String key) 
	{
		
			if(Terms != null)
		return Terms.get(key);
			else
				return  (List<Terms>) Terms;
	 }

	public Map<String, List<Terms>> getTerms() 
	{
		return Terms;
	}
	
	public void setTerms(Map<String, List<Terms>> terms) 
	{
		Terms = terms;
	}
	
	//~~~~~Terms setter
	public void setTerms(String key, Terms theTerm) 
	{
		List <Terms> theList = null;
		
		if(Terms != null && Terms.containsKey(key)  ) {
			theList = Terms.get(key);
			//theList.add(thePerson);
		}
		else {
			theList = new ArrayList<Terms>();
			//theList.add(thePerson);
			
			if(Terms != null )
				Terms.put(key, theList);
		}
		theList.add(theTerm);
	}
	
    /**method to append to xmlForm
    * @param string
    */
	public void appendToXmlForm(String string)
	{
		xmlForm.append(string);
	}
	
    /**method to set terms to relations
    * @param xmlTerm
    * @param termKey
    * @param relations
    * @param termsList
    */
	public void setTermsToRelations(String xmlTerm, String termKey, Relations r, List<?> termsList)
	{
		String termString = null;
		for (int a = 0; a < termsList.size(); a++) 
		{	
			Element termsNode = (Element) termsList.get(a);

			//go inside Terms and get all term elements
			List <?> inTerms = termsNode.getChildren(xmlTerm);
			Terms terms = new Terms();
			
			for (int x = 0; x < inTerms.size(); x++) 
			{
				Element inNode = (Element) inTerms.get(x);
				termString = inNode.getText();
				//take out any quotation
				if(termString.contains("\""))
				{
					termString = termString.replace("\"", "");
				}
				terms.addTerm(termString);
				r.setTerms(termKey,terms);				
			}
		}
	}
	
    /**method to set terms to relations in FSObitPermuter
    * @param xmlTerm
    * @param termKey
    * @param relations
    * @param termsList
    */
	public void setTermsToRelationsFS(Relations r, String term1, String term2, String term3, String term4)
	{
	    //getting TERMS && setting terms if any are not null
		if(term1 != null || term2 != null || term3 != null || term4 !=null)
		{
			Terms terms = new Terms();
			
			r.appendToXmlForm("<terms>\n");
			
			if (term1 != null && !term1.toUpperCase().contains("SAME") && !term1.toUpperCase().contains("THIS")) 
			{
				terms.addTerm(term1);
				r.appendToXmlForm("\t<term>" + term1 + "</term>\n");
			}
	
			if (term2 != null  && !term2.equalsIgnoreCase(term1) && !term2.toUpperCase().contains("SAME") && !term2.toUpperCase().contains("THIS")) 
			{
				terms.addTerm(term2);
				r.appendToXmlForm("\t<term>" + term2 + "</term>\n");
			}
		   
			if (term3 != null && !term3.equalsIgnoreCase(term1) && !term3.equalsIgnoreCase(term2)
					&& !term3.toUpperCase().contains("SAME") && !term3.toUpperCase().contains("THIS")) 
			{
				terms.addTerm(term3);
				r.appendToXmlForm("\t<term>" + term3 + "</term>\n");
			}
	
			if (term4 != null && !term4.equalsIgnoreCase(term1) && !term4.equalsIgnoreCase(term2) && !term4.equalsIgnoreCase(term3)
					&& !term4.toUpperCase().contains("SAME") && !term4.toUpperCase().contains("THIS")) 
			{
				terms.addTerm(term4);
				r.appendToXmlForm("\t<term>" + term4 + "</term>\n");
			}   
			r.appendToXmlForm("</terms>\n");
			setTerms("all", terms);
		}
	}

    /**method to add relatives to the arrayList
    * @param key
    * @param person
    */
	public void addRel(String key, List<Person> person) 
	{
		relatives.put(key, person);
	}	
	
	public List<com.newsbank.permuter.types.Terms> getTermsValue(int index) 
	{
		return Terms.get(index);			 
	}
		
    /**method to count how many relatives in array list
    * @return
    */
	public int countRel() 
	{
		int termCount = 0;
		
		if(relatives != null) 
			 termCount = relatives.size();
			
		return termCount;
	}
	
	/**method to count how many primary people in array list
	* @return
	*/
	public int countPrim() 
	{
		int termCount = 0;
		
		if(primaries != null) 
			 termCount = primaries.size();
			
		return termCount;
	}

	/**method to count how many terms in array list
	* @return
	*/
	public int countTerms() 
	{
		int termCount = 0;
		
		if(Terms != null) 
			 termCount = Terms.size();
			
		return termCount;
	}

	/**Method to get a list of persons with a specific key
	* @return
	*/
	public List<Person> getKey(String key) 
	{
		List<Person> keyInfo = null;
		
		if(relatives != null) 
			keyInfo = relatives.get(key);
		
		return keyInfo;			
	}
}



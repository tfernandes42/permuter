package com.newsbank.permuter.types;

import java.util.ArrayList;
import java.util.List;

public class Terms {
	
	//array list to hold organization objects
	List <String> terms = new ArrayList <String>();
			
		public Terms(List<String> terms) 
		{
			super();
			this.terms = terms;
		}

		public Terms() {
		}
		
		public List<String> getTerms() 
		{
			return terms;
		}

		public void setTerms(List<String> terms) 
		{
			this.terms = terms;
		}

	    /**method to add terms to the organization arrayList
		* @param newTerm
		*/
		public void addTerm(String newTerm) 
		{
			terms.add(newTerm);
		}		
		
		/**method to count how many terms in organization arrayList
		* @return
		*/
		public int countTerms() 
		{
			int termCount = terms.size();
			return termCount;			
		}
		
		/**method to get terms value
		* @param index
		* @return
		*/
		public String getValue(int index) 
		{
			return terms.get(index);			 
		}
		
		/**Method to getTerm(i) from organization arrayList
		* @param index
		* @return
		*/
		public String getTerm(int index) 
		{
			String term = terms.get(index);
			return term;			
		}
}

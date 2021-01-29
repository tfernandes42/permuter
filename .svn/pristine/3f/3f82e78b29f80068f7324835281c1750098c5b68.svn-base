package com.newsbank.permuter.types;

import java.util.List;

public class Person {

		public static enum Gender
		{
			MALE, FEMALE, UNKNOWN
		}

		List <String> org;
		String nameFirst;
		String nameMid;
		String nameNick;
		String birthLast;
		String nameLast;
		String relationship;
		Gender gender;
		String nameSuf;
		List <String> nameLastList;
		
		//Constructors
		   public Person()
			{
			}
			
			public Person( List <String> org, String nameFirst, String nameMid, String nameNick, String birthLast, String nameLast, String relationship,
					Gender gender, String nameSuf, List <String> nameLastList)
			{
				super();
				this.org = org;
				this.nameFirst = nameFirst;
				this.nameMid = nameMid;
				this.nameNick = nameNick;
				this.birthLast = birthLast;
				this.nameLast = nameLast;
				this.relationship = relationship;
				this.gender = gender;
				this.nameSuf = nameSuf;
				this.nameLastList = nameLastList;
			} 
	
			public Person(List <String> org, String nameFirst, String nameMid, String nameNick, String birthLast, String nameLast, 
					Gender gender, String nameSuf, List <String> nameLastList)
			{
				super();
				this.org = org;
				this.nameFirst = nameFirst;
				this.nameMid = nameMid;
				this.nameNick = nameNick;
				this.birthLast = birthLast;
				this.nameLast = nameLast;
				this.gender = gender;
				this.nameSuf = nameSuf;
				this.nameLastList = nameLastList;
			} 


		//getters and setters
		public List <String> getOrg() 
		{
			return org;
		}

		public void setOrg(List <String> org) 
		{
			this.org = org;
		}

		public String getNameFirst() 
		{
			return nameFirst;
		}

		public void setNameFirst(String nameFirst) 
		{
			this.nameFirst = nameFirst;
		}

		public String getNameMid() 
		{
			return nameMid;
		}

		public void setNameMid(String nameMid) 
		{
			this.nameMid = nameMid;
		}

		public String getNameNick() 
		{
			return nameNick;
		}

		public void setNameNick(String nameNick) 
		{
			this.nameNick = nameNick;
		}

		public String getBirthLast() 
		{
			return birthLast;
		}

		public void setBirthLast(String birthLast) 
		{
			this.birthLast = birthLast;
		}

		public String getNameLast() 
		{
			return nameLast;
		}

		public void setNameLast(String nameLast) 
		{
			this.nameLast = nameLast;
		}

		public String getRelationship() 
		{
			return relationship;
		}

		public void setRelationship(String relationship) 
		{
			this.relationship = relationship;
		}

		public Gender getGender() 
		{
	        return gender;
	    }

	    public void setGender(Gender gender) 
	    {
	        this.gender = gender;
	    }
	    
		public String getNameSuf() 
		{
			return nameSuf;
		}

		public void setNameSuf(String nameSuf) 
		{
			this.nameSuf = nameSuf;
		}
		
		public List <String> getNameLastList() 
		{
			return org;
		}

		public void setNameLastList(List <String> nameLastList) 
		{
			this.nameLastList = nameLastList;
		}

		
	    /**method to set names to person - first,middle,birth,last
		* @param nameFirst
		* @param nameMid
		* @param nameNick
		* @param birthLast
		* @param nameLast
		* @param suffixValue
		* @param rel
		*/
		public void setNames(Relations r, String nameFirst, String nameMid, String nameNick, String birthLast, String nameLast,
				String suffixValue, String rel) 
		{
			if(nameFirst != null && !nameFirst.isEmpty())
 			{
    				setNameFirst(nameFirst.trim());
    				r.appendToXmlForm("\t<nameFirst>"+ nameFirst.trim() +"</nameFirst>\n");
 			}
    			if(nameMid != null && !nameMid.isEmpty())
 			{
    				setNameMid(nameMid.trim());
    				r.appendToXmlForm("\t<nameMid>"+ nameMid.trim() +"</nameMid>\n");
 			}
    			if(nameNick != null && !nameNick.isEmpty())
 			{
    				setNameNick(nameNick.trim());
    				r.appendToXmlForm("\t<nameNick>"+ nameNick.trim() +"</nameNick>\n");
 			}
    			if(birthLast != null && !birthLast.isEmpty())
 			{
    				setBirthLast(birthLast.trim());
    				r.appendToXmlForm("\t<birthLast>"+ birthLast.trim() +"</birthLast>\n");
 			}
    			if(nameLast != null && !nameLast.isEmpty())
 			{
    				setNameLast(nameLast.trim());
    				r.appendToXmlForm("\t<nameLast>"+ nameLast.trim() +"</nameLast>\n");
 			}
    			if(suffixValue != null && !suffixValue.isEmpty())
 			{
    				setNameSuf(suffixValue.trim());
    				r.appendToXmlForm("\t<nameSuf>"+ suffixValue.trim() +"</nameSuf>\n");
 			}
    			if(rel != null && !rel.isEmpty())
 			{
    				setRelationship(rel.trim());	
    				r.appendToXmlForm("\t<relationship>"+ rel.trim() +"</relationship>\n");
 			}						
			 /*if (genderElement != null) {secondaryPerson.setGender(Person.Gender.valueOf(genderElement.getText().toUpperCase()));}*/
		}
		
		/**method to process primaryPerson name permutations
		* @param init
		* @param nameFirstInitial
		* @param nameMidInitial
		* @param birthLastInitial
		* @param nameMids
		* @param sMid
		* @param sBlast
		* @param fNameLastList
		* @return
		*/
		public String primPermute(StringBuilder init, char nameFirstInitial, char nameMidInitial, 
				char birthLastInitial, String nameMids, String sMid, String sBlast, List <String> fNameLastList, 
				boolean permBool) 
		{
			StringBuilder trip = new StringBuilder();
			
			//if person only has nameFirst -----------------------------------------
			if(nameCount() == 1 && nameFirst != null) 
			{
				if (!nameLastList.isEmpty())
				{
					for (int i = 0; i < nameLastList.size(); i ++)
					{
						if (nameFirst.length()>1) {
							if(permBool != true && !init.toString().contains(nameFirst + "\" ONEAR/2 \"" + nameLastList.get(i)))
							{
								init.append("\""+nameFirst + "\" ONEAR/2 \"" + nameLastList.get(i)+ "\" OR ");
							}
							trip.append("\"" + nameFirst + " " + nameLastList.get(i)+ "\"");
							
							//add nameLast to fNameLastList
							if(!fNameLastList.contains(nameLastList.get(i)))
							{
								fNameLastList.add(nameLastList.get(i));
							}
						
							//init
							if(permBool != true)
				   			{
								init.append("\"" +nameLastList.get(i) + " " + nameFirst+ "\"");	
				   			}
						}
					}
				}
				else 
				{
					if(nameFirst.length()>1)
						trip.append("\""+nameFirst+ "\"");
				}
			}
			
			//if person only has nameMid
			if( nameCount() == -2 && nameMid != null ) 
			{

				if (!nameLastList.isEmpty())
				{
					for (int i = 0; i < nameLastList.size(); i ++)
					{
						if (nameMid.length()>1) {
							/*if(!init.toString().contains(nameMid + "\" ONEAR/2 \"" + nameLastList.get(i)))
							{
								init.append("\""+nameMid + "\" ONEAR/2 \"" + nameLastList.get(i)+ "\" OR ");
							}*/
							trip.append("\"" + nameMid + " " + nameLastList.get(i)+ "\"");
							
							//add nameLast to fNameLastList
							if(!fNameLastList.contains(nameLastList.get(i)))
							{
								fNameLastList.add(nameLastList.get(i));
							}
						
							//init
						   	//init.append("\"" +nameLastList.get(i) + " " + nameMid+ "\"");	
						}
					}
				}
				else 
				{
					if(nameMid.length()>1)
						trip.append("\""+nameMid+ "\"");
				}
			
			}
			
			//if person only has nameFirst and nameMid
			if(nameCount() == 2 && nameFirst != null && nameMid != null) 
			{
				if (!nameLastList.isEmpty())
				{
					for (int i = 0; i < nameLastList.size(); i ++)
					{
				   		if (nameFirst.length()>1) {
				   			trip.append("\"" + nameFirst + " " + nameLastList.get(i)+ "\"");
				   			//init
				   			if(permBool != true)
				   			{
				   				init.append("\"" +nameLastList.get(i) + " " + nameFirst+ "\"");	
				   			}
				   		}
					   	trip.append("\"" +nameFirst + " " + nameMid+ " " + nameLastList.get(i)+ "\"");	
					   	
						//add nameLast to fNameLastList
						if(!fNameLastList.contains(nameLastList.get(i)))
						{
							fNameLastList.add(nameLastList.get(i));
						}
						
					   	//J W Spencer ******************
						if(permBool != true)
			   			{
							init.append("\"" +nameFirstInitial + " " + nameMidInitial+ " " + nameLastList.get(i)+ "\"");
			   			}
					   	
					   	if (!nameMid.equalsIgnoreCase(sMid) && nameFirst.length() > 1) 
					   	{
					   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + nameLastList.get(i)+ "\"");
					   		trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + nameLastList.get(i)+ "\"");
					   		if (nameMids != null) 
					   		{
					   			trip.append("\"" +nameFirst + " " + nameMids + nameLastList.get(i)+ "\"");
					   		}
					   	}
					}
				}
				else 
					trip.append("\""+nameFirst+ " " +nameMid+ "\"");
			}	//-------------------------------------------------------------------------------------------
			
			//if primary has everything but nameFirst
			if (nameFirst == null) 
			{
				if (nameMid != null && birthLast != null && nameLast != null) 
				{
					trip.append("\""+nameMid+ " " +birthLast+ "\"");
					trip.append("\""+nameMid+ " " +nameLast+ "\"");
					
					if (!birthLast.equalsIgnoreCase(sBlast) && nameMid.length() > 1) 
					{
						trip.append("\""+nameMid+ " " +birthLastInitial+ " " +nameLast+ "\"");
					}
					if (nameNick != null) 
					{
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
					}
				}
				
				else if (nameMid != null && (birthLast != null || nameLast != null)) 
				{
					if (nameMid != null && birthLast != null) 
					{
						trip.append("\""+nameMid+ " " +birthLast+ "\"");

						if (nameNick != null) 
						{
						   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
						}
					}
					else if (nameMid != null && nameLast != null) 
					{
						trip.append("\""+nameMid+ " " +nameLast+ "\"");

						if (nameNick != null) 
						{
						   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
						}
					}
				}
			}

			//person ONLY has a either LAST names; takes last name from the first primary person
			if (nameCount() == -3 && nameFirst == null && nameMid == null) 
			{
				if (nameLast != null && birthLast != null) 
				{
					trip.append("\""+birthLast+"\""+ " OR " +"\""+nameLast+ "\"");
				}
				else if (birthLast != null) 
				{
					trip.append("\"" +birthLast+ "\"");
				}
				else if (nameLast != null) 
				{
					trip.append("\"" +nameLast+ "\"");
				}
			}
			
			//person has a nameFirst && birthLast	
			if (nameCount() == 2 && nameFirst != null && birthLast != null && !birthLast.isEmpty()) 
			{
				if (nameFirst.length()>1) {
					if(permBool != true && !init.toString().contains(nameFirst + "\" ONEAR/2 \"" + birthLast))
					{
						init.append("\""+nameFirst + "\" ONEAR/2 \"" + birthLast+ "\" OR ");
					}
					trip.append("\"" + nameFirst + " " + birthLast+ "\"");
					//trip.append("\"" + nameFirst + "\" ONEAR/1 ? ONEAR/1 \"" + birthLast+ "\"");
				
					//init
					if(permBool != true)
		   			{
						init.append("\"" +birthLast + " " + nameFirst+ "\"");
		   			}
				}
				   	if (nameNick != null) 
				   	{
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
				   	}
			}
			//person has a nameFirst && nameLast
			else if (nameCount() == 2 && nameFirst!= null && nameLast != null && !nameLast.isEmpty()) 
			{
				if (nameFirst.length()>1)
				{
					if(permBool != true && !init.toString().contains(nameFirst + "\" ONEAR/2 \"" + nameLast))
					{
						init.append("\""+nameFirst + "\" ONEAR/2 \"" + nameLast+ "\" OR ");
					}
					trip.append("\"" + nameFirst + " " + nameLast+ "\"");
					//trip.append("\"" + nameFirst + "\" ONEAR/1 ? ONEAR/1 \"" + nameLast+ "\"");
					
					//init
					if(permBool != true)
		   			{
						init.append("\"" +nameLast + " " + nameFirst+ "\"");
		   			}
				}
			   	if (nameNick != null) 
			   	{
				   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
			   	}
			}

			//person has 3 names and one of the last names will be null
			if (nameCount() == 3 && nameFirst != null && nameMid != null) 
			{				   	
			   	if (birthLast != null) 
			   	{
			   		if (nameFirst.length()>1) {
			   			trip.append("\"" + nameFirst + " " + birthLast+ "\"");
			   			//init
			   			if(permBool != true)
			   			{
			   				init.append("\"" +birthLast + " " + nameFirst+ "\"");
			   			}
			   		}
				   	trip.append("\"" +nameFirst + " " + nameMid+ " " + birthLast+ "\"");	
				   	
				   	//initial initial last
				   	if(permBool != true)
		   			{
				   		init.append("\"" +nameFirstInitial + " " + nameMidInitial+ " " + birthLast+ "\"");
		   			}
				   	
				   	if (!nameMid.equalsIgnoreCase(sMid) && nameFirst.length() > 1) 
				   	{
				   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + birthLast+ "\"");
				   		trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + birthLast+ "\"");
				   		if (nameMids != null) 
				   		{
				   			trip.append("\"" +nameFirst + " " + nameMids + birthLast+ "\"");
				   		}
				   	}
				   	
				   	if (nameNick != null) 
				   	{
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
				   	} 	
			   	}
			   	
			   	if (nameLast != null ) 
			   	{
			   		if (nameFirst.length()>1) {
						trip.append("\"" + nameFirst + " " + nameLast+ "\"");
						//init
						if(permBool != true)
			   			{
							init.append("\"" +nameLast + " " + nameFirst+ "\"");
			   			}
				   	}
				   	trip.append("\"" +nameFirst + " " + nameMid+ " " + nameLast+ "\"");	
				   	
				   	//initial initial last
				   	if(permBool != true)
		   			{
				   		init.append("\"" +nameFirstInitial + " " + nameMidInitial+ " " + nameLast+ "\"");
		   			}
				   	
				   	if (!nameMid.equalsIgnoreCase(sMid) && nameFirst.length() > 1) 
				   	{
				   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
					   	trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + nameLast+ "\"");	
				   		if (nameMids != null) 
				   		{
				   			trip.append("\"" +nameFirst + " " + nameMids+ nameLast+ "\"");
				   		}
				   	}
				   	if (nameNick != null) 
				   	{
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	} 	
			   	}
			}
			
			//person has 3 part name but no middle name 
			if (nameCount() == 3 && nameFirst != null && nameMid == null) 
			{
				if (nameFirst.length()>1) {
					trip.append("\"" + nameFirst + " " + birthLast+ "\"");
					if(permBool != true && !init.toString().contains(nameFirst + "\" ONEAR/2 \"" + birthLast))
					{
						init.append("\"" + nameFirst + "\" ONEAR/2 \"" + birthLast+ "\""); //maybe
					}
					trip.append("\"" + nameFirst + " " + nameLast+ "\"");
					if(permBool != true && !init.toString().contains(nameFirst + "\" ONEAR/2 \"" + nameLast))
					{
						init.append("\"" + nameFirst + "\" ONEAR/2 \"" + nameLast+ "\""); //maybe
					}
					//init
				   	init.append("\"" +birthLast + " " + nameFirst+ "\"");
				   	init.append("\"" +nameLast + " " + nameFirst+ "\"");
				}
				if (!birthLast.equalsIgnoreCase(sBlast) && nameFirst.length() > 1) 
				{
					trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
				}
				
				//J W Spencer ******************
				init.append("\"" +nameFirstInitial + " " + birthLastInitial+ " " + nameLast+ "\"");
				
			   	if (nameNick != null) 
			   	{
				   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
			   	} 	
			}

			//person has 4 part name
			if (nameCount() == 4) 
			{
				if (nameFirst.length()>1)
				{
					trip.append("\"" +nameFirst + " " + birthLast+ "\"");	
					trip.append("\"" +nameFirst + " " + nameLast+ "\"");
					//init
					if(permBool != true)
		   			{
						init.append("\"" +nameLast + " " + nameFirst+ "\"");
						init.append("\"" +birthLast + " " + nameFirst+ "\"");
		   			}
				}
				if (!birthLast.equalsIgnoreCase(sBlast) && nameFirst.length() > 1) 
				{				
					trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
				}
				
				//initial initial last
				if(permBool != true)
	   			{
					init.append("\"" +nameFirstInitial + " " + birthLastInitial+ " " + nameLast+ "\"");
	   			}
				
				trip.append("\"" + nameFirst+ " " + nameMid + " " + birthLast + "\"");
				if (nameMid.length() > 1) 
				{
					trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + birthLast + "\"");
				}
				trip.append("\"" + nameFirst+ " " + nameMid + " " + nameLast + "\"");
				if (nameMid.length() > 1) 
				{
					trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + nameLast + "\"");
				}
				
				//initial initial last
				if(permBool != true)
	   			{
					init.append("\"" + nameFirstInitial+ " " + nameMidInitial + " " + birthLast + "\"");
					init.append("\"" + nameFirstInitial+ " " + nameMidInitial + " " + nameLast + "\"");
	   			}
				
				if (!nameMid.equalsIgnoreCase(sMid) && nameFirst.length() > 1) 
				{
					trip.append("\"" + nameFirst+ " " + nameMidInitial + " " + birthLast + "\"");
					trip.append("\"" + nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
			   		if (nameMids != null) 
			   		{
			   			trip.append("\"" +nameFirst + " " + nameMids+ birthLast+ "\"");
			   			trip.append("\"" +nameFirst + " " + nameMids+ nameLast+ "\"");
			   		}

				}
				
			   	if (nameNick != null) 
			   	{
				   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
			   	} 	
			}
			return trip.toString();
		}
	    
		/**method to process secondPerson name permutations
		* @param nameFirstInitial
		* @param nameMidInitial
		* @param birthLastInitial
		* @param fNameFirst
		* @param fBirthLast
		* @param fNameLast
		* @param relNameMids
		* @param sRelMid
		* @param sRelBlast
		* @param fNameLastList
		* @return
		*/
		public String secPermute(char nameFirstInitial, char nameMidInitial, char birthLastInitial, 
				String fNameFirst, String fBirthLast, String fNameLast, String relNameMids, String sRelMid, String sRelBlast,
				List <String> fNameLastList) 
		{
			StringBuilder trip = new StringBuilder();

			//person has EVERYTHING BUT NAMEFIRST
			if (nameFirst == null) 
			{
				if(nameMid != null && birthLast != null && nameLast != null) 
				{
					trip.append("\""+nameMid+ " " +birthLast+ "\"");
					trip.append("\""+nameMid+ " " +nameLast+ "\"");
					
					if (!birthLast.equalsIgnoreCase(sRelBlast) && nameMid.length() > 1) 
					{
						trip.append("\""+nameMid+ " " +birthLastInitial+ " " +nameLast+ "\"");
					}
					if(nameNick != null) 
					{
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
					}
				}
				
				else if (nameMid != null && birthLast != null || nameLast != null) 
				{
					if(nameMid != null && birthLast != null) 
					{
						trip.append("\""+nameMid+ " " +birthLast+ "\"");

						if(nameNick != null) 
						{
						   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
						}
					}
					else if (nameMid != null && nameLast != null) 
					{
						trip.append("\""+nameMid+ " " +nameLast+ "\"");

						if(nameNick != null) 
						{
						   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
						}
					}
				}
			}
			
			//person ONLY has a NICK name; takes last name from the first primary person
			/*if(nameCount() == -1) 
			{
				if (fNameLast != null) 
				{
					trip.append("\"" +nameNick+ " " + fNameLast+ "\"");
				}
				if (fBirthLast != null) 
				{
					trip.append("\"" +nameNick+ " " + fBirthLast+ "\"");
				}
			}*/
			//-------------------------------------------------------------------------------------------
			//in initialize relationships
			String [] relList = {"SPOUSE", "WIFE", "HUSBAND", "EX-WIFE", "EX-HUSBAND","PARENT", "MOTHER", "FATHER", "PARENT-IN-LAW", "MOTHER-IN-LAW", "FATHER-IN-LAW", "STEPPARENT", "STEPMOTHER", "STEPFATHER",
					"GRANDPARENT", "GRANDMOTHER", "GRANDFATHER", "GRANDPARENT-IN-LAW", "GRANDMOTHER-IN-LAW", "GRANDFATHER-IN-LAW","STEP-GRANDPARENT",
					"STEP-GRANDMOTHER", "STEP-GRANDFATHER", "GREAT-GRANDPARENT", "GREAT-GRANDMOTHER", "GREAT-GRANDFATHER","AUNT", "UNCLE","SIBLING", "SISTER", "BROTHER", "SIBLING-IN-LAW", "SISTER-IN-LAW", 
					"BROTHER-IN-LAW", "STEPSIBLING", "STEPSISTER", "STEPBROTHER","CHILD","DAUGHTER","SON","CHILD-IN-LAW","DAUGHTER-IN-LAW","SON-IN-LAW","STEPCHILD","STEPDAUGHTER","STEPSON","GRANDCHILD","GRANDDAUGHTER",
					"GRANDSON","GRANDCHILD-IN-LAW","GRANDDAUGHTER-IN-LAW","GRANDSON-IN-LAW","STEP-GRANDCHILD","STEP-GRANDDAUGHTER","STEP-GRANDSON"};

			boolean keyRel = false;
			//see if it's a key relationship
			if(relationship != null)
			{
				for (int i = 0; i < relList.length; i++)
				{
					if(relationship.equalsIgnoreCase(relList[i]))
					{
						keyRel = true;
					}
				}
			}
			
			//person ONLY has a FIRST name; takes last name from the first primary person ---------------------
			if(nameCount() == 1 && nameFirst != null && !nameFirst.equalsIgnoreCase(fNameFirst)) 
			{				//if it is a key relationship
				if(keyRel == true)
				{
					if (!fNameLastList.isEmpty()) 
					{
						for(int i = 0; i < fNameLastList.size(); i++)
						{
							String last = fNameLastList.get(i);
							//if there is a multiple part last name only use last part
							if(last.contains(" "))
							{
								String [] fnl = fNameLastList.get(i).split(" ");
								last = fnl[fnl.length-1];
							}
							trip.append("\"" + nameFirst + " " + last+ "\"");
		
							if(nameNick != null) 
							{
							   	trip.append("\"" +nameNick+ " " + last+ "\"");
							}
						}
					}
				}
				else //if not a key relationship OR no relationship, just output nameFirst
					trip.append("\"" + nameFirst + "\"");
			}

			//person ONLY has a MIDDLE name; takes last name from the first primary person
			if(nameCount() == -2) 
			{
				//if it is a key relationship
				if(keyRel == true)
				{
					if (!fNameLastList.isEmpty()) 
					{
						for(int i = 0; i < fNameLastList.size(); i++)
						{
							String last = fNameLastList.get(i);
							//if there is a multiple part last name only use last part
							if(last.contains(" "))
							{
								String [] fnl = fNameLastList.get(i).split(" ");
								last = fnl[fnl.length-1];
							}
							trip.append("\"" + nameMid + " " + last+ "\"");
		
							if(nameNick != null) 
							{
							   	trip.append("\"" +nameNick+ " " + last+ "\"");
							}
						}
					}
				}
				else //if not a key relationship OR no relationship, just output nameFirst
					trip.append("\"" + nameMid + "\"");
			}	
			
			//person has a first and a middle name but no last name(s); grabs last name from the FIRST primary person
			if (nameCount() == 2  && nameFirst != null && nameMid != null && birthLast == null && nameLast == null) 
			{	
				//if it is a key relationship
				if(keyRel == true)
				{
					if (!fNameLastList.isEmpty()) 
					{
						for(int i = 0; i < fNameLastList.size(); i++)
						{
							//if there is a multiple part last name only use last part
							String last = fNameLastList.get(i);
							//if there is a multiple part last name only use last part
							if(last.contains(" "))
							{
								String [] fnl = fNameLastList.get(i).split(" ");
								last = fnl[fnl.length-1];
							}
							trip.append("\"" + nameFirst + " " + last+ "\"");
							trip.append("\"" + nameFirst + " " + nameMid + " " + last+ "\"");
							if(nameMid.length() > 1 ) 
							{
								trip.append("\"" + nameFirstInitial + " " + nameMid + " " + last+ "\"");
							}
							if (!nameMid.equalsIgnoreCase(sRelMid) && nameFirst.length() > 1) 
							{
								trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + last+ "\"");	
							}
		
							if(nameNick != null) 
							{
								trip.append("\"" +nameNick+ " " + last+ "\"");
							}
						}
					}
				}
				else //if not a key relationship OR no relationship, just output nameFirst
					trip.append("\"" + nameFirst + " " + nameMid+ "\"");
			}
			//-------------------------------------------------------------------------------------------
			
			//person ONLY has either LAST names; takes last name from the first primary person
			if(nameCount() == -3 && nameFirst == null && nameMid == null) 
			{
				if (nameLast != null && birthLast != null) 
				{
					trip.append("\""+birthLast+"\""+ " OR " + "\""+nameLast+ "\"");
				}
				else if (birthLast != null) 
				{
					trip.append("\"" +birthLast+ "\"");
				}
				else if (nameLast != null) 
				{
					trip.append("\"" +nameLast+ "\"");
				}
			} 

			//person has a nameFirst && birthLast	
			if (nameCount() == 2 && nameFirst != null && birthLast != null) 
			{
				trip.append("\"" + nameFirst + " " + birthLast+ "\"");

				if (nameNick != null) 
				{
					trip.append("\"" +nameNick+ " " + birthLast+ "\"");
					}
				}
			
			//person has a nameFirst && nameLast				
			else if (nameCount() == 2 && nameFirst != null && nameLast != null) 
			{
				trip.append("\"" + nameFirst + " " + nameLast+ "\"");

				   if (nameNick != null) 
				   {
					   trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   }
			}
		
			//person has 3 names and one of the last names will be null
			if (nameCount() == 3 && nameFirst != null && nameMid != null) 
			{				   	
			   	if (birthLast != null) 
			   	{
					trip.append("\"" + nameFirst + " " + birthLast+ "\"");
				   	trip.append("\"" +nameFirst + " " + nameMid+ " " + birthLast+ "\"");	
				   	
				   	if(!nameMid.equalsIgnoreCase(sRelMid) && nameFirst.length() > 1) 
				   	{
				   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + birthLast+ "\"");	
				   		trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + birthLast+ "\"");	
				   		if(relNameMids != null) 
				   		{
				   			trip.append("\"" +nameFirst + " " + relNameMids + birthLast+ "\"");
				   		}
				   	}
				   	
				   	if (nameNick != null) 
				   	{
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
				   	} 	
			   	}
			   	
			   	if (nameLast != null ) 
			   	{
					trip.append("\"" + nameFirst + " " + nameLast+ "\"");
				   	trip.append("\"" +nameFirst + " " + nameMid+ " " + nameLast+ "\"");
				   	
				   	if (!nameMid.equalsIgnoreCase(sRelMid) && nameFirst.length() > 1) 
				   	{
				   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
					   	trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + nameLast+ "\"");
				   		if(relNameMids != null) 
				   		{
				   			trip.append("\"" +nameFirst + " " + relNameMids + nameLast+ "\"");
				   		}
				   	}
				   	if (nameNick != null) 
				   	{
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	} 	
			   	}
			}

			//person has 3 part name but no middle name 
			if (nameCount() == 3 && nameFirst != null && nameLast != null && birthLast != null && nameMid == null) 
			{
				trip.append("\"" + nameFirst + " " + birthLast+ "\"");	
				trip.append("\"" + nameFirst + " " + nameLast+ "\"");
				
				if (!birthLast.equalsIgnoreCase(sRelBlast) && nameFirst.length() > 1) 
				{
					trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
				}
				
			   	if (nameNick != null) 
			   	{
				   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
			   	} 	
			}
			
			//person has 4 part name
			if (nameCount() == 4) 
			{
				trip.append("\"" +nameFirst + " " + birthLast+ "\"");	
				trip.append("\"" +nameFirst + " " + nameLast+ "\"");
				
				if (!birthLast.equalsIgnoreCase(sRelBlast) && nameFirst.length() > 1) 
				{				
					trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
				}
				
				trip.append("\"" + nameFirst+ " " + nameMid + " " + birthLast + "\"");
				if(nameMid.length() > 1) 
				{
					trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + birthLast + "\"");
				}
				trip.append("\"" + nameFirst+ " " + nameMid + " " + nameLast + "\"");
				if(nameMid.length() > 1) 
				{
					trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + nameLast + "\"");
				}
				
				if (!nameMid.equalsIgnoreCase(sRelMid) && nameFirst.length() > 1) 
				{
					trip.append("\"" + nameFirst+ " " + nameMidInitial + " " + birthLast + "\"");
					trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
					
			   		if(relNameMids != null) 
			   		{
			   			trip.append("\"" +nameFirst + " " + relNameMids + birthLast+ "\"");
			   			trip.append("\"" +nameFirst + " " + relNameMids + nameLast+ "\"");
			   			}
			   		}
				if (nameNick != null) 
				{
					trip.append("\"" +nameNick+ " " + nameLast+ "\"");
					trip.append("\"" +nameNick+ " " + birthLast+ "\"");
				} 	
			}
			
			return trip.toString();
		}
		
		/**method to count the number of name parts a person has
		* @return
		*/
		public int nameCount() 
		{	
			int count = 0;
			
			if ( nameFirst != null && nameMid != null && birthLast != null && nameLast != null ) 
				count = 4;
			
			else if (nameFirst != null && nameMid != null && birthLast != null && nameLast == null) 
				count = 3;
	
			else if (nameFirst != null && nameMid != null && birthLast == null  && nameLast != null) 
				count = 3;
			
			else if (nameFirst != null && nameMid == null && birthLast != null && nameLast != null) 
				count = 3;
					
			else if (nameFirst != null && nameMid != null && birthLast == null && nameLast == null) 
				count = 2;
			
			else if (nameFirst != null && nameMid == null && birthLast == null && nameLast != null) 
				count = 2;
			
			else if (nameFirst != null && nameMid == null && birthLast != null && nameLast == null) 
				count = 2;
			
			else if (nameFirst != null && nameMid != null && birthLast == null && nameLast == null) 
				count = 2;
			
			else if (nameFirst != null && nameMid == null && birthLast == null && nameLast == null) 
				count = 1;
			
			else if (nameNick != null && nameMid == null && birthLast == null && nameLast == null) 
				count = -1;
			
			else if (nameFirst == null && nameMid != null && birthLast == null && nameLast == null) 
				count = -2;
			
			else if (nameFirst == null && nameMid == null && birthLast != null || nameLast != null) 
				count = -3;
			else if (org != null) 
				count = -4;
			
			else 
				count = 0;
			
			return count;
		}
	    
	}



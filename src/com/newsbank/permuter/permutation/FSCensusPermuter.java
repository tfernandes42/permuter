package com.newsbank.permuter.permutation;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.newsbank.permuter.types.Person;
import com.newsbank.permuter.types.Relations;
import com.newsbank.permuter.types.Terms;

public class FSCensusPermuter extends AbstractPermutation {

//	private final static Logger kLogger = LogManager.getLogger();
	private final static Logger kLogger = Logger.getLogger(FSCensusPermuter.class);
	
	//Terms terms;	
	
	public Relations process(String fileData)
	{
   	 	//terms = new Terms();
		Relations r = new Relations();
		
		SAXBuilder builder = new SAXBuilder();
		
		try
		{
			Document document = (Document) builder.build(new StringReader(fileData));

			//root element
			Element rootNode = document.getRootElement();
			
			Element htmlNode = null;
			Element nbxNode = null;
			Namespace NS = null;
			Element enhancements = null;
		
			if(rootNode.getChild("NBX") != null || rootNode.getChild("HTML") != null) {
				if (rootNode.getChild("HTML") != null) {
					htmlNode = rootNode.getChild("HTML");
					NS = Namespace.getNamespace("nbx","http://www.newsbank.com/xml/nbx/1.0");
					enhancements = htmlNode.getChild("enhancements", NS);
				}
				else {
					nbxNode = rootNode.getChild("NBX");
					NS = Namespace.getNamespace("nbx","http://www.newsbank.com/xml/nbx/1.0");
					enhancements = nbxNode.getChild("enhancements", NS);
					}
				}
			else
			{
				NS = Namespace.getNamespace("nbx","http://www.newsbank.com/xml/nbx/1.0");
				enhancements = rootNode.getChild("enhancements", NS);
			}
			
			Element enhancement = enhancements.getChild("enhancement", NS);
			
			Namespace NS2 = Namespace.getNamespace("","http://www.newsbank.com/xml/FamilySearch/1.0");
			
			Element FamilySearch = enhancement.getChild("FamilySearch", NS2);			

			Element record = FamilySearch.getChild("record", NS2); 
			
			@SuppressWarnings("rawtypes")
			List list = record.getChildren("person", NS2);
		

			String [] all = { "Wife", "Husband", "Spouse", "Ex-Wife","Ex-Husband","Mother", "Father", "Parent", "Sister", "Brother", "Sister-In-Law", 
					"Brother-In-Law", "Aunt", "Uncle", "Cousin", "Mother-In-Law", "Stepfather", "Sibling", "Stepmother","Father-In-Law", "Stepbrother",
					"Stepsister", "Sibling-In-Law", "Parent-In-Law", "Stepsibling", "Fiancé", "Stepparent", "Domestic Partner"};

			for (int i = 0; i < list.size(); i++) {
				String nameFirst = null;
				String nameMid = null;
				String nameLast = null;
				String birthLast = null;
				String nameNick = null;
				

			   Element node = (Element) list.get(i);		
			   
			   //start to break up names into their category... first, mid, nick, last...				   
			   String first = node.getChildText("person_PR_NAME_GN", NS2); 
			   String last = node.getChildText("person_PR_NAME_SURN", NS2);
			   
			   Element gendeElement = node.getChild("person_PR_SEX_CODE", NS2);
			   
			    if (first != null && first.contains(" Or ")) {
				  String[] orBirthFirst = first.split(" Or ");

				  first = orBirthFirst[0].substring(0, orBirthFirst[0].length());
				  nameNick = orBirthFirst[1].substring(0, orBirthFirst[1].length());
			    } 
			    
			     if (last != null && last.contains(" Or")) {
			    		int or = last.indexOf(" Or");
					last = last.substring(0, or);
			     }
			     //if the Or is the first thing in the SURName field
			     else if (last != null && last.contains("Or")) {
			    	 	last = null;
			     }
			     
			     String[] firstPart = null;
			     if(first != null) {
			    	 	firstPart = first.split(" ");
			     }
			     String[] secondPart = null;
			    		 if(last != null) {
			    		 secondPart = last.split(" ");
			    		 }
			    		 
			    	//firstPart name separation	 
			    if(first != null && firstPart.length == 3) {
					nameFirst = firstPart[0].substring(0, firstPart[0].length());
					nameMid = firstPart[1].substring(0, firstPart[1].length());
		    		 	birthLast = firstPart[2].substring(0, firstPart[2].length());
			     }
			     else if(first != null && firstPart.length == 2) {
					nameFirst = firstPart[0].substring(0, firstPart[0].length());
					nameMid = firstPart[1].substring(0, firstPart[1].length());   
			     }
			     else if(first != null && firstPart.length == 1) {
					nameFirst = firstPart[0].substring(0, firstPart[0].length());					   
			     }
			    
		    		//secondPart name separation 
			    	 if (last != null && secondPart.length == 2){
			    		 birthLast = secondPart[0].substring(0, secondPart[0].length());
				   	 nameLast = secondPart[1].substring(0, secondPart[1].length());
			    	 }
			    	 else if (last != null && secondPart.length == 1){
			    		 nameLast = secondPart[0].substring(0, secondPart[0].length());
			    	}
			    	 
			    	 //setting primaryPerson and adding them to Relations
			     if (node.getChildText("person_REL_TYPE", NS2)!= null && node.getChildText("person_REL_TYPE", NS2).equals("Deceased")) {
			    	 	Person primaryPerson = new Person();
			    	 
				   	primaryPerson.setNameFirst(nameFirst);
					primaryPerson.setNameMid(nameMid);
					primaryPerson.setNameNick(nameNick);
					primaryPerson.setBirthLast(birthLast);
					primaryPerson.setNameLast(nameLast);

					if (gendeElement != null) {
						primaryPerson.setGender(Person.Gender.valueOf(gendeElement.getText().toUpperCase()));
					}
					r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);

					Terms terms = new Terms();
				     //getting TERMS && setting terms
					if (node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2) != null ) {
						terms.addTerm(node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2));
					}
				   
					if (node.getChildText("person_PR_DEATH_CITY_TOWN", NS2) != null  && 
						   !node.getChildText("person_PR_DEATH_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2))) {
					   terms.addTerm(node.getChildText("person_PR_DEATH_CITY_TOWN", NS2));
					}
				   
					if (node.getChildText("person_PR_RES_CITY_TOWN", NS2) != null && 
						   !node.getChildText("person_PR_RES_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2))
						   && !node.getChildText("person_PR_RES_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_DEATH_CITY_TOWN", NS2))) {
					   terms.addTerm(node.getChildText("person_PR_RES_CITY_TOWN", NS2));
					}
				   
				   if (node.getChildText("person_PR_BUR_CITY_TOWN", NS2) != null && 
						   !node.getChildText("person_PR_BUR_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2))
						   && !node.getChildText("person_PR_BUR_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_DEATH_CITY_TOWN", NS2)) 
						   && !node.getChildText("person_PR_BUR_CITY_TOWN", NS2).equalsIgnoreCase(node.getChildText("person_PR_RES_CITY_TOWN", NS2))) {
					  terms.addTerm(node.getChildText("person_PR_BUR_CITY_TOWN", NS2));
				   }   
				   r.setTerms("all", terms);
			     }
			     else			   
				     if (node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2)!= null && node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).equals("Head")) {
				    	 	Person primaryPerson = new Person();
				    	 
					   	primaryPerson.setNameFirst(nameFirst);
						primaryPerson.setNameMid(nameMid);
						primaryPerson.setNameNick(nameNick);
						primaryPerson.setBirthLast(birthLast);
						primaryPerson.setNameLast(nameLast);

						if (gendeElement != null) {
							primaryPerson.setGender(Person.Gender.valueOf(gendeElement.getText().toUpperCase()));
						}
						r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);

						Terms terms = new Terms();
					     //getting TERMS && setting terms
						   if (node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2) != null ) {
							terms.addTerm(node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2));
						   }

						   if (node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2) != null  && 
								   !node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2).equalsIgnoreCase(node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2))) {
							   terms.addTerm(node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2));
						   }
						   
						   if (node.getChildText("event_EVENT_CITY", NS2) != null && 
								   !node.getChildText("event_EVENT_CITY", NS2).equalsIgnoreCase(node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2))
								   && !node.getChildText("event_EVENT_CITY", NS2).equalsIgnoreCase(node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2))) {
							   terms.addTerm(node.getChildText("event_EVENT_CITY", NS2));
						   }

						   if (node.getChildText("event_EVENT_COUNTY", NS2) != null && 
								   !node.getChildText("event_EVENT_COUNTY", NS2).equalsIgnoreCase(node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2))
								   && !node.getChildText("event_EVENT_COUNTY", NS2).equalsIgnoreCase(node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2)) 
								   && !node.getChildText("event_EVENT_COUNTY", NS2).equalsIgnoreCase(node.getChildText("event_EVENT_CITY", NS2))) {
							  terms.addTerm(node.getChildText("event_EVENT_COUNTY", NS2));
						   }   
						   r.setTerms("all", terms);
				     }
			     
			     //setting secondaryPerson and adding them to Relations
			     for (int a = 0; a < all.length; a++)	{
			    	 	if ( node.getChildText("person_REL_TYPE", NS2) != null && node.getChildText("person_REL_TYPE", NS2).equals(all[a])) {
			    	 		Person primaryPerson = new Person();
			    	 		
			    	 		primaryPerson.setNameFirst(nameFirst);
			    	 		primaryPerson.setNameMid(nameMid);
			    	 		primaryPerson.setNameNick(nameNick);
			    	 		primaryPerson.setBirthLast(birthLast);
			    	 		primaryPerson.setNameLast(nameLast);
			    	 		primaryPerson.setRelationship(node.getChildText("person_REL_TYPE", NS2));
						if (gendeElement != null) {
							primaryPerson.setGender(Person.Gender.valueOf(gendeElement.getText().toUpperCase()));
						}
						r.setPrimaries(primaryPerson.getRelationship(), primaryPerson);
			    	 	}
			    	 
			    	 	else if (node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2) != null && 
			    			 node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2).equals(all[a])) {
			    	 		Person primaryPerson = new Person();
			    	 		
			    	 		primaryPerson.setNameFirst(nameFirst);
			    	 		primaryPerson.setNameMid(nameMid);
			    	 		primaryPerson.setNameNick(nameNick);
			    	 		primaryPerson.setBirthLast(birthLast);
			    	 		primaryPerson.setNameLast(nameLast);
			    	 		primaryPerson.setRelationship(node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2));							
						if (gendeElement != null) {
							primaryPerson.setGender(Person.Gender.valueOf(gendeElement.getText().toUpperCase()));
						}
						r.setPrimaries(primaryPerson.getRelationship(), primaryPerson);
			    	 	}
			    	 	else if (node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2) != null && 
				    			 node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).equals(all[a])) {
				    	 		Person primaryPerson = new Person();
				    	 		
				    	 		primaryPerson.setNameFirst(nameFirst);
				    	 		primaryPerson.setNameMid(nameMid);
				    	 		primaryPerson.setNameNick(nameNick);
				    	 		primaryPerson.setBirthLast(birthLast);
				    	 		primaryPerson.setNameLast(nameLast);
				    	 		primaryPerson.setRelationship(node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2));							
							if (gendeElement != null) {
								primaryPerson.setGender(Person.Gender.valueOf(gendeElement.getText().toUpperCase()));
							}
							r.setPrimaries(primaryPerson.getRelationship(), primaryPerson);
				    	 }
			    }
			}
	  } catch (Throwable io) {
		  kLogger.warn(io);
//		io.printStackTrace(System.err);
		
	  }
		
	return r;
	}
	

	public String permute(Relations r) 
	{
		StringBuilder trip= new StringBuilder();
		StringBuilder onear = new StringBuilder();
		
	 	String nameFirst = null;
		String nameMid = null;
		String nameNick = null;
		String birthLast = null;
		String nameLast = null;
	 	String fNameFirst = null;
		//String fNameNick = null;
	 	String fBirthLast = null;
		String fNameLast = null;
		//Gender gender = null;
		char nameFirstInitial = 0;
		char nameMidInitial = 0;
		char birthLastInitial = 0;
		String sMid = null;
		String sBlast = null;
		String theKey = null;
		
		boolean first = false;
		
		trip.append("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0=(");

		//for loop to make this iterate through all the relatives in r
		Map <String, List<Person>> primaries = r.getPrimaries();
		Iterator<String> itPrimary=primaries.keySet().iterator();
		while (itPrimary.hasNext())
		{
			theKey = itPrimary.next();
			//kLogger.debug("Relatives:" + theKey);
			
			List<Person> personList = primaries.get(theKey);
			for(Person primary : personList)
			{

		 	nameFirst = primary.getNameFirst();
			nameMid = primary.getNameMid();
			nameNick = primary.getNameNick();
			birthLast = primary.getBirthLast();
			nameLast = primary.getNameLast();
			//gender = primary.getGender();

			//get initials of certain names
			if(nameFirst != null) {
			nameFirstInitial = nameFirst.charAt(0);
			}
			if(nameMid != null) {
				nameMidInitial = nameMid.charAt(0);
				sMid = Character.toString(nameMidInitial);
			}
			if(birthLast != null) {
				birthLastInitial = birthLast.charAt(0);
				sBlast = Character.toString(birthLastInitial);
			}
			
			String nameMids = null;
			//if nameMid has more than one parts 
			if(nameMid != null && nameMid.contains(" ")) {
				String [] nm = nameMid.split(" ");
				StringBuilder nm2= new StringBuilder();
				
				for(int i = 0; i < nm.length; i++) {
					nm2.append(nm[i].charAt(0) + " ");
				}
				nameMids = nm2.toString();
			}
			
			//set up for coupleRefs
			if ( first == false) {
				fNameFirst = nameFirst;
				fBirthLast = birthLast;
				fNameLast = nameLast;

				/*if (nameNick != null) {
					fNameNick = nameNick;
				}*/
			}

			first = true;
			//===================================================
			//start building primary person trip
			//===================================================
			//if a primary person only has first name nothing will be output
			
			//if primary has everything but nameFirst
			if (nameFirst == null) {
				
				if(nameMid != null && birthLast != null && nameLast != null) {
					trip.append("\""+nameMid+ " " +birthLast+ "\"");
					trip.append("\""+nameMid+ " " +nameLast+ "\"");
					
					if (!birthLast.equals(sBlast) && nameMid.length() > 1) {
						trip.append("\""+nameMid+ " " +birthLastInitial+ " " +nameLast+ "\"");
					}
					if(nameNick != null) {
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
					}
				}
				
				else if (nameMid != null && birthLast != null || nameLast != null) {
					if(nameMid != null && birthLast != null) {
						trip.append("\""+nameMid+ " " +birthLast+ "\"");

						if(nameNick != null) {
						   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
						}
					}
					else if (nameMid != null && nameLast != null) {
						trip.append("\""+nameMid+ " " +nameLast+ "\"");

						if(nameNick != null) {
						   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");	
						}
					}
				}
			}

			//person ONLY has a either LAST names; takes last name from the first primary person
			if(primary.nameCount() == -3 && nameFirst == null && nameMid == null) {
				if (nameLast != null && birthLast != null) {
					trip.append("\""+birthLast+"\""+ " OR " +"\""+nameLast+ "\"");
				}
				else if (birthLast != null) {
					trip.append("\"" +birthLast+ "\"");
				}
				else if (nameLast != null) {
					trip.append("\"" +nameLast+ "\"");
				}
			}
			
			//person has a nameFirst && birthLast	
			if (primary.nameCount() == 2 && nameFirst != null && birthLast != null) {
					onear.append(" "+nameFirst + " ONEAR/3 " + birthLast+ " ");
					trip.append("\"" + nameFirst + " " + birthLast+ "\"");
				   	trip.append("\"" +birthLast + " " + nameFirst+ "\"");	
				   	
				   	if (nameNick != null) {
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");	
				   	}
				}
			
			//person has a nameFirst && nameLast				
				else if (primary.nameCount() == 2 && nameFirst!= null && nameLast != null) {
					onear.append(" "+nameFirst + " ONEAR/3 " + nameLast+ " ");
					trip.append("\"" + nameFirst + " " + nameLast+ "\"");
				   	trip.append("\"" +nameLast + " " + nameFirst+ "\"");	
				   	
				   	if (nameNick != null) {
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
				   	}
				}

				//person has 3 names and one of the last names will be null
				if (primary.nameCount() == 3 && nameFirst != null && nameMid != null) {				   	
				   	if (birthLast != null) {
						trip.append("\"" + nameFirst + " " + birthLast+ "\"");
					   	trip.append("\"" +birthLast + " " + nameFirst+ "\"");	
					   	trip.append("\"" +nameFirst + " " + nameMid+ " " + birthLast+ "\"");	
					   	
					   	//J W Spencer ******************
					   	trip.append("\"" +nameFirstInitial + " " + nameMidInitial+ " " + birthLast+ "\"");
					   	
					   	if(!nameMid.equals(sMid) && nameFirst.length() > 1) {
					   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + birthLast+ "\"");
					   		if(nameMids != null) {
					   			trip.append("\"" +nameFirst + " " + nameMids + birthLast+ "\"");
					   		}
					   	}
					   	
					   	if (nameNick != null) {
						   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
					   	} 	
				   	}
				   	
				   	if (nameLast != null ) {
						trip.append("\"" + nameFirst + " " + nameLast+ "\"");
					   	trip.append("\"" +nameLast + " " + nameFirst+ "\"");	
					   	trip.append("\"" +nameFirst + " " + nameMid+ " " + nameLast+ "\"");	
					   	
					   	//J W Spencer ******************
					   	trip.append("\"" +nameFirstInitial + " " + nameMidInitial+ " " + nameLast+ "\"");
					   	
					   	if (!nameMid.equals(sMid) && nameFirst.length() > 1) {
					   		trip.append("\"" +nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
						   	trip.append("\"" +nameFirstInitial + " " + nameMid+ " " + nameLast+ "\"");	
					   		if(nameMids != null) {
					   			trip.append("\"" +nameFirst + " " + nameMids+ nameLast+ "\"");
					   		}
					   	}
					   	if (nameNick != null) {
						   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
					   	} 	
				   	}
				}
				
				//person has 3 part name but no middle name 
				if (primary.nameCount() == 3 && nameFirst != null && nameMid == null) {
					trip.append("\"" + nameFirst + " " + birthLast+ "\"");	
					trip.append("\"" + nameFirst + " " + nameLast+ "\"");
				   	trip.append("\"" +nameLast + " " + nameFirst+ "\"");
					
					if (!birthLast.equals(sBlast) && nameFirst.length() > 1) {
						trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" +nameFirstInitial + " " + birthLastInitial+ " " + nameLast+ "\"");
					
				   	if (nameNick != null) {
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
				   	} 	
				}
				
				//person has 4 part name
				if (primary.nameCount() == 4 ) {
					trip.append("\"" +nameFirst + " " + birthLast+ "\"");	
					trip.append("\"" +nameFirst + " " + nameLast+ "\"");
					trip.append("\"" +nameLast + " " + nameFirst+ "\"");	
					
					if (!birthLast.equals(sBlast) && nameFirst.length() > 1) {				
						trip.append("\"" +nameFirst + " " + birthLastInitial+ " " + nameLast+ "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" +nameFirstInitial + " " + birthLastInitial+ " " + nameLast+ "\"");
					
					trip.append("\"" + nameFirst+ " " + nameMid + " " + birthLast + "\"");
					if(nameMid.length() > 1) {
						trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + birthLast + "\"");
					}
					trip.append("\"" + nameFirst+ " " + nameMid + " " + nameLast + "\"");
					if(nameMid.length() > 1) {
						trip.append("\"" + nameFirstInitial+ " " + nameMid + " " + nameLast + "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" + nameFirstInitial+ " " + nameMidInitial + " " + birthLast + "\"");
					trip.append("\"" + nameFirstInitial+ " " + nameMidInitial + " " + nameLast + "\"");
					
					if (!nameMid.equals(sMid) && nameFirst.length() > 1) {
						trip.append("\"" + nameFirst+ " " + nameMidInitial + " " + birthLast + "\"");
						trip.append("\"" + nameFirst + " " + nameMidInitial+ " " + nameLast+ "\"");
				   		if(nameMids != null) {
				   			trip.append("\"" +nameFirst + " " + nameMids+ birthLast+ "\"");
				   			trip.append("\"" +nameFirst + " " + nameMids+ nameLast+ "\"");
				   		}

					}
					
				   	if (nameNick != null) {
					   	trip.append("\"" +nameNick+ " " + nameLast+ "\"");
					   	trip.append("\"" +nameNick+ " " + birthLast+ "\"");
				   	} 	
				}
			}
		}
		trip.append(")");
		String str1 = trip.toString().substring(trip.indexOf("(") +1, trip.indexOf(")"));
		String str2 = deleteDup(str1);
		StringBuilder strBuild = new StringBuilder();
		
		if(!onear.toString().isEmpty()) {
			trip.append("&p_bool_primary-1=OR&p_field_primary-1=alltext&p_params_primary-1=weight:0&p_text_primary-1=(");
			
			if(onear.toString().contains("  ")) {
				strBuild.append(onear.toString().replaceAll("  ", " OR "));
				trip.append(strBuild);
			}
			else
				trip.append(onear);
			
			trip.append(")");
		}	
		
			//===================================================
			//start building relatives trip
			//===================================================
			trip.append("&p_field_second-0=alltext&p_params_second-0=weight:3&p_text_second-0=(");	
			
			//for loop to make this iterate through all the relatives in r
			Map <String, List<Person>> relatives = r.getRelatives();
			Iterator<String> it=relatives.keySet().iterator();
			while (it.hasNext())
			{
				String theKey2 = it.next();
				//kLogger.debug("Relatives:" + theKey);
				
				List<Person> personList = relatives.get(theKey2);
				for(Person relative : personList)
				{
	        	
				String relNameFirst = relative.getNameFirst();
				String relNameMid = relative.getNameMid();
				String relNameNick = relative.getNameNick();
				String relNameLast = relative.getNameLast();
				String relBirthLast = relative.getBirthLast();
				//String relationship = relative.getRelationship();
				//Gender relGender = relative.getGender();
				char relNameMidInitial = 0;
				char relBirthLastInitial = 0;
				char relNameFirstInitial = 0;
				String sRelMid = null;
				String sRelBlast = null;
	
				//set initials of necessary names
				if(relNameFirst != null) {
				relNameFirstInitial = relNameFirst.charAt(0);
				}
				if(relNameMid != null) {
					relNameMidInitial = relNameMid.charAt(0);
					sRelMid = Character.toString(relNameMidInitial);
				}
				if(relBirthLast != null) {
					relBirthLastInitial = relBirthLast.charAt(0);
					sRelBlast = Character.toString(relBirthLastInitial);
				}
				
				String relNameMids = null;
				//if nameMid has more than one parts 
				if(relNameMid != null && relNameMid.contains(" ")) {
					String [] nm = relNameMid.split(" ");
					StringBuilder nm2= new StringBuilder();
					
					for(int i = 0; i < nm.length; i++) {
						nm2.append(nm[i].charAt(0) + " ");
					}
					relNameMids = nm2.toString();
				}
				
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~start forming name permutations~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				
				//person has EVERYTHING BUT NAMEFIRST
				if (relNameFirst == null) {
					if(relNameMid != null && relBirthLast != null && relNameLast != null) {
						trip.append("\""+relNameMid+ " " +relBirthLast+ "\"");
						trip.append("\""+relNameMid+ " " +relNameLast+ "\"");
						
						if (!birthLast.equals(sRelBlast) && relNameMid.length() > 1) {
							trip.append("\""+relNameMid+ " " +relBirthLastInitial+ " " +relNameLast+ "\"");
						}
						if(relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");	
						   	trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");	
						}
					}
					
					else if (relNameMid != null && relBirthLast != null || relNameLast != null) {
						if(relNameMid != null && relBirthLast != null) {
							trip.append("\""+relNameMid+ " " +relBirthLast+ "\"");
	
							if(relNameNick != null) {
							   	trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");	
							}
						}
						else if (relNameMid != null && relNameLast != null) {
							trip.append("\""+relNameMid+ " " +relNameLast+ "\"");
	
							if(relNameNick != null) {
							   	trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");	
							}
						}
					}
				}
				
				//person ONLY has a NICK name; takes last name from the first primary person
				if(relative.nameCount() == -1) {
					if (fNameLast != null) {
						trip.append("\"" +relNameNick+ " " + fNameLast+ "\"");
					}
					if (fBirthLast != null) {
						trip.append("\"" +relNameNick+ " " + fBirthLast+ "\"");
					}
				}
	
				//person ONLY has a FIRST name; takes last name from the first primary person
				if(relative.nameCount() == 1 && relNameFirst != null && !relNameFirst.equals(fNameFirst) ) {
					if (fNameLast != null) {
						trip.append("\"" + relNameFirst + " " + fNameLast+ "\"");
	
						if(relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + fNameLast+ "\"");
						}
					}
					if (fBirthLast != null ) {
						trip.append("\"" + relNameFirst + " " + fBirthLast+ "\"");
						
						if(relNameNick != null) {
							trip.append("\"" +relNameNick+ " " + fBirthLast+ "\"");
						}
					}
				}
	
				//person ONLY has a MIDDLE name; takes last name from the first primary person
				if(relative.nameCount() == -2) {
					if (fNameLast != null) {
						trip.append("\"" + relNameMid + " " + fNameLast+ "\"");
	
						if(relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + fNameLast+ "\"");
						}
					}
					if (fBirthLast != null ) {
						trip.append("\"" + relNameMid + " " + fBirthLast+ "\"");
						
						if(relNameNick != null) {
							trip.append("\"" +relNameNick+ " " + fBirthLast+ "\"");
						}
					}
				}
				
				//person ONLY has a either LAST names; takes last name from the first primary person
				if(relative.nameCount() == -3 && relNameFirst == null && relNameMid == null) {
					if (relNameLast != null && relBirthLast != null) {
						trip.append("\""+relBirthLast+"\""+ " OR " + "\""+relNameLast+ "\"");
					}
					else if (relBirthLast != null) {
						trip.append("\"" +relBirthLast+ "\"");
					}
					else if (relNameLast != null) {
						trip.append("\"" +relNameLast+ "\"");
					}
				}
	
				//person has a first and a middle name but no last name(s); grabs last name from the FIRST primary person
				if (relative.nameCount() == 2  && relNameFirst != null && relNameMid != null && relBirthLast == null && relNameLast == null) {
					
					if (fNameLast != null) {
						trip.append("\"" + relNameFirst + " " + fNameLast+ "\"");
						trip.append("\"" + relNameFirst + " " + relNameMid + " " + fNameLast+ "\"");
						if(relNameMid.length() > 1 ) {
							trip.append("\"" + relNameFirstInitial + " " + relNameMid + " " + fNameLast+ "\"");
						}
						if (!relNameMid.equals(sRelMid) && relNameFirst.length() > 1) {
							trip.append("\"" +relNameFirst + " " + relNameMidInitial+ " " + fNameLast+ "\"");	
						}
	
						if(relNameNick != null) {
							trip.append("\"" +relNameNick+ " " + fNameLast+ "\"");
						}
					}
					
					if(fBirthLast != null) {
						trip.append("\"" + relNameFirst + " " + fBirthLast+ "\"");
						trip.append("\"" + relNameFirst + " " + relNameMid + " " + fBirthLast+ "\"");
						if(relNameMid.length() > 1) {
							trip.append("\"" + relNameFirstInitial + " " + relNameMid + " " + fBirthLast+ "\"");
						}
						if (!relNameMid.equals(sRelMid) && relNameFirst.length() > 1) {
							trip.append("\"" +relNameFirst + " " + relNameMidInitial+ " " + fBirthLast+ "\"");	
						}
						
						if(relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + fBirthLast+ "\"");
						}
					}
				}
	
				//person has a nameFirst && birthLast	
				if (relative.nameCount() == 2 && relNameFirst != null && relBirthLast != null) {
					trip.append("\"" + relNameFirst + " " + relBirthLast+ "\"");
					
					if (relNameNick != null) {
						trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");
						}
					}
				
				//person has a nameFirst && nameLast				
					else if (relative.nameCount() == 2 && relNameFirst != null && relNameLast != null) {
						trip.append("\"" + relNameFirst + " " + relNameLast+ "\"");
					   	
					   	if (relNameNick != null) {
					   		trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");
					   	}
					}
			
				//person has 3 names and one of the last names will be null
				if (relative.nameCount() == 3 && relNameFirst != null && relNameMid != null) {				   	
				   	if (relBirthLast != null) {
						trip.append("\"" + relNameFirst + " " + relBirthLast+ "\"");
					   	trip.append("\"" +relNameFirst + " " + relNameMid+ " " + relBirthLast+ "\"");	
					   	
					  //J W Spencer ******************
					   	trip.append("\"" +relNameFirstInitial + " " + relNameMidInitial+ " " + relBirthLast+ "\"");	
					   	
					   	if(!relNameMid.equals(sRelMid) && relNameFirst.length() > 1) {
					   		trip.append("\"" +relNameFirst + " " + relNameMidInitial+ " " + relBirthLast+ "\"");	
					   		if(relNameMids != null) {
					   			trip.append("\"" +relNameFirst + " " + relNameMids + relBirthLast+ "\"");
					   		}
					   	}
					   	
					   	if (relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");
					   	} 	
				   	}
				   	
				   	if (relNameLast != null ) {
						trip.append("\"" + relNameFirst + " " + relNameLast+ "\"");
					   	trip.append("\"" +relNameFirst + " " + relNameMid+ " " + relNameLast+ "\"");
					   	
					  //J W Spencer ******************
					   	trip.append("\"" +relNameFirstInitial + " " + relNameMidInitial + " " + relNameLast+ "\"");
					  
					   	if (!relNameMid.equals(sRelMid) && relNameFirst.length() > 1) {
					   		trip.append("\"" +relNameFirst + " " + relNameMidInitial+ " " + relNameLast+ "\"");
						   	trip.append("\"" +relNameFirstInitial + " " + relNameMid+ " " + relNameLast+ "\"");
						   	
					   		if(relNameMids != null) {
					   			trip.append("\"" +relNameFirst + " " + relNameMids + relNameLast+ "\"");
					   		}
					   	}
					   	if (relNameNick != null) {
						   	trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");
					   	} 	
				   	}
				}
	
				//person has 3 part name but no middle name 
				if (relative.nameCount() == 3 && relNameFirst != null && relNameLast != null && relBirthLast != null && relNameMid == null) {
					trip.append("\"" + relNameFirst + " " + relBirthLast+ "\"");	
					trip.append("\"" + relNameFirst + " " + relNameLast+ "\"");
					
					if (!relBirthLast.equals(sRelBlast) && relNameFirst.length() > 1) {
						trip.append("\"" +relNameFirst + " " + relBirthLastInitial+ " " + relNameLast+ "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" +relNameFirstInitial + " " + relBirthLastInitial+ " " + relNameLast+ "\"");
	
				   	if (relNameNick != null) {
					   	trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");
					   	trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");
				   	} 	
				}
				
				//person has 4 part name
				if (relative.nameCount() == 4 ) {
					trip.append("\"" +relNameFirst + " " + relBirthLast+ "\"");	
					trip.append("\"" +relNameFirst + " " + relNameLast+ "\"");
					
					if (!relBirthLast.equals(sRelBlast) && relNameFirst.length() > 1) {				
						trip.append("\"" +relNameFirst + " " + relBirthLastInitial+ " " + relNameLast+ "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" +relNameFirstInitial + " " + relBirthLastInitial+ " " + relNameLast+ "\"");
					
					trip.append("\"" + relNameFirst+ " " + relNameMid + " " + relBirthLast + "\"");
					if(relNameMid.length() > 1) {
						trip.append("\"" + relNameFirstInitial+ " " + relNameMid + " " + relBirthLast + "\"");
					}
					trip.append("\"" + relNameFirst+ " " + relNameMid + " " + relNameLast + "\"");
					if(relNameMid.length() > 1) {
						trip.append("\"" + relNameFirstInitial+ " " + relNameMid + " " + relNameLast + "\"");
					}
					
					//J W Spencer ******************
					trip.append("\"" + relNameFirstInitial+ " " + relNameMidInitial + " " + relBirthLast + "\"");
					trip.append("\"" + relNameFirstInitial+ " " + relNameMidInitial + " " + relNameLast + "\"");
					
					if (!relNameMid.equals(sRelMid) && relNameFirst.length() > 1) {
						trip.append("\"" + relNameFirst+ " " + relNameMidInitial + " " + relBirthLast + "\"");
						trip.append("\"" +relNameFirst + " " + relNameMidInitial+ " " + relNameLast+ "\"");
						
				   		if(relNameMids != null) {
				   			trip.append("\"" +relNameFirst + " " + relNameMids + relBirthLast+ "\"");
				   			trip.append("\"" +relNameFirst + " " + relNameMids + relNameLast+ "\"");
				   			}
				   		}
					if (relNameNick != null) {
						trip.append("\"" +relNameNick+ " " + relNameLast+ "\"");
						trip.append("\"" +relNameNick+ " " + relBirthLast+ "\"");
						} 	
					}
				}
			}
			
			trip.append(")");
			if(trip.toString().contains("&p_field_second-0=alltext&p_params_second-0=weight:3&p_text_second-0=()")) {
				trip.delete(trip.length() - 71, trip.length());
			}
		
			//===================================================
			//start building coupleRef trip
			//===================================================
			/*
			trip.append("&p_bool_second-1=OR&p_field_second-1=alltext&p_params_second-1=weight:3&p_text_second-1=(");
			
			if (r.countPrim() == 2) {
				
				trip.append("\"" +nameFirst + " and " +fNameFirst + "\"");
				trip.append("\"" +nameFirst + " & " +fNameFirst + "\"");
				trip.append("\"" +fNameFirst + " and " +nameFirst + "\"");
				trip.append("\"" +fNameFirst + " & " +nameFirst + "\"");
				
				//if there's a nick name
				if (fNameNick != null && nameNick != null ) {
					trip.append("\"" +nameNick + " and " +fNameNick + "\"");
					trip.append("\"" +nameNick + " & " +fNameNick + "\"");
					trip.append("\"" +fNameNick + " and " +nameNick + "\"");
					trip.append("\"" +fNameNick + " & " +nameNick + "\"");
				} 
				
				if (nameNick != null ) {
					trip.append("\"" +nameNick + " and " +fNameFirst + "\"");
					trip.append("\"" +nameNick + " & " +fNameFirst + "\"");
					trip.append("\"" +fNameFirst + " and " +nameNick + "\"");
					trip.append("\"" +fNameFirst + " & " +nameNick + "\"");
				}
				
				if (fNameNick != null ) {
					trip.append("\"" +nameFirst + " and " +fNameNick + "\"");
					trip.append("\"" +nameFirst + " & " +fNameNick + "\"");
					trip.append("\"" +fNameNick + " and " +nameFirst + "\"");
					trip.append("\"" +fNameNick + " & " +nameFirst + "\"");
				}				
			}
			trip.append(")");
			
			if(trip.toString().contains("&p_bool_second-1=OR&p_field_second-1=alltext&p_params_second-1=weight:3&p_text_second-1=()")) {
				trip.delete(trip.length() - 90, trip.length());
			} */
			//===================================================
			//start building terms trip
			//===================================================
			trip.append("&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=(");
			
			List<Terms> termsList2 = r.getTerms("all");
			
			if(termsList2 != null) {
				for(int i=0; i< termsList2.size(); i++)
				{
					Terms term = termsList2.get(i);
					
					List<String> termStr = term.getTerms();
					for(int x = 0; x < termStr.size();x++) {
						String terms = termStr.get(x);
						
						String s = new String();
						String st = new String();

						//adding & or AND of terms that contain them
						if(!s.toLowerCase().equals(terms.toLowerCase()) && !st.toLowerCase().equals(terms.toLowerCase())) {
							trip.append("\"" +terms+ "\"");
						}				
						
						if(!addAnd(terms, trip).equals("")) {
							s = addAnd(terms, trip);

								trip.append("\"");
								s = s.replace("\"", "\"\"");
								trip.append(s + "\"");
						}
						
						if(!removePeriod(terms, trip).equals("")) {
							st = removePeriod(terms, trip);

								trip.append("\"");
								st = st.replace("\"", "\"\"");
								trip.append(st + "\"");
						}
					}
				}
			}

			trip.append(")");
			
			if(trip.toString().contains("&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=()")) {
				trip.delete(trip.length() - 90, trip.length());
			}

			trip.append("&p_bool_second-3=OR&p_field_second-3=alltext&p_params_second-3=weight:0&p_text_second-3=(");

			StringBuilder t = new StringBuilder();
			t.append(deleteDup(trip.toString()));

			t.append(str2 + ")");
			
			if(t.toString().contains("&p_bool_second-3=OR&p_field_second-3=alltext&p_params_second-3=weight:0&p_text_second-3=()")) {
				t.delete(t.length()-91, t.length());
			}
			
			if(!onear.toString().isEmpty()) {
			
				if(onear.toString().contains("  ")) {
					t.append("&p_bool_second-4=OR&p_field_second-4=alltext&p_params_second-4=weight:0&p_text_second-4=(" +strBuild+ ")");
				}
				else
					t.append("&p_bool_second-4=OR&p_field_second-4=alltext&p_params_second-4=weight:0&p_text_second-4=(" +onear+ ")");
			}
			
			char c = t.charAt(t.length()-1);
			String strC = String.valueOf(c);
			
			if(!strC.equals(")")) {
				t.append(")");
			}
			
			StringBuilder t2 = new StringBuilder();
			StringBuilder triplets = new StringBuilder();
			
			t2.append(addOr(t));
			return
					triplets.append(replaceAmp(t2)).toString();
	}

    //method to delete duplicates
	private static String deleteDup(String trip) {
	   String triplets = trip;
	   
	   List<String> words = new ArrayList<String>();
	   List<String> phrases = new ArrayList<String>();
	   String word = null;
	   String phrase = null;
	   CharSequence target = null ;
	   String removed = null;
	   
       StringTokenizer st1 = new StringTokenizer(triplets, ")\""); 
       
       while(st1.hasMoreTokens()) {
    	   word = st1.nextToken();
    	   words.add(word);
    	   }
       
       for(int i = 0; i < words.size(); i++) {
    	   if(!words.get(i).equals(" ") && words.get(i).length() < 65) {
    		   phrase = words.get(i);
	       phrases.add(phrase);
	       }
       }
       
       //if triplets contains any of the phrases in phrases remove from triplets
       for (int a = 0; a < phrases.size(); a ++) {
    	   String tripRev = new StringBuffer(trip).reverse().toString();

    	   if( trip.split((String) "\""+phrases.get(a)+"\"", -1).length > 2) {
    		   target = phrases.get(a);
    		   String targetRev = new StringBuffer(target).reverse().toString();
    		   
	       removed = tripRev.replaceFirst(("\"" +targetRev+ "\""), "");
	       removed = new StringBuffer(removed).reverse().toString();
	       trip = removed;
	       }
       }
       if(removed != null) {
    			return removed;
       }
       else 
    	   return trip;
    }
	
	//method to add OR between phrases
	private static String addOr(StringBuilder trip) {
		CharSequence target = "\"\"";
		CharSequence replacement = "\" OR \"";
		String newString = null;
		
		for(int i = 0; i < trip.length(); i++) {
    			newString = trip.toString().replace(target, replacement);
    		}
    		return newString;
    }
	
	//method to encode ampersands 
	private static String replaceAmp(StringBuilder trip) {
		CharSequence target = " & ";
		CharSequence replacement = " &amp; ";
		String newString = null;
		
		for(int i = 0; i < trip.length(); i++) {
    			newString = trip.toString().replace(target, replacement);
    		}
    		return newString;
    }
    
    //method to take care of and/& variations 
    private static String addAnd(String string, StringBuilder trip) {
    	String newString = null;

    		if(string.contains(" and ")) {	
    			newString = string.replace(" and ", " & ");
    			if(trip.toString().contains(newString)) {
    				newString = "";
    			}
    		}
    		else if(string.contains(" AND ")) {	
    			newString = string.replace(" AND ", " &amp; ");
    			if(trip.toString().contains(newString)) {
    				newString = "";
    			}
    		}
    		else if(string.contains(" & ")) {	
    			newString = string.replace(" & ", " and ");
    			if(trip.toString().contains(newString)) {
    				newString = "";
    			}
    		}
    		else 
    			newString = "";    		
    		
    		return newString;
    }

    //method to remove periods between letters 
    private static String removePeriod(String string, StringBuilder trip) {
    	String newString = null;

    		if(string.contains(".")) {	
    			newString = string.replace(".", "");
    			if(trip.toString().contains(newString)) {
    				newString = "";
    			}
    		}
    		else 
    			newString = "";    		
    		
    		return newString;
    }
}
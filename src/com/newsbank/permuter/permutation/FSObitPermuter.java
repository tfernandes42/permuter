package com.newsbank.permuter.permutation;

import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//import org.apache.logging.log4j.LogManager;
import com.newsbank.logwrapper.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.newsbank.permuter.PermutedResult;
import com.newsbank.permuter.types.Person;
import com.newsbank.permuter.types.Config;
import com.newsbank.permuter.types.ContainerQuery;
//import com.newsbank.permuter.types.Person.Gender;
import com.newsbank.permuter.types.Relations;
import com.newsbank.permuter.types.Terms;

public class FSObitPermuter extends AbstractPermutation {

	private final static Logger kLogger = Logger.getLogger(FSObitPermuter.class);

	// set so that ContainerQuery processes the triplet like a Census instead of a normal obit
	private ContainerQuery containerQuery = new ContainerQuery(ContainerQuery.kContainerTypeCensus);
	
	private String targetPersonID = null;
	private String form = null;
	private boolean coupleRef = false;
	
	public Relations process(String fileData)
	{	
		Instant start = Instant.now();
		Relations r = new Relations();
		
		SAXBuilder builder = new SAXBuilder();
		
		try
		{
			Document document = (Document) builder.build(new StringReader(fileData));
			
			//Initialize all lists
			Config conf = new Config();
			String[] prefix = conf.getPrefix();
			String[] suffix = conf.getSuffix();
			String[] all = conf.getAll();
			String[] head0 = conf.getHead0();
			String[] spouse0 = conf.getSpouse0();
			String[] parent0 = conf.getParent0();
			String[] sibling0 = conf.getSibling0();
			String[] child0 = conf.getChild0();
			String[] head = conf.getHead();
			String[] spouse = conf.getSpouse();
			String[] parent = conf.getParent();
			String[] sibling = conf.getSibling();
			String[] child = conf.getChild();

			//root element
			Element rootNode = document.getRootElement();
			//start going through document and grabbing the key elements
			Element htmlNode = null;
			Element nbxNode = null;
			Namespace NS = null;
			Element enhancements = null;
			
			if(rootNode.getChild("NBX") != null || rootNode.getChild("HTML") != null) 
			{
				if (rootNode.getChild("HTML") != null) 
				{
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

			String name = null;
			String targetPerson = null;
			String targetprim = null;
			
			//loop through each person in the record to grab id of person being searched
			for (int i = 0; i < list.size(); i++) 
			{	
				Element node = (Element) list.get(i);
				String pid = node.getChildText("person_id", NS2);
				
				if(pid.equalsIgnoreCase(targetPersonID))
				{
					if(node.getChildText("person_REL_TYPE", NS2)!= null)
					{
						targetPerson = node.getChildText("person_REL_TYPE", NS2).toUpperCase();
						targetprim = node.getChildText("person_REL_TYPE", NS2).toUpperCase();
					}
					else if(node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2)!= null)
					{
						targetPerson = node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).toUpperCase();
						targetprim = node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).toUpperCase();
					}
					else if(node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2) != null)
					{
						targetPerson = node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2).toUpperCase();
						targetprim = node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2).toUpperCase();
					}
					else 
					{
						targetPerson = "OTHER";
						name = node.getChildText("person_PR_NAME_GN", NS2).toUpperCase();
						
					}
				}
			} //--------------------- end looping through persons ---------------------------------------
			
	    	 //set targetPerson to main category name - Head,Parent,Child,Spouse,Sibling
		 coupleRef = true;
	    	 if(targetPerson != null && ( targetPerson.equalsIgnoreCase("HEAD") || targetPerson.equalsIgnoreCase("DECEASED") )) 
	    	 {
	    		 targetPerson = "HEAD";
	    	 }
	    	 else if(targetPerson != null && ( targetPerson.equalsIgnoreCase("SPOUSE") || targetPerson.equalsIgnoreCase("WIFE") ||  
	    			 targetPerson.equalsIgnoreCase("HUSBAND") || targetPerson.equalsIgnoreCase("EX-WIFE") || targetPerson.equalsIgnoreCase("EX-HUSBAND")) ) 
	    	 {
	    		 targetPerson = "SPOUSE";
	    	 }
	    	 else if(targetPerson != null && ( targetPerson.equalsIgnoreCase("PARENT") || targetPerson.equalsIgnoreCase("MOTHER") ||  
	    			 targetPerson.equalsIgnoreCase("FATHER") || targetPerson.equalsIgnoreCase("PARENT-IN-LAW") || targetPerson.equalsIgnoreCase("MOTHER-IN-LAW") || 
	    			 targetPerson.equalsIgnoreCase("FATHER-IN-LAW") || targetPerson.equalsIgnoreCase("STEPPARENT") || targetPerson.equalsIgnoreCase("STEPMOTHER") || 
	    			 targetPerson.equalsIgnoreCase("STEPFATHER") || targetPerson.equalsIgnoreCase("GRANDPARENT") || targetPerson.equalsIgnoreCase("GRANDMOTHER")  || 
	    			 targetPerson.equalsIgnoreCase("GRANDFATHER") || targetPerson.equalsIgnoreCase("GRANDPARENT-IN-LAW") || targetPerson.equalsIgnoreCase("GRANDMOTHER-IN-LAW") || 
	    			 targetPerson.equalsIgnoreCase("GRANDFATHER-IN-LAW") || targetPerson.equalsIgnoreCase("STEP-GRANDPARENT") || targetPerson.equalsIgnoreCase("STEP-GRANDMOTHER") || 
	    			 targetPerson.equalsIgnoreCase("STEP-GRANDFATHER") || targetPerson.equalsIgnoreCase("GREAT-GRANDPARENT") || targetPerson.equalsIgnoreCase("GREAT-GRANDMOTHER") || 
	    			 targetPerson.equalsIgnoreCase("GREAT-GRANDFATHER") || targetPerson.equalsIgnoreCase("AUNT") || targetPerson.equalsIgnoreCase("UNCLE") )) 
	    	 {
	    		 coupleRef = false;
	    		 targetPerson = "PARENT";
	    	 }
	    	 else if(targetPerson != null && (targetPerson.equalsIgnoreCase("SIBLING") || targetPerson.equalsIgnoreCase("SISTER") ||  
	    			 targetPerson.equalsIgnoreCase("BROTHER") || targetPerson.equalsIgnoreCase("SIBLING-IN-LAW") || targetPerson.equalsIgnoreCase("SISTER-IN-LAW") ||  
	    			 targetPerson.equalsIgnoreCase("BROTHER-IN-LAW") || targetPerson.equalsIgnoreCase("STEPSIBLING") || targetPerson.equalsIgnoreCase("STEPSISTER") || 
	    			 targetPerson.equalsIgnoreCase("STEPBROTHER"))) 
	    	 {
	    		 coupleRef = false;
	    		 targetPerson = "SIBLING";
	    	 }
	    	 
	    	 else if(targetPerson != null && ( targetPerson.equalsIgnoreCase("CHILD") || targetPerson.equalsIgnoreCase("DAUGHTER") ||  
	    			 targetPerson.equalsIgnoreCase("SON") || targetPerson.equalsIgnoreCase("CHILD-IN-LAW") || targetPerson.equalsIgnoreCase("DAUGHTER-IN-LAW") ||  
	    			 targetPerson.equalsIgnoreCase("SON-IN-LAW") || targetPerson.equalsIgnoreCase("STEPCHILD") || targetPerson.equalsIgnoreCase("STEPDAUGHTER") ||
	    			 targetPerson.equalsIgnoreCase("STEPSON") || targetPerson.equalsIgnoreCase("GRANDCHILD") || targetPerson.equalsIgnoreCase("GRANDDAUGHTER") ||  
	    			 targetPerson.equalsIgnoreCase("GRANDSON") || targetPerson.equalsIgnoreCase("GRANDCHILD-IN-LAW") || targetPerson.equalsIgnoreCase("GRANDDAUGHTER-IN-LAW") ||  
	    			 targetPerson.equalsIgnoreCase("GRANDSON-IN-LAW") || targetPerson.equalsIgnoreCase("STEP-GRANDCHILD") || targetPerson.equalsIgnoreCase("STEP-GRANDDAUGHTER") ||
	    			 targetPerson.equalsIgnoreCase("STEP-GRANDSON") )) 
	    	 {
	    		 coupleRef = false;
	    		 targetPerson = "CHILD";
	    	 }
	    	 else if (targetPerson != null)
	    	 {
	    		 coupleRef = false;
	    		 targetPerson = "OTHER";
	    	 }
	    	   
	    	List <String> nameLastList = new ArrayList<String>();
	    	List <String> birthLastList = new ArrayList<String>();
	    	
	    	//start xmlForm
	    	r.appendToXmlForm("<permuter>\n");
	    	
	    	//loop through each person in the record to grab all other key info - name, suffix, key terms
	    	for (int i = 0; i < list.size(); i++)  
	    	{
			String nameFirst = null;
			String nameMid = null;
			String nameLast = null;
			String birthLast = null;
			String nameNick = null;
			String suffixValue = null;

		   Element node = (Element) list.get(i);
		   String first = null;
		   String last = null;
		   
		   //Capture name from record
		   if(node.getChildText("person_PR_NAME_GN", NS2) != null || node.getChildText("person_PR_NAME_SURN", NS2) != null)
		   {
			   first = node.getChildText("person_PR_NAME_GN", NS2); 
			   last = node.getChildText("person_PR_NAME_SURN", NS2);
			   
			   if(node.getChildText("person_PR_NAME_SUF", NS2) != null )
			   {
				   suffixValue = removeEndPer(node.getChildText("person_PR_NAME_SUF", NS2));
			   }
		   }
		   else // if FSHOBIT document ------------------------------------------------------------------
		   {
			   //String fullname = node.getChildText("person_PR_NAME", NS2).replaceAll("[^\\p{L} ]", " ");
			   String fullname = node.getChildText("person_PR_NAME", NS2);
			   
			   //period logic 
			   fullname = removeEndPer(fullname);
			   
			   //take out double spaces
			   while (fullname.contains("  ")) 
			   {
				   fullname = fullname.replace("  ", " ");
			   }
			   String [] nameArray = fullname.split(" ");
			   String [] newNameArr = null;
			   boolean pre = false;
			   boolean suff = false;

			   //see if first part of nameArray includes any of the prefix's
			   for (int x = 0; x < prefix.length; x++) 
			   {
				   if(nameArray[0].equalsIgnoreCase(prefix[x]))
				   {
					   pre = true;
					   newNameArr = Arrays.copyOfRange(nameArray, 1, nameArray.length);
					   //loop through all prefix options again for next word in name
					   for (int y = 0; y < prefix.length; y++)
					   {
						   if(newNameArr[0].equalsIgnoreCase(prefix[y]))
						   {
							   newNameArr = Arrays.copyOfRange(newNameArr, 1, newNameArr.length);
						   }
					   }
				   }   
			   }
			   
			   //save suffix in a StringBuilder
			   String namesuf1 = null;
			   String namesuf2 = null;
				
			   //see if last two parts of newNameArr includes any of the suffix's
			   for (int x = 0; x < suffix.length; x++) 
			   {
				   if(pre == true)
				   {
					   if(newNameArr[newNameArr.length-1].equalsIgnoreCase(suffix[x]))
					   {
						   suff = true;
						   
						   //add suffix to nameSuf
						   namesuf1 = newNameArr[newNameArr.length-1];
							
						   //remove end suffix from array
						   newNameArr = Arrays.copyOfRange(newNameArr, 0, newNameArr.length-1);
						   
						   //added for recursion
						   for (int y = 0; y < suffix.length; y++) 
						   {
							   if(newNameArr[newNameArr.length-1].equalsIgnoreCase(suffix[y]))
							   {
								   //add suffix to nameSuf
								   namesuf2 = newNameArr[newNameArr.length-1];
									
								   newNameArr = Arrays.copyOfRange(newNameArr, 0, newNameArr.length-1);
							   }
						   }
					   }
				   }
				   //if pre equals false
				   else 
				   {
					   if(nameArray[nameArray.length-1].equalsIgnoreCase(suffix[x]))
					   {
						   suff = true;
						   
						   //add suffix to nameSuf
						   namesuf1 = nameArray[nameArray.length-1];
							
						   //remove end suffix from array
						   newNameArr = Arrays.copyOfRange(nameArray, 0, nameArray.length-1);
						   
						   //added for recursion
						   for (int y = 0; y < suffix.length; y++) 
						   {
							   if(newNameArr[newNameArr.length-1].equalsIgnoreCase(suffix[y]))
							   {  
								   //add suffix to nameSuf
								   namesuf2 = newNameArr[newNameArr.length-1];
									
								   //remove end suffix from array
								   newNameArr = Arrays.copyOfRange(newNameArr, 0, newNameArr.length-1);
							   }
						   }
					   }  
				   }
			   }
			   
			   //make newNameArr equal to nameArray if there were no pre / suff modifications
			   if(pre == false && suff == false)
			   {
				   newNameArr = nameArray;
			   }
			   
			   //split newNameArr into first and last names
			   //if there is one part to the name
			   if (newNameArr.length == 1 && ( pre == true || suff == true ) ) 
			   {
				   last = newNameArr[0];
			   } 
			   else if (newNameArr.length == 1 && pre == false) 
			   {
				   first = newNameArr[0];
			   }
			   else if(newNameArr.length > 1)	//if there is more than one part
			   { 
				   String [] firstArr = Arrays.copyOfRange(newNameArr, 0, newNameArr.length-1);
				   String [] lastArr = Arrays.copyOfRange(newNameArr, newNameArr.length-1, newNameArr.length);

				   //first name
				   boolean pass = false;
				   StringBuilder firstBuilder = new StringBuilder();
				   for(String s : firstArr) 
				   {   
					   if(pass == true)
					   {
						   firstBuilder.append(" ");
					   }
					   
					   firstBuilder.append(s);
					   pass = true;
				   }
				   first = firstBuilder.toString();
				   
				   //last name
				   pass = false;
				   StringBuilder lastBuilder = new StringBuilder();
				   for(String s : lastArr) 
				   {
					   if(pass == true)
					   {
						   lastBuilder.append(" ");
					   }
					   
					   lastBuilder.append(s);
					   pass = true;
				   }
				   last = lastBuilder.toString();
				   
				    //save name suffix to suffixValue
				    if(namesuf1 != null || namesuf2 != null)
				    {
				    		if(namesuf1 != null && namesuf2 != null)
				    		{
				    			suffixValue = namesuf2+" "+namesuf1;
				    		}
				    		else if(namesuf1 != null && namesuf2 == null)
				    		{
				    			suffixValue = namesuf1;
				    		}
				    		suffixValue = removeEndPer(suffixValue);
				    }
			   }
		   } // --------------------------- end FSHOBIT section ----------------------------------------------------
		   
		   //Element genderElement = node.getChild("person_PR_SEX_CODE", NS2);
		   
		   //remove any Or in a name element
		    if (first != null && first.contains(" Or ")) 
		    {
			  String[] orBirthFirst = first.split(" Or ");

			  first = orBirthFirst[0].substring(0, orBirthFirst[0].length());
			  nameNick = orBirthFirst[1].substring(0, orBirthFirst[1].length());
		    } 
		    
		     if (last != null && last.contains(" Or")) 
		     {
		    		int or = last.indexOf(" Or");
				last = last.substring(0, or);
		     }
		     
		     //if the Or is the first thing in the SURName field
		     else if (last != null && last.contains("Or")) 
		     {
		    	 	last = null;
		     }
		     
		     //break up name into parts
		     String[] firstPart = null;
		     if(first != null) {
		    	 	firstPart = first.split(" ");
		     }
		     
		     String[] secondPart = null;
	    		 if(last != null) 
	    		 {
	    			 secondPart = last.split(" ");
	    		 }
		    		 
		    	//firstPart name separation	 
		    if(first != null && firstPart.length == 3) 
		    {
				nameFirst = firstPart[0].substring(0, firstPart[0].length());
				nameMid = firstPart[1].substring(0, firstPart[1].length());
	    		 	birthLast = firstPart[2].substring(0, firstPart[2].length());
		     }
		     else if(first != null && firstPart.length == 2) 
		     {
				nameFirst = firstPart[0].substring(0, firstPart[0].length());
				nameMid = firstPart[1].substring(0, firstPart[1].length()); 
		     }
		     else if(first != null && firstPart.length == 1) 
		     {
				nameFirst = firstPart[0].substring(0, firstPart[0].length());					   
		     }
		    
	    		//secondPart name separation 
		    	 if (last != null && secondPart.length == 2)
		    	 {
		    		 birthLast = secondPart[0].substring(0, secondPart[0].length());
			   	 nameLast = secondPart[1].substring(0, secondPart[1].length());
		    	 }
		    	 else if (last != null && secondPart.length == 1)
		    	 {
		    		 nameLast = secondPart[0].substring(0, secondPart[0].length());
		    	 }
		    	 String pid = node.getChildText("person_id", NS2);
		    	 
		    	 //see value of targetPerson to decide primaryPerson & set primaryPerson and secondaryPerson to Relations
		    	 if(targetPerson!=null && targetPerson.equalsIgnoreCase("HEAD")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, head0, name);
		    		 }
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, head, nameLastList, birthLastList);
		     }
		    	 else if(targetPerson != null && targetPerson.equalsIgnoreCase("SPOUSE")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, spouse0, name);
		    		 }
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, spouse, nameLastList, birthLastList);
		    	 }
		    	 else if(targetPerson != null && targetPerson.equalsIgnoreCase("PARENT")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, parent0, name);
		    		 }
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, parent, nameLastList, birthLastList);
		    	 }
		    	 else if(targetPerson != null && targetPerson.equalsIgnoreCase("SIBLING")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, sibling0, name);
		    		 }
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, sibling, nameLastList, birthLastList);
		    	 }
		    	 else if(targetPerson != null && targetPerson.equalsIgnoreCase("CHILD")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, child0, name);
		    		 }
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, child, nameLastList, birthLastList);
		    	 }
		    	 else if(targetPerson != null && targetPerson.equalsIgnoreCase("OTHER")) 
		    	 {
		    		 if(pid.equalsIgnoreCase(targetPersonID))
		    		 {
		    			 String [] tp = {targetprim};
		    			 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, tp, name);
		    		 }
		    	 }
		    	 else 
		    	 {
		    		 addPrimary(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, head0, name);
		    		 addSecond(r, node, NS2, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, all, nameLastList, birthLastList);
		    	 }
		 } //------------------------------------- end looping through persons ------------------------------------------------------------------------------
	    	
	    	//add model element if census document
	    	if (containerQuery.isSet() && !containerQuery.isDefault())
	    	{
	    		r.appendToXmlForm("<model>census</model>\n");
	    	}
	    	//close xmlForm and set to r
	    	r.appendToXmlForm("</permuter>");
	    	
	    	//for nameFirst/nameMid assumptions
	    	setNameLastList(r, nameLastList);
	  } 
		catch (Throwable io) {
			kLogger.warn(io.getMessage(), io);
//		io.printStackTrace(System.err);
	  }
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
		kLogger.debug("elapsed time: "+ timeElapsed);
		
		return r;
	}

	public String permute(Relations r) 
	{
		if( form != null && (form.equalsIgnoreCase("true") || form.equals("1")) )
		{
			return r.getXmlForm().toString();
		}
		else
		{
		//initialize variables
	 	String nameFirst = null;
		String nameMid = null;
		String birthLast = null;
		String nameLast = null;
		String nameSuf = null;
	 	String fNameFirst = null;
		//String fNameNick = null;
	 	String fBirthLast = null;
		String fNameLast = null;
		//Gender gender = null;
		char nameFirstInitial = 0;
		char nameMidInitial = 0;
		char birthLastInitial = 0;
		String midInit = null;
		char midInit0 = 0;
		char sMnInit0 = 0;
		String sMnInit = null;
		String sMid = null;
		String sBlast = null;
		String theKey = null;
		String mid = null;
		String firstN = null;
		boolean first = false; //for coupleRefs		
		StringBuilder trip = new StringBuilder();
		StringBuilder init = new StringBuilder();
		StringBuilder suff = new StringBuilder();
		List<String> spouses = new ArrayList<String>();
		List<String> fNameLastList = new ArrayList<String>();
		
		//===================================================
		//start building primary triplet
		//===================================================
		trip.append("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0=(");

		//for loop to iterate through all the relatives in r
		Map <String, List<Person>> primaries = r.getPrimaries();
		Iterator<String> itPrimary = primaries.keySet().iterator();
		while (itPrimary.hasNext())
		{
			theKey = itPrimary.next();
			//kLogger.debug("Relatives:" + theKey);
			
			//iterate through all primary keys
			List<Person> personList = primaries.get(theKey);
			for(Person primary : personList)
			{
			 	nameFirst = primary.getNameFirst();
				nameMid = primary.getNameMid();
				birthLast = primary.getBirthLast();
				nameLast = primary.getNameLast();
				nameSuf = primary.getNameSuf();
				
				//retain nameSuff info and store it in suff StringBuilder for later use
				if(nameSuf != null)
				{
					//remove end periods if any
					nameSuf = removeEndPer(nameSuf);
					
					if (nameLast != null)
					{
						suff.append("\"" +nameLast+ " " + nameSuf+ "\"");
						
						//if nameSuf contains a '.'
						if(nameSuf.contains("."))
						{
							suff.append("\"" +nameLast+ " " + nameSuf.replace(".", "")+ "\"");
						}
					}
					if (birthLast != null)
					{
						suff.append("\"" +birthLast+ " " + nameSuf+ "\"");
						
						//if nameSuf contains a '.'
						if(nameSuf.contains("."))
						{
							suff.append("\"" +birthLast+ " " + nameSuf.replace(".", "")+ "\"");
						}
					}
					if (nameLast == null && birthLast == null)
					{
						suff.append("\"" +nameSuf+ "\"");
						
						//if nameSuf contains a '.'
						if(nameSuf.contains("."))
						{
							suff.append("\"" +nameSuf.replace(".", "")+ "\"");
						}
					}
				}
				
				//get initials of useful names
				if (nameFirst != null) 
				{
					//take out any quotation
					if (nameFirst.contains("\""))
					{
						nameFirst = nameFirst.replace("\"", "");
					}
					nameFirst = removeEndPer(nameFirst);
					nameFirstInitial = nameFirst.charAt(0);
				}
				if (nameMid != null) 
				{
					//take out any quotation
					if(nameMid.contains("\""))
					{
						nameMid = nameMid.replace("\"", "");
					}
					nameMid = removeEndPer(nameMid);
					nameMidInitial = nameMid.charAt(0);
					sMid = Character.toString(nameMidInitial);
				}
				if (birthLast != null && !birthLast.isEmpty()) 
				{
					//take out any quotation
					if(birthLast.contains("\""))
					{
						birthLast = birthLast.replace("\"", "");
					}
					birthLast = removeEndPer(birthLast);
					birthLastInitial = birthLast.charAt(0);
					sBlast = Character.toString(birthLastInitial);
				}
				
				//take out any quotation
				if (nameLast != null && nameLast.contains("\""))
				{
					nameLast = nameLast.replace("\"", "");
				}
				nameLast = removeEndPer(nameLast);
				
				//if nameMid has more than one parts 
				String nameMids = null;
				if (nameMid != null && nameMid.contains(" "))
				{
					String [] nm = nameMid.split(" ");
					StringBuilder nm2= new StringBuilder();
					
					for(int i = 0; i < nm.length; i++) 
					{
						nm2.append(nm[i].charAt(0) + " ");
					}
					nameMids = nm2.toString();
				}
				
				//set up for coupleRefs
				if (first == false) 
				{
					fNameFirst = nameFirst;
					fBirthLast = birthLast;
					fNameLast = nameLast;
				}
				
				//set fNameLastList with primary nameLast & birthLast
				if(nameLast != null)
				{
					fNameLastList.add(nameLast);
				}
				if(birthLast != null)
				{
					fNameLastList.add(birthLast);
				}
				
				//boolean to state weather first or not first primary person (first = true once first primary has passed)
				first = true;
				
				boolean permutationsBool = false;
				//call and append primary person permutations to trip
				trip.append( primary.primPermute(init, nameFirstInitial, nameMidInitial, birthLastInitial, nameMids, sMid, sBlast, fNameLastList, permutationsBool) );
			}
		}
		trip.append(")");
		String str1 = trip.toString().substring(trip.indexOf("(") +1, trip.indexOf(")"));
		str1 = deleteDup(str1,nameFirst,nameLast,birthLast);
		
		//primary-1 triplet
		if (init != null && !init.toString().isEmpty()) 
		{
			trip.append("&p_bool_primary-1=OR&p_field_primary-1=alltext&p_params_primary-1=weight:0&p_text_primary-1=(" +
		init + ")");
		}
		
		//===================================================
		//start building PRIMARY couple ref triplet
		//===================================================
		String [] all = {"WIFE", "HUSBAND", "SPOUSE", "EX-WIFE","EX-HUSBAND", "FIANCE", "DOMESTIC PARTNER"};
		StringBuffer onear2 = new StringBuffer();
		
		//boolean to make sure only first spouses' info gets retained
		boolean firstSpouse = true;
		
		trip.append("&p_bool_primary-2=OR&p_field_primary-2=alltext&p_params_primary-2=weight:1&p_text_primary-2=(");
		if (!r.getRelatives().isEmpty()) 
		{
			//for loop to make this iterate through all the relatives in r
			Map <String, List<Person>> relatives = r.getRelatives();
			Iterator<String> it=relatives.keySet().iterator();
			while (it.hasNext())
			{
				String theKey2 = it.next();
				List<Person> personList = relatives.get(theKey2);
				for(Person relative : personList)
				{
					for (int a = 0; a < all.length; a++)	
					{
						if (relative.getRelationship()!=null) 
						{
							if (relative.getRelationship().equalsIgnoreCase(all[a])) 
							{
								mid = relative.getNameMid();
								if (mid != null) 
								{	//take out any quotation
									if (mid.contains("\""))
									{
										mid = mid.replace("\"", "");
									}
									mid = removeEndPer(mid);
									midInit0 = relative.getNameMid().charAt(0);
									midInit = Character.toString(midInit0);
								}
	
								firstN = relative.getNameFirst();
								//take out any quotation
								if (firstN != null && firstN.contains("\""))
								{
									firstN = firstN.replace("\"", "");
								}
								firstN = removeEndPer(firstN);
								
								//save first spouses first & mid name (if available) to construct second coupleRefs
								if (firstSpouse == true && mid != null && relative.getNameLast() != null) 
								{
									spouses.add(firstN+" "+ mid+" "+ relative.getNameLast());
								}
								else if (firstSpouse == true && mid == null && relative.getNameLast() != null) 
								{
									spouses.add(firstN+" "+ relative.getNameLast());
								}
								else if (firstSpouse == true)
								{
									spouses.add(firstN);
								}
								
								//set to false so only the first spouses' info gets used
								firstSpouse = false;
								
								//loop through primaries here 
								itPrimary=primaries.keySet().iterator();
								while (itPrimary.hasNext())
								{
									theKey = itPrimary.next();
									//kLogger.debug("Relatives:" + theKey);
									
									personList = primaries.get(theKey);
									for(Person primary : personList)
									{
								 	nameFirst = primary.getNameFirst();
									nameMid = primary.getNameMid();
									birthLast = primary.getBirthLast();
									nameLast = primary.getNameLast();
									
									//start forming coupleRefs
									if ( coupleRef == true && nameFirst != null && firstN != null && nameLast != null && nameLast.equalsIgnoreCase(relative.getNameLast()) ) 
									{
										if (nameFirst.length()>1)
										{
											trip.append("\"" +nameFirst + " " + firstN + " " + nameLast+ "\"");
											trip.append("\"" +nameFirst + " and " + firstN + " " + nameLast+ "\"");
											//primary-3
											if (nameMid==null&&mid==null) 
											{
												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3" + firstN  +"q0t3 ONEAR/2 q0t3"+ nameLast+ "q0t3");
												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3and " + firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
											}
											if (nameMid==null) 
											{
												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3" + firstN  +" "+ nameLast+ "q0t3");
												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3and " + firstN + " " + nameLast+ "q0t3");
											}

											if (mid==null) 
											{
												onear2.append("q0t3" +nameFirst + " " + firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
												onear2.append("q0t3" +nameFirst + " and " + firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
											}
										}
			
										if (nameMid != null) 
										{
											//make sure nameMid is not just an initial
											if(nameMid.length() > 1)
											{
												trip.append("\"" +nameFirst + " " + nameMid +" "+ firstN + " " + nameLast+ "\"");
												trip.append("\"" +nameFirst + " " + nameMid +" and "+ firstN + " " + nameLast+ "\"");
											}

											trip.append("\"" +nameFirst + " " + nameMidInitial +" "+ firstN + " " + nameLast+ "\"");
											trip.append("\"" +nameFirst + " " + nameMidInitial +" and "+ firstN + " " + nameLast+ "\"");

											if (mid==null) 
											{
												//make sure nameMid is not just an initial
												if(nameMid.length() > 1)
												{
													onear2.append("q0t3" +nameFirst + " " + nameMid +" "+ firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
													onear2.append("q0t3" +nameFirst + " " + nameMid +" and "+ firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
												}

												onear2.append("q0t3" +nameFirst + " " + nameMidInitial +" "+ firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
												onear2.append("q0t3" +nameFirst + " " + nameMidInitial +" and "+ firstN + "q0t3 ONEAR/2 q0t3" + nameLast+ "q0t3");
											}
										}
										
										if (mid != null&&nameFirst.length()>1) 
										{
											//make sure mid is not just an initial
											if(mid.length() > 1)
											{
												trip.append("\"" +nameFirst + " " + firstN + " " + mid +" "+ nameLast+ "\"");
												trip.append("\"" +nameFirst + " and " + firstN + " " + mid +" "+ nameLast+ "\"");
											}

											trip.append("\"" +nameFirst + " " + firstN + " " + midInit +" "+ nameLast+ "\"");
											trip.append("\"" +nameFirst + " and " + firstN + " " + midInit +" "+ nameLast+ "\"");
											//primary-3
											if(nameMid==null) 
											{
												//make sure mid is not just an initial
												if(mid.length() > 1)
												{
													onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3" + firstN + " " + mid +" "+ nameLast+ "q0t3");
													onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3and " + firstN + " " + mid +" "+ nameLast+ "q0t3");
												}

												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3" + firstN + " " + midInit +" "+ nameLast+ "q0t3");
												onear2.append("q0t3" +nameFirst + "q0t3 ONEAR/2 q0t3and " + firstN + " " + midInit +" "+ nameLast+ "q0t3");
											}
										}
										
										if ( mid != null && nameMid != null) 
										{
											//make sure nameMid && mid are not just initials
											if(mid.length() > 1 && nameMid.length() > 1)
											{
												trip.append("\"" +nameFirst + " " + nameMid +" "+ firstN + " " + mid +" "+ nameLast+ "\"");
												trip.append("\"" +nameFirst + " " + nameMid +" and "+ firstN + " " + mid +" "+ nameLast+ "\"");
											}

											trip.append("\"" +nameFirst + " " + nameMidInitial +" "+ firstN + " " + midInit +" "+ nameLast+ "\"");
											trip.append("\"" +nameFirst + " " + nameMidInitial +" and "+ firstN + " " + midInit +" "+ nameLast+ "\"");

											trip.append("\"" +nameFirst + " " + nameMid +" "+ firstN + " " + midInit +" "+ nameLast+ "\"");
											trip.append("\"" +nameFirst + " " + nameMid +" and "+ firstN + " " + midInit +" "+ nameLast+ "\"");
											trip.append("\"" +nameFirst + " " + nameMidInitial +" "+ firstN + " " + mid +" "+ nameLast+ "\"");
											trip.append("\"" +nameFirst + " " + nameMidInitial +" and "+ firstN + " " + mid +" "+ nameLast+ "\"");
											}
										}
										//finish looping through primaries
									}
								}		
							}
						}
						 mid=null;
					}
				}
			}
		}
		trip.append(")");
		//add to second-7
		String prim2 = deleteDup(trip.toString().substring(trip.indexOf("primary-2=(") + 11, trip.length()-1), nameFirst, nameLast, birthLast);
		
		//remove primary-2 is triplet is empty
		if(trip.toString().contains("&p_bool_primary-2=OR&p_field_primary-2=alltext&p_params_primary-2=weight:1&p_text_primary-2=()")) 
		{
			trip.delete(trip.length() - 94, trip.length());
		}
		
		//primary-3 triplet
		if(onear2 != null && !onear2.toString().isEmpty()) 
		{
			trip.append("&p_bool_primary-3=OR&p_field_primary-3=alltext&p_params_primary-3=weight:0&p_text_primary-3=(" +
		onear2 + ")");
		}
		
			//===================================================
			//start building relatives triplet
			//===================================================
			trip.append("&p_field_second-0=alltext&p_params_second-0=weight:1&p_text_second-0=(");	
			
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
					String relNameLast = relative.getNameLast();
					String relBirthLast = relative.getBirthLast();
					//String relationship = relative.getRelationship();
					//Gender relGender = relative.getGender();
					char relNameMidInitial = 0;
					char relBirthLastInitial = 0;
					char relNameFirstInitial = 0;
					String sRelMid = null;
					String sRelBlast = null;
		
					//set initials & fix necessary names ==========================================================
					if(relNameFirst != null) 
					{
						//take out any quotation
						if(relNameFirst.contains("\""))
						{
							relNameFirst = relNameFirst.replace("\"", "");
						}
						relNameFirst = removeEndPer(relNameFirst);
						relNameFirstInitial = relNameFirst.charAt(0);
					}
					if(relNameMid != null) 
					{
						//take out any quotation
						if(relNameMid.contains("\""))
						{
							relNameMid = relNameMid.replace("\"", "");
						}
						relNameMid = removeEndPer(relNameMid);
						relNameMidInitial = relNameMid.charAt(0);
						sRelMid = Character.toString(relNameMidInitial);
					}
					if(relBirthLast != null) 
					{
						//take out any quotation
						if(relBirthLast.contains("\""))
						{
							relBirthLast = relBirthLast.replace("\"", "");
						}
						relBirthLast = removeEndPer(relBirthLast);
						relBirthLastInitial = relBirthLast.charAt(0);
						sRelBlast = Character.toString(relBirthLastInitial);
					}
					//take out any quotation
					if(relNameLast != null && relNameLast.contains("\""))
					{
						relNameLast = relNameLast.replace("\"", "");
					}
					relNameLast = removeEndPer(relNameLast);
					//==========================================================
					
					//if nameMid has more than one parts 
					String relNameMids = null;
					if(relNameMid != null && relNameMid.contains(" ")) 
					{
						String [] nm = relNameMid.split(" ");
						StringBuilder nm2= new StringBuilder();
						
						for(int i = 0; i < nm.length; i++) 
						{
							nm2.append(nm[i].charAt(0) + " ");
						}
						relNameMids = nm2.toString();
					}
					
					//call and append second persons permutations to trip
					trip.append( relative.secPermute(relNameFirstInitial, relNameMidInitial, relBirthLastInitial, fNameFirst, fBirthLast, fNameLast, relNameMids, sRelMid, sRelBlast, fNameLastList) );					
				}
			}
			
			trip.append(")");
			
			//remove second-0 triplet if empty
			if(trip.toString().contains("&p_field_second-0=alltext&p_params_second-0=weight:1&p_text_second-0=()")) 
			{
				trip.delete(trip.length() - 71, trip.length());
			}

			//===================================================
			//start building sec coupleRef triplet
			//===================================================
			//populate
			String spouse = null;
			String [] split;
			String sFn = null;
			String sMn = null;
			String sLn = null;
			trip.append("&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=(");
			
			//check if there is anything in spouses
			for (int i = 0; i < spouses.size(); i++)
			{
				if(spouses.get(i) != null)
				{
				if(coupleRef == true){
					spouse = spouses.get(i).trim();
					if(spouse.contains(" ")) 
					{	
						//if spouse has any double space 
						if(spouse.contains("  "))
						{
							spouse = spouse.replace("  ", " ");
						}
						split = spouse.split(" ");
						
						//if spouse has first, mid, last
						if(split.length == 3)
						{
							sFn=split[0];
							sMn=split[1];
							sLn=split[2];
						}
						
						//if spouse has first & last
						if(split.length == 2)
						{
							sFn=split[0];
							sLn=split[1];
						}
					}
					else
						sFn = spouse;
					
					//take out any quotation
					if(sFn != null && sFn.contains("\""))
					{
						sFn = sFn.replace("\"", "");
					}
					sFn = removeEndPer(sFn);
					
					if(sMn != null) 
					{
						//take out any quotation
						if(sMn.contains("\""))
						{
							sMn = sMn.replace("\"", "");
						}
						sMn = removeEndPer(sMn);
						sMnInit0 = sMn.charAt(0);
						sMnInit = Character.toString(sMnInit0);
					}
	
					//loop through primaries here 
					itPrimary=primaries.keySet().iterator();
					while (itPrimary.hasNext())
					{
						theKey = itPrimary.next();
						//kLogger.debug("Relatives:" + theKey);
						
						List<Person> personList = primaries.get(theKey);
						for(Person primary : personList)
						{
							nameFirst = primary.getNameFirst();
							nameMid = primary.getNameMid();
							birthLast = primary.getBirthLast();
							nameLast = primary.getNameLast();
							
							if(nameFirst!= null && sFn != null && nameLast!=null && nameLast.equalsIgnoreCase(sLn)) 
								{
							if(sFn.length()>1)
							{
								trip.append("\"" + sFn + " " + nameFirst+ " " + nameLast+ "\"");
								trip.append("\"" + sFn + " and " +  nameFirst + " " + nameLast+ "\"");
							
								if(nameMid != null) 
								{
									//make sure nameMid is not just an initial
									if(nameMid.length() > 1)
									{
										trip.append("\"" +sFn + " " +nameFirst + " " + nameMid +" "+  nameLast+ "\"");
										trip.append("\"" + sFn + " and "  +nameFirst + " " + nameMid +" "+ nameLast+ "\"");
									}

									trip.append("\"" +sFn + " " +nameFirst + " " + nameMidInitial +" "+  nameLast+ "\"");
									trip.append("\"" + sFn + " and "  +nameFirst + " " + nameMidInitial +" "+ nameLast+ "\"");
								}
							}
							
							if(sMn != null) 
							{
								//make sure sMn is not just an initial
								if(sMn.length() > 1)
								{
									trip.append("\"" + sFn + " "+ sMn +" "+nameFirst + " "  + nameLast+ "\"");
									trip.append("\"" + sFn + " "+ sMn +" and "+nameFirst + " " + nameLast+ "\"");
								}

								trip.append("\"" + sFn + " "+ sMnInit +" "+nameFirst + " "  + nameLast+ "\"");
								trip.append("\"" + sFn + " "+ sMnInit +" and "+nameFirst + " " + nameLast+ "\"");
							}
							if( sMn != null && nameMid != null) 
							{
								//make sure sMn && nameMid are not just initials
								if(sMn.length() > 1 && nameMid.length() > 1)
								{
									trip.append("\"" + sFn + " " + sMn +" "+nameFirst + " " + nameMid +" "+ nameLast+ "\"");
									trip.append("\"" + sFn + " " + sMn +" and "+nameFirst + " " + nameMid +" "+ nameLast+ "\"");
								}

								trip.append("\"" + sFn + " " + sMnInit +" "+nameFirst + " " + nameMidInitial +" "+ nameLast+ "\"");
								trip.append("\"" + sFn + " " + sMnInit +" and "+nameFirst + " " + nameMidInitial +" "+ nameLast+ "\"");

								trip.append("\"" + sFn + " " + sMn +" "+nameFirst + " " + nameMidInitial +" "+ nameLast+ "\"");
								trip.append("\"" + sFn + " " + sMn +" and "+nameFirst + " " + nameMidInitial +" "+ nameLast+ "\"");
								trip.append("\"" + sFn + " " + sMnInit +" "+nameFirst + " " + nameMid +" "+ nameLast+ "\"");
								trip.append("\"" + sFn + " " + sMnInit +" and "+nameFirst + " " + nameMid +" "+ nameLast+ "\"");						
								}
							}
						}
					}
					sMn = null;
					}
				}
			}
			trip.append(")");
			
			//remove second-2 if triplet is empty
			if(trip.toString().contains("&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=()")) 
			{
				trip.delete(trip.length() - 90, trip.length());
			}

			//===================================================
			//start building terms triplet
			//===================================================
			List<Terms> termsList = r.getTerms("all");
			
			if(termsList != null && termsList.get(0).countTerms() != 0)
			{
				trip.append("&p_bool_second-4=OR&p_field_second-4=alltext&p_params_second-4=weight:1&p_text_second-4=(");
				addTerms(termsList,trip);
				trip.append(")");
			}
			
			trip.append("&p_bool_second-5=OR&p_field_second-5=alltext&p_params_second-5=weight:0&p_text_second-5=(");
			
			//delete all duplicate phrase in triplets above
			StringBuilder t = new StringBuilder();
			t.append(deleteDup(trip.toString(), nameFirst, nameLast, birthLast));
			
			t.append(str1 + ")");
			
			if(t.toString().contains("&p_bool_second-5=OR&p_field_second-5=alltext&p_params_second-5=weight:0&p_text_second-5=()")) 
			{
				t.delete(t.length()-90, t.length());
			}
			
			if(init != null && !init.toString().isEmpty()) 
			{
				String initFinal = deleteDup(init.toString(), nameFirst, nameLast, birthLast);
				t.append("&p_bool_second-6=OR&p_field_second-6=alltext&p_params_second-6=weight:0&p_text_second-6=(" +initFinal+ ")");
			}
			
			//triplet second-7
			if(prim2 != null && !prim2.equals("")) 
			{
				t.append("&p_bool_second-7=OR&p_field_second-7=alltext&p_params_second-7=weight:0&p_text_second-7=(" +prim2+ ")");
			}
			
			//triplet second-8
			if(onear2 != null && !onear2.toString().isEmpty()) 
			{
				t.append("&p_bool_second-8=OR&p_field_second-8=alltext&p_params_second-8=weight:0&p_text_second-8=(" +onear2+ ")");
			}
			
			//triplet second-9
			if(suff != null && suff.length() > 0) 
			{
				t.append("&p_bool_second-9=OR&p_field_second-9=alltext&p_params_second-9=weight:1&p_text_second-9=(");

				//add nameSuf permutations if available
				t.append(deleteDup(suff.toString(), nameFirst, nameLast, birthLast));
				//t.append(suff.toString());
				
				t.append(")");
			}
			
			//-------------------------- add notterms --------------------------
			List<Terms> termsList2 = r.getTerms("allnot");
			if(termsList2 != null)
			{
				t.append("&p_bool_notterms-0=NOT&p_field_notterms-0=alltext&p_text_notterms-0=(");
				addTerms(termsList2,t);
				t.append(")");
			}	//---------------------may not be needed--------------------------
			
			StringBuilder t2 = new StringBuilder();
			StringBuilder triplets = new StringBuilder();
			
			String tee = t.toString().replaceAll("q0t3", "\"");
			t = new StringBuilder();
			t.append(tee);
			
			//add OR between triplet phrases
			t2.append(addOr(t));
			
			//remove and '&' left in triplet phrases with a space
			String tpl = triplets.append(replaceAmp(t2)).toString();
			
			//replace any double spaces
			if(tpl.contains("  ")) 
			{
				tpl = tpl.replace("  ", " ");
			}
			
			//containerQuery logic
			tpl=containerQuery.process(tpl);
						
			return
					tpl + "&searchLogtf=true";
		}
	}

    /**method to get and add term phrases to triplets ***Terms() class?
    * @param termsList
    * @param triplet
    */
	private static void addTerms(List<Terms> termsList, StringBuilder trip) 
    {
		Terms term = new Terms();
		if(termsList != null && termsList.get(0) != null) 
		{
			for(int i=0; i< termsList.size(); i++)
			{
				term = termsList.get(i);
			}
			
			List<String> termStr = term.getTerms();
			for(int x = 0; x < termStr.size();x++) 
			{
				String terms = termStr.get(x);
				
				//make sure terms is not empty
				if(terms.length() > 0)
				{
					//remove any ending periods
					if(terms.contains(". ") || terms.charAt(terms.length() - 1) == '.')
					{
						terms = removeEndPer(terms);
					}
					
					String s = new String();
					String st = new String();
	
					//adding & or AND of terms that contain them
					if(!s.equalsIgnoreCase(terms) && !st.equalsIgnoreCase(terms)) 
					{
						trip.append("\"" +terms+ "\"");
					}				
						
					if(terms.contains(" and ") || terms.contains(" & ")) 
					{
						s = addAnd(terms, trip);
						if(s != null)
						{
							trip.append("\"");
							s = s.replace("\"", "\"\"");
							trip.append(s + "\"");
						}
					}
						
					if(terms.contains(".")) 
					{
						st = terms.replace(".", "");
	
						if(st != null && !trip.toString().contains(st) )
						{
							trip.append("\"");
							st = st.replace("\"", "\"\"");
							trip.append(st + "\"");
						}
					}
				}
			}	
		}
    }
	
    /**method to take care of and/& variations
    * @param String
    * @param triplet
    * @return
    */
    private static String addAnd(String string, StringBuilder trip) 
    {
    		String newString = null;

    		if(string.contains(" and ")) 
    		{	
    			newString = string.replace(" and ", " & ");
    			if(trip.toString().contains(newString)) 
    			{
    				newString = "";
    			}
    			else 
    				newString = newString.replace(" & ", " ");
    		}
    		else if(string.contains(" AND ")) 
    		{	
    			newString = string.replace(" AND ", " & ");
    			if(trip.toString().contains(newString)) 
    			{
    				newString = "";
    			}
    			else 
    				newString = newString.replace(" & ", " ");
    		}
    		else if(string.contains(" & ")) 
    		{	
    			newString = string.replace(" & ", " and ");
    			if(trip.toString().contains(newString)) 
    			{
    				newString = "";
    			}
    		}
    		else if(string.contains(" &amp; ")) 
    		{	
    			newString = string.replace(" &amp; ", " and ");
    			if(trip.toString().contains(newString)) 
    			{
    				newString = "";
    			}
    		}
    		else 
    			newString = "";    		
    		
    		return newString;
    }
    
    /**method to replace any '&' left in triplet string with a space
    * @param triplet
    * @return
    */
	private static String replaceAmp(StringBuilder trip) 
	{
		CharSequence target = " & ";
		CharSequence replacement = " ";
		String newString = null;
		
		for(int i = 0; i < trip.length(); i++) 
		{
    			newString = trip.toString().replace(target, replacement);

    		}

    		return newString;
    }
    
    /**method to add OR between triplet phrases
    * @param triplet
    * @return
    */
	private static String addOr(StringBuilder trip) 
	{
		CharSequence target = "\"\"";
		CharSequence replacement = "\" OR \"";
		String newString = null;
		
		for(int i = 0; i < trip.length(); i++) 
		{
    			newString = trip.toString().replace(target, replacement);
    		}
    		return newString;
    }
	
    /**method to remove period at the end of words/letters
    * @param str
    * @return
    */
	public static String removeEndPer(String str) 
	{
		if(str != null && str.length() > 0 && str.contains(". "))
		{
			str = str.replace(". ", " ");
		}
	    if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == '.') {
	        str = str.substring(0, str.length() - 1);
	    }
	    return str;
	}
	
    /**method to delete duplicate triplet values
    * @param triplet
    * @param nameFirst
    * @param nameLast
    * @param birthLast
    * @return
    */
	private static String deleteDup(String trip, String nameFirst, String nameLast, String birthLast) 
	{
	   String triplets = trip;
	   
	   List<String> words = new ArrayList<String>();
	   List<String> phrases = new ArrayList<String>();
	   String word = null;
	   String phrase = null;
	   CharSequence target = null ;
	   String removed = null;
	   
       StringTokenizer st1 = new StringTokenizer(triplets, ")\""); 
       
       while(st1.hasMoreTokens()) 
       {
    	   word = st1.nextToken().trim();
    	   words.add(word);
    	   }
       
       for(int i = 0; i < words.size(); i++) 
       {
    	   if(!words.get(i).equals(" ") && words.get(i).length() < 65) 
    	   {
    		   phrase = words.get(i);
	       phrases.add(phrase);
	       }
       }
       
       //if triplets contains any of the phrases in phrases remove from triplets
       for (int a = 0; a < phrases.size(); a ++) 
       {
	    	   String tripRev = new StringBuffer(trip).reverse().toString();
	    	   if(countNeedlesInHaystack( trip, "\""+phrases.get(a)+"\"") > 1)
	    	   {
	    		   target = phrases.get(a);
	    		   //add conditional to not check phrases that are just a persons nameFirst or nameLast or birthLast
	    		   if(!target.equals(nameFirst) && !target.equals(nameLast) && !target.equals(birthLast)) {
		    		   String targetRev = new StringBuffer(target).reverse().toString();
		    		   
			       removed = tripRev.replaceFirst(("\"" +targetRev+ "\""), "");
			       removed = new StringBuffer(removed).reverse().toString();
			       
			       trip = removed;
		    		   }
		   }
       }
       if(removed != null) 
       {
    			return removed;
       }
       else 
    	   	return trip;
    }
	
    /**method to take care of duplicate phrases split problem
    * How many times does the needle occur in the string 'haystack'?
    * @param haystack
    * @param needle
    * @return
    */
    public static int countNeedlesInHaystack(String haystack, String needle) 
    {
        int retval = 0;
        int offset = 0;
        while( (offset = haystack.indexOf(needle, offset)) != -1 ) {
               retval += 1;
               offset += 1;
        }
        return retval;
    }
    
    /**method to add primary person to relations
    * @param r
    * @param node
    * @param NS2
    * @param nameFirst
    * @param nameMid
    * @param nameNick
    * @param birthLast
    * @param nameLast
    * @param suffixValue
    * @param group
    * @param name
     * @return 
    */
    private static void addPrimary(Relations r, Element node, Namespace NS2, String nameFirst, String nameMid, String nameNick, 
    		String birthLast, String nameLast, String suffixValue, String [] group, String name)
    {
    	
   	 for (int a = 0; a < group.length; a++)
   	 {
   		 String term1 = null;
   		 String term2 = null;
   		 String term3 = null;
   		 String term4 = null;
   		 
	     if (node.getChildText("person_REL_TYPE", NS2) != null && node.getChildText("person_REL_TYPE", NS2).equalsIgnoreCase(group[a])) 
	     {
	    	 	Person primaryPerson = new Person();
	    	 	
	    	 	String rel = null;
	    	 	r.appendToXmlForm("<primary>\n");
	    	 	primaryPerson.setNames(r, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	    	 	r.appendToXmlForm("</primary>\n");
	    	 	
			r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);

			term1 = node.getChildText("person_PR_BIRTH_CITY_TOWN", NS2);
			term2 = node.getChildText("person_PR_DEATH_CITY_TOWN", NS2);
			term3 = node.getChildText("person_PR_RES_CITY_TOWN", NS2);
			term4 = node.getChildText("person_PR_BUR_CITY_TOWN", NS2);
			
			r.setTermsToRelationsFS(r, term1, term2, term3, term4);
	     }
   	
	     else if (node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2)!= null && node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).equalsIgnoreCase(group[a])) 
	     {
	    	 	Person primaryPerson = new Person();
	    	 	
	    	 	String rel = null;
	    	 	r.appendToXmlForm("<primary>\n");
	    	 	primaryPerson.setNames(r,nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	    	 	r.appendToXmlForm("</primary>\n");
	    	 	
			r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);
			
			term1 = node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2);
			term2 = node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2);
			term3 = node.getChildText("event_EVENT_CITY", NS2);
			term4 = node.getChildText("event_EVENT_COUNTY", NS2);
			
			r.setTermsToRelationsFS(r, term1, term2, term3, term4);
	     }
	     
	     else if (node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2)!= null && node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2).equalsIgnoreCase(group[a])) 
	     {
	    	 	Person primaryPerson = new Person();
	    	 	
	    	 	String rel = null;
	    	 	r.appendToXmlForm("<primary>\n");
	    	 	primaryPerson.setNames(r,nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	    	 	r.appendToXmlForm("</primary>\n");
	    	 	
			r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);

			term1 = node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2);
			term2 = node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2);
			term3 = node.getChildText("event_EVENT_CITY", NS2);
			term4 = node.getChildText("event_EVENT_COUNTY", NS2);
			
			r.setTermsToRelationsFS(r, term1, term2, term3, term4);
	     }
	     
	     else if (name != null && name.equalsIgnoreCase(node.getChildText("person_PR_NAME_GN", NS2)) ) 
	     {
	    	 	Person primaryPerson = new Person();
	    	 	
	    	 	String rel = null;
	    	 	r.appendToXmlForm("<primary>\n");
	    	 	primaryPerson.setNames(r,nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	    	 	r.appendToXmlForm("</primary>\n");
	    	 	
			r.setPrimaries(primaryPerson.getNameFirst(), primaryPerson);

			term1 = node.getChildText("person_PR_PREV_RESIDENCE_CITY", NS2);
			term2 = node.getChildText("person_PR_PREV_RESIDENCE_COUNTY", NS2);
			term3 = node.getChildText("event_EVENT_CITY", NS2);
			term4 = node.getChildText("event_EVENT_COUNTY", NS2);
			
			r.setTermsToRelationsFS(r, term1, term2, term3, term4);
	     	}
   	 	}
    }
    
    /**method to add second person to relations
    * @param r
    * @param node
    * @param NS2
    * @param nameFirst
    * @param nameMid
    * @param nameNick
    * @param birthLast
    * @param nameLast
    * @param suffixValue
    * @param group
    */
    private static void addSecond(Relations r, Element node, Namespace NS2, String nameFirst, String nameMid, String nameNick, 
    		String birthLast, String nameLast, String suffixValue, String [] group, List <String> nameLastList, List <String> birthLastList)
    {
		//in initialize relationships
		String [] relationship = {"HEAD", "SPOUSE", "WIFE", "HUSBAND", "DECEASED", "EX-WIFE", "EX-HUSBAND","PARENT", "MOTHER", "FATHER", "PARENT-IN-LAW", "MOTHER-IN-LAW", "FATHER-IN-LAW", "STEPPARENT", "STEPMOTHER", "STEPFATHER",
				"GRANDPARENT", "GRANDMOTHER", "GRANDFATHER", "GRANDPARENT-IN-LAW", "GRANDMOTHER-IN-LAW", "GRANDFATHER-IN-LAW","STEP-GRANDPARENT",
				"STEP-GRANDMOTHER", "STEP-GRANDFATHER", "GREAT-GRANDPARENT", "GREAT-GRANDMOTHER", "GREAT-GRANDFATHER","AUNT", "UNCLE","SIBLING", "SISTER", "BROTHER", "SIBLING-IN-LAW", "SISTER-IN-LAW", 
				"BROTHER-IN-LAW", "STEPSIBLING", "STEPSISTER", "STEPBROTHER","CHILD","DAUGHTER","SON","CHILD-IN-LAW","DAUGHTER-IN-LAW","SON-IN-LAW","STEPCHILD","STEPDAUGHTER","STEPSON","GRANDCHILD","GRANDDAUGHTER",
				"GRANDSON","GRANDCHILD-IN-LAW","GRANDDAUGHTER-IN-LAW","GRANDSON-IN-LAW","STEP-GRANDCHILD","STEP-GRANDDAUGHTER","STEP-GRANDSON"};

    		for (int a = 0; a < group.length; a++)
    		{
    			if ( node.getChildText("person_REL_TYPE", NS2) != null && node.getChildText("person_REL_TYPE", NS2).equalsIgnoreCase(group[a])) 
    			{
	   			Person secondaryPerson = new Person();
	   			
	   			String rel = node.getChildText("person_REL_TYPE", NS2);
	   			r.appendToXmlForm("<second>\n");
	   			secondaryPerson.setNames(r, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	   			r.appendToXmlForm("</second>\n");
	   			
	   	 		 r.setRelatives(secondaryPerson.getRelationship(), secondaryPerson);
					
				//if primaryPerson only has a nameFirst OR nameMid, take all of nameLast from key relationships & add to nameList
				if(secondaryPerson.getRelationship() != null && 
						(secondaryPerson.getNameLast() != null || secondaryPerson.getBirthLast() != null ))
				{
					//loop through all key relatives and save their nameLast in a List 
					for (int i = 0; i < relationship.length; i++)
					{
						if(secondaryPerson.getRelationship().equalsIgnoreCase(relationship[i]))
						{
							if(secondaryPerson.getNameLast() != null && !nameLastList.contains(secondaryPerson.getNameLast()))
							{
								nameLastList.add(secondaryPerson.getNameLast());
							}
							if(secondaryPerson.getBirthLast() != null && !nameLastList.contains(secondaryPerson.getBirthLast()))
							{
								nameLastList.add(secondaryPerson.getBirthLast());
							}
						}
					}
				} //------------------------ end key relatives loop ------------------------------
	   	 	 }
	   		 
	   		 else if (node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2) != null && 
	   				 node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2).equalsIgnoreCase(group[a])) 
	   		 {
	   			Person secondaryPerson = new Person();
	   			
	   			String rel = node.getChildText("person_PR_RELATIONSHIP_TO_DEC", NS2);
	   			r.appendToXmlForm("<second>\n");
	   			secondaryPerson.setNames(r, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	   			r.appendToXmlForm("</second>\n");
	   			
				r.setRelatives(secondaryPerson.getRelationship(), secondaryPerson);
				 
				//if primaryPerson only has a nameFirst OR nameMid, take all of nameLast from key relationships & add to nameList
				if(secondaryPerson.getRelationship() != null && 
						(secondaryPerson.getNameLast() != null || secondaryPerson.getBirthLast() != null ))
				{
					//loop through all key relatives and save their nameLast in a List 
					for (int i = 0; i < relationship.length; i++)
					{
						if(secondaryPerson.getRelationship().equalsIgnoreCase(relationship[i]))
						{
							if(secondaryPerson.getNameLast() != null && !nameLastList.contains(secondaryPerson.getNameLast()))
							{
								nameLastList.add(secondaryPerson.getNameLast());
							}
							if(secondaryPerson.getBirthLast() != null && !nameLastList.contains(secondaryPerson.getBirthLast()))
							{
								nameLastList.add(secondaryPerson.getBirthLast());
							}
						}
					}
				} //------------------------ end key relatives loop ------------------------------
	   		 }
	   		 else if (node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2) != null && 
	   				 node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2).equalsIgnoreCase(group[a])) 
	   		 {
	   			Person secondaryPerson = new Person();
	   			 
	   			String rel = node.getChildText("person_PR_RELATIONSHIP_TO_HEAD", NS2);
	   			r.appendToXmlForm("<second>\n");
	   			secondaryPerson.setNames(r, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);
	   			r.appendToXmlForm("</second>\n");
	   			
				r.setRelatives(secondaryPerson.getRelationship(), secondaryPerson);
				 
				//if primaryPerson only has a nameFirst OR nameMid, take all of nameLast from key relationships & add to nameList
				if(secondaryPerson.getRelationship() != null && 
						(secondaryPerson.getNameLast() != null || secondaryPerson.getBirthLast() != null ))
				{
					//loop through all key relatives and save their nameLast in a List 
					for (int i = 0; i < relationship.length; i++)
					{
						if(secondaryPerson.getRelationship().equalsIgnoreCase(relationship[i]))
						{
							if(secondaryPerson.getNameLast() != null && !nameLastList.contains(secondaryPerson.getNameLast()))
							{
								nameLastList.add(secondaryPerson.getNameLast());
							}
							if(secondaryPerson.getBirthLast() != null && !nameLastList.contains(secondaryPerson.getBirthLast()))
							{
								nameLastList.add(secondaryPerson.getBirthLast());
							}
						}
					}
				} //------------------------ end key relatives loop ------------------------------
	   		 }
	    	 }
    }
    
    /**method to set nameLastList for nameFirst AND/OR nameMid assumptions ***Person() class?
    * @param relations
    * @param nameLastList
    * @param birthLastList
    */
	public void setNameLastList(Relations r, List <String> nameLastList)
	{	
		String theKey = null;

		Map <String, List<Person>> primaries = r.getPrimaries();
		Iterator<String> itPrimary = primaries.keySet().iterator();
		
		while (itPrimary.hasNext())
		{
			theKey = itPrimary.next();
			
			//iterate through all primary keys
			List<Person> personList = primaries.get(theKey);
			for(Person primaryPerson : personList)
			{
				//primaryPerson has nameFirst AND nameMid -------------------------------------------
				if(primaryPerson.getNameFirst() != null && primaryPerson.getNameMid() != null 
						&& primaryPerson.getBirthLast() == null && primaryPerson.getNameLast() == null)
				{
					primaryPerson.setNameLastList(nameLastList);
				} 
				//if primary person only has nameFist OR only has nameMid
				else if ((primaryPerson.getNameFirst() != null  || primaryPerson.getNameMid() != null) 
						&& primaryPerson.getBirthLast() == null && primaryPerson.getNameLast() == null)
				{
					primaryPerson.setNameLastList(nameLastList);
				}
			}
		}
	}
    
	@Override
	public PermutedResult convert(String inData, String inFormat, Map<String, String> options)
		throws Throwable 
	{
		targetPersonID=options.get("person");
		form=options.get("form");
		return super.convert(inData, inFormat, options);
	}
    
   
}

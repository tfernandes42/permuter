package com.newsbank.permuter.permutation;

import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import com.newsbank.permuter.PermutedResult;
import com.newsbank.permuter.net.GatewaySearch;
import com.newsbank.permuter.net.HttpRequestor;
import com.newsbank.permuter.types.ContainerQuery;
import com.newsbank.permuter.types.Person;
import com.newsbank.permuter.types.Relations;
import com.newsbank.permuter.types.Terms;

public class PhilanPermuter extends AbstractPermutation 
{
	private final static Logger logger = Logger.getLogger(PhilanPermuter.class);
	
	private String gatewayQuery = null;
	private final ContainerQuery containerQuery = new ContainerQuery();
	
	private String hhubValue = null;
	private String highlightTerms = null;
	private String previewTerms = null;
	
	private boolean permutationsBool = false;	//boolean to set High or low permutations
	private boolean secondaryEvidBool = false;	//boolean to see whether search requires secondary evidence
	

	public Relations process(String fileData) 
	{
		Instant start = Instant.now();
		Relations relations = new Relations();
		SAXBuilder builder = new SAXBuilder();

		try {
			Document document = builder.build(new StringReader(fileData));

			Element rootNode = document.getRootElement(); // root element (personSearch)
			removeEmptyElements(rootNode);

			// gateway option trigger
			createGatewayQuery(rootNode);

			// container-query trigger
			if (rootNode.getChild("model") != null) {
				containerQuery.setContainer(rootNode.getChildText("model").trim());
			}
			
			// PreviewTerms - HighlightTerms trigger
			if (rootNode.getChild("previewTerms") != null && rootNode.getChildText("previewTerms").equalsIgnoreCase("primary")) {
				previewTerms = "primary";
			}
						
			// permutations element trigger -- only high permutations will be used
			if (rootNode.getChild("permutations") != null && rootNode.getChildText("permutations").equalsIgnoreCase("HIGH")) {
				permutationsBool = true;
			}
			
			// secondary (evidence) element trigger -- secondary evidence will be needed (second-5 second-6 drops)
			if (rootNode.getChild("secondEvid") != null && rootNode.getChildText("secondEvid").equalsIgnoreCase("REQUIRED")) {
				secondaryEvidBool = true;
			}

			// create primary persons elements list
			@SuppressWarnings("unchecked")
			List<Element> primList = rootNode.getChildren("primary");

			// set primary person(s)
			// ----------------------------------------------------------------------------------
			int num = 0;
			String primKey = null;
			Set<String> primaryKeys = new HashSet<>();
			Person primaryPerson;
			if (primList.isEmpty()) {
				primaryPerson = new Person();

				//If no primary person, create one 
				Element e = rootNode.addContent("primary");
				e.addContent("nameFirst");
//TODO: get an explanation for this, just created an empty nameFirst Element and now asking  for text.
				primaryPerson.setNameFirst(e.getChildText("nameFirst"));
				relations.setPrimaries(e.getChildText("nameFirst"), primaryPerson);

			} else {
				List<String> organs = new ArrayList<>();
				for (Element node : primList) {
					// handleNode(rootNode);
					primaryPerson = new Person();

					initOrgs(node, organs);
					primaryPerson.setOrg(organs);

					String nameFirst = node.getChildText("nameFirst");
					String nameMid = node.getChildText("nameMid");
					String nameNick = node.getChildText("nameNick");
					String birthLast = node.getChildText("birthLast");
					String nameLast = node.getChildText("nameLast");
					String suffixValue = node.getChildText("nameSuf");
					String rel = node.getChildText("relationship");

					primaryPerson.setNames(relations, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);

					//HHUB value
					if(nameFirst != null && nameMid == null && (nameLast != null || birthLast != null))
					{
						//save first and last name
						if(nameLast != null)
						{
							hhubValue = nameFirst +" "+ nameLast;
						}
						else if(birthLast != null)
						{
							hhubValue = nameFirst +" "+ birthLast;
						}
						
					}
					// setup nameAlts
					if (node.getChildText("nameAlts") != null) {
						Element nameAltsNode = node.getChild("nameAlts");
						processNameAlt(relations, primaryPerson, nameAltsNode, primaryKeys);
					}

					// setup nameVar
					if (node.getChildText("nameVar") != null) {

						@SuppressWarnings("unchecked")
						List<Element> nameVars = node.getChildren("nameVar");

						for (Element varNode : nameVars) {
							String nameVar = removePreSuff(varNode.getText());
							fullnameParsing(nameVar, primaryKeys, relations);
						}
					}

					// if primaryPerson is not empty, add to Relations relations
					if (primaryPerson.nameCount() != 0) {
						primKey = "primary" + num++;
						primaryKeys.add(primKey);
						relations.setPrimaries(primKey, primaryPerson);
						// relations = new Relations(primaryPerson);
					}
				}
			} // end primary person(s)
			// -----------------------------------------------------------------------

			// create second persons elements list
			@SuppressWarnings("unchecked")
			List<Element> list = rootNode.getChildren("second");

			// set second person(s)
			// -------------------------------------------------------------
			List<String> nameLastList = new ArrayList<>();
			List<String> organs2 = new ArrayList<>();
			for (Element node : list) {
				Person secondaryPerson = new Person();

				initOrgs(node, organs2);
				secondaryPerson.setOrg(organs2);

				String nameFirst = node.getChildText("nameFirst");
				String nameMid = node.getChildText("nameMid");
				String nameNick = node.getChildText("nameNick");
				String birthLast = node.getChildText("birthLast");
				String nameLast = node.getChildText("nameLast");
				String suffixValue = node.getChildText("nameSuf");
				String rel = node.getChildText("relationship");

				secondaryPerson.setNames(relations, nameFirst, nameMid, nameNick, birthLast, nameLast, suffixValue, rel);

				relations.setRelatives(primKey, secondaryPerson);

				// initialize relationships

				// if primaryPerson only has a nameFirst OR nameMid, take all of nameLast from
				// key relationships & add to nameList
				if (secondaryPerson.getRelationship() != null
						&& (secondaryPerson.getNameLast() != null || secondaryPerson.getBirthLast() != null)) {
					// loop through all key relatives and save their nameLast in a List
					String[] relationship = {"DECEASED", "HEAD", "SPOUSE", "WIFE", "HUSBAND", "EX-WIFE", "EX-HUSBAND",
							"DECEASED SPOUSE", "DIVORCED SPOUSE", "EX-SPOUSE", "FORMER HUSBAND", "FORMER SPOUSE",
							"FORMER WIFE", "SURVIVING SPOUSE", "WIDOW", "WIDOWER", "PARENT", "MOTHER", "FATHER",
							"PARENT-IN-LAW", "MOTHER-IN-LAW", "FATHER-IN-LAW", "STEPPARENT", "STEPMOTHER", "STEPFATHER",
							"GRANDPARENT", "GRANDMOTHER", "GRANDFATHER", "GRANDPARENT-IN-LAW", "GRANDMOTHER-IN-LAW",
							"GRANDFATHER-IN-LAW", "STEP-GRANDPARENT", "STEP-GRANDMOTHER", "STEP-GRANDFATHER",
							"GREAT-GRANDPARENT", "GREAT-GRANDMOTHER", "GREAT-GRANDFATHER", "AUNT", "UNCLE", "SIBLING",
							"SISTER", "BROTHER", "SIBLING-IN-LAW", "SISTER-IN-LAW", "BROTHER-IN-LAW", "STEPSIBLING",
							"STEPSISTER", "STEPBROTHER", "CHILD", "DAUGHTER", "SON", "CHILD-IN-LAW", "DAUGHTER-IN-LAW",
							"SON-IN-LAW", "STEPCHILD", "STEPDAUGHTER", "STEPSON", "GRANDCHILD", "GRANDDAUGHTER", "GRANDSON",
							"GRANDCHILD-IN-LAW", "GRANDDAUGHTER-IN-LAW", "GRANDSON-IN-LAW", "STEP-GRANDCHILD",
							"STEP-GRANDDAUGHTER", "STEP-GRANDSON"};
					for (String s : relationship) {
						if (secondaryPerson.getRelationship().equalsIgnoreCase(s)) {
							if (secondaryPerson.getNameLast() != null
									&& !nameLastList.contains(secondaryPerson.getNameLast())) {
								nameLastList.add(secondaryPerson.getNameLast());
							}
							if (secondaryPerson.getBirthLast() != null
									&& !nameLastList.contains(secondaryPerson.getBirthLast())) {
								nameLastList.add(secondaryPerson.getBirthLast());
							}
						}
					}
				}

			} // end second person(s)
			// -------------------------------------------------------------

			// for nameFirst/nameMid assumptions
			setNameLastList(relations, nameLastList);

			//trigger married name permutations --------------------------------------------------------------------------------
			//~~~~~~~a~~~~~~~~~~~~
			Iterator<String> primKeys = primaryKeys.iterator();
			boolean primBool = false;
			
			while (primKeys.hasNext())
			{
				String pKey = primKeys.next();
				List<Person> prims = relations.getPrimaries(pKey);
			
				for (int i = 0; i < prims.size(); i++) 
				{
					Person primPerson = prims.get(i);
					
					//if primary has a birthLast but not a nameLast 
					if ( primPerson.getBirthLast() != null && primPerson.getNameLast() == null )
					{
						//for loop through secondaries
						List<Person> relList = relations.getRelatives(primKey);
						for (Person rel : relList)
						{
							//see if any relatives are spouses and have a name last 
							if (rel.getNameLast()!=null && rel.getRelationship()!=null && ( rel.getRelationship().toUpperCase().contains("SPOUSE") || rel.getRelationship().toUpperCase().contains("WIFE") ||
									rel.getRelationship().toUpperCase().contains("HUSBAND") || rel.getRelationship().toUpperCase().contains("EX-WIFE") ||
									rel.getRelationship().toUpperCase().contains("EX-HUSBAND") || rel.getRelationship().toUpperCase().contains("FIANCE")||
									rel.getRelationship().toUpperCase().contains("DOMESTIC PARTNER") ) )
							{
								//grab spouses' last name and add to primary person nameLast if there is only one spouse
								if (relList != null && relList.size() == 1)
								{
									primPerson.setNameLast(rel.getNameLast());
								}
								//if there are more than one spouses create new primary person
								else 
								{
									if (rel != null && !rel.getNameLast().equalsIgnoreCase(primPerson.getBirthLast())) 
									{
										Person person = new Person();
										if (primPerson.getNameFirst()!=null)
										{
											person.setNameFirst(primPerson.getNameFirst());
										}
										if (primPerson.getNameMid()!=null)
										{
											person.setNameMid(primPerson.getNameMid());
										}
										if (primPerson.getNameNick()!=null)
										{
											person.setNameNick(primPerson.getNameNick());
										}
										if (primPerson.getNameSuf()!=null)
										{
											person.setNameSuf(primPerson.getNameSuf());
										}
										person.setBirthLast(primPerson.getBirthLast());
										person.setNameLast(rel.getNameLast());	
										
										if (primBool == true)
										{
											primKey = primKey + num++;
										}
										//add new person to r
										relations.setPrimaries(primKey, person);
										
										primBool = true;
									}
								}
							}
						}	
					}
					//~~~~~~~b~~~~~~~~~~~~
					//for loop through secondaries
					if( primPerson.getNameLast() != null )
					{
						List<Person> relList = relations.getRelatives(primKey);
						if (relList != null)
						{
							for(Person rel : relList)
							{
								//see if any relatives are spouses and have a birthLast that is not the same at primary nameLast 
							 	if (rel.getRelationship()!=null && rel.getNameLast()==null && rel.getBirthLast()!=null && !primPerson.getNameLast().equalsIgnoreCase(rel.getBirthLast())
										&& ( rel.getRelationship().toUpperCase().contains("SPOUSE") || rel.getRelationship().toUpperCase().contains("WIFE") ||
										rel.getRelationship().toUpperCase().contains("HUSBAND") || rel.getRelationship().toUpperCase().contains("EX-WIFE") ||
										rel.getRelationship().toUpperCase().contains("EX-HUSBAND") || rel.getRelationship().toUpperCase().contains("FIANCE")||
										rel.getRelationship().toUpperCase().contains("DOMESTIC PARTNER") ) )
								{
									rel.setNameLast(primPerson.getNameLast());
								}
							}
						}
					}
				}
			}	//end married name permutations --------------------------------------------------------------------------------
			
			// get terms and set them to terms ArrayList
			List<?> termsList = rootNode.getChildren("terms");
			String xmlTerm = "term";
			String termKey = "all";
			relations.setTermsToRelations(xmlTerm, termKey, relations, termsList);

			// getNotterms and add them to notterms ArrayList
			List<?> nottermsList = rootNode.getChildren("notterms");
			termKey = "allnot";
			xmlTerm = "notterm";
			relations.setTermsToRelations(xmlTerm, termKey, relations, nottermsList);
		} catch (Throwable io) {
			logger.error(io);
		}
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
//		logger.debug("elapsed time: {}", timeElapsed);
		logger.debug("elapsed time: "+ timeElapsed);
		return relations;
	}

	private  void createGatewayQuery(Element rootNode) {
		if (rootNode.getChild("gateway") != null) {
			StringBuilder sb = new StringBuilder(rootNode.getChildText("gateway").trim());
//			gatewayQuery = rootNode.getChildText("gateway").trim();
			if (rootNode.getChild("scorelimit") != null) {
				sb.append("&scorelimit=").append(Integer.parseInt(rootNode.getChildText("scorelimit")));
//				gatewayQuery = gatewayQuery + "&scorelimit=" + Integer.parseInt(rootNode.getChildText("scorelimit"));
			}
			if (rootNode.getChild("option") != null) {
				sb.append("&option=").append(rootNode.getChildText("option"));
//				gatewayQuery = gatewayQuery + "&option=" + rootNode.getChildText("option");
			}
			gatewayQuery = sb.toString();
		}
	}

	public String permute(Relations r) {
		Instant start = Instant.now();

//	TODO: never used so remarking out
//	    String nameNick;
		//		String fNameNick = null;
		// Gender gender = null;

		// set extra query if form contains gateway element
		StringBuilder trip = new StringBuilder();
		if (gatewayQuery != null) {
			trip.append(gatewayQuery);
		}

		// ===================================================
		// start building primary triplet
		// ===================================================
		trip.append("&p_field_primary-0=alltext&p_params_primary-0=weight:1&p_text_primary-0=(");

		// for loop to iterate through all the primary relatives in r
		Map<String, List<Person>> primariesMap = r.getPrimaries();

		// initialize variables
		Collection<List<Person>> personListCollection = primariesMap.values();
		List<String> fNameLastList = new ArrayList<>();
		List<String> org = new ArrayList<>();
		StringBuilder suff = new StringBuilder();
		StringBuilder init = new StringBuilder();
		// for coupleRefs
		boolean first = false;
		String sBlast = null;
		String sMid = null;
		char birthLastInitial = 0;
		char nameMidInitial = 0;
		char nameFirstInitial = 0;
		String fNameLast = null;
		String fBirthLast = null;
		String fNameFirst = null;
		String nameLast = null;
		String birthLast = null;
		String nameMid;
		String nameFirst = null;
		for (List<Person> persons : personListCollection) {
			for(Person primary : persons){
				org = primary.getOrg();
				nameFirst = primary.getNameFirst();
				nameMid = primary.getNameMid();
//				nameNick = primary.getNameNick();
				birthLast = primary.getBirthLast();
				nameLast = primary.getNameLast();
				String nameSuf = primary.getNameSuf();
				// gender = primary.getGender();

				// retain nameSuff info and store it in suff StringBuilder for later use
				if (nameSuf != null) {
					// remove end periods if any
					nameSuf = removeEndPer(nameSuf);

					processNameLast(nameLast, nameSuf, suff);
					processNameLast(birthLast, nameSuf, suff);
					if (nameLast == null && birthLast == null) {
						suff.append("\"").append(nameSuf).append("\"");

						// if nameSuf contains a '.'
						if (nameSuf.contains(".")) {
							suff.append("\"").append(nameSuf.replace(".", "")).append("\"");
						}
					}
				}

				// get initials of useful names
				if (StringUtils.isNotEmpty(nameFirst)) {
					// take out any quotation
					if (nameFirst.contains("\"")) {
						nameFirst = nameFirst.replace("\"", "");
					}
					nameFirst = removeEndPer(nameFirst);
					if (StringUtils.isNotEmpty(nameFirst)) {
						nameFirstInitial = nameFirst.charAt(0);
					}
				}
				if (StringUtils.isNotEmpty(nameMid)) {
					// take out any quotation
					if (nameMid.contains("\"")) {
						nameMid = nameMid.replace("\"", "");
					}
					nameMid = removeEndPer(nameMid);
					if (StringUtils.isNotEmpty(nameMid)) {
						nameMidInitial = nameMid.charAt(0);
						sMid = Character.toString(nameMidInitial);
					}
				}
				if (StringUtils.isNotEmpty(birthLast)) {
					// take out any quotation
					if (birthLast.contains("\"")) {
						birthLast = birthLast.replace("\"", "");
					}
					birthLast = removeEndPer(birthLast);
					if (StringUtils.isNotEmpty(birthLast)) {
						birthLastInitial = birthLast.charAt(0);
						sBlast = Character.toString(birthLastInitial);
					}
				}

				// take out any quotation
				if (nameLast != null && nameLast.contains("\"")) {
					nameLast = nameLast.replace("\"", "");
				}
				nameLast = removeEndPer(nameLast);
//TODO: note that nameNick was never used
				// take out any quotation
//				if (nameNick != null && nameNick.contains("\"")) {
//					nameNick = nameNick.replace("\"", "");
//				}
//				nameNick = removeEndPer(nameNick);

				// if nameMid has more than one parts
				String nameMids = null;
				if (nameMid != null && nameMid.contains(" ")) {
					String[] nm = nameMid.split(" ");
					StringBuilder nm2 = new StringBuilder();

					for (String value : nm) {
						nm2.append(value.charAt(0)).append(" ");
					}
					nameMids = nm2.toString();
				}

				// set up for coupleRefs
				if (!first) {
					fNameFirst = nameFirst;
					fBirthLast = birthLast;
					fNameLast = nameLast;
				}

				// set fNameLastList with primary nameLast & birthLast
				if (nameLast != null) {
					fNameLastList.add(nameLast);
				}
				if (birthLast != null) {
					fNameLastList.add(birthLast);
				}

				// boolean to state weather first or not first primary person (first = true once
				// first primary has passed)
				first = true;

				// call and append primary person permutations to trip
				trip.append(primary.primPermute(init, nameFirstInitial, nameMidInitial, birthLastInitial, nameMids,
						sMid, sBlast, fNameLastList, permutationsBool));
				}
		}

		trip.append(")");
		String str1 = trip.substring(trip.indexOf("(") + 1, trip.indexOf(")"));
		str1 = deleteDup(str1, nameFirst, nameLast, birthLast);

		// primary-1 triplet
		if (!init.toString().isEmpty()) {
			trip.append("&p_bool_primary-1=OR&p_field_primary-1=alltext&p_params_primary-1=weight:0&p_text_primary-1=(").append(init).append(")");
		}

		// ===================================================
		// start building PRIMARY couple ref triplet
		// ===================================================

		// boolean to make sure only first spouses' info gets retained

		trip.append("&p_bool_primary-2=OR&p_field_primary-2=alltext&p_params_primary-2=weight:1&p_text_primary-2=(");
		StringBuilder onear2 = new StringBuilder();
		List<String> spouses = new ArrayList<>();
		String theKey;
		if (!r.getRelatives().isEmpty()) {
			// for loop to make this iterate through all the relatives in r
			Map<String, List<Person>> relatives = r.getRelatives();
			boolean firstSpouse = true;
			String[] all = {"WIFE", "HUSBAND", "SPOUSE", "EX-WIFE", "EX-HUSBAND", "FIANCE", "DOMESTIC PARTNER"};
			String midInit = null;
			for (String theKey2 : relatives.keySet()) {
				List<Person> personList = relatives.get(theKey2);
				for (Person relative : personList) {
					for (String value : all) {
						if (relative.getRelationship() != null) {
							if (relative.getRelationship().equalsIgnoreCase(value)) {
								String mid = relative.getNameMid();
								if (mid != null) { // take out any quotation
									if (mid.contains("\"")) {
										mid = mid.replace("\"", "");
									}
									mid = removeEndPer(mid);
									char midInit0 = relative.getNameMid().charAt(0);
									midInit = Character.toString(midInit0);
								}

								String firstN = relative.getNameFirst();
								// take out any quotation
								if (firstN != null && firstN.contains("\"")) {
									firstN = firstN.replace("\"", "");
								}
								firstN = removeEndPer(firstN);

								// save first spouses first & mid name (if available) to construct second
								// coupleRefs
								if (firstSpouse && mid != null && relative.getNameLast() != null) {
									spouses.add(firstN + " " + mid + " " + relative.getNameLast());
								} else if (firstSpouse && mid == null && relative.getNameLast() != null) {
									spouses.add(firstN + " " + relative.getNameLast());
								} else if (firstSpouse) {
									spouses.add(firstN);
								}

								// set to false so only the first spouses' info gets used
								firstSpouse = false;

								// loop through primaries here
								for (String s : primariesMap.keySet()) {
									theKey = s;
									// kLogger.debug("Relatives:" + theKey);

									personList = primariesMap.get(theKey);
									for (Person primary : personList) {
										nameFirst = primary.getNameFirst();
										nameMid = primary.getNameMid();
//										nameNick = primary.getNameNick();
										birthLast = primary.getBirthLast();
										nameLast = primary.getNameLast();

										// start forming coupleRefs
										if ( /* coupleRef == true && */ nameFirst != null && firstN != null
												&& nameLast != null
												&& nameLast.equalsIgnoreCase(relative.getNameLast())) {
											if (nameFirst.length() > 1) {
												trip.append("\"").append(nameFirst).append(" ").append(firstN).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" and ").append(firstN).append(" ").append(nameLast).append("\"");
												// primary-3
												if (nameMid == null && mid == null) {
													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3and ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
												}
												if (nameMid == null) {
													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3").append(firstN).append(" ").append(nameLast).append("q0t3");
													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3and ").append(firstN).append(" ").append(nameLast).append("q0t3");
												}

												if (mid == null) {
													onear2.append("q0t3").append(nameFirst).append(" ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
													onear2.append("q0t3").append(nameFirst).append(" and ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
												}
											}

											if (nameMid != null) {
												// make sure nameMid is not just an initial
												if (nameMid.length() > 1) {
													trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" ").append(firstN).append(" ").append(nameLast).append("\"");
													trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" and ").append(firstN).append(" ").append(nameLast).append("\"");
												}

												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(firstN).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" and ").append(firstN).append(" ").append(nameLast).append("\"");

												if (mid == null) {
													// make sure nameMid is not just an initial
													if (nameMid.length() > 1) {
														onear2.append("q0t3").append(nameFirst).append(" ").append(nameMid).append(" ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
														onear2.append("q0t3").append(nameFirst).append(" ").append(nameMid).append(" and ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
													}

													onear2.append("q0t3").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
													onear2.append("q0t3").append(nameFirst).append(" ").append(nameMidInitial).append(" and ").append(firstN).append("q0t3 ONEAR/2 q0t3").append(nameLast).append("q0t3");
												}
											}

											if (mid != null && nameFirst.length() > 1) {
												// make sure mid is not just an initial
												if (mid.length() > 1) {
													trip.append("\"").append(nameFirst).append(" ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
													trip.append("\"").append(nameFirst).append(" and ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
												}

												trip.append("\"").append(nameFirst).append(" ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" and ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");
												// primary-3
												if (nameMid == null) {
													// make sure mid is not just an initial
													if (mid.length() > 1) {
														onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("q0t3");
														onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3and ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("q0t3");
													}

													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("q0t3");
													onear2.append("q0t3").append(nameFirst).append("q0t3 ONEAR/2 q0t3and ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("q0t3");
												}
											}

											if (mid != null && nameMid != null) {
												// make sure nameMid && mid are not just initials
												if (mid.length() > 1 && nameMid.length() > 1) {
													trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
													trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" and ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
												}

												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" and ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");

												trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" ").append(nameMid).append(" and ").append(firstN).append(" ").append(midInit).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
												trip.append("\"").append(nameFirst).append(" ").append(nameMidInitial).append(" and ").append(firstN).append(" ").append(mid).append(" ").append(nameLast).append("\"");
											}
										}
										// finish looping through primaries
									}
								}
							}
						}
					}
				}
			}
		}
		trip.append(")");
		// add to second-7
		String prim2 = deleteDup(trip.substring(trip.indexOf("primary-2=(") + 11, trip.length() - 1),
				nameFirst, nameLast, birthLast);

		// remove primary-2 is triplet is empty
		if (trip.toString().contains(
				"&p_bool_primary-2=OR&p_field_primary-2=alltext&p_params_primary-2=weight:1&p_text_primary-2=()")) {
			trip.delete(trip.length() - 94, trip.length());
		}

		// primary-3 triplet
		if (!onear2.toString().isEmpty()) {
			trip.append("&p_bool_primary-3=OR&p_field_primary-3=alltext&p_params_primary-3=weight:0&p_text_primary-3=(").append(onear2).append(")");
		}

		// primary-4 triplet (org)
		String prim4 = null;
		if (org != null && !org.isEmpty()) {
			trip.append(
					"&p_bool_primary-4=OR&p_field_primary-4=alltext&p_params_primary-4=weight:1&p_text_primary-4=(");
			for (String o : org) {
				o = orgPermuter(o);
				trip.append(o);
			}
			trip.append(")");

			// add to second-10 -- maybe delete because already taken care of in orgPermuter
			// method
			prim4 = deleteDup(trip.substring(trip.indexOf("primary-4=(") + 11, trip.length() - 1), nameFirst,
					nameLast, birthLast);
		}

		// ===================================================
		// start building relatives triplet
		// ===================================================
		trip.append("&p_field_second-0=alltext&p_params_second-0=weight:1&p_text_second-0=(");

		// for loop to make this iterate through all the relatives in r
		Map<String, List<Person>> relatives = r.getRelatives();
		List<String> orgs = new ArrayList<>();
		for (String theKey2 : relatives.keySet()) {
			// kLogger.debug("Relatives:" + theKey);

			List<Person> personList = relatives.get(theKey2);
			for (Person relative : personList) {
				orgs = relative.getOrg();
				String relNameFirst = relative.getNameFirst();
				String relNameMid = relative.getNameMid();
//				String relNameNick = relative.getNameNick();
//				String relNameLast = relative.getNameLast();
				String relBirthLast = relative.getBirthLast();
				// String relationship = relative.getRelationship();
				// Gender relGender = relative.getGender();

				// set initials & fix necessary names
				// ==========================================================
				char relNameFirstInitial = 0;
				if (relNameFirst != null) {
					// take out any quotation
					if (relNameFirst.contains("\"")) {
						relNameFirst = relNameFirst.replace("\"", "");
					}
					relNameFirst = removeEndPer(relNameFirst);
					relNameFirstInitial = relNameFirst.charAt(0);
				}
				String sRelMid = null;
				char relNameMidInitial = 0;
				if (relNameMid != null) {
					// take out any quotation
					if (relNameMid.contains("\"")) {
						relNameMid = relNameMid.replace("\"", "");
					}
					relNameMid = removeEndPer(relNameMid);
					relNameMidInitial = relNameMid.charAt(0);
					sRelMid = Character.toString(relNameMidInitial);
				}
				String sRelBlast = null;
				char relBirthLastInitial = 0;
				if (relBirthLast != null) {
					// take out any quotation
					if (relBirthLast.contains("\"")) {
						relBirthLast = relBirthLast.replace("\"", "");
					}
					relBirthLast = removeEndPer(relBirthLast);
					relBirthLastInitial = relBirthLast.charAt(0);
					sRelBlast = Character.toString(relBirthLastInitial);
				}
				// take out any quotation
//				if (relNameLast != null && relNameLast.contains("\"")) {
//					relNameLast = relNameLast.replace("\"", "");
//				}
//				relNameLast = removeEndPer(relNameLast);
				// take out any quotation
//				if (relNameNick != null && relNameNick.contains("\"")) {
//					relNameNick = relNameNick.replace("\"", "");
//				}
//				relNameNick = removeEndPer(relNameNick);
				// ==========================================================

				// if nameMid has more than one parts
				String relNameMids = null;
				if (relNameMid != null && relNameMid.contains(" ")) {
					String[] nm = relNameMid.split(" ");
					StringBuilder nm2 = new StringBuilder();

					for (String s : nm) {
						nm2.append(s.charAt(0)).append(" ");
					}
					relNameMids = nm2.toString();
				}

				// call and append second persons permutations to trip
				trip.append(relative.secPermute(relNameFirstInitial, relNameMidInitial, relBirthLastInitial, fNameFirst,
						fBirthLast, fNameLast, relNameMids, sRelMid, sRelBlast, fNameLastList));
			}
		}

		trip.append(")");

		// remove second-0 triplet if empty
		if (trip.toString().contains("&p_field_second-0=alltext&p_params_second-0=weight:1&p_text_second-0=()")) {
			trip.delete(trip.length() - 71, trip.length());
		}

		// ===================================================
		// start building sec coupleRef triplet
		// ===================================================
		// populate
		trip.append("&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=(");

		// check if there is anything in spouses
		String sLn = null;
		String sMn = null;
		String sFn = null;
		String sMnInit = null;
		for (String value : spouses) {
			if (value != null) {
				// if(coupleRef == true){
				String spouse = value.trim();
				if (spouse.contains(" ")) {
					// if spouse has any double space
					if (spouse.contains("  ")) {
						spouse = spouse.replace("  ", " ");
					}
					String[] split = spouse.split(" ");

					// if spouse has first, mid, last
					if (split.length == 3) {
						sFn = split[0];
						sMn = split[1];
						sLn = split[2];
					}

					// if spouse has first & last
					if (split.length == 2) {
						sFn = split[0];
						sLn = split[1];
					}
				} else
					sFn = spouse;

				// take out any quotation
				if (sFn != null && sFn.contains("\"")) {
					sFn = sFn.replace("\"", "");
				}
				sFn = removeEndPer(sFn);

				if (sMn != null) {
					// take out any quotation
					if (sMn.contains("\"")) {
						sMn = sMn.replace("\"", "");
					}
					sMn = removeEndPer(sMn);
					char sMnInit0 = sMn.charAt(0);
					sMnInit = Character.toString(sMnInit0);
				}

				// loop through primaries here
				for (String s : primariesMap.keySet()) {
					theKey = s;
					// kLogger.debug("Relatives:" + theKey);

					List<Person> personList = primariesMap.get(theKey);
					for (Person primary : personList) {
						nameFirst = primary.getNameFirst();
						nameMid = primary.getNameMid();
//						nameNick = primary.getNameNick();
						birthLast = primary.getBirthLast();
						nameLast = primary.getNameLast();

						if (nameFirst != null && sFn != null && nameLast != null && nameLast.equalsIgnoreCase(sLn)) {
							if (sFn.length() > 1) {
								trip.append("\"").append(sFn).append(" ").append(nameFirst).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" and ").append(nameFirst).append(" ").append(nameLast).append("\"");

								if (nameMid != null) {
									// make sure nameMid is not just an initial
									if (nameMid.length() > 1) {
										trip.append("\"").append(sFn).append(" ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
										trip.append("\"").append(sFn).append(" and ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
									}

									trip.append("\"").append(sFn).append(" ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");
									trip.append("\"").append(sFn).append(" and ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");
								}
							}

							if (sMn != null) {
								// make sure sMn is not just an initial
								if (sMn.length() > 1) {
									trip.append("\"").append(sFn).append(" ").append(sMn).append(" ").append(nameFirst).append(" ").append(nameLast).append("\"");
									trip.append("\"").append(sFn).append(" ").append(sMn).append(" and ").append(nameFirst).append(" ").append(nameLast).append("\"");
								}

								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" ").append(nameFirst).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" and ").append(nameFirst).append(" ").append(nameLast).append("\"");
							}
							if (sMn != null && nameMid != null) {
								// make sure sMn && nameMid are not just initials
								if (sMn.length() > 1 && nameMid.length() > 1) {
									trip.append("\"").append(sFn).append(" ").append(sMn).append(" ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
									trip.append("\"").append(sFn).append(" ").append(sMn).append(" and ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
								}

								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" and ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");

								trip.append("\"").append(sFn).append(" ").append(sMn).append(" ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" ").append(sMn).append(" and ").append(nameFirst).append(" ").append(nameMidInitial).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
								trip.append("\"").append(sFn).append(" ").append(sMnInit).append(" and ").append(nameFirst).append(" ").append(nameMid).append(" ").append(nameLast).append("\"");
							}
						}
					}
				}
				sMn = null;
				// }
			}
		}
		trip.append(")");

		// remove second-2 if triplet is empty
		if (trip.toString().contains(
				"&p_bool_second-2=OR&p_field_second-2=alltext&p_params_second-2=weight:1&p_text_second-2=()")) {
			trip.delete(trip.length() - 90, trip.length());
		}

		// ===================================================
		// start building terms triplet
		// ===================================================
		List<Terms> termsList = r.getTerms("all");

		if (termsList != null && termsList.get(0).countTerms() != 0) {
			trip.append("&p_bool_second-4=OR&p_field_second-4=alltext&p_params_second-4=weight:1&p_text_second-4=(");
			addTerms(termsList, trip);
			trip.append(")");
		}

		//second-5 logic
		trip.append("&p_bool_second-5=OR&p_field_second-5=alltext&p_params_second-5=weight:0&p_text_second-5=(");

		// delete all duplicate phrase in triplets above
		StringBuilder t = new StringBuilder();
		t.append(deleteDup(trip.toString(), nameFirst, nameLast, birthLast));

		t.append(str1).append(")");

		if (t.toString().contains(
				"&p_bool_second-5=OR&p_field_second-5=alltext&p_params_second-5=weight:0&p_text_second-5=()")) {
			t.delete(t.length() - 90, t.length());
		}
		
		//if secondaryEvidBool == true remove second-5
		StringBuilder ttemp = new StringBuilder();
		if(t.toString().contains("&p_bool_second-5=") && secondaryEvidBool == true)
		{
			String prim5 = t.toString().substring(t.toString().indexOf("&p_bool_second-5="), t.toString().indexOf(")", t.toString().indexOf("&p_bool_second-5="))) + ")";
			ttemp.append(t.toString().replace(prim5,""));
			t = new StringBuilder();
			t.append(ttemp);
		}
		
		if (!init.toString().isEmpty() && secondaryEvidBool != true) {
			String initFinal = deleteDup(init.toString(), nameFirst, nameLast, birthLast);
			t.append("&p_bool_second-6=OR&p_field_second-6=alltext&p_params_second-6=weight:0&p_text_second-6=(").append(initFinal).append(")");
		}

		// triplet second-7
		if (prim2 != null && !prim2.equals("")) {
			t.append("&p_bool_second-7=OR&p_field_second-7=alltext&p_params_second-7=weight:0&p_text_second-7=(").append(prim2).append(")");
		}

		// triplet second-8
		if (!onear2.toString().isEmpty()) {
			t.append("&p_bool_second-8=OR&p_field_second-8=alltext&p_params_second-8=weight:0&p_text_second-8=(").append(onear2).append(")");
		}

		// triplet second-9
		if (suff.length() > 0 || (orgs != null && !orgs.isEmpty())) {
			
			if(containerQuery.isObit())
			{
				t.append("&p_bool_second-9=OR&p_field_second-9=dece&p_params_second-9=weight:1&p_text_second-9=(");
			}
			else
				t.append("&p_bool_second-9=OR&p_field_second-9=alltext&p_params_second-9=weight:1&p_text_second-9=(");
			
			for (String o : orgs) {
				t.append("\"").append(o).append("\"");
			}

			// add nameSuf permutations if available
			t.append(deleteDup(suff.toString(), nameFirst, nameLast, birthLast));
			
			t.append(")");
		}

		// second-10
		if (prim4 != null && !prim4.equals("")) {
			t.append("&p_bool_second-10=OR&p_field_second-10=alltext&p_params_second-10=weight:0&p_text_second-10=(").append(prim4).append(")");
		}
		
		//hhub value second-11
		if (containerQuery.isObit() && hhubValue != null)
		{
			t.append("&p_bool_second-11=OR&p_field_second-11=deceVal&p_params_second-11=weight:1&p_text_second-11=(\"");
			t.append(hhubValue+ "\")");	
		}

		// -------------------------- add notterms --------------------------
		List<Terms> termsList2 = r.getTerms("allnot");
		if (termsList2 != null) 
		{
			t.append("&p_bool_notterms-0=NOT&p_field_notterms-0=alltext&p_text_notterms-0=(");
			addTerms(termsList2, t);
			t.append(")");
		} // ---------------------may not be needed--------------------------

		String tee = t.toString().replaceAll("q0t3", "\"");
		t = new StringBuilder();
		t.append(tee);

		// add OR between triplet phrases
		StringBuilder t2 = new StringBuilder();
		t2.append(addOr(t));

		// remove and '&' left in triplet phrases with a space
		String tpl = replaceAmp(t2);

		// replace any double spaces
		if (tpl.contains("  ")) {
			tpl = tpl.replace("  ", " ");
		}

		// containerQuery logic
		tpl = containerQuery.process(tpl);
		
		//remove person:1:obit section if <permutations>high</permutations> or <secondEvid>required</secondEvid>
		/*if (secondaryEvidBool == true)
		{
			if(tpl.contains("&p_container_second-105=person:1:obit"))
			{
				tpl = tpl.replace("&p_container_second-105=person:1:obit", "");
			}
			if(tpl.contains("&p_container_second-106=person:1:obit"))
			{
				tpl = tpl.replace("&p_container_second-106=person:1:obit", "");
			}
		}
		if (permutationsBool == true && tpl.contains("&p_container_primary-101=person:1:obit"))
		{
			tpl = tpl.replace("&p_container_primary-101=person:1:obit", "");
		}*/
		//use only first name in second-111
		if(tpl.contains("&p_text_second-111=("))
		{
			String [] hhubArray = hhubValue.split(" ");
			tpl = tpl.replace("&p_text_second-111=(\"" + hhubValue, "&p_text_second-111=(\"" + hhubArray[0]);
		}
		//if secondEvid = REQUIRED but no secondary evidence; manipulate so out is 0
		if(secondaryEvidBool == true && !tpl.contains("second"))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(tpl+ "&p_field_second-0=unq&p_params_second-0=weight:0&p_text_second-0=(NOSECONDARYEVIDENCE)");
			tpl = sb.toString();
		}
		
		//save all info from primary triplets into highlightTerms if <previewTerms>primary</previewTerms>
		if(previewTerms != null && previewTerms.equalsIgnoreCase("primary"))
		{
			StringBuilder highlightBuilder = new StringBuilder();
			if(tpl.contains("primary-0=("))
			{
				String primary0 = tpl.substring(tpl.indexOf("primary-0=(") +11, tpl.indexOf(")", tpl.indexOf("primary-0=(")));
				highlightBuilder.append(primary0);
			}
			/*if(tpl.contains("primary-1=(")) // 01/26/19 commented out, low permutations noisy
			{
				String primary1 = tpl.substring(tpl.indexOf("primary-1=(") +11, tpl.indexOf(")", tpl.indexOf("primary-1=(")));
				highlightBuilder.append(" OR " + primary1);
			}*/
			if(tpl.contains("primary-2=("))
			{
				String primary2 = tpl.substring(tpl.indexOf("primary-2=(") +11, tpl.indexOf(")", tpl.indexOf("primary-2=(")));
				highlightBuilder.append(" OR " + primary2);
			}
			if(tpl.contains("primary-4=("))
			{
				String primary4 = tpl.substring(tpl.indexOf("primary-4=(") +11, tpl.indexOf(")", tpl.indexOf("primary-4=(")));
				//might work
				if(highlightBuilder != null && highlightBuilder.length() > 0)
				{
					highlightBuilder.append(" OR ");
				}
				highlightBuilder.append(primary4);
			}
	
			if (highlightBuilder != null && highlightBuilder.length() > 0)
			{
				highlightTerms = highlightBuilder.toString().replace("\"", "");
				highlightTerms = highlightTerms.replace(" OR ", ";,;");
				
				//if highlightTerms has an onear/2 term-----------------
				if(highlightTerms.contains("ONEAR/2"))
				{
					StringBuilder sb = new StringBuilder();
					String [] terms = highlightTerms.split(";,;");
					//termsArray = new HashSet<String>(Arrays.asList(termsArray)).toArray(new String[0]); //delete dupes
					
					for (String term : terms)
					{
						//do not use onear/2 terms
						if(!term.contains("ONEAR/2"))
						{
							sb.append(term + ";,;");
						}
					}
					sb.delete(sb.length()-3, sb.length()); //delete ending ";,;"
					highlightTerms = sb.toString();

				} //-----------------------------------------------------
				
				highlightTerms = "&highlightTerms=" + highlightTerms;
			}
		}
		
		Instant end = Instant.now();
		long timeElapsed = Duration.between(start, end).toMillis();
		logger.debug("elapsed time: "+ timeElapsed);
		return tpl;
	}

	private static void processNameLast(String nameLast, String nameSuf, StringBuilder suff) {
		if (nameLast != null) {
			suff.append("\"").append(nameLast).append(" ").append(nameSuf).append("\"");

			if (nameSuf.contains(".")) {
				suff.append("\"").append(nameLast).append(" ").append(nameSuf.replace(".", "")).append("\"");
			}
		}
	}

	/**
	 * method to set nameLastList for nameFirst AND/OR nameMid assumptions
	 * ***Person() class?
	 *
	 * @param r relations
	 * @param nameLastList list of last names
	 */
	private static void setNameLastList(Relations r, List<String> nameLastList) {
		r.getPrimaries().values().forEach(personList -> personList.forEach(primaryPerson -> {
			// primaryPerson has nameFirst AND nameMid
			// -------------------------------------------
			if (primaryPerson.getNameFirst() != null && primaryPerson.getNameMid() != null
					&& primaryPerson.getBirthLast() == null && primaryPerson.getNameLast() == null) {
				primaryPerson.setNameLastList(nameLastList);
			}
			// if primary person only has nameFirst OR only has nameMid
			else if ((primaryPerson.getNameFirst() != null || primaryPerson.getNameMid() != null)
					&& primaryPerson.getBirthLast() == null && primaryPerson.getNameLast() == null) {
				primaryPerson.setNameLastList(nameLastList);
			}
		}));
	}

	/**
	 * method to process organization permutations ***Person() class?
	 *
	 * @param org organization
	 * @return a possibly modified organization string
	 */
	private static String orgPermuter(String org) {

		// If there is more than one word: remove stopwords from beginning
		if (org.contains(" ")) {
			String[] stopwords = {"THE", "AND", "AT", "BY", "FOR", "OF", "ON", "TO", "DO", "LA", "DE"};
			for (String stopword : stopwords) {
				String[] orgArr = org.split(" ");
				if (orgArr[0].equalsIgnoreCase(stopword)) {
					orgArr = Arrays.copyOfRange(orgArr, 1, orgArr.length);
				}
				// make string array back into a string
				org = arrayToString(orgArr);
			}
		}

		// If there is still more than one word: remove suffix words from end
		// (recursive)
		if (org.contains(" ")) {
			String[] suffwords = {"GMBH", "AG", "CORP", "PLC", "S.A", "SA", "LTD", "AB", "INC", "ASA", "SA", "S.P.A",
					"SPA", "N.V", "NV", "AB", "L.P", "LP", "DIV", "COOP", "L.L.C", "LLC", "KG", "LTDA", "S.A.S", "SAS"};
			for (String suffword : suffwords) {
				String[] orgArr = org.split(" ");
				if (orgArr.length > 0 && orgArr[orgArr.length - 1].equalsIgnoreCase(suffword)) {
					orgArr = Arrays.copyOfRange(orgArr, 0, orgArr.length - 1);
					// added for recursion
					for (String s : suffwords) {
						if (orgArr[orgArr.length - 1].equalsIgnoreCase(s)) {
							orgArr = Arrays.copyOfRange(orgArr, 0, orgArr.length - 1);
						}
					}
				}
				org = arrayToString(orgArr);
			}
		}

		return "\"" + org + "\"";
	}

	/**
	 * method to initialize organizations ***Person() class?
	 *
	 * @param node
	 * @param organs
	 */
	private static void initOrgs(Element node, List<String> organs) {
		/*
		 * for (int i = 0; i < list.size(); i++) { //handleNode(rootNode); Element node
		 * = list.get(i);
		 */

		@SuppressWarnings("unchecked")
		List<Element> organizations = node.getChildren("org");

		for (Element o : organizations) {
			// remove any ending periods
			if (o.getText().contains(". ") || o.getText().charAt(o.getText().length() - 1) == '.') {
				String o2 = removeEndPer(o.getText());
				o.setText(o2);
			}
			organs.add(o.getText());

			// if o still contains a period, strip the period and add the new version to
			// organs
			if (o.getText().contains(".")) {
				String o2 = o.getText().replace(".", "");
				organs.add(o2);
			}
			// if o contains &/and make a new version with the opposite and add new version
			// to organs
			if (o.getText().contains(" and ") || o.getText().contains(" & ")) {
				StringBuilder orgBuilder = new StringBuilder();
				orgBuilder.append(organs.get(organs.size() - 1));
				String o2 = addAnd(o.getText(), orgBuilder);
				if (o2 != null) {
					organs.add(o2);
				}
			}
		}
	}

	/**
	 * method to process fullname parsing
	 *
	 * @param nameAltsNode
	 */
	private static void processNameAlt(Relations r, Person primaryPerson, Element nameAltsNode,
									   Set<String> primaryKeys) {
		// ------------------------ turn into a method
		// ----------------------------------||
		@SuppressWarnings("unchecked")
		List<Element> altList = nameAltsNode.getChildren("nameAlt");

		// loop through all nameAlt to process "birth name" first
		for (Element altNode : altList) {
			String value = altNode.getChildText("value");
			String type = altNode.getChildText("type");
			int valueSize = value.split(" ").length;

			if (type != null && type.toUpperCase().contains("BIRTH") && valueSize == 1) {
				primaryPerson.setBirthLast(value);
			}
		}
		// loop through all nameAlt to process rest
		for (Element altNode : altList) {
			String value = altNode.getChildText("value");
			String type = altNode.getChildText("type");
			int valueSize = value.split(" ").length;

			if (type != null && type.toUpperCase().contains("BIRTH") && valueSize == 1) {
				primaryPerson.setBirthLast(value);
			} else if ((type == null || type.toUpperCase().contains("NICK")
					|| (!type.toUpperCase().contains("BIRTH") && !type.toUpperCase().contains("STAGE")))
					&& valueSize == 1) {
				Person person = new Person();

				if (primaryPerson.getNameLast() != null && primaryPerson.getBirthLast() != null) {
					person.setNameLast(value + " " + primaryPerson.getNameLast() + "\"\"" + value + " "
							+ primaryPerson.getBirthLast());
				} else if (primaryPerson.getNameLast() != null && primaryPerson.getBirthLast() == null) {
					person.setNameLast(value + " " + primaryPerson.getNameLast());
				} else if (primaryPerson.getBirthLast() != null && primaryPerson.getNameLast() == null) {
					person.setNameLast(value + " " + primaryPerson.getBirthLast());
				}
				r.setPrimaries(value, person);
			} else if (type != null && type.toUpperCase().contains("STAGE")) {
				Person person = new Person();
				person.setNameLast(value);
				r.setPrimaries(value, person);
			}
			// nameAlt basic ("fullname" parsing)
			else {
				value = removePreSuff(value);
				fullnameParsing(value, primaryKeys, r);
			}
		}
		// ---------------------------------------------------------||
	}

	/**
	 * method to process fullname parsing
	 *
	 * @param name
	 * @param primaryKeys
	 * @param r
	 */
	private static void fullnameParsing(String name, Set<String> primaryKeys, Relations r) {
		Person primaryPerson2 = new Person();
		Person primaryPerson3 = new Person();

		String[] namePieces = name.split(" ");

		// remove periods in between letters
		String nameVar2 = null;
		String[] namePieces2 = null;
		if (name.contains(".")) {
			nameVar2 = name.replace(".", "");
			namePieces2 = nameVar2.split(" ");
		}

		// see if last piece contains "!", if it does add it to nameSuf and remove from
		// namePieces
		if (namePieces[namePieces.length - 1].contains("!")) {
			String sfx = namePieces[namePieces.length - 1].replace("!", " ");

			// remove extra space at the end, if there is one
			if (sfx.charAt(sfx.length() - 1) == ' ') {
				sfx = sfx.replaceAll("\\s+$", "");
			}
			primaryPerson2.setNameSuf(sfx);

			namePieces = Arrays.copyOfRange(namePieces, 0, namePieces.length - 1);

			// do the same to namePieces2
			if (sfx.contains(".")) {
				sfx = sfx.replace(".", "");
				primaryPerson3.setNameSuf(sfx);

				namePieces2 = Arrays.copyOfRange(namePieces2, 0, namePieces2.length - 1);
			}
		}

		if (namePieces.length == 4) {
			primaryPerson2.setNameFirst(namePieces[0]);
			primaryPerson2.setNameMid(namePieces[1]);
			primaryPerson2.setBirthLast(namePieces[2]);
			primaryPerson2.setNameLast(namePieces[3]);

			if (nameVar2 != null) {
				primaryPerson3.setNameFirst(namePieces2[0]);
				primaryPerson3.setNameMid(namePieces2[1]);
				primaryPerson3.setBirthLast(namePieces2[2]);
				primaryPerson3.setNameLast(namePieces2[3]);
			}
		} else if (namePieces.length == 3) {
			primaryPerson2.setNameFirst(namePieces[0]);
			primaryPerson2.setNameMid(namePieces[1]);
			primaryPerson2.setNameLast(namePieces[2]);

			if (nameVar2 != null) {
				primaryPerson3.setNameFirst(namePieces2[0]);
				primaryPerson3.setNameMid(namePieces2[1]);
				primaryPerson3.setNameLast(namePieces2[2]);
			}
		} else if (namePieces.length == 2) {
			// primaryPerson2.setNameFirst(namePieces[0]);
			primaryPerson2.setNameLast(namePieces[0] + " " + namePieces[1]);

			if (nameVar2 != null) {
				// primaryPerson3.setNameFirst(namePieces2[0]);
				primaryPerson3.setNameLast(namePieces2[0] + " " + namePieces2[1]);
			}
		} else if (namePieces.length == 1) {
			primaryPerson2.setNameLast(namePieces[0]);

			if (nameVar2 != null) {
				primaryPerson3.setNameLast(namePieces2[0]);
			}
		}

		primaryKeys.add(name);
		r.setPrimaries(name, primaryPerson2);

		// set version without periods
		if (nameVar2 != null) {
			primaryKeys.add(nameVar2);
			r.setPrimaries(nameVar2, primaryPerson3);
		}
	}

	/**
	 * method to remove prefix / suffix in names
	 *
	 * @param name
	 * @return
	 */
	private static String removePreSuff(String name) {
		// prefix & suffix

		// period logic
		name = removeEndPer(name);
		// name = name.replaceAll("[^\\p{L} ]", " ");

		// take out double spaces
		while (name.contains("  ")) {
			name = name.replace("  ", " ");
		}

		String[] personArr = name.split(" ");

		// see if first part of nameArray includes any of the prefix's
		boolean pre = false;
		String[] newPersonArr = null;
		String[] prefix = {"MISTER", "MR", "MISTRESS", "MRS", "MS", "MISS", "DOCTOR", "DR", "PROFESSOR", "PROF", "PR",
				"FATHER", "FR", "PASTOR", "PR", "BROTHER", "BR", "SISTER", "SR", "MOTHER", "RABBI", "REVEREND", "REV",
				"MONSIGNOR", "MSGR", "ADMIRAL", "ADM", "MARSHAL", "MAR", "SHERIFF", "GENERAL", "GEN", "LIEUTENANT",
				"LT", "MAJOR", "MAJ", "COMMODORE", "COM", "BRIGADIER", "BRIG", "BR", "CAPTAIN", "CAPT", "COLONEL",
				"COL", "COMMANDER", "OFFICER", "OFF", "ENSIGN", "ENS", "SERGEANT", "SGT", "CORPORAL", "PRIVATE", "PVT",
				"SEAMAN", "AIRMAN", "VICE", "PRESIDENT", "PRES"};
		for (String value : prefix) {
			if (personArr.length > 0 && personArr[0].equalsIgnoreCase(value)) {
				pre = true;
				newPersonArr = Arrays.copyOfRange(personArr, 1, personArr.length);
				// loop through all prefix options again for next word in name
				for (String s : prefix) {
					if (newPersonArr[0].equalsIgnoreCase(s)) {
						newPersonArr = Arrays.copyOfRange(newPersonArr, 1, newPersonArr.length);
					}
				}
			}
		}

		// save suffix in a StringBuilder
		String namesuf2 = null;

		// see if last two parts of newNameArr includes any of the suffix's
		String namesuf1 = null;
		boolean suff = false;
		String[] suffix = {"JR", "JUNIOR", "SR", "SENIOR", "2ND", "II", "3RD", "III", "4TH", "IV", "5TH", "V", "6TH",
				"VI", "AJ", "A.J", "CEO", "C.E.O", "CNA", "C.N.A", "CP", "C.P", "CPA", "C.P.A", "CPPS", "C.P.P.S",
				"CSJ", "C.S.J", "CSV", "C.S.V", "DC", "D.C", "DDS", "D.D.S", "DO", "D.O", "DV", "D.V", "DVM", "D.V.M",
				"EDD", "ED.D", "ESQ", "ESQUIRE", "JD", "J.D", "LCPL", "L.C.P.L", "LPN", "L.P.N", "MBA", "M.B.A", "MD",
				"M.D", "OFM", "O.F.M", "OP", "O.P", "OSF", "O.S.F", "PHD", "PH.D", "RN", "R.N", "RSM", "R.S.M", "SJ",
				"S.J", "US", "U.S", "USAF", "U.S.A.F", "USAR", "U.S.A.R", "USMC", "U.S.M.C", "USN", "U.S.N"};
		for (String value : suffix) {
			if (pre) {
				if (newPersonArr.length > 0 && newPersonArr[newPersonArr.length - 1].equalsIgnoreCase(value)) {
					suff = true;

					// add suffix to nameSuf
					namesuf1 = newPersonArr[newPersonArr.length - 1];

					// remove end suffix from array
					newPersonArr = Arrays.copyOfRange(newPersonArr, 0, newPersonArr.length - 1);

					// added for recursion
					for (String s : suffix) {
						if (newPersonArr[newPersonArr.length - 1].equalsIgnoreCase(s)) {
							// add suffix to nameSuf
							namesuf2 = "!" + newPersonArr[newPersonArr.length - 1];

							// remove end suffix from array
							newPersonArr = Arrays.copyOfRange(newPersonArr, 0, newPersonArr.length - 1);
						}
					}
				}
			}
			// if pre equals false
			else {
				if (personArr.length > 0 && personArr[personArr.length - 1].equalsIgnoreCase(value)) {
					suff = true;

					// add suffix to nameSuf
					namesuf1 = personArr[personArr.length - 1];

					// remove end suffix from array
					newPersonArr = Arrays.copyOfRange(personArr, 0, personArr.length - 1);

					// added for recursion
					for (String s : suffix) {
						if (newPersonArr[newPersonArr.length - 1].equalsIgnoreCase(s)) {
							// add suffix to nameSuf
							namesuf2 = "!" + newPersonArr[newPersonArr.length - 1];

							// remove end suffix from array
							newPersonArr = Arrays.copyOfRange(newPersonArr, 0, newPersonArr.length - 1);
						}
					}
				}
			}
		}

		// make newNameArr equal to nameArray if there were no pre / suff modifications
		if (!pre && !suff) {
			newPersonArr = personArr;
		}

		// make newPersonArr = nameVar
		StringBuilder nameBuilder = new StringBuilder();
		boolean pass = false;
		for (String s : newPersonArr) {
			if (pass) {
				nameBuilder.append(" ");
			}

			nameBuilder.append(s);
			pass = true;
		}
		name = nameBuilder.toString();

		// add name suffix to the end of namevar
		if (namesuf1 != null || namesuf2 != null) {
			if (namesuf1 != null && namesuf2 != null) {
				name = name + " " + namesuf2 + "!" + namesuf1;
			} else if (namesuf1 != null) {
				name = name + " " + namesuf1 + "!";
			}
		}
		return name;
	}

//	/**
//	 * method to set terms to relations ***Relations()/Terms() class?
//	 *
//	 * @param xmlTerm
//	 * @param termKey
//	 * @param relations
//	 * @param termsList
//	 */
//	/*
//	 * private static void setTerms(String xmlTerm, String termKey, Relations r,
//	 * List<?> termsList) { String termString = null; for (int a = 0; a <
//	 * termsList.size(); a++) { Element termsNode = (Element) termsList.get(a);
//	 *
//	 * //go inside Terms and get all term elements List <?> inTerms =
//	 * termsNode.getChildren(xmlTerm); Terms terms = new Terms();
//	 *
//	 * for (int x = 0; x < inTerms.size(); x++) { Element inNode = (Element)
//	 * inTerms.get(x); termString = inNode.getText(); //take out any quotation
//	 * if(termString.contains("\"")) { termString = termString.replace("\"", ""); }
//	 * terms.addTerm(termString); r.setTerms(termKey,terms); } } }
//	 */

	/**
	 * method to get and add term phrases to triplets ***Terms() class?
	 *
	 * @param termsList
	 * @param trip
	 */
	private static void addTerms(List<Terms> termsList, StringBuilder trip) {
		Terms term = new Terms();
		if (termsList != null && termsList.get(0) != null) {
//			FIXME: Looks like mistake, or only wanting to get last value in the list? Better way to do it.
			for (Terms value : termsList) {
				term = value;
			}

			List<String> termStr = term.getTerms();
			for (String terms : termStr) {
				// make sure terms is not empty
				if (terms.length() > 0) {
					// remove any ending periods
					if (terms.contains(". ") || terms.charAt(terms.length() - 1) == '.') {
						terms = removeEndPer(terms);
					}

					String st = "";

					// adding & or AND of terms that contain them
					String s = "";
					if (!s.equalsIgnoreCase(terms) && !st.equalsIgnoreCase(terms)) {
						trip.append("\"").append(terms).append("\"");
					}

					if (terms.contains(" and ") || terms.contains(" & ")) {
						s = addAnd(terms, trip);
						if (s != null) {
							trip.append("\"");
							s = s.replace("\"", "\"\"");
							trip.append(s).append("\"");
						}
					}

					if (terms.contains(".")) {
						st = terms.replace(".", "");

						if (!trip.toString().contains(st)) {
							trip.append("\"");
							st = st.replace("\"", "\"\"");
							trip.append(st).append("\"");
						}
					}
				}
			}
		}
	}

	/**
	 * method to eliminate any empty elements in input form
	 *
	 * @param element
	 */
	private static void removeEmptyElements(Element element) {
		if (element.getChildren().size() == 0 && "".equals(element.getValue())) {
			element.getParent().removeContent(element);
			return;
		}
		// recurse the children
		for (int i = 0; i < element.getChildren().size(); i++) {
			removeEmptyElements((Element) element.getChildren().get(i));
		}
	}

	/**
	 * method to convert an array to a string
	 *
	 * @param arr
	 * @return
	 */
	private static String arrayToString(String[] arr) {
		// make Array a string
		StringBuilder orgBuilder = new StringBuilder();
		boolean pass = false;
		for (String s : arr) {
			if (pass) {
				orgBuilder.append(" ");
			}
			orgBuilder.append(s);
			pass = true;
		}
		return orgBuilder.toString();
	}

	/**
	 * method to take care of and/& variations
	 *
	 * @param string
	 * @param trip
	 * @return
	 */
	private static String addAnd(String string, StringBuilder trip) {
		String newString;

		if (string.contains(" and ")) {
			newString = string.replace(" and ", " & ");
			if (trip.toString().contains(newString)) {
				newString = "";
			} else
				newString = newString.replace(" & ", " ");
		} else if (string.contains(" AND ")) {
			newString = string.replace(" AND ", " & ");
			if (trip.toString().contains(newString)) {
				newString = "";
			} else
				newString = newString.replace(" & ", " ");
		} else if (string.contains(" & ")) {
			newString = string.replace(" & ", " and ");
			if (trip.toString().contains(newString)) {
				newString = "";
			}
		} else if (string.contains(" &amp; ")) {
			newString = string.replace(" &amp; ", " and ");
			if (trip.toString().contains(newString)) {
				newString = "";
			}
		} else
			newString = "";

		return newString;
	}

	/**
	 * method to replace any '&' left in triplet string with a space
	 *
	 * @param trip
	 * @return
	 */
	private static String replaceAmp(StringBuilder trip) {
		String newString = null;

		CharSequence replacement = " ";
		CharSequence target = " & ";
		for (int i = 0; i < trip.length(); i++) {
			newString = trip.toString().replace(target, replacement);

		}

		return newString;
	}

	/**
	 * method to add OR between triplet phrases
	 *
	 * @param trip
	 * @return
	 */
	private static String addOr(StringBuilder trip) {
		String newString = null;

		CharSequence replacement = "\" OR \"";
		CharSequence target = "\"\"";
		for (int i = 0; i < trip.length(); i++) {
			newString = trip.toString().replace(target, replacement);
		}
		return newString;
	}

	/**
	 * method to remove period at the end of words/letters
	 *
	 * @param str
	 * @return
	 */
	private static String removeEndPer(String str) {
		if (str != null && str.length() > 0 && str.contains(". ")) {
			str = str.replace(". ", " ");
		}
		if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == '.') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	/**
	 * method to delete duplicate triplet values
	 *
	 * @param trip
	 * @param nameFirst
	 * @param nameLast
	 * @param birthLast
	 * @return
	 */
	private static String deleteDup(String trip, String nameFirst, String nameLast, String birthLast) {
		String triplets = trip;

		StringTokenizer st1 = new StringTokenizer(triplets, ")\"");

		List<String> words = new ArrayList<>();
		while (st1.hasMoreTokens()) {
			String word = st1.nextToken().trim();
			words.add(word);
		}

		List<String> phrases = new ArrayList<>();
		for (String s : words) {
			if (!s.equals(" ") && s.length() < 65) {
				phrases.add(s);
			}
		}

		// if triplets contains any of the phrases in phrases remove from triplets
		String removed = null;
		for (String s : phrases) {
			StringBuilder sb = new StringBuilder(trip);
			sb.reverse();
			String tripRev = sb.toString();
			if (countNeedlesInHaystack(trip, "\"" + s + "\"") > 1) {
				// add conditional to not check phrases that are just a persons nameFirst or
				// nameLast or birthLast
				if (!(s).equals(nameFirst) && !(s).equals(nameLast) && !(s).equals(birthLast)) {
					String targetRev = new StringBuilder(s).reverse().toString();

					removed = tripRev.replaceFirst(("\"" + targetRev + "\""), "");
					removed = new StringBuilder(removed).reverse().toString();

					trip = removed;
				}
			}
		}
		if (removed != null) {
			return removed;
		} else
			return trip;
	}

	/**
	 * method to take care of duplicate phrases split problem How many times does
	 * the needle occur in the string 'haystack'?
	 *
	 * @param haystack
	 * @param needle
	 * @return
	 */
	public static int countNeedlesInHaystack(String haystack, String needle) {
		int offset = 0;
		int retval = 0;
		while ((offset = haystack.indexOf(needle, offset)) != -1) {
			retval += 1;
			offset += 1;
		}
		return retval;
	}

	@Override
	public PermutedResult convert(String inData, String inFormat, Map<String, String> options) throws Throwable {
//		FIXME: This method has 8 different return statements. Begging for a refactor
		Instant start = Instant.now();

		Relations r = process(inData);

		String tripletData = this.permute(r) + "&searchLogtf=true";
		if(highlightTerms != null && highlightTerms.length() > 0)
		{
			tripletData = tripletData + highlightTerms;
		}

		if (StringUtils.isNotEmpty(gatewayQuery)) {
			// remove scorelimit and option from gateway request then add it back to triplet
			// data
			// so it can be used in rankEvidence
			String scorelimit = null;
			if (gatewayQuery.contains("scorelimit")) {
				String[] eQuery = gatewayQuery.split("scorelimit");
				scorelimit = "&scorelimit" + eQuery[1];
				tripletData = tripletData.replace(scorelimit, "");
			}
			String option = null;
			if (gatewayQuery.contains("option")) {
				String[] eQuery = gatewayQuery.split("option");
				option = "&option" + eQuery[1];
				tripletData = tripletData.replace(option, "");
			}
			// revome '-' if it got included in maxhits value
			if (tripletData.contains("maxhits=-")) {
				tripletData = tripletData.replace("maxhits=-", "maxhits=");
			}

			// call the build URL w/triplet data and extra query; call gateway and return
			// result
			GatewaySearch gs = new GatewaySearch();
			GatewaySearch gsearch = new GatewaySearch();

			// if rankEvidence is not needed do this
			// ------------------------------------------------------------------------
			String url;
			String gatewayResult;
			String docInfo;
			if (option != null && (option.contains("norank") || option.contains("docref"))) {
				if (tripletData.contains("maxhits")) {
					// make sure maxhits has the '-' in front since it is a fed call
					if (!tripletData.contains("maxhits=-")) {
						tripletData = tripletData.replace("maxhits", "maxhits=-");
					}
					url = gs.getURL(tripletData + "&offset=1", "fed");
				} else {
					url = gs.getURL(tripletData + "&offset=1&maxhits=-120", "fed");
				}
				// should split the pieces at the question mark
				int theQueryIndex = url.indexOf('?');
				gatewayResult = gs.getDocument(url.substring(0, theQueryIndex), url.substring(theQueryIndex + 1));

				if (option.contains("docref")) {
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(new StringReader(gatewayResult));

					Element resultList = document.getRootElement();
					Element fedtask = resultList.getChild("fedtask");
					if (null != fedtask) {
						Element result = fedtask.getChild("result");
						if (null != result) {
							Element hitlist = result.getChild("hitlist");
							if (null != hitlist) {
								@SuppressWarnings("rawtypes")
								Iterator listIt = hitlist.getChildren("hit").iterator();

								// grab all docrefs (make sure there are hits first)
								if (!gatewayResult.contains("total=\"0\"")) {
									boolean b = false;
									StringBuilder docrefs = new StringBuilder();
									while (listIt.hasNext()) {
										Element hit = (Element) listIt.next();
										String docref = hit.getChildText("docref");
										if (b) {
											docrefs.append(",");
										}
										docrefs.append(docref);
										b = true;
									}
									docrefs.substring(0, docrefs.length() - 1);

									String[] arr = docrefs.toString().split(",");
									StringBuilder docrefsFinal = new StringBuilder();
									docrefsFinal.append("<docrefs>");
									int theCount = 0;
									for (String s : arr) {
										theCount++;
										docrefsFinal.append("<docref>").append(s).append("</docref>");
									}

									docrefsFinal.append("<count>").append(theCount).append("</count>");
									docrefsFinal.append("</docrefs>");
									Instant end = Instant.now();
									long timeElapsed = Duration.between(start, end).toMillis();
//									logger.info("elapsed time: {}", timeElapsed);
									logger.info("elapsed time: "+ timeElapsed);
									return new PermutedResult("text/plain", docrefsFinal.toString());
								} else {
									Instant end = Instant.now();
									long timeElapsed = Duration.between(start, end).toMillis();
//									logger.info("elapsed time: {}", timeElapsed);
									logger.info("elapsed time: "+ timeElapsed);
									return new PermutedResult("text/xml;charset=UTF-8",
											"<docrefs>\n  <count>0</count>\n</docrefs>");
								}
							}else {
								Instant end = Instant.now();
								long timeElapsed = Duration.between(start, end).toMillis();
//								logger.warn("{} was NULL, elapsed time: {}","fedtask/result/hitlist", timeElapsed);
								logger.warn("fedtask/result/hitlist was NULL, elapsed time: {}"+ timeElapsed);
								return new PermutedResult("text/xml;charset=UTF-8",
										"<docrefs>\n  <count>0</count>\n</docrefs>");
							}
						}else {
							Instant end = Instant.now();
							long timeElapsed = Duration.between(start, end).toMillis();
//							logger.warn("{} was NULL, elapsed time: {}","fedtask/result", timeElapsed);
							logger.warn("fedtask/result was NULL, elapsed time: "+ timeElapsed);
							return new PermutedResult("text/xml;charset=UTF-8",
									"<docrefs>\n  <count>0</count>\n</docrefs>");
						}
					}else {
						Instant end = Instant.now();
						long timeElapsed = Duration.between(start, end).toMillis();
//						logger.warn("{} was NULL, elapsed time: {}","fedtask", timeElapsed);
						logger.warn("fedtask was NULL, elapsed time: {}"+ timeElapsed);
						return new PermutedResult("text/xml;charset=UTF-8",
								"<docrefs>\n  <count>0</count>\n</docrefs>");
					}
				} else {
					Instant end = Instant.now();
					long timeElapsed = Duration.between(start, end).toMillis();
//					logger.info("elapsed time: {}", timeElapsed);
					logger.info("elapsed time: "+timeElapsed);
					return new PermutedResult("text/plain", gatewayResult);
				}
			}

			// ---------------------------------------------------------------------------------------------------------------
			else if (option != null && (option.contains("evidscore-cutoff") || option.contains("evidencescore"))) {
				// eric's endpoint call
				String url2;
				if (tripletData.contains("maxhits")) {
					url = gs.getURL(tripletData + "&offset=1", "doc");
					url2 = gsearch.getURL(tripletData +"&offset=1", "fed");

				} else {
					url = gs.getURL(tripletData +"&offset=1&maxhits=120", "doc");
					url2 = gsearch.getURL(tripletData +"&offset=1&maxhits=-120", "fed");
				}

				// should split the pieces at the question mark
				int theQueryIndex = url.indexOf('?');
				int queryIndex = url2.indexOf('?');

				logger.debug("preparing to generate RankEvidence with threaded calls");
				Instant threadedStart = Instant.now();
				HttpRequestor docInfoReq = new HttpRequestor("doc", url.substring(0, theQueryIndex),
						url.substring(theQueryIndex + 1));
				HttpRequestor gatewayResultReq = new HttpRequestor("gw", url2.substring(0, queryIndex),
						url2.substring(queryIndex + 1));
				Thread gatewayResultThread = new Thread(gatewayResultReq, "gw1");
				gatewayResultThread.start();
				Thread docInfoThread = new Thread(docInfoReq, "d1");
				docInfoThread.start();

				docInfoThread.join();
				gatewayResultThread.join();
				Instant threadedEnd = Instant.now();

				long threadTimeElapsed = Duration.between(threadedStart, threadedEnd).toMillis();
//				long was = Long.parseLong(ThreadContext.get("serviceTime"));
//				String elapsedCallTime = Long.toString(was+threadTimeElapsed);
//				logger.debug("elapsedCallTime {}",elapsedCallTime);
//				ThreadContext.put("serviceTime", elapsedCallTime);

				docInfo = docInfoReq.getResult();
				gatewayResult = gatewayResultReq.getResult();

				// kLogger.debug("gatewayresult!:" + gatewayResult);
				if (StringUtils.isEmpty(gatewayResult)|| gatewayResult.contains("total=\"0\"")
						|| StringUtils.isEmpty(docInfo)||docInfo.contains("count=\"0\"")
						|| docInfo.contains("missing authentication token")) {
					GatewaySearch gs2 = new GatewaySearch();
					String emptycall = gs2.getURL(tripletData, "fed");
					int theQueryIndex2 = emptycall.indexOf('?');
					String gatewayResultEmpty = gs2.getDocument(emptycall.substring(0, theQueryIndex2),
							emptycall.substring(theQueryIndex2 + 1));
					Instant end = Instant.now();
					long timeElapsed = Duration.between(start, end).toMillis();
//					logger.info("elapsed time: {}", timeElapsed);
					logger.info("elapsed time: "+ timeElapsed);
					return new PermutedResult("text/xml;charset=UTF-8", gatewayResultEmpty);
				} else {
					String output;
					if (scorelimit != null) {
						output = RankEvidence.process(gatewayResult, docInfo, tripletData + scorelimit + option);
					}
//FIXME: How can this ever be reached? It's duplicate condition of above. Must be a mistake.
//					else if (scorelimit != null) {
//						output = RankEvidence.process(gatewayResult, docInfo, tripletData + scorelimit);
//					}
//FIXME: We know option is not null as it was assigned around line 1808, so unwrapping this
//					else if (option != null) {
//						output = RankEvidence.process(gatewayResult, docInfo, tripletData + option);
//					} else if (scorelimit == null && option == null) {
//						output = RankEvidence.process(gatewayResult, docInfo, tripletData);
//					}
					else {
						output = RankEvidence.process(gatewayResult, docInfo, tripletData + option);
					}
					Instant end = Instant.now();
					long timeElapsed = Duration.between(start, end).toMillis();
//					logger.debug("elapsed time: {}", timeElapsed);
					logger.debug("elapsed time: "+ timeElapsed);
					return new PermutedResult("text/plain", output);
				}
			}
			// no options
			// ---------------------------------------------------------------------------------------------------------------
			else {
				// eric's endpoint call
				if (tripletData.contains("maxhits")) {
					url = gs.getURL(tripletData +"&offset=1", "doc");
				} else {
					url = gs.getURL(tripletData +"&offset=1&maxhits=120", "doc");
				}

				// kLogger.debug("gateway fedsearch URL: "+url);

				// should split the pieces at the question mark
				int theQueryIndex = url.indexOf('?');
				docInfo = gs.getDocument(url.substring(0, theQueryIndex), url.substring(theQueryIndex + 1));

				if (docInfo.isEmpty() || docInfo.contains("count=\"0\"")) {
					GatewaySearch gs2 = new GatewaySearch();
					String emptycall = gs2.getURL(tripletData, "fed");
					int theQueryIndex2 = emptycall.indexOf('?');
					String gatewayResultEmpty = gs2.getDocument(emptycall.substring(0, theQueryIndex2),
							emptycall.substring(theQueryIndex2 + 1));
					Instant end = Instant.now();
					long timeElapsed = Duration.between(start, end).toMillis();
//					logger.info("elapsed time: {}", timeElapsed);
					logger.info("elapsed time: "+ timeElapsed);

					return new PermutedResult("text/xml;charset=UTF-8", gatewayResultEmpty);
				} else {
					String gateway = null;
//						FIXME: We know gateway is always null, and will cause RankEvidence to throw a NPE.
					String output;
					if (scorelimit != null && option != null) {
						output = RankEvidence.process(gateway, docInfo, tripletData + scorelimit + option);
					} else if (scorelimit != null) {
						output = RankEvidence.process(gateway, docInfo, tripletData + scorelimit);
					} else if (option != null) {
						output = RankEvidence.process(gateway, docInfo, tripletData + option);
					}
					//					FIXME: We know these are all null from above checks, and below else takes care of the exact same thing, so commenting out
//					else if (scorelimit == null && option == null) {
//						output = RankEvidence.process(gateway, docInfo, tripletData);
//					}
					else {
//						FIXME: Again, we know gateway is always null and will cause RankEvidence to throw a NPE.
						output = RankEvidence.process(gateway, docInfo, tripletData);
					}
					Instant end = Instant.now();
					long timeElapsed = Duration.between(start, end).toMillis();
//					logger.info("elapsed time: {}", timeElapsed);
					logger.info("elapsed time: "+ timeElapsed);
					return new PermutedResult("text/plain", output);
				}
			}
		} else {
			Instant end = Instant.now();
			long timeElapsed = Duration.between(start, end).toMillis();
//			logger.info("elapsed time: {}", timeElapsed);
			logger.info("elapsed time: "+ timeElapsed);
			return new PermutedResult("text/plain", tripletData);
		}
	}
}

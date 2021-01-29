package com.newsbank.permuter.permutation;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.newsbank.permuter.PermutedResult;
import com.newsbank.permuter.net.DocFetch;
import com.newsbank.permuter.net.GatewaySearch;
import com.newsbank.permuter.types.Config;
import com.newsbank.permuter.types.DocStore;

public class RankEvidence implements Permutation
{	
//	private final static Logger kLogger = LogManager.getLogger();
	private final static Logger kLogger = Logger.getLogger(RankEvidence.class);
	//FIXME: Throws NPE if gateway is null, and gateway can be null 5 times in PhilanPermuter.
	public static String process(String gateway, String docInfo, String queryInfo)
	{
		StringBuilder evidence = new StringBuilder();
		
		//kLogger.debug("DOCINFO!: "+docInfo);
		//kLogger.debug("gateway!: "+gateway);
		//kLogger.debug("queryInfo!: "+queryInfo);
		//System.exit(0);
		
		Map <String, List<DocStore>> docs = new HashMap<String, List<DocStore>>();

		StringBuilder docrefs = new StringBuilder();
		StringBuilder evidencescore = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		SAXBuilder builder = new SAXBuilder();
		String unqVal = null;
		String UNQ = null;
		int score = 0;

		Config conf = new Config();
		String fulltext = conf.getFulltext();
		String citation = conf.getCitation();
		String alltext = conf.getAlltext();

		
		//remove any line feeds in queryInfo
		queryInfo=queryInfo.trim();

		int q1 = queryInfo.indexOf("p_field_primary-0=");
		int q2 = queryInfo.indexOf("&p_params_primary-0");
		String option = null;
		if(queryInfo.contains("&p_field_primary-0="))
		{
			option = queryInfo.substring(q1 + 18, q2 ).toUpperCase();
		}
		else 
			option = "alltext"; 

		evidence.append(queryInfo + "\n");

		String [] ft = fulltext.split(" ");
		String [] cit = citation.split(" ");
		String [] at = alltext.split(" ");
		
		//pull out key phrases in queryInfo to see if it appears in each doc
		String prim0 = null;
		String prim2 = null;
		String prim4 = null;
		String sec0 = null;
		String sec2 = null;
		String sec4 = null;
		String sec9 = null;
		List<String> words = new ArrayList<String>();
		List<String> words1 = new ArrayList<String>();
		List<String> words2 = new ArrayList<String>();
		List<String> words3 = new ArrayList<String>();
		List<String> words4 = new ArrayList<String>();
		List<String> words5 = new ArrayList<String>();
		List<String> words6 = new ArrayList<String>();

		//grab what's inside of each triplet 
		if(queryInfo.contains("primary-0=(") && !queryInfo.contains("primary-0=()")) 
		{
			prim0 = queryInfo.substring(queryInfo.indexOf("primary-0=(") +11, queryInfo.indexOf(")&")).replaceAll("[^\\p{L} ]", " ");
		}
		
		if(queryInfo.contains("primary-2=(") && !queryInfo.contains("primary-2=()")) 
		{
			prim2 = queryInfo.substring(queryInfo.indexOf("primary-2=(") +11, queryInfo.indexOf(")&", queryInfo.indexOf("primary-2=("))).replaceAll("[^\\p{L} ]", " ");
		}
		
		if(queryInfo.contains("primary-4=(") && !queryInfo.contains("primary-4=()")) 
		{
			prim4 = queryInfo.substring(queryInfo.indexOf("primary-4=(") +11, queryInfo.indexOf(")&", queryInfo.indexOf("primary-4=("))).replaceAll("[^\\p{L} ]", " ");;
		}
		
		if(queryInfo.contains("second-0=(") && !queryInfo.contains("second-0=()")) 
		{
			sec0 = queryInfo.substring(queryInfo.indexOf("second-0=(") +10, queryInfo.indexOf(")&", queryInfo.indexOf("second-0=("))).replaceAll("[^\\p{L} ]", " ");;
		}

		if(queryInfo.contains("second-2=(") && !queryInfo.contains("second-2=()")) 
		{
			sec2 = queryInfo.substring(queryInfo.indexOf("second-2=(") +10, queryInfo.indexOf(")&", queryInfo.indexOf("second-2=("))).replaceAll("[^\\p{L} ]", " ");
		}

		if(queryInfo.contains("second-4=(") && !queryInfo.contains("second-4=()")) 
		{
			sec4 = queryInfo.substring(queryInfo.indexOf("second-4=(") +10, queryInfo.indexOf(")&", queryInfo.indexOf("second-4=("))).replaceAll("[^\\p{L} ]", " ");;
		}
		
		if(queryInfo.contains("second-9=(") && !queryInfo.contains("second-9=()")) 
		{
			sec9 = queryInfo.substring(queryInfo.indexOf("second-9=(") +10, queryInfo.indexOf(")&", queryInfo.indexOf("second-9=("))).replaceAll("[^\\p{L} ]", " ");;
		}
		
		//break up data inside triplets into terms 
		if(prim0 != null)
		{
			String [] st = prim0.split("OR");
			words=terms(st);
		}
		
		if(sec0 != null)
		{
			String [] st = sec0.split("OR");
			words2=terms(st);
		}
		
		if(sec4 != null)
		{
			String [] st = sec4.split("OR");
			words3=terms(st);
		}
		
		if(prim2 != null)
		{
			String [] st = prim2.split("OR");
			words4=terms(st);
		}
		
		if(prim4 != null)
		{
			String [] st = prim4.split("OR");
			words1=terms(st);
		}
		
		if(sec2 != null)
		{
			String [] st = sec2.split("OR");
			words5=terms(st);
		}
		
		if(sec9 != null)
		{
			String [] st = sec9.split("OR");
			words6=terms(st);
		}
		
		try //see if terms are in document, how many times and output
		{
			Document document = (Document) builder.build(new StringReader(docInfo));
			
			//kLogger.debug("docinfo: " + docInfo);
			//root element (personSearch)
			Element rootNode = document.getRootElement();
			
			@SuppressWarnings("unchecked")
			
			List<Element> nbx = rootNode.getChildren("NBX");
			Iterator<Element> it = nbx.iterator();

			//grab everything that's inside of the corresponding elements; FULLTEXT/CITATION/ALLTEXT 
			while(it.hasNext()) 
			{
				Map <String, Integer> evid = new HashMap<String, Integer>();
				List <DocStore> docAtt = new ArrayList<DocStore>();
				DocStore theDocs = new DocStore();
				
				sb = new StringBuilder();
				Element nbxNode = it.next();
				
				String unq = nbxNode.getChildText("UNQ");
				
				if(option != null && option.equals("FULLTEXT")) 
				{
					for(int i = 0; i < ft.length; i++) 
					{
						Element node = nbxNode.getChild(ft[i]);
	
						if(node != null) 
						{
							sb.append(nbxNode.getChildText(ft[i]) + " ");
							elmWithin(node,sb);
						}
					}
				}
				else if(option != null && option.equals("CITATION")) 
				{
					for(int i = 0; i < cit.length; i++) 
					{
						Element node = nbxNode.getChild(cit[i]);
						if(node != null) 
						{
							sb.append(nbxNode.getChildText(cit[i]) + " ");
							elmWithin(node,sb);
						}
					}
				}
				else if(option == null || option.equals("ALLTEXT")) 
				{
					for(int i = 0; i < at.length; i++) 
					{
						Element node = nbxNode.getChild(at[i]);
						if(node != null) 
						{
							sb.append(nbxNode.getChildText(at[i]) + " ");
							elmWithin(node, sb);
						}
					}
				}
				else {
					for(int i = 0; i < at.length; i++) 
					{
						Element node = nbxNode.getChild(at[i]);
						if(node != null) 
						{
							sb.append(nbxNode.getChildText(at[i]) + " ");
							elmWithin(node,sb);
						}
					}
				}
				
				//take out all punctuation and tokenize
				String fileString = null;
				fileString = sb.toString().replaceAll("[^\\p{L} ]", " ").toString();
				while (fileString.contains("  ")) 
				{
					fileString = fileString.replace("  ", " ");
				}
				//kLogger.debug("document:" + fileString);
				
				//count how many occurrence of each phrase there is in the document 
				theDocs.setQuery(queryInfo);
				evidence.append("\""+unq);
				theDocs.setUnq(unq);
				UNQ = unq;
				//evidence.append("Token count) "+fileString.length() + "\n");
				theDocs.setTokCount(fileString.length());
				evidence.append("\n");
				score = 0;

				//define which ones get 2 VS 1 point for towards score: if scoretwo = true - 2 pts | if scoretwo = false - 1 pt
				boolean scoretwo = true;
				
				//type element to set type
				String type = null; 
				
				//for prim-2
				type = "primary";
				score=wordMatch(score, scoretwo, fileString, words4, evidence, evid, type); //2pts
				scoretwo = false;
				type = null;
				
				//sec-2
				type = "second";
				score=wordMatch(score, scoretwo, fileString, words5, evidence, evid, type);
				scoretwo = true;
				type = null;
				
				//prim-0
				type = "primary";
				score=wordMatch(score, scoretwo, fileString, words, evidence, evid, type); //2pts
				
				//prim-4
				score=wordMatch(score, scoretwo, fileString, words1, evidence, evid, type); //2pts
				scoretwo = false;
				type = null;
				
				//sec-0
				type = "second";
				score=wordMatch(score, scoretwo, fileString, words2, evidence, evid, type);
				type = null;
				
				//sec-4
				type = "term";
				score=wordMatch(score, scoretwo, fileString, words3, evidence, evid, type);
				type = null;
				
				//sec-9
				type = "org";
				score=wordMatch(score, scoretwo, fileString, words6, evidence, evid, type);
				theDocs.setScore(score);
				type = null;
				
				//adding evidence to docstore 'theDocs'
				theDocs.setEvidence(evid);
				
				//adding all documents (w their attributes) to DocStore list
				docAtt.add(theDocs);
				evidence.append("score) "+score+"\"\n");
				
				//adding all key information to documents map
				docs.put(score + " " + UNQ, docAtt);
			}
		}
		catch (Throwable io) 
		{
			io.printStackTrace(System.err);
		}
		
		//get each key in the document 
		Set<String> set = docs.keySet();
		Iterator<String> setIt = set.iterator();
		StringBuilder key = new StringBuilder();
		
		while(setIt.hasNext()) 
		{
			unqVal= setIt.next();
			key.append(unqVal+",");
		}
		String [] arr = key.toString().split(",");
		
		StringBuilder scores = new StringBuilder();
		boolean comma = false;
		int scorelimit = 0;
		
		//do not sort if evidencescore option requested
		if(!queryInfo.contains("evidencescore"))
		{
			//sort list of UNQs by score; consider putting sort method inside docref if statement
			sort(arr);
		}
		
		//populate scores array
		for(int x = 0; x < arr.length;x++) 
		{
			if(comma == true) 
			{
				scores.append(",");
			}
			scores.append(arr[x].split(" ")[0]);
			comma = true;
		}
		
		//evaluate rank cutoff using Colleen's average method
		if(!queryInfo.contains("evidencescore"))
		{
			int average = 0;
			String[] scoreArray = scores.toString().split(",");
			//int last = Integer.parseInt(scoreArray[scoreArray.length-1]);
			//if lowest score is greater than 3 keep them all
			if(scoreArray != null && scoreArray.length > 1 /*&& last < 3*/) 
			{
				//get top half of scoreArray
				int half = scoreArray.length / 2 + 1;
				//kLogger.debug("half!!: "+half);
				String [] halfArray =Arrays.copyOfRange(scoreArray, 0, half);
	
				//find average of top half, round down, and keep that score as the cutoff 
				for(int i = 0; i< halfArray.length;i++)
				{
					average += Integer.parseInt(halfArray[i]);
				}
				scorelimit=(int)1.0d * average/halfArray.length;
				if(scorelimit > 3) {
					scorelimit = 3;
				}
				kLogger.debug("average!!: "+average);
			}
		}
		
		StringBuilder bucket = new StringBuilder();
		StringBuilder bucketNone = new StringBuilder();
		boolean flag = false;
		boolean bool0 = false;
		int theScore = 0;
		String [] creds = null;
		String gatewayResult = null;
		
		//evidencescore & evidscore-cutoff section -----------------------------------------------------------------------------------------------
		if(queryInfo.contains("evidencescore") || queryInfo.contains("evidscore-cutoff"))
		{
			SAXBuilder bld = new SAXBuilder();
			Document document=null;
			try {
				document = (Document) bld.build(new StringReader(gateway));
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			//grabbing docrefs from gateway request and putting it into a StringBuilder
			Element resultList = document.getRootElement();
			Element fedtask = resultList.getChild("fedtask");
			Element result = fedtask.getChild("result");
			Element hitlist = result.getChild("hitlist");
			@SuppressWarnings("rawtypes")
			Iterator listIt = hitlist.getChildren("hit").iterator();
		 
			boolean b = false;
			//grab all docrefs
//			TODO: get rid of boolean b and checks, enable 
			while (listIt.hasNext()) 
			{
				Element hit = (Element) listIt.next();
				String docref = hit.getChildText("docref");
				if(b == true)
				{
					docrefs.append(",");
				}
				docrefs.append(docref);
				b = true;
			}
//			FIXME: the above loop has d1,d2,d3, ... dLast  Fortunately, below #toString#sustring is ignored. 
			docrefs.toString().substring(0,docrefs.length()-1);
			String [] docrefArr = docrefs.toString().split(",");
			ArrayList<String> drArr = new ArrayList<String>();
			 
			//make an array with score + docref
			for(int i = 0; i<docrefArr.length;i++) 
			{
				String docref = docrefArr[i];
				for(int x = 0; x< arr.length;x++)
				{
					String [] arrSplit = arr[x].split(" ");
					if(docref.contains(arrSplit[1]))
					{
						drArr.add(arrSplit[0]+" "+docref);
					}
				}
			}

			//build <personsearch> section
			evidencescore.append("\n<personsearch>");
			//for loop through all docrefs and scores
			for(int x = 0; x < drArr.size();x++) 
			{
				String[] dr = drArr.get(x).split(" ");
				String rankevid = dr[0];
				String docref = dr[1];
				String unq = dr[1].substring(dr[1].indexOf("/") + 1, dr[1].indexOf("/") + 17);
				
				if(queryInfo.contains("evidscore-cutoff"))
				{
					if(Integer.parseInt(rankevid) >= scorelimit) 
					{
						evidencescore.append("\n<result docref=\""+docref+"\" unq=\""+unq+"\">\n");
						evidencescore.append("<rank_evidence>"+rankevid+"</rank_evidence>");
						//add <values> block
						if(docs.containsKey(rankevid +" "+ unq))
						{
							evidencescore.append("\n<values>\n");
							Map<String, Integer> val = docs.get(rankevid +" "+ unq).get(0).getEvidence();
							Set<String> value = val.keySet();
							Iterator <String> valIt = value.iterator();

							while(valIt.hasNext())
							{
								String phrase = valIt.next();

								evidencescore.append("<value count=\""+ val.get(phrase) +"\" type=\"");
								//new
								String [] phraseSplit = phrase.split("%");
								String phraseActual = phraseSplit[0];
								String phraseType = phraseSplit[1];
								
								evidencescore.append(phraseType  +"\">"+ phraseActual +"</value>\n");
							}
							//evidencescore.append(docs.get(rankevid +" "+ unq).get(0).getUnq());
							evidencescore.append( "</values>\n");
						}
						evidencescore.append("</result>");
					}	
				}
				else
				{
					evidencescore.append("\n<result docref=\""+docref+"\" unq=\""+unq+"\">\n");
					evidencescore.append("<rank_evidence>"+rankevid+"</rank_evidence>");
					//add <values> block
					if(docs.containsKey(rankevid +" "+ unq))
					{
						evidencescore.append("\n<values>\n");
						Map<String, Integer> val = docs.get(rankevid +" "+ unq).get(0).getEvidence();
						Set<String> value = val.keySet();
						Iterator <String> valIt = value.iterator();

						while(valIt.hasNext())
						{
							String phrase = valIt.next();

							evidencescore.append("<value count=\""+ val.get(phrase) +"\" type=\"");
							//new
							String [] phraseSplit = phrase.split("%");
							String phraseActual = phraseSplit[0];
							String phraseType = phraseSplit[1];
							
							evidencescore.append(phraseType  +"\">"+ phraseActual +"</value>\n");
						}
						//evidencescore.append(docs.get(rankevid +" "+ unq).get(0).getUnq());
						evidencescore.append( "</values>\n");
					}
					evidencescore.append("</result>");
				}
			}
			evidencescore.append("</personsearch>\n");
			
			if(queryInfo.contains("evidencescore")) {
				gateway=gateway.replace("</resultList>", evidencescore+"</resultList>");
				return gateway;
			}
		}

		//gateway search set up --------------------------------------------------------------------------------------------------------
		if ((queryInfo.contains("product") && !queryInfo.contains("evidencescore")) || queryInfo.contains("evidscore-cutoff")) 
		{
				//grab scorelimit if user has passed it in through form
				if(queryInfo.contains("%26scorelimit"))
				{
					creds = queryInfo.split("%26");
					for (int i = 0; i < creds.length; i++) {
						if(creds[i].contains("scorelimit")) {
							scorelimit=Integer.parseInt(creds[i].substring(creds[i].indexOf("scorelimit%3D")+13, creds[i].indexOf("scorelimit%3D")+14).trim());
						}
					}
				}
				else if (queryInfo.contains("&scorelimit"))
				{
					creds= queryInfo.split("&");
					for (int i = 0; i < creds.length; i++) {
						if(creds[i].contains("scorelimit")) {
							scorelimit=Integer.parseInt(creds[i].substring(creds[i].indexOf("scorelimit=")+11, creds[i].indexOf("scorelimit=")+12).trim());
						}
					}
				}
			
			//need to add if statement and rearrange if we want to give user capability to set cutoff score from
			if(!queryInfo.contains("maxhits")) 
			{
				bucket.append(queryInfo.substring(0, queryInfo.indexOf("&p_field_primary-0"))+"&p_field_unq-0=unq&maxhits=-120&p_text_unq-0=");
			}
			else if(queryInfo.contains("maxhits=-")) 
			{
				bucket.append(queryInfo.substring(0, queryInfo.indexOf("&p_field_primary-0"))+"&p_field_unq-0=unq&p_text_unq-0=");
			}
			else if(queryInfo.contains("maxhits=")) 
			{
				queryInfo = queryInfo.replace("maxhits=", "maxhits=-");
				bucket.append(queryInfo.substring(0, queryInfo.indexOf("&p_field_primary-0"))+"&p_field_unq-0=unq&p_text_unq-0=");
			}
			bucketNone.append(queryInfo.substring(0, queryInfo.indexOf("&p_field_primary-0"))+"&p_field_unq-0=unq&p_text_unq-0=none");

			//divide into buckets >= 4 (possibly 3)
			for(int x = 0; x < arr.length;x++) 
			{
				theScore = Integer.parseInt(arr[x].substring(0, arr[x].indexOf(" ")).trim());
				//else user inputs buck number
				if(theScore >= scorelimit /*&& scoreParam == true*/) 
				{
					bool0 = true;
					if(flag == true) 
					{
						bucket.append("|");
					}
					flag = true;
					
					String unq = arr[x].substring(arr[x].indexOf(" "), arr[x].length()).trim();
					bucket.append(unq);
					//kLogger.debug("UNQs: "+bucket.toString());				
				}
			}
			//add original triplets for sorting
			bucket.append(queryInfo.substring(queryInfo.indexOf("&p_field_primary-0"), queryInfo.indexOf("&searchLogtf=true")+17));
			if(queryInfo.contains("evidscore-cutoff")) 
			{
				bucket.append("&sortfield=YMD_date:D");
			}
			
			//build UNQs into triplet data and generate gateway call to grab ordered UNQS
			GatewaySearch gs = new GatewaySearch();
			String s = null;
			
			if(bool0 == true)
			{
				s = gs.getURL(bucket.toString(),"fed");
			}
			else //gatewaySearch for empty bucket, if no doc(s) in high bucket
				s = gs.getURL(bucketNone.toString(),"fed");
				
			int theQueryIndex = s.indexOf('?');
			gatewayResult = gs.getDocument(s.substring(0, theQueryIndex), s.substring(theQueryIndex+1));
			
			if(queryInfo.contains("evidscore-cutoff")) {
				gatewayResult=gatewayResult.replace("</resultList>", evidencescore+"</resultList>");
			}
			return gatewayResult;
		}
		else
			return evidence.toString();
			//return scores.toString(); //debug
	}
	
	//see if any of key ONEAR terms are in the docs
	public static int onearMatch(int score, boolean scoretwo, String fileString, List<String> words, StringBuilder evidence, 
			Map <String, Integer> evid)
	{
		fileString=fileString.toUpperCase();
		
		List<String> slist = new ArrayList<String>();
		for(int i = 0; i < words.size(); i++) 
		{
			//Pattern p =null;
			//Matcher m =null;
			String wrd = words.get(i);
			boolean pass = false;
			
			if(wrd.contains("nearnear")) 
			{
				String [] str=wrd.split("nearnear");
				Pattern p = Pattern.compile(str[0].trim().toUpperCase()+" (?:\\s*[a-zA-Z]+){1,2} "+ str[1].trim().toUpperCase());
				Matcher m = p.matcher(fileString);
				int count = 0;
				
				while(m.find()) 
				{ 
					count ++;
					slist.add(m.group());
					if(pass == false) {
						evidence.append(m.group());
					}
					pass = true;
					evid.put(m.group(), 1);
					//add evidence to list or map
				}	
				if(count != 0) 
				{
					evidence.append(": "+count + "\n");
				}
			}
			else if(wrd.contains("neeaarr")) 
			{
				String wrd1 = wrd.replace("neeaarr", "");
				String [] str=wrd1.split("qmark1");
				Pattern p = Pattern.compile(str[0].trim().toUpperCase()+" /w{1} "+ str[1].trim().toUpperCase());
				Matcher m = p.matcher(fileString);
				int count = 0;

				while(m.find()) 
				{ 
					count ++;
					slist.add(m.group());
					if(pass == false) {
						evidence.append(m.group());
					}
					pass = true;
					evid.put(m.group(), 1);
					//add evidence to list or map
				}	
				if(count != 0) 
				{
					evidence.append(": "+count + "\n");
				}
			}
		}
		LinkedHashSet<String> lhSetNumbers = new LinkedHashSet<String>(slist);
		for(int a =0; a < lhSetNumbers.size(); a++) 
		{
			if(scoretwo==true)
			{
				score = score + 2;
			}
			else 
				score = score + 1;
		}
		
		return
			score;
	}
	
	//see if any of key 'regular' terms are in the docs -> in progress
	public static int wordMatch(int score, boolean scoretwo, String fileString, List<String> words, StringBuilder evidence, 
			Map <String, Integer> evid, String type)		
	{
		int idx = 0;
		int wordCount = 0;
		
		// normalize to upper case for matching
		fileString=fileString.toUpperCase();
		
		//kLogger.debug("word count: " + words.size());
		
		// iterate over all the words
		for (String target : words)
		{
			//String target = words.get(a);
			int count = 0;
			int c = 0;
			if(!StringUtils.isEmpty(target) && !StringUtils.isEmpty(fileString)) 
			{
				wordCount++;
				// normalize to upper case for matching
				String targetUC = target.toUpperCase();
				while ((idx = fileString.indexOf(targetUC, idx)) >= 0) 
				{
					c++;
					count=1;
					idx += target.length();
				}
				if(count==1) 
				{
					if(scoretwo==true) 
					{
						score= score + 2;
					}
					else
						score= score + 1;
				}
			}
			if(count != 0) 
			{
				evidence.append(target+"%"+type+ ": " + c + "\n");
				evid.put(target+"%"+type, c);
			}
		}
		//kLogger.debug("words evaluated: " + wordCount);
		
		return 
			score;
	}
	
	//break up data inside triplets into terms  
	public static List<String> terms(String [] st) 
	{
		String word = null;
		List<String> words = new ArrayList<String>();
		for (int i= 0; i < st.length;i++) 
		{
			word = st[i];
			String w = null;
			if(word.contains("\""))
			{
				w = word.replace("\"", "");
				word=null;
				word= w;
			}
			words.add(word.trim());
		   	}
		return words;
	}
	public static void elmWithin(Element node, StringBuilder sb) 
	{
		if(node.getChildren() != null) 
		{
			@SuppressWarnings("unchecked") 
			List<Element> subList = node.getChildren();
			Iterator<Element> it = subList.iterator();

			//iterate through children list			
			while(it.hasNext()) 
			{
				Element e = it.next();
				sb.append(e.getText());
			}
		}
	}
	
	//sort array with unqs by their score
	public static void sort(String [] arr) 
	{
		// took out for now, may need to be back in
		//Integer.parseInt(arr[0].substring(0, arr[0].indexOf(" ")));

		for(int i=0;i<arr.length-1;i++)
		{
		    for(int j=i+1;j<arr.length;j++)
		    {
		        if(Integer.parseInt(arr[i].substring(0, arr[i].indexOf(" ")))
		        		< Integer.parseInt(arr[j].substring(0, arr[j].indexOf(" "))))
		        {
		        		String temp = arr[i];
		        		arr[i] = arr[j];
		        		arr[j] = temp;
		        }
		    }
		}
	}

	@Override
	public PermutedResult convert(String inData, String inFormat, Map <String,String> options) throws Throwable 
	{
		String docInfo = null;
		String queryInfo;
		String gateway = null;

		SAXBuilder builder1 = new SAXBuilder();
		Document document1 = (Document) builder1.build(new StringReader(inData));
		Element rootNode = document1.getRootElement(); 

		//still getting it from the test file 
		if(rootNode.getChildText("doc") != null) 
		{
			docInfo = "<docs>"+rootNode.getChildText("doc")+ "</docs>";
		}
		else if(rootNode.getChildText("id") != null) 
		{
			String c = null;
			
			StringBuilder sb = new StringBuilder();
			String ID = rootNode.getChildText("id");
			
			//can change DocFetch to gateway call for all UNQs if speed needed
			if (ID.contains(",")) 
			{
				String [] st = ID.split(",");
				for (int i= 0; i < st.length;i++) 
				{
					String id = st[i].trim();
					kLogger.debug("ID: "+id);
					DocFetch docFetch = new DocFetch();
					String url = docFetch.getURL(id);
					docInfo = docFetch.getDocument(url);
					sb.append(docInfo.substring(43, docInfo.length() - 12));
				}
				
				docInfo= null;
				docInfo = "<docs>" + sb.toString() + "</docs>";
			}

			else 
			{
			DocFetch docFetch = new DocFetch();
			String url = docFetch.getURL(ID);
			//kLogger.debug("url: " + url);
			 
			docInfo = docFetch.getDocument(url);
			c= docInfo.substring(43, docInfo.length() - 12);
			docInfo = null;
			docInfo = "<docs>" + c + "</docs>";
			}
		}

		queryInfo = rootNode.getChildText("query");
		
		@SuppressWarnings("static-access")
		String result = this.process(gateway, docInfo, queryInfo);
		return new PermutedResult("text/plain", result);
	}
}


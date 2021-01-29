package com.newsbank.permuter.types;
import org.jdom.Element;
public class GatewayQuery {

	private String g_query;
	
	public GatewayQuery()
	{}
	
	public GatewayQuery(String inQuery)
	{
		setGatewayQuery(inQuery);
	}
	
	public String getGatewayQuery()
	{
		return g_query;
	}
	
	public void setGatewayQuery(String inQuery)
	{
	if (inQuery != null)
		g_query = inQuery;
	else
		g_query=null;
	}
	
	public void process(Element rootNode)
	{
		if (rootNode.getChild("gateway") != null)
		{
			g_query = rootNode.getChildText("gateway").trim();
			if (rootNode.getChild("scorelimit") != null)
			{
				g_query = g_query + "&scorelimit=" + Integer.parseInt(rootNode.getChildText("scorelimit"));
			}
			if (rootNode.getChild("option") != null)
			{
				g_query = g_query + "&option=" + rootNode.getChildText("option");
			}
		}
		setGatewayQuery(g_query);
	}
	
	public void appendToTrip(StringBuilder trip)
	{
		if (g_query != null) 
		{
			trip.append(g_query);
		}	
	}
}

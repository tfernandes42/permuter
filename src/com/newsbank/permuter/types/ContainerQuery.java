package com.newsbank.permuter.types;


public class ContainerQuery
{
	public static final String kContainerTypeUnknown	=	"UNKNOWN";
	public static final String kContainerTypeDefault	=	"DEFAULT";
	public static final String kContainerTypeObit		=	"OBIT";
	public static final String kContainerTypeCensus		=	"CENSUS";
	
	private String m_container;
	
	public ContainerQuery()
		{
		this(kContainerTypeUnknown);
		}
	
	public ContainerQuery(String inContainer)
		{
		setContainer(inContainer);
		}
	
	public String getContainer()
		{
		return m_container;
		}

	public void setContainer(String inContainer)
		{
		if (inContainer != null)
			m_container = inContainer.toUpperCase();
		else
			m_container=kContainerTypeUnknown;
		}

	public boolean isSet()
		{
		return !kContainerTypeUnknown.equals(getContainer());
		}

	public boolean isDefault()
		{
		return kContainerTypeDefault.equals(getContainer());
		}

	public boolean isObit()
		{
		return kContainerTypeObit.equals(getContainer());
		}

	public boolean isCensus()
		{
		return kContainerTypeCensus.equals(getContainer());
		}

	public String process(String inTriplets)
	{
		String tpl=inTriplets;
		
		String container = "\n"+tpl;
		//===================================================
		//start building container-query add on
		//===================================================
		//if(containerQuery != null && !containerQuery.toUpperCase().equals("DEFAULT"))
		if (isSet() && !isDefault())
		{	
			container = container.replace("alltext", "allfs");
			container = container.replace("primary-", "primary-10");
			container = container.replace("second-", "second-10");
			//change allfs to personName in obit and census 
			container = container.replace("&p_field_primary-100=allfs","&p_field_primary-100=personName");
			container = container.replace("&p_field_primary-101=allfs","&p_field_primary-101=personName");
			//container = container.replace("&p_field_primary-102=allfs","&p_field_primary-102=personName");
			//container = container.replace("&p_field_primary-103=allfs","&p_field_primary-103=personName");
			container = container.replace("&p_field_second-105=allfs","&p_field_second-105=personName");
			container = container.replace("&p_field_second-106=allfs","&p_field_second-106=personName");
			container = container.replace("&p_field_second-109=dece","&p_field_second-109=personName");
			//container = container.replace("&p_field_second-107=allfs","&p_field_second-107=personName");
			//container = container.replace("&p_field_second-108=allfs","&p_field_second-108=personName");

			//hhubValue
			container = container.replace("&p_field_second-1011=deceVal","&p_field_second-1011=personNameGivenVal");
			container = container.replace("second-1011","second-111");
			
			container = container + "&p_bool_primary-100=OR&p_bool_second-100=OR";
			//if(containerQuery.toUpperCase().equals("OBIT")) 
			if(isObit()) 
			{
				//add container section
				if (container.contains("primary-100"))
				{
					container = container + "&p_container_primary-100=person:1:obit";
				}
				if (container.contains("primary-101"))
				{
					container = container + "&p_container_primary-101=person:1:obit";
				}
				if (container.contains("second-105"))
				{
					container = container + "&p_container_second-105=person:1:obit";
				}	
				if (container.contains("second-106"))
				{
					container = container + "&p_container_second-106=person:1:obit";
				}
				if (container.contains("second-111"))
				{
					container = container + "&p_container_second-111=person:1:obit";
				}
				if (container.contains("second-109"))
				{
					container = container + "&p_container_second-109=person:1:obit";
				}
				tpl = tpl.replace("field_primary-0=alltext", "field_primary-0=dece");
				tpl = tpl.replace("field_primary-1=alltext", "field_primary-1=dece");
				tpl = tpl.replace("field_primary-2=alltext", "field_primary-2=dece");
				tpl = tpl.replace("field_primary-3=alltext", "field_primary-3=dece");
				tpl = tpl.replace("field_second-5=alltext", "field_second-5=dece");
				tpl = tpl.replace("field_second-6=alltext", "field_second-6=dece");
				tpl = tpl.replace("field_second-7=alltext", "field_second-7=dece");
				tpl = tpl.replace("field_second-8=alltext", "field_second-8=dece");
			}
			else if(isCensus()) 
			{
				//add container section
				if (container.contains("primary-100"))
				{
					container = container + "&p_container_primary-100=person:1:census";
				}
				if (container.contains("primary-101"))
				{
					container = container + "&p_container_primary-101=person:1:census";
				}
				if (container.contains("second-105"))
				{
					container = container + "&p_container_second-105=person:1:census";
				}	
				if (container.contains("second-106"))
				{
					container = container + "&p_container_second-106=person:1:census";
				}
				if (container.contains("second-111"))
				{
					container = container + "&p_container_second-111=person:1:census";
				}
				if (container.contains("second-109"))
				{
					container = container.replace("&p_field_second-109=allfs","&p_field_second-109=personName");
					container = container + "&p_container_second-109=person:1:census";
				}
			}
			
			//remove primary-102,primary-103,second-102 (second-103 removed due to the removal of second-3)
			if(container.contains("&p_bool_primary-102="))
			{
				String prim102 = container.substring(container.indexOf("&p_bool_primary-102="), container.indexOf(")&", container.indexOf("primary-102=("))) + ")";
				container = container.replace(prim102,"");
			}
			if(container.contains("&p_bool_primary-103="))
			{
				String prim103 = container.substring(container.indexOf("&p_bool_primary-103="), container.indexOf(")&", container.indexOf("primary-103=("))) + ")";
				container = container.replace(prim103,"");
			}
			if(container.contains("&p_bool_second-102="))
			{
				String sec102 = container.substring(container.indexOf("&p_bool_second-102="), container.indexOf(")&", container.indexOf("second-102=("))) + ")";
				container = container.replace(sec102,"");
			}
			if(container.contains("&p_bool_second-107="))
			{
				String sec107 = container.substring(container.indexOf("&p_bool_second-107="), container.indexOf(")&", container.indexOf("second-107=("))) + ")";
				container = container.replace(sec107,"");
			}
			if(container.contains("&p_bool_second-108="))
			{
				String sec108 = container.substring(container.indexOf("&p_bool_second-108="), container.indexOf(")&", container.indexOf("second-108=("))) + ")";
				container = container.replace(sec108,"");
			}
			tpl = tpl + container;		
		}
		return tpl;
	}
	
}

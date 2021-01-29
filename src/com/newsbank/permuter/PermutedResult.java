package com.newsbank.permuter;

public class PermutedResult
	{
	private final static String kDefaultContentType	=	"text/plain";
	
	private String m_contentType;
	private String m_data;
	
	public PermutedResult()
		{
		this(kDefaultContentType, "");
		}
	
	public PermutedResult(String inContentType, String inData)
		{
		setContentType(inContentType);
		setData(inData);
		}

	public String getContentType()
		{
		return m_contentType;
		}

	public void setContentType(String inContentType)
		{
		m_contentType = inContentType;
		}

	public String getData()
		{
		return m_data;
		}

	public void setData(String inData)
		{
		m_data = inData;
		}
	}

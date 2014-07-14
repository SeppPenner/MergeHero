/*
 * MergeHero: Differing and Merging Folders & Files
 *
 * Copyright © 2004, Dynamsoft, Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.opensource.org/licenses/gpl-3.0.html.
 */

/*
 * Created on 2004-12-23
 */
package com.dynamsoft.sourceanywhere;


/**
 * @author Falcon Young
 */ 
public class BaseDataObject 
{
    public static final int BYTE_LENGTH = 1;
    public static final int SHORT_LENGTH = 2;
    public static final int INT_LENGTH = 4;
    public static final int LONG_LENGTH = 8;
    public static final int FLOAT_LENGTH = 4;
    public static final int DOUBLE_LENGTH = 8;


	private int m_iValue;
	private byte m_byValue;
	private short m_sValue;
	private boolean m_bValue;
	private float m_fValue;
	private double m_dbValue;
	private String m_strValue;
	private long m_lValue;
	
	
	public BaseDataObject()
	{
		m_iValue = 0;
		m_byValue = 0;
		m_sValue = 0;
		m_bValue = false;
		m_fValue = 0;
		m_dbValue = 0;		
		m_strValue = "";
		m_lValue = 0;
	}
	
	
	// int 
	public void setIntValue(int iValueToSet)
	{
		this.m_iValue = iValueToSet ;
	}
	public int getIntValue()
	{
		return m_iValue;
	}
	
	
	// byte
	public void setByteValue(byte byValueToSet)
	{
		this.m_byValue = byValueToSet ;
	}
	public byte getByteValue()
	{
		return m_byValue;
	}
	
	
	// short
	public void setShortValue(short sValueToSet)
	{
		this.m_sValue = sValueToSet;
	}
	public short getShortValue()
	{
		return m_sValue;
	}
	
	
	// boolean
	public void setBooleanValue(boolean bValueToSet)
	{
		this.m_bValue = bValueToSet;
	}
	public boolean getBooleanValue()
	{
		return m_bValue;
	}
	
	
	//set and get float
	public void setFloatValue(float fValueToSet)
	{
		this.m_fValue = fValueToSet;
	}
	public float getFloatValue()
	{
		return m_fValue;
	}
	
	
	//	set and get float
	public void setDoubleValue(double dbValueToSet)
	{
		this.m_dbValue = dbValueToSet;
	}
	public double getDoubleValue()
	{
		return m_dbValue;
	}
	
	
	//	set and get String
	public void setStringValue(String strValueToSet)
	{
		this.m_strValue = strValueToSet;
	}
	public String getStringValue()
	{
		return m_strValue;
	}
	
	
	//  set and get long
	public void setLongValue(long lValueToSet)
	{
		this.m_lValue = lValueToSet;
	}
	public long getLongValue()
	{
		return m_lValue;
	}
}

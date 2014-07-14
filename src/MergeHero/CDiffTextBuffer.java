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

package MergeHero;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import MergeHeroLib.SLineInfo;

import com.dynamsoft.sourceanywhere.BaseDataObject;

/*
 * Created on 2005-4-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CDiffTextBuffer extends CGhostTextBuffer
{

    public static void main(String[] args)
    {
    }
    
    protected CDiffTextBuffer()
    {
        super();
    }
    
    public CDiffTextBuffer(String strFilePath)
    {
        super(strFilePath);
    }
    
    public static final int SIZE_ARRAY_GROW	= 500;
 	public int m_enumBufferType = 0; 
	public String m_strTempPath = "";
	
	
	public void SetDiffTextBuffer(int enumBufferType)
	{
		m_enumBufferType = enumBufferType;
	}

	public void SetModified(boolean bModified)
	{
		super.SetModified(bModified);
	}
	
	public void OnNotifyLineHasBeenEdited(int nLine)
	{
		SetLineFlag(nLine, MERGE_LINEFLAGS.LF_INSERTED, false, false, false);
		SetLineFlag(nLine, MERGE_LINEFLAGS.LF_TRIVIAL, false, false, false);
		SetLineFlag(nLine, MERGE_LINEFLAGS.LF_DELETED, false, false, false);
		SetLineFlag(nLine, MERGE_LINEFLAGS.LF_CONFLICT, false, false, false);

		super.OnNotifyLineHasBeenEdited(nLine);
	}
	
	public boolean FlagIsSet(int unLine, long dwFlag)
	{
	    SLineInfo li = (SLineInfo)m_aryLineInfo.get(unLine);
		return ((li.m_dwFlags & dwFlag) == dwFlag);
	}

	public boolean GetFullLine(int nLineIndex, BaseDataObject strLine)
	{
		if (GetFullLineLength(nLineIndex) <= 0)
		{
			return false;
		}

		strLine.setStringValue(String.valueOf(GetLineChars(nLineIndex)));

		return true;
	}

	public boolean GetLine(int nLineIndex, BaseDataObject strLine)
	{ 
		int nLineLength = super.GetLineLength(nLineIndex); 
		
		if(nLineLength < 0) 
		{
			return false; 
		}
		else if(nLineLength == 0) 
		{
			strLine.setStringValue(""); 
		}
		else 
		{ 
		    strLine.setStringValue(String.valueOf(super.GetLineChars(nLineIndex)));
		} 

		return true; 
	}
}

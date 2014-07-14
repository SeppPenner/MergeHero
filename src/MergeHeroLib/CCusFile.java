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

package MergeHeroLib;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.dynamsoft.sourceanywhere.BaseDataObject;

/*
 * Created on 2005-4-14
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
public class CCusFile
{
    public static void main(String[] args)
    {
    }
    
    public static final int ERROR_SUCCESS = 0;
	public class SError
	{
		String m_strApiname;
		int m_nSyserrnum; // valid if apiname nonempty
		String m_strDesc; // valid if apiname empty
		boolean hasError() { return m_strApiname.length()!= 0 || m_strDesc.length() != 0; }
		void ClearError() { m_strApiname = ""; m_nSyserrnum = ERROR_SUCCESS; m_strDesc = ""; }
		SError() { ClearError(); }
	}

	public boolean OpenReadOnly(String pszFilename)
	throws UnsupportedEncodingException, FileNotFoundException, IOException
	{
	    return true;
	}
	public void Close() throws IOException
	{
	}

	public boolean IsOpen()
	{
	    return true;
	}

	public String GetFullyQualifiedPath()
	{
	    return "";
 	}

	public SError GetLastUniError()
	{
	    return null;
	}

	public boolean ReadBom()
	{
	    return true;
	}	

	public String GetUnicoding()
	{
	    return "";
	}
	
	public void SetUnicoding(int nUnicoding)
	{
	}

	public int GetCodepage()
	{
	    return 0;
	}
	        
	public void SetCodepage(int nCcodepage)
	{
	}

	public boolean ReadString(BaseDataObject strLine)throws IOException
	{
	    return true;
	}
	
	public boolean ReadString(BaseDataObject strLine, BaseDataObject strEol) throws IOException
	{	
	    return true;
	}

	public int GetLineNumber()
	{
	    return 0;
	}
	
	public long GetPosition()
	{	    
	    return 0;
	}

	public boolean WriteString(String strLine) throws IOException
	{
	    return true;
	}

	public class STxtstats
	{
		public int m_ncrs;
		public int m_nlfs;
		public int m_ncrlfs;
		public int m_nzeros;
		public int m_nlosses;
		public STxtstats() { clear(); }
		public void clear() { m_ncrs = m_nlfs = m_ncrlfs = m_nzeros = m_nlosses = 0; }
	}

	public STxtstats GetTxtStats()
	{
	    return null;
	} 
}

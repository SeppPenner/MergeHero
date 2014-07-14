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
 * Created on 2005-4-14
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHeroLib;
import java.io.File;
/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CCusLocalFile extends CCusFile
{
    public static void main(String[] args)
    {
    }
    
    public static final String US_ASCII = "US-ASCII"; 
    public static final String UTF_8 = "UTF-8"; 
    public static final String UTF_16BE = "UTF-16BE"; 
    public static final String UTF_16LE = "UTF-16LE"; 
    
    protected int m_nStatusFetched = 0; // 0 not fetched, -1 error, +1 success
	protected CFileStatus m_filestatus = new CFileStatus();
	protected long m_nFilesize = 0;
	protected String m_strFilepath = "";
	protected String m_strFilename = "";
	protected int m_nLineno = 0; // current 0-based line of m_current
	protected SError m_lastError = new SError();
	protected boolean m_bReadbom = false; // whether have tested for BOM
	protected String m_nUnicoding = ""; // enum UNICODESET in unicoder.h
	protected int m_nCharsize = 0; // 2 for UCS-2, else 1
	protected int m_nCodepage = 0; // only valid if m_unicoding==ucr::NONE;
	protected STxtstats m_txtstats = new STxtstats();   
	
//	 Create disconnected CCusLocalFile, but with name
	CCusLocalFile()
	{
		Clear();
	}

//	 Reset all variables to empty
	public void Clear()
	{
		m_nStatusFetched = 0;
		ClearFilestatus(m_filestatus);
		m_nFilesize = 0;
		m_strFilepath = "";
		m_strFilename = "";
		m_nLineno = -1;
		m_bReadbom = false;
		m_nUnicoding = US_ASCII;
		m_nCharsize = 1;
		m_nCodepage = 0;//GetDefaultCodepage();
		m_txtstats.clear();
	}

	public void ClearFilestatus(CFileStatus fileStatus)
	{
		fileStatus.m_ctime = 0;
		fileStatus.m_mtime = 0;
		fileStatus.m_atime = 0;
		fileStatus.m_size = 0;
		fileStatus.m_attribute = 0;
		fileStatus.m_szFullName = "";
	}
	
//	 Get file status into member variables
	public boolean DoGetFileStatus(String strFile)
	{
		m_lastError.ClearError();
		m_nStatusFetched = -1;

		File file = new File(strFile);
		
		if (!file.exists())
		{
			LastError("File doesn't exist!", 0);
			return false;
		}
		m_strFilepath = file.getPath();
		m_nFilesize = file.length();
		m_nStatusFetched = 1;

		return true;
	}

//	 Record an API call failure
	public void LastError(String lpszApiname, int nSyserrnum)
	{
		m_lastError.ClearError();

		m_lastError.m_strApiname = lpszApiname;
		m_lastError.m_nSyserrnum = nSyserrnum;
	}

//	 Record a custom error
	public void LastErrorCustom(String pszDesc)
	{
		m_lastError.ClearError();
		m_lastError.m_strDesc = pszDesc;
	}	
 
	public String GetFullyQualifiedPath() { return m_strFilepath; }
	public CFileStatus GetCusFileStatus() { return m_filestatus; }

	public SError GetLastUniError() { return m_lastError; }

	public String GetUnicoding() { return m_nUnicoding; }
	public void SetUnicoding(String nUnicoding) { m_nUnicoding = nUnicoding; }

	public int GetCodepage() { return m_nCodepage; }
	public void SetCodepage(int nCodepage) { m_nCodepage = nCodepage; }

	public int GetLineNumber() { return m_nLineno; }

	public STxtstats GetTxtStats() { return m_txtstats; }
}

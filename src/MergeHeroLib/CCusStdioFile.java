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
 * Created on 2005-4-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHeroLib;
import java.io.*;
/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CCusStdioFile extends CCusLocalFile
{
    public static void main(String[] args)
    {
    }
    
    private OutputStreamWriter outputStreamWriter = null;
    private String strFile = "";
    private long m_nData = 0;
    
    public void Close()
    {
    	if (IsOpen())
    	{
    	    try
            {
                outputStreamWriter.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    	}
    	m_nStatusFetched = 0;
    	ClearFilestatus(m_filestatus);
    	m_nFilesize = 0;
    	m_nLineno = -1;
    	m_bReadbom = false;
    	m_txtstats.clear();
    } 
    
//  Is it currently attached to a file ?
    public boolean IsOpen()
    {
    	return outputStreamWriter != null;
    }
    
//  Get file status into member variables/
    public boolean GetFileStatus()
    {
    	if (IsOpen())
    	{
    		return false; // unfortunately we'll hit our lock
    	}

    	return DoGetFileStatus(strFile); // DoGetFileStatus must open the file itself
    }
    
    public boolean OpenCreate(String strFilename) 
    	throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        Close();
        strFile = new String(strFilename);
        File file = null;
        
        try
        {
            file = new File(strFile);
            file.createNewFile();
            if (!file.canWrite())
            {
                LastErrorCustom("Can't write the destionation file.");
                return false;
            }
            else if (file.isDirectory())
            {
                LastErrorCustom("The destination file is a directory.");
                return false;
            }
            
           	m_nLineno = 0; // GetFileStatus requires file be "open", which means nonnegative line number
        	if (!GetFileStatus())
        	{
        		return false;
        	}
        	
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            
            return true;
        }
        catch (UnsupportedEncodingException e)
        {
            LastErrorCustom(e.toString());
        }
        catch (FileNotFoundException e)
        {
            LastErrorCustom(e.toString());
        }
        
        return false; 
    }  
    
    public void LastError(String strApiname, int nSyserrnum)
    {
    	m_lastError.ClearError();
    	m_lastError.m_strApiname = strApiname;
    	m_lastError.m_nSyserrnum = nSyserrnum;
    }

    public void LastErrorCustom(String strDesc)
    {
    	m_lastError.ClearError();
    	m_lastError.m_strDesc = strDesc;
    }
    
//  Write BOM (byte order mark) if Unicode file
    public int WriteBom() throws IOException
    {
        int[] bom = new int[3];
     	if (m_nUnicoding.compareToIgnoreCase(UTF_16LE) == 0)
    	{
    		bom[0] = 0xFF;
    		bom[1] = 0xFE;
      		m_nData = 2;
    	}
    	else if (m_nUnicoding.compareToIgnoreCase(UTF_16BE) == 0)
    	{
    	    bom[0] = 0xFE;
    	    bom[1] = 0xFF;
      		m_nData = 2;
    	}
    	else if (m_nUnicoding.compareToIgnoreCase(UTF_8) == 0)
    	{
    	    bom[0] = 0xEF;
    	    bom[1] = 0xBB;
    	    bom[2] = 0xBF;
     		m_nData = 3;
    	}
    	else
    	{
    		m_nData = 0;
        }
     	
     	try
     	{
     	    if (outputStreamWriter == null)
     	    {
     	        return 0;
     	    }
     	    for (int i = 0; i < m_nData; i++)
     	    {
     	        outputStreamWriter.write(bom[i]);
     	    }

     	    return (int)m_nData;
     	}
     	catch (IOException e)
     	{
     	   LastErrorCustom(e.toString());
     	}
  
    	return 0;
    } 
    
//  Write one line (doing any needed conversions)
    public boolean WriteString(String strLine, int iLength) throws IOException
    {
        if (outputStreamWriter == null)
        {
            return false;
        }
        
        try
        {
           outputStreamWriter.write(strLine, 0, iLength);
           return true;
        }
    	catch (IOException e)
     	{
     	   LastErrorCustom(e.toString());
     	}
    	
    	return false;
    }
}

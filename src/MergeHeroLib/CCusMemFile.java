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

import com.dynamsoft.sourceanywhere.BaseDataObject;
/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CCusMemFile extends CCusLocalFile
{
    public static void main(String[] args)
    {
    }
    
    private InputStreamReader inputStream = null;
    private String strFile = "";
    private static final int windows = 0;
    private static final int mac = 1;
    private static final int unix = 2;
    private int iPreviousReaded = -1;
    
    public void Close()throws IOException
    {
    	Clear();
    	
    	if (inputStream != null)
    	{
    	    inputStream.close();
    	}
    } 
    
//  Is it currently attached to a file? 
    public boolean IsOpen()
    {
    	// We don't test the handle here, because we allow "opening" empty file
    	// but memory-mapping doesn't work on that, so that uses a special state
    	// of no handle, but linenumber of 0
    	return strFile.length() != 0;
    } 
    
//  Get file status into member variables
    public boolean GetFileStatus()
    {
    	if (!IsOpen())
    	{
    		return false;
    	}

    	return DoGetFileStatus(strFile);
    }
    
//  Open file for generic read-only access
    public boolean OpenReadOnly(String strFilename) 
    	throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        strFile = new String(strFilename);
        File file = null;
        
        try
        {
            file = new File(strFile);
            if (!file.exists())
            {
                LastErrorCustom("The destination file doesn't exist.");
                return false;
            }
            else if (!file.canRead())
            {
                LastErrorCustom("Can't read the destionation file.");
                return false;
            }
            else if (file.isDirectory())
            {
                LastErrorCustom("The destination file is a directory.");
                return false;
            }
            // Set file readonly
            //file.setReadOnly();
            
        	m_nLineno = 0; // GetFileStatus requires file be "open", which means nonnegative line number
        	if (!GetFileStatus())
        	{
        		return false;
        	}
        	m_nLineno = -1; 
            
            // Check unicode type
            FileInputStream bomReaded = new FileInputStream(file);
            m_nCharsize = 0;
            int nReaded = 0;
        	if (m_nFilesize >= 2)
        	{
        	    byte[] byBom = {'0', '0'};
        	    bomReaded.read(byBom);
        	    nReaded = 2;
        	    
        		if (byBom[0] == (byte)0xFF && byBom[1] == (byte)0xFE)
        		{
        			m_nUnicoding = UTF_16LE;
        			m_nCharsize = 2;
         		}
        		else if (byBom[0] == (byte)0xFE && byBom[1] == (byte)0xFF)
        		{
        			m_nUnicoding = UTF_16BE;
        			m_nCharsize = 2;
         		}
        	}
        	if (m_nFilesize >= 3 && m_nUnicoding.compareToIgnoreCase(US_ASCII) == 0)
        	{
        	    byte[] byBom = {'0', '0', '0'};        	    
        	    bomReaded.skip(-nReaded);
        	    bomReaded.read(byBom);
        	    nReaded = 3;
        	    
        		if (byBom[0] == (byte)0xEF && byBom[1] == (byte)0xBB && byBom[2] == (byte)0xBF)
        		{
        			m_nUnicoding = UTF_8;
        			m_nCharsize = 3;
        		}
        	}
        	m_bReadbom = true;            
            
        	bomReaded.skip(-nReaded);
        	bomReaded.skip(m_nCharsize);
            inputStream = new InputStreamReader(bomReaded, m_nUnicoding);
            m_nLineno = 0;
            
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
        catch (IOException e)
        {
            LastErrorCustom(e.toString());
        }       
        
        return false;
   } 
    
//  Read one (DOS or UNIX or Mac) line. Do not include eol chars.
    public boolean ReadString(BaseDataObject strLine) throws IOException
    {
        BaseDataObject strEol = new BaseDataObject();
    	boolean bRet = ReadString(strLine, strEol);
    	return bRet;
    }  
    
//  Read one (DOS or UNIX or Mac) line
    public boolean ReadString(BaseDataObject strLine, BaseDataObject strEol) throws IOException
    {
    	strLine.setStringValue("");
    	strEol.setStringValue("");
    	String strTempValue = "";
    	String strTempEol = "";
    	int eol = -1;
    	
    	try
        {
            if (!inputStream.ready())
            {
            	if (iPreviousReaded != -1)
            	{// May be the last line has been readed
            		strLine.setStringValue(String.valueOf((char)iPreviousReaded));
            		iPreviousReaded = -1;
            	}
            	
                LastErrorCustom("The input stream is invalid.");
                return false;
            }
            
            int iReaded = 0;
            eol = -1;
            while(true)
            {
            	if (iPreviousReaded != -1)
            	{
            		strTempValue += String.valueOf((char)iPreviousReaded);
            		iPreviousReaded = -1;
            	}
            	
                iReaded = inputStream.read();
                if (iReaded <= -1)
                {
                    break;
                }
                
                if (iReaded == '\r')
                {
                    eol = mac;
                    
                    iReaded = inputStream.read();
                    if (iReaded == -1)
                    {
                        break;
                    }
                    
                    if (iReaded == '\n')
                    {
                        eol = windows;
                    }
                    else
                    {
                    	iPreviousReaded = iReaded;
                    }
                }
                else if (iReaded == '\n')
                {
                    eol = unix;
                }
                else if (iReaded == 0)
                {
                    ++m_txtstats.m_nzeros;
                    return true;
                }
                else
                {
                    strTempValue += String.valueOf((char)iReaded);
                }
               
                // Set eol
                if (eol != -1)
                {
                    if (eol == windows)
                    {
                        strTempEol += "\r\n";
                        m_txtstats.m_ncrlfs ++;
                    }
                    else if (eol == mac)
                    {
                        strTempEol += "\r";
                        m_txtstats.m_ncrs ++;
                    }
                    else
                    {
                        strTempEol += "\n";
                        m_txtstats.m_nlfs ++;
                    }
                    
                    strEol.setStringValue(strTempEol);
                    strLine.setStringValue(strTempValue);
                    ++m_nLineno;
                    
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            LastErrorCustom(e.toString());
            e.printStackTrace();
        }
        
        // Set eol
        if (eol != -1)
        {
            if (eol == windows)
            {
                strTempEol += "\r\n";
                m_txtstats.m_ncrlfs ++;
            }
            else if (eol == mac)
            {
                strTempEol += "\r";
                m_txtstats.m_ncrs ++;
            }
            else
            {
                strTempEol += "\n";
                m_txtstats.m_nlfs ++;
            }
            
            strEol.setStringValue(strTempEol);
        }  
        strLine.setStringValue(strTempValue);
        ++m_nLineno;
    	
    	return true;
    }
}

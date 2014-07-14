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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;

import MergeHeroLib.CDiffMergeBlockArray;
import MergeHeroLib.CDiffOptions;
import MergeHeroLib.CDynamicDirectoriesCompare;
import MergeHeroLib.CDynamicFilesDiffAndMerge;
import MergeHeroLib.CDynamicDirectoriesCompare.CDiffItemArray;

import com.dynamsoft.sourceanywhere.BaseDataObject;
import com.dynamsoft.sourceanywhere.StringArray;


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
public class MergeHeroApp
{
    public static void main(String[] args)throws UnsupportedEncodingException,
    FileNotFoundException, IOException
    {
     	if (theApp != null)
    	{
    		theApp.dispose();
    	}

     	new MergeHeroApp();
   	    	
        if (!theApp.InitInstance(args))
        {
        	theApp.dispose();
        }
    }
    
    public MergeHeroApp()
    {
    	theApp = this;
    	
    	assert(theApp != null);
    }
    
    public void DiffAndMerge(String[] args)
    {
    	assert(theApp != null);
    	
    	CDiffAndMergeThread diffAndMergeThread = new CDiffAndMergeThread(args);
    	diffAndMergeThread.start();
    	
   	   	while (true)
	   	{
	   		try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   		
			//System.out.println("dispose value " + IsDisposed());
	   		if (IsDisposed())
	   		{
	   			break;
	   		}
	   	}	
    }
    
    private class CDiffAndMergeThread extends Thread
	{
    	private String[] m_args = null;
    	public CDiffAndMergeThread(String[] args)
    	{
    		m_args = args;
    	}
    	
    	public void run()
    	{
    	   	if (!InitInstance(m_args))
        	{
        		dispose();
        	}   
    	}
	}
    
    private boolean IsDisposed()
	{
    	return IsMainFrmDisposed();
	}
 
    private MainJFrame mainFrame = new MainJFrame();
    public static MergeHeroApp theApp = null;
    public final int CHAR_ALIGN = 16;
	public boolean m_bDirDiff = false;
	public long m_dwFlags = 0;
	public String m_strError = null;
	public StringArray m_aryFiles = new StringArray();
	public boolean m_bIsMerge = true;

	// Files compare
	public CDiffTextBuffer m_LocalTextBuf = new CDiffTextBuffer(); 
	public CDiffTextBuffer m_ServerTextBuf = new CDiffTextBuffer();
	public CDiffTextBuffer m_MergeTextBuf = new CDiffTextBuffer();
	public CDiffTextBuffer m_OrgTextBuf = new CDiffTextBuffer();
	public CDiffMergeBlockArray m_aryMergedBlock = new CDiffMergeBlockArray();
	public int m_nMergedBlockIndex = 0;
	public CDynamicFilesDiffAndMerge m_fileDiff = new CDynamicFilesDiffAndMerge();
	
	// Directory compare 
	CDynamicDirectoriesCompare m_dirDiff = new CDynamicDirectoriesCompare();	
	public String m_strCompareDir = "";
	public String m_strToDir = "";
	public boolean m_bRecursive = false;
	public CDiffItemArray m_aryDiffItemArray = m_dirDiff.new CDiffItemArray(); 
	
	private String m_strCaption = Common.TITLE_CAPTION;
	private boolean m_bInitSuccess = false;
	private int m_nCountConflict = 0;
	
	public CDiffOptions m_diffOptions = new CDiffOptions();
	public String m_strLeftLabel = "";
	public String m_strRightLabel = "";
	public boolean m_bDiffBinary = false;
	
	public int ALIGN_BUF_SIZE(int size)
	{
	    return ((size) / CHAR_ALIGN) * CHAR_ALIGN + CHAR_ALIGN;
	}
	
	public void dispose()
	{
		if (mainFrame != null)
		{
			mainFrame.dispose();
		}
	}
	
	public void Init()
	{
	    m_LocalTextBuf.FreeAll();
	    m_ServerTextBuf.FreeAll();
	    m_MergeTextBuf.FreeAll();
	    m_OrgTextBuf.FreeAll();
		m_LocalTextBuf.SetDiffTextBuffer(EnumBufferType.LOCAL_BUFFER);
		m_ServerTextBuf.SetDiffTextBuffer(EnumBufferType.SERVER_BUFFER);
		m_MergeTextBuf.SetDiffTextBuffer(EnumBufferType.MERGE_BUFFER);
		m_OrgTextBuf.SetDiffTextBuffer(EnumBufferType.ORIGINAL_BUFFER);

		m_dwFlags = 0;
		m_nCountConflict = 0;
		m_aryMergedBlock.clear();
		m_nMergedBlockIndex = -1;
		m_bDirDiff = false;
		m_bRecursive = false;
		
		m_bDiffBinary = false;
		m_strLeftLabel = "";
		m_strRightLabel = "";
	}
	
	private boolean InitInstance(String[] args)
	{
		Init();
		
		mainFrame.createAndShowGUI(mainFrame);
		
		if (args.length > 1)
		{
			int lRet = CusParseCommandLine(args);

			if (lRet == Common.DIFF_HELP || lRet == Common.DIFF_PARAMS_ERROR)
			{
				JOptionPane.showMessageDialog(mainFrame, m_strError, Common.TITLE_CAPTION, JOptionPane.ERROR_MESSAGE);
				return false;
			}

			lRet = CheckFileIsExist();
			if (lRet != Common.DIFF_PARAMS_OK)
			{
			    JOptionPane.showMessageDialog(mainFrame, m_strError, Common.TITLE_CAPTION, JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			mainFrame.bOperating = true;


			if (!m_bDirDiff)
			{
			    BaseDataObject bHasConflict = new BaseDataObject();
				if (!Diff(true, bHasConflict))
				{
					mainFrame.bOperating = false;
					return false;
				}

				if ((m_dwFlags & EnumMergeType.Three_Way_Merge) == 
					    EnumMergeType.Three_Way_Merge &&
						!bHasConflict.getBooleanValue())
				{// auto merge, so not show visual merge
					
					mainFrame.bOperating = false;
					return false;
				}
			}
			else
			{
				if (!DirCompare())
				{
					mainFrame.bOperating = false;
					return false;
				}
			}

			m_bInitSuccess = true;
		}


		if (m_bDirDiff)
		{
			m_strCaption = "Directory Compare";
		}
		
		if (m_bInitSuccess)
		{
			if (mainFrame != null)
			{
				mainFrame.InitForCmdLine(m_strCaption);
			}
		}
		
		mainFrame.bOperating = false;
		
		return true;
	}
	
	public int CusParseCommandLine(String[] args)
	{
		String str = "";
		int i = 0;
		m_aryFiles.clear();
		
		m_diffOptions.m_bIgnoreCase = false;
		m_diffOptions.m_bIgnoreEOL = false;
		m_diffOptions.m_bShowIdenticalFiles = false;

		for (i = 0; i < args.length; i++)
		{
			str = args[i];
					
			if (str.length() == 0)
			{
				continue;
			}
			
			if (str.startsWith("-"))
			{
				// remove flag specifier
				str = str.substring(1);

				// -? for help
				if (str.equalsIgnoreCase("?"))
				{
					m_strError = "MergeHero Options:\n[-Diff]        Diff two files only\n[-Merge]  Three-way merge\n[-R]            Compare directories Recursively\n\n";
					return Common.DIFF_HELP;
				}
				
				m_strCaption = ("Files Diff and Merge");
				// -Diff for only diff, default diff and merge
				if (str.compareToIgnoreCase("Diff") == 0)
				{
					m_bIsMerge = false;
					m_strCaption = ("Files Diff Only");
				}

				// -Merge for Three-way merge, default Two-way merge
				if (str.compareToIgnoreCase("Merge") == 0)
				{
					m_dwFlags |= EnumMergeType.Three_Way_Merge;
					m_strCaption = ("Three-way merge");
				}

				// -R for compare directories recursively
				if (str.compareToIgnoreCase("R") == 0)
				{
					m_bRecursive = true;
				}
				
				// -LeftLabel
				if (str.compareToIgnoreCase("LeftLabel") == 0)
				{
					m_strLeftLabel = args[++i];
				}

				// -RightLabel
				if (str.compareToIgnoreCase("RightLabel") == 0)
				{
					m_strRightLabel = args[++i];
				}

				// -DiffBinary
				if (str.compareToIgnoreCase("DiffBinary") == 0)
				{
					m_bDiffBinary = true;
				}

				// -Ignore EOL
				if (str.compareToIgnoreCase("IgnoreEOL") == 0)
				{
					m_diffOptions.m_bIgnoreEOL = true;
				}

				// -Ignore case
				if (str.compareToIgnoreCase("IgnoreCase") == 0)
				{
					m_diffOptions.m_bIgnoreCase = true;
				}

				// -Show identical files
				if (str.compareToIgnoreCase("ShowIdenticalFiles") == 0)
				{
					m_diffOptions.m_bShowIdenticalFiles = true;
				}				

				continue;
			}

			str = trim(str, "\"");
			m_aryFiles.add(str);
		}

		if (m_aryFiles.size() < 2)
		{
			m_strError = "Invalid parameters, please reference help.";
			return Common.DIFF_PARAMS_ERROR;
		}
		
		return Common.DIFF_PARAMS_OK;
	}

	public boolean Diff(boolean bAutoMerge, BaseDataObject bHasConflict)
	{
	    BaseDataObject strError = new BaseDataObject();
		if ((m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{// Three way merge
			m_OrgTextBuf.SetFilePath((String)m_aryFiles.get(0));
			m_ServerTextBuf.SetFilePath((String)m_aryFiles.get(1));
			m_LocalTextBuf.SetFilePath((String)m_aryFiles.get(2));
			if (m_aryFiles.size() >= 4)
			{
				m_MergeTextBuf.SetFilePath((String)m_aryFiles.get(3));
			}
			else
			{
				bAutoMerge = false;
			}

			if (!m_fileDiff.ThreeWayMerge(m_OrgTextBuf, m_ServerTextBuf, m_LocalTextBuf, 
				m_MergeTextBuf, m_diffOptions, bAutoMerge, bHasConflict, strError))
			{
			    JOptionPane.showMessageDialog(mainFrame, strError.getStringValue(), Common.TITLE_CAPTION,JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else
		{
			if (m_bIsMerge)
			{// Two way merge
				m_OrgTextBuf.SetFilePath((String)m_aryFiles.get(0));
				m_LocalTextBuf.SetFilePath((String)m_aryFiles.get(1));
				if (m_aryFiles.size() >= 3)
				{
					m_MergeTextBuf.SetFilePath((String)m_aryFiles.get(2));
				}

				if (!m_fileDiff.TwoWayMerge(m_OrgTextBuf, m_LocalTextBuf, m_MergeTextBuf, 
					m_diffOptions, strError))
				{
				    JOptionPane.showMessageDialog(mainFrame, strError.getStringValue(), Common.TITLE_CAPTION, 
		                    JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			else
			{
				m_OrgTextBuf.SetFilePath((String)m_aryFiles.get(0));
				m_LocalTextBuf.SetFilePath((String)m_aryFiles.get(1));
				
				BaseDataObject bIdentical = new BaseDataObject();

				if (!m_fileDiff.CompareFiles(m_OrgTextBuf, m_LocalTextBuf, m_diffOptions, 
					bIdentical, strError))
				{
				    JOptionPane.showMessageDialog(mainFrame, strError.getStringValue(), Common.TITLE_CAPTION, 
		                    JOptionPane.ERROR_MESSAGE);
					return false;
				}
				else
				{
					if (bIdentical.getBooleanValue())
					{
					    JOptionPane.showMessageDialog(mainFrame, "The selected files are identical.", Common.TITLE_CAPTION, 
			                    JOptionPane.ERROR_MESSAGE);
					}

					if (!m_diffOptions.m_bShowIdenticalFiles && bIdentical.getBooleanValue())
					{
						return false;
					}	
				}
			}
		}

		// Get diff result
		m_fileDiff.GetDiffMergeBlock(m_aryMergedBlock);
		
		return true;
	}

	public int CheckFileIsExist()
	{
		String str = "";
		int iSize = m_aryFiles.size();
		
		File file0 = null;
		File file1 = null;
		
		if ((m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{
			if (iSize >= 4)
			{
				str = trimSeparatorChar((String)m_aryFiles.get(3));
				int nPos = str.lastIndexOf(File.separatorChar);
				if (nPos != -1)
				{
					str = str.substring(0, nPos);
				}
				if (!RecursiveCreateDirectory(0, str))
				{
					JOptionPane.showMessageDialog(mainFrame, "Failed to create" + str, Common.TITLE_CAPTION, 
		                    JOptionPane.ERROR_MESSAGE);		
					return Common.DIFF_PARAMS_ERROR;
				}
				iSize = 3;
			}
		}
		else
		{
			if (m_bIsMerge)
			{
				if (iSize >= 3)
				{
					str = trimSeparatorChar((String)m_aryFiles.get(2));
					int nPos = str.lastIndexOf(File.separatorChar);
					if (nPos != -1)
					{
						str = str.substring(0, nPos);
					}	
					if (!RecursiveCreateDirectory(0, str))
					{
					    JOptionPane.showMessageDialog(mainFrame, "Failed to create" + str, Common.TITLE_CAPTION, 
			                    JOptionPane.ERROR_MESSAGE);	
						return Common.DIFF_PARAMS_ERROR;
					}
					iSize = 2;
				}
			}
		}	
		
		for (int i = 0; i < iSize; i++)
		{
			str = (String)m_aryFiles.get(i);
			File file = new File(str);
			if (!file.canRead() || !file.exists())
			{
				m_strError = "\'" + str + "\'" + " not found.";
				return Common.DIFF_PARAMS_ERROR;	
			}
		}
		
		if (iSize == 2)
		{
		    file0 = new File((String)m_aryFiles.get(0));
		    file1 = new File((String)m_aryFiles.get(1));
		     
			if (file0.isDirectory() && file1.isDirectory())
			{
				m_bDirDiff = true;
				m_bIsMerge = false;
				m_dwFlags = 0;
			}
		}

		return Common.DIFF_PARAMS_OK;
	}
	
	String trimSeparatorChar(String strDir)
	{
		int nPos = strDir.lastIndexOf(File.separatorChar);
		
		while ((nPos != -1) && (nPos == strDir.length() - 1))
		{
			 strDir = strDir.substring(0, nPos);	
			 nPos  = strDir.lastIndexOf(File.separatorChar);
		}	
		
		return strDir;
	}
	
	boolean RecursiveCreateDirectory(int iStart, String strDir)
	{
		String strTemp = "";

		strDir = trimSeparatorChar(strDir);
		
		File file = new File(strDir);
		
		if (file.exists())
		{
			return true;
		}
		
		return file.mkdirs();
	}

	public boolean DirCompare()
	{
		if (!m_bDirDiff)
		{
			return false;
		}

		if (m_aryFiles.size() != 2)
		{
			return false;
		}
		
		m_aryDiffItemArray.clear();
		m_strCompareDir = (String)m_aryFiles.get(0);
		m_strToDir = (String)m_aryFiles.get(1);

		// Compare directories	
		BaseDataObject strError = new BaseDataObject();
		if (!m_dirDiff.CompareDirectories(m_strCompareDir, m_strToDir, m_bRecursive, m_aryDiffItemArray, strError))
		{
		    JOptionPane.showMessageDialog(mainFrame, strError.getStringValue(), Common.TITLE_CAPTION, 
                    JOptionPane.ERROR_MESSAGE);	
			return false;
		}
				
		return true;
	}

	public boolean IsValidPath(String  strName)
	{
		File file = new File(strName);
		
		if (file.exists() && file.canRead())
		{
		    return true;
		}
		
		return false;
	}
	
	private boolean IsMainFrmDisposed()
	{
		if (mainFrame != null)
		{
			return mainFrame.m_bDisposed;
		}
		
		return true;
	}
	
	private String trim(String str, String strToTrim)
	{
		assert (null != str);
		while (str.startsWith(strToTrim))
		{
			str = str.substring(1);
		}
		
		while (str.endsWith(strToTrim))
		{
			str = str.substring(0,  str.length() - 1);
		}
		
		return str;
	}
}

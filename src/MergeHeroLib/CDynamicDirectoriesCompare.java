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
import java.io.File;
import java.util.*;

import com.dynamsoft.sourceanywhere.MD4;
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
public class CDynamicDirectoriesCompare
{
    public static void main(String[] args)
    {
    }
      
  	private static final int FILEFLAG = 0x3; 
  	private static final int TEXT = 0x1;
  	private static final int BIN = 0x2;
  	private static final int DIRFLAG = 0x30;
  	private static final int FILE = 0x10;
  	private static final int DIR = 0x20;
  	private static final int SIDEFLAG = 0x300;
  	private static final int LEFT = 0x100;
  	private static final int RIGHT = 0x200;
  	private static final int BOTH = 0x300;
  	private static final int COMPAREFLAGS = 0x7000;
  	private static final int SAME = 0x1000;
  	private static final int DIFF = 0x2000;
  	private static final int SKIPPED = 0x3000;
  	private static final int CMPERR = 0x4000;
  	private static final int NOCMP = 0x0000;
  	
 	private CDirDiff m_dirDiff = new CDirDiff();
  	private CDirDiffThread m_dirDiffThread = new CDirDiffThread();
 
  	
  	// compare function
  	public boolean CompareDirectories(String strCompare, String strTo, 
		boolean bRecursive, CDiffItemArray aryDiffItem, BaseDataObject strError)
  	{
  	  if (!m_dirDiff.SetDiffParameter(strCompare, strTo, bRecursive))
  	  {
  	    strError.setStringValue(m_dirDiff.m_strError);
  	    return false;
  	  }
  	  
  	  if (!m_dirDiffThread.CompareDirectories(m_dirDiff))
  	  {
  	    strError.setStringValue(m_dirDiff.m_strError);
  	    return false; 	    
  	  }
  	  
  	  aryDiffItem.clear();
  	  aryDiffItem.addAll(m_dirDiff.m_aryDiffItem);
  	  
      return true;
  	}
  	 	
    public class SDiffInfo
    {
    	public String m_strName = "";

    	public long m_mtime = 0;
    	public long m_ctime = 0;
    	public long m_size = 0;
    	public int m_attrs = 0;
    }	

    public class SDiffCode
    {
        int m_iDiffcode;

        SDiffCode()
        {
            m_iDiffcode = NOCMP;
        }

        // file/directory
        public boolean IsDirectory()
        {
            return ((m_iDiffcode & DIRFLAG) == DIR);
        }

        // left/right
        public boolean IsSideLeft()
        {
            return ((m_iDiffcode & SIDEFLAG) == LEFT);
        }

        public boolean IsSideRight()
        {
            return ((m_iDiffcode & SIDEFLAG) == RIGHT);
        }

        // result filters
        public boolean IsResultError()
        {
            return ((m_iDiffcode & COMPAREFLAGS) == CMPERR);
        }

        public boolean IsResultSame()
        {
            return ((m_iDiffcode & COMPAREFLAGS) == SAME);
        }

        public boolean IsResultDiff()
        {
            return (!IsResultSame() && !IsResultSkipped() && !IsResultError()
                    && !IsSideLeft() && !IsSideRight());
        }

        public boolean IsResultSkipped()
        {
            return ((m_iDiffcode & COMPAREFLAGS) == SKIPPED);
        }

        // type
        public boolean IsBin()
        {
            return ((m_iDiffcode & FILEFLAG) == BIN);
        }
    }
    
    public class SDiffItem extends SDiffCode
    {
    	public SDiffInfo m_leftDiffInfo = null;
    	public SDiffInfo m_rightDiffInfo = null;
    	public String m_strSubDirName = "";
    	
    	public String GetLeftFullName(final CDirDiff pDirDiff)  
    	{ 
    		String strPath = "";

    		if (!IsSideRight())
    		{
    			strPath = pDirDiff.m_strLeft;
    			if (m_strSubDirName.length() > 0)
    			{
    				strPath += "\\" + m_strSubDirName;
    			}
    		}
    		return strPath;
    	}

    	String GetRightFullName(final CDirDiff pDirDiff)  
    	{ 
    		String strPath = "";
    		
    		if (!IsSideLeft())
    		{
    			strPath = pDirDiff.m_strRight;
    			if (m_strSubDirName.length() > 0)
    			{
    				strPath += "\\" + m_strSubDirName;
    			}
    		}
    		return strPath;
    	}

    	public String GetExtension() 
    	{
    		String strTemp = "";
    		if (IsSideLeft())
    		{
    			strTemp = m_leftDiffInfo.m_strName;
    		} 
    		else if (IsSideRight())
    		{
    			strTemp = m_rightDiffInfo.m_strName;
    		}
    		else
    		{
    			strTemp = m_rightDiffInfo.m_strName;
    		}

    		int iPos = strTemp.lastIndexOf('.');
    		if (iPos != -1)
    		{
    			return (strTemp.substring(iPos + 1));
    		}
    		else
    		{
    			return "";
    		}
    	}
     }

    public class CDiffItemArray extends ArrayList
    {
//    	override the parent member functio
    	public void add(int iIndex, Object SDiffItemToAdd) //insert
    	{
    		if (SDiffItemToAdd instanceof SDiffItem)
    			super.add(iIndex, SDiffItemToAdd);
    	}
    	
    	public boolean add(Object SDiffItemToAdd)
    	{
    		if (SDiffItemToAdd instanceof SDiffItem)
    			return super.add(SDiffItemToAdd);
    		else
    			return false;
    	}
    	
    	public Object set(int iIndex, Object SDiffItemToAdd)
    	{
    		if (SDiffItemToAdd instanceof SDiffItem)
    			return super.set(iIndex, SDiffItemToAdd);
    		else
    			return null;
    	}
    }
    
    public class CDirDiff  
    {
        // Attribute
    	public boolean m_bRecursive = false;
    	public String m_strLeft = "";
    	public String m_strRight = "";
    	public String m_strError = "";

    	// Attribute
    	private CDiffItemArray m_aryDiffItem = new CDiffItemArray();

    	// method
    	public boolean SetDiffParameter(String strLeftDir, String strRightDir,
				boolean bRecursive)
    	{
    	    File leftDir = new File(strLeftDir);
    	    File rightDir = new File(strRightDir);
    	    if (!leftDir.exists() || !leftDir.canRead())
    	    {
				m_strError = "The input folder " + strLeftDir + " is invalid!";
				return false;
    	    }
    	    
      	    if (!rightDir.exists() || !rightDir.canRead())
    	    {
				m_strError = "The input folder " + strRightDir + " is invalid!";
				return false;
    	    }
      	    
    	    m_strLeft = strLeftDir;
    	    m_strRight = strRightDir;
    	    m_bRecursive = bRecursive;
    	    
    	    return true;
    	}	
    	
    	public void AddDiff(SDiffItem  diffItem)
    	{
    		m_aryDiffItem.add(diffItem);
    	}

    	public void RemoveDiff(int nPos)
    	{
    		int nCount = m_aryDiffItem.size();

    		if (nPos >= 0 && nPos < nCount)
    		{
    			m_aryDiffItem.remove(nPos);
    		}
    	}

    	public void RemoveAll()
    	{
    		m_aryDiffItem.clear();
    	}

    	public int GetDiffCount()
    	{
    		return m_aryDiffItem.size();
    	}

    	SDiffItem GetDiffAt(int nPos)
    	{
    		int nCount = m_aryDiffItem.size();
  
    		return (SDiffItem)m_aryDiffItem.get(nPos);
    	}	
    }
    
//  directory or file info for one row in diff result
    private class SItemInfo
    {
        String m_strName = "";
    	long m_mtime = 0;
    	long m_ctime = 0;
    	long m_size = 0;
    	int m_attrs = 0;
    };

    class SItemInfoArray extends ArrayList
    {
//    	override the parent member functio
    	public void add(int iIndex, Object SItemInfoToAdd) //insert
    	{
    		if (SItemInfoToAdd instanceof SItemInfo)
    			super.add(iIndex, SItemInfoToAdd);
    	}
    	
    	public boolean add(Object SItemInfoToAdd)
    	{
    		if (SItemInfoToAdd instanceof SItemInfo)
    			return super.add(SItemInfoToAdd);
    		else
    			return false;
    	}
    	
    	public Object set(int iIndex, Object SItemInfoToAdd)
    	{
    		if (SItemInfoToAdd instanceof SItemInfo)
    			return super.set(iIndex, SItemInfoToAdd);
    		else
    			return null;
    	}
    } 
    
    class CusComparator implements Comparator
    {
        private boolean bCaseSensitiveSelf;
        public CusComparator(boolean bCaseSensitive)
        {
            bCaseSensitiveSelf = bCaseSensitive;
         }
        
        public int compare(Object o1, Object o2)
        {
            if (bCaseSensitiveSelf)
            {
                return CmpString(o1, o2);
            }
            else
            {
                return CmpiString(o1, o2);
            }
        }
//      case-sensitive collate function for qsorting an array
        public int CmpString(final Object pElem1, final Object pElem2)
        {
         	final SItemInfo pItemInfo1 = (SItemInfo)pElem1;
        	final SItemInfo pItemInfo2 = (SItemInfo)pElem2;

        	return pItemInfo1.m_strName.compareTo(pItemInfo2.m_strName);
        }

//         case-insensitive collate function for qsorting an array
        public int CmpiString(final Object pElem1, final Object pElem2)
        {
        	final SItemInfo pItemInfo1 = (SItemInfo)pElem1;
        	final SItemInfo pItemInfo2 = (SItemInfo)pElem2;
        	
        	return pItemInfo1.m_strName.compareToIgnoreCase(pItemInfo2.m_strName);
        }       
    }
    
    public class CDirDiffThread
    {
        public boolean CompareDirectories(CDirDiff pDirDiff)
        {
         	if (pDirDiff.m_strLeft.compareToIgnoreCase(pDirDiff.m_strRight) == 0)
        	{
        		pDirDiff.m_strError = "The same folder is opened!";
        		return false;
        	}
         	
         	pDirDiff.m_aryDiffItem.clear();

        	boolean bCaseSensitive = false;

        	String strSubdir = ""; // blank to start at roots specified in diff context

        	return DirScan(strSubdir, pDirDiff, bCaseSensitive);
        }
        
        // sort specified array
        private void Sort(SItemInfoArray paryDirInfo, boolean bCaseSensitive)
        {
            CusComparator comparator = new CusComparator(bCaseSensitive);
            int size = paryDirInfo.size();
            Object[] object = new Object[size];
            for (int i = 0; i < size; i++)
            {
                object[i] = paryDirInfo.get(i);
            }
            Arrays.sort(object, comparator);
        }

        // Compare (NLS aware) two strings, either case-sensitive or case-insensitive as caller specifies
        int CollStr(String str1, String str2, boolean bCaseSensitive)
        {
        	if (bCaseSensitive)
        	{
        		return str1.compareTo(str2);
        	}
        	else
        	{
        		return str1.compareToIgnoreCase(str2);
        	}
        }

//         Load arrays with all directories & paryFileInfo in specified dir
        void LoadFiles(final String strDir, SItemInfoArray paryDirInfo, 
        								SItemInfoArray  paryFileInfo)
        {
         	String strPattern = strDir;
 
         	File file = new File(strPattern);
          	String[] strFiles = file.list();
         	for (int i = 0; i < strFiles.length; i++)
         	{
        			file = new File(strPattern + File.separator+ strFiles[i]);
  
        			SItemInfo itemInfo = new SItemInfo();
        			itemInfo.m_ctime = file.lastModified();
        			itemInfo.m_mtime = file.lastModified();

        			if (file.isFile())
        			{
        				itemInfo.m_size = file.length();
        			}
        			else
        			{
        				itemInfo.m_size = -1;  // No size for directories
        			}
        			itemInfo.m_strName = strFiles[i];
        			itemInfo.m_attrs = 0;

        			(file.isDirectory() ? paryDirInfo : paryFileInfo).add(itemInfo);
         	}
        }

//         Load arrays with all directories & paryFileInfo in specified dir
        public void LoadAndSortFiles(final String strDir, SItemInfoArray  paryDirInfo, 
        										SItemInfoArray paryFileInfo, boolean bCaseSensitive)
        {
        	LoadFiles(strDir, paryDirInfo, paryFileInfo);
        	Sort(paryDirInfo, bCaseSensitive);
        	Sort(paryFileInfo, bCaseSensitive);
        }

        public int IsDiffFiles(final String strLeftFilePath, String strRightFilePath, 
        								CDirDiff pDirDiff)
        {
        	if (strLeftFilePath.length() == 0 || strRightFilePath.length() == 0)
        	{
        		return CMPERR;
        	}

         	byte[] strLeftCheckSum = new byte[16];
        	byte[] strRightCheckSum = new byte[16];

        	strLeftCheckSum = MD4.getFileMD4(strLeftFilePath);
        	strRightCheckSum = MD4.getFileMD4(strRightFilePath);
            
            if (CMemoryOperator.memcmp(strLeftCheckSum, strRightCheckSum, 16) != 0)
            {
                return DIFF;
            }
 
         	return SAME;
        }

//         Send one file or directory result back through the diff info
        public void StoreDiffResult(final String strSubDir, SItemInfo pLeftInfo, 
                SItemInfo pRightInfo, int iDiffCode, CDirDiff pDirDiff)
        {
         	SDiffItem diffItem = new SDiffItem();

        	diffItem.m_strSubDirName = strSubDir;
        	diffItem.m_iDiffcode = iDiffCode;

        	if (null != pLeftInfo)
        	{
        	    diffItem.m_leftDiffInfo = new SDiffInfo();
        		diffItem.m_leftDiffInfo.m_attrs = pLeftInfo.m_attrs;
        		diffItem.m_leftDiffInfo.m_ctime = pLeftInfo.m_ctime;
        		diffItem.m_leftDiffInfo.m_mtime = pLeftInfo.m_mtime;
        		diffItem.m_leftDiffInfo.m_size = pLeftInfo.m_size;
        		diffItem.m_leftDiffInfo.m_strName = pLeftInfo.m_strName;
        	}

        	if (null != pRightInfo)
        	{
        	    diffItem.m_rightDiffInfo = new SDiffInfo();
        		diffItem.m_rightDiffInfo.m_attrs = pRightInfo.m_attrs;
        		diffItem.m_rightDiffInfo.m_ctime = pRightInfo.m_ctime;
        		diffItem.m_rightDiffInfo.m_mtime = pRightInfo.m_mtime;
        		diffItem.m_rightDiffInfo.m_size = pRightInfo.m_size;
        		diffItem.m_rightDiffInfo.m_strName = pRightInfo.m_strName;
        	}

        	pDirDiff.AddDiff(diffItem);
        }

        boolean DirScan(final String strSubdir, CDirDiff pDirDiff, boolean bCaseSensitive)
        {
          	final String strBackslash = File.separator;
        	String strLeftDir = pDirDiff.m_strLeft;
        	String strRightDir = pDirDiff.m_strRight;
        	String strSubprefix = "";

        	if (strSubdir.length() != 0)
        	{
        		strLeftDir += strBackslash + strSubdir;
        		strRightDir += strBackslash + strSubdir;
        		strSubprefix = strSubdir + strBackslash;
        	}

        	SItemInfoArray aryLeftDirs = new SItemInfoArray();
           	SItemInfoArray aryLeftFiles = new SItemInfoArray();
           	SItemInfoArray aryRightDirs = new SItemInfoArray();
           	SItemInfoArray aryRightFiles = new SItemInfoArray();
        	LoadAndSortFiles(strLeftDir, aryLeftDirs, aryLeftFiles, bCaseSensitive);
        	LoadAndSortFiles(strRightDir, aryRightDirs, aryRightFiles, bCaseSensitive);
        	SItemInfo leftInfo = new SItemInfo();
        	SItemInfo rightInfo = new SItemInfo();

        	// If need cancel operation

        	// Handle directories
        	// i points to current directory in left list (aryLeftDirs)
        	// j points to current directory in right list (aryRightDirs)
        	int i = 0, j = 0;

        	while (true)
        	{
        		// If need cancel operation
        	    if (i < aryLeftDirs.size())
        	    {
        	        leftInfo = (SItemInfo)aryLeftDirs.get(i);
        	    }
        	    if (j < aryRightDirs.size())
        	    {
        	        rightInfo = (SItemInfo)aryRightDirs.get(j);
        	    }
        		if (i < aryLeftDirs.size() && (j == aryRightDirs.size() || 
        			CollStr(leftInfo.m_strName, rightInfo.m_strName, bCaseSensitive) < 0))
        		{
        			int iDiffCode = LEFT | DIR;

        			// Advance left pointer over left-only entry, and then retest with new pointers
         			StoreDiffResult(strSubdir, leftInfo, null, iDiffCode, pDirDiff);

        			++i;
        			continue;
        		}
        		if (j < aryRightDirs.size() && (i == aryLeftDirs.size() || 
        			CollStr(leftInfo.m_strName, rightInfo.m_strName, bCaseSensitive) > 0))
        		{
        			int iDiffCode = RIGHT | DIR;

        			// Advance right pointer over right-only entry, and then retest with new pointers
        			StoreDiffResult(strSubdir, null, rightInfo, iDiffCode, pDirDiff);

        			++j;
        			continue;
        		}
        		if (i < aryLeftDirs.size())
        		{
         			String strNewsub = strSubprefix + leftInfo.m_strName;

        			if (!pDirDiff.m_bRecursive)
        			{
        				// Non-recursive compare
        				// We are only interested about list of subdirectories to show - user can open them
        				// TODO: scan one level deeper to see if directories are identical/different
        				int iDiffCode = BOTH | DIR;
        				StoreDiffResult(strSubdir, leftInfo, rightInfo, iDiffCode, pDirDiff);
        			}
        			else
        			{
        				// Recursive compare
        				// Scan recursively all subdirectories too, we are not adding folders
        				if (!DirScan(strNewsub, pDirDiff, bCaseSensitive))
        				{
        					return false;
        				}
        			}

        			++i;
        			++j;
        			continue;
        		}
        		break;
        	}

        	// Handle files
        	// i points to current file in left list (aryLeftFiles)
        	// j points to current file in right list (aryRightFiles)
        	i = 0; j = 0;
        	while (true)
        	{
        		// If need cancel operation
           	    if (i < aryLeftFiles.size())
        	    {
        	        leftInfo = (SItemInfo)aryLeftFiles.get(i);
        	    }
        	    if (j < aryRightFiles.size())
        	    {
        	        rightInfo = (SItemInfo)aryRightFiles.get(j);
        	    }
        		if (i < aryLeftFiles.size() && (j == aryRightFiles.size() || 
        			CollStr(leftInfo.m_strName, rightInfo.m_strName, bCaseSensitive) < 0))
        		{
        			// Test against filter
        			int iDiffCode = LEFT | FILE;

        			StoreDiffResult(strSubdir, leftInfo, null, iDiffCode, pDirDiff);
        			
        			// Advance left pointer over left-only entry, and then retest with new pointers
        			++i;
        			continue;
        		}
        		if (j < aryRightFiles.size() && (i == aryLeftFiles.size() || 
        			CollStr(leftInfo.m_strName, rightInfo.m_strName, bCaseSensitive)>0))
        		{
        			// Test against filter
        			int iDiffCode = RIGHT | FILE;

        			StoreDiffResult(strSubdir, null, rightInfo, iDiffCode, pDirDiff);
        			
        			// Advance right pointer over right-only entry, and then retest with new pointers
        			++j;
        			continue;
        		}
        		if (i < aryLeftFiles.size())
        		{
        			int iDiffCode = BOTH | FILE;

        			String strLeftName = leftInfo.m_strName;
        			String strRightName = rightInfo.m_strName;

        			// Files to compare
        			String strLeftFilePath = strLeftDir + strBackslash + strLeftName;
        			String strRightFilePath = strRightDir + strBackslash + strRightName;
        			
        			// Really compare
        			iDiffCode |= IsDiffFiles(strLeftFilePath, strRightFilePath, pDirDiff);

        			// report result back to caller
        			StoreDiffResult(strSubdir, leftInfo, rightInfo, iDiffCode, pDirDiff);

        			++i;
        			++j;
        			continue;
        		}
        		break;
        	}

        	return true;
        }
    }
}

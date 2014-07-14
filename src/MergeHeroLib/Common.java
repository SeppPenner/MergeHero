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
 * Created on 2005-4-13
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
package MergeHeroLib;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.dynamsoft.sourceanywhere.BaseDataObject;
import com.dynamsoft.sourceanywhere.MD4;

public interface Common 
{ 
  //define return value
    public static final int NO_MEMORY = 0x0000;
    public static final int DIFF_OK = 0x0001;
    public static final int SAME_FILE = 0x0002;
    public static final int NOT_SUPPORT = 0x0003;
    public static final int DIFF_FAIL = 0x0004;
    public static final String szCRLF = "\r\n";
    public static final String lpszCrlfs[] = { "0x0d0a", //  DOS/Windows style
            "0x0a", //  UNIX style
            "0x0a" //  Macintosh style
    };

    public static int MAX_PATH = 260;
}

interface EnumLoadResult
{
  public static final int Load_Error = 0;
  public static final int Load_Ok = 1;
  public static final int Load_Ok_Impure = 2;
  public static final int Load_Binary = 3;
}

interface EnumSaveResult
{
  public static final int Save_Ok = 0;   
  public static final int Save_Fail = 1;
  public static final int Save_No_FileName = 2;  
  public static final int Save_Cancel = 3;
}

class CDiffResult
{
  // record all match lines
  public int m_lOrgMatchFrom;
  public int m_lOrgMatchTo;
  public int m_lDestMatchFrom;
  public int m_lDestMatchTo;
  public int m_lOrgLinesCount;
  public int m_lDestLinesCount;
  public CDiffResult m_pPrev; // previous diff result;
  // record all deleted lines
  public int m_lDeleteFrom;
  public int m_lDeleteTo;
  // record all insert lines
  public int m_lInsertFrom;
  public int m_lInsertTo;
  
  CDiffResult()
  {
	m_lOrgMatchFrom = 0;
	m_lOrgMatchTo = -1;
	m_lDestMatchFrom = 0;
	m_lDestMatchTo = -1;
	m_lDeleteFrom = 0;
	m_lDeleteTo = -1;
	m_lInsertFrom = 0;
	m_lInsertTo = -1;
	m_lOrgLinesCount = 0;
	m_lDestLinesCount = 0;
	m_pPrev = null;
  }
  
  public CDiffResult copy(final CDiffResult src)
  {
    m_lOrgMatchFrom = src.m_lOrgMatchFrom;
    m_lOrgMatchTo = src.m_lOrgMatchTo;
    m_lDestMatchFrom = src.m_lDestMatchFrom;
    m_lDestMatchTo = src.m_lDestMatchTo;
    m_lOrgLinesCount = src.m_lOrgLinesCount;
    m_lDestLinesCount = src.m_lDestLinesCount;
    m_lDeleteFrom = src.m_lDeleteFrom;
    m_lDeleteTo = src.m_lDeleteTo;
    m_lInsertFrom = src.m_lInsertFrom;
    m_lInsertTo = src.m_lInsertTo;
    m_pPrev = src.m_pPrev;
     return this;
  }
  
  public void GetEditScript()
  {
  	if (m_pPrev == null)
  	{// deal with the first diff result
  		if (m_lOrgMatchFrom > 1)
  		{
  			m_lDeleteFrom = 1;
  			m_lDeleteTo = m_lOrgMatchFrom - 1;
  		}
  		else if (m_lOrgMatchFrom == 0)
  		{
  			m_lDeleteFrom = 1;
  			m_lDeleteTo = m_lOrgLinesCount;
  		}
  		
  		if (m_lDestMatchFrom > 1)
  		{
  			m_lInsertFrom = 1;
  			m_lInsertTo = m_lDestMatchFrom - 1;
  		}
  		else if (m_lDestMatchFrom == 0)
  		{
  			m_lInsertFrom = 1;
  			m_lInsertTo = m_lDestLinesCount;
  		}
  	}
  	else
  	{
  		long lDelta = m_lOrgMatchFrom - m_pPrev.m_lOrgMatchTo;

  		if (m_lOrgMatchFrom <= m_lOrgLinesCount && lDelta > 1)
  		{
  			m_lDeleteFrom = m_pPrev.m_lOrgMatchTo + 1;
  			m_lDeleteTo = m_lOrgMatchFrom - 1;
  		}
  		
  		lDelta = m_lDestMatchFrom - m_pPrev.m_lDestMatchTo;

  		if (m_lDestMatchFrom <= m_lDestLinesCount && lDelta > 1)
  		{
  			m_lInsertFrom = m_pPrev.m_lDestMatchTo + 1;
  			m_lInsertTo = m_lDestMatchFrom - 1;
  		}
  	}
  }

  public void GetLastEditScript()
  {
  	if (m_pPrev != null)
  	{
  		m_lOrgLinesCount = m_pPrev.m_lOrgLinesCount;
  		m_lDestLinesCount = m_pPrev.m_lDestLinesCount;

  		if (m_pPrev.m_lOrgMatchTo > 0 &&
  			m_pPrev.m_lOrgMatchTo < m_lOrgLinesCount &&
  			m_pPrev.m_lDeleteTo < m_lOrgLinesCount)
  		{
  			m_lDeleteFrom = m_pPrev.m_lOrgMatchTo + 1;
  			m_lDeleteTo = m_lOrgLinesCount;
  		}

  		if (m_pPrev.m_lDestMatchTo > 0 &&
  			m_pPrev.m_lDestMatchTo < m_lDestLinesCount &&
  			m_pPrev.m_lInsertTo < m_lDestLinesCount)
  		{
  			m_lInsertFrom = m_pPrev.m_lDestMatchTo + 1;
  			m_lInsertTo = m_lDestLinesCount;
  		}
   	}
  }  
}

class CDiffResultArray extends ArrayList
{
//	override the parent member functio
	public void add(int iIndex, Object diffResultToAdd) //insert
	{
		if (diffResultToAdd instanceof CDiffResult)
			super.add(iIndex, diffResultToAdd);
	}
	
	public boolean add(Object diffResultToAdd)
	{
		if (diffResultToAdd instanceof CDiffResult)
			return super.add(diffResultToAdd);
		else
			return false;
	}
	
	public Object set(int iIndex, Object diffResultToAdd)
	{
		if (diffResultToAdd instanceof CDiffResult)
			return super.set(iIndex, diffResultToAdd);
		else
			return null;
	}
}
//The result of comparison is an "edit script": a chain of "struct change".
//Each "struct change" represents one place where some lines are deleted
//and some are inserted.
//Remember to delete the memory point in CDiffResultArray at last.

class SDiffResult
{
  public int m_nCountInserted; // lines of destination file changed here.

  public int m_nCountDeleted; // lines of original file changed here.

  CDiffResultArray m_aryDiffResult = new CDiffResultArray(); // save the CDiffResult point for release memory

  SDiffResult()
  {
     m_nCountInserted = 0;
     m_nCountDeleted = 0;
  }
}

class CUtility
{
	static void GetChecksum(CDynamicTextBuffer pTextBuffer, CDiffOptions diffOptions, long[] aryChecksum)
	{
		int iSize = pTextBuffer.m_aryLineInfo.size();
		
		if (diffOptions.m_bIgnoreCase && diffOptions.m_bIgnoreEOL)
		{
			for (int i = 0; i < iSize; i++)
			{
				SLineInfo lineInfo = (SLineInfo)(pTextBuffer.m_aryLineInfo.get(i));
				
				String strTemp = new String(lineInfo.m_pcLine);
				strTemp = strTemp.toLowerCase();
				strTemp = strTemp.substring(0, lineInfo.Length());

				aryChecksum[i] = GetChecksum(strTemp);
			}
		}
		else if (diffOptions.m_bIgnoreCase && !diffOptions.m_bIgnoreEOL)
		{
			for (int i = 0; i < iSize; i++)
			{
				SLineInfo lineInfo = (SLineInfo)(pTextBuffer.m_aryLineInfo.get(i));

				String strTemp = new String(lineInfo.m_pcLine);
				strTemp = strTemp.toLowerCase();
				
				(aryChecksum)[i] = GetChecksum(strTemp);
			}
		}
		else if (!diffOptions.m_bIgnoreCase && diffOptions.m_bIgnoreEOL)
		{
			for (int i = 0; i < iSize; i++)
			{
				SLineInfo lineInfo = (SLineInfo)(pTextBuffer.m_aryLineInfo.get(i));
				String strTemp = new String(lineInfo.m_pcLine);
				strTemp = strTemp.substring(0, lineInfo.Length());
				
				(aryChecksum)[i] = GetChecksum(strTemp);
			}
		}
		else
		{
			for (int i = 0; i < iSize; i++)
			{
				SLineInfo lineInfo = (SLineInfo)(pTextBuffer.m_aryLineInfo.get(i));

				(aryChecksum)[i] = GetChecksum(String.valueOf(lineInfo.m_pcLine));
			}
		}	
	}
	
	public static boolean DiffUsingMD4(final String strOrgFile, final String strDestFile)
	{
	    byte[] strOrgCheckSum = new byte[16];
	    byte[] strDestCheckSum = new byte[16];
	
	    strOrgCheckSum = MD4.getFileMD4(strOrgFile);
	    strDestCheckSum = MD4.getFileMD4(strDestFile);
	
	    if (strOrgCheckSum == null || strDestCheckSum == null)
	    {
	        return false;
	    }
	    
	    if (CMemoryOperator.memcmp(strOrgCheckSum, strDestCheckSum, 16) != 0)
	    {
	        return false;
	    }
	    else
	    {
	        return true;
	    }
	}    
	
	public static void PrimeTextBuffersForDiff(CDynamicTextBuffer pCompareFileBuffer, 
			   CDynamicTextBuffer pToFileBuffer, 
			   SDiffResult diffResult,
			   CDiffMergeBlockArray paryDiffMergeBlock)
	{
		int nDiff = (int)(diffResult.m_aryDiffResult.size());
		int i = 0, j = 0;
		int nLinsertCount = 0, nRinsertCount = 0, nInsertGhostCount = 0;
		
		long dwLeft = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED, dwRight = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED;
		
		paryDiffMergeBlock.clear();
		
		int nRealMergeBlock = 0;
		int nInsertPos = 0;
		
		for (i = 0; i < nDiff; i++)
		{
			CDiffResult curDiff = (CDiffResult)diffResult.m_aryDiffResult.get(i);
			if (curDiff.m_lDeleteTo == -1 && curDiff.m_lInsertTo == -1)
			{
			    continue;
			}
			else
			{
				if (curDiff.m_lDeleteTo != -1)
				{
					// first insert ghost line in the right view
					nInsertGhostCount = curDiff.m_lDeleteTo - curDiff.m_lDeleteFrom + 1;
					nInsertPos = curDiff.m_lDeleteFrom + nLinsertCount - 1;
					pToFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
					nRinsertCount += nInsertGhostCount;
					
					for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
					{
					    pCompareFileBuffer.SetLineFlag(j - 1, dwLeft, true, false, false);
					}
					
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
					mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1;
					mergedBlock.m_nRightStartLine = nInsertPos;
					mergedBlock.m_nRightEndLine = nInsertPos + nInsertGhostCount - 1;
					mergedBlock.m_bRightIsGhost = true;
					
					paryDiffMergeBlock.add(mergedBlock);
				}
				
				if (curDiff.m_lInsertTo != -1)
				{
					// second insert ghost line in the left view
					nInsertGhostCount = curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1;
					nInsertPos = curDiff.m_lInsertFrom + nRinsertCount - 1;
					pCompareFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
					nLinsertCount += nInsertGhostCount;
					
					for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
					{
					    pToFileBuffer.SetLineFlag(j - 1, dwRight, true, false, false);				
					}
					
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nLeftStartLine = nInsertPos;
					mergedBlock.m_nLeftEndLine = nInsertPos + nInsertGhostCount - 1;
					mergedBlock.m_bLeftIsGhost = true;
					mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
					mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1;
					
					paryDiffMergeBlock.add(mergedBlock);
				}
			}
		}
		
		// Get all edit script
		GetAllEditScript(pCompareFileBuffer, pToFileBuffer, diffResult);
	
		pToFileBuffer.FinishLoading();
		pCompareFileBuffer.FinishLoading();
	}	
	
	public static void PrimeTextBuffersForTwoWayMerge(CDynamicTextBuffer pCompareFileBuffer,
			   CDynamicTextBuffer pToFileBuffer, CDynamicTextBuffer pResultBuffer,
			   SDiffResult diffResult,
			   CDiffMergeBlockArray paryDiffMergeBlock)
	{
		// Prepare result buffer
		PrepareResultBuffer(pResultBuffer, pCompareFileBuffer, pToFileBuffer);
 
		int nDiff = diffResult.m_aryDiffResult.size();
		int i = 0;
		int j = 0;
		int nLinsertCount = 0;
		int nRinsertCount = 0;
		int nInsertGhostCount = 0;
		int nDeleteCount = 0;
		int nRealLineInMerge = 0;
		CDiffResult curDiff = null;

		long dwLeft = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED | 
		CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED;
		long dwRight = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED | 
		CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED;
		
		paryDiffMergeBlock.clear();
		
		SLineInfo li = null;

		for (i = 0; i < nDiff; i++)
		{
			curDiff = (CDiffResult)diffResult.m_aryDiffResult.get(i);
			int nInsertPos;

			if (curDiff.m_lDeleteTo != -1)
			{
				// first insert ghost line in the right view
				nInsertGhostCount = curDiff.m_lDeleteTo - curDiff.m_lDeleteFrom + 1;
				nInsertPos = curDiff.m_lDeleteFrom + nLinsertCount - 1;
				pToFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
				nRinsertCount += nInsertGhostCount;

				for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
				{
					pCompareFileBuffer.SetLineFlag(j - 1, dwLeft, true, false, false);
					li = new SLineInfo();
					pResultBuffer.CopyLine(li, (SLineInfo)pCompareFileBuffer.m_aryLineInfo.get(j - 1));
					pResultBuffer.m_aryLineInfo.add(li);
					nRealLineInMerge ++;
					pResultBuffer.SetLineFlag(nRealLineInMerge - 1, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN | 
					        CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, 1, true);
				}
				
				SDiffMergeBlock  mergedBlock = new SDiffMergeBlock();
				mergedBlock.m_nMergeStartLine = nRealLineInMerge - nInsertGhostCount;
				mergedBlock.m_nMergeEndLine = nRealLineInMerge - 1;
				mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
				mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1;
				mergedBlock.m_nRightStartLine = nInsertPos;
				mergedBlock.m_nRightEndLine = nInsertPos + nInsertGhostCount - 1;
				mergedBlock.m_bRightIsGhost = true;
				
				paryDiffMergeBlock.add(mergedBlock);
			}

			if (curDiff.m_lInsertTo != -1)
			{
				// second insert ghost line in the left view
				nInsertGhostCount = curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1;
				nInsertPos = curDiff.m_lInsertFrom + nRinsertCount - 1;
				pCompareFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
				nLinsertCount += nInsertGhostCount;

				for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
				{
					pToFileBuffer.SetLineFlag(j - 1, dwRight, true, false, false);
					li = new SLineInfo();
					pResultBuffer.CopyLine(li, (SLineInfo)pToFileBuffer.m_aryLineInfo.get(j - 1));
					nRealLineInMerge ++;
					pResultBuffer.m_aryLineInfo.add(li);
				}

				SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
				mergedBlock.m_nMergeStartLine = nRealLineInMerge - nInsertGhostCount;
				mergedBlock.m_nMergeEndLine = nRealLineInMerge - 1;
				mergedBlock.m_nLeftStartLine = nInsertPos;
				mergedBlock.m_nLeftEndLine = nInsertPos + nInsertGhostCount - 1;
				mergedBlock.m_bLeftIsGhost = true;
				mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
				mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1;
				mergedBlock.m_bRightAdded = true;
				
				paryDiffMergeBlock.add(mergedBlock);
			}

			for (j = (curDiff.m_lOrgMatchFrom + nLinsertCount); j <= (curDiff.m_lOrgMatchTo + nLinsertCount); j++)
			{
			    li = new SLineInfo();
				pResultBuffer.CopyLine(li, (SLineInfo)pCompareFileBuffer.m_aryLineInfo.get(j - 1));
				pResultBuffer.m_aryLineInfo.add(li);
				nRealLineInMerge ++;
			}
		}

		// Get all edit script
		GetAllEditScript(pCompareFileBuffer, pToFileBuffer, diffResult);

		pToFileBuffer.FinishLoading();
		pCompareFileBuffer.FinishLoading();
		pResultBuffer.FinishLoading();
	}	
	
	public static void ReCombineDiffResult(CDynamicTextBuffer  pBaseBuffer, 
			   CDynamicTextBuffer  pTheirsBuffer, 
			   CDynamicTextBuffer  pYoursBuffer, 
			   SDiffResult diffResult1, 
			   SDiffResult diffResult2)
	{
		int i = 0, j = 0;
		int nlDiff = (int)(diffResult1.m_aryDiffResult.size());
		int nrDiff = (int)(diffResult2.m_aryDiffResult.size());
		int nGrowCount = 0, nInsertCount = 0, nRealLinesInMerge = 0;
		
		CDiffResult curDiff = null;
		SLineInfo li = null;
		
		// deal with the left view
		for (i = 0; i < nlDiff; i++)
		{
			curDiff = (CDiffResult)diffResult1.m_aryDiffResult.get(i);
			
			// First insert all deleted lines to the left view, set delete flags
			if (curDiff.m_lDeleteTo != -1)
			{
				int nLine = curDiff.m_lDeleteFrom;
				int nInsertPos = 0;
				
				nInsertPos = curDiff.m_lDeleteFrom;
				
				while (nLine <= curDiff.m_lDeleteTo)
				{
				    li = (SLineInfo)pBaseBuffer.m_aryLineInfo.get(nLine - 1);
					pTheirsBuffer.InsertLine(li.m_pcLine, li.m_nLength + li.m_nEolChars, (nInsertPos - 1 + nGrowCount), 1);
					
					pTheirsBuffer.SetLineFlag((nInsertPos - 1 + nGrowCount), 
					        CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, 0, true);
					nLine++;
					nInsertPos++;
				}
			}
				
			nInsertCount = (curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1);
				
			nGrowCount += nInsertCount;
		}
			
		nGrowCount = 0;
		nInsertCount = 0;
		// deal with right view
		for (i = 0; i < nrDiff; i++)
		{
			curDiff = (CDiffResult)diffResult2.m_aryDiffResult.get(i);
			
			// First insert all deleted lines to the right view, set delete flags
			if (curDiff.m_lDeleteTo != -1)
			{
				int nLine = curDiff.m_lDeleteFrom;
				int nInsertPos = 0;
				
				nInsertPos = curDiff.m_lDeleteFrom;
				
				while (nLine <= curDiff.m_lDeleteTo)
				{
				    li = (SLineInfo)pBaseBuffer.m_aryLineInfo.get(nLine - 1);
					pYoursBuffer.InsertLine(li.m_pcLine, 
					li.m_nLength + li.m_nEolChars, 
					(nInsertPos - 1 + nGrowCount), 1);
					
					pYoursBuffer.SetLineFlag((nInsertPos - 1 + nGrowCount), CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, 0, true);
					nLine++;
					nInsertPos++;
				}
			}
			
			nInsertCount = (curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1);
			
			nGrowCount += nInsertCount;
		}
				
		// Get all edit script
		GetAllEditScript(pTheirsBuffer, pYoursBuffer, diffResult1, diffResult2);
	}

	public static void PrimeTextBuffersForThreeWayMerge(CDynamicTextBuffer  pCompareFileBuffer, 
							CDynamicTextBuffer  pToFileBuffer, 
							CDynamicTextBuffer  pResultBuffer, 
							SDiffResult diffResult, 
							CDiffMergeBlockArray  paryDiffMergeBlock)
	{
	
		PrepareResultBuffer(pResultBuffer, pCompareFileBuffer, pToFileBuffer);
		
		int i = 0, j = 0;
		int nRealLinesInMerge = 0;
		int nRealMergedBlock = 0;
		int nLinsertCount = 0, nRinsertCount = 0, nInsertGhostCount = 0;
		int nInsert = 0, nDelete = 0;
		int k = 0;
		int nPrevDeletedLines = 0;
		int iDiffReulstSize = (int)(diffResult.m_aryDiffResult.size());
		
		long dwInsert = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, dwDelete = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, dwConflict = CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED;
		
		paryDiffMergeBlock.clear();
		
		SLineInfo conflictLineInfo = new SLineInfo();
		GetConflictLine(conflictLineInfo);
		
		SLineInfo li = null;
		CDiffResult curDiff = null;
		
		for (i = 0; i < iDiffReulstSize; i++)
		{
			curDiff = (CDiffResult)diffResult.m_aryDiffResult.get(i);
			
			// Find the deleted lines in matched lines
			if (curDiff.m_lDeleteTo != -1 && curDiff.m_lInsertTo == -1)
			{// insert ghost line in the right view
				nInsertGhostCount = curDiff.m_lDeleteTo - curDiff.m_lDeleteFrom + 1;
				int nInsertPos = curDiff.m_lDeleteFrom + nLinsertCount - 1;
				pToFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
				
				for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
				{
					pCompareFileBuffer.SetLineFlag(j - 1, dwInsert, 0, true);
					
					// insert the server version inserted line
					li = new SLineInfo();
					pResultBuffer.CopyLine(li, (SLineInfo)pCompareFileBuffer.m_aryLineInfo.get(j - 1));
					nRealLinesInMerge ++;
					pResultBuffer.m_aryLineInfo.add(li);
					pResultBuffer.m_ulNumMergedLines ++;
				}
				
				SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
				mergedBlock.m_nMergeStartLine = nRealLinesInMerge - nInsertGhostCount;
				mergedBlock.m_nMergeEndLine = nRealLinesInMerge - 1;
				mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
				mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1;
				mergedBlock.m_nRightStartLine = nInsertPos;
				mergedBlock.m_nRightEndLine = nInsertPos + nInsertGhostCount - 1;
				mergedBlock.m_bRightIsGhost = true;
				mergedBlock.m_bLeftAdded = true;
				
				paryDiffMergeBlock.add(mergedBlock);
				
				nRinsertCount += nInsertGhostCount;
				nPrevDeletedLines = 0;
			}
			else if (curDiff.m_lDeleteTo == -1 && curDiff.m_lInsertTo != -1)
			{// insert ghost line in the left view
				nInsertGhostCount = curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1;
				int nInsertPos = curDiff.m_lInsertFrom + nRinsertCount - 1;
				pCompareFileBuffer.InsertEmptyLine(nInsertPos, nInsertGhostCount);
				for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
				{
					pToFileBuffer.SetLineFlag(j - 1, dwInsert, 0, true);
					li = new SLineInfo();
					pResultBuffer.CopyLine(li, (SLineInfo)pToFileBuffer.m_aryLineInfo.get(j - 1));
					pResultBuffer.m_aryLineInfo.add(li);
					nRealLinesInMerge ++;
				}
				
				SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
				mergedBlock.m_nMergeStartLine = nRealLinesInMerge - nInsertGhostCount;
				mergedBlock.m_nMergeEndLine = nRealLinesInMerge - 1;
				mergedBlock.m_nLeftStartLine = nInsertPos;
				mergedBlock.m_nLeftEndLine = nInsertPos + nInsertGhostCount - 1;
				mergedBlock.m_bLeftIsGhost = true;
				mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
				mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1;
				mergedBlock.m_bRightAdded = true;
				
				paryDiffMergeBlock.add(mergedBlock);
				
				nLinsertCount += nInsertGhostCount;
				nPrevDeletedLines = 0;
			}
			else if (curDiff.m_lDeleteTo != -1 && curDiff.m_lInsertTo != -1)
			{// there has some conflict
				nInsert = curDiff.m_lInsertTo - curDiff.m_lInsertFrom + 1;
				nDelete = curDiff.m_lDeleteTo - curDiff.m_lDeleteFrom + 1;
				
				// Insert empty line to merge view, if need
				int nBeInsertedLine = nPrevDeletedLines > (nInsert + nDelete) ? nPrevDeletedLines : (nInsert + nDelete);
				nBeInsertedLine -= nPrevDeletedLines;
				
				pResultBuffer.m_ulNumConflictLines ++;
				
				if (nInsert > nDelete)
				{
					nInsertGhostCount = nInsert - nDelete;
					pCompareFileBuffer.InsertConflictLine((curDiff.m_lDeleteTo + nLinsertCount), nInsertGhostCount);
					
					for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
					{
					    pToFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
					{
					    pCompareFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMerge - nPrevDeletedLines;
					mergedBlock.m_nMergeEndLine = nRealLinesInMerge + nBeInsertedLine - 1;
					mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
					mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1;
					mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
					mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1 + nInsertGhostCount;
					mergedBlock.m_bConflict = true;
					
					paryDiffMergeBlock.add(mergedBlock);
					
					nLinsertCount += nInsertGhostCount;
				}
				else if (nInsert < nDelete)
				{
					nInsertGhostCount = nDelete - nInsert;
					pToFileBuffer.InsertConflictLine((curDiff.m_lInsertTo + nRinsertCount), nInsertGhostCount);
					
					for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
					{
					    pToFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
					{
					    pCompareFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMerge - nPrevDeletedLines;
					mergedBlock.m_nMergeEndLine = nRealLinesInMerge + nBeInsertedLine - 1;
					mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
					mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1 + nInsertGhostCount;
					mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
					mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1;
					mergedBlock.m_bConflict =  true;
					
					paryDiffMergeBlock.add(mergedBlock);
					
					nRinsertCount += nInsertGhostCount;	
				}
				else
				{
					for (j = (curDiff.m_lInsertFrom + nRinsertCount); j <= (curDiff.m_lInsertTo + nRinsertCount); j++)
					{
					    pToFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					for (j = (curDiff.m_lDeleteFrom + nLinsertCount); j <= (curDiff.m_lDeleteTo + nLinsertCount); j++)
					{
					    pCompareFileBuffer.SetLineFlag(j - 1, dwConflict, 0, true);
					}
					
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMerge - nPrevDeletedLines;
					mergedBlock.m_nMergeEndLine = nRealLinesInMerge + nBeInsertedLine - 1;
					mergedBlock.m_nRightStartLine = curDiff.m_lInsertFrom + nRinsertCount - 1;
					mergedBlock.m_nRightEndLine = curDiff.m_lInsertTo + nRinsertCount - 1;
					mergedBlock.m_nLeftStartLine = curDiff.m_lDeleteFrom + nLinsertCount - 1;
					mergedBlock.m_nLeftEndLine = curDiff.m_lDeleteTo + nLinsertCount - 1;
					mergedBlock.m_bConflict =  true;			
					
					paryDiffMergeBlock.add(mergedBlock);
				}
					
				for (k = 0; k < nBeInsertedLine; k++)
				{
				    li = new SLineInfo();
				    pResultBuffer.CopyLine(li, conflictLineInfo);
				    pResultBuffer.m_aryLineInfo.add(li);
				    nRealLinesInMerge ++;
				}
				nPrevDeletedLines = 0;
			}
				
			// Insert orginal line to merge view, if exists
			int nMatchLines = curDiff.m_lDestMatchTo - curDiff.m_lDestMatchFrom + 1;
			boolean bHasConflict = false;
			
			if ((i + 1) < iDiffReulstSize)
			{
				CDiffResult nextDiff = (CDiffResult)diffResult.m_aryDiffResult.get(i + 1);
				bHasConflict = ((nextDiff.m_lDeleteFrom != 0) && (nextDiff.m_lInsertFrom != 0));
			}
			
		    pResultBuffer.InsertEmptyLine(nRealLinesInMerge, nMatchLines);
			nRealLinesInMerge += nMatchLines;
			
			for (k = 0; k < nMatchLines; k++)
			{
				SLineInfo rightLineInfo = (SLineInfo)pToFileBuffer.m_aryLineInfo.get(curDiff.m_lDestMatchTo - k - 1 + nRinsertCount);
				SLineInfo leftLineInfo = (SLineInfo)pCompareFileBuffer.m_aryLineInfo.get(curDiff.m_lOrgMatchTo - k - 1 + nLinsertCount);
				
				li = new SLineInfo();
				pResultBuffer.CopyLine(li, rightLineInfo);
				pResultBuffer.m_aryLineInfo.set(nRealLinesInMerge - k - 1, li);
				
				if ((rightLineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED || 
				(leftLineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED)
				{
				    pResultBuffer.SetLineFlag(nRealLinesInMerge - k - 1, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED, 1, true);
				}
				
				if ((rightLineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED && 
				(leftLineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED)
				{
					// Let the line hiden in left and right view
					rightLineInfo.m_dwFlags |= CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN;
					leftLineInfo.m_dwFlags |= CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN;
					
					if (!bHasConflict)
					{
					    pResultBuffer.SetLineFlag(nRealLinesInMerge - k - 1, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN | CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED, 1, true);
					}
					else
					{
						if (nPrevDeletedLines == k)
						{
							pResultBuffer.SetLineFlag(nRealLinesInMerge - k - 1, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT, 1, true);
							nPrevDeletedLines ++;
						}
					}
				}
			}
		
			int nLeftDeleteStart = -1, nRightDeleteStart = -1;
			int nRealLinesInMergeTemp = nRealLinesInMerge - nMatchLines;
			int nDeleteCount = 0;
			int nOtherStart = 0;
			boolean bHasMergedBlock = false;
			
			for (k = 0; k < nMatchLines; k++)
			{
				while (k <= nMatchLines && (pToFileBuffer.GetLineFlags(curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED && 
				(pCompareFileBuffer.GetLineFlags(curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED)
				{
					if (nLeftDeleteStart == -1)
					{
						nLeftDeleteStart = curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount;
						nOtherStart = curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount;
					}
					nDeleteCount ++;
					k ++;
				}
					
				if (nLeftDeleteStart != -1)
				{
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMergeTemp;
					mergedBlock.m_nMergeEndLine = nRealLinesInMergeTemp + nDeleteCount - 1;
					mergedBlock.m_nLeftStartLine = nLeftDeleteStart;
					mergedBlock.m_nLeftEndLine = nLeftDeleteStart + nDeleteCount - 1;
					mergedBlock.m_nRightStartLine = nOtherStart;
					mergedBlock.m_nRightEndLine = nOtherStart + nDeleteCount - 1;
					mergedBlock.m_bRightIsNormal = true;
					
					paryDiffMergeBlock.add(mergedBlock);
					
					nRealLinesInMergeTemp += nDeleteCount;
					nDeleteCount = 0;
					nLeftDeleteStart = -1;
					bHasMergedBlock = true;
				}
				
				while (k <= nMatchLines && (pToFileBuffer.GetLineFlags(curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED && 
				(pCompareFileBuffer.GetLineFlags(curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED)
				{
					if (nLeftDeleteStart == -1 && nRightDeleteStart == -1)
					{
						nLeftDeleteStart = curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount;
						nRightDeleteStart = curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount;
					}
					nDeleteCount ++;
					k ++;
				}
					
				if (nLeftDeleteStart != -1 && nRightDeleteStart != -1)
				{
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMergeTemp;
					mergedBlock.m_nMergeEndLine = nRealLinesInMergeTemp + nDeleteCount - 1;
					mergedBlock.m_nLeftStartLine = nLeftDeleteStart;
					mergedBlock.m_nLeftEndLine = nLeftDeleteStart + nDeleteCount - 1;
					mergedBlock.m_nRightStartLine = nRightDeleteStart;
					mergedBlock.m_nRightEndLine = nRightDeleteStart + nDeleteCount - 1;
					mergedBlock.m_bHidden = true;
					
					paryDiffMergeBlock.add(mergedBlock);
					
					nRealLinesInMergeTemp += nDeleteCount;
					nDeleteCount = 0;
					nLeftDeleteStart = -1;
					nRightDeleteStart = -1;
					bHasMergedBlock = true;
				}
				
				while (k <= nMatchLines && (pToFileBuffer.GetLineFlags(curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED && 
				(pCompareFileBuffer.GetLineFlags(curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED)
				{
					if (nRightDeleteStart == -1)
					{
					    nRightDeleteStart = curDiff.m_lDestMatchFrom + k - 1 + nRinsertCount;
					    nOtherStart = curDiff.m_lOrgMatchFrom + k - 1 + nLinsertCount;
					}
					nDeleteCount ++;
					k ++;
				}
				
				if (nRightDeleteStart != -1)
				{
					SDiffMergeBlock mergedBlock = new SDiffMergeBlock();
					mergedBlock.m_nMergeStartLine = nRealLinesInMergeTemp;
					mergedBlock.m_nMergeEndLine = nRealLinesInMergeTemp + nDeleteCount - 1;
					mergedBlock.m_nLeftStartLine = nOtherStart;
					mergedBlock.m_nLeftEndLine = nOtherStart + nDeleteCount - 1;
					mergedBlock.m_bLeftIsNormal = true;
					mergedBlock.m_nRightStartLine = nRightDeleteStart;
					mergedBlock.m_nRightEndLine = nRightDeleteStart + nDeleteCount - 1;
					
					paryDiffMergeBlock.add(mergedBlock);
					
					nRealLinesInMergeTemp += nDeleteCount;
					nDeleteCount = 0;
					nRightDeleteStart = -1;
					bHasMergedBlock = true;
				}
				
				if (!bHasMergedBlock)
				{
				    nRealLinesInMergeTemp ++;
				}
				else
				{
				    bHasMergedBlock = false;
				    k -- ;
				}
			}
		}
		
		pToFileBuffer.FinishLoading();
		pCompareFileBuffer.FinishLoading();		
		pResultBuffer.FinishLoading();
	}

	public static boolean TryToAutoMerge(CDynamicTextBuffer  pBaseBuffer, CDynamicTextBuffer  pTheirsBuffer, 
			  CDynamicTextBuffer  pYoursBuffer, CDynamicTextBuffer  pResultBuffer, 
			  BaseDataObject pbHasError, BaseDataObject  pstrError)
	{	
		// Compare base and theirs
		if (DiffUsingMD4(pBaseBuffer.GetFilePath(), pTheirsBuffer.GetFilePath()))
		{
		    if (!CopyFiles(pYoursBuffer.GetFilePath(), pResultBuffer.GetFilePath(), false))
			{
				pstrError.setStringValue("Failed to save merge result.");
				pbHasError.setBooleanValue(true);
			}
			
			return true;
		}
		
		// Compare base and yours
		if (DiffUsingMD4(pBaseBuffer.GetFilePath(), pYoursBuffer.GetFilePath()))
		{
			if (!CopyFiles(pTheirsBuffer.GetFilePath(), pResultBuffer.GetFilePath(), false))
			{
			    pstrError.setStringValue("Failed to save merge result.");
			    pbHasError.setBooleanValue(true);
			}
			
			return true;
		}
	
		// Compare yours and theirs
		if (DiffUsingMD4(pYoursBuffer.GetFilePath(), pTheirsBuffer.GetFilePath()))
		{
			if (!CopyFiles(pYoursBuffer.GetFilePath(), pResultBuffer.GetFilePath(), false))
			{
				pstrError.setStringValue("Failed to save merge result.");
				pbHasError.setBooleanValue(true);
			}
			
			return true;
		}

		return false;
	}

	private static boolean IsEol(char ch)
	{
	    return ch=='\r' || ch=='\n';
	}

    private static boolean IsDosEol(String sz)
    {
    	return sz.compareTo("\r\n") == 0;
    }

    public static boolean LoadFile(CDynamicTextBuffer dynamicTextBuffer, BaseDataObject  pstrError)
    {
		try
        {
            return dynamicTextBuffer.LoadFile(pstrError);
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		return false;
    }


    //Private functions, begin
	private static void PrepareResultBuffer(CDynamicTextBuffer  pResultBuffer, 
				   CDynamicTextBuffer  pCompareFileBuffer, 
				   CDynamicTextBuffer  pToFileBuffer)
	{	
		// copy a local buffer copy for merge result
		pResultBuffer.InitNew(pToFileBuffer.GetCRLFMode());
		pResultBuffer.m_bInit = true;
		pResultBuffer.m_bReadOnly = false;
		pResultBuffer.m_bModified = true;
		pResultBuffer.m_nDefaultEncoding = pToFileBuffer.m_nDefaultEncoding;
		
		// After call InitNew, m_aryLineInfo size is 1, not 0
		pResultBuffer.m_aryLineInfo.clear();
	}

	private final static int  CHAR_ALIGN = 16;
		
	private static  int ALIGN_BUF_SIZE(int size)
	{
	    return ((size) / CHAR_ALIGN) * CHAR_ALIGN + CHAR_ALIGN;
	}
	   
	private static void GetConflictLine(SLineInfo conflictLineInfo)
	{
		String strConflict = "";
		
		int nLength = strConflict.length();
		conflictLineInfo.m_nLength = nLength;
		conflictLineInfo.m_nMax = ALIGN_BUF_SIZE(conflictLineInfo.m_nLength + 1);
		conflictLineInfo.m_pcLine = new char[conflictLineInfo.m_nMax];
		
		conflictLineInfo.m_pcLine[conflictLineInfo.m_nLength] = '\0';
		
        String str = String.valueOf(conflictLineInfo.m_pcLine);
        if (nLength > 1 && IsDosEol(str.substring(conflictLineInfo.m_nLength - 2, conflictLineInfo.m_nLength)))
        {
            conflictLineInfo.m_nEolChars = 2;
        }
        else if (nLength == 1 && IsEol(conflictLineInfo.m_pcLine[conflictLineInfo.m_nLength - 1]))
        {
            conflictLineInfo.m_nEolChars = 1;
        }
        conflictLineInfo.m_nLength -= conflictLineInfo.m_nEolChars;
		
		conflictLineInfo.m_dwFlags |= CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT;
	}

	private static void GetAllEditScript(CDynamicTextBuffer  pCompareFileBuffer, 
						CDynamicTextBuffer  pToFileBuffer, 
						SDiffResult diffResult)
	{
		int iSize = (int)(diffResult.m_aryDiffResult.size());
		
		for (int i = 0; i < iSize; i++)
		{
		    CDiffResult curDiff = (CDiffResult)diffResult.m_aryDiffResult.get(i);
			pCompareFileBuffer.m_ulNumDeletedLines += curDiff.m_lDeleteTo -
			curDiff.m_lDeleteFrom + 1;
			
			pToFileBuffer.m_ulNumInsertedLines += curDiff.m_lInsertTo -
			curDiff.m_lInsertFrom + 1;
		}
	}
	
	static void GetAllEditScript(CDynamicTextBuffer  pCompareFileBuffer, 
				CDynamicTextBuffer  pToFileBuffer, 
				SDiffResult diffResult1, 
				SDiffResult diffResult2)
	{
		int i = 0;
		int iSize1 = (int)(diffResult1.m_aryDiffResult.size());
		int iSize2 = (int)(diffResult2.m_aryDiffResult.size());
		
		CDiffResult curDiff = null;
		
		for (i = 0; i < iSize1; i++)
		{// Get all deleted and inserted line count in first diff
		    curDiff = (CDiffResult)diffResult1.m_aryDiffResult.get(i); 
			pCompareFileBuffer.m_ulNumDeletedLines += curDiff.m_lDeleteTo - 
			curDiff.m_lDeleteFrom + 1;
			
			pCompareFileBuffer.m_ulNumInsertedLines += curDiff.m_lInsertTo - 
			curDiff.m_lInsertFrom + 1;
		}
		
		for (i = 0; i < iSize2; i++)
		{// Get all deleted and inserted line count in second diff
		    curDiff = (CDiffResult)diffResult2.m_aryDiffResult.get(i);
			pToFileBuffer.m_ulNumDeletedLines += curDiff.m_lDeleteTo - 
			curDiff.m_lDeleteFrom + 1;
			
			pToFileBuffer.m_ulNumInsertedLines += curDiff.m_lInsertTo - 
			curDiff.m_lInsertFrom + 1;
		}
	}
	
	private static boolean CopyFiles(String strOrgFile, String strDestFile, boolean bFailIfExists)
	{	
	    File orgFile = new File(strOrgFile);
        File destFile = new File(strDestFile);

        if (!destFile.exists())
        {
        	if (copyFile(orgFile, destFile))
        	{
        		return destFile.setLastModified(System.currentTimeMillis());
        	}
        	else
        	{
        		return false;
        	}
        }
        
        if (bFailIfExists)
        {
            return false;
        }

        if (strOrgFile.equalsIgnoreCase(strDestFile))
        {
        	return destFile.setLastModified(System.currentTimeMillis());
        }

        boolean bReadOnly = false;
        
        if (!destFile.canWrite())
        {
            bReadOnly = true;
            if (!setFileReadonly(strDestFile, false))
            {
                return false;
            }
        }

        if (copyFile(orgFile, destFile))
        {
        	if (destFile.setLastModified(System.currentTimeMillis()))
        	{
        	    if (bReadOnly)
        	    {
        	        return destFile.setReadOnly();
        	    }
        	    
        	    return true;
        	}
        }

        if (bReadOnly)
        {
            destFile.setReadOnly();
        }
        
        return false;
	}	
	
//	Function Description:copy the content of a file(fileSrc) to another(fileDest)
	private static boolean copyFile(File fileSrc,File fileDest)
	{
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		
		try
		{
			fileInputStream = new FileInputStream(fileSrc);
			fileOutputStream = new FileOutputStream(fileDest);
			
			int iBufferSize = 1024 * 1024; //Buffer Size: 1M
			byte byaryBuffer[] = new  byte[iBufferSize];
			
			for (int i = 0; i < fileSrc.length()/iBufferSize; i++)	//if the file size big than 1M
			{
				fileInputStream.read(byaryBuffer);
				
				fileOutputStream.write(byaryBuffer);
			}
			
			byaryBuffer = null;
			
			int iNotReadLength =  (int)(fileSrc.length() % iBufferSize);
			
			if (iNotReadLength > 0)
			{
				byte byaryNotRead[] = new  byte[iNotReadLength];
				
				fileInputStream.read(byaryNotRead);
				fileOutputStream.write(byaryNotRead);
				
				byaryNotRead = null;
			}
			
			try
			{
				fileInputStream.close();
				fileInputStream = null;
				fileOutputStream.close();
				fileOutputStream = null;
			}
			catch(IOException ex)
			{
			    return false;
			}			
			
			return true;
		}
		catch(Exception e)
		{
			try
			{
				if (fileInputStream != null)
					fileInputStream.close();
				
				if (fileOutputStream != null)
					fileOutputStream.close();
			}
			catch(IOException ex)
			{
			}//try
		}//try
		
		return false;
	}
	
		//Function description:Create local folder recursively
	//						For example: if the input parameter is "c:\\windows\\a\\b"
	//									 if the path "c:\\windows" exist already,but "c:\\windows\\a" is not exist
	//									 the function will create two folder,"c:\\windows\\a" and "c:\\windows\\a\\b"
	private static boolean createLocalFolder(String strLocalFolder)
	{
		File fileToCreate = new File(strLocalFolder);
		
		if (fileToCreate.exists())
			return true;
		else
		{
			fileToCreate.mkdirs();
			
			return fileToCreate.exists();
		}
	}
	
	//	Function description:Set the specified file--strLocalFileFullName attributes,
	//						if bReadOnly == ,set the file to readonly;
	//						otherwise,set the file to writable.
	private static boolean setFileReadonly(String strLocalFileFullName,boolean bReadOnly) 
	{	
		File fileToSet  = new File(strLocalFileFullName);
		
		if (fileToSet.exists() && fileToSet.isFile())
		{		
			if (bReadOnly)
			{
				return fileToSet.setReadOnly();
			}
			else
			{
				try  
				{
					//backup the modification time of the file to set attribute
					long lTimeModification = fileToSet.lastModified();
					
					//create a temp file
					long lTime = System.currentTimeMillis();
					File fileTemp = File.createTempFile("~~~saw" + String.valueOf(lTime),".TMP");
					
					//copy the content of the file(which attribute will be set) to the temp file
					copyFile(fileToSet,fileTemp);
					
					//delete the file(which attriabute will be set)
					fileToSet.delete();
					
					//create a file which name is the same to the deleted files,
					//after creating ,the file attribute is writable
					fileToSet.createNewFile();
					
					//copy the content of the temp to the file to set 
					copyFile(fileTemp,fileToSet);
					
					//Delete the temp file
					fileTemp.delete();
					
					//Set the buckup modification time to the file
					fileToSet.setLastModified(lTimeModification); 
					
					return true;
				}
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}//try
			}//if
		}//if	
		
		return false;
	}
	
	private static long GetChecksum(String str)
	{
		if (str == null)
		{
			assert(false);
			return 0;
		}
		
		return str.hashCode();
	}
	
	//Private functions, end
	
}
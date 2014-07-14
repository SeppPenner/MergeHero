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
import java.util.ArrayList;

import javax.swing.JPanel;
import com.dynamsoft.sourceanywhere.BaseDataObject;

/*
 * Created on 2005-4-18
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
public class CDynamicTextBuffer
{
    public static void main(String[] args)
    {
    }
    
    public interface MERGE_LINEFLAGS
    {
      public static final long LF_INSERTED = 0x0001L;
      public static final long LF_TRIVIAL = 0x0002L;
      public static final long LF_DELETED = 0x0004L;
      public static final long LF_CONFLICT = 0x0008L;
      public static final long LF_GHOST = 0x0010L;
      public static final long LF_MOVED = 0x0020L;
      public static final long LF_ADDED = 0x0040L;
      public static final long LF_HIDEN = 0x0080L;
    } 
    
	public interface EnumUpdateFlag
	{
	  public static final int UPDATE_HORZRANGE = 0x0001;  // update horz scrollbar
	  public static final int UPDATE_VERTRANGE = 0x0002;  // update vert scrollbar
	  public static final int UPDATE_SINGLELINE = 0x0100; // single line has changed
	  public static final int UPDATE_FLAGSONLY = 0x0200;  // only line-flags were changed
	  public static final int UPDATE_RESET = 0x1000;       // document was reloaded, update all!
	}
	
//	 carriage return and line feed define
	public interface EnumCRLFSTYLE
	{
	  public static final int CRLF_STYLE_AUTOMATIC = -1;
	  public static final int CRLF_STYLE_DOS = 0;
	  public static final int CRLF_STYLE_UNIX = 1;
	  public static final int CRLF_STYLE_MAC = 2;
	  public static final int CRLF_STYLE_NATIVE = 3;
	}	   
    
    public final int	UNDO_BUF_SIZE = 1024;
    public final int  CHAR_ALIGN = 16;
    public int m_nDefaultEncoding = -1;
    public int m_nSourceEncoding = m_nDefaultEncoding;
    public boolean m_bInit = false;
    public boolean m_bBinaryFile = false;
    public boolean m_bReadOnly = false;
    public boolean m_bModified = false;
    public int m_nCRLFMode = 0;
    public boolean m_bEolSensitive = false;
    public boolean m_bCreateBackupFile = false;
	public CLineInfoArray m_aryLineInfo = new CLineInfoArray();
	
	public long m_ulNumInsertedLines = 0;
	public long m_ulNumDeletedLines = 0;
	// The following members only using in merge view
	public long m_ulNumMergedLines = 0;
	public long m_ulNumConflictLines = 0;	
	
	protected String m_strFilePath = "";
	
	protected static int m_nMaxString = 0;
	
	// Unicode encoding from ucr::UNICODESET 
	// m_nUnicoding and m_codepage are indications of how the buffer is supposed to be saved on disk
	// In memory, it is invariant, depending on build:
	// ANSI:
	//   in memory it is CP_ACP/CP_THREAD_ACP 8-bit characters
	// Unicode:
	//   in memory it is wchars
	public String m_strUnicoding = ""; 
	public int m_nCodepage = 0; // 8-bit codepage, if relevant m_unicoding==ucr::NONEpublic static final String EnumSaveResult = null;
	
	
	protected CDynamicTextBuffer()
	{
	  
	}
	
	public CDynamicTextBuffer(String strFilePath)
	{
	  m_strFilePath = strFilePath;
	}
	
	public void SetFilePath(String strFilePath)
	{
	  m_strFilePath = strFilePath;
	}
	
	public String GetFilePath()
	{
	  return m_strFilePath;
	}
   
    public void ResetInit() 
    {
        InitNew(EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC); 
    }
    
    public int ALIGN_BUF_SIZE(int size)
    {
        return ((size) / CHAR_ALIGN) * CHAR_ALIGN + CHAR_ALIGN;
    }
    
    public static boolean IsEol(char ch)
    {
    	return ch=='\r' || ch=='\n';
    }

    public static boolean IsDosEol(String sz)
    {
    	return sz.compareTo("\r\n") == 0;
    }

    //  Insert the same line once or several times
    //  nPosition : not defined (or -1) = add lines at the end of array
    public void InsertLine(char[] pszLine, int nLength, int nPosition,
            int nCount)
    {
        if (nLength == -1)
        {
            if (pszLine == null)
            {
                nLength = 0;
            }
            else
            {
                nLength = pszLine.length;
            }
        }

        SLineInfo lineInfo = new SLineInfo();
        lineInfo.m_nLength = nLength;
        lineInfo.m_nMax = ALIGN_BUF_SIZE(lineInfo.m_nLength + 1);
        lineInfo.m_pcLine = new char[lineInfo.m_nMax];

        if (lineInfo.m_nLength > 0)
        {
            long dwLen = lineInfo.m_nLength;
            CMemoryOperator.CopyMemory(lineInfo.m_pcLine, pszLine, (int) dwLen);
        }
        lineInfo.m_pcLine[lineInfo.m_nLength] = '\0';

        String str = String.valueOf(pszLine);
        int nEols = 0;
        if (nLength > 1 && IsDosEol(str.substring(nLength - 2, nLength)))
        {
            nEols = 2;
        }
        else if (nLength > 1 && IsEol(pszLine[nLength - 1]))
        {
            nEols = 1;
        }
        lineInfo.m_nLength -= nEols;
        lineInfo.m_nEolChars = nEols;

        // nPosition not defined ? Insert at end of array
        if (nPosition == -1)
        {
            nPosition = m_aryLineInfo.size();
        }
        
        m_aryLineInfo.add(nPosition, lineInfo);

        // insert all lines in one pass
        // duplicate the text data for lines after the first one
        for (int i = 1; i < nCount; i++)
        {
            SLineInfo lineInfo1 = new SLineInfo();
            lineInfo1.m_pcLine = new char[lineInfo.m_nMax];

            CMemoryOperator.CopyMemory(lineInfo1.m_pcLine, lineInfo.m_pcLine,
                    lineInfo.Length());
            m_aryLineInfo.add(nPosition + i, lineInfo1);
        }
    }
    
    public void InsertConflictLine(int nPosition, int nCount)
    {
    	if (nCount > 0)
    	{
    		SLineInfo lineInfo = new SLineInfo();
    		lineInfo.m_nLength = 0;
    		lineInfo.m_nEolChars = 0;
    		lineInfo.m_nMax = ALIGN_BUF_SIZE(lineInfo.m_nLength + 1);

    		// nPosition not defined ? Insert at end of array
    		if (nPosition == -1)
    		{
    			nPosition = m_aryLineInfo.size();
    		}

    		for (int i = 0; i < nCount; i++) 
    		{
    			lineInfo.m_pcLine = new char[lineInfo.m_nMax];
    			lineInfo.m_pcLine[0] = '\0';
    			// insert all lines in one pass
    			m_aryLineInfo.add(nPosition + i, lineInfo);
    			SetLineFlag(nPosition + i, MERGE_LINEFLAGS.LF_CONFLICT | MERGE_LINEFLAGS.LF_MOVED, true, false, false);
    		}
    	}
    }
    
    public void InsertEmptyLine(int nPosition, int nCount)
    {
    	if (nCount > 0)
    	{
    		SLineInfo lineInfo = new SLineInfo();
    		lineInfo.m_nLength = 0;
    		lineInfo.m_nEolChars = 0;
    		lineInfo.m_nMax = ALIGN_BUF_SIZE(lineInfo.m_nLength + 1);

    		// nPosition not defined ? Insert at end of array
    		if (nPosition == -1)
    		{
    			nPosition = m_aryLineInfo.size();
    		}

    		for (int i = 0; i < nCount; i++) 
    		{
    			lineInfo.m_pcLine = new char[lineInfo.m_nMax];
    			lineInfo.m_pcLine[0] = '\0';
    			// insert all lines in one pass
    			m_aryLineInfo.add(nPosition + i, lineInfo);
    			SetLineFlag(nPosition + i, MERGE_LINEFLAGS.LF_GHOST, true, false, false);
    		}
    	}
    }
    

    //  Add characters to end of specified line
    //  Specified line must not have any EOL characters
    public void AppendLine(int nLineIndex, char[] pszChars, int nLength)
    {
        if (nLength == -1)
        {
            if (pszChars == null)
            {
                return;
            }
            nLength = pszChars.length;
        }

        if (nLength == 0)
        {
            // this line is empty line
    		SLineInfo lineInfo = new SLineInfo();
    		lineInfo.m_nLength = 0;
    		lineInfo.m_nEolChars = 0;
    		lineInfo.m_nMax = ALIGN_BUF_SIZE(lineInfo.m_nLength + 1);

    		lineInfo.m_pcLine = new char[lineInfo.m_nMax];
    		lineInfo.m_pcLine[0] = '\0';
    		lineInfo.m_dwFlags = 0;
    		// insert all lines in one pass
    		m_aryLineInfo.add(lineInfo);   		
            return;
        }
        
        m_nMaxString = m_nMaxString > nLength ? m_nMaxString : nLength;
        
        SLineInfo lineInfo = new SLineInfo();
        int nBufNeeded = lineInfo.m_nLength + nLength + 1;

        if (nBufNeeded > lineInfo.m_nMax)
        {
            lineInfo.m_nMax = ALIGN_BUF_SIZE(nBufNeeded);

            char[] pcNewBuf = new char[lineInfo.m_nMax];
            if (lineInfo.FullLength() > 0)
            {
                CMemoryOperator.CopyMemory(pcNewBuf, lineInfo.m_pcLine,
                        (lineInfo.FullLength() + 1));
            }

            lineInfo.m_pcLine = null;
            lineInfo.m_pcLine = pcNewBuf;
        }

        for (int i = 0; i < nLength; i++)
        {
            lineInfo.m_pcLine[lineInfo.m_nLength + i] = pszChars[i];
        }
        lineInfo.m_nLength += nLength;
        lineInfo.m_pcLine[lineInfo.m_nLength] = '\0';

        // Did line gain eol ? (We asserted above that it had none at start)
        String str = String.valueOf(lineInfo.m_pcLine);
        if (nLength > 1 && IsDosEol(str.substring(lineInfo.m_nLength - 2, lineInfo.m_nLength)))
        {
            lineInfo.m_nEolChars = 2;
        }
        else if (IsEol(lineInfo.m_pcLine[lineInfo.m_nLength - 1]))
        {
            lineInfo.m_nEolChars = 1;
        }
        lineInfo.m_nLength -= lineInfo.m_nEolChars;
        m_aryLineInfo.add(lineInfo);
    }
    
    public void CopyLine(SLineInfo destLine, SLineInfo srcLine)
    {
    	if (destLine == null)
    	{
    		return;
    	}

    	if (srcLine.m_pcLine == null)
    	{// if original line is empty, the destination line must insert an empty line
    		destLine.m_nLength = 0;
    		destLine.m_nEolChars = 0;
    		destLine.m_nMax = ALIGN_BUF_SIZE(destLine.m_nLength + 1);
    		
    		destLine.m_pcLine = new char[destLine.m_nMax];
    		destLine.m_pcLine[0] = '\0';

    		// Must copy the flags, in spite of the original line is null
    		destLine.m_dwFlags = srcLine.m_dwFlags;

    		return;
    	}

    	if (destLine.m_pcLine != null)
    	{
     		destLine.m_pcLine = null;
    	}

    	destLine.m_pcLine = new char[srcLine.m_nMax];
    	CMemoryOperator.CopyMemory(destLine.m_pcLine, srcLine.m_pcLine, srcLine.m_pcLine.length);

    	destLine.m_dwFlags = srcLine.m_dwFlags;
    	destLine.m_nEolChars = srcLine.m_nEolChars;
    	destLine.m_nLength = srcLine.m_nLength;
    	destLine.m_nMax = srcLine.m_nMax;
    }
    
    public void ReplaceLine(int nLine, SLineInfo srcLine)
    {
    	if (nLine >= m_aryLineInfo.size() || srcLine.m_pcLine.length == 0)
    	{
    		return;
    	}
    	
    	CopyLine((SLineInfo)m_aryLineInfo.get(nLine), srcLine);
    }

    public void MoveLine(int nStartLineMF, int nEndlineMF, int nNewStartLineMT)
    {
    	int ndiff = nNewStartLineMT - nStartLineMF;
    	SLineInfo lineInfo = null;

    	if (ndiff > 0)
    	{
    		for (int i = nEndlineMF; i >= nStartLineMF; i--)
    		{
    		    lineInfo = (SLineInfo)m_aryLineInfo.get(i+ndiff);
    			lineInfo = (SLineInfo)m_aryLineInfo.get(i);
    		}
    	}
    	else if (ndiff < 0)
    	{
    		for (int i = nStartLineMF; i <= nEndlineMF; i++)
    		{
       		    lineInfo = (SLineInfo)m_aryLineInfo.get(i+ndiff);
    			lineInfo = (SLineInfo)m_aryLineInfo.get(i);
    		}
    	}
    }

    public void SetEmptyLine(int nPosition, int nCount)
    {
    	if (nCount > 0)
    	{
    		SLineInfo lineInfo = new SLineInfo();
    		lineInfo.m_nLength = 0;
    		lineInfo.m_nEolChars = 0;
    		lineInfo.m_nMax = ALIGN_BUF_SIZE(lineInfo.m_nLength + 1);

    		SLineInfo linefoTemp = null;
    		for (int i = 0; i < nCount; i++) 
    		{
    		    linefoTemp = (SLineInfo)m_aryLineInfo.get(nPosition+i);
    		    linefoTemp = lineInfo;
    		    linefoTemp.m_pcLine = new char[lineInfo.m_nMax];
    		    linefoTemp.m_pcLine[0] = '\0';
    		}
    	}
    }
    
    public boolean InitNew(int nCrlfStyle)
    {
    	InsertLine("".toCharArray(), -1, -1, 1);
    	m_bInit = true;
    	m_bReadOnly = true;
    	m_nCRLFMode = nCrlfStyle;
    	m_bModified = false;
		m_bBinaryFile = false;		
    	
    	m_ulNumConflictLines = 0;
    	m_ulNumDeletedLines = 0;
    	m_ulNumInsertedLines = 0;
    	m_ulNumMergedLines = 0;

    	return true;
    }
    
    public boolean GetReadOnly() 
    {
    	return m_bReadOnly;
    }

    public void SetReadOnly(boolean bReadOnly)
    {
     	m_bReadOnly = bReadOnly;
    }

    public int GetCRLFMode()
    {
    	return m_nCRLFMode;
    }

//     Default EOL to use if editor has to manufacture one
//     (this occurs with ghost lines)
    public void SetCRLFMode(int nCRLFMode)
    {
    	if (nCRLFMode == EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC)
    	{
    		nCRLFMode = EnumCRLFSTYLE.CRLF_STYLE_DOS;
    	}

    	m_nCRLFMode = nCRLFMode;
      }

    public boolean ApplyEOLMode()
    {
    	String lpEOLtoApply = GetDefaultEol();
    	boolean bChanged = false;

    	for (int i = 0 ; i < m_aryLineInfo.size() ; i++)
    	{
    	    SLineInfo lineInfo = (SLineInfo)m_aryLineInfo.get(i);
    		// the last real line has no EOL
    		if (lineInfo.m_nEolChars == 0)
    		{
    			continue;
    		}

    		bChanged |= ChangeLineEol(i, lpEOLtoApply);
    	}
    	
    	if (bChanged)
    	{
    		SetModified(true);
    	}
    	
    	return bChanged;
    }

    public int GetLineCount()
    {
    	return m_aryLineInfo.size();
    }
    
    public String GetLineText(int nLine)
    {
        SLineInfo lineInfo = (SLineInfo)m_aryLineInfo.get(nLine);
        return String.valueOf(lineInfo.m_pcLine).substring(0, lineInfo.Length());
    }
    
//  number of characters in line (excluding any trailing eol characters)
    public int GetLineLength(int nLine)
    {
        SLineInfo lineInfo = (SLineInfo)m_aryLineInfo.get(nLine);
    	return lineInfo.m_nLength;
    }

//     number of characters in line (including any trailing eol characters)
    public int GetFullLineLength(int nLine)
    {
        SLineInfo lineInfo = (SLineInfo)m_aryLineInfo.get(nLine);
    	return lineInfo.m_nLength + lineInfo.m_nEolChars;
    }

//     get pointer to any trailing eol characters (pointer to empty string if none)
    public String GetLineEol(int nLine) 
    {
        SLineInfo lineInfo = (SLineInfo)m_aryLineInfo.get(nLine);
     	if (lineInfo.m_nEolChars > 0)
    	{
    	    String str = String.valueOf(lineInfo.m_pcLine);
    		return str.substring(lineInfo.Length(), lineInfo.Length() + lineInfo.m_nEolChars);
    	}
    	else
    	{
    		return "";
    	}
    }

    public boolean ChangeLineEol(int nLine, String lpEOL) 
    {
    	SLineInfo  li = (SLineInfo)m_aryLineInfo.get(nLine);
    	int nNewEolChars = lpEOL.length();
    	if (nNewEolChars == li.m_nEolChars)
    	{
    	    for (int i = 0; i < lpEOL.length(); i++)
    	    {
    	        if (li.m_pcLine[i + li.Length()] != lpEOL.charAt(i))
    	        {
    	            return false;
    	        }
     	    }
    	}
    		
    	int nBufNeeded = li.m_nLength + nNewEolChars+1;
    	if (nBufNeeded > li.m_nMax)
    	{
    		li.m_nMax = ALIGN_BUF_SIZE (nBufNeeded);
    		char[] pcNewBuf = new char[li.m_nMax];
    		if (li.FullLength() > 0)
    		{
    		    CMemoryOperator.CopyMemory(pcNewBuf, li.m_pcLine, (li.FullLength()+1));
    		}
    		li.m_pcLine = pcNewBuf;
    	}
    		
    	// copy also the 0 to zero-terminate the line
    	for (int i = 0; i < (nNewEolChars); i++ )
    	{
    	    li.m_pcLine[li.m_nLength + i] = lpEOL.charAt(i);
    	}
    	// set zero-terminate
    	li.m_pcLine[li.m_nLength + nNewEolChars] = '\0';
    	            
    	li.m_nEolChars = nNewEolChars;
    	
    	// modified
    	return true;
    }

    public char[] GetLineChars(int nLine)
    {
     	// You must call InitNew() or LoadFromFile() first!
    	if (nLine >= GetLineCount())
    	{
    		return null;
    	}
    	SLineInfo li = (SLineInfo)m_aryLineInfo.get(nLine);
    	
    	return li.m_pcLine;
    }
    
    public long GetLineFlags(int nLine)
    {
    	if (nLine >= GetLineCount())
    	{
    		return 0;
    	}

    	SLineInfo li = (SLineInfo)m_aryLineInfo.get(nLine);
    	
    	return li.m_dwFlags;
    }
    
    public static int FlagToIndex(long dwFlag)
    {
    	int nIndex = 0;
    	while ((dwFlag & 1) == 0)
        {
    		dwFlag = dwFlag >> 1;
    		nIndex++;
    		if (nIndex == 32)
    		{
    			return -1;
    		}
        }

    	dwFlag = dwFlag & 0xFFFFFFFE;
    	if (dwFlag != 0)
    	{
    		return -1;
    	}
    	return nIndex;
    }

    public int FindLineWithFlag(long dwFlag)
    {
    	int nSize = m_aryLineInfo.size();
    	for (int i = 0; i < nSize; i++)
        {
    	    SLineInfo li = (SLineInfo)m_aryLineInfo.get(i);
    		if ((li.m_dwFlags & dwFlag) != 0)
    		{
    			return i;
    		}
        }

    	return -1;
    }

    public int GetLineWithFlag(long dwFlag)
    {
    	int nFlagIndex = FlagToIndex(dwFlag);
    	if (nFlagIndex < 0)
        {
    		return -1;
        }
    	return FindLineWithFlag (dwFlag);
    }

    public void SetLineFlag(int nLine, long dwFlag, boolean bSet, 
    							  boolean bRemoveFromPreviousLine, boolean bUpdate)
    {
    	if (nLine == -1)
        {
    		nLine = FindLineWithFlag (dwFlag);
    		if (nLine == -1)
    		{
    			return;
    		}
    		bRemoveFromPreviousLine = false;
        }
    	
    	SLineInfo li = (SLineInfo)m_aryLineInfo.get(nLine);
    	long dwNewFlags = li.m_dwFlags;
    	if (bSet)
    	{
    		if (dwFlag == 0)
    		{
    			dwNewFlags = 0;
    		}
    		else
    		{
    			dwNewFlags = dwNewFlags | dwFlag;
    		}
    	}
    	else
    	{
    		dwNewFlags = dwNewFlags & ~dwFlag;
    	}
    	
    	if (li.m_dwFlags != dwNewFlags)
        {
    		if (bRemoveFromPreviousLine)
            {
    			int nPrevLine = FindLineWithFlag (dwFlag);
    			if (bSet)
                {
    				if (nPrevLine >= 0)
                    {
    					li.m_dwFlags &= ~dwFlag;
    					if (bUpdate)
    					{
    						UpdateViews(null, null, EnumUpdateFlag.UPDATE_SINGLELINE | EnumUpdateFlag.UPDATE_FLAGSONLY, nPrevLine);
    					}
                    }
                }
             }
    		
    		li.m_dwFlags = dwNewFlags;
    		if (bUpdate)
    		{
    			UpdateViews(null, null, EnumUpdateFlag.UPDATE_SINGLELINE | EnumUpdateFlag.UPDATE_FLAGSONLY, nLine);
    		}
        }
    }
    
    public void SetLineFlag(int nLine, long dwAddFlag, long dwRemoveFlag, boolean bReset)
    {
    	if (nLine < 0 || nLine >= GetLineCount())
    	{
    		return;
    	}

    	SLineInfo li = (SLineInfo)m_aryLineInfo.get(nLine);
    	long dwNewFlags = li.m_dwFlags;

    	dwNewFlags = dwNewFlags & ~dwRemoveFlag;

    	if (bReset)
    	{
    		dwNewFlags = 0;
    	}

    	dwNewFlags = dwNewFlags | dwAddFlag;

    	li.m_dwFlags = dwNewFlags;
    }

    public void SetLinesFlag(int nStartLine, int nEndLine, long dwFlag)
    {
    	for (int i = nStartLine; i <= nEndLine; i++)
    	{
    		SetLineFlag(i, dwFlag, 1, false);
    	}
    }

//     Get text of specified line range (excluding ghost lines)
    public void GetTextWithoutEmptys(int nStartLine, int nStartChar,
    									   int nEndLine, int nEndChar, 
    									   BaseDataObject strText, int nCrlfStyle)
    {
    	String sEol = GetStringEol(nCrlfStyle);
    	GetText(nStartLine, nStartChar, nEndLine, nEndChar, strText, sEol.toCharArray());
    }

    public void GetText(int nStartLine, int nStartChar, int nEndLine, int nEndChar,
            BaseDataObject strText, char[] pszCRLF)
    {
     	if (pszCRLF == null)
    	{
    		pszCRLF = Common.szCRLF.toCharArray();
    	}
    	int nCRLFLength = pszCRLF.length;

    	SLineInfo li = null;
    	int nBufSize = 0;
    	for (int L = nStartLine; L <= nEndLine; L++)
        {
    	    li = (SLineInfo)m_aryLineInfo.get(L);
    		nBufSize += li.m_nLength;
    		nBufSize += nCRLFLength;
        }
    	
    	char[] pszBuf = new char[nBufSize];
    	int copied = 0;
     	
    	if (nStartLine < nEndLine)
        {
    	    li = (SLineInfo)m_aryLineInfo.get(nStartLine);
    		int nCount = li.m_nLength - nStartChar;
    		if (nCount > 0)
            {
    		    for (int i = 0; i < nCount; i++)
    		    {
    		        pszBuf[i] = li.m_pcLine[nStartChar + i];
    		    }
    		    copied += nCount;
            }
    		for (int i = 0; i < nCRLFLength; i++)
    		{
    		    pszBuf[i + copied] = pszCRLF[i];
    		}
    		copied += nCRLFLength;
    		
    		for (int I = nStartLine + 1; I < nEndLine; I++)
            {
    		    li = (SLineInfo)m_aryLineInfo.get(I);
    			nCount = li.m_nLength;
    	   		if (nCount > 0)
                {
        		    for (int i = 0; i < nCount; i++)
        		    {
        		        pszBuf[i + copied] = li.m_pcLine[i];
        		    }
        		    copied += nCount;
                }
        		for (int i = 0; i < nCRLFLength; i++)
        		{
        		    pszBuf[i + copied] = pszCRLF[i];
        		}
        		copied += nCRLFLength;
            }
    		if (nEndChar > 0)
            {
    		    li = (SLineInfo)m_aryLineInfo.get(nEndLine);
    		    for (int i = 0; i < nEndChar; i++)
    		    {
    		        pszBuf[i + copied] = li.m_pcLine[i];
    		    }
    		    copied += nEndChar;
            }
        }
    	else
        {
    		int nCount = nEndChar - nStartChar;
    		li = (SLineInfo)m_aryLineInfo.get(nStartLine);
   		    for (int i = 0; i < nCount; i++)
		    {
		        pszBuf[i + copied] = li.m_pcLine[nStartChar + i];
		    }
   		 copied += nCount;
        }

    	pszBuf[copied] = 0;
    	strText.setStringValue(String.valueOf(pszBuf));
     }

    public void UpdateViews(JPanel pSource, CUpdateContext pContext,
    							   long dwUpdateFlags, int nLineIndex)
    {
        
    }

   
//     Remove the last [bytes] characters from specified line, and return them
//     (EOL characters are included)
    public String StripTail (int i, int nBytes)
    {
    	SLineInfo li = (SLineInfo)m_aryLineInfo.get(i);

     	int offset = li.FullLength() - nBytes;
    	
     	char[] sz = new char[nBytes];
     	for (int j = 0; j < nBytes; j++)
     	{
     	    sz[j] = li.m_pcLine[j + offset];
     	}
     	
    	String str = String.valueOf(sz);
    	li.m_nLength = offset;
    	li.m_nEolChars = 0;

    	return str;
    }
    
    public boolean HasEol(char[] szText)
    {
    	int len = szText.length;
    	return (len > 0 && IsEol(szText[len-1]));
    }

    public String GetStringEol(int nCRLFMode)
    {
    	switch(nCRLFMode)
    	{
    	case EnumCRLFSTYLE.CRLF_STYLE_DOS: return "\r\n";

    	case EnumCRLFSTYLE.CRLF_STYLE_UNIX: return "\n";

    	case EnumCRLFSTYLE.CRLF_STYLE_MAC: return "\r";

    	default: return "\r\n";
    	}
    }

    public String GetDefaultEol()
    {
    	switch(m_nCRLFMode)
    	{
    	case EnumCRLFSTYLE.CRLF_STYLE_DOS: return ("\r\n");

    	case EnumCRLFSTYLE.CRLF_STYLE_UNIX: return ("\n");

    	case EnumCRLFSTYLE.CRLF_STYLE_MAC: return ("\r");

    	default: return ("\r\n");
    	}
    }
       
    public void SetModified(boolean bModified)
    {
    	m_bModified = bModified;
    }

//     Delete one or several lines
    public void DeleteLine(int line, int nCount)
    {
    	for (int i = nCount - 1; i >= 0; i--)
    	{
    		m_aryLineInfo.remove(line+i);
    	}
    }
  
    public int GetMaxString()
    {
        return m_nMaxString;
    }
    
    public void FreeAll()
    {
        //  Free text
    	m_aryLineInfo.clear();
    	
    	// Undo buffer will be cleared by its destructor
    	m_bInit = false;
    	m_bReadOnly = true;
     	m_bModified = false;
		m_bBinaryFile = false;
    	
    	m_ulNumConflictLines = 0;
    	m_ulNumDeletedLines = 0;
    	m_ulNumInsertedLines = 0;
    	m_ulNumMergedLines = 0;	
    }
	
	// A SRealityBlock is a block of lines with no ghost lines
	protected class SRealityBlock
	{ 
		public int m_nStartReal = 0; 
		public int m_nStartApparent = 0; 
		public int m_nCount = 0; 
	}

	// The array of reality blocks is kept in order
	protected class SRealityBlockArray extends ArrayList
	{
//		override the parent member functio
		public void add(int iIndex, Object SRealityBlockToAdd) //insert
		{
			if (SRealityBlockToAdd instanceof SRealityBlock)
				super.add(iIndex, SRealityBlockToAdd);
		}
		
		public boolean add(Object SRealityBlockToAdd)
		{
			if (SRealityBlockToAdd instanceof SRealityBlock)
				return super.add(SRealityBlockToAdd);
			else
				return false;
		}
		
		public Object set(int iIndex, Object SRealityBlockToAdd)
		{
			if (SRealityBlockToAdd instanceof SRealityBlock)
				return super.set(iIndex, SRealityBlockToAdd);
			else
				return null;
		}
	}	
	
	protected SRealityBlockArray	m_arySRealityBlocks = new SRealityBlockArray();
 
	//	 Do what we need to do just after we've been reloaded
	public void FinishLoading()
	{
		if (!m_bInit)
		{
			return;
		}

		RecomputeRealityMapping();
	}
    
//	 apparent <-> real line conversion
//	 Return apparent line of highest real (file) line. 
//	 Return -1 if no lines.
	protected int ApparentLastRealLine()
	{
		int nMax = m_arySRealityBlocks.size() - 1;

		if (nMax < 0)
		{
			return -1;
		}

		SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(nMax);

		return (block.m_nStartApparent + block.m_nCount - 1);
	}	
	
//	 Recompute the reality mapping (this is fairly naive)
	protected void RecomputeRealityMapping()
	{
		m_arySRealityBlocks.clear();

		int reality = -1; // last encountered real line
		int i = 0; // current line
		SRealityBlock block = new SRealityBlock(); // current block being
                                                   // traversed (in state 2)
		
		// This is a state machine with 2 states
		
		// state 1, i-1 not real line
		while(true)
		{
		    if (i==GetLineCount())
		    {
		        return;
		    }
		    if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) == MERGE_LINEFLAGS.LF_GHOST)
		    {
		        ++i;
		    }
		    else
		    {
		        break;
		    }
		}

		// this is the first line of a reality block
		block.m_nStartApparent = i;
		block.m_nStartReal = reality + 1;
		++reality;
		++i;
		// fall through to other state
		
		// state 2, i-1 is real line
		while (true)
		{
		if (i==GetLineCount() || (GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) == MERGE_LINEFLAGS.LF_GHOST)
		{
			block.m_nCount = i - block.m_nStartApparent;

			m_arySRealityBlocks.add(block);
			if (i==GetLineCount())
			{
				return;
			}

			++i;
			
			while(true)
			{
			    if (i==GetLineCount())
			    {
			        return;
			    }
			    if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) == MERGE_LINEFLAGS.LF_GHOST)
			    {
			        ++i;
			    }
			    else
			    {
			        break;
			    }
			}

			// this is the first line of a reality block
			block = new SRealityBlock();
			block.m_nStartApparent = i;
			block.m_nStartReal = reality + 1;
			++reality;
			++i;
		}
		else
		{
		++reality;
		++i;
		}
		}
	}	
	
//	 Load file from disk into buffer
//	 return RESULT_OK or RESULT_OK_IMPURE (load OK, but the EOL are of different types)
//	 or an error code (list in files.h)
//	 If this method fails, it calls InitNew so the CDiffTextBuffer is in a valid state
	public boolean LoadFile(BaseDataObject strError) throws UnsupportedEncodingException,
           FileNotFoundException, IOException
   {
	   assert(m_strFilePath.length() != 0);
	   FreeAll();
 
       String sExt = null;
       int nRetVal = EnumLoadResult.Load_Ok;
       
       String error;
       error = "Failed to open file.";
       error += m_strFilePath;
 
       CCusMemFile memFile = new CCusMemFile();
       CCusFile pFile = memFile;

       try
       {
           // Now we only use the UniFile interface
           // which is something we could implement for HTTP and/or FTP files
           if (!pFile.OpenReadOnly(m_strFilePath))
           {
               nRetVal = EnumLoadResult.Load_Error;
               InitNew(EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC); // leave crystal
                                                            // editor
               // in valid, empty
               // state
               pFile.Close();
               strError.setStringValue(error);
               return false;
           }
           else
           {
               // Recognize Unicode files with BOM (byte order mark)
               // or else, use the codepage we were given to interpret the
               // 8-bit
               // characters
               if (!pFile.ReadBom())
               {
                   pFile.SetCodepage(0);
               }

               int unLineno = 0;
               BaseDataObject strLine = new BaseDataObject();
               BaseDataObject strEol = new BaseDataObject();
               String strPrevEol = "";
               boolean bDone = false;

               int next_line_report = 100; // for trace messages
               int next_line_multiple = 5; // for trace messages

               // preveol must be initialized for empty files
               strPrevEol = "\n";

               do
               {
                   bDone = !pFile.ReadString(strLine, strEol);
                   
                   if (pFile.GetTxtStats().m_nzeros > 0)
                   {
                       ResetInit(); // leave crystal editor in valid, empty
                                    // state
                       pFile.Close();
                       m_bBinaryFile = true;
                       return true;
                   }
                   // if last line had no eol, we can quit
                   if (bDone && strPrevEol.length() == 0)
                   {
                       break;
                   }
                   // but if last line had eol, we add an extra (empty) line to
                   // buffer
                   String strtemp = strLine.getStringValue();
                   strtemp += strEol.getStringValue();
                   strLine.setStringValue(strtemp);
                   AppendLine(unLineno,
                           strLine.getStringValue().toCharArray(), strLine
                                   .getStringValue().length());
                   ++unLineno;
                   strPrevEol = strEol.getStringValue();

               }
               while (!bDone);
           }
       }
       catch (Exception e)
       {
           e.printStackTrace();
           nRetVal = EnumLoadResult.Load_Error;
           InitNew(EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC); // leave crystal
           // editor in valid,
           // empty state
           pFile.Close();
           strError.setStringValue(error);
           
           return false;
       }

       //Try to determine current CRLF mode (most frequent)
       if (m_nCRLFMode == EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC)
       {
           m_nCRLFMode = GetTextFileStyle(pFile.GetTxtStats());
       }

       SetCRLFMode(m_nCRLFMode);

       m_bInit = true;
       m_bModified = false;
 
       FinishLoading();
 
       // stash original encoding away
       m_strUnicoding = pFile.GetUnicoding();
       m_nCodepage = pFile.GetCodepage();
       
       pFile.Close();

       return true;
   }
	
	public boolean SaveFile(BaseDataObject strError)
	{
	    assert(m_strFilePath.length() != 0);
	    
	    return SaveFile(m_strFilePath, strError);
	}
	
//	 Saves file from buffer to disk
//	 bTempFile : FALSE if we are saving user files and
//	 TRUE if we are saving workin-temp-files for diff-engine
//	 return SAVE_DONE or an error code (list in MergeDoc.h)
	public boolean SaveFile (String strFileName, BaseDataObject strError)
	{
		if (strFileName.length() == 0)
		{
			return false;	// No filename, cannot save...
		}

		if (m_nCRLFMode == EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC) // &&!mf->m_options.GetInt(OPT_ALLOW_MIXED_EOL))
		{
			// get the default nCrlfStyle of the CDiffTextBuffer
		    m_nCRLFMode = GetCRLFMode();
		}

		boolean bOpenSuccess = true;
		boolean bSaveSuccess = false;
		
	   String error;
       error = "Failed to open file.";
	   error += strFileName;

		CCusStdioFile file = new CCusStdioFile();
		file.SetUnicoding(m_strUnicoding);
		file.SetCodepage(m_nCodepage);

		try
       {
           bOpenSuccess = !!file.OpenCreate(strFileName);
 
           if (!bOpenSuccess)
           {
               strError.setStringValue(error);
               return false;
           }

           //file.WriteBom();

           // line loop : get each real line and write it in the file
           String strLine = "";
           String strEol = GetStringEol(m_nCRLFMode);
           int nLineCount = m_aryLineInfo.size();
           int iWritten = 0;
           for (int nLine = 0; nLine < nLineCount; ++nLine)
           {
               long dwFlags = GetLineFlags(nLine);
               if ((dwFlags & MERGE_LINEFLAGS.LF_GHOST) > 0 ||
                       (dwFlags & MERGE_LINEFLAGS.LF_DELETED) == MERGE_LINEFLAGS.LF_DELETED ||
           			(dwFlags & MERGE_LINEFLAGS.LF_HIDEN) == MERGE_LINEFLAGS.LF_HIDEN)
               {
                   continue;
               }

               // get the characters of the line (excluding EOL)
               if (GetLineLength(nLine) > 0)
               {
                   strLine = String.valueOf(this.GetLineText(nLine));
                   iWritten = this.GetLineLength(nLine);
              }
               else
               {
                   strLine = "";
                   iWritten = 0;
               }

               // last real line ?
               if (nLine == ApparentLastRealLine())
               {
                   // write the line and exit loop
                   file.WriteString(strLine, iWritten);
                   break;
               }

               // normal real line : append an EOL
               if (m_nCRLFMode == EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC
                       || m_nCRLFMode == EnumCRLFSTYLE.CRLF_STYLE_NATIVE)
               {
                   // either the EOL of the line (when preserve original EOL
                   // chars is on)
                   strLine += GetLineEol(nLine);
                   iWritten += GetLineEol(nLine).length();
               }
               else
               {
                   // or the default EOL for this file
                   strLine += strEol;
                   iWritten += strEol.length();
               }

               // write this line to the file (codeset or unicode conversions are done there)
               file.WriteString(strLine, iWritten);
               strLine = "";
               iWritten = 0;
           }
           file.Close();
       }
       catch (Exception e)
       {
           e.printStackTrace();
           file.Close();
           strError.setStringValue(error);
           return false;
       }

       bSaveSuccess = true;
       file.Close();

		if (bSaveSuccess)
		{
		    m_bModified = false;
			return true;
		}
		else
		{
		    error = "Failed to save file.";
		    strError.setStringValue(error);
			return false;
		}
	}	
	
	public int GetTextFileStyle(CCusMemFile.STxtstats stats)
	{
		if (stats.m_ncrlfs >= stats.m_nlfs)
		{
			if (stats.m_ncrlfs >= stats.m_ncrs)
			{
				return EnumCRLFSTYLE.CRLF_STYLE_DOS;
			}
			else
			{
				return EnumCRLFSTYLE.CRLF_STYLE_MAC;
			}
		}
		else
		{
			if (stats.m_nlfs >= stats.m_ncrs)
			{
				return EnumCRLFSTYLE.CRLF_STYLE_UNIX;
			}
			else
			{
				return EnumCRLFSTYLE.CRLF_STYLE_MAC;
			}
		}
	}

	public boolean IsTextFileStylePure(CCusMemFile.STxtstats stats)
	{
		int nType = 0;
		nType += (stats.m_ncrlfs > 0) ? 1 : 0;
		nType += (stats.m_ncrs > 0) ? 1 : 0;
		nType += (stats.m_nlfs > 0) ? 1 : 0;

		return (nType <= 1);
	} 		
}

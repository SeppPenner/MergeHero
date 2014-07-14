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
import java.util.ArrayList;

import javax.swing.JPanel;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CPoint;
import MergeHeroLib.CUpdateContext;
import MergeHeroLib.SLineInfo;

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
public class CTextBuffer extends CDynamicTextBuffer
{
    public static void main(String[] args)
    {
    }
    
    public CTextBuffer(String strFilePath)
    {
        super(strFilePath);
    }
    
    /**
     * 
     */
    protected CTextBuffer()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public final int	UNDO_BUF_SIZE = 1024;
    public final int  CHAR_ALIGN = 16;
    public int m_nUndoBufSize = 0;
    public boolean m_bInsertTabs = true;
    public int  m_nTabSize = 4;
	private static int m_nMaxString = 0;
	// Position where the last change was made.
    public CPoint m_ptLastChange = new CPoint(-1, -1);
    // Connected views
    ArrayList m_lpViews = new ArrayList();
	CDeleteContext context = new CDeleteContext();
	boolean m_bAdjustLine = false;
    
    public void ResetInit() 
    {
        FreeAll();
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
    
    public boolean InitNew(int nCrlfStyle)
    {
        super.InitNew(nCrlfStyle);
        
     	m_bInsertTabs	= true;
    	m_nTabSize = 4;
    	m_nUndoBufSize = UNDO_BUF_SIZE;

    	UpdateViews(null, null, EnumUpdateFlag.UPDATE_RESET, -1);
    	m_ptLastChange.x = m_ptLastChange.y = -1;

    	return true;
    }

    public void AddView (CTextView pView)
    {
    	m_lpViews.add(pView);
    }

    public void RemoveView(CTextView pView)
    {
        for (int i = 0; i < m_lpViews.size(); i++)
        {
            CTextView temp = (CTextView)m_lpViews.get(i);
            if (pView.equals(temp))
            {
                m_lpViews.remove(i);
                return;
            }
        }
    }

    public void UpdateViews(JPanel pSource, CUpdateContext pContext,
    							   long dwUpdateFlags, int nLineIndex)
    {
    	if (pSource == null)
    	{
    		return;
    	}
        for (int i = 0; i < m_lpViews.size(); i++)
        {
            CTextView temp = (CTextView)m_lpViews.get(i);
            if (pSource.equals(temp))
            {
                temp.UpdateView((CTextView)pSource, pContext, dwUpdateFlags, nLineIndex);
            }
        }
    }
    
 //  InternalDeleteText uses only apparent line numbers
    public boolean InternalDeleteText(CTextView pSource, int nStartLine, int nStartChar, int nEndLine, int nEndChar)
    {
    	if (m_bReadOnly)
    	{
    		return false;
    	}
    	
    	context.m_ptStart.y = nStartLine;
    	context.m_ptStart.x = nStartChar;
    	context.m_ptEnd.y = nEndLine;
    	context.m_ptEnd.x = nEndChar;
    	/*
    	if (nStartLine == nEndLine)
        {
    		// delete part of one line
    		SLineInfo li = (SLineInfo)m_aryLineInfo.get(nStartLine);
    		if (nEndChar < li.Length() || li.m_nEolChars != 0)
            {
    			// preserve characters after deleted range by shifting up
    		    for (int i = 0; i < (li.FullLength() - nEndChar); i++)
    		    {
    		        li.m_pcLine[nStartChar + i] =  li.m_pcLine[nEndChar + i];
    		    }
            }
    		li.m_nLength -= (nEndChar - nStartChar);
    		li.m_pcLine[li.FullLength()] = '\0';
    		
    		if (pSource != null)
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_SINGLELINE | EnumUpdateFlag.UPDATE_HORZRANGE, nStartLine);
    		}
        }
    	else
        {
    	    SLineInfo liEnd = (SLineInfo)m_aryLineInfo.get(nEndLine);
    	    SLineInfo liStart = (SLineInfo)m_aryLineInfo.get(nStartLine);
    		int nRestCount = liEnd.FullLength() - nEndChar;
    		String strTail = "";    		
    		if (nRestCount > 0)
            {
    		    char[] sz = new char[nRestCount];
    		    for (int i = 0; i < nRestCount; i++)
    		    {
    		        sz[i] = liEnd.m_pcLine[nEndChar + i];
    		    }
    			strTail = String.valueOf(sz);
            }
    		
    		int nDelCount = nEndLine - nStartLine;
    		for (int L = nStartLine + 1; L <= nEndLine; L++)
    		{
    		    m_aryLineInfo.remove(L);
    		}
     		
    		// nEndLine is no more valid
    		liStart.m_nLength = nStartChar;
    		liStart.m_pcLine[nStartChar] = 0;
    		liStart.m_nEolChars = 0;

    		if (nRestCount > 0)
            {
    			AppendLine(nStartLine, strTail.toCharArray(), nRestCount);
            }
    		*/
    		m_bAdjustLine = false;
       		if (nEndLine == (m_aryLineInfo.size() - 1))
       		{
       		    //this.ChangeLineEol(nLine, this.GetDefaultEol());
       		    m_bAdjustLine = true;
       		}
       		
   			// delete multiple lines
        	for (int L = nEndLine - 1; L >= nStartLine; L--)
        	{
        	    m_aryLineInfo.remove(L);
        	}
        	
        	// remove the last line eol
		
    		if (pSource!=null)
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, nStartLine);
    		}
        //}
    	
    	if (!m_bModified)
    	{
    		SetModified(true);
    	}
    	// remember current cursor position as last editing position
    	m_ptLastChange = context.m_ptStart;
    	
    	return true;
    }
    
    public boolean InternalInsertText(CTextView pSource, int nLine, int nPos, CTextBuffer pSrcBuf, 
            int nStartLine, int nEndLine)
    {
    	if (m_bReadOnly)
    	{
    		return false;
    	}
    	
    	context.m_ptStart.x = nPos;
    	context.m_ptStart.y = nLine;
    	
    	/*int nRestCount = GetFullLineLength(nLine) - nPos;
    	String strTail = "";
    	if (nRestCount > 0)
        {
    		// remove end of line (we'll put it back on afterwards)
    		strTail = StripTail(nLine, nRestCount);
        }*/
    	
    	int nInsertedLines = 0;
    	int nCurrentLine = nLine;
    	
    	// if add to the last line, change the eol
    	m_bAdjustLine = false;
    	if (nLine == (m_aryLineInfo.size() - 1))
    	{
    	    ChangeLineEol(nLine, this.GetDefaultEol());
    	    m_bAdjustLine = true;
    	    nEndLine ++;
    	    nInsertedLines = 1;
    	}
    	
    	for (int i = nStartLine; i < nEndLine; i++)
    	{
    	    SLineInfo li = (SLineInfo)pSrcBuf.m_aryLineInfo.get(i);
    	    SLineInfo newLi = new SLineInfo();
    	    CopyLine(newLi, li);
    	    m_aryLineInfo.add(nLine + nInsertedLines, newLi);
    	    nInsertedLines++;
    	}
    	
    	if (pSource!=null)
        {
    		if (nInsertedLines > 0)
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, nLine);
    		}
    		else
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_SINGLELINE | EnumUpdateFlag.UPDATE_HORZRANGE, nLine);
    		}
        }
    	
    	if (!m_bModified)
    	{
    		SetModified(true);
    	}
    	
    	return true;
    }

//     InternalInsertText uses only apparent line numbers
    public boolean InternalInsertText(CTextView pSource, int nLine, int nPos, char[] pszText, 
            BaseDataObject nEndLine, BaseDataObject nEndChar)
    {
     	if (m_bReadOnly)
    	{
    		return false;
    	}
    	
    	context.m_ptStart.x = nPos;
    	context.m_ptStart.y = nLine;
    	
    	int nRestCount = GetFullLineLength(nLine) - nPos;
    	String strTail = "";
    	if (nRestCount > 0)
        {
    		// remove end of line (we'll put it back on afterwards)
    		strTail = StripTail(nLine, nRestCount);
        }
    	
    	int nInsertedLines = 0;
    	int nCurrentLine = nLine;
    	int nTextPos = 0;
    	for (;;)
        {
    	    String str = String.valueOf(pszText);
     		int haseol = 0;
    		//nTextPos = 0;
    		// advance to end of line
    		while (!IsEol(pszText[nTextPos]))
    		{
    			nTextPos++;
    		}
    		// advance after EOL of line
    		if (IsDosEol(str.substring(nTextPos, nTextPos + 2)))
            {
    			haseol = 1;
    			nTextPos += 2;
            }
    		else if (IsEol(pszText[nTextPos]))
            {
    			haseol = 1;
    			nTextPos++;
            }
    		
    		// The first line of the new text is appended to the start line
    		// All succeeding lines are inserted
    		if (nCurrentLine == nLine)
            {
    			AppendLine(nLine, pszText, nTextPos);
            }
    		else
            {
    			InsertLine(pszText, nTextPos, nCurrentLine, 1);
    			nInsertedLines ++;
            }   		
    		
    		if (pszText[nTextPos] == 0)
            {
    			// we just finished our insert
    			// now we have to reattach the tail
    			if (haseol > 0)
                {
    				nEndLine.setIntValue(nCurrentLine+1);
    				nEndChar.setIntValue(0);
                }
    			else
                {
    				nEndLine.setIntValue(nCurrentLine);
    				nEndChar.setIntValue(GetLineLength(nEndLine.getIntValue()));
                }
    			if (strTail.length() != 0)
                {
    				if (haseol > 0)
    				{
    					InsertLine(strTail.toCharArray(), -1, nEndLine.getIntValue(), 1);
    					nInsertedLines ++;
    				}
    				else
    				{
    					AppendLine(nEndLine.getIntValue(), strTail.toCharArray(), nRestCount);
    				}
                }
    			if (nEndLine.getIntValue() == GetLineCount())
                {
    				// We left cursor after last screen line
    				// which is an illegal cursor position
    				// so manufacture a new trailing line
    				InsertLine("".toCharArray(), -1, -1, 1);
    				nInsertedLines ++;
                }
    			break;
            }
    		
    		++nCurrentLine;
    		//pszText += nTextPos;
        }
    	
    	// Compute the context : all positions after context.m_ptBegin are
    	// shifted accordingly to (context.m_ptEnd - context.m_ptBegin)
    	// The begin point is the insertion point.
    	// The end point is more tedious : if we insert in a ghost line, we reuse it, 
    	// so we insert fewer lines than the number of lines in the text buffer
    	if ((nEndLine.getIntValue() - nLine) != nInsertedLines)
        {
    		context.m_ptEnd.y = nLine + nInsertedLines;
    		context.m_ptEnd.x = GetFullLineLength(context.m_ptEnd.y);
        }
    	else
        {
    		context.m_ptEnd.x = nEndChar.getIntValue();
    		context.m_ptEnd.y = nEndLine.getIntValue();
        }
    	
    	if (pSource!=null)
        {
    		if (nInsertedLines > 0)
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, nLine);
    		}
    		else
    		{
    			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_SINGLELINE | EnumUpdateFlag.UPDATE_HORZRANGE, nLine);
    		}
        }
    	
    	if (!m_bModified)
    	{
    		SetModified(true);
    	}
    	// remember current cursor position as last editing position
    	m_ptLastChange.x = nEndChar.getIntValue();
    	m_ptLastChange.y = nEndLine.getIntValue();
    	
    	return true;
    }
    
    public boolean InsertText(CTextView pSource, int nLine, int nPos,
            char[] pszText, BaseDataObject nEndLine, BaseDataObject nEndChar,
            int nAction, boolean bHistory)
    {
        if (!InternalInsertText(pSource, nLine, nPos, pszText, nEndLine,
                nEndChar))
        {
            return false;
        }

        if (!bHistory)
        {
            return true;
        }

        return true;
    }
    
    public boolean DeleteText(CTextView pSource, int nStartLine,
            int nStartChar, int nEndLine, int nEndChar, int nAction,
            boolean bHistory)
    {
        BaseDataObject sTextToDelete = new BaseDataObject();
        sTextToDelete.setStringValue("");
        GetTextWithoutEmptys(nStartLine, nStartChar, nEndLine, nEndChar,
                sTextToDelete, EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC);

        if (!InternalDeleteText(pSource, nStartLine, nStartChar, nEndLine,
                nEndChar))
        {
            return false;
        }

        if (!bHistory)
        {
            return true;
        }

        return true;
    }
    
    public boolean DeleteLine(CTextView pSource, int nStartLine,
            int nStartChar, int nEndLine, int nEndChar, int nAction,
            boolean bHistory)
    {
        BaseDataObject sTextToDelete = new BaseDataObject();
        sTextToDelete.setStringValue("");
        GetTextWithoutEmptys(nStartLine, nStartChar, nEndLine, nEndChar,
                sTextToDelete, EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC);

        CDeleteContext context = new CDeleteContext();
        context.m_ptStart.y = nStartLine;
        context.m_ptStart.x = nStartChar;
        context.m_ptEnd.y = nEndLine;
        context.m_ptEnd.x = nEndChar;

        DeleteLine(nStartLine, (nEndLine - nStartLine + 1));

        if (pSource != null)
        {
            UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE
                    | EnumUpdateFlag.UPDATE_VERTRANGE, nStartLine);
        }

        if (!bHistory)
        {
            return true;
        }

        return true;
    }
    
    public CPoint GetLastChangePos()
    {
    	return m_ptLastChange;
    }

    public void RestoreLastChangePos(CPoint pt)
    {
    	m_ptLastChange = pt;
    }

    public int GetTabSize()
    {
    	return m_nTabSize;
    }

    public void SetTabSize(int nTabSize)
    {
     	m_nTabSize = nTabSize;
    }

    public boolean GetInsertTabs()  
    {
        return m_bInsertTabs;
    }
    
    public void FreeAll()
    {
        super.FreeAll();
        //  Free text
    	m_aryLineInfo.clear();
    	
    	// Undo buffer will be cleared by its destructor
    	m_bInit = false;
    }
    
    public void ClearLineContent(int nLine, int nCount)
    {
    	if (nCount > 0)
    	{
    		for (int i = nLine; i < (nCount + nLine); i++) 
    		{
    			if (i < 0 || i >= m_aryLineInfo.size())
    			{
    				assert(false);
    				return;
    			}

    			SLineInfo  lineInfo = (SLineInfo)m_aryLineInfo.get(i);
    			lineInfo.m_dwFlags = MERGE_LINEFLAGS.LF_HIDEN | MERGE_LINEFLAGS.LF_CONFLICT;
    			lineInfo.m_pcLine[0] =  '\0';
    			lineInfo.m_nLength = 0;
    		}
    	}     
    }
}

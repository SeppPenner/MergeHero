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
import MergeHeroLib.CMemoryOperator;
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
public class CGhostTextBuffer extends CTextBuffer
{
    public static void main(String[] args)
    {
    }
    	
	public boolean InitNew(int nCrlfStyle)
	{
		return super.InitNew(nCrlfStyle);
	}
	
	public CGhostTextBuffer(String strFilePath)
	{
	    super(strFilePath);
	}
	
/**
     * 
     */
    protected CGhostTextBuffer()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    //	 Get text of specified lines (ghost lines will not contribute to text).
//	 nCrlfStyle determines the EOL type in the returned buffer.
//	 If nCrlfStyle equals CRLF_STYLE_AUTOMATIC, we read the EOL from the line buffer
//	 note This function has its base in CrystalTextBuffer
//	 CTextBuffer::GetTextWithoutEmptys() is for a buffer with no ghost lines.
//	 CTextBuffer::GetText() returns text including ghost lines.
//	 These two base functions never read the EOL from the line buffer, they
//	 use CRLF_STYLE_DOS when nCrlfStyle equals CRLF_STYLE_AUTOMATIC.
	public void GetTextWithoutEmptys(int nStartLine, int nStartChar, 
	        int nEndLine, int nEndChar, BaseDataObject strText, int nCrlfStyle)
	{
		int lines = m_aryLineInfo.size();
		
		// estimate size (upper bound)
		int nBufSize = 0;
		int nSoffset = 0;
		int nEoffset = 0;
		int nChars = 0;

		for (int i = nStartLine; i <= nEndLine; ++i)
		{
			nBufSize += (GetFullLineLength(i) + 2); // in case we insert EOLs
		}

		char[] pszBuf = new char[nBufSize];
		int iCopied = 0;
		
		if (nCrlfStyle != EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC)
		{
			// we must copy this EOL type only
			String strEol = GetStringEol (nCrlfStyle);
			
			for (int i = nStartLine; i <= nEndLine; ++i)
			{
				// exclude ghost lines
				if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) > 0)
					continue;
				
				// copy the line, excluding the EOL
				nSoffset = (i==nStartLine ? nStartChar : 0);
				nEoffset = (i==nEndLine ? nEndChar : GetLineLength(i));
				nChars = nEoffset - nSoffset;

				SLineInfo li = (SLineInfo)m_aryLineInfo.get(i);
				for (int j = 0; j < nChars; j++)
				{
				    pszBuf[iCopied + j] = li.m_pcLine[j + nSoffset];
				}
				iCopied += nChars;
				
				// copy the EOL of the requested type
				if (i != ApparentLastRealLine())
				{
					for (int j = 0; j < strEol.length(); j++)
					{
					    pszBuf[iCopied + j] = strEol.charAt(j);
					}
					iCopied += strEol.length();
				}
			}
		} 
		else 
		{
			for (int i = nStartLine; i <= nEndLine; ++i)
			{
				// exclude ghost lines
				if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) > 0)
					continue;
				
				// copy the line including the EOL
				nSoffset = (i==nStartLine ? nStartChar : 0);
				nEoffset = (i==nEndLine ? nEndChar : GetFullLineLength(i));
				nChars = nEoffset - nSoffset;

				SLineInfo li = (SLineInfo)m_aryLineInfo.get(i);
				for (int j = 0; j < nChars; j++)
				{
				    pszBuf[iCopied + j] = li.m_pcLine[j + nSoffset];
				}
				iCopied += nChars;
				
				// check that we really have an EOL
				if (i != ApparentLastRealLine() && GetLineLength(i)==GetFullLineLength(i))
				{
					// Oops, real line lacks EOL
					// (If this happens, editor probably has bug)
					String strEol = GetStringEol (nCrlfStyle);
					for (int j = 0; j < strEol.length(); j++)
					{
					    pszBuf[iCopied + j] = strEol.charAt(j);
					}
					iCopied += strEol.length();
				}
			}
		}

		pszBuf[0] = 0;
		strText.setStringValue(String.valueOf(pszBuf));
	}
	
	public void RemoveAllGhostLines()
	{
		int nlines = GetLineCount();
		int nNewnl = 0;
		int i;

		// Free the buffer of ghost lines
		for(i = nlines - 1; i >= 0; i--)
		{
			if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) > 0)
			{
				m_aryLineInfo.remove(i);
			}
		}

		// Compact non-ghost lines
		// (we copy the buffer address, so the buffer don't move and we don't free it)
		for(i = 0; i < nlines; i++)
		{
		    SLineInfo li = null;
			if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) == 0)
			{
			    li = (SLineInfo)m_aryLineInfo.get(nNewnl++);
				li = (SLineInfo)m_aryLineInfo.get(i);
			}
		}

		RecomputeRealityMapping();
	}


//	 Return underlying real line. 
//	 For ghost lines, return NEXT HIGHER real line (for trailing ghost line, return last real line + 1). 
//	 If nApparentLine is greater than the last valid apparent line, ASSERT
//	 ie, lines 0->0, 1->2, 2->4, 
//	 for argument of 3, return 2
	public int ComputeRealLine(int nApparentLine)
	{
		int nMax = m_arySRealityBlocks.size() - 1;
		// first get the degenerate cases out of the way
		// empty file ?
		if (nMax < 0)
		{
			return 0;
		}
		
		// after last block ?
		SRealityBlock maxblock = (SRealityBlock)m_arySRealityBlocks.get(nMax);
		if (nApparentLine >= (maxblock.m_nStartApparent + maxblock.m_nCount))
		{
			return maxblock.m_nStartReal + maxblock.m_nCount;
		}
		
		// binary search to find correct (or nearest block)
		int nLo = 0;
		int nHi = nMax;
		int i = 0;

		while (nLo <= nHi)
		{
			i = (nLo + nHi)/2;

			SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(i);

			if (nApparentLine < block.m_nStartApparent)
			{
				nHi = i - 1;
			}
			else if (nApparentLine >= (block.m_nStartApparent + block.m_nCount))
			{
				nLo = i + 1;
			}
			else // found it inside this block
			{
				return (nApparentLine - block.m_nStartApparent) + block.m_nStartReal;
			}
		}

		// it is a ghost line just before block blo
		SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(nLo);
		return block.m_nStartReal;
	}

//	 Return apparent line for this underlying real line. 
//	 If real line is out of bounds, return last valid apparent line + 1
	public int ComputeApparentLine(int nRealLine)
	{
		int nMax = m_arySRealityBlocks.size() - 1;
		int nRet = 0;
		// first get the degenerate cases out of the way
		// empty file ?
		if (nMax < 0)
		{
			return 0;
		}

		// after last block ?
		SRealityBlock maxblock = (SRealityBlock)m_arySRealityBlocks.get(nMax);
		if (nRealLine >= (maxblock.m_nStartReal + maxblock.m_nCount))
		{
			return GetLineCount();
		}
		
		// binary search to find correct (or nearest block)
		int nLo = 0;
		int nHi = nMax;
		int i = 0;
		while (nLo <= nHi)
		{
			i = (nLo + nHi)/2;

			SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(i);

			if (nRealLine < block.m_nStartReal)
			{
				nHi = i - 1;
			}
			else if (nRealLine >= (block.m_nStartReal + block.m_nCount))
			{
				nLo = i + 1;
			}
			else
			{
				nRet = (nRealLine - block.m_nStartReal) + block.m_nStartApparent;

				if (nRet > GetLineCount())
				{
					nRet = GetLineCount();
				}

				return nRet;
			}
		}

		return -1;
	}


//	 Return underlying real line and ghost adjustment 
//	 as nApparentLine = apparent(nRealLine) - nGhostAdjustment 
//	 nRealLine for ghost lines is the NEXT HIGHER real line (for trailing ghost line, last real line + 1).
//	 If nApparentLine is greater than the last valid apparent line, ASSERT
//	 ie, lines 0->0, 1->2, 2->4,  
//	 for argument of 3, return 2, and decToReal = 1
	public int ComputeRealLineAndGhostAdjustment(int nApparentLine, BaseDataObject nDecToReal)
	{
		int nMax = m_arySRealityBlocks.size() - 1;
		// first get the degenerate cases out of the way
		// empty file ?
		if (nMax < 0) 
		{
			nDecToReal.setIntValue(0);
			return 0;
		}
		
		// after last block ?
		SRealityBlock  maxblock = (SRealityBlock)m_arySRealityBlocks.get(nMax);
		if (nApparentLine >= (maxblock.m_nStartApparent + maxblock.m_nCount))
		{
			nDecToReal.setIntValue(GetLineCount() - nApparentLine);
			return (maxblock.m_nStartReal + maxblock.m_nCount);
		}
		
		// binary search to find correct (or nearest block)
		int nLo = 0;
		int nHi = nMax;
		int i = 0;
		while (nLo <= nHi)
		{
			i = (nLo + nHi)/2;
			SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(i);
			if (nApparentLine < block.m_nStartApparent)
			{
				nHi = i - 1;
			}
			else if (nApparentLine >= block.m_nStartApparent + block.m_nCount)
			{
				nLo = i + 1;
			}
			else // found it inside this block
			{
				nDecToReal.setIntValue(0);
				return (nApparentLine - block.m_nStartApparent) + block.m_nStartReal;
			}
		}
		// it is a ghost line just before block blo
		SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(nLo);
		nDecToReal.setIntValue(block.m_nStartApparent - nApparentLine);
		return block.m_nStartReal;
	}


//	 Return apparent line for this underlying real line, with adjustment : 
//	 nApparent = apparent(nReal) - nDecToReal
//	 If the previous real line has apparent number   apparent(nReal) - dec, with dec < nDecToReal, 
//	 return apparent(nReal) - nDec + 1
	public int ComputeApparentLine(int nRealLine, int nDecToReal)
	{
		int nLo = 0, nHi = 0, i = 0;
		int nPreviousBlock;
		int nApparent;
		int nMax = m_arySRealityBlocks.size() - 1;

		// first get the degenerate cases out of the way
		// empty file ?
		if (nMax < 0)
		{
			return 0;
		}

		// after last block ?
		SRealityBlock maxblock = (SRealityBlock)m_arySRealityBlocks.get(nMax);
		if (nRealLine >= (maxblock.m_nStartReal + maxblock.m_nCount))//
		{
			nPreviousBlock = nMax;
			nApparent = GetLineCount();

			return limitWithPreviousBlock(nPreviousBlock, nDecToReal, nApparent);
		}
		
		// binary search to find correct (or nearest block)
		nLo = 0;
		nHi = nMax;

		while (nLo <= nHi)
		{
			i = (nLo + nHi)/2;

			SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(i);

			if (nRealLine < block.m_nStartReal)
			{
				nHi = i - 1;
			}
			else if (nRealLine >= (block.m_nStartReal + block.m_nCount))
			{
				nLo = i + 1;
			}
			else
			{
				if (nRealLine > block.m_nStartReal)
				{
					// limited by the previous line in this block
					return ((nRealLine - block.m_nStartReal) + block.m_nStartApparent);
				}

				nPreviousBlock = i - 1;
				nApparent = (nRealLine - block.m_nStartReal) + block.m_nStartApparent;

				return limitWithPreviousBlock(nPreviousBlock, nDecToReal, nApparent);
			}
		}

		return -1;
	}
	
	public int limitWithPreviousBlock(int nPreviousBlock, int nDecToReal, int nApparent)
	{
		// we must keep above the value lastApparentInPreviousBlock
		int nLastApparentInPreviousBlock = 0;
		if (nPreviousBlock == -1)
		{
			nLastApparentInPreviousBlock = -1;
		}
		else
		{
			SRealityBlock previousBlock = (SRealityBlock)m_arySRealityBlocks.get(nPreviousBlock);
			nLastApparentInPreviousBlock = previousBlock.m_nStartApparent + previousBlock.m_nCount - 1;
		}
		
		while (nDecToReal-- != 0) 
		{
			nApparent --;
			if (nApparent == nLastApparentInPreviousBlock)
			{
				return (nApparent+1);
			}
		}

		return nApparent;
	}

//	 we recompute EOL from the real line before nStartLine to nEndLine
	public void RecomputeEOL(CTextView pSource, int nStartLine, int nEndLine)
	{
		if (ApparentLastRealLine() <= nEndLine)
		{
			// EOL may have to change on the real line before nStartLine
			int nRealBeforeStart;
			for (nRealBeforeStart = nStartLine-1 ; nRealBeforeStart >= 0 ; nRealBeforeStart--)
			{
				if ((GetLineFlags(nRealBeforeStart) & MERGE_LINEFLAGS.LF_GHOST) == 0)
				{
					break;
				}
				if (nRealBeforeStart >= 0)
				{
					nStartLine = nRealBeforeStart;
				}
			}
		}
		
		boolean bLastRealLine = (ApparentLastRealLine() <= nEndLine);
		int i;
		for (i = nEndLine ; i >= nStartLine ; i --)
		{
		    SLineInfo li = (SLineInfo)m_aryLineInfo.get(i);
			if ((GetLineFlags(i) & MERGE_LINEFLAGS.LF_GHOST) == 0)
			{
				if (bLastRealLine)
				{
					bLastRealLine = false;
					if (li.m_nEolChars != 0) 
					{
						// if the last real line has an EOL, remove it
					    li.m_pcLine[li.m_nLength] = '\0';
					    li.m_nEolChars = 0;
						if (pSource != null)
						{
							UpdateViews(pSource, null, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_SINGLELINE, i);
						}
					}
				}
				else
				{
					if (li.m_nEolChars == 0) 
					{
						// if a real line (not the last) has no EOL, add one
						AppendLine (i, GetDefaultEol().toCharArray(), GetDefaultEol().length());
						if (pSource != null)
						{
							UpdateViews(pSource, null, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_SINGLELINE, i);
						}
					}
				}
			}
			else 
			{
				if (li.m_nEolChars != 0) 
				{
					// if a ghost line has an EOL, remove it
					li.m_pcLine[li.m_nLength] = '\0';
					li.m_nEolChars = 0;
					if (pSource != null)
					{
						UpdateViews (pSource, null, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_SINGLELINE, i);
					}
				}
			}
		}
	}
	
	public boolean InsertText(CTextView pSource, int nLine, int nPos, CTextBuffer pSrcBuf, 
            int nStartLine, int nEndLine)
	{
		if (!super.InternalInsertText(pSource, nLine, nPos, pSrcBuf, nStartLine, nEndLine))
		{
			return false;
		}	

		return true;
	}

//	 nEndLine and nEndChar are the coordinates of the end od the inserted text
//	 They are valid as long as you do not call FlushUndoGroup
//	 If you need to call FlushUndoGroup, just store them in a variable which
//	 is preserved with real line number during Rescan (m_ptCursorPos, m_ptLastChange for example)
	public boolean InsertText(CTextView  pSource, int nLine, int nPos, char[] pszText,
									  BaseDataObject nEndLine, BaseDataObject nEndChar, int nAction, boolean bHistory)
	{
		boolean bGroupFlag = false;
		if (bHistory)
		{
		}

		if (!super.InsertText(pSource, nLine, nPos, pszText, nEndLine, nEndChar, nAction, bHistory))
		{
			return false;
		}

		int i = 0;
		boolean bFirstLineGhost = ((GetLineFlags(nLine) & MERGE_LINEFLAGS.LF_GHOST) != 0);

		// when inserting an EOL terminated text into a ghost line,
		// there is a dicrepancy between nInsertedLines and nEndLine-nRealLine
		boolean bDiscrepancyInInsertedLines ;
		if (bFirstLineGhost && nEndChar.getIntValue() == 0)
		{
			bDiscrepancyInInsertedLines = true;
		}
		else
		{
			bDiscrepancyInInsertedLines = false;
		}

		// compute the number of real lines created (for undo)
		int nRealLinesCreated = nEndLine.getIntValue() - nLine;
		if (bFirstLineGhost && nEndChar.getIntValue() > 0)
		{
			// we create one more real line
			nRealLinesCreated ++;
		}

		for (i = nLine ; i < nEndLine.getIntValue() ; i++)
		{
			OnNotifyLineHasBeenEdited(i);
			if (!bDiscrepancyInInsertedLines)
			{
				OnNotifyLineHasBeenEdited(i);
			}
		}

		// when inserting into a ghost line block, we want to replace ghost lines
		// with our text, so delete some ghost lines below the inserted text
		if (bFirstLineGhost)
		{
			// where is the first line after the inserted text ?
			int nInsertedTextLinesCount = nEndLine.getIntValue() - nLine + (bDiscrepancyInInsertedLines ? 0 : 1);
			int nLineAfterInsertedBlock = nLine + nInsertedTextLinesCount;
			// delete at most nInsertedTextLinesCount - 1 ghost lines
			// as the first ghost line has been reused
			int nMaxGhostLineToDelete = Math.min(nInsertedTextLinesCount - 1, GetLineCount()-nLineAfterInsertedBlock);
			for (i = 0 ; i < nMaxGhostLineToDelete ; i++)
			{
				if ((GetLineFlags(nLineAfterInsertedBlock+i) & MERGE_LINEFLAGS.LF_GHOST) == 0)
				{
					break;
				}
			}
			InternalDeleteGhostLine(pSource, nLineAfterInsertedBlock, i);
		}

		for (i = nLine ; i < nEndLine.getIntValue() ; i++)
		{
			SetLineFlag (i, MERGE_LINEFLAGS.LF_GHOST, false, false, false);
		}
		if (!bDiscrepancyInInsertedLines)
		{
			// if there is no discrepancy, the final cursor line is real
			// as either some text was inserted in it, or it inherits the real status from the first line
			SetLineFlag (i, MERGE_LINEFLAGS.LF_GHOST, false, false, false);
		}
		else
			// if there is a discrepancy, the final cursor line was not changed during insertion so we do nothing
			;

		// now we can recompute
		if ((nEndLine.getIntValue() > nLine) || bFirstLineGhost)
		{
			// TODO: Be smarter, and don't recompute if it is easy to see what changed
			RecomputeRealityMapping();
		}

		RecomputeEOL(pSource, nLine, nEndLine.getIntValue());

		if (!bHistory)
		{
			return true;
		}

		// nEndLine may have changed during Rescan
		nEndLine.setIntValue(m_ptLastChange.y);

		return true;
	}
	
	public boolean DeleteText(CTextView pSource, int nStartLine,
            int nStartChar, int nEndLine, int nEndChar, int nAction,
            boolean bHistory)
    {
        if (bHistory)
        {
       }

        // flags are going to be deleted so we store them now
        boolean bLastLineGhost = ((GetLineFlags(nEndLine) & MERGE_LINEFLAGS.LF_GHOST) != 0);
        boolean bFirstLineGhost = ((GetLineFlags(nStartLine) & MERGE_LINEFLAGS.LF_GHOST) != 0);
        // count the number of real lines in the deleted block (for first/last
        // line,
        // include partial real lines)
        int nRealLinesInDeletedBlock = ComputeRealLine(nEndLine)
                - ComputeRealLine(nStartLine);
        if (!bLastLineGhost)
        {
            nRealLinesInDeletedBlock++;
        }

        BaseDataObject sTextToDelete = new BaseDataObject();
        GetTextWithoutEmptys(nStartLine, nStartChar, nEndLine, nEndChar, sTextToDelete, EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC);
        if (!super.DeleteText(pSource, nStartLine, nStartChar, nEndLine,
                nEndChar, nAction, bHistory))
        {
            return false;
        }

        OnNotifyLineHasBeenEdited(nStartLine);

        // the first line inherits the status of the last one
        // but exception... if the last line is a ghost, we preserve the status
        // of the
        // first line
        // (then if we use backspace in a ghost line, we don't delete the
        // previous line)
        if (!bLastLineGhost)
        {
            SetLineFlag(nStartLine, MERGE_LINEFLAGS.LF_GHOST, false, false, false);
        }
        else
        {
            boolean bFlagException = !bFirstLineGhost;
            if (bFlagException)
            {
                SetLineFlag(nStartLine, MERGE_LINEFLAGS.LF_GHOST, false, false,
                        false);
            }
            else
            {
                SetLineFlag(nStartLine, MERGE_LINEFLAGS.LF_GHOST, true, false,
                        false);
            }
        }

        // now we can recompute
        if (nStartLine != nEndLine)
        {
            // TODO: Be smarter, and don't recompute if it is easy to see what changed
            RecomputeRealityMapping();
        }

        RecomputeEOL(pSource, nStartLine, nStartLine);

        if (!bHistory)
        {
            return true;
        }

         return true;
    }
	
	public boolean DeleteLine(CTextView pSource, int nStartLine, int nStartChar,
			  int nEndLine, int nEndChar, int nAction, boolean bHistory)
	{
	    if (bHistory)
	    {
	    }

	    BaseDataObject strTextToDelete = new BaseDataObject();
	    GetTextWithoutEmptys(nStartLine, nStartChar, nEndLine, nEndChar, strTextToDelete, EnumCRLFSTYLE.CRLF_STYLE_AUTOMATIC);
	    super.DeleteLine(pSource, nStartLine, nStartChar, nEndLine, nEndChar, nAction, bHistory);

	    OnNotifyLineHasBeenEdited(nStartLine);

	    if(!bHistory)
	    {
	        return false;
	    }

	    return true;
	}

	public boolean InsertGhostLine(CTextView pSource, int nLine)
	{
	    if (!InternalInsertGhostLine(pSource, nLine))
	    {
	        return false;
	    }

	    // set ghost flags
	    SetLineFlag(nLine, MERGE_LINEFLAGS.LF_GHOST, true, false, false);
	    RecomputeRealityMapping();

	    return true;
	}

//	 InternalDeleteGhostLine accepts only apparent line numbers
	public boolean InternalDeleteGhostLine (CTextView pSource, int nLine, int nCount)
	{
		if (m_bReadOnly)
		{
			return false;
		}

		if (nCount == 0)
		{
			return true;
		}

		CDeleteContext context = new CDeleteContext();
		context.m_ptStart.y = nLine;
		context.m_ptStart.x = 0;
		context.m_ptEnd.y = nLine+nCount;
		context.m_ptEnd.x = 0;

		for (int L = nLine+nCount - 1 ; L >= nLine ; L--)
		{
			m_aryLineInfo.remove(L);
		}

		if (pSource!=null)
		{
			// the last parameter is just for speed : don't recompute lines before this one
			// it must be a valid line number, so if we delete the last lines, we give the last of the remaining lines
			if (nLine == GetLineCount())
			{
				UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, GetLineCount()-1);
			}
			else
			{
				UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, nLine);
			}
		}

		if (!m_bModified)
		{
			SetModified(true);
		}

		return true;
	}

//	 InternalInsertGhostLine accepts only apparent line numbers
	public boolean InternalInsertGhostLine (CTextView pSource, int nLine)
	{
		if (m_bReadOnly)
		{
			return false;
		}

		CInsertContext context = new CInsertContext();
		context.m_ptStart.x = 0;
		context.m_ptStart.y = nLine;

		super.InsertLine("".toCharArray(), 0, nLine, 1);

		context.m_ptEnd.x = 0;
		context.m_ptEnd.y = nLine+1;

		if (pSource!=null)
		{
			UpdateViews(pSource, context, EnumUpdateFlag.UPDATE_HORZRANGE | EnumUpdateFlag.UPDATE_VERTRANGE, nLine);
		}

		if (!m_bModified)
		{
			SetModified (true);
		}

		OnNotifyLineHasBeenEdited(nLine);

		return true;
	}
	 
//	Check all lines, and ASSERT if reality blocks differ from flags. 
//	This means that this only has effect in DEBUG build
	public void CheckFlagsFromReality(boolean bFlag) 
	{
		int nMax = m_arySRealityBlocks.size() - 1;
		int i = 0;
		int j = 0;
		for (i = 0 ; i <= nMax ; i++)
		{
			SRealityBlock block = (SRealityBlock)m_arySRealityBlocks.get(i);

			for ( ; j < block.m_nStartApparent ; j++)
			{
				//ASSERT ((GetLineFlags(i) & LF_GHOST) != 0);
			}

			for ( ; j < block.m_nStartApparent+block.m_nCount ; j++)
			{
				//ASSERT ((GetLineFlags(j) & LF_GHOST) == 0);
			}
		}

		for ( ; j < GetLineCount() ; j++)
		{
			//ASSERT ((GetLineFlags(j) & LF_GHOST) != 0);
		}
	}

	public void OnNotifyLineHasBeenEdited(int nLine)
	{
		return;
	}

//	/ Replace line (removing any eol, and only including one if in strText)
	public void ReplaceFullLine(CTextView  pSource, int nLine, String strText, int nAction)
	{
		if (CMemoryOperator.memcmp(GetLineEol(nLine).getBytes(), GetEol(strText).getBytes(), GetEol(strText).length()) == 0)
		{
			// (optimization) eols are the same, so just replace text inside line
			// we must clean strText from its eol...
			String strTextWithoutEol = strText;
			int newLength = strTextWithoutEol.length()- GetEol(strTextWithoutEol).length();
			ReplaceLine(pSource, nLine, strTextWithoutEol, nAction);
			return;
		}

		// we may need a last line as the DeleteText end is (x=0,y=line+1)
		if (nLine+1 == GetLineCount())
		{
			InsertGhostLine(pSource, GetLineCount());
		}

		if (super.GetFullLineLength(nLine) > 0)
		{
			DeleteText(pSource, nLine, 0, nLine+1, 0, nAction, true); 
		}

		BaseDataObject nEndLine = new BaseDataObject();
		BaseDataObject nEndChar = new BaseDataObject();
		if (strText.length() != 0)
		{
			InsertText(pSource, nLine, 0, strText.toCharArray(), nEndLine, nEndChar, nAction, true);
		}
	}

	public void ReplaceLine(CTextView pSource, int nLine, String strText, int nAction)
	{
		BaseDataObject nEndLine = new BaseDataObject();
		BaseDataObject nEndChar = new BaseDataObject();
		if (strText.length() != 0)
		{
			InsertText(pSource, nLine, 0, strText.toCharArray(), nEndLine, nEndChar, nAction, true);
		}
	}

//	/ Return pointer to the eol chars of this string, or pointer to empty string if none
	public String GetEol(String str)
	{
	    int len = str.length();
	    byte[] by = str.getBytes();
		if (len >1 && by[len-2] =='\r' && by[len-1]=='\n')
		{
			return str.substring(len - 2);
		}
		if (len >0 && by[len-1] =='\r' || by[len-1]=='\n')
		{
			return str.substring(len - 1);
		}

		return "";
	}

}

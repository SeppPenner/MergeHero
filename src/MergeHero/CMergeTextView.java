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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CPoint;

import com.dynamsoft.sourceanywhere.BaseDataObject;

/*
 * Created on 2005-4-25
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
public class CMergeTextView extends CGhostTextView implements MouseListener
{
    private StatusJPanel m_pMergeViewStatus = null;
    public int m_enumBufferType = 0;
    public CDiffTextBuffer m_pMergeTextBuffer = null;
	CPoint m_ptBlockStart = new CPoint(0, 0), m_ptBlockEnd = new CPoint(0, 0);
   	CPoint ptSelStart = new CPoint(0, 0), ptSelEnd = new CPoint(0, 0);
 	CPoint ptStart = new CPoint(0, 0), ptEnd = new CPoint(0, 0);
	int m_nLineIndex = 0;
	long m_dwFlags = 0;
	int m_nIndex = 0;
	BaseDataObject dataTemp = new BaseDataObject();
	LocationViewJPanel m_pLocationView = null;
	CDiffTextView m_pLeftView = null, m_pRightView = null;

    public static void main(String[] args)
    {
    }
    
    public void SetStatusInterface(StatusJPanel statusPane, LocationViewJPanel pLocationView,
            CDiffTextView pLeftView, CDiffTextView pRightView)
    {
        m_pMergeViewStatus = statusPane;
        m_pLocationView = pLocationView;
        m_pLeftView = pLeftView;
        m_pRightView = pRightView;
        
        addMouseListener(this);
    }
    
    public void ResetView()
    {
    	super.ResetView();
     	m_bMergeView = true;
     	m_ptBlockStart.zero();
     	m_ptBlockEnd.zero();
    	m_nLineIndex = 0;
    	m_dwFlags = 0;
    	
    	if (m_pMergeViewStatus != null)
    	{
            m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_CONFLICTS, "");
            m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_MERGE_LINE, "");
            m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_MERGED, "");
    	}
    }

//     Return text buffer for file in view
    public CTextBuffer LocateTextBuffer()
    {
    	if (m_enumBufferType == EnumBufferType.MERGE_BUFFER)
    	{
    		m_pMergeTextBuffer = MergeHeroApp.theApp.m_MergeTextBuf;
    		return m_pMergeTextBuffer;
    	}

    	return null;
    }
    
    public void Update(int nPos)
    {
    	if (MergeHeroApp.theApp.m_bIsMerge)
    	{
    		if (GetLineCount() < GetScreenLines())
    		{
    			return;
    		}

    		// Find the latest match diff block
    		int nScrollPos = GetScrollPos(nPos);

    		if (nScrollPos < 0)
    		{
    			nScrollPos = 0;
    			ScrollToLine(nScrollPos, false, true);
    		}
    		else if (nScrollPos > (GetLineCount() - 1))
    		{
    			ScrollToLine(GetLineCount() - 1, false, true);
    		}
    		else
    		{
    			ScrollToLine(nScrollPos, false, true);
    		}   	
    	}
    }
    
    int GetScrollPos(int nPos)
    {
     	return nPos;
    }
    
//  Update statusbar info, Override from CTextView
//  we tab-expand column, but we don't tab-expand char count,
//  since we want to show how many chars there are and tab is just one
//  character although it expands to several spaces.
 void OnUpdateCaret()
    {
        if (m_pMergeViewStatus != null)
        {
            if (IsTextBufferInitialized())
            {
                CPoint cursorPos = GetCursorPos();
                int nScreenLine = cursorPos.y;
                int nRealLine = ComputeRealLine(nScreenLine);
                String strLine = ("Line:");

                // Is this a ghost line ?
                if ((m_pMergeTextBuffer.GetLineFlags(nScreenLine) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_GHOST) > 0)
                {
                    // Ghost lines display eg "Line 12-13"
                    strLine = "Line: " + nRealLine + "-" + (nRealLine + 1)
                            + "/" + m_pMergeTextBuffer.m_aryLineInfo.size();
                }
                else
                {
                    // Regular lines display eg "Line 13 Characters: 25 EOL:
                    // CRLF"
                    strLine = "Line: " + (nRealLine + 1) + "/"
                            + m_pMergeTextBuffer.m_aryLineInfo.size();
                }

                m_pMergeViewStatus.setText(
                        EnumMainStatusIndex.PANE_MAIN_MERGE_LINE, strLine);
                String str = "Merged: "
                        + m_pMergeTextBuffer.m_ulNumMergedLines;
                m_pMergeViewStatus.setText(
                        EnumMainStatusIndex.PANE_MAIN_MERGED, str);
                if (m_pMergeTextBuffer.m_ulNumConflictLines == 0)
                {
                    m_pMergeViewStatus.setText( EnumMainStatusIndex.PANE_MAIN_CONFLICTS,
                            "No Conflict");
                }
                else
                {
                    str = "Conflicts: " + m_pMergeTextBuffer.m_ulNumConflictLines;
                    m_pMergeViewStatus.setText( EnumMainStatusIndex.PANE_MAIN_CONFLICTS, str);
                }
            }
            else
            {
                m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_CONFLICTS, "");
                m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_MERGE_LINE, "");
                m_pMergeViewStatus.setText(EnumMainStatusIndex.PANE_MAIN_MERGED, "");
            }
        }
    }
    
    public void Update(int nLine1, int nLine2)
    {
        super.InvalidateLines(nLine1, nLine2, true);
        UpdateCaret();
    }
    
	public void mouseClicked(MouseEvent evt)
	{	    
	    this.requestFocus();
	    //FileDiffViewPanel.pActiveView = this;
	}
}

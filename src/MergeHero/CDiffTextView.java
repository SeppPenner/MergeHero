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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CPoint;
import MergeHeroLib.SDiffMergeBlock;
import MergeHeroLib.SLineInfo;

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
public class CDiffTextView extends CGhostTextView implements ActionListener, MouseListener
{
    private StatusJPanel m_pDiffViewStatus = null;
    public CDiffTextBuffer m_pDiffBuffer = null;
    public int m_enumBufferType = 0;
    CPoint m_ptBlockStart = new CPoint(0, 0), m_ptBlockEnd = new CPoint(0, 0);
   	CPoint ptSelStart = new CPoint(0, 0), ptSelEnd = new CPoint(0, 0);
 	CPoint ptStart = new CPoint(0, 0), ptEnd = new CPoint(0, 0);
 	CPoint ptPos = new CPoint(0, 0);
 	CPoint pos = new CPoint(0, 0), pt = new CPoint(0, 0);
 	CPoint m_ptStart = new CPoint(0, 0), m_ptEnd = new CPoint(0, 0);
	long m_dwFlags = 0;
	int m_statusPaneIndex = 0;
	LocationViewJPanel m_pLocationView = null;
	CMergeTextView m_pMergeView = null;
	BaseDataObject dataTemp = new BaseDataObject();
	JPopupMenu menu = new JPopupMenu();
	JMenuItem menuItemApplyChange = null, menuItemRemoveChange = null, menuItemApplyBothChanges = null;
	private boolean m_bLeftView = false;
	int m_iMergedBlockIndex = -1;

    public static void main(String[] args)
    {
    }
    
    public void SetStatusInterface(StatusJPanel statusPane, int statusPaneIndex, LocationViewJPanel pLocationView,
            CMergeTextView pMergeView)
    {
        m_pDiffViewStatus = statusPane;
        m_pLocationView = pLocationView;
       m_pMergeView = pMergeView;
        m_statusPaneIndex = statusPaneIndex;
       
        createMenu();
        addMouseListener(this);
    }
    
	protected JMenuItem addMenuItem(String strMenu, int nMnemonic, 
	        KeyStroke keyAccelerator, String strActionCmd)
	{
	    JMenuItem menuItem = new JMenuItem(strMenu);
	    menuItem.setMnemonic(nMnemonic);
	    menuItem.setAccelerator(keyAccelerator);
	    menuItem.setActionCommand(strActionCmd);
	    menuItem.addActionListener(this);
	    
	    return menuItem;
	}
    
    public void createMenu()
    {
        menu.removeAll();
        menuItemApplyChange = addMenuItem("Apply Change", KeyEvent.VK_A, 
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "Apply Change");
        menu.add(menuItemApplyChange);
        menuItemRemoveChange = addMenuItem("Remove Change", KeyEvent.VK_R, 
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "Remove Change");
        menu.add(menuItemRemoveChange);
        menuItemApplyBothChanges = addMenuItem("Apply Both Changes", KeyEvent.VK_R, 
                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "Apply Both Changes");
        menu.add(menuItemApplyBothChanges);   
    }
    
    public void ResetView()
    {
    	super.ResetView();
    	m_bMergeView = false;  	
    	m_nLineIndex = 0;
    	m_ptBlockStart.zero();
    	m_ptBlockEnd.zero();
    	if (m_pDiffViewStatus != null)
    	{
    		m_pDiffViewStatus.setText(m_statusPaneIndex, "");
    	}
    }

//  Return text buffer for file in view
    public CTextBuffer LocateTextBuffer()
    {
    	if (m_enumBufferType == EnumBufferType.LOCAL_BUFFER)
    	{
    		m_pDiffBuffer = MergeHeroApp.theApp.m_LocalTextBuf;
    	}
    	else if (m_enumBufferType == EnumBufferType.SERVER_BUFFER)
    	{
    		m_pDiffBuffer = MergeHeroApp.theApp.m_ServerTextBuf;
    	}
    	else if (m_enumBufferType == EnumBufferType.ORIGINAL_BUFFER)
    	{
    		m_pDiffBuffer = MergeHeroApp.theApp.m_OrgTextBuf;
    	}

    	return m_pDiffBuffer;
    }
  
    int GetTopLine() 
    {
        return m_nTopLine;
    }
    
//  Update statusbar info, Override from CTextView
//  we tab-expand column, but we don't tab-expand char count,
//  since we want to show how many chars there are and tab is just one
//  character although it expands to several spaces.
    void OnUpdateCaret()
    {
        if (m_pDiffViewStatus != null)
        {
            m_pDiffBuffer = (CDiffTextBuffer)m_pTextBuffer;
            if (IsTextBufferInitialized())
            {
                CPoint cursorPos = GetCursorPos();
                int nScreenLine = cursorPos.y;
                int nRealLine = ComputeRealLine(nScreenLine);
                String strLine = "";

                // Is this a ghost line ?
                if ((m_pTextBuffer.GetLineFlags(nScreenLine) & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_GHOST) > 0)
                {
                    // Ghost lines display eg "Line 12-13"
                    strLine = nRealLine + "-" + (nRealLine + 1);
                }
                else
                {
                    // Regular lines display eg "Line: 13/456 Deleted: 2
                    // Inserted: 10"
                    strLine = String.valueOf(nRealLine + 1);
                }

                String str = "Line: " + strLine + "/"
                        + m_pDiffBuffer.GetLineCount() + "  "
                        + "Deleted: "
                        + m_pDiffBuffer.m_ulNumDeletedLines + "  "
                        + "Inserted: "
                        + m_pDiffBuffer.m_ulNumInsertedLines;

                m_pDiffViewStatus.setText(m_statusPaneIndex, str);
            }
            else
            {
                m_pDiffViewStatus.setText(m_statusPaneIndex, "");
            }
        }
    }

    void Update(int nPos)
    {
        int nLineCount = 0;

        int nRealLine = nPos;

        m_pDiffBuffer = (CDiffTextBuffer)m_pTextBuffer;
        nLineCount = m_pDiffBuffer.GetLineCount() - 1;
        if (nRealLine > nLineCount)
        {
            nRealLine = nLineCount;
        }
        if (nRealLine < 0)
        {
            nRealLine = 0;
        }

        ptPos.x = 0;
        ptPos.y = nRealLine;

        // Scroll line to center of view
        int nScrollLine = nRealLine;
        nScrollLine -= GetScreenLines() / 2;
        if (nScrollLine < 0)
        {
            nScrollLine = 0;
        }

        ScrollToLine(nScrollLine, false, true);
        SetCursorPos(ptPos);
        //SetAnchor(ptPos);
    }
    
	// React to menu selections.
	public void actionPerformed(ActionEvent e) 
	{
	    if ("Apply Change".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        OnMenuApplyChange();
	    }
	    else if ("Remove Change".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        OnMenuRemoveChange();
	    }
	    else if ("Apply Both Changes".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        OnMenuApplyBothChanges();
	    }
	}
	
	void OnMenuApplyChange() 
	{
	    if (m_pMergeView == null)
	    {
	        return;
	    }
	    
	    this.OnApplyChange();
	    
	    InvalidateLines(m_ptStart.y, m_ptEnd.y, true);
	}
	
	void OnMenuRemoveChange() 
	{
	    if (m_pMergeView == null)
	    {
	        return;
	    }
	    
	    this.OnRemoveChange();
	    
	    InvalidateLines(m_ptStart.y, m_ptEnd.y, true);
	}
	
	void OnMenuApplyBothChanges() 
	{
	    if (m_pMergeView == null)
	    {
	        return;
	    }
	    
	    this.OnApplyBothChanges();
	    
	    InvalidateLines(m_ptStart.y, m_ptEnd.y, true);
	}
	
	void OnUpdateMenuApplyChange() 
	{
	    menuItemApplyChange.setEnabled(false);
	    
		if (GetMergeBlockIndex())
		{
			assert(m_iMergedBlockIndex >= 0 && m_iMergedBlockIndex < MergeHeroApp.theApp.m_aryMergedBlock.size());
			SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);

			if (diffMergeBlock.m_bHidden)
			{
				return;
			}

			if (diffMergeBlock.m_bConflict)
			{
				if (diffMergeBlock.m_bLeftAdded && diffMergeBlock.m_bRightAdded)
				{
				    menuItemApplyChange.setEnabled(true);
					return;
				}
			}

			if (m_bLeftView)
			{
				if (!diffMergeBlock.m_bLeftIsGhost && !diffMergeBlock.m_bLeftIsNormal)
				{
				    menuItemApplyChange.setEnabled(!diffMergeBlock.m_bLeftAdded);
				}
			}
			else
			{
				if (!diffMergeBlock.m_bRightIsGhost && !diffMergeBlock.m_bRightIsNormal)
				{
				    menuItemApplyChange.setEnabled(!diffMergeBlock.m_bRightAdded);
				}
			}
		}  
	}
	
	void OnUpdateMenuRemoveChange() 
	{
	    menuItemRemoveChange.setEnabled(false);
	    
		if (GetMergeBlockIndex())
		{
		    SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);
			if (diffMergeBlock.m_bHidden)
			{
			    menuItemRemoveChange.setEnabled(false);
				return;
			}

			if (m_bLeftView)
			{
				if (!diffMergeBlock.m_bLeftIsGhost &&
					!diffMergeBlock.m_bLeftIsNormal)
				{
				    menuItemRemoveChange.setEnabled(diffMergeBlock.m_bLeftAdded);
				}
			}
			else
			{
				if (!diffMergeBlock.m_bRightIsGhost &&
					!diffMergeBlock.m_bRightIsNormal)
				{
				    menuItemRemoveChange.setEnabled(diffMergeBlock.m_bRightAdded);
				}
			}
		}
	}
	
	void OnUpdateMenuApplyBothChanges()
	{
	    menuItemApplyBothChanges.setEnabled(false);
	    
		if (GetMergeBlockIndex())
		{
		    SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);
			if (diffMergeBlock.m_bHidden)
			{
				return;
			}

			menuItemApplyBothChanges.setEnabled(diffMergeBlock.m_bConflict);
		} 
	}
	
	public void mouseReleased(MouseEvent evt)
	{
	    m_pDiffBuffer = (CDiffTextBuffer)m_pTextBuffer;
		if (!MergeHeroApp.theApp.m_bIsMerge || m_pDiffBuffer == null
                || !m_pDiffBuffer.m_bInit)
        {
			super.mouseReleased(evt);
            return;
        }
		
	    if (evt.isPopupTrigger())
	    {
	        OnUpdateMenuApplyChange();
	        OnUpdateMenuRemoveChange();
	        OnUpdateMenuApplyBothChanges();
	        menu.show((Component)evt.getSource(), evt.getX(), evt.getY());
	    }	 
	    
	    super.mouseReleased(evt);
	}
	
	public void mousePressed(MouseEvent evt)
	{
	    m_pDiffBuffer = (CDiffTextBuffer)m_pTextBuffer;
		if (!MergeHeroApp.theApp.m_bIsMerge || m_pDiffBuffer == null
                || !m_pDiffBuffer.m_bInit)
        {
			super.mousePressed(evt);
            return;
        }
		
	    if (evt.isPopupTrigger())
	    {
	        OnUpdateMenuApplyChange();
	        OnUpdateMenuRemoveChange();
	        OnUpdateMenuApplyBothChanges();
	        menu.show((Component)evt.getSource(), evt.getX(), evt.getY());
	    }	
	    
	    super.mousePressed(evt);
	}
 
	public void mouseClicked(MouseEvent evt)
	{	    
	    this.requestFocus();
	    //FileDiffViewPanel.pActiveView = this;
	    super.mouseClicked(evt);
	}
	
	boolean GetMergeBlockIndex()
	{
		CPoint pos = GetCursorPos();
		int nCurrentLine = pos.y;
		int iTotalLineCount = GetLineCount();
		m_ptStart.y = m_ptStart.x = 0;
		m_ptEnd.x = m_ptEnd.y = 0;

		if (nCurrentLine < 0 || nCurrentLine >= iTotalLineCount)
		{
			return false;
		}

		m_bLeftView = false;
		if (m_enumBufferType == EnumBufferType.LOCAL_BUFFER)
		{
			m_bLeftView = false;
		}
		else if (m_enumBufferType == EnumBufferType.SERVER_BUFFER)
		{
			m_bLeftView = true;
		}
		else if (m_enumBufferType == EnumBufferType.ORIGINAL_BUFFER)
		{
			m_bLeftView = true;
		}
		else
		{
			assert(false);
		}

		int nMergedBlockSize = MergeHeroApp.theApp.m_aryMergedBlock.size();

		if (m_bLeftView)
		{
			for (int i = 0; i < nMergedBlockSize; i++)
			{
				SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(i);

				if (nCurrentLine >= diffMergeBlock.m_nLeftStartLine && 
					nCurrentLine <= diffMergeBlock.m_nLeftEndLine)
				{
					m_iMergedBlockIndex = i;
					m_ptStart.y = diffMergeBlock.m_nLeftStartLine - (nCurrentLine - pos.y);
					m_ptEnd.y = diffMergeBlock.m_nLeftEndLine - (nCurrentLine - pos.y);
					m_ptEnd.x = GetLineLength(m_ptEnd.y);
					return true;
				}
			}
		}
		else
		{
			for (int i = 0; i < nMergedBlockSize; i++)
			{
				SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(i);

				if (nCurrentLine >= diffMergeBlock.m_nRightStartLine && 
					nCurrentLine <= diffMergeBlock.m_nRightEndLine)
				{
					m_iMergedBlockIndex = i;
					m_ptStart.y = diffMergeBlock.m_nRightStartLine - (nCurrentLine - pos.y);
					m_ptEnd.y = diffMergeBlock.m_nRightEndLine - (nCurrentLine - pos.y);
					m_ptEnd.x = GetLineLength(m_ptEnd.y);

					return true;
				}
			}
		}

		return false;
	}	
	
	int OnApplyChange()
	{
		if (m_pDiffBuffer == null || m_iMergedBlockIndex < 0 || m_iMergedBlockIndex >= MergeHeroApp.theApp.m_aryMergedBlock.size() ||
			!m_pDiffBuffer.m_bInit || !MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
		{
			assert(false);
			return -1;
		}

		SDiffMergeBlock diffMergedBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);
		int nIncrease = 0;
		
		SLineInfo li = null;
		SLineInfo lisrc = null;

		if (m_bLeftView)
		{
			for (int i = diffMergedBlock.m_nLeftStartLine; i <= diffMergedBlock.m_nLeftEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, false);
			}

			if (diffMergedBlock.m_bConflict)
			{
				nIncrease = 0;
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					if ((diffMergedBlock.m_nLeftStartLine + nIncrease) <= diffMergedBlock.m_nLeftEndLine)
					{
						SLineInfo lineInfo = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(diffMergedBlock.m_nLeftStartLine + nIncrease);
						if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
						{
						    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
							MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
						}
						else
						{
							MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
						}
					}
					else
					{
						MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
					}
					nIncrease ++;
				}

				if (!diffMergedBlock.m_bLeftAdded && !diffMergedBlock.m_bRightAdded)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines --;
				}
			}
			else
			{
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN, false);
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumMergedLines ++;
				}
			}

			diffMergedBlock.m_bLeftAdded = true;
			diffMergedBlock.m_bRightAdded = false;
		}
		else
		{
			for (int i = diffMergedBlock.m_nRightStartLine; i <= diffMergedBlock.m_nRightEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, false);
			}

			if (diffMergedBlock.m_bConflict)
			{
				nIncrease = 0;
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					if ((diffMergedBlock.m_nRightStartLine + nIncrease) <= diffMergedBlock.m_nRightEndLine)
					{
						SLineInfo lineInfo = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(diffMergedBlock.m_nRightStartLine + nIncrease);
						if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
						{
						    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
							MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
						}
						else
						{
							MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
						}
					}
					else
					{
						MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
					}

					nIncrease ++;
				}

				if (!diffMergedBlock.m_bLeftAdded && !diffMergedBlock.m_bRightAdded)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines --;
				}
			}
			else
			{
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN, false);
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumMergedLines ++;
				}
			}

			diffMergedBlock.m_bLeftAdded = false;
			diffMergedBlock.m_bRightAdded = true;
		}

		UpdateMergeView(diffMergedBlock);

		return 0;
	}

	int OnRemoveChange()
	{
		if (m_pDiffBuffer == null || m_iMergedBlockIndex < 0 || m_iMergedBlockIndex >= MergeHeroApp.theApp.m_aryMergedBlock.size() ||
			!m_pDiffBuffer.m_bInit || !MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
		{
			assert(false);
			return -1;
		}

		SDiffMergeBlock diffMergedBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);
		int nIncrease = 0;
		SLineInfo li = null;
		SLineInfo lisrc = null;

		if (m_bLeftView)
		{
			for (int i = diffMergedBlock.m_nLeftStartLine; i <= diffMergedBlock.m_nLeftEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, false);
			}

			if (diffMergedBlock.m_bConflict)
			{	// 1. set all merged lines empty
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
				}

				// 2. if right view added, add it, otherwise add old version
				if (diffMergedBlock.m_bRightAdded)
				{
					nIncrease = 0;
					for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
					{
						if ((diffMergedBlock.m_nRightStartLine + nIncrease) <= diffMergedBlock.m_nRightEndLine)
						{
							SLineInfo lineInfo = (SLineInfo)MergeHeroApp.theApp.m_LocalTextBuf.m_aryLineInfo.get(diffMergedBlock.m_nRightStartLine + nIncrease);
							if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
							{
							    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
								MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
							}
						}

						nIncrease ++;
					}
				}
				else
				{
					if ((m_iMergedBlockIndex - 1) >= 0 && (m_iMergedBlockIndex - 1) < MergeHeroApp.theApp.m_aryMergedBlock.size())
					{
						SDiffMergeBlock prevMergedBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex - 1);

						if (prevMergedBlock.m_bHidden)
						{
							nIncrease = 0;
							for (int i = prevMergedBlock.m_nMergeStartLine; i <= prevMergedBlock.m_nMergeEndLine; i++)
							{
								if ((prevMergedBlock.m_nLeftStartLine + nIncrease) <= prevMergedBlock.m_nLeftEndLine)
								{
								    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
								    lisrc = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(prevMergedBlock.m_nLeftStartLine + nIncrease);
									MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lisrc);
									MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT, 1, true);
								}
								nIncrease ++;
							}
						}
					}
				}

				if (!diffMergedBlock.m_bRightAdded)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines ++;
				}
			}
			else
			{
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN, 0, false);
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumMergedLines --;
				}
			}

			diffMergedBlock.m_bLeftAdded = false;
		}
		else
		{
			for (int i = diffMergedBlock.m_nRightStartLine; i <= diffMergedBlock.m_nRightEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, false);
			}

			if (diffMergedBlock.m_bConflict)
			{	// 1. set all merged lines empty
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);
				}

				// 2. if left view added, add it, otherwise add old version
				if (diffMergedBlock.m_bLeftAdded)
				{
					// Get left buffer
					CDiffTextBuffer pLeftBuffer = null;
					if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
					{
						pLeftBuffer = MergeHeroApp.theApp.m_ServerTextBuf;
					}
					else
					{
						pLeftBuffer = MergeHeroApp.theApp.m_OrgTextBuf;
					}

					if (pLeftBuffer == null || !pLeftBuffer.m_bInit)
					{
						assert(false);
						return -1;
					}
					
					nIncrease = 0;
					for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
					{
						if ((diffMergedBlock.m_nLeftStartLine + nIncrease) <= diffMergedBlock.m_nLeftEndLine)
						{
							SLineInfo lineInfo = (SLineInfo)pLeftBuffer.m_aryLineInfo.get(diffMergedBlock.m_nLeftStartLine + nIncrease);
							if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
							{
							    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
								MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
							}
						}

						nIncrease ++;
					}
				}
				else
				{
					if ((m_iMergedBlockIndex - 1) >= 0 && (m_iMergedBlockIndex - 1) < MergeHeroApp.theApp.m_aryMergedBlock.size())
					{
						SDiffMergeBlock prevMergedBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex - 1);

						if (prevMergedBlock.m_bHidden)
						{
							nIncrease = 0;
							for (int i = prevMergedBlock.m_nMergeStartLine; i <= prevMergedBlock.m_nMergeEndLine; i++)
							{
								if ((prevMergedBlock.m_nRightStartLine + nIncrease) <= prevMergedBlock.m_nRightEndLine)
								{
								    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
								    lisrc = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(prevMergedBlock.m_nRightStartLine + nIncrease);
									MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lisrc);
									MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT, 1, true);
								}
								nIncrease ++;
							}
						}
					}
				}

				if (!diffMergedBlock.m_bLeftAdded)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines ++;
				}
			}
			else
			{
				for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
				{
					MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN, 0, false);
					MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumMergedLines --;
				}
			}
				
			diffMergedBlock.m_bRightAdded = false;
		}

		UpdateMergeView(diffMergedBlock);

		return 0;
	}

	int OnApplyBothChanges()
	{
		if (m_pDiffBuffer == null || m_iMergedBlockIndex < 0 || m_iMergedBlockIndex >= MergeHeroApp.theApp.m_aryMergedBlock.size() ||
			!m_pDiffBuffer.m_bInit || !MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
		{
			assert(false);
			return -1;
		}

		SDiffMergeBlock diffMergedBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(m_iMergedBlockIndex);
		int nLeftIncrease = 0, nRightIncrease = 0;
		SLineInfo li = null;

		if (!diffMergedBlock.m_bConflict)
		{
			assert(false);
			return -1;
		}

		if (m_bLeftView)
		{
			for (int i = diffMergedBlock.m_nLeftStartLine; i <= diffMergedBlock.m_nLeftEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, false);
			}

			nLeftIncrease = nRightIncrease = 0;
			for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
			{
				// 1. Set merged lines empty
				MergeHeroApp.theApp.m_MergeTextBuf.ClearLineContent(i, 1);

				// 2. Add left view
				if ((diffMergedBlock.m_nLeftStartLine + nLeftIncrease) <= diffMergedBlock.m_nLeftEndLine)
				{
					SLineInfo lineInfo = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(diffMergedBlock.m_nLeftStartLine + nLeftIncrease);
					nLeftIncrease ++;
					if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
					{
					    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
						MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
						continue;
					}
				}

				// 3. Add right view
				if ((diffMergedBlock.m_nRightStartLine + nRightIncrease) <= diffMergedBlock.m_nRightEndLine)
				{
					SLineInfo  lineInfo = (SLineInfo)MergeHeroApp.theApp.m_LocalTextBuf.m_aryLineInfo.get(diffMergedBlock.m_nRightStartLine + nRightIncrease);
					if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
					{
					    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
						MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
					}
				
					nRightIncrease ++;
				}
			}

			if (!diffMergedBlock.m_bLeftAdded && !diffMergedBlock.m_bRightAdded)
			{
				MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines --;
			}

			diffMergedBlock.m_bLeftAdded = true;
			diffMergedBlock.m_bRightAdded = true;
		}
		else
		{
			for (int i = diffMergedBlock.m_nLeftStartLine; i <= diffMergedBlock.m_nLeftEndLine; i++)
			{
				m_pDiffBuffer.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_MOVED, false);
			}

			// Get left buffer
			CDiffTextBuffer pLeftBuffer = null;
			if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
			{
				pLeftBuffer = MergeHeroApp.theApp.m_ServerTextBuf;
			}
			else
			{
				pLeftBuffer = MergeHeroApp.theApp.m_OrgTextBuf;
			}

			if (pLeftBuffer == null || !pLeftBuffer.m_bInit)
			{
				assert(false);
				return -1;
			}

			nLeftIncrease = nRightIncrease = 0;
			for (int i = diffMergedBlock.m_nMergeStartLine; i <= diffMergedBlock.m_nMergeEndLine; i++)
			{
				// 1. Set merged lines hide
				MergeHeroApp.theApp.m_MergeTextBuf.SetLineFlag(i, CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN, 0, false);

				// 2. Add right view
				if ((diffMergedBlock.m_nRightStartLine + nRightIncrease) <= diffMergedBlock.m_nRightEndLine)
				{
					SLineInfo lineInfo = (SLineInfo)m_pDiffBuffer.m_aryLineInfo.get(diffMergedBlock.m_nRightStartLine + nRightIncrease);
					nRightIncrease ++;
					if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
					{
					    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
						MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
						continue;
					}
				}
			
				// 3. Add left view
				if ((diffMergedBlock.m_nLeftStartLine + nLeftIncrease) <= diffMergedBlock.m_nLeftEndLine)
				{
					SLineInfo lineInfo = (SLineInfo)pLeftBuffer.m_aryLineInfo.get(diffMergedBlock.m_nLeftStartLine + nLeftIncrease);
					if ((lineInfo.m_dwFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN)
					{
					    li = (SLineInfo)MergeHeroApp.theApp.m_MergeTextBuf.m_aryLineInfo.get(i);
						MergeHeroApp.theApp.m_MergeTextBuf.CopyLine(li, lineInfo);
					}
					nLeftIncrease ++;
				}
			}

			if (!diffMergedBlock.m_bLeftAdded && !diffMergedBlock.m_bRightAdded)
			{
				MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines --;
			}

			diffMergedBlock.m_bLeftAdded = true;
			diffMergedBlock.m_bRightAdded = true;
		}

		UpdateMergeView(diffMergedBlock);

		return 0;
	}
	
	void UpdateMergeView(SDiffMergeBlock  diffMergedBlock)
	{
	    if (m_pMergeView != null)
	    {
	        m_pMergeView.Update(diffMergedBlock.m_nMergeStartLine, diffMergedBlock.m_nMergeEndLine);
	        MergeHeroApp.theApp.m_MergeTextBuf.SetModified(true);
	    }
	}
}

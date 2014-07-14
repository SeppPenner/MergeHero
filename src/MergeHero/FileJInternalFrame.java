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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.JSplitPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CPoint;
import MergeHeroLib.SDiffMergeBlock;

import com.dynamsoft.sourceanywhere.BaseDataObject;
import com.dynamsoft.sourceanywhere.IntegerArray;

/**
 * @author Ellie Smith
 *
 */
public class FileJInternalFrame extends JInternalFrame implements InternalFrameListener
{
    private JSplitPane splitPane = null;
    private boolean m_bTwoWayMerge = false;
    public boolean m_bOnlyDiff = false;
    private FileDiffViewPanel diffPane = null;
    private StatusJPanel m_MainStatusBar = null;
    private StatusJPanel m_wndDiffFilePath = null;
    private StatusJPanel m_wndMergeViewBar = null; 
	private CDiffTextView m_pLeftView = null;
	private CDiffTextView m_pRightView = null;
	private CMergeTextView m_pMergeView = null;
	private LocationViewJPanel m_pLocationView = null;
    private String m_strCaption = "";
    private IntegerArray m_aryDiff = new IntegerArray();
    private JFileChooser fc = new JFileChooser();
    
    private int m_nCurDiff = 0;
//  Non-diff lines shown above diff when scrolling to it
    final int CONTEXT_LINES_ABOVE = 5;
//     Non-diff lines shown below diff when scrolling to it
    final int CONTEXT_LINES_BELOW = 3;
    CPoint ptStart = new CPoint(0, 0), ptEnd = new CPoint(0, 0);
    
    private MainJFrame mainFrame = null;
    public boolean m_bActived = false;

	/**
	* Auto-generated main method to display this 
	* JInternalFrame inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		FileJInternalFrame inst = new FileJInternalFrame(false);
		inst.setCaption("File diff");
		JDesktopPane jdp = new JDesktopPane();
		jdp.add(inst);
		jdp.setPreferredSize(inst.getPreferredSize());
		frame.setContentPane(jdp);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void setMainFrame(MainJFrame mainFrame)
	{
		this.mainFrame = mainFrame;
		
		if (m_pLeftView != null)
		{
			m_pLeftView.mainFrame = mainFrame;
		}
		if (m_pRightView != null)
		{
			m_pRightView.mainFrame = mainFrame;
		}
		if (m_pMergeView != null)
		{
			m_pMergeView.mainFrame = mainFrame;
		}
	}
	
	public FileJInternalFrame(boolean bOnlyDiff) 
	{
		super();
		m_bOnlyDiff = bOnlyDiff;
		initGUI(bOnlyDiff);
		addInternalFrameListener(this);
	}
	
	public void setCaption(String strCaption)
	{
	    m_strCaption = strCaption;
	    this.setTitle(m_strCaption);
	}
	
	private void initGUI(boolean bOnlyDiff)
	{
		try {
			setPreferredSize(new Dimension(400, 300));
			setBounds(new Rectangle(0, 0, 400, 300));
			this.getContentPane().setLayout(new BorderLayout());
			diffPane = new FileDiffViewPanel(bOnlyDiff);
			getContentPane().add(diffPane);
			m_MainStatusBar = new StatusJPanel(3, 310);
			getContentPane().add(m_MainStatusBar, BorderLayout.SOUTH);
			setVisible(true);
			setClosable(true);
			setMaximizable(true);
			setIconifiable(true);
			setResizable(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileDiffViewPanel getDiffPane()
	{
	    return diffPane;
	}
	
	public boolean OnFileOpenThreeway()
	{
		m_bTwoWayMerge = false;

		MergeHeroApp.theApp.m_bIsMerge = true;

		if (!ThreeWayMerge())
		{
			return false;
		}
		
		return true;
	}

	public boolean OnFileOpenTwoway()
	{
		m_bTwoWayMerge = true;
		MergeHeroApp.theApp.m_bIsMerge = true;
		
		if (!OpenFilesToCompare())
		{
			return false;
		}
		
		return true;
	}
	
	public boolean OnFileDiff()
	{
		m_bTwoWayMerge = false;

		MergeHeroApp.theApp.m_bIsMerge = false;

		if (!OpenFilesToCompare())
		{
			return false;
		}

		return true;
	}
	
	public boolean OpenFilesToCompare()
	{	
	    setEnabled(false);
		
	    m_aryDiff.clear();
		m_nCurDiff = 0;
		boolean bRet = false;

		if (MergeHeroApp.theApp.m_bIsMerge)
		{
			bRet = CompareFiles();
		}
		else
		{
			bRet = CompareFilesOnlyDiff();
		}
		
		setEnabled(true);

		return bRet;
	}
	
	private boolean CompareFiles()
	{
		// Get the view
		m_pLeftView = diffPane.getLeftView();
		m_pRightView = diffPane.getRightView();	
		m_pMergeView = diffPane.getMergeView();
		m_pLocationView = diffPane.getLocationView();
		m_wndDiffFilePath = diffPane.getTopStatusbar();

		// Reset view
		m_pLeftView.ResetView();
		m_pRightView.ResetView();
		m_pMergeView.ResetView();
		m_pLocationView.ResetView();
		diffPane.reset();
		m_pLocationView.attachView(m_pLeftView, m_pRightView);

		m_pLeftView.SetStatusInterface(diffPane.getBottomStatusbar(), 0, m_pLocationView, 
		        m_pMergeView);
		m_pRightView.SetStatusInterface(diffPane.getBottomStatusbar(), 1, m_pLocationView, 
		        m_pMergeView);

		m_pMergeView.SetStatusInterface(m_MainStatusBar, m_pLocationView, m_pLeftView, m_pRightView);
		
		m_pLeftView.m_enumBufferType = EnumBufferType.SERVER_BUFFER;
		m_pRightView.m_enumBufferType = EnumBufferType.LOCAL_BUFFER;
		m_pMergeView.m_enumBufferType = EnumBufferType.MERGE_BUFFER;

		MergeHeroApp.theApp.Init();

		m_pLeftView.DetachFromBuffer();
		m_pRightView.DetachFromBuffer();
		m_pMergeView.DetachFromBuffer();
		
		if (m_bTwoWayMerge)
		{
			m_pLeftView.m_enumBufferType = EnumBufferType.ORIGINAL_BUFFER;
		}
		else
		{
		    MergeHeroApp.theApp.m_dwFlags |= EnumMergeType.Three_Way_Merge;
		}
		
		BaseDataObject bHasConflict = new BaseDataObject();
		if (MergeHeroApp.theApp.Diff(true, bHasConflict))
		{
			m_pLeftView.AttachToBuffer(null);
			m_pRightView.AttachToBuffer(null);
			m_pMergeView.AttachToBuffer(null);
			
			String str = "", strTemp = "";
			if (m_bTwoWayMerge)
			{
				strTemp = MergeHeroApp.theApp.m_OrgTextBuf.GetFilePath();
			}
			else
			{
				strTemp = MergeHeroApp.theApp.m_ServerTextBuf.GetFilePath();
			}
			
			// Use input label replace file name
			if (MergeHeroApp.theApp.m_strLeftLabel.length() != 0)
			{
				strTemp = MergeHeroApp.theApp.m_strLeftLabel;
			}
			
			if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
			{
				str = Common.LEFT_LABEL + strTemp;
			}
			else
			{
				str = strTemp;
			}
			
			m_wndDiffFilePath.setText(0, str);
			
			if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
			{
				// Use input label replace file name
				if (MergeHeroApp.theApp.m_strRightLabel.length() != 0)
				{
					str = Common.RIGHT_LABEL + MergeHeroApp.theApp.m_strRightLabel;
				}
				else
				{	
					str = Common.RIGHT_LABEL + MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
				}
			}
			else
			{
				// Use input label replace file name
				if (MergeHeroApp.theApp.m_strRightLabel.length() != 0)
				{
					str = MergeHeroApp.theApp.m_strRightLabel;
				}
				else
				{
					str = MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
				}	
			}
			m_wndDiffFilePath.setText(1, str);

			GetAllDiffs();
			
			m_pLeftView.OnUpdateCaret();
			m_pRightView.OnUpdateCaret();
			m_pMergeView.OnUpdateCaret();
			
			return true;
		}

		m_pLeftView.UpdateView(null, null, CDynamicTextBuffer.EnumUpdateFlag.UPDATE_RESET, -1);
		m_pRightView.UpdateView(null, null, CDynamicTextBuffer.EnumUpdateFlag.UPDATE_RESET, -1);
		m_pMergeView.UpdateView(null, null, CDynamicTextBuffer.EnumUpdateFlag.UPDATE_RESET, -1);
		m_wndDiffFilePath.setText(0, "");
		m_wndDiffFilePath.setText(1, "");
		
		return false;
	}

	private boolean CompareFilesOnlyDiff()
	{
		// Get the view
		m_pLeftView = diffPane.getLeftView();
		m_pRightView = diffPane.getRightView();	
		m_pLocationView = diffPane.getLocationView();
		m_wndDiffFilePath = diffPane.getTopStatusbar();

		// Reset view
		m_pLeftView.ResetView();
		m_pRightView.ResetView();
		m_pLocationView.ResetView();
		diffPane.reset();
		m_pLocationView.attachView(m_pLeftView, m_pRightView);

		m_pLeftView.SetStatusInterface(diffPane.getBottomStatusbar(), 0, m_pLocationView, 
		        m_pMergeView);
		m_pRightView.SetStatusInterface(diffPane.getBottomStatusbar(), 1, m_pLocationView, 
		        m_pMergeView);

		m_pLeftView.m_enumBufferType = EnumBufferType.ORIGINAL_BUFFER;
		m_pRightView.m_enumBufferType = EnumBufferType.LOCAL_BUFFER;
		
	    MergeHeroApp.theApp.Init();

		m_pLeftView.DetachFromBuffer();
		m_pRightView.DetachFromBuffer();
		
		BaseDataObject bHasConflict = new BaseDataObject();
		if (MergeHeroApp.theApp.Diff(true, bHasConflict))
		{
			m_pLeftView.AttachToBuffer(null);
			m_pRightView.AttachToBuffer(null);			
			
			String str = "";
			str = MergeHeroApp.theApp.m_OrgTextBuf.GetFilePath();
			// Use input label replace file name
			if (MergeHeroApp.theApp.m_strLeftLabel.length() != 0)
			{
				str = MergeHeroApp.theApp.m_strLeftLabel;
			}
			m_wndDiffFilePath.setText(0, str);
			str = MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
			// Use input label replace file name
			if (MergeHeroApp.theApp.m_strRightLabel.length() != 0)
			{
				str = MergeHeroApp.theApp.m_strRightLabel;
			}	
			
			m_wndDiffFilePath.setText(1, str);
			
			GetAllDiffs();
			
			str = "Differences: " + String.valueOf(m_aryDiff.size());
			m_MainStatusBar.setText(EnumMainStatusIndex.PANE_FILE_DIFFS, str);
			
			m_pLeftView.OnUpdateCaret();
			m_pRightView.OnUpdateCaret();
			
			return true;
		}

		m_pLeftView.UpdateView(null, null, CDynamicTextBuffer.EnumUpdateFlag.UPDATE_RESET, -1);
		m_pRightView.UpdateView(null, null, CDynamicTextBuffer.EnumUpdateFlag.UPDATE_RESET, -1);
		m_wndDiffFilePath.setText(0, "");
		m_wndDiffFilePath.setText(1, "");

		return false;
	}

	private void GetAllDiffs()
	{
		int nDiffs = MergeHeroApp.theApp.m_aryMergedBlock.size();
		m_aryDiff.clear();

		for (int i = 0; i < nDiffs; i++)
		{
			m_aryDiff.add(new Integer(i));
		}
	}

	public int SaveFile()
	{
		if (!MergeHeroApp.theApp.m_bIsMerge || !MergeHeroApp.theApp.m_MergeTextBuf.m_bInit ||
			!MergeHeroApp.theApp.m_MergeTextBuf.m_bModified)
		{
			return EnumSaveResult.Save_Ok;
		}

		int nType = JOptionPane.YES_NO_CANCEL_OPTION;
		
		String str = "";
		String strFile = "";
		BaseDataObject strError = new BaseDataObject();
		if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{
			if (MergeHeroApp.theApp.m_aryFiles.size() == 4)
			{
			    strFile = (String)MergeHeroApp.theApp.m_aryFiles.get(3);
				if (strFile.length() != 0 && 
				MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
				{
					str = "Save changes to " + (String)MergeHeroApp.theApp.m_aryFiles.get(3) + " ?";
					int nRet = JOptionPane.showConfirmDialog(this, str, m_strCaption, nType);

					if (nRet == JOptionPane.YES_OPTION)
					{
						if (MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines != 0)
						{
							str = "Unable to save merged file, "  + 
							String.valueOf(MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines) +
							" conflicts remaining!";
							JOptionPane.showMessageDialog(this, str, m_strCaption, JOptionPane.ERROR_MESSAGE);
							return EnumSaveResult.Save_Fail;
						}
						if (!MergeHeroApp.theApp.m_MergeTextBuf.SaveFile(strError))
						{
						    JOptionPane.showMessageDialog(this, "Failed to save the merge result!", m_strCaption, 
						            JOptionPane.ERROR_MESSAGE);
							return EnumSaveResult.Save_Fail;
						}

						return EnumSaveResult.Save_Ok;
					}
					else if (nRet == JOptionPane.NO_OPTION)
					{
						return EnumSaveResult.Save_Ok;
					}
					else if (nRet == JOptionPane.CANCEL_OPTION)
					{
						return EnumSaveResult.Save_Cancel;
					}
				}
			}
			else
			{
				str = "Do you want to save the changes?";

				int nRet = JOptionPane.showConfirmDialog(this, str, m_strCaption, nType);

				if (nRet == JOptionPane.YES_OPTION)
				{
					if(SaveAsFile())
					{
						return EnumSaveResult.Save_Ok;
					}
					else
					{
						return EnumSaveResult.Save_Fail;
					}
				}
				else if (nRet == JOptionPane.NO_OPTION)
				{
					return EnumSaveResult.Save_Ok;
				}
				else if (nRet == JOptionPane.CANCEL_OPTION)
				{
					return EnumSaveResult.Save_Cancel;
				}
			}
		}
		else
		{
			if (MergeHeroApp.theApp.m_aryFiles.size() == 3)
			{
			    strFile = (String)MergeHeroApp.theApp.m_aryFiles.get(2);
				if (strFile.length() != 0 && 
				MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
				{
					str = "Save changes to " + (String)MergeHeroApp.theApp.m_aryFiles.get(2) + "?";
					int nRet = JOptionPane.showConfirmDialog(this, str, m_strCaption, nType);

					if (nRet == JOptionPane.YES_OPTION)
					{
						if (!MergeHeroApp.theApp.m_MergeTextBuf.SaveFile(strError))
						{
						    JOptionPane.showMessageDialog(this, "Failed to save the merge result!", m_strCaption, 
						            JOptionPane.ERROR_MESSAGE);
							return EnumSaveResult.Save_Fail;
						}

						return EnumSaveResult.Save_Ok;
					}
					else if (nRet == JOptionPane.NO_OPTION)
					{
						return EnumSaveResult.Save_Ok;
					}
					else if (nRet == JOptionPane.CANCEL_OPTION)
					{
						return EnumSaveResult.Save_Cancel;
					}
				}
			}
			else
			{
				str = ("Do you want to save the changes?");

				int nRet = JOptionPane.showConfirmDialog(this, str, m_strCaption, nType);

				if (nRet == JOptionPane.YES_OPTION)
				{
					if(SaveAsFile())
					{
						return EnumSaveResult.Save_Ok;
					}
					else
					{
						return EnumSaveResult.Save_Fail;
					}
				}
				else if (nRet == JOptionPane.NO_OPTION)
				{
					return EnumSaveResult.Save_Ok;
				}
				else if (nRet == JOptionPane.CANCEL_OPTION)
				{
					return EnumSaveResult.Save_Cancel;
				}
			}
		}

		return EnumSaveResult.Save_Ok;
	}

	void InitForCmdLine()
	{
	    m_bOnlyDiff = !MergeHeroApp.theApp.m_bIsMerge;
		// Get the view
		m_pLeftView = diffPane.getLeftView();
		m_pRightView = diffPane.getRightView();	
		if (!m_bOnlyDiff)
		{
		    m_pMergeView = diffPane.getMergeView();
		}
		m_pLocationView = diffPane.getLocationView();

		m_wndDiffFilePath = diffPane.getTopStatusbar();

		// Reset view
		m_pLeftView.ResetView();
		m_pRightView.ResetView();
		diffPane.reset();
		if (!m_bOnlyDiff)
		{
		    m_pMergeView.ResetView();
		}
		m_pLocationView.ResetView();
		m_pLocationView.attachView(m_pLeftView, m_pRightView);

		m_pLeftView.SetStatusInterface(diffPane.getBottomStatusbar(), 0, m_pLocationView, 
		        m_pMergeView);
		m_pRightView.SetStatusInterface(diffPane.getBottomStatusbar(), 1, m_pLocationView, 
		        m_pMergeView);

		if (!m_bOnlyDiff)
		{
		    m_pMergeView.SetStatusInterface(m_MainStatusBar, m_pLocationView, m_pLeftView, m_pRightView);
		}
		
		m_pLeftView.m_enumBufferType = EnumBufferType.SERVER_BUFFER;
		m_pRightView.m_enumBufferType = EnumBufferType.LOCAL_BUFFER;
		if (!m_bOnlyDiff)
		{
		    m_pMergeView.m_enumBufferType = EnumBufferType.MERGE_BUFFER;
		}

		if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{
			m_bTwoWayMerge = false;
		}
		else
		{
			m_bTwoWayMerge = true;
		}
		
		if (m_bTwoWayMerge)
		{
			m_pLeftView.m_enumBufferType = EnumBufferType.ORIGINAL_BUFFER;
		}
		
		m_pLeftView.AttachToBuffer(null);
		m_pRightView.AttachToBuffer(null);

		if (MergeHeroApp.theApp.m_bIsMerge)
		{
			m_pMergeView.AttachToBuffer(null);
		}
		
		String str = (""), strTemp = "";
		if (m_bTwoWayMerge)
		{
			strTemp = MergeHeroApp.theApp.m_OrgTextBuf.GetFilePath();
		}
		else
		{
			strTemp = MergeHeroApp.theApp.m_ServerTextBuf.GetFilePath();
		}
		
		// Use input label replace file name
		if (MergeHeroApp.theApp.m_strLeftLabel.length() != 0)
		{
			strTemp = MergeHeroApp.theApp.m_strLeftLabel;
		}	

		if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{	
			str = Common.LEFT_LABEL + strTemp;
		}
		else
		{
			str = strTemp;
		}

		m_wndDiffFilePath.setText(0, str);

		if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
		{
			// Use input label replace file name
			if (MergeHeroApp.theApp.m_strRightLabel.length() != 0)
			{
				str = Common.RIGHT_LABEL + MergeHeroApp.theApp.m_strRightLabel;
			}
			else
			{
				str = Common.RIGHT_LABEL + MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
			}
		}
		else
		{
			// Use input label replace file name
			if (MergeHeroApp.theApp.m_strRightLabel.length() != 0)
			{
				str = MergeHeroApp.theApp.m_strRightLabel;
			}
			else
			{	
				str = MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
			}
		}

		m_wndDiffFilePath.setText(1, str);
		GetAllDiffs();
		
		m_pLeftView.OnUpdateCaret();
		m_pRightView.OnUpdateCaret();
		if (MergeHeroApp.theApp.m_bIsMerge)
		{
			m_pMergeView.OnUpdateCaret();
		}
	}
	
	private boolean ThreeWayMerge()
	{
		this.setEnabled(false);

		m_aryDiff.clear();
		m_nCurDiff = 0;
		boolean bRet = false;

		if (MergeHeroApp.theApp.m_bIsMerge)
		{
			bRet = CompareFiles();
		}
		
		this.setEnabled(true);

		return bRet;
	}

	public boolean SaveAsFile()
	{
		if (!MergeHeroApp.theApp.m_bIsMerge || !MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
		{
			return true;
		}

		if (!MergeHeroApp.theApp.m_LocalTextBuf.m_bInit || 
		        MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath().length() == 0)
		{
			return false;
		}

		String str = MergeHeroApp.theApp.m_LocalTextBuf.GetFilePath();
		String strFileToSave = "";

		int nPos = str.lastIndexOf('.');
		String strDefExt = "", strFile = "", strPath = "";

		if (nPos != -1)
		{
			strDefExt = str.substring(nPos + 1);
			
			nPos = str.lastIndexOf('\\');
			if (nPos != -1)
			{
				strFile = str.substring(nPos + 1);
				strPath = str.substring(0, nPos - 1);
			}
		}
		
		File file = new File(strPath);
		if (file.exists())
		{
		    fc.setCurrentDirectory(file);
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int iRet = fc.showSaveDialog(this);
		BaseDataObject strError = null;
        
        if (iRet == JFileChooser.APPROVE_OPTION)
        {
            file = fc.getSelectedFile();
            strFileToSave = file.getPath();
  			if (!MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
			{
				return false;
			}

			if (MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines != 0)
			{
				str = "Unable to save merged file, " + String.valueOf(MergeHeroApp.theApp.m_MergeTextBuf.m_ulNumConflictLines) +
				" conflicts remaining!";
				JOptionPane.showMessageDialog(this, str, m_strCaption, 
			            JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if (!MergeHeroApp.theApp.m_MergeTextBuf.SaveFile(strFileToSave, strError))
			{
				JOptionPane.showMessageDialog(this, "Failed to save the merge result!", m_strCaption, 
			            JOptionPane.ERROR_MESSAGE);
				return false;
			}

			// Save the saved file path
			int nSize = MergeHeroApp.theApp.m_aryFiles.size();
			if ((MergeHeroApp.theApp.m_dwFlags & EnumMergeType.Three_Way_Merge) == EnumMergeType.Three_Way_Merge)
			{
				if (nSize < 4)
				{
					MergeHeroApp.theApp.m_aryFiles.add(strFileToSave);
				}
				else
				{
				    strFile = (String)MergeHeroApp.theApp.m_aryFiles.get(3);
				    strFile = strFileToSave;
				}
			}
			else
			{
				if (nSize < 3)
				{
					MergeHeroApp.theApp.m_aryFiles.add(strFileToSave);
				}
				else
				{
				    strFile = (String)MergeHeroApp.theApp.m_aryFiles.get(2);
				    strFile = strFileToSave;
				}
			}
		}
		
		return true;
	}
	
	public void OnNextdiff() 
	{
		if (!IsLastDiff())
		{
			SelectDiff(m_nCurDiff + 1, true, false);
		}	
	}

	public boolean OnUpdateNextdiff() 
	{
		int nDiffs = m_aryDiff.size();

		if (nDiffs <= 0)
		{
			return false;
		}
		else
		{
			return !IsLastDiff();
		}	
	}

	public void OnPrevdiff() 
	{
		if (!IsFirstDiff())
		{
			SelectDiff(m_nCurDiff - 1, true, false);
		}	
	}

	public boolean OnUpdatePrevdiff() 
	{
		return !IsFirstDiff();	
	}

	public void OnFirstdiff() 
	{
		SelectDiff(0, true, false);
	}

	public boolean OnUpdateFirstdiff() 
	{
		int nDiffs = m_aryDiff.size();

		if (nDiffs == 1)
		{
			return true;
		}
		else
		{
			return !IsFirstDiff();	
		}
	}

	public boolean OnUpdateLastdiff() 
	{
		int nDiffs = m_aryDiff.size();

		if (nDiffs <= 0)
		{
			return false;
		}
		else if (nDiffs == 1)
		{
			return true;
		}
		else
		{
			return !IsLastDiff();
		}	
	}

	public void OnLastdiff() 
	{
		int nDiffs = m_aryDiff.size();
		SelectDiff(nDiffs - 1, true, false);
	}	
	
	boolean IsFirstDiff()
	{
	 	if (m_nCurDiff == 0)
	 	{
	 		return true;
	 	}
	 	else
	 	{
	 		return false;
		}
	}

	boolean IsLastDiff()
	{
		int nDiffs = m_aryDiff.size();

		if (m_nCurDiff == (nDiffs - 1))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

//	 Selects diff by number and syncs other file
//	 [in] nDiff Diff to select, must be >= 0
//	 [in] bScroll Scroll diff to view
//	 [in] bSelectText Select diff text
//	 CDiffTextView::ShowDiff()
	void SelectDiff(int nDiff, boolean bScroll, boolean bSelectText)
	{
		// Check that nDiff is valid
		if (nDiff < 0 || nDiff >= m_aryDiff.size())
		{
			return;
		}

		m_nCurDiff = nDiff;
		ShowDiff(bScroll, bSelectText);
	}
	
//	 Scrolls to current diff and/or selects diff text
//	 [in] bScroll If TRUE scroll diff to view
//	 [in] bSelectText If TRUE select diff text
//	 If bScroll and bSelectText are FALSE, this does nothing!
//	 This shouldn't be called when no diff is selected, so
//	 somebody could try to ASSERT(nDiff > -1)...
	void ShowDiff(boolean bScroll, boolean bSelectText)
	{
		int nDiffs = m_aryDiff.size();

		// Try to trap some errors
		if (m_nCurDiff >= nDiffs || m_nCurDiff < 0)
		{
			return;
		}

		if (m_nCurDiff >= 0 && m_nCurDiff < nDiffs)
		{
		    Integer index = (Integer)m_aryDiff.get(m_nCurDiff);
		    SDiffMergeBlock diffMergeBlock = (SDiffMergeBlock)MergeHeroApp.theApp.m_aryMergedBlock.get(index.intValue());
			ptStart.x = 0;
			ptStart.y = diffMergeBlock.m_nLeftStartLine;
			ptEnd.x = 0;
			ptEnd.y = diffMergeBlock.m_nLeftEndLine;

			if (bScroll)
			{
				// If diff first line outside current view - context OR
				// if diff last line outside current view - context OR
				// if diff is bigger than screen
				int nTopLine = m_pLeftView.GetTopLine();

				if ((ptStart.y < nTopLine + CONTEXT_LINES_ABOVE) ||
					(ptEnd.y >= nTopLine + m_pLeftView.GetScreenLines() - CONTEXT_LINES_BELOW) ||
					(ptEnd.y - ptStart.y) >= m_pLeftView.GetScreenLines())
				{
					int line = ptStart.y - CONTEXT_LINES_ABOVE;
					if (line < 0)
					{
						line = 0;
					}

					mainFrame.fileFrame.getDiffPane().setLeftViewVertScrollPos(line * m_pLeftView.m_nLineHeight);
				}

				if (m_pLocationView != null)
				{
				   m_pLocationView.Update(ptStart.y);
				}
				m_pLeftView.SetCursorPos(ptStart);
				m_pRightView.SetCursorPos(ptStart);
				//m_pLeftView.SetAnchor(ptStart);
				//m_pRightView.SetAnchor(ptStart);
			}
		}
	}
	
	public void saveModified()
	{
	    if (!m_bOnlyDiff && MergeHeroApp.theApp.m_MergeTextBuf.m_bModified)
        {
            int nRet = SaveFile();
            if (nRet == EnumSaveResult.Save_Fail
                    || nRet == EnumSaveResult.Save_Cancel)
            {
                this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            }
            else
            {
                diffPane.reset();
                this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            }
        }	
	    else
	    {
	        diffPane.reset();
	    }
	}
	
	public void internalFrameActivated(InternalFrameEvent e)
	{
		m_bActived = true;
	}
	
	public void internalFrameClosed(InternalFrameEvent e)
	{
	    mainFrame.FramePoint.x -= MainJFrame.FRAME_INCREASE;
	    mainFrame.FramePoint.y -= MainJFrame.FRAME_INCREASE;
	}
	
	public void internalFrameClosing(InternalFrameEvent e)
	{
	    saveModified();
	}
	
	public void internalFrameDeactivated(InternalFrameEvent e)
	{
		m_bActived = false;
	}
	
	public void internalFrameDeiconified(InternalFrameEvent e)
	{
		m_bActived = true;
	}
	
	public void internalFrameIconified(InternalFrameEvent e)
	{
		m_bActived = false;
	}
	
	public void internalFrameOpened(InternalFrameEvent e)
	{
	}
}
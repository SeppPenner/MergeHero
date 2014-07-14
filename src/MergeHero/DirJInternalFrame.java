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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import MergeHeroLib.CDynamicDirectoriesCompare;
import MergeHeroLib.CDynamicDirectoriesCompare.SDiffItem;

/**
 * @author Lincoln Burrows
 *
 */
public class DirJInternalFrame extends JInternalFrame implements InternalFrameListener
{
	private String m_strComResultDetail = "";
	private String m_strCaption = "";
	private int m_uiTotalItems = 0;
	private int m_uiTotalDiffs = 0;
	private int m_uiTotalSames = 0;
	private int m_uiSameNameDirs = 0;
	private int m_uiLeftOnlyFiles = 0;
	private int m_uiRightOnlyFiles = 0;
	private int m_uiLeftOnlyDirs = 0;
	private int m_uiRightOnlyDirs = 0;
	private int m_uiCompareError = 0;
	private CusDefTableModel jTableModel = null;
	private JTable jTable = null;
	private JScrollPane jScrollPane = null;
	private StatusJPanel statusJPanelTop = null;
	private StatusJPanel statusJPanelBottom = null;
	public static final int ITEM_EQUAL	= 0;
	public static final int ITEM_NOT_EQUAL	= 1;
	public static final int ITEM_FOLDER = 2;
	public static final int ITEM_LEFT_FILE	= 3;
	public static final int ITEM_RIGHT_FILE = 4;
	public static final int ITEM_LEFT_FOLDER = 5;
	public static final int ITEM_RIGHT_FOLDER = 6;
	public static final int ITEM_ERROR	= 7;
	public static final int ITEM_UNKNOW = 8;
	protected SimpleDateFormat dateFormat = new SimpleDateFormat();
	Object[][] data = null;
	private String strLeftDir = "", strRightDir = "";
	public boolean m_bActived = false;
	
	String[] columnNames = {"Name", "Directory", "Comparison Result", "Extension", "Left Size", "Right Size", 
			  "Left Date", "Right Date"};
	
	private int nTableRow = 0;
	private static int COUNTITEMS	= 8;
	private static int  ITEMNAME	= 0;
	private static int  DIRNAME	= 1;
	private static int  DIFFRESULT = 2;
	private static int  ITEMTYPE	= 3;
	private static int  ITEMLSIZE	= 4;
	private static int  ITEMRSIZE	= 5;
	private static int  ITEMLDATE	= 6;
	private static int  ITEMRDATE	= 7;
	private static int imgCount = 9;
	public static ImageIcon[] imgIcon = new ImageIcon[imgCount]; 

	MainJFrame mainFrame = null;
	
	/**
	* Auto-generated main method to display this 
	* JInternalFrame inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		DirJInternalFrame inst = new DirJInternalFrame();
		JDesktopPane jdp = new JDesktopPane();
		jdp.add(inst);
		jdp.setPreferredSize(inst.getPreferredSize());
		frame.setContentPane(jdp);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public DirJInternalFrame() 
	{
		super();
		initGUI();
		addInternalFrameListener(this);
		strLeftDir = MergeHeroApp.theApp.m_strCompareDir;
		strRightDir = MergeHeroApp.theApp.m_strToDir;
	}
	
	public void setMainFrame(MainJFrame mainFrame)
	{
		this.mainFrame = mainFrame;
	}
	
	public void setCaption(String strCaption)
	{
	    m_strCaption = strCaption;
	    this.setTitle(m_strCaption);
	}
	
	private void initGUI() {
		try {
			this.setPreferredSize(new java.awt.Dimension(400, 300));
			this.setBounds(0, 0, 400, 300);
			GridBagLayout thisLayout = new GridBagLayout();
			this.getContentPane().setLayout(thisLayout);
			setVisible(true);
			setClosable(true);
			setMaximizable(true);
			setIconifiable(true);
			setResizable(true);
			
			loadImages();
			
            jScrollPane = new JScrollPane();
            createTable();
            jTableModel = new CusDefTableModel(data, columnNames);
            jTable = new JTable();
            jScrollPane.setViewportView(jTable);
            jTable.setModel(jTableModel);
            jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jTable.getColumnModel().getColumn(ITEMNAME).setCellRenderer(new CusTableCellRender());
            jTable.addMouseListener(new MouseAdapter()
                    {
                public void mouseClicked(MouseEvent evt)
                {
                    if (evt.getClickCount() == 2)
                    {
                        onDbClick(jTable.getSelectedRow());
                    }
                }               
                    });
            
            statusJPanelTop = new StatusJPanel(2, 82);
            statusJPanelTop.setText(0, strLeftDir);
            statusJPanelTop.setText(1, strRightDir);
            statusJPanelBottom = new StatusJPanel(3, 310);
            statusJPanelBottom.setText(0, "Total Items: " + String.valueOf(m_uiTotalItems));
            statusJPanelBottom.setText(1, "Different files: " + String.valueOf(m_uiTotalDiffs));
            statusJPanelBottom.setText(2, "Identical files: " + String.valueOf(m_uiTotalSames));
  
			GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;           
            gbc.weightx = 100;
            gbc.weighty = 0;
            add(statusJPanelTop, gbc, 0, 0, 1, 1);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0;
            gbc.weighty = 100;
            add(jScrollPane, gbc, 0, 1, 1, 1);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.SOUTH;           
            gbc.weightx = 100;
            gbc.weighty = 0;
            add(statusJPanelBottom, gbc, 0, 2, 1, 1);
            //setContentPane(jScrollPane);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showResult(JFrame frame)
	{
        JOptionPane.showMessageDialog(frame, m_strComResultDetail, 
             m_strCaption, JOptionPane.PLAIN_MESSAGE);
	}
	
	public void loadImages()
	{
	    String imgLocation = "image/equal.GIF";
	    imgIcon[ITEM_EQUAL] = createImageIcon(imgLocation);
	    imgLocation = "image/notequal.GIF";
	    imgIcon[ITEM_NOT_EQUAL] = createImageIcon(imgLocation);
	    imgLocation = "image/folder.GIF";
	    imgIcon[ITEM_FOLDER] = createImageIcon(imgLocation);
	    imgLocation = "image/leftfile.GIF";
	    imgIcon[ITEM_LEFT_FILE] = createImageIcon(imgLocation);
	    imgLocation = "image/rightfile.GIF";
	    imgIcon[ITEM_RIGHT_FILE] = createImageIcon(imgLocation);
	    imgLocation = "image/leftfolder.GIF";
	    imgIcon[ITEM_LEFT_FOLDER] = createImageIcon(imgLocation);
	    imgLocation = "image/rightfolder.GIF";
	    imgIcon[ITEM_RIGHT_FOLDER] = createImageIcon(imgLocation);
	    imgLocation = "image/error.GIF";
	    imgIcon[ITEM_ERROR] = createImageIcon(imgLocation);
	    imgLocation = "image/unknow.GIF";
	    imgIcon[ITEM_UNKNOW] = createImageIcon(imgLocation);
	}
	
	public static ImageIcon createImageIcon(String strPath)
	{
	    URL imgURL = DirJInternalFrame.class.getResource(strPath);
	    if (imgURL != null)
	    {
	        return new ImageIcon(imgURL);
	    }
	    else
	    {
	        //System.err.println("Error to find the image.");
	        return null;
	    }
	}
	
	protected void add(Component c, GridBagConstraints gbc, int x, int y, int w, int h)
	{
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = w;
	    gbc.gridheight = h;
	    getContentPane().add(c, gbc);
	}
	
	protected void createTable()
	{
	    nTableRow = MergeHeroApp.theApp.m_aryDiffItemArray.size();
	    data = new Object[nTableRow][COUNTITEMS];
	    
	    for (int i = 0; i < nTableRow; i++)
	    {
	      CDynamicDirectoriesCompare.SDiffItem diffItem =  (SDiffItem) MergeHeroApp.theApp.m_aryDiffItemArray.get(i);

			SDirDiffInfo dirDiffInfo = new SDirDiffInfo();
			GetDirDiffInfo(diffItem, dirDiffInfo);

			int j = 0;
			SImageColumn imgColumn = new SImageColumn();
			imgColumn.m_strName = dirDiffInfo.m_strName;
			imgColumn.m_iImageIndex = dirDiffInfo.m_iImageIndex;
			
			data[i][j++] = imgColumn;
			data[i][j++] = dirDiffInfo.m_strDirectory;
			data[i][j++] = dirDiffInfo.m_strDiffReslut;
			data[i][j++] = dirDiffInfo.m_strType;
			data[i][j++] = dirDiffInfo.m_strLeftSize;
			data[i][j++] = dirDiffInfo.m_strRightSize;
			data[i][j++] = dirDiffInfo.m_strLeftDate;
			data[i][j++] = dirDiffInfo.m_strRightDate;

			m_uiTotalItems++;
	    }
	    
		m_strComResultDetail = "Comparison result :\n\nIdentical files = " + String.valueOf(m_uiTotalSames);		
		m_strComResultDetail += "\nDifferent files = " + String.valueOf(m_uiTotalDiffs);
		m_strComResultDetail += "\nFolders with the same name = ";
		m_strComResultDetail += String.valueOf(m_uiSameNameDirs) + "\nComparison errors = ";
		m_strComResultDetail += String.valueOf(m_uiCompareError)+ "\n\nComparison directories only in left folder = ";
		m_strComResultDetail += String.valueOf(m_uiLeftOnlyDirs) + "\nComparison directories only in right folder = ";
		m_strComResultDetail += String.valueOf(m_uiRightOnlyDirs) + "\n\nComparison files only in left folder = ";
		m_strComResultDetail += String.valueOf(m_uiLeftOnlyFiles) + "\nComparison files only in right folder = ";
		m_strComResultDetail += String.valueOf(m_uiRightOnlyFiles) + "\n\nTotal comparison files and directories = ";
		m_strComResultDetail += String.valueOf(m_uiTotalItems) + "\n\n\nNote: You can view these through clicking menu \"View --> Show \ndirectory comparison result.\"";
  	}
	
	public class SDirDiffInfo
	{
		public String m_strName = "";
		public String m_strDirectory = "";
		public String m_strDiffReslut = "";
		public String m_strType = "";
		public String m_strLeftSize = "";
		public String m_strRightSize = "";
		public String m_strLeftDate = "";
		public String m_strRightDate = "";
	    public int m_iImageIndex = 0;
	}
	
	public class SImageColumn
	{
		public String m_strName = "";
		public int m_iImageIndex = 0;
		
		public String toString()
		{
		    return m_strName;
		}
	}

	private void GetDirDiffInfo(CDynamicDirectoriesCompare.SDiffItem diffItem, SDirDiffInfo dirDiffInfo)
	{
		dirDiffInfo.m_iImageIndex = ITEM_UNKNOW;
		dirDiffInfo.m_strDirectory = diffItem.m_strSubDirName;
		dirDiffInfo.m_strDiffReslut = "";
		Date date = null;
		
		if (diffItem.IsSideLeft())
		{
			dirDiffInfo.m_strName = diffItem.m_leftDiffInfo.m_strName;

			dirDiffInfo.m_strLeftDate = dateFormat.format(new Date(diffItem.m_leftDiffInfo.m_mtime));
			if (!diffItem.IsDirectory())
			{
				dirDiffInfo.m_strLeftSize = String.valueOf(diffItem.m_leftDiffInfo.m_size);
			}
		}
		else if (diffItem.IsSideRight())
		{
			dirDiffInfo.m_strName = diffItem.m_rightDiffInfo.m_strName;

			dirDiffInfo.m_strRightDate = dateFormat.format(new Date(diffItem.m_rightDiffInfo.m_mtime));
			if (!diffItem.IsDirectory())
			{
				dirDiffInfo.m_strRightSize = String.valueOf(diffItem.m_rightDiffInfo.m_size);
			}
		}
		else
		{
			dirDiffInfo.m_strName = diffItem.m_rightDiffInfo.m_strName;

			dirDiffInfo.m_strLeftDate = dateFormat.format(new Date(diffItem.m_leftDiffInfo.m_mtime));
			dirDiffInfo.m_strRightDate = dateFormat.format(new Date(diffItem.m_rightDiffInfo.m_mtime));

			if (!diffItem.IsDirectory())
			{
				dirDiffInfo.m_strLeftSize = String.valueOf(diffItem.m_leftDiffInfo.m_size);
				dirDiffInfo.m_strRightSize = String.valueOf(diffItem.m_rightDiffInfo.m_size);
			}
		}
					
		if (diffItem.IsDirectory())
		{
			if (diffItem.IsSideLeft())
			{
				dirDiffInfo.m_iImageIndex = ITEM_LEFT_FOLDER;
				dirDiffInfo.m_strDiffReslut = "Left Only";

				m_uiLeftOnlyDirs ++;
			}
			else if (diffItem.IsSideRight())
			{
				dirDiffInfo.m_iImageIndex = ITEM_RIGHT_FOLDER;
				dirDiffInfo.m_strDiffReslut = "Right Only";

				m_uiRightOnlyDirs ++;
			}
			else
			{
				dirDiffInfo.m_iImageIndex = ITEM_FOLDER;

				m_uiSameNameDirs ++;
			}
		}
		else
		{
			dirDiffInfo.m_strType = diffItem.GetExtension();

			if (diffItem.IsSideLeft())
			{
				dirDiffInfo.m_iImageIndex = ITEM_LEFT_FILE;
				dirDiffInfo.m_strDiffReslut = "Left Only";

				m_uiLeftOnlyFiles ++;
			}
			else if (diffItem.IsSideRight())
			{
				dirDiffInfo.m_iImageIndex = ITEM_RIGHT_FILE;
				dirDiffInfo.m_strDiffReslut = "Right Only";

				m_uiRightOnlyFiles ++;
			}
			else
			{
				if (diffItem.IsResultSame())
				{
					dirDiffInfo.m_iImageIndex = ITEM_EQUAL;
					dirDiffInfo.m_strDiffReslut = "Identical";

					m_uiTotalSames ++;
				}
				
				if (diffItem.IsResultDiff())
				{
					dirDiffInfo.m_iImageIndex = ITEM_NOT_EQUAL;
					dirDiffInfo.m_strDiffReslut = "Different";

					m_uiTotalDiffs ++;
				}
			}

		}

		if (diffItem.IsResultError())
		{
			dirDiffInfo.m_iImageIndex = ITEM_ERROR;
			dirDiffInfo.m_strDiffReslut = "Compare Error";

			m_uiCompareError ++;
		}
	}
	
	public void ResetTableData()
	{
		m_uiTotalItems = 0;
		m_uiTotalDiffs = 0;
		m_uiTotalSames = 0;
		m_uiLeftOnlyFiles = 0;
		m_uiRightOnlyFiles = 0;
		m_uiLeftOnlyDirs = 0;
		m_uiRightOnlyDirs = 0;
		m_uiSameNameDirs = 0;
		m_uiCompareError = 0;
		
	    int i = 0;
	    for (i = jTableModel.getRowCount() - 1; i >= 0 ; i--)
	    {
	        jTableModel.removeRow(i);
	    }
	    createTable();
	    for (i = 0; i < nTableRow; i ++)
	    {
	        jTableModel.addRow(data[i]);
	    }
	    
	    if (MergeHeroApp.theApp.m_strLeftLabel.length() == 0)
	    {
	    	statusJPanelTop.setText(0, MergeHeroApp.theApp.m_strCompareDir);
	    }
	    else
	    {
	    	statusJPanelTop.setText(0, MergeHeroApp.theApp.m_strLeftLabel);
	    }
	    
	    if (MergeHeroApp.theApp.m_strRightLabel.length() == 0)
	    {
	    	statusJPanelTop.setText(1, MergeHeroApp.theApp.m_strToDir);
	    }
	    else
	    {
	    	statusJPanelTop.setText(1, MergeHeroApp.theApp.m_strRightLabel);
	    }
        statusJPanelBottom.setText(0, "Total Items: " + String.valueOf(m_uiTotalItems));
        statusJPanelBottom.setText(1, "Different files: " + String.valueOf(m_uiTotalDiffs));
        statusJPanelBottom.setText(2, "Identical files: " + String.valueOf(m_uiTotalSames));
	}
	
	public void onDbClick(int iIndex)
	{
	    if (iIndex < 0 || iIndex >= jTable.getRowCount())
	    {
	        return;
	    }
	    
		String strDiffResult = (String)data[iIndex][DIFFRESULT];
		SImageColumn imgColumn = (SImageColumn)(data[iIndex][ITEMNAME]);
		String strName = imgColumn.m_strName;
		String strSubDir = (String)data[iIndex][DIRNAME];
		String strLeftFile = "", strRightFile = "";
		
		strLeftFile += strLeftDir;
		strRightFile += strRightDir;
		
		if (strSubDir.length() != 0)
		{
			strLeftFile += File.separator;
			strRightFile += File.separator;
			strLeftFile += strSubDir;
			strRightFile += strSubDir;
		}
		
		strLeftFile += File.separator;
		strRightFile += File.separator;
		strLeftFile += strName;
		strRightFile += strName;
		
		if (strDiffResult.compareToIgnoreCase("Different") == 0)
		{
            MergeHeroApp.theApp.m_aryFiles.clear();
            MergeHeroApp.theApp.m_aryFiles.add(strLeftFile);
            MergeHeroApp.theApp.m_aryFiles.add(strRightFile);
            MergeHeroApp.theApp.m_bDirDiff = false;
            if (mainFrame.fileDiffFrame.OnFileDiff())
            {
            	mainFrame.createFileInternalFrame("Files Diff Only", true);
            }
		}
		else if (strDiffResult.length() == 0)
		{
		    File left = new File(strLeftFile);
		    File right = new File(strRightFile);
		    
		    if (left.isDirectory() && right.isDirectory())
		    {
	            MergeHeroApp.theApp.m_aryFiles.clear();
	            MergeHeroApp.theApp.m_aryFiles.add(strLeftFile);
	            MergeHeroApp.theApp.m_aryFiles.add(strRightFile);
	            MergeHeroApp.theApp.m_bDirDiff = true;
	            MergeHeroApp.theApp.m_bRecursive = false;
	            
	            this.setEnabled(false);
	            if (MergeHeroApp.theApp.DirCompare())
	            {
	            	mainFrame.createDirInternalFrame("Directory Compare");       
	            }
	            this.setEnabled(true);
		    }
		}		
	}
	
	public void internalFrameActivated(InternalFrameEvent e)
	{
		mainFrame.dirFrame = this;
	    m_bActived = true;
	}
	
	public void internalFrameClosed(InternalFrameEvent e)
	{
		mainFrame.FramePoint.x -= MainJFrame.FRAME_INCREASE;
		mainFrame.FramePoint.y -= MainJFrame.FRAME_INCREASE;
	}
	
	public void internalFrameClosing(InternalFrameEvent e)
	{
	    //MainJFrame.desktop.remove(this);
	}
	
	public void internalFrameDeactivated(InternalFrameEvent e)
	{
		m_bActived = false;
	}
	
	public void internalFrameDeiconified(InternalFrameEvent e)
	{
	}
	
	public void internalFrameIconified(InternalFrameEvent e)
	{
	}
	
	public void internalFrameOpened(InternalFrameEvent e)
	{
	}	
}

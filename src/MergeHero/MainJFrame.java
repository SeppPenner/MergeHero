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
import java.beans.PropertyVetoException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.*;

import javax.swing.JDesktopPane;
import javax.swing.WindowConstants;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CPoint;

/**
 * @author Lincoln Burrows
 *
 */
public class MainJFrame extends JFrame implements ActionListener, MenuListener, WindowListener
{
	public boolean m_bDisposed = false;
	private MainJFrame mainFrame = null;
    public JDesktopPane desktop = new JDesktopPane();
    public FileJInternalFrame fileFrame = null;
    public FileJInternalFrame fileDiffFrame = new FileJInternalFrame(true);
    private FileJInternalFrame fileMergeFrame = new FileJInternalFrame(false);;
    public DirJInternalFrame dirFrame = null;
    public CPoint FramePoint = new CPoint(-10, -10);
    private final int MENU_FILE = 0;
    private final int MENU_EDIT = 1;
    private final int MENU_VIEW = 2;
    private final int MENU_OPTIONS = 3;
    private final int MENUITEM_DIRDIFF = 0;
    private final int MENUITEM_FILEDIFF = 2;
    private final int MENUITEM_FILETWOWAYMERGE = 3;
    private final int MENUITEM_FILETHREEMERGE = 4;
    private final int MENUITEM_SAVE = 6;
    private final int MENUITEM_SAVEAS = 7;
    private final int MENUITEM_EXIT = 9;
    private final int MENUITEM_COPY = 0;
    private final int MENUITEM_SELECTALL = 2;
    private final int MENUITEM_FIND = 4;
    private final int MENUITEM_FINDNEXT = 5;
    private final int MENUITEM_DIRRESULT = 0;
    private final int MENUITEM_PDIFF = 2;
    private final int MENUITEM_NDIFF = 3;
    private final int MENUITEM_FDIFF = 4;
    private final int MENUITEM_LDIFF = 5;
    private final int MENUITEM_IGNORE_EOL = 0;
    private final int MENUITEM_IGNORE_CASE = 1;
    private final int MENUITEM_SHOW_IDENTICAL = 3;
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 300;
    public static final int FRAME_INCREASE = 30;
    
    public boolean bOperating = false;
  
    JMenuBar menuBar = new JMenuBar();
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) 
	{
	    // Schedule a job for the event-dispatching thread:
	    // creating and showing this application's GUI.
	    SwingUtilities.invokeLater(new Runnable()
	    {
	        public void run()
	        {
	        	MainJFrame frame = new MainJFrame();
	            frame.createAndShowGUI(frame);
	        }
	    });
	}
	
	public MainJFrame() {
		super("MergeHero");
		initGUI();
		addWindowListener(this);
	}
	
	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			this.getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			pack();
			setSize(800, 600);
			
			// Set up the GUI
			setJMenuBar(createMenuBar());
			desktop = new JDesktopPane();
			setContentPane(desktop);
				
			// Make dragging a little faster but perhaps uglier.
			desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);//?			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void createFileInternalFrame(String strCaption, boolean bOnlyDiff)
	{
	    JInternalFrame[] allInternalFrame = desktop.getAllFrames();
	    for (int i = allInternalFrame.length - 1; i >= 0 ; i --)
	    {
	        if (allInternalFrame[i] instanceof FileJInternalFrame)
	        {
	            desktop.remove(i);
	    	    FramePoint.x -= FRAME_INCREASE;
	    	    FramePoint.y -= FRAME_INCREASE;
	        }
	    }
	    
	    desktop.updateUI();

	    if (bOnlyDiff)
        {
            fileFrame = fileDiffFrame;
        }
        else
        {
            fileFrame = fileMergeFrame;
        }
	    fileFrame.setMainFrame(mainFrame);
        //desktop.removeAll();
	    FramePoint.x += FRAME_INCREASE;
	    FramePoint.y += FRAME_INCREASE;
	    fileFrame.reshape(FramePoint.x, FramePoint.y, FRAME_WIDTH, FRAME_HEIGHT);
        desktop.add(fileFrame);
	   
 	    fileFrame.setVisible(true);
	    try
	    {
	        fileFrame.setCaption(strCaption);
	        fileFrame.setSelected(true);
            try
            {
                fileFrame.setMaximum(true);
                fileFrame.getDiffPane().getDiffSplitPane().setDividerLocation(0.5);
             }
            catch (PropertyVetoException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
	    }
	    catch(PropertyVetoException e)
	    {	 
	        e.printStackTrace();
	    }
	}

	protected void createDirInternalFrame(String strCaption)
	{
	    DirJInternalFrame newDirFrame = new DirJInternalFrame();
	    dirFrame = newDirFrame;
	    if (dirFrame == null)
	    {
	        return;
	    }
	    
	    dirFrame.setMainFrame(mainFrame);
		
	    FramePoint.x += FRAME_INCREASE;
	    FramePoint.y += FRAME_INCREASE;
	    dirFrame.reshape(FramePoint.x, FramePoint.y, FRAME_WIDTH, FRAME_HEIGHT); 
	    dirFrame.ResetTableData();
        dirFrame.setVisible(true);
        desktop.add(dirFrame);	 

        try
        {
            dirFrame.setSelected(true);
            dirFrame.setCaption(strCaption);
            try
            {
                dirFrame.setMaximum(true);
            }
            catch (PropertyVetoException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            dirFrame.showResult(mainFrame);
        }
        catch (PropertyVetoException e)
        {
            e.printStackTrace();
        }
	}	

	protected JMenuBar createMenuBar()
	{
	    // Set up the file menu.
	    JMenu menu = new JMenu("File");
	    menu.setMnemonic(KeyEvent.VK_F);
	    menu.addMenuListener(this);
	    menuBar.add(menu);
	    
	    // Set up the the File menu item.
	    // Add compare dir item
	    menu.add(addMenuItem("Compare two directories...", KeyEvent.VK_C, 
	            KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), "Compare two directories..."));
	    
	    // Add seprator
	    menu.addSeparator();
	    
		// Add diff two files
	    menu.add(addMenuItem("Diff two files...", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK), 
	            "Diff two files..."));
	    
		// Add Merge two files
	    menu.add(addMenuItem("Merge two files...", KeyEvent.VK_M, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), 
	            "Merge two files..."));
	    
		// Add three-way Merge
	    menu.add(addMenuItem("Merge three files...", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK), 
	            "Merge three files..."));
	    
	    // Add seprator
	    menu.addSeparator();
	    
		// Add save 
	    menu.add(addMenuItem("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), 
	            "Save"));

		// Add save as
	    menu.add(addMenuItem("Save As", KeyEvent.VK_A, null, "Save As"));

	    // Add seprator
	    menu.addSeparator();

	    // Add Exit
	    menu.add(addMenuItem("Exit", KeyEvent.VK_X, null, "Exit"));
	    
	    // Set up the edit menu.
	    menu = new JMenu("Edit");
	    menu.setMnemonic(KeyEvent.VK_E);
	    menu.addMenuListener(this);
	    menuBar.add(menu);
	    
	    // Set up the the edit menu item.
	    // Add copy
	    menu.add(addMenuItem("Copy", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
	            "Copy"));
	    
	    // Add seprator
	    menu.addSeparator();
	    
		// Add select all
	    menu.add(addMenuItem("Select All", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK),
	            "Select All"));
	    
	    // Add seprator
	    menu.addSeparator();
	    
		// Add find 
	    menu.add(addMenuItem("Find", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), "Find"));

		// Add find next
	    menu.add(addMenuItem("Find Next", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
	            "Find Next"));

	    // Set up the the view menu item.
	    menu = new JMenu("View");
	    menu.setMnemonic(KeyEvent.VK_V);
	    menu.addMenuListener(this);
	    menuBar.add(menu);
	    
	    // Add view result
	    menu.add(addMenuItem("Show directory comparison result", KeyEvent.VK_R,
	            null, "Show directory comparison result"));
	    
	    // Add seprator
	    menu.addSeparator();
	    
	    // goto next difference
	    menu.add(addMenuItem("Previous difference", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK),
        "Previous difference"));	    
	    menu.add(addMenuItem("Next difference", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK),
	            "Next difference"));
	    menu.add(addMenuItem("First difference", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK),
        "First difference"));	    
	    menu.add(addMenuItem("Last difference", KeyEvent.VK_L, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK),
	            "Last difference"));
	    
	    // Set up diff options menu item
	    menu = new JMenu("Options");
	    menu.setMnemonic(KeyEvent.VK_O);
	    menu.addMenuListener(this);
	    menuBar.add(menu);
	    
	    menu.add(addCheckboxMenuItem("Ignore End-of-Line terminator", KeyEvent.VK_E, 
	    		"Ignore End-of-Line terminator"));
	    menu.add(addCheckboxMenuItem("Ignore Case", KeyEvent.VK_C, 
				"Ignore Case"));

	    // Add seprator
	    menu.addSeparator();
	    
	    menu.add(addCheckboxMenuItem("Show Identical Files", KeyEvent.VK_I, 
				"Show Identical Files"));
	    menuBar.add(menu);
	    
	    // Set up the the about menu item.
	    menu = new JMenu("Help");
	    menu.setMnemonic(KeyEvent.VK_H);
	    menuBar.add(menu);
	    
	    // Add about result
	    menu.add(addMenuItem("About MergeHero...", KeyEvent.VK_A, null, null));
	    
	    return menuBar;	    
	}
	
	protected JCheckBoxMenuItem addCheckboxMenuItem(String strMenu, int nMnemonic, 
	        String strActionCmd)
	{
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(strMenu);
	    menuItem.setMnemonic(nMnemonic);
	    menuItem.setActionCommand(strActionCmd);
	    menuItem.addActionListener(this);
	    
	    return menuItem;		
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
	
	protected JToolBar createToolBar(String strText)
	{
	    JToolBar toolBar = new JToolBar(strText);
	    
	    toolBar.add(addButton("Save", "save", "Save", "Save file"));
	    toolBar.add(addButton("Next", "next_d", "Next difference", "Scroll to the next difference"));
	    toolBar.add(addButton("Prev", "prev_d", "Previous difference", "Scroll to the previous difference"));
	    toolBar.add(addButton("Last", "last_d", "Last difference", "Scroll to the last differencee"));
	    toolBar.add(addButton("First", "first_d", "First difference", "Scroll to the first difference"));
	    
	    toolBar.setOpaque(true);
    
	    return toolBar;
	}
	
	protected JButton addButton(String strText, String strImg, 
	        String strActionCmd, String strToolTip)
	{
	    JButton button = new JButton();
	    button.setActionCommand(strActionCmd);
	    button.setToolTipText(strToolTip);
	    String imgLocation = "image/" + strImg + ".jpeg";
	    URL imgURL = MainJFrame.class.getResource(imgLocation);
	    if (imgURL != null)
	    {
	        button.setIcon(new ImageIcon(imgURL, strText));
	    }
	    else
	    {
	        button.setText(strText);
	    }
	    button.addActionListener(this);
	    
	    return button;
	}
	
	// React to menu selections.
	public void actionPerformed(ActionEvent e) 
	{
	    if ("Compare two directories...".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        DirCompareJDialog.createAndShowGUI(this, MergeHeroApp.theApp.m_strCompareDir, MergeHeroApp.theApp.m_strToDir);
	        if (DirCompareJDialog.bDoOk)
	        {
	            this.setEnabled(false);
	            bOperating = true;
	            if (MergeHeroApp.theApp.DirCompare())
	            {
	                createDirInternalFrame("Directory Compare");       
	            }
	            else
	            {
	            	desktop.removeAll();
	            }
	            boolean bEnabled = false;
	            while (!bEnabled)
	            {
	            	bEnabled = dirFrame.isEnabled();
	            	//System.out.println(bEnabled);
	            	
	            	try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	            bOperating = false;
	            this.setEnabled(true);
	        }
	    }
	    else if ("Diff two files...".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        FileDiffJDialog.createAndShowGUI(this, "Select files to Diff");
	        if (FileDiffJDialog.bDoOk)
	        {
	        	this.setEnabled(false);
	        	bOperating = true;
               if (fileDiffFrame.OnFileDiff())
                {
                    createFileInternalFrame("Files Diff Only", true);
                }
                else
                {
                	desktop.removeAll();
                }
	            boolean bEnabled = false;
	            while (!bEnabled)
	            {
	            	bEnabled = fileDiffFrame.isEnabled();
	            	//System.out.println(bEnabled);
	            	
	            	try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            } 
	            bOperating = false;
               this.setEnabled(true);
               desktop.updateUI();
            }
	    }
	    else if ("Merge two files...".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        FileDiffJDialog.createAndShowGUI(this, "Select files to compare and merge");
	        if (FileDiffJDialog.bDoOk)
	        {
	        	this.setEnabled(false);
	        	bOperating = true;
	            if (fileMergeFrame.OnFileOpenTwoway())
	            {
	                createFileInternalFrame("Files Diff and Merge", false);
	            }
	            else
	            {
	            	desktop.removeAll();
	            }
	            boolean bEnabled = false;
	            while (!bEnabled)
	            {
	            	bEnabled = fileMergeFrame.isEnabled();
	            	//System.out.println(bEnabled);
	            	
	            	try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	            bOperating = false;
	            this.setEnabled(true);
	            desktop.updateUI();
	        }
	    }
	    else if ("Merge three files...".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        ThreeWayMergeJDialog.createAndShowGUI(this);
	        if (ThreeWayMergeJDialog.bDoOk)
	        {
	        	this.setEnabled(false);
	        	bOperating = true;
	            if (fileMergeFrame.OnFileOpenThreeway())
	            {
	                createFileInternalFrame("Three-way merge", false);
	            }
	            else
	            {
	            	desktop.removeAll();
	            }
	            boolean bEnabled = false;
	            while (!bEnabled)
	            {
	            	bEnabled = fileMergeFrame.isEnabled();
	            	//System.out.println(bEnabled);
	            	
	            	try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	            bOperating = false;
	            this.setEnabled(true);
	            desktop.updateUI();
	        }
	    }
	    else if ("Save".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	       if (fileFrame != null)
	       {
	       		this.setEnabled(false);
	           fileFrame.SaveFile();
	           this.setEnabled(true);
	       }
	    }
	    else if ("Save As".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
		       if (fileFrame != null)
		       {
		       	this.setEnabled(false);
		           fileFrame.SaveAsFile();
		           this.setEnabled(true);
		       }	        
	    }
	    else if ("About MergeHero...".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        AboutJDialog.createAndShowGUI(this);
	    }
	    else if ("Exit".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        quit();
	    }
	    else if ("Show directory comparison result".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        dirFrame.showResult(this);
	    }
	    else if ("Previous difference".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.OnPrevdiff();
	        }
	    }
	    else if ("Next difference".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.OnNextdiff();
	        }	       
	    }
	    else if ("First difference".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.OnFirstdiff();
	        }	        
	    }
	    else if ("Last difference".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.OnLastdiff();
	        }	        
	    }
	    else if ("Copy".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
                fileFrame.getDiffPane().copy();
	        }	        
	    }
	    else if ("Select All".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
                fileFrame.getDiffPane().selectAll();
	        }	   	        
	    }
	    else if ("Find".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.getDiffPane().find();
	        }
	    }
	    else if ("Find Next".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (fileFrame != null && fileFrame.isVisible())
	        {
	            fileFrame.getDiffPane().findNext();
	        }
	    }	  
	    else if ("Ignore End-of-Line terminator".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	    	MergeHeroApp.theApp.m_diffOptions.m_bIgnoreEOL = !MergeHeroApp.theApp.m_diffOptions.m_bIgnoreEOL; 
	    }
	    else if ("Ignore Case".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	    	MergeHeroApp.theApp.m_diffOptions.m_bIgnoreCase = !MergeHeroApp.theApp.m_diffOptions.m_bIgnoreCase;
	    }
	    else if ("Show Identical Files".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	    	MergeHeroApp.theApp.m_diffOptions.m_bShowIdenticalFiles = !MergeHeroApp.theApp.m_diffOptions.m_bShowIdenticalFiles; 
		}	  
	}
	
	public void disableSomeMenu()
	{
	    menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_DIRRESULT).setEnabled(false);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_PDIFF).setEnabled(false);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_NDIFF).setEnabled(false);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_FDIFF).setEnabled(false);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_LDIFF).setEnabled(false);	
        
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVEAS).setEnabled(false);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVE).setEnabled(false);  
        
        enbaleEditMenu(false);
	}
	
	public void enbaleEditMenu(boolean bEnabled)
	{
        menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_COPY).setEnabled(bEnabled);
        menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_SELECTALL).setEnabled(bEnabled);
        menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_FIND).setEnabled(bEnabled);
        menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_FINDNEXT).setEnabled(bEnabled);	    
	}
	
	private void enableAllMenu(boolean bEnabled)
	{
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_DIRDIFF).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_FILEDIFF).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_FILETWOWAYMERGE).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_FILETHREEMERGE).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVEAS).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVE).setEnabled(bEnabled);
        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_EXIT).setEnabled(bEnabled);  

        enbaleEditMenu(bEnabled);
 
	    menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_DIRRESULT).setEnabled(bEnabled);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_PDIFF).setEnabled(bEnabled);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_NDIFF).setEnabled(bEnabled);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_FDIFF).setEnabled(bEnabled);
        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_LDIFF).setEnabled(bEnabled);	
        
        menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_IGNORE_EOL).setEnabled(bEnabled);
        menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_IGNORE_CASE).setEnabled(bEnabled);
        menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_SHOW_IDENTICAL).setEnabled(bEnabled);
	}
	
	public void menuSelected(MenuEvent evt)
	{
		if (bOperating)
		{
			enableAllMenu(false);
			return;
		}
		else
		{
			enableAllMenu(true);
		}
		
		disableSomeMenu();
		
		if (desktop.getAllFrames().length == 0)
		{
		    return;
		}

		if (MergeHeroApp.theApp.m_bIsMerge && MergeHeroApp.theApp.m_MergeTextBuf.m_bInit)
        {
		    if (MergeHeroApp.theApp.m_MergeTextBuf.m_bModified)
		    {
		        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVE).setEnabled(true);
		    }
		    else
		    {
		        menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVE).setEnabled(false);
		    }
		    menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVEAS).setEnabled(true);
        }
        else
        {
            menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVE).setEnabled(false);
            menuBar.getMenu(MENU_FILE).getItem(MENUITEM_SAVEAS).setEnabled(false);
        }
		
		if (dirFrame != null && dirFrame.m_bActived)
		{
		    menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_DIRRESULT).setEnabled(true);
		    enbaleEditMenu(false);
		}
		else
		{
		    menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_DIRRESULT).setEnabled(false);		    
		    enbaleEditMenu(false);
		    
		    if (fileFrame != null && fileFrame.m_bActived)
		    {
		        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_PDIFF).setEnabled(fileFrame.OnUpdatePrevdiff());
		        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_NDIFF).setEnabled(fileFrame.OnUpdateNextdiff());
		        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_FDIFF).setEnabled(fileFrame.OnUpdateFirstdiff());
		        menuBar.getMenu(MENU_VIEW).getItem(MENUITEM_LDIFF).setEnabled(fileFrame.OnUpdateLastdiff());
		        
		        if (FileDiffViewPanel.pActiveView != null)
		        {
		            menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_COPY).setEnabled(fileFrame.getDiffPane().isSelection());
		            menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_SELECTALL).setEnabled(true);
		            menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_FIND).setEnabled(true);
		            menuBar.getMenu(MENU_EDIT).getItem(MENUITEM_FINDNEXT).setEnabled(true);
		        }
		    }
		}	
		
		JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_IGNORE_EOL);
		menuItem.setState(MergeHeroApp.theApp.m_diffOptions.m_bIgnoreEOL);
		menuItem = (JCheckBoxMenuItem)menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_IGNORE_CASE);
		menuItem.setState(MergeHeroApp.theApp.m_diffOptions.m_bIgnoreCase);
		menuItem = (JCheckBoxMenuItem)menuBar.getMenu(MENU_OPTIONS).getItem(MENUITEM_SHOW_IDENTICAL);
		menuItem.setState(MergeHeroApp.theApp.m_diffOptions.m_bShowIdenticalFiles);		
	}
	
	public void menuDeselected(MenuEvent evt)
	{// do nothing
	    
	}
	
	public void menuCanceled(MenuEvent evt)
	{// do nothing
	    
	}
	
	// quit the application
	protected void quit()
	{
	    saveModified();
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public void createAndShowGUI(MainJFrame frame)
	{
	    // Make sure we have nice window decorations.
	    //JFrame.setDefaultLookAndFeelDecorated(true);
	    // Display the window.
	    mainFrame = frame;
	    mainFrame.setVisible(true);
	}
	
	public void InitForCmdLine(String strCaption)
	{
		this.setEnabled(false);
		bOperating = true;
		//System.out.println(bOperating);
		
		if (MergeHeroApp.theApp.m_bDirDiff)
        {
 		    createDirInternalFrame(strCaption);
 		    
            boolean bEnabled = false;
            while (!bEnabled)
            {
            	bEnabled = dirFrame.isEnabled();
            	//System.out.println(bEnabled);
            	
            	try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
        else
        {
	        if (MergeHeroApp.theApp.m_bIsMerge)
	        {
	            fileFrame = fileMergeFrame;
	        }
	        else
	        {
	            fileFrame = fileDiffFrame;
	        }
            fileFrame.InitForCmdLine();
            createFileInternalFrame(strCaption, !MergeHeroApp.theApp.m_bIsMerge);
            
            boolean bEnabled = false;
            while (!bEnabled)
            {
            	bEnabled = fileFrame.isEnabled();
            	//System.out.println(bEnabled);
            	
            	try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
         }
		bOperating = false;
		//System.out.println(bOperating);
		this.setEnabled(true);
	}
	
	public void saveModified()
	{
	    if (MergeHeroApp.theApp.m_bIsMerge && MergeHeroApp.theApp.m_MergeTextBuf.m_bModified &&
	            fileFrame != null && fileFrame.isVisible())
        {
            int nRet = fileFrame.SaveFile();
            if (nRet == EnumSaveResult.Save_Fail
                    || nRet == EnumSaveResult.Save_Cancel)
            {
                this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            }
            else
            {
                this.dispose();
            }
        }
	    else
	    {
	    	this.dispose();
	    }
	}

	public void windowClosing(WindowEvent e)
	{
	    saveModified();
	}
	public void windowOpened(WindowEvent e)
	{	    
	}
	public void windowClosed(WindowEvent e)
	{	    
	}
	public void windowIconified(WindowEvent e)
	{	    
	}
	public void windowDeiconified(WindowEvent e)
	{	    
	}
	public void windowActivated(WindowEvent e)
	{	    
	}
	public void windowDeactivated(WindowEvent e)
	{	    
	}
	
	public void ShowDiff()
	{
		createFileInternalFrame("Files Diff Only", true);
	}
	
	public void dispose()
	{
        while (bOperating)
        {       	
        	try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }	

 		m_bDisposed = true;
		super.dispose();
	}
}


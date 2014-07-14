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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;


import javax.swing.JPanel;
import javax.swing.JFrame;

/**
 * @author Lincoln Burrows
 *
 */
public class DirCompareJDialog extends javax.swing.JDialog implements ActionListener, KeyListener 
{
	private JPanel jPanelGroupBox;
	private JTextField jTextFieldLeft;
	private JButton jButtonLeftBrowse;
	private JButton jButtonRightBrowse;
	private JButton jButtonCancel;
	private JButton jButtonOk;
	private JCheckBox jCheckBoxRecursive;
	private JTextField jTextFieldRight;
	private JLabel jLabelRight;
	private JLabel jLabelLeft;
	private JFileChooser fc = new JFileChooser();
	
	private boolean	m_bRecursive = false;
	private String m_strLeftDir = "";
	private String m_strRightDir = "";
	public static boolean bDoOk = false; 

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setResizable(false);
		DirCompareJDialog.createAndShowGUI(frame, "", "");
	}
	
	public DirCompareJDialog(Frame owner, String strLeft, String strRight) {
		super(owner, true);
		initGUI();
		bDoOk = false;
		m_strLeftDir = strLeft;
		m_strRightDir = strRight;
		jTextFieldLeft.setText(m_strLeftDir);
		jTextFieldRight.setText(m_strRightDir);
		
		if (m_strLeftDir.length() != 0 && m_strRightDir.length() != 0)
		{
		  jButtonOk.setEnabled(true);
		}
	}
	
	private void initGUI() {
		try {
			this.setName("DlgDirCompare");
			this.setTitle("Select Directories To Compare");
            this.getContentPane().setLayout(null);
            {
                jPanelGroupBox = new JPanel();
                jPanelGroupBox.setLayout(null);
                this.getContentPane().add(jPanelGroupBox);
                jPanelGroupBox.setBounds(6, 11, 422, 114);
                jPanelGroupBox.setBorder(BorderFactory.createTitledBorder("Directories To Compare"));
                jPanelGroupBox.setOpaque(false);
                {
                }
                {
                    jTextFieldRight = new JTextField();
                    jPanelGroupBox.add(jTextFieldRight);
                    jTextFieldRight.addKeyListener(this);
                    jTextFieldRight.setBounds(59, 58, 260, 24);
                }
                {
                    jLabelRight = new JLabel();
                    jPanelGroupBox.add(jLabelRight);
                    jLabelRight.setText("Right:");
                    jLabelRight.setBounds(8, 59, 72, 20);
                }
                {
                    jLabelLeft = new JLabel();
                    jPanelGroupBox.add(jLabelLeft);
                    jLabelLeft.setText("Left:");
                    jLabelLeft.setBounds(8, 29, 49, 17);
                }
                {
                    jTextFieldLeft = new JTextField();
                    jPanelGroupBox.add(jTextFieldLeft);
                    jTextFieldLeft.addKeyListener(this);
                     jTextFieldLeft.setBounds(59, 25, 260, 24);
                  }
                {
                    jButtonLeftBrowse = new JButton();
                    jPanelGroupBox.add(jButtonLeftBrowse);
                    jButtonLeftBrowse.setText("Browse");
                    jButtonLeftBrowse.setMnemonic(KeyEvent.VK_B);
                    jButtonLeftBrowse.setActionCommand("Left Browse");
                    jButtonLeftBrowse.addActionListener(this);
                    jButtonLeftBrowse.setBounds(332, 25, 80, 24);
                }
                {
                    jCheckBoxRecursive = new JCheckBox();
                    jPanelGroupBox.add(jCheckBoxRecursive);
                    jCheckBoxRecursive.setText("Recursive");
                    jCheckBoxRecursive.setMnemonic(KeyEvent.VK_R);
                    jCheckBoxRecursive.setActionCommand("Recursive");
                    jCheckBoxRecursive.addActionListener(this);
                    jCheckBoxRecursive.setBounds(55, 89, 128, 17);
                }
                {
                    jButtonRightBrowse = new JButton();
                    jPanelGroupBox.add(jButtonRightBrowse);
                    jButtonRightBrowse.setText("Browse");
                    jButtonRightBrowse.setMnemonic(KeyEvent.VK_W);
                    jButtonRightBrowse.setActionCommand("Right Browse");
                    jButtonRightBrowse.addActionListener(this);
                    jButtonRightBrowse.setBounds(332, 58, 80, 24);
                }
            }
            {
                jButtonOk = new JButton();
                this.getContentPane().add(jButtonOk);
                jButtonOk.setText("Ok");
                jButtonOk.setActionCommand("Ok");
                jButtonOk.setEnabled(false);
                jButtonOk.addActionListener(this);
                jButtonOk.setBounds(433, 19, 75, 24);
            }
            {
                jButtonCancel = new JButton();
                this.getContentPane().add(jButtonCancel);
                jButtonCancel.setText("Cancel");
                jButtonCancel.setActionCommand("Cancel");                
                jButtonCancel.addActionListener(this);
                jButtonCancel.setBounds(433, 52, 75, 24);
            }
			this.setSize(523, 160);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public static void createAndShowGUI(JFrame frame, String strLeft, String strRight)
	{
	    // Make sure we have nice window decorations.
	    //JDialog.setDefaultLookAndFeelDecorated(true);
	    
	    // Create and set up the window.
	    DirCompareJDialog dirCompareJDlg = new DirCompareJDialog(frame, strLeft, strRight);
	    //dirCompareJDlg.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
	    
	    // Display the window
	    dirCompareJDlg.setResizable(false);
	    dirCompareJDlg.setVisible(true);
	}
	
	public void keyTyped(KeyEvent e)
	{
	    EnableOkBtn();
	}
	
	public void keyPressed(KeyEvent e)
	{
	    EnableOkBtn();	    
	}
	
	public void keyReleased(KeyEvent e)
	{
	    EnableOkBtn();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if ("Left Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strLeftDir = file.getPath();
	            jTextFieldLeft.setText(m_strLeftDir);
	        }
	        EnableOkBtn();
	    }
	    else if ("Right Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strRightDir = file.getPath();
	            jTextFieldRight.setText(m_strRightDir);
	        }
	        EnableOkBtn();
	    }	
	    else if ("Recursive".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        m_bRecursive = jCheckBoxRecursive.isSelected();
	    }	
	    else if ("Ok".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (!EnableOkBtn())
	        {
	            return;
	        }
	        else if (!isValidDir(m_strLeftDir) || !isValidDir(m_strRightDir))
	        {
	            return;
	        }
	        else if (m_strLeftDir.compareToIgnoreCase(m_strRightDir) == 0)
	        {
	            JOptionPane.showMessageDialog(this, "The same folder is selected.", 
		                this.getTitle(), JOptionPane.ERROR_MESSAGE);        
	            return;
	        }
	        else
	        {
	            MergeHeroApp.theApp.m_aryFiles.clear();
	            MergeHeroApp.theApp.m_aryFiles.add(m_strLeftDir);
	            MergeHeroApp.theApp.m_aryFiles.add(m_strRightDir);
	            MergeHeroApp.theApp.m_bDirDiff = true;
	            MergeHeroApp.theApp.m_bRecursive = m_bRecursive;
	            bDoOk = true;
	            dispose();
	        }
	    }	
	    else if ("Cancel".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        dispose();
	    }	
	}
	
	private boolean isValidDir(String strPath)
	{
	    File file = new File(strPath);
	    
        if (!file.exists() || !file.isDirectory())
        {
            JOptionPane.showMessageDialog(this, "Invalid input " + strPath, 
	                this.getTitle(), JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        return true;
	}
	
	private void openFileChoose()
	{
	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	public boolean EnableOkBtn()
	{
	    m_strLeftDir = jTextFieldLeft.getText();
	    m_strRightDir = jTextFieldRight.getText();
        m_strLeftDir.trim();
        m_strRightDir.trim();
        
        if (m_strLeftDir.length() == 0 || m_strRightDir.length() == 0)
        {
            jButtonOk.setEnabled(false);
            return false;
        } 
        else
        {
            jButtonOk.setEnabled(true);
            return true;
        }
 	}

}

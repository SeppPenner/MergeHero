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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFrame;

/**
 * @author Ellie Smith
 *
 */
public class FileDiffJDialog extends javax.swing.JDialog implements ActionListener, KeyListener
{
	private JPanel jPanelGroupBox;
	private JTextField jTextFieldOrg;
	private JButton jButtonOrgBrowse;
	private JButton jButtonDestBrowse;
	private JButton jButtonCancel;
	private JButton jButtonOk;
	private JTextField jTextFieldDest;
	private JLabel jLabelDest;
	private JLabel jLabelOrg;    

	private JFileChooser fc = new JFileChooser();
	
	private String m_strOrgFile = "";
	private String m_strDestFile = "";
	public static boolean bDoOk = false; 
	private String m_strCaption = "Selcet Files To Diff" ;
	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		FileDiffJDialog.createAndShowGUI(frame, "Select files to diff");
	}
	
	public FileDiffJDialog(JFrame frame, String strCaption) {
		super(frame, true);
		m_strCaption = strCaption;
		initGUI();
		bDoOk = false;
	}
	
	private void initGUI() {
		try {
			this.setName("FileDiff");
			this.setTitle(m_strCaption);
            this.getContentPane().setLayout(null);
            {
                jPanelGroupBox = new JPanel();
                jPanelGroupBox.setLayout(null);
                this.getContentPane().add(jPanelGroupBox);
                jPanelGroupBox.setBounds(6, 11, 422, 100);
                jPanelGroupBox.setBorder(BorderFactory.createTitledBorder("Files To Diff"));
                jPanelGroupBox.setOpaque(false);
                {
                }
                {
                    jTextFieldDest = new JTextField();
                    jPanelGroupBox.add(jTextFieldDest);
                    jTextFieldDest.addKeyListener(this);
                    jTextFieldDest.setBounds(85, 58, 230, 24);
                }
                {
                    jLabelDest = new JLabel();
                    jPanelGroupBox.add(jLabelDest);
                    jLabelDest.setText("Destination:");
                    jLabelDest.setBounds(8, 59, 72, 20);
                }
                {
                    jLabelOrg = new JLabel();
                    jPanelGroupBox.add(jLabelOrg);
                    jLabelOrg.setText("Original:");
                    jLabelOrg.setBounds(8, 29, 72, 20);
                }
                {
                    jTextFieldOrg = new JTextField();
                    jPanelGroupBox.add(jTextFieldOrg);
                    jTextFieldOrg.addKeyListener(this);
                     jTextFieldOrg.setBounds(85, 25, 230, 24);
                 }
                {
                    jButtonOrgBrowse = new JButton();
                    jPanelGroupBox.add(jButtonOrgBrowse);
                    jButtonOrgBrowse.setText("Browse");
                    jButtonOrgBrowse.setMnemonic(KeyEvent.VK_B);
                    jButtonOrgBrowse.setActionCommand("Org Browse");
                    jButtonOrgBrowse.addActionListener(this);
                     jButtonOrgBrowse.setBounds(332, 25, 80, 24);
                }
                {
                    jButtonDestBrowse = new JButton();
                    jPanelGroupBox.add(jButtonDestBrowse);
                    jButtonDestBrowse.setText("Browse");
                    jButtonDestBrowse.setMnemonic(KeyEvent.VK_W);
                    jButtonDestBrowse.setActionCommand("Dest Browse");
                    jButtonDestBrowse.addActionListener(this);
                     jButtonDestBrowse.setBounds(332, 58, 80, 24);
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
            this.setSize(523, 150);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public static void createAndShowGUI(JFrame frame, String strCaption)
	{
	    // Make sure we have nice window decorations.
	    // JDialog.setDefaultLookAndFeelDecorated(true);
	    
	    // Create and set up the window.
	    FileDiffJDialog fileDiffJDlg = new FileDiffJDialog(frame, strCaption);
	    //fileDiffJDlg.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
	    
	    // Display the window.
	    fileDiffJDlg.setResizable(false);
	    fileDiffJDlg.setVisible(true);
	}
	
	public void keyTyped(KeyEvent e)
	{
	    m_strOrgFile = jTextFieldOrg.getText();
	    m_strDestFile = jTextFieldDest.getText();
	    EnableOkBtn();
	}
	
	public void keyPressed(KeyEvent e)
	{
	    m_strOrgFile = jTextFieldOrg.getText();
	    m_strDestFile = jTextFieldDest.getText();
	    EnableOkBtn();	    
	}
	
	public void keyReleased(KeyEvent e)
	{
	    m_strOrgFile = jTextFieldOrg.getText();
	    m_strDestFile = jTextFieldDest.getText();
	    EnableOkBtn();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if ("Org Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strOrgFile = file.getPath();
	            jTextFieldOrg.setText(m_strOrgFile);
	        }
	        EnableOkBtn();
	    }
	    else if ("Dest Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strDestFile = file.getPath();
	            jTextFieldDest.setText(m_strDestFile);
	        }
	        EnableOkBtn();
	    }	
	    else if ("Ok".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (!EnableOkBtn())
	        {
	            return;
	        }
	        else if (!isValidFile(m_strOrgFile) || !isValidFile(m_strDestFile))
	        {
	            return;
	        }
	        else if (m_strOrgFile.compareToIgnoreCase(m_strDestFile) == 0)
	        {
	            JOptionPane.showMessageDialog(this, "The same file is selected.", 
		                this.getTitle(), JOptionPane.ERROR_MESSAGE);        
	            return;
	        }
	        else
	        {
	            MergeHeroApp.theApp.m_aryFiles.clear();
	            MergeHeroApp.theApp.m_aryFiles.add(m_strOrgFile);
	            MergeHeroApp.theApp.m_aryFiles.add(m_strDestFile);
	            MergeHeroApp.theApp.m_bDirDiff = false;
	            bDoOk = true;
	            dispose();
	        }
	    }	
	    else if ("Cancel".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        dispose();
	    }	
	}
	
	private boolean isValidFile(String strPath)
	{
	    File file = new File(strPath);
	    
        if (!file.exists() || !file.isFile())
        {
            JOptionPane.showMessageDialog(this, "Invalid input " + strPath, 
	                this.getTitle(), JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        return true;
	}
	
	private void openFileChoose()
	{
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	
	public boolean EnableOkBtn()
	{
	    m_strOrgFile = jTextFieldOrg.getText();
	    m_strDestFile = jTextFieldDest.getText();
        m_strOrgFile.trim();
        m_strDestFile.trim();
        
        if (m_strOrgFile.length() == 0 || m_strDestFile.length() == 0)
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

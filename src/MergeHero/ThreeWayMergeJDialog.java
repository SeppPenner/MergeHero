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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Lincoln Burrows
 *
 */
public class ThreeWayMergeJDialog extends javax.swing.JDialog implements ActionListener, KeyListener
{
	private JPanel jPanelGroupBox;
	private JTextField jTextFieldBase;
	private JButton jButtonBaseBrowse;
	private JButton jButtonYourBrowse;
	private JButton jButtonTheirBrowse;
	private JTextField jTextFieldTheir;
	private JLabel jLabelTheir;
	private JButton jButtonCancel;
	private JButton jButtonOk;
	private JTextField jTextFieldYour;
	private JLabel jLabelYour;
	private JLabel jLabelBase;   
	
	private JFileChooser fc = new JFileChooser();
	
	private String m_strBaseFile = "";
	private String m_strTheirFile = "";
	private String m_strYourFile = "";
	public static boolean bDoOk = false; 	

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		ThreeWayMergeJDialog.createAndShowGUI(frame);
	}
	
	public ThreeWayMergeJDialog(JFrame frame) {
		super(frame, true);
		initGUI();
		bDoOk = false;
	}
	
	private void initGUI() {
		try {
			this.setName("ThreeWayMerge");
			this.setTitle("Three-Way Merge");
            this.getContentPane().setLayout(null);
            {
                jPanelGroupBox = new JPanel();
                jPanelGroupBox.setLayout(null);
                this.getContentPane().add(jPanelGroupBox);
                 jPanelGroupBox.setBorder(BorderFactory.createTitledBorder("Files To Merge"));
                jPanelGroupBox.setBounds(6, 11, 422, 120);
                jPanelGroupBox.setOpaque(false);
                {
                    jTextFieldYour = new JTextField();
                    jPanelGroupBox.add(jTextFieldYour);
                    jTextFieldYour.addKeyListener(this);
                    jTextFieldYour.setBounds(88, 89, 230, 24);
                }
                {
                    jLabelYour = new JLabel();
                    jPanelGroupBox.add(jLabelYour);
                    jLabelYour.setText("Your File:");
                    jLabelYour.setBounds(6, 89, 72, 20);
                }
                {
                    jLabelBase = new JLabel();
                    jPanelGroupBox.add(jLabelBase);
                    jLabelBase.setText("Base File:");
                    jLabelBase.setBounds(8, 29, 72, 20);
                }
                {
                    jTextFieldBase = new JTextField();
                    jPanelGroupBox.add(jTextFieldBase);
                    jTextFieldBase.addKeyListener(this);
                    jTextFieldBase.setBounds(89, 25, 230, 24);
                 }
                {
                    jButtonBaseBrowse = new JButton();
                    jPanelGroupBox.add(jButtonBaseBrowse);
                    jButtonBaseBrowse.setText("Browse");
                    jButtonBaseBrowse.setMnemonic(KeyEvent.VK_R);
                    jButtonBaseBrowse.setActionCommand("Base Browse");
                    jButtonBaseBrowse.addActionListener(this);
                    jButtonBaseBrowse.setBounds(332, 25, 80, 24);
                }
                {
                    jButtonYourBrowse = new JButton();
                    jPanelGroupBox.add(jButtonYourBrowse);
                    jButtonYourBrowse.setText("Browse");
                    jButtonYourBrowse.setMnemonic(KeyEvent.VK_O);
                    jButtonYourBrowse.setActionCommand("Your Browse");
                    jButtonYourBrowse.addActionListener(this);
                    jButtonYourBrowse.setBounds(332, 89, 80, 24);
                }
                {
                    jLabelTheir = new JLabel();
                    jPanelGroupBox.add(jLabelTheir);
                    jLabelTheir.setText("Their File:");
                     jLabelTheir.setBounds(7, 59, 72, 20);
                }
                {
                    jTextFieldTheir = new JTextField();
                    jPanelGroupBox.add(jTextFieldTheir);
                    jTextFieldTheir.addKeyListener(this);
                    jTextFieldTheir.setBounds(89, 58, 230, 24);
                }
                {
                    jButtonTheirBrowse = new JButton();
                    jPanelGroupBox.add(jButtonTheirBrowse);
                    jButtonTheirBrowse.setText("Browse");
                    jButtonTheirBrowse.setMnemonic(KeyEvent.VK_W);
                    jButtonTheirBrowse.setActionCommand("Their Browse");
                    jButtonTheirBrowse.addActionListener(this);
                    jButtonTheirBrowse.setBounds(332, 58, 80, 24);
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
                jButtonCancel.setBounds(433, 48, 75, 24);
            }
			this.setSize(523, 171);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public static void createAndShowGUI(JFrame frame)
	{
	    // Make sure we have nice window decorations.
	    //JDialog.setDefaultLookAndFeelDecorated(true);
	    
	    // Create and set up the window.
	    ThreeWayMergeJDialog threeWayMergeDiffJDlg = new ThreeWayMergeJDialog(frame);
	    //threeWayMergeDiffJDlg.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
	    
	    // Display the window.
	    threeWayMergeDiffJDlg.setResizable(false);
	    threeWayMergeDiffJDlg.setVisible(true);
	}
	
	public void keyTyped(KeyEvent e)
	{
	    m_strBaseFile = jTextFieldBase.getText();
	    m_strYourFile = jTextFieldYour.getText();
	    m_strTheirFile = jTextFieldTheir.getText();
	    EnableOkBtn();
	}
	
	public void keyPressed(KeyEvent e)
	{
	    m_strBaseFile = jTextFieldBase.getText();
	    m_strYourFile = jTextFieldYour.getText();
	    m_strTheirFile = jTextFieldTheir.getText();
	    EnableOkBtn();   
	}
	
	public void keyReleased(KeyEvent e)
	{
	    m_strBaseFile = jTextFieldBase.getText();
	    m_strYourFile = jTextFieldYour.getText();
	    m_strTheirFile = jTextFieldTheir.getText();
	    EnableOkBtn();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if ("Base Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strBaseFile = file.getPath();
	            jTextFieldBase.setText(m_strBaseFile);
	        }
	        EnableOkBtn();
	    }
	    else if ("Their Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strTheirFile = file.getPath();
	            jTextFieldTheir.setText(m_strTheirFile);
	        }
	        EnableOkBtn();
	    }	
	    else if ("Your Browse".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        openFileChoose();
	        int iRet = fc.showOpenDialog(this);
	        
	        if (iRet == JFileChooser.APPROVE_OPTION)
	        {
	            File file = fc.getSelectedFile();
	            m_strYourFile = file.getPath();
	            jTextFieldYour.setText(m_strYourFile);
	        }
	        EnableOkBtn();
	    }	
	    else if ("Ok".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        if (!EnableOkBtn())
	        {
	            return;
	        }
	        else if (!isValidFile(m_strBaseFile) || !isValidFile(m_strYourFile) ||
	                !isValidFile(m_strTheirFile))
	        {
	            return;
	        }
	        else if (m_strBaseFile.compareToIgnoreCase(m_strYourFile) == 0 ||
	                m_strBaseFile.compareToIgnoreCase(m_strTheirFile) == 0 ||
	                m_strTheirFile.compareToIgnoreCase(m_strYourFile) == 0)
	        {
	            JOptionPane.showMessageDialog(this, "The same file is selected.", 
		                this.getTitle(), JOptionPane.ERROR_MESSAGE);        
	            return;
	        }
	        else
	        {
	            MergeHeroApp.theApp.m_aryFiles.clear();
	            MergeHeroApp.theApp.m_aryFiles.add(m_strBaseFile);
	            MergeHeroApp.theApp.m_aryFiles.add(m_strTheirFile);
	            MergeHeroApp.theApp.m_aryFiles.add(m_strYourFile);
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
	    m_strBaseFile = jTextFieldBase.getText();
	    m_strYourFile = jTextFieldYour.getText();
	    m_strTheirFile = jTextFieldTheir.getText();
	    m_strBaseFile.trim();
	    m_strYourFile.trim();
	    m_strTheirFile.trim();
        
        if (m_strBaseFile.length() == 0 || m_strYourFile.length() == 0 ||
                m_strTheirFile.length() == 0)
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

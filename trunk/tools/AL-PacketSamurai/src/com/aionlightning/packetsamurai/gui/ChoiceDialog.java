/**
 * 
 */
package com.aionlightning.packetsamurai.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.aionlightning.packetsamurai.PacketSamurai;


/**
 * @author Ulysses R. Ribeiro
 *
 */
@SuppressWarnings("serial")
public class ChoiceDialog extends JDialog
{
    private int[] _selection;
    private CountDownLatch _latch = new CountDownLatch(1);
    
    public ChoiceDialog(String title, String[] choicetitles, String[][] choices)
    {
        if (choices.length != choicetitles.length)
        {
            throw new IllegalArgumentException("The array of choices should match the options(titles) number.");
        }
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        this.setSize(400,300);
        this.setAlwaysOnTop(true);
        this.addWindowListener(new ChoiceWindowListener());
        
        _selection = new int[choices.length];
        ChoiceActionListener cal = new ChoiceActionListener();
        
        ButtonGroup[] groups = new ButtonGroup[choices.length];
        JPanel[] radioPanels = new JPanel[choices.length];
        JTabbedPane tabbedPane = new JTabbedPane();
        
        GridBagConstraints cons = new GridBagConstraints();
        cons.anchor = GridBagConstraints.NORTHWEST;
        //cons.fill = GridBagConstraints.BOTH;
        //cons.weightx = 1.0;
        //cons.weighty = 1.0;
        for (int i = 0; i < choices.length; i++)
        {
            groups[i] = new ButtonGroup();
            radioPanels[i] = new JPanel(new GridBagLayout());
            
            cons.gridy = 0;
            for (int j = 0; j < choices[i].length; j++)
            {
                JRadioButton rb = new JRadioButton(choices[i][j]);
                rb.setActionCommand(i+" "+j);
                rb.addActionListener(cal);
                radioPanels[i].add(rb, cons);
                groups[i].add(rb);
                if (j == 0)
                {
                    groups[i].setSelected(rb.getModel(), true);
                }
                cons.gridy++;
            }
            
            tabbedPane.add(new JScrollPane(radioPanels[i]), choicetitles[i]);
        }
        
        this.add(tabbedPane, BorderLayout.CENTER);
        
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand("ok");
        okButton.addActionListener(cal);
        this.add(okButton, BorderLayout.PAGE_END);
    }
    
    private void setSelection(int index, int selected)
    {
        _selection[index] = selected;
    }
    
    public void setSelection(int[] selection)
    {
        _selection = selection;
    }
    
    public int[] getSelection()
    {
        return _selection;
    }
    
    public CountDownLatch getLatch()
    {
        return _latch;
    }
    
    public class ChoiceActionListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("ok"))
            {
                ChoiceDialog.this.getLatch().countDown();
                ChoiceDialog.this.dispose();
            }
            else
            {
                String[] parts = e.getActionCommand().split(" ");
                ChoiceDialog.this.setSelection(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
        
    }
    
    public class ChoiceWindowListener implements WindowListener
    {

        public void windowActivated(WindowEvent e)
        {
            
        }

        public void windowClosed(WindowEvent e)
        {
            
        }

        public void windowClosing(WindowEvent e)
        {
            ChoiceDialog.this.setSelection(null);
            ChoiceDialog.this.getLatch().countDown();
            e.getWindow().dispose();
        }

        public void windowDeactivated(WindowEvent e)
        {
            
        }

        public void windowDeiconified(WindowEvent e)
        {
            
        }

        public void windowIconified(WindowEvent e)
        {
            
        }

        public void windowOpened(WindowEvent e)
        {
            
        }
        
    }
    
    public static int[] choiceDialog(String title, String[] choicetitles, String[][] choices)
    {
        ChoiceDialog cd = new ChoiceDialog(title, choicetitles, choices);
        
        if (PacketSamurai.getUserInterface() instanceof Main)
            cd.setLocationRelativeTo(((Main) PacketSamurai.getUserInterface()).getMainFrame());
        
        cd.setVisible(true);
        try
        {
            cd.getLatch().await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return null;
        }
        return cd.getSelection();
    }
}

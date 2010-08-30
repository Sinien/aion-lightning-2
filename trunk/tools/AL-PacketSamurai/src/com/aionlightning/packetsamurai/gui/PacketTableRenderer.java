/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.aionlightning.packetsamurai.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author Ulysses R. Ribeiro
 *
 */
@SuppressWarnings("serial")
public class PacketTableRenderer extends DefaultTableCellRenderer implements TableCellRenderer
{
    private TooltipTable _table;
    
    public PacketTableRenderer(TooltipTable table)
    {
        _table = table;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
    {
        Component c;
        if (value instanceof Component)
        {
            c = (Component) value;
            if (isSelected)
            {
                c.setForeground(table.getSelectionForeground());
                c.setBackground(table.getSelectionBackground());
            }
        }
        else
        {
            c =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        }
        
        if (c instanceof JComponent)
        {
            JComponent jc = (JComponent) c;
            jc.setToolTipText(_table.getToolTip(row, col));
        }
        
        if (_table.getIsMarked(row) && !isSelected)
        {
            c.setBackground(Color.YELLOW);
        }
        else if (!isSelected)
        {
            c.setBackground(table.getBackground());
        }
        
        return c;
    }

    public interface TooltipTable
    {
        public String getToolTip(int row, int col);
        
        public boolean getIsMarked(int row);
    }
}

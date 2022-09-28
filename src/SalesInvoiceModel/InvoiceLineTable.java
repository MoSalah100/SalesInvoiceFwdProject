/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SalesInvoiceModel;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author DELL
 */
public class InvoiceLineTable extends AbstractTableModel {

    private String[] cols = {"Item", "Unit Price", "Count", "Total"};
    private List<InvoiceLine> lines;

    public InvoiceLineTable(List<InvoiceLine> lines) {
        this.lines = lines;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }
    
    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InvoiceLine line = lines.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return line.getItem();
            case 1: return line.getPrice();
            case 2: return line.getCount();
            case 3: return line.getTotal();
        }
        return "";
    }
    
}

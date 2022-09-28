/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SalesInvoiceController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import SalesInvoiceModel.InvoiceHeader;
import SalesInvoiceModel.InvoiceTable;
import SalesInvoiceModel.InvoiceLine;
import SalesInvoiceModel.InvoiceLineTable;
import SalesInvoiceView.CreateInvoiceDialog;
import SalesInvoiceView.CreateInvoiceLineDialog;
import SalesInvoiceView.SalesInvoiceStructure;

/**
 *
 * @author DELL
 */
public class SalesInvoiceController implements ActionListener, ListSelectionListener {

    private SalesInvoiceStructure frame;
    private CreateInvoiceDialog headerDialog;
    private CreateInvoiceLineDialog lineDialog;

    public SalesInvoiceController(SalesInvoiceStructure frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("ActionListener");

        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case "New Invoice":
                newInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "New Line":
                newLine();
                break;
            case "Delete Line":
                deleteLine();
                break;
            case "Load Files":
                loadFiles(null, null);
                break;
            case "Save Data":
                saveData();
                break;
            case "newInvoiceOK":
                newInvoiceOK();
                break;
            case "newInvoiceCancel":
                newInvoiceCancel();
                break;
            case "newLineOK":
                newLineOK();
                break;
            case "newLineCancel":
                newLineCancel();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        System.out.println("ListSelectionListener");

        int row = frame.getInvoicesTable().getSelectedRow();
        System.out.println("Selected Row: " + row);
        if (row > -1 && row < frame.getInvoices().size()) {
            InvoiceTable inv = frame.getInvoices().get(row);
            frame.getCustNameLabel().setText(inv.getCustomer());
            frame.getInvDateLabel().setText(frame.sdf.format(inv.getDate()));
            frame.getInvNumLabel().setText("" + inv.getNum());
            frame.getInvTotalLabel().setText("" + inv.getTotal());

            List<InvoiceLine> lines = inv.getLines();
            frame.getLinesTable().setModel(new InvoiceLineTable(lines));
        } else {
            frame.getCustNameLabel().setText("");
            frame.getInvDateLabel().setText("");
            frame.getInvNumLabel().setText("");
            frame.getInvTotalLabel().setText("");

            frame.getLinesTable().setModel(new InvoiceLineTable(new ArrayList<InvoiceLine>()));
        }

    }

    private void newInvoice() {
        headerDialog = new CreateInvoiceDialog(frame);
        headerDialog.setVisible(true);
    }

    private void deleteInvoice() {
        int row = frame.getInvoicesTable().getSelectedRow();
        if (row != -1) {
            frame.getInvoices().remove(row);
            ((InvoiceHeader) frame.getInvoicesTable().getModel()).fireTableDataChanged();
        }
    }

    private void newLine() {
        int selectedInv = frame.getInvoicesTable().getSelectedRow();
        if (selectedInv != -1) {
            lineDialog = new CreateInvoiceLineDialog(frame);
            lineDialog.setVisible(true);
        }
    }

    private void deleteLine() {
        int row = frame.getLinesTable().getSelectedRow();
        if (row != -1) {
            int headerRow = frame.getInvoicesTable().getSelectedRow();
            InvoiceLineTable lineTableModel = (InvoiceLineTable) frame.getLinesTable().getModel();
            lineTableModel.getLines().remove(row);
            lineTableModel.fireTableDataChanged();
            ((InvoiceHeader) frame.getInvoicesTable().getModel()).fireTableDataChanged();
            frame.getInvoicesTable().setRowSelectionInterval(headerRow, headerRow);
        }
    }

    public void loadFiles(String headrPath, String linePath) {
        File headerFile = null;
        File lineFile = null;
        if (headrPath == null && linePath == null) {
            JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                headerFile = fc.getSelectedFile();
                result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    lineFile = fc.getSelectedFile();
                }
            }
        } else {
            headerFile = new File(headrPath);
            lineFile = new File(linePath);
        }

        if (headerFile != null && lineFile != null) {
            try {
                /*
                1,22-11-2020,Ali
                2,13-10-2021,Saleh
                 */
                // collection streams
                List<String> headerLines = Files.lines(Paths.get(headerFile.getAbsolutePath())).collect(Collectors.toList());
                /*
                1,Mobile,3200,1
                1,Cover,20,2
                1,Headphone,130,1	
                2,Laptop,9000,1
                2,Mouse,135,1
                 */
                List<String> lineLines = Files.lines(Paths.get(lineFile.getAbsolutePath())).collect(Collectors.toList());
                //ArrayList<Invoice> invoices = new ArrayList<>();
                frame.getInvoices().clear();
                for (String headerLine : headerLines) {
                    String[] parts = headerLine.split(",");  // "1,22-11-2020,Ali"  ==>  ["1", "22-11-2020", "Ali"]
                    String numString = parts[0];
                    String dateString = parts[1];
                    String name = parts[2];
                    int num = Integer.parseInt(numString);
                    Date date = SalesInvoiceStructure.sdf.parse(dateString);
                    InvoiceTable inv = new InvoiceTable(num, name, date);
                    //invoices.add(inv);
                    frame.getInvoices().add(inv);
                }
                System.out.println("Check point");
                for (String lineLine : lineLines) {
                    // lineLine = "1,Mobile,3200,1"
                    String[] parts = lineLine.split(",");
                    /*
                    parts = ["1", "Mobile", "3200", "1"]
                     */
                    int num = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int count = Integer.parseInt(parts[3]);
                    InvoiceTable inv = getInvoiceByNum(num);
                    InvoiceLine line = new InvoiceLine(name, price, count, inv);
                    inv.getLines().add(line);
                }
                System.out.println("Check point");
                frame.getInvoicesTable().setModel(new InvoiceHeader(frame.getInvoices()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private InvoiceTable getInvoiceByNum(int num) {
        for (InvoiceTable inv : frame.getInvoices()) {
            if (num == inv.getNum()) {
                return inv;
            }
        }
        return null;
    }

    private void saveData() {
        String invoicesData = "";
        String linesData = "";
        for (InvoiceTable invoice : frame.getInvoices()) {
            invoicesData += invoice.toCSV();
            invoicesData += "\n";
            for (InvoiceLine line : invoice.getLines()) {
                linesData += line.toCSV();
                linesData += "\n";
            }
        }

        JFileChooser fc = new JFileChooser();
        int result = fc.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File headerFile = fc.getSelectedFile();
            result = fc.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File lineFile = fc.getSelectedFile();
                try {
                    FileWriter headerFW = new FileWriter(headerFile);
                    headerFW.write(invoicesData);
                    headerFW.flush();
                    headerFW.close();

                    FileWriter lineFW = new FileWriter(lineFile);
                    lineFW.write(linesData);
                    lineFW.flush();
                    lineFW.close();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error while saving data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void newInvoiceOK() {
        String customer = headerDialog.getCustNameField().getText();
        String date = headerDialog.getInvDateField().getText();
        headerDialog.setVisible(false);
        headerDialog.dispose();
        int num = getNextInvoiceNum();
        try {
            Date invDate = frame.sdf.parse(date);
            InvoiceTable inv = new InvoiceTable(num, customer, invDate);
            frame.getInvoices().add(inv);
            ((InvoiceHeader) frame.getInvoicesTable().getModel()).fireTableDataChanged();
        } catch (ParseException pex) {
            JOptionPane.showMessageDialog(frame, "Error in date format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getNextInvoiceNum() {
        int num = 1;
        for (InvoiceTable inv : frame.getInvoices()) {
            if (inv.getNum() > num) {
                num = inv.getNum();
            }
        }
        return num + 1;
    }

    private void newInvoiceCancel() {
        headerDialog.setVisible(false);
        headerDialog.dispose();
    }

    private void newLineOK() {
        int selectedInv = frame.getInvoicesTable().getSelectedRow();
        if (selectedInv != -1) {
            InvoiceTable inv = frame.getInvoices().get(selectedInv);
            String name = lineDialog.getItemNameField().getText();
            String priceStr = lineDialog.getItemPriceField().getText();
            String countStr = lineDialog.getItemCountField().getText();
            lineDialog.setVisible(false);
            lineDialog.dispose();
            double price = Double.parseDouble(priceStr);
            int count = Integer.parseInt(countStr);
            InvoiceLine line = new InvoiceLine(name, price, count, inv);
            inv.getLines().add(line);
            ((InvoiceLineTable) frame.getLinesTable().getModel()).fireTableDataChanged();
            ((InvoiceHeader) frame.getInvoicesTable().getModel()).fireTableDataChanged();
            frame.getInvoicesTable().setRowSelectionInterval(selectedInv, selectedInv);
        }
    }

    private void newLineCancel() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
    }

}

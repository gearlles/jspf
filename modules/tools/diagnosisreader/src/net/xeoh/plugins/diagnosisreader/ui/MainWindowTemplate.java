/*
 * Created by JFormDesigner on Wed Mar 30 12:26:50 CEST 2011
 */

package net.xeoh.plugins.diagnosisreader.ui;

import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Ralf Biedert
 */
public class MainWindowTemplate extends JFrame {
    /** */
    private static final long serialVersionUID = -1517673471088767556L;
    
    /** */
    public MainWindowTemplate() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        this.dropPanel = new DropPanel();
        this.converter = new JComboBox();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setTitle("Diagnosis Converter");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "pref, $rgap, center:pref:grow, $lcgap, pref",
            "pref:grow, $lgap, default"));
        contentPane.add(this.dropPanel, cc.xywh(1, 1, 5, 2));
        contentPane.add(this.converter, cc.xy(3, 3));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    protected DropPanel dropPanel;
    protected JComboBox converter;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}

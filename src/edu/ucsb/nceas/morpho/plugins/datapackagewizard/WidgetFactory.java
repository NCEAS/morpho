package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

public class WidgetFactory {
  
  private static boolean debug = false;
  
  private WidgetFactory() {}
   
  public static JTextArea makeTextArea( String text, 
                                        int numberOfRows, boolean isEditable) {
    
    JTextArea area = new JTextArea(text);
    area.setRows(numberOfRows);
    area.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    area.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    area.setEditable(isEditable);
    area.setOpaque(isEditable);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    return area;
  }
  

  private static final StringBuffer buff = new StringBuffer();
  
  public static JLabel makeHTMLLabel(String text, int numberOfLines) {
  
    buff.delete(0, buff.length());
    buff.append("<html><table width=\"95%\"><tr><td valign=\"top\">");
    buff.append(text);
    buff.append("</td></tr></table></html>");
    
    return makeLabel( buff.toString(), false, 
                      getDimForNumberOfLines(numberOfLines));
  
  }


  public static JLabel makeLabel(String text, boolean hiliteRequired) {

    return makeLabel( text, hiliteRequired, 
                      WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
  }
  
  public static JLabel makeLabel( String text, 
                                  boolean hiliteRequired, Dimension dims) {
    
    JLabel label = new JLabel(text);
    
    setPrefMaxSizes(label, dims);
    label.setMinimumSize(dims);
    label.setAlignmentX(SwingConstants.LEFT);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    
    label.setBorder(BorderFactory.createMatteBorder(1,3,1,3, (Color)null));
    if (hiliteRequired) {
      label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    } else { 
      label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    }
    
    if (debug) {
      label.setBackground(java.awt.Color.blue);
      label.setOpaque(true);
    }
    return label;
  }
  
  
  public static JTextField makeOneLineTextField() {
    
    return makeOneLineTextField("");
  }

  public static JTextField makeOneLineTextField(String initialValue) {
    
    JTextField field = new JTextField();
    setPrefMaxSizes(field, WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    field.setText(initialValue);
    field.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    field.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    return field;
  }
  
  
  public static JPanel makePanel() {
  
    return makePanel(-1);
  }
  
  public static JPanel makePanel(int numberOfLines) {
  
    JPanel panel = new JPanel();
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
  
    if (numberOfLines>0) {
    
      setPrefMaxSizes(panel, getDimForNumberOfLines(numberOfLines));
    }
    
    if (debug) {
      panel.setBackground(java.awt.Color.green);
      panel.setOpaque(true);
    }
    
    return panel;
  }
  
  
  public static Component makeDefaultSpacer() {
    
    return Box.createRigidArea(WizardSettings.DEFAULT_SPACER_DIMS);
  }

  public static void hiliteComponent(JComponent component) {
  
    component.setOpaque(true);
    component.setBackground(
                    WizardSettings.WIZARD_CONTENT_HILITE_BG_COLOR);
    component.setForeground(
                    WizardSettings.WIZARD_CONTENT_HILITE_FG_COLOR);
  }
  
  public static void unhiliteComponent(JComponent component) {
  
    component.setOpaque(false);
    component.setForeground(
                    WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    component.setBackground(null);
  }
  
  
  
  // ***************************************************************************

  private static Dimension getDimForNumberOfLines(int numberOfLines) {
  
    double width = WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.getWidth();
    double height = numberOfLines 
                  * WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.getHeight();

    return new Dimension((int)width, (int)height);
  }  
  
  
  private static void setPrefMaxSizes(JComponent component, Dimension dims) {
  
    component.setPreferredSize(dims);
    component.setMaximumSize(dims);
  }
}
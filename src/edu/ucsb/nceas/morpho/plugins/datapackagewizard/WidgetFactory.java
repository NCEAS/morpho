package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class WidgetFactory {
  
  private WidgetFactory() {}
   
  public static JTextArea makeMultilineTextArea(String text, boolean isEditable) {
    
    JTextArea label = new JTextArea(text);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    if (!isEditable) {
      label.setOpaque(false);
      label.setEditable(false);
    }
    label.setLineWrap(true);
    label.setWrapStyleWord(true);
    return label;
  }
  
  public static JLabel makeLabel(String text, boolean hiliteRequired) {
  
    JLabel label = new JLabel(text);
    label.setMinimumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    label.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    label.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    if (hiliteRequired) {
      label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    } else { 
      label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    }
    label.setOpaque(false);
    return label;
  }
  
  
  public static JTextField makeOneLineTextField() {
    
    return makeOneLineTextField("");
  }

  public static JTextField makeOneLineTextField(String initialValue) {
    
    JTextField field = new JTextField();
    field.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    field.setText(initialValue);
    field.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    field.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    return field;
  }
    
}
package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class WidgetFactory {

  private static boolean debugHilite = false;

  private WidgetFactory() {}

  public static JTextArea makeTextArea( String text,
                                        int numberOfRows, boolean isEditable) {

    if (text==null) text="";
    JTextArea area = new JTextArea(text);
    area.setRows(numberOfRows);
    area.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    area.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    area.setEditable(isEditable);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    return area;
  }


  private static final StringBuffer buff = new StringBuffer();

  public static JLabel makeHTMLLabel(String text, int numberOfLines) {

    return makeHTMLLabel(text, numberOfLines, false);
  }

  public static JLabel makeHTMLLabel(String text, int numberOfLines, boolean hiliteRequired) {

    if (text==null) text="";
    buff.delete(0, buff.length());
    buff.append(WizardSettings.HTML_TABLE_LABEL_OPENING);
    buff.append(text);
    buff.append(WizardSettings.HTML_TABLE_LABEL_CLOSING);

    return makeLabel( buff.toString(), hiliteRequired,
                      getDimForNumberOfLines(numberOfLines));

  }



  public static JLabel makeLabel(String text, boolean hiliteRequired) {

    return makeLabel( text, hiliteRequired,
                      WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
  }

  public static JLabel makeLabel( String text,
                                  boolean hiliteRequired, Dimension dims) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    setPrefMaxSizes(label, dims);
    label.setMinimumSize(dims);
    label.setAlignmentX(SwingConstants.LEADING);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);

    label.setBorder(BorderFactory.createMatteBorder(1,3,1,3, (Color)null));
    if (hiliteRequired) {
      label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    } else {
      label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    }

    if (debugHilite) {
      label.setBackground(java.awt.Color.blue);
      label.setOpaque(true);
    }
    return label;
  }

  public static JButton makeJButton(String title, ActionListener actionListener) {
    return makeJButton(title, actionListener, WizardSettings.LIST_BUTTON_DIMS);
  }

  public static JButton makeJButton(String title, ActionListener actionListener,
                                                              Dimension dims) {
    if (title==null) title = "";
    JButton button = new JButton(title);
    button.setForeground(WizardSettings.BUTTON_TEXT_COLOR);
    button.setFont(WizardSettings.BUTTON_FONT);
    if (actionListener!=null) button.addActionListener(actionListener);
    setPrefMaxSizes(button, dims);
    return button;
  }


  public static JTextField makeOneLineTextField() {

    return makeOneLineTextField("");
  }

  public static JTextField makeOneLineTextField(String initialValue) {

    if (initialValue==null) initialValue="";
    JTextField field = new JTextField();
    setPrefMaxSizes(field, WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    field.setText(initialValue);
    field.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    field.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    return field;
  }

  public static void addTitledBorder(JComponent component, String title) {

    if (title==null) title="";
    component.setBorder(BorderFactory.createTitledBorder(
              BorderFactory.createLineBorder(
                                  WizardSettings.WIZARD_CONTENT_TEXT_COLOR, 1),
              title,
              0,
              0,
              WizardSettings.WIZARD_CONTENT_BOLD_FONT,
              WizardSettings.WIZARD_CONTENT_TEXT_COLOR));
  }


  public static JPanel makeVerticalPanel(int numberOfLines) {

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    if (numberOfLines>0) {
      setPrefMaxSizes(panel, getDimForNumberOfLines(numberOfLines));
    }

    if (debugHilite) {
      panel.setBackground(java.awt.Color.green);
      panel.setOpaque(true);
    }

    return panel;
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

    if (debugHilite) {
      panel.setBackground(java.awt.Color.green);
      panel.setOpaque(true);
    }

    return panel;
  }


  public static Component makeDefaultSpacer() {

    return Box.createRigidArea(WizardSettings.DEFAULT_SPACER_DIMS);
  }


  public static Component makeHalfSpacer() {

    return Box.createRigidArea(new Dimension(
                    WizardSettings.DEFAULT_SPACER_DIMS.width/2,
                    WizardSettings.DEFAULT_SPACER_DIMS.height/2));
  }


  public static void hiliteComponent(JComponent component) {

    if (component==null) return;
    component.setOpaque(true);
    component.setBackground(
                    WizardSettings.WIZARD_CONTENT_HILITE_BG_COLOR);
    component.setForeground(
                    WizardSettings.WIZARD_CONTENT_HILITE_FG_COLOR);
  }

  public static void unhiliteComponent(JComponent component) {

    if (component==null) return;
    component.setOpaque(false);
    component.setForeground(
                    WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    component.setBackground(null);
  }



  public static JPanel makeRadioPanel(String[] buttonsText,
                                  int selectedIndex, ActionListener listener) {

    if (buttonsText==null) buttonsText = new String[] { "" };

    JPanel outerRadioPanel = new JPanel(new BorderLayout());

    JPanel radioPanel = new JPanel(new GridLayout(0, 1));

    int totalButtons = buttonsText.length;

    JRadioButton[] buttons = new JRadioButton[totalButtons];
    ButtonGroup group = new ButtonGroup();
    //adding an invisible button as the first in the container, and the
    //buttongroup, since once any button has been clicked, the group cannot
    //be reset so none are selected, other than by "clicking" (programmatically)
    //on this hidden jbutton. Note that the existence of this feature is
    //transparent to client code.
    JRadioButton dummyJButton = new JRadioButton("");
    Dimension hiddenDims = new Dimension(10,1);
    dummyJButton.setMaximumSize(hiddenDims);
    dummyJButton.setPreferredSize(hiddenDims);
    dummyJButton.setVisible(false);
    group.add(dummyJButton);
    outerRadioPanel.add(dummyJButton, BorderLayout.NORTH);

    for (int i=0; i<totalButtons; i++) {

      buttons[i] = new JRadioButton(buttonsText[i]);
      buttons[i].setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttons[i].setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
      buttons[i].setActionCommand(buttonsText[i]);
      if (i==selectedIndex) buttons[i].setSelected(true);
      group.add(buttons[i]);
      buttons[i].addActionListener(listener);
      radioPanel.add(buttons[i]);
    }
    setPrefMaxSizes(radioPanel, getDimForNumberOfLines(5*totalButtons/4));
    outerRadioPanel.add(radioPanel, BorderLayout.CENTER);

    return outerRadioPanel;
  }


  public static JPanel makeCheckBoxPanel(String[] boxesText,
                                  int selectedIndex, ItemListener listener) {

    if (boxesText==null) boxesText = new String[] { "" };
    JPanel cbPanel = new JPanel(new GridLayout(0, 1));

    int totalBoxes = boxesText.length;

    JCheckBox[] boxes = new JCheckBox[totalBoxes];

    for (int i=0; i<totalBoxes; i++) {

      boxes[i] = new JCheckBox(boxesText[i]);
      boxes[i].setFont(WizardSettings.WIZARD_CONTENT_FONT);
      boxes[i].setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
      boxes[i].setActionCommand(boxesText[i]);
      if (i==selectedIndex) boxes[i].setSelected(true);
      boxes[i].addItemListener(listener);
      cbPanel.add(boxes[i]);
    }
    setPrefMaxSizes(cbPanel, getDimForNumberOfLines(5*totalBoxes/4));
    return cbPanel;
  }


  public static JCheckBox makeCheckBox(String labelStr, boolean hiliteRequired) {

    JCheckBox cb = new JCheckBox(labelStr);
    if (hiliteRequired) {
      cb.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    } else {
      cb.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    }
    cb.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    return cb;
  };


  public static JComboBox makePickList(String[] listValues, boolean isEditable,
                                    int selectedIndex, ItemListener listener) {

    if (listValues==null) listValues = new String[] { "" };

    JComboBox comboBox = new JComboBox(listValues);

    comboBox.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    comboBox.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    comboBox.addItemListener(listener);
    comboBox.setEditable(isEditable);
    if (selectedIndex > -1 && selectedIndex < comboBox.getItemCount()) {

      comboBox.setSelectedIndex(selectedIndex);
    }

    setPrefMaxSizes(comboBox, WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    return comboBox;
  }



  public static CustomList makeList(String[] colNames,
                                    Object[] colTemplates,
                                    int displayRows,
                                    boolean showAddButton,
                                    boolean showEditButton,
                                    boolean showDuplicateButton,
                                    boolean showDeleteButton,
                                    boolean showMoveUpButton,
                                    boolean showMoveDownButton) {

    return new CustomList(colNames, colTemplates, displayRows, showAddButton,
        showEditButton, showDuplicateButton, showDeleteButton, showMoveUpButton,
                                                            showMoveDownButton);
  }


  // ***************************************************************************

  private static Dimension getDimForNumberOfLines(int numberOfLines) {

    double width = WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.getWidth();
    double height = numberOfLines
                  * WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.getHeight();

    return new Dimension((int)width, (int)height);
  }


  public static void setPrefMaxSizes(JComponent component, Dimension dims) {

    component.setPreferredSize(dims);
    component.setMaximumSize(dims);
  }


  /**
    Function that creates a container dialog for a given panel. The container provides
    the 'OK' and 'Cancel' buttons at the bottom of the container . The listeners for the
    'OK' and 'Cancel' button are provided as parameters.

    @param centerPanel - the JPanel that is to displayed in this dialog
    @param okListener  - the ActionListener for the 'OK' button
    @param cancelListener  - the ActionListener for the 'Cancel' button
    @return JDialog 	- returns a JDialog that contains the centerPanel and a button panel
                        at the bottom
  */

  public static JDialog makeContainerDialog(JPanel centerPanel, ActionListener okListener, ActionListener cancelListener) {

    return makeContainerDialog(centerPanel, okListener, cancelListener, "OK", "Cancel");
  }

  /**
    Function that creates a container dialog for a given panel. The container provides
    the 'OK' and 'Cancel' buttons at the bottom of the container . The listeners for the
    'OK' and 'Cancel' button are provided as parameters. The labels for the buttons are
    also provided.

    @param centerPanel - the JPanel that is to displayed in this dialog
    @param okListener  - the ActionListener for the 'OK' button
    @param cancelListener  - the ActionListener for the 'Cancel' button
    @param okCaption  - the label for the 'OK' button
    @param cancelCaption  - the label for the 'Cancel' button
    @return JDialog 	- returns a JDialog that contains the centerPanel and a button panel
                        at the bottom
  */

  public static JDialog makeContainerDialog(JPanel centerPanel, ActionListener okListener, ActionListener cancelListener, String okCaption, String cancelCaption)
  {

    JDialog dialog = new JDialog();
    Container c = dialog.getContentPane();
    c.setLayout(new BorderLayout());

    dialog.setModal(true);
    dialog.setVisible(false);

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    buttonsPanel.add(Box.createHorizontalGlue());
    buttonsPanel.setOpaque(false);

    buttonsPanel.setBorder(
    BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));
    c.add(buttonsPanel, BorderLayout.SOUTH);

    JButton okButton  = new JButton(okCaption);
    okButton.addActionListener(okListener);
    okButton.setForeground(WizardSettings.BUTTON_TEXT_COLOR);
    okButton.setFont(WizardSettings.BUTTON_FONT);

    JButton cancelButton = new JButton(cancelCaption);
    cancelButton.addActionListener(cancelListener);
    cancelButton.setForeground(WizardSettings.BUTTON_TEXT_COLOR);
    cancelButton.setFont(WizardSettings.BUTTON_FONT);

    buttonsPanel.add(okButton);
    buttonsPanel.add(Box.createHorizontalStrut(WizardSettings.PADDING));
    buttonsPanel.add(cancelButton);
    buttonsPanel.add(Box.createHorizontalStrut(WizardSettings.PADDING));

    c.add(centerPanel, BorderLayout.CENTER);
    return dialog;
  }

}


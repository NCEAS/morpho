package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class HelpDialog extends JDialog {

	private final Color TOP_PANEL_BG_COLOR = new Color(11, 85, 112);

	private final Font TITLE_FONT = new Font("Sans-Serif", Font.BOLD, 13);

	private final Color TITLE_TEXT_COLOR = new Color(255, 255, 255);

	private final Dimension TOP_PANEL_DIMS = new Dimension(100, 40);
	
	public static final Dimension HELP_DIALOG_SIZE = new Dimension(400, 500);

	private String helpText = null;
	
	private String titleText = null;

	public HelpDialog(Frame owner, String titleText, String bodyText) {
		super(owner);
		this.helpText = bodyText;
		this.titleText = titleText;
		init();
	}
	public HelpDialog(Dialog owner, String titleText, String bodyText) {
		super(owner);
		this.helpText = bodyText;
		this.titleText = titleText;
		init();
	}

	void init() {

		setTitle("Help");
		setModal(true);
		setVisible(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		JLabel titleLabel = new JLabel(titleText);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TITLE_TEXT_COLOR);
		titleLabel.setBorder(new EmptyBorder(WizardSettings.PADDING, 0,
				WizardSettings.PADDING, 0));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setPreferredSize(TOP_PANEL_DIMS);
		topPanel.setBorder(new EmptyBorder(0, 2 * WizardSettings.PADDING, 0,
				2 * WizardSettings.PADDING));
		topPanel.setBackground(TOP_PANEL_BG_COLOR);
		topPanel.setOpaque(true);
		topPanel.add(titleLabel);

		contentPane.add(topPanel, BorderLayout.NORTH);

		JEditorPane editor = new JEditorPane();
		editor.setEditable(false);
		editor.setContentType("text/html");
		editor.setText(helpText);
		editor.setCaretPosition(0);

		contentPane.add(new JScrollPane(editor,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);
	}
}

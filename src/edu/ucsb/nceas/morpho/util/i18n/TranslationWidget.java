package edu.ucsb.nceas.morpho.util.i18n;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

public class TranslationWidget extends AbstractUIPage {
	
	private CustomList translations;
	
	private JButton launcher;
	
	private String[] columnNames = {
			Language.getInstance().getMessage("Value"),
			Language.getInstance().getMessage("Language")};

	private String rootXPath;
	
	private String pageID;
	private String pageNumber;
	private String subtitle;
	private String title = Language.getInstance().getMessage("Translations");

	// saves the initial data before editing
	private OrderedMap originalData;

	public JButton getButton() {
		
		final TranslationWidget instance = this;
		if (launcher == null) {
			launcher = 
				new JButton(
					new AbstractAction(
							Language.getInstance().getMessage("Translations")) {
				public void actionPerformed(ActionEvent e) {
					//show the widget in a dialog
					ModalDialog md = 
						new ModalDialog(
								instance,
								UIController.getInstance().getCurrentActiveWindow(),
								UISettings.POPUPDIALOG_WIDTH,
	                            UISettings.POPUPDIALOG_HEIGHT/2);
				}
			});
		}
		return launcher;
	}
	
	public TranslationWidget() {
		init();
	}
	
	private void init() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		translations = 
			WidgetFactory.makeList(
				columnNames, 
				new Object[] {new JTextField(), new JTextField()}, 
				5, true, false, false, true, true, true);
		
		JPanel tPanel = WidgetFactory.makePanel();
		tPanel.add(WidgetFactory.makeLabel(Language.getInstance().getMessage("Translations"), false));
		tPanel.add(translations);
				
//		JPanel helpPanel = WidgetFactory.makePanel();
//		helpPanel.add(WidgetFactory.makeLabel("Language format: xx-XX (i.e 'en-US')", false, null));
//		this.add(helpPanel);
		
		this.add(tPanel);


	}
	
	private void addTranslation(String value, String lang) {
		List<String> rowList = new ArrayList<String>();
		rowList.add(value);
		rowList.add(lang);
		try {
			translations.addRow(rowList);
		} catch (Exception e) {
			Log.debug(30, "Ignoring table model exception when adding a row: " + e.getMessage());
		}
	}
	
	public String getValueAt(int i) {
		List<String> rowList = (List<String>) translations.getListOfRowLists().get(i);
		return rowList.get(0);
	}
	
	public String getLangAt(int i) {
		List<String> rowList = (List<String>) translations.getListOfRowLists().get(i);
		return rowList.get(1);
	}
	
	public int getTranslationCount() {
		return translations.getRowCount();
	}

	@Override
	public String getNextPageID() {
		return nextPageID;
	}

	@Override
	public OrderedMap getPageData() {
		// pass through
		return getPageData(this.rootXPath);
	}

	@Override
	public OrderedMap getPageData(String rootXPath) {
		OrderedMap data = new OrderedMap();
		List<List<String>> rows = (List<List<String>>) translations.getListOfRowLists();
		for (int i = 0; i < getTranslationCount(); i++) {
			String value = this.getValueAt(i);
			String lang = this.getLangAt(i);
			String path = rootXPath + "/value" + "[" + (i+1) + "]";
			data.put(path, value);
			data.put(path + "/@xml:lang", lang);
		}
		return data;
	}

	@Override
	public String getPageID() {
		return pageID;
	}

	@Override
	public String getPageNumber() {
		return pageNumber;
	}

	@Override
	public String getSubtitle() {
		return subtitle;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean onAdvanceAction() {
		// get the current state of the translations
		this.originalData = this.getPageData();
		return true;
	}

	@Override
	public void onLoadAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRewindAction() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void cancelAction() {
		// return the page to the original state
		try {
			this.setPageData(originalData, rootXPath);
		} catch (Exception e) {
			Log.debug(5, e.getMessage());
			e.printStackTrace();
		}	
	}

	@Override
	public boolean setPageData(OrderedMap data, String rootXPath) {
		// save the original data
		this.originalData = new OrderedMap();
		this.originalData.putAll(data);
		
		// clear the list
		this.translations.removeAllRows();
		
		this.rootXPath = rootXPath;
		List<String> toDeleteList = new ArrayList<String>();
		Iterator<String> keys = data.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key.contains("value") && !key.contains("@xml:lang")) {
				String value = (String) data.get(key);
				// get lang attribute for this too
				String langXpath = key + "/@xml:lang";
				String lang = (String) data.get(langXpath);
				// add to translations list
				addTranslation(value, lang);
				// add to delete list
				toDeleteList.add(langXpath);
				toDeleteList.add(key);
			}
		}
		// remove the keys we find
		for (String key: toDeleteList) {
			data.remove(key);
		}
		
		return data.isEmpty();
	}
	
}

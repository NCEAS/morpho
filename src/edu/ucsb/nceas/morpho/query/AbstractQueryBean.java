package edu.ucsb.nceas.querybean;

import java.awt.*;
import javax.swing.JTabbedPane;

public abstract class AbstractQueryBean extends Container
{
    public void searchFor(String text) {}
    
    public void setUserName(String name) {}
    public void setPassWord(String ps) {}
	public void setEditor (edu.ucsb.nceas.metaedit.AbstractMdeBean mde) {}
	public void setTabbedPane(JTabbedPane jp) {}
	public void setSearchLocal(boolean sl) {}
	public void setSearchNetwork(boolean sn) {}
	public void setExpertMode(boolean ex) {}
}
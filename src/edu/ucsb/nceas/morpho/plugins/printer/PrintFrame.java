/**
*  '$RCSfile: PrintFrame.java,v $'
*  Copyright: 2000 Regents of the University of California and the
*              National Center for Ecological Analysis and Synthesis
*    Authors: @authors@
*    Release: @release@
*
*   '$Author: sambasiv $'
*     '$Date: 2003-12-12 20:24:05 $'
* '$Revision: 1.1 $'
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package edu.ucsb.nceas.morpho.plugins.printer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JButton;
import javax.swing.JComponent; 

import java.awt.print.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.text.View;
import javax.swing.text.BoxView;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Point2D;

import edu.ucsb.nceas.morpho.util.Log;


public class PrintFrame extends JFrame implements ActionListener, HyperlinkListener
{
	JEditorPane editor;
	JButton printButton;
	JButton pageSetupButton;
	JButton exitButton;
	JScrollPane scrollPane;
	PageFormat pageFormat = null;
	PrinterJob job = null;
	
	
	PrintFrame(String text, String contentType) {
		
		super();
		
		editor = new JEditorPane();
		scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		printButton = new JButton("Print");
		pageSetupButton = new JButton("Page Setup");
		exitButton = new JButton("Close");
		editor.setContentType(contentType);
		editor.setText(text);
		editor.setEditable(false);
		editor.addHyperlinkListener(this);
		
		pageSetupButton.addActionListener(this);
		printButton.addActionListener(this);
		exitButton.addActionListener(this);
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(pageSetupButton);
		panel.add(printButton);
		panel.add(exitButton);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		setSize(800,600);
		setVisible(true);
		
		job = PrinterJob.getPrinterJob();
		pageFormat = job.defaultPage();
	
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) 
	{
		Log.debug(50,"hyperlinkUpdate called in PrintPlugin; eventType=" + e.getEventType());
		Log.debug(50,"hyperlinks not supported in Print Window");
		/*if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
			Log.debug(50,"eventType=ACTIVATED; description="
			+e.getDescription()+"; url="+e.getURL());
			try {
				controller.display(e.getDescription());
			} catch (DocumentNotFoundException ex) {
				Log.debug(12,"HTMLPanel.hyperlinkUpdate(): "
				+"DocumentNotFoundException from HyperlinkEvent: "
				+ e.getEventType() + "; exception is: "+ex);
				ex.printStackTrace(System.err);
			}
		}*/
		
	}
	
	public void actionPerformed(ActionEvent ae) 
	{
		Object obj = ae.getSource();
		Dimension oldD = this.getSize();
		if(obj == printButton) 
		{
			job = PrinterJob.getPrinterJob();
			PrintableComponent pc = new PrintableComponent(editor, pageFormat);
			if(job.printDialog())
			{
				pc.scaleToFitX();
				job.setPageable(pc);
				try {
					job.print();
				}
				catch(PrinterException pe)
				{
					Log.debug(6, "Printer Exception - " + pe);
				}
			}
		}
		else if(obj == pageSetupButton)
		{
			pageFormat = job.pageDialog(pageFormat);
		}
		else if(obj == exitButton)
		{
			this.dispose();
		}
		
	} // end of actionPerformed
	
	
	
	// the Printable class
	
	public class PrintableComponent extends PageableComponent implements Printable {
		
		private double mScaleX;
		private double mScaleY;
		
		/**
		* The Swing component to print.
		*/
		private JComponent mComponent;
		
		/**
		* Create a Pageable that can print a Swing JComponent.
		*
		* @param c The swing JComponent to be printed.
		*
		* @param format The size of the pages over which
		* the componenent will be printed.
		*/
		public PrintableComponent(JComponent c, PageFormat format) {
			
			
			
			setPageFormat(format);
			setPrintable(this);
			setComponent(c);
			
			Rectangle componentBounds = c.getBounds(null);
			System.out.println("Bounds - " + componentBounds.width +";" + componentBounds.height);
			
			setSize(componentBounds.width, componentBounds.height);
			setScale(1, 1);
		}
		
		
		protected void setComponent(JComponent c) {
			mComponent = c;
		}
		protected void setScale(double scaleX, double scaleY) {
			mScaleX = scaleX;
			mScaleY = scaleY;
		}
		
		public void scaleToFit(boolean useSymmetricScaling) {
			PageFormat format = getPageFormat();
			Rectangle componentBounds = mComponent.getBounds(null);
			double scaleX = format.getImageableWidth() /componentBounds.width;
			double scaleY = format.getImageableHeight() /componentBounds.height;
			System.out.println("Scale: " + scaleX + " " + scaleY);
			if (scaleX < 1 || scaleY < 1) {
				if (useSymmetricScaling) {
					if (scaleX < scaleY) {
						scaleY = scaleX;
					} else {
						scaleX = scaleY;
					}
				}
				setSize( (float) (componentBounds.width * scaleX), (float) (componentBounds.height * scaleY) );
				setScale(scaleX, scaleY);
				
			}
		}
		
		public void scaleToFitX() {
			PageFormat format = getPageFormat();
			Rectangle componentBounds = mComponent.getBounds(null);
			double scaleX = format.getImageableWidth() /componentBounds.width;
			double scaleY = scaleX;
			if (scaleX < 1) {
				setSize( (float) format.getImageableWidth(),
				(float) (componentBounds.height * scaleY));
				setScale(scaleX, scaleY);
			}
		}
		
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
			
			System.out.println("in print function - ");
			Graphics2D g2 = (Graphics2D) graphics;
			g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			Rectangle componentBounds = mComponent.getBounds(null);
			g2.translate(-componentBounds.x, -componentBounds.y);
			g2.scale(mScaleX, mScaleY);
			boolean wasBuffered = mComponent.isDoubleBuffered();
			mComponent.paint(g2);
			mComponent.setDoubleBuffered(wasBuffered);
			return PAGE_EXISTS;
		}
		
	} // end of PrintableComponent class

	// the Pageable class
		
	public class PageableComponent implements Pageable {
		
		private int mNumPagesX;
		private int mNumPagesY;
		private int mNumPages;
		private Printable mPainter;
		private PageFormat mFormat;
		
		/**
		* Create a java.awt.Pageable that will print
		*  a canvas over as many pages as are needed.
		* *
		* @param width The width of the canvas.
		*
		* @param height The height of the canvas.
		*
		* @param painter The printable object that will drawn the contents
		*
		* @param format The PageFormat description of the pages 
		*/
		
		public PageableComponent(float width, float height, Printable painter, PageFormat format) {
			
			setPrintable(painter);
			setPageFormat(format);
			setSize(width, height);
			setPageFormat(PrinterJob.getPrinterJob().defaultPage());
		}
		
		protected PageableComponent() {
		}
		protected void setPrintable(Printable painter) {
			mPainter = painter;
		}
		
		protected void setPageFormat(PageFormat pageFormat) {
			mFormat = pageFormat;
		}
		
		protected void setSize(float width, float height) {
			mNumPagesX = (int) ((width + mFormat.getImageableWidth() - 1)/ mFormat.getImageableWidth());
			mNumPagesY = (int) ((height + mFormat.getImageableHeight() - 1)/ mFormat.getImageableHeight());
			mNumPages = mNumPagesX * mNumPagesY;
			Log.debug(20, "Number Of Pages for printing = " + mNumPages);
			
		}
		
		/**
		* Returns the number of pages over which the canvas
		* will be drawn.
		*/
		public int getNumberOfPages() {
			return mNumPages;
		}
		
		protected PageFormat getPageFormat() {
			return mFormat;
		}
		
		/** 
		* Returns the PageFormat of the page specified by
		* pageIndex. For a Pageable object the PageFormat
		* is the same for all pages.
		*
		* @param pageIndex the zero based index of the page whose
		* PageFormat is being requested
		* @return the PageFormat describing the size and
		* orientation.
		* @exception IndexOutOfBoundsException
		* the Pageable  does not contain the requested
		* page.
		*/
		
		public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		
			if (pageIndex >= mNumPages) {
				throw new IndexOutOfBoundsException();
			}
			return getPageFormat();
		}
		
		/**
		* Returns the <code>Printable</code> instance responsible for
		* rendering the page specified by <code>pageIndex</code>.
		* @param pageIndex the zero based index of the page whose
		* Printable is being requested
		* @return the Printable that renders the page.
		* @exception IndexOutOfBoundsException, when the Pageable does not contain the requested
		* page.
		*/
		
		public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
			
			if (pageIndex >= mNumPages) {
				throw new IndexOutOfBoundsException();
			}
			mFormat = PrinterJob.getPrinterJob().defaultPage();
			try{
				System.out.println(mFormat.getImageableHeight());
			}catch(Exception e) {
				Log.debug(6, "Exception in  getPrintable - " + e);
			}
			
			double originX = (pageIndex % mNumPagesX) * mFormat.getImageableWidth();
			double originY = (pageIndex / mNumPagesX) * mFormat.getImageableHeight();
			Point2D.Double origin = new Point2D.Double(originX, originY);
			return new TranslatedPrintable(mPainter, origin);
		}
		
		
		/**
		* This inner class's responsibility is to translate
		* the coordinate system before invoking a canvas's
		* painter. 
		*/
		
		class TranslatedPrintable implements Printable {
		
			private Printable mPainter;
			
			private Point2D mOrigin;
			
			/**
			* Create a new Printable that will translate
			* the drawing done by painter on to the
			* imageable area of a page.
			*
			* @param painter The object responsible for drawing
			* the canvas
			*
			* @param origin The point in the canvas that will be
			* mapped to the upper-left corner of
			* the page's imageable area.
			*/
			
			public TranslatedPrintable(Printable painter, Point2D origin) {
				
				mPainter = painter;
				mOrigin = origin;
			}
			
			/**
			* Prints the page at the specified index into the specified 
			* {@link Graphics} context in the specified
			* format. The zero based index of the requested page is specified 
			* by pageIndex. If the requested page does not exist then this method 
			* returns NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
			* If the Printable object aborts the print job then it throws PrinterException.
			*
			* @param graphics the context into which the page is drawn 
			* @param pageFormat the size and orientation of the page being drawn
			* @param pageIndex the zero based index of the page to be drawn
			* @return PAGE_EXISTS if the page is rendered successfully
			* or NO_SUCH_PAGE if pageIndex specifies a non-existent page.
			* @exception java.awt.print.PrinterException
			* thrown when the print job is terminated.
			*/
			
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
			
				Graphics2D g2 = (Graphics2D) graphics;
				g2.translate(-mOrigin.getX(), -mOrigin.getY());
				mPainter.print(g2, pageFormat, 1);
				return PAGE_EXISTS; 
				
			}
		}
	}
	
	
	
	
	
	
} // end of PrintFrame class

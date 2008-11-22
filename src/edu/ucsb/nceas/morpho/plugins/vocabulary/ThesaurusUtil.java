package edu.ucsb.nceas.morpho.plugins.vocabulary;

import java.util.*;
import org.w3.www._2001.sw.Europe.skos.ServiceAPI.*;
import org.w3.www._2001.sw.Europe.skos.namespace.*;
import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ucsb.nceas.morpho.Morpho;

public class ThesaurusUtil {

	public static String BT = "BT";
	public static String NT = "NT";
	public static String RT = "RT";
	public static String SC = "SC";
	public static String SN = "SN";
	public static String UF = "UF";
	public static String USE = "USE";

	// the following declarations are to get the associated terms off the
	// conceptResult object. They are stored in the following order. So use the
	// following variables or their values to retrieve the right associated
	// group of terms
	public static final int BROADER = 0;
	public static final int NARROWER = 1;
	public static final int RELATED = 2;
	public static final int SUBJECT = 3;
	public static final int USEFOR = 4;

	public static String thesaurusURL = "http://nbii-thesaurus.ornl.gov/ws/services/SKOSThesaurusService";

	public static Log log = LogFactory.getLog(ThesaurusUtil.class);

	private static ThesaurusUtil instance = null;
	
	private ConceptResult[] conceptResults = null;
	

	public static ThesaurusUtil getInstace() {
		if (instance == null) {
			instance = new ThesaurusUtil();
		}
		return instance;
	}
	
	public ThesaurusUtil() {
		//look up the the url if it is given
		String url = Morpho.getConfiguration().get("nbiiURL", 0);
		if (url != null) {
			thesaurusURL = url;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// test("evolution");
		ThesaurusUtil.getInstace().search("evolution");	
	}
	
	/**
	 * this seems to be the most useful in that it returns the broader and narrower terms for
	 * each related concept (based on input keyword)
	 * problem will be how to show the relationship intelligently for search boxes
	 */
	public ConceptResult[] search(String usrTerm) {
		try {
			SKOSThesaurusService skosService = new SKOSThesaurusServiceLocator();
			SKOSThesaurus thesaurus = skosService.getSKOSThesaurusService();
			URI thesuri = new URI();
			thesuri.setUri(thesaurusURL);
			conceptResults = 
				thesaurus.getConceptResultsByKeyword(usrTerm, thesuri);
			
			if (conceptResults.length > 0) {
				// STEP2: for each retrieved terms get the synonyms for each
				// attribute (i.e. for BT,NT.SC etc..)
				log.debug("Found Terms:");
				for (int tm = 0; tm < conceptResults.length; tm++) {
					ConceptResult currConResult = conceptResults[tm];
					Concept currMatchConcept = currConResult.getConcept();
					
					log.debug(currMatchConcept.getPreferredLabel());
				
					if (currMatchConcept != null) {
						ConceptRelatives[] conceptRelatives = currConResult.getConceptRelatives();
						log.debug("-------------");
						showRelatives(conceptRelatives, BROADER);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return conceptResults;
	}
	
	public ConceptResult[] getConceptResults() {
		return conceptResults;
	}
	
	public String[] getTerms(String selectedConcept, int category) {
		String[] tempString = null;
		for (int i = 0; i< conceptResults.length; i++) {
			if (selectedConcept.equals(conceptResults[i].getConcept().getPreferredLabel())) {
				ConceptRelatives[] conceptRelatives = conceptResults[i].getConceptRelatives();
				if (conceptRelatives != null) {
					ConceptRelatives cr = conceptRelatives[category];
					if (cr != null) {
						Concept[] tempConcepts = cr.getConcepts();
						tempString = new String[tempConcepts.length];
						for (int j=0; j < tempConcepts.length; j++) {
							tempString[j] = tempConcepts[j].getPreferredLabel();
						}
					} 
				}
				break;
			}
		}
		return tempString;
	}
	
	private static void showRelatives(ConceptRelatives[] conceptRelatives, int category) {
		// display synonyms for a particular Term Category
		if (conceptRelatives != null) {
			//  terms
			ConceptRelatives btConRel = conceptRelatives[category];
			if (btConRel != null) {
				switch (category) {
					case BROADER:
						log.debug("Broader terms:");
						break;
					case NARROWER:
						log.debug("Narrower terms:");
						break;
					case RELATED:
						log.debug("Related terms:");
						break;
					case SUBJECT:
						log.debug("Subject terms:");
						break;
					case USEFOR:
						log.debug("Use for terms:");
						break;		
					default:
						break;
				}
				Concept[] btConcepts = btConRel.getConcepts();
				for (int i = 0; i < btConcepts.length; i++) {
					Concept currConcept = btConcepts[i];
					log.debug(
							currConcept.getPreferredLabel()
									+ "=" +
							currConcept.getUri().getUri());
				}
			} 
			else {
				//log.debug("No relative terms found");
			}
		}
	}

	public static void test(String searchTerm) {

		// ********************************************************************//
		// This is a simple jsp application that demonstrates the use of
		// nbii thesaurus web service
		// This jsp uses java wrapper objects that are created by WSDL2Java
		// utility.
		// The command for running this utiltiy is
		// java org.apache.axis.wsdl.WSDL2Java
		// http://nbii-thesaurus.ornl.gov/ws/services/SKOSThesaurusService?wsdl
		// WSDL2Java is a utility under the Apache Axis project which will build
		// the stubs necessary to make web service calls.
		// Alternately the client stub files could be downloaded from the main
		// page.
		// Please find the link to the axis project under developer resources in
		// the home page.
		// Make sure that you have axis.jar file in your classpath while
		// executing this command,
		// as that is where org.apache.axis.wsdl.WSDL2Java resides.
		// You will find a number of .java files generated in a subdirectory
		// under org.w3.www._2001.sw.Europe.skos.namespace and
		// org.w3.www._2001.sw.Europe.skos.ServiceAPI
		// These are the stub files or wrapper classes.
		// The wrapper classes allow you to code in java data types and convert
		// them transparently to XML.
		// Some useful links
		// --http://ws.apache.org/axis/java/reference.html WSDL2Java reference
		// --http://ws.apache.org/axis/ Axis
		// updates:
		// 1) 01/19/05: updated to handle exceptions/faults that are generated
		// by invalid strings
		// and also to ensure minimum length
		// ********************************************************************//

		String usrTerm = searchTerm;

		boolean displayAll = false;
		String[] termGroup = { "all" };
		ArrayList termGroupList = new ArrayList();
		for (int i = 0; i < termGroup.length; i++) {
			// System.out.println(termGroup[i]);
			termGroupList.add(termGroup[i]);
		}

		if (termGroupList.contains("all")) {
			displayAll = true;
		}

		try {
			SKOSThesaurusService skosService = new SKOSThesaurusServiceLocator();
			SKOSThesaurus thesaurus = skosService.getSKOSThesaurusService();
			URI thesuri = new URI();
			thesuri.setUri(thesaurusURL);
			// STEP1: first get the entire terms from the db based on the user
			// input
			ConceptResult[] matchingTerms = thesaurus
					.getConceptResultsByKeyword(usrTerm, thesuri);

			if (matchingTerms.length > 0) {
				if (usrTerm.indexOf("http://thesaurus.nbii.gov/Concept/") != -1) {
					usrTerm = matchingTerms[0].getConcept().getPreferredLabel();
				}

				// STEP2: for each retrieved terms get the synonyms for each
				// attribute (i.e. for BT,NT.SC etc..)
				for (int tm = 0; tm < matchingTerms.length; tm++) {
					ConceptResult currConResult = matchingTerms[tm];
					Concept currMatchConcept = currConResult.getConcept();

					String prefLabel = "";
					ConceptRelatives[] conceptRelatives = null;
					if (currMatchConcept != null) {
						prefLabel = currMatchConcept.getPreferredLabel();
						conceptRelatives = currConResult.getConceptRelatives();
					}

					System.out
							.println("<tr><td colspan=\"2\" align=\"left\" bgcolor=\"#99ccff\" class='RegularBodyText' height=20> <b>"
									+ prefLabel + "</b></td></tr>");
					// display synonyms for a particular Term Category
					if (conceptRelatives != null) {
						if (displayAll || termGroupList.contains(BT)) {
							// broader terms
							ConceptRelatives btConRel = conceptRelatives[BROADER];
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150  height=20>Broader Terms:</td>");
							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");

							if (btConRel != null) {
								Concept[] btConcepts = btConRel.getConcepts();
								for (int i = 0; i < btConcepts.length; i++) {
									Concept currConcept = btConcepts[i];
									System.out
											.println("<a href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="
													+ currConcept.getUri()
															.getUri()
													+ "\">"
													+ currConcept
															.getPreferredLabel()
													+ "</a>");
									if (i < (btConcepts.length - 1)) {
										System.out.print(", ");
									}
								}
								System.out.println("</td></tr>");

							} else {
								System.out
										.println("No broader terms</td></tr>");
							}
						}
						if (displayAll || termGroupList.contains(NT)) {
							// narrower terms
							ConceptRelatives ntConRel = conceptRelatives[NARROWER];
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Narrower Terms:</td>");
							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");

							if (ntConRel != null) {
								Concept[] ntConcepts = ntConRel.getConcepts();
								for (int i = 0; i < ntConcepts.length; i++) {
									Concept currConcept = ntConcepts[i];
									System.out
											.println("<a href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="
													+ currConcept.getUri()
															.getUri()
													+ "\">"
													+ currConcept
															.getPreferredLabel()
													+ "</a>");
									if (i < (ntConcepts.length - 1)) {
										System.out.print(", ");
									}
								}
								System.out.println("</td></tr>");

							} else {
								System.out
										.println("No narrower terms</td></tr>");

							}
						}
						if (displayAll || termGroupList.contains(RT)) {
							// related terms
							ConceptRelatives rtConRel = conceptRelatives[RELATED];
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Related Terms:</td>");
							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");

							if (rtConRel != null) {
								Concept[] rtConcepts = rtConRel.getConcepts();
								for (int i = 0; i < rtConcepts.length; i++) {
									Concept currConcept = rtConcepts[i];
									System.out
											.println("<a href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="
													+ currConcept.getUri()
															.getUri()
													+ "\">"
													+ currConcept
															.getPreferredLabel()
													+ "</a>");
									if (i < (rtConcepts.length - 1)) {
										System.out.print(", ");
									}
								}
								System.out.println("</td></tr>");

							} else {
								System.out
										.println("No related terms</td></tr>");

							}
						}
						if (displayAll || termGroupList.contains(SC)) {
							// subject category
							ConceptRelatives subjConRel = conceptRelatives[SUBJECT];
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Subject Category:</td>");
							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");

							if (subjConRel != null) {
								Concept[] subjConcepts = subjConRel
										.getConcepts();
								for (int i = 0; i < subjConcepts.length; i++) {
									Concept currConcept = subjConcepts[i];
									// System.out.println("<a
									// href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="+currConcept.getPreferredLabel()+"\">"+currConcept.getPreferredLabel()+"</a>");
									System.out.println(currConcept
											.getPreferredLabel());
									if (i < (subjConcepts.length - 1)) {
										System.out.print(", ");
									}
								}
								System.out.println("</td></tr>");

							} else {
								System.out
										.println("No subject categories</td></tr>");

							}
						}
						if (displayAll || termGroupList.contains(SN)) {
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Scope Note:</td>");
							// String[] ntTermsList =
							// (String[])port.getNTTerms(usrTerm);

							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");
							if (currMatchConcept.getScopeNote() != null
									&& !currMatchConcept.getScopeNote().equals(
											"")) {
								System.out.println(currMatchConcept
										.getScopeNote());
							} else {
								System.out.println("No scope note");
							}
							System.out.println("</td></tr>");
						}
						if (displayAll || termGroupList.contains(UF)) {
							// use for terms
							ConceptRelatives ufConRel = conceptRelatives[USEFOR];
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Use For:</td>");
							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");

							if (ufConRel != null) {
								Concept[] ufConcepts = ufConRel.getConcepts();
								for (int i = 0; i < ufConcepts.length; i++) {
									Concept currConcept = ufConcepts[i];
									System.out
											.println("<a href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="
													+ currConcept.getUri()
															.getUri()
													+ "\">"
													+ currConcept
															.getPreferredLabel()
													+ "</a>");
									if (i < (ufConcepts.length - 1)) {
										System.out.print(", ");
									}
								}
								System.out.println("</td></tr>");

							} else {
								System.out
										.println("No use for terms</td></tr>");

							}

						}
						if (displayAll || termGroupList.contains(USE)) {
							String[] uTermsList = currMatchConcept
									.getNonPreferredLabels();
							System.out.println("<tr>");
							System.out
									.println("<td bgcolor=\"#E0E0E0\" class='RegularBodyText' width=150 height=20>Use:</td>");
							// String[] ntTermsList =
							// (String[])port.getNTTerms(usrTerm);

							System.out
									.println("<td class='RegularBodyText' width=650  height=20 bgcolor=\"#ffffff\">");
							if (uTermsList != null && uTermsList.length > 0) {
								for (int i = 0; i < uTermsList.length; i++) {
									System.out
											.println("<a href=\"skosThesaurusSearch.jsp?termGroup=all&usrTerm="
													+ uTermsList[i]
													+ "\">"
													+ uTermsList[i] + "</a>");
									if (i != uTermsList.length - 1) {
										System.out.print(",");
									}
								}
							} else {
								System.out.println("No use terms");
							}
							System.out.println("</td></tr>");
						}
					}
				}
			} else {

			}
		} catch (AxisFault re) {
			System.out
					.println("<SPAN class='CharTableRowStyle' style=\"color: Red;\">ERROR:</SPAN> "
							+ re.getFaultCode() + ":" + re.getFaultString());
		} catch (Exception e) {
			System.out.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

}

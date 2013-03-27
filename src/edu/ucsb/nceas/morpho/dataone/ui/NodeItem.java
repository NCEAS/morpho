package edu.ucsb.nceas.morpho.dataone.ui;

import org.dataone.service.types.v1.Node;

/**
 * Custom object for rendering a Node in the combobox 
 * Overrides the toString method to display something meaningful 
 * without loosing information about the node (e.g., nodeId)
 * 
 * @author leinfelder
 * 
 */
public class NodeItem {

	private Node node;

	public NodeItem(Node node) {
		this.node = node;
	}

	@Override
	public String toString() {
		String retVal = null;
		if (node != null) {
			retVal = node.getName();
			// retVal = node.getIdentifier().getValue() + " (" + node.getName() + ")";
			// retVal = node.getBaseURL();
		}
		return retVal;
	}
	
	public Node getNode() {
		return this.node;
	}
}

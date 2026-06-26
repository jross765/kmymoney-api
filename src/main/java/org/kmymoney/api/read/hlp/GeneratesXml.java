package org.kmymoney.api.read.hlp;

// Will generate XML strings directly from one of our 
// wrapper classes.
// 
// Needed for one special case: Merging two transactions the 
// "KMyMoney-way", where the XML string of the dying transaction
// is saved as a property of the surviving one (where it will
// stay until the merge is finally explicitly accepted on the GUI).
// For details, cf. module "API Extensions".
public interface GeneratesXml {

	String toXMLString();
	
}

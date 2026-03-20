package org.kmymoney.api.write.hlp.fil;

import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.base.basetypes.simple.KMMInstID;

public interface KMyMoneyWritableFile_Inst {
	
	/**
	 * @param instID 
	 * @param id the unique id of the institution to look for
	 * @return the customer or null if it's not found
	 * 
	 * @see #getInstitutionByID(KMMInstID)
	 */
	KMyMoneyWritableInstitution getWritableInstitutionByID(KMMInstID instID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new institution with no values that is already added to this file
	 */
	KMyMoneyWritableInstitution createWritableInstitution(String name);

	/**
	 *
	 * @param inst the transaction to remove.
	 */
	void removeInstitution(KMyMoneyWritableInstitution inst);

}

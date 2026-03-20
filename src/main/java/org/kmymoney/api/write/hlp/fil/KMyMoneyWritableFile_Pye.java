package org.kmymoney.api.write.hlp.fil;

import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

public interface KMyMoneyWritableFile_Pye {
	
	/**
	 * @param pyeID the unique id of the payee to look for
	 * @return the payee or null if it's not found
	 * 
	 * @see #getPayeeByID(KMMPyeID)
	 */
	KMyMoneyWritablePayee getWritablePayeeByID(KMMPyeID pyeID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new payee with no values that is already added to this file
	 */
	KMyMoneyWritablePayee createWritablePayee(String name);

	/**
	 *
	 * @param pye the transaction to remove.
	 */
	void removePayee(KMyMoneyWritablePayee pye);

}

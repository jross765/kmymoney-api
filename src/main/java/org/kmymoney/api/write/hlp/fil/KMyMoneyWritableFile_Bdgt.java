package org.kmymoney.api.write.hlp.fil;

import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

public interface KMyMoneyWritableFile_Bdgt {
	
	/**
	 * @param bdgtID 
	 * @param id the unique id of the budget to look for
	 * @return the budget or null if it's not found
	 * 
	 * @see #getBudgetByID(KMMBdgtID)
	 */
	KMyMoneyWritableBudget getWritableBudgetByID(KMMBdgtID bdgtID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new budget with no values that is already added to this file
	 */
	KMyMoneyWritableBudget createWritableBudget(String name);

	/**
	 *
	 * @param inst the budget to remove.
	 */
	void removeBudget(KMyMoneyWritableBudget bdgt);

}

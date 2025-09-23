package org.kmymoney.api.write;

import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.api.write.hlp.KMyMoneyWritablePricePairCore;

/**
 * Price pair that can be modified.
 * 
 * @see KMyMoneyPricePair
 */
public interface KMyMoneyWritablePricePair extends KMyMoneyPricePair, 
                                                   KMyMoneyWritablePricePairCore,
                                                   KMyMoneyWritableObject
{

	/**
	 * 
	 * @param prcPr
	 */
	void set(KMyMoneyPricePair prcPr);

	/**
	 * 
	 * @param prcPr
	 * 
	 * @see #getID()
	 */
	void setID(KMMPricePairID prcPr);
	
}

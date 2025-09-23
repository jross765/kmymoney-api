package org.kmymoney.api.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.api.write.hlp.KMyMoneyWritablePricePairCore;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Price that can be modified.
 * 
 * @see KMyMoneyPrice
 */
public interface KMyMoneyWritablePrice extends KMyMoneyPrice, 
                                               KMyMoneyWritablePricePairCore,
                                               KMyMoneyWritableObject
{

	KMyMoneyWritablePricePair getWritableParentPricePair();
	
    // ----------------------------

	/**
	 * 
	 * @param prcPrID
	 * 
	 * @see #getParentPricePair()
	 * @see #getParentPricePairID()
	 */
    void setParentPricePairID(KMMPricePairID prcPrID);
	
    /**
     * 
     * @param prcPr
     * 
     * @see #getParentPricePair()
     * @see #getParentPricePairID()
     */
    void setParentPricePair(KMyMoneyPricePair prcPr);
	
    // ----------------------------

    /**
     * 
     * @param date
     * 
     * @see #getDate()
     */
    void setDate(LocalDate date);

    /**
     * 
     * @param dateTime
     */
    void setDateTime(LocalDateTime dateTime);

    /**
     * 
     * @param src
     * 
     * @see #getSource()
     */
    void setSource(Source src);

    /**
     * 
     * @param val
     * 
     * @see #getValue()
     */
    void setValue(FixedPointNumber val);

}

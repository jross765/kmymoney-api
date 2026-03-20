package org.kmymoney.api.write.hlp.fil;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.KMyMoneyWritablePrice;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

public interface KMyMoneyWritableFile_Prc {
	
	/**
	 * @param prcPrID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getPricePairByID(KMMPricePairID)
	 */
	KMyMoneyWritablePricePair getWritablePricePairByID(KMMPricePairID prcPrID);
	
	/**
	 * @see KMyMoneyFile#getPricePairs()
	 * @return writable versions of all prices in the book.
	 * 
	 * @see #getPricePairs()
	 */
	Collection<KMyMoneyWritablePricePair> getWritablePricePairs();

	// ----------------------------

	/**
	 * @param fromSecCurrID 
	 * @param toCurrID 
	 * @return a new price pair with no splits that is already added to this file
	 */
	KMyMoneyWritablePricePair createWritablePricePair(KMMQualifSecCurrID fromSecCurrID,
													  KMMQualifCurrID toCurrID);

	/**
	 * @param prcPrID 
	 * @return a new price pair with no splits that is already added to this file
	 */
	KMyMoneyWritablePricePair createWritablePricePair(KMMPricePairID prcPrID);

	/**
	 *
	 * @param prcPr 
	 */
	void removePricePair(KMyMoneyWritablePricePair prcPr);

	// ---------------------------------------------------------------

	/**
	 * @param prcID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getPriceByID(KMMPriceID)
	 */
	KMyMoneyWritablePrice getWritablePriceByID(KMMPriceID prcID);
	
	/**
	 * 
	 * @param secID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceBySecIDDate(KMMSecID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceBySecIDDate(KMMSecID secID, LocalDate date);
	
	/**
	 * 
	 * @param secID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifSecIDDate(KMMQualifSecID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifSecIDDate(KMMQualifSecID secID, LocalDate date);
	
	/**
	 * 
	 * @param curr
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByCurrDate(Currency, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByCurrDate(Currency curr, LocalDate date);
	
	/**
	 * 
	 * @param currID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifCurrIDDate(KMMQualifCurrID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifCurrIDDate(KMMQualifCurrID currID, LocalDate date);
	
	/**
	 * 
	 * @param secCurrID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifSecCurrIDDate(KMMQualifSecCurrID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifSecCurrIDDate(KMMQualifSecCurrID secCurrID, LocalDate date);
	
    // ---------------------------------------------------------------
	
	/**
	 * @return writable versions of all prices in the book.
	 * 
	 * @see #getPrices()
	 */
	Collection<KMyMoneyWritablePrice> getWritablePrices();

	// ----------------------------

	/**
	 * @param prcPr 
	 * @param date 
	 * @return a new price with no splits that is already added to this file
	 */
	KMyMoneyWritablePrice createWritablePrice(KMyMoneyPricePairImpl prcPr, LocalDate date);

	/**
	 *
	 * @param prc 
	 * @param sec the transaction to remove.
	 */
	void removePrice(KMyMoneyWritablePrice prc);

}

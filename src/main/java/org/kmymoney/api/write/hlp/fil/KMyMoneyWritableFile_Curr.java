package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.write.KMyMoneyWritableCurrency;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface KMyMoneyWritableFile_Curr {
	
	/**
	 * 
	 * @param currCode
	 * @return
	 * 
	 * @see #getCurrencyByID(KMMCurrID)
	 */
	KMyMoneyWritableCurrency getWritableCurrencyByID(KMMCurrID currID);
	
	/**
	 * 
	 * @param qualifID
	 * @return
	 * 
	 * @see #getCurrencyByQualifID(KMMQualifCurrID)
	 */
	KMyMoneyWritableCurrency getWritableCurrencyByQualifID(KMMQualifCurrID qualifID);
	
//	List<KMyMoneyWritableCurrency> getWritableCurrencyByName(String expr);
//
//    List<KMyMoneyWritableCurrency> getWritableCurenciesByName(String expr, boolean relaxed);
//    
//    KMyMoneyWritableCurrency getWritableCurrencyByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;
    
	/**
	 * 
	 * @return
	 * 
	 * @see #getCurrencies()
	 */
	Collection<KMyMoneyWritableCurrency> getWritableCurrencies();

	// ----------------------------

	/**
	 * @param currID ISO Currency code. 
	 * @param name   Currency name
	 * @return a new transaction with no splits that is already added to this file
	 */
	KMyMoneyWritableCurrency createWritableCurrency(String currID, String name);

	/**
	 * Add a new currency.<br/>
	 * If the currency already exists, add a new price-quote for it.
	 * 
	 * @param pCmdtySpace        the name space (e.g. "GOODS" or "CURRENCY")
	 * @param pCmdtyId           the currency-name
	 * @param conversionFactor   the conversion-factor from the base-currency (EUR).
	 * @param pCmdtyNameFraction number of decimal-places after the comma
	 * @param pCmdtyName         common name of the new currency
	 */
	public void addCurrency(
			String pCmdtySpace,
			String pCmdtyId,
			FixedPointNumber conversionFactor,
			int pCmdtyNameFraction,
			String pCmdtyName);

	/**
	 *
	 * @param curr the transaction to remove.
	 */
	// void removeCurrency(KMyMoneyWritableCurrency curr);

}

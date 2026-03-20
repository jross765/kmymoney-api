package org.kmymoney.api.read.hlp.fil;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface KMyMoneyFile_Prc {

	/**
	 * @param prcPrID id of a price pair
	 * @return the identified price pair or null
	 */
	KMyMoneyPricePair getPricePairByID(KMMPricePairID prcPrID);

	/**
	 * @return all price pairs defined in the book
	 */
	// ::TODO: Change Collection --> List
	Collection<KMyMoneyPricePair> getPricePairs();

	// ---------------------------------------------------------------

	/**
	 * @param prcID id of a price
	 * @return the identified price or null
	 */
	KMyMoneyPrice getPriceByID(KMMPriceID prcID);

	// ---
	
	KMyMoneyPrice getPriceBySecIDDate(KMMSecID secID, LocalDate date);
	
	KMyMoneyPrice getPriceByQualifSecIDDate(KMMQualifSecID secID, LocalDate date);
	
	// ---
	
	KMyMoneyPrice getPriceByCurrIDDate(KMMCurrID currID, LocalDate date);
	
	KMyMoneyPrice getPriceByQualifCurrIDDate(KMMQualifCurrID currID, LocalDate date);
	
	KMyMoneyPrice getPriceByCurrDate(Currency curr, LocalDate date);
	
	// ---
	
	KMyMoneyPrice getPriceByQualifSecCurrIDDate(KMMQualifSecCurrID secCurrID, LocalDate date);

    // ---------------------------------------------------------------
    
    /**
	 * @return all prices defined in the book
	 */
	List<KMyMoneyPrice> getPrices();

    // sic: List, not Collection
	List<KMyMoneyPrice> getPricesBySecID(KMMSecID secID);
	
	List<KMyMoneyPrice> getPricesByQualifSecID(KMMQualifSecID secID);
	
	// ---
	
	List<KMyMoneyPrice> getPricesByCurrID(KMMCurrID currID);

	List<KMyMoneyPrice> getPricesByQualifCurrID(KMMQualifCurrID currID);

	List<KMyMoneyPrice> getPricesByCurr(Currency curr);
	
	// ---
	
	List<KMyMoneyPrice> getPricesByQualifSecCurrID(KMMQualifSecCurrID secCurrID);
	
	/**
	 * @param secCurrID
	 * @param pCmdtySpace the name space for pCmdtyId
	 * @param pCmdtyId    the currency-name
	 * @return the latest price-quote in the KMyMoney-file in EURO
	 * @throws InvalidQualifSecCurrIDException 
	 * @throws InvalidQualifSecCurrTypeException 
	 */
	FixedPointNumber getLatestPrice(KMMQualifSecCurrID secCurrID);

	BigFraction      getLatestPriceRat(KMMQualifSecCurrID secCurrID);

}

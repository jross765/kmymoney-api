package org.kmymoney.api.read;

import java.util.List;

import org.kmymoney.api.read.hlp.KMyMoneyPricePairCore;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;

/**
 * In KMyMoney, a price pair is a data structure which holds all the 
 * prices with <strong>one</strong> specific pair of from-currency/security
 * and to-currency, such as e.g.:
 * <ul>
 *   <li>USD to EUR (currency quotes)</li>   
 *   <li>Security MBG.DE (Mercedes-Benz Group AG) to EUR (share prices)</li>
 * </ul>
 * So, e.g., <strong>all</strong> USD/EUR quotes are held in the USD-EUR price pair,
 * <strong>all</strong> MBG.DE/EUR quotes are held in the MBG.DE-EUR price pair,
 * etc.
 */
public interface KMyMoneyPricePair extends Comparable<KMyMoneyPricePair>,
										   KMyMoneyPricePairCore 
{

    /**
     * @return Returns the ID of the price pair object. In lack of a proper technical IDs 
     *         for price pair entries in KMyMoney, this is essentially the pair ("from-security/currency",
     *         "to-currency").
     * @throws InvalidQualifSecCurrIDException
     * @throws InvalidQualifSecCurrTypeException
     */
    KMMPricePairID getID() throws InvalidQualifSecCurrIDException;
    
    // ---------------------------------------------------------------
    
    /**
     * @return Returns the price objects (for different dates/sources) under 
     *         the price pair.
     */
    List<KMyMoneyPrice> getPrices();
	
}

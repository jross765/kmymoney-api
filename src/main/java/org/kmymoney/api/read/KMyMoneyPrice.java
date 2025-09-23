package org.kmymoney.api.read;

import java.time.LocalDate;

import org.kmymoney.api.read.hlp.KMyMoneyPricePairCore;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * A price is an umbrella term comprising:
 * <ul>
 *   <li>A currency's exchange rate</li>
 *   <li>A security's quote</li>
 *   <li>A pseudo-security's price</li> 
 * </ul>
 */
public interface KMyMoneyPrice extends Comparable<KMyMoneyPrice>,
                                       KMyMoneyPricePairCore
{

	/*
	 * After superficial code analysis, it seems that the KMyMoney developers 
	 * generally put little emphasis on type safety -- sloppily speaking, 
	 * "everything's a string".
	 * That includes the price source. At present, the author does not even
	 * know precisely whether the strings written by KMyMoney for a price
	 * source are locale-specific or not (hopefully not).
	 * It seems that we generally *cannot" map from/to an enum. However, 
	 * it might be that we can define a sort of "base enum" for the most
	 * basic/common cases.
	 * Very dissatisfying indeed...     
	 */
    public enum Source {
    	
    	USER        ( "User" ),
    	TRANSACTION ( "Transaction" );
    	
    	// ---
	      
    	private String code = "UNSET";

    	// ---
    	      
    	Source(String code) {
    	    this.code = code;
    	}
    	      
    	// ---
    		
    	public String getCode() {
    	    return code;
    	}
    		
    	// no typo!
    	public static Source valueOff(String code) {
    	    for ( Source src : values() ) {
    	    	if ( src.getCode().equals(code) ) {
    	    		return src;
    	    	}
    	    }
    		    
    	    return null;
    	}
    }
	
    // ---------------------------------------------------------------
    
    /**
     * @return Returns the ID of the price object. In lack of a proper technical IDs 
     *         for price entries in KMyMoney, this is essentially the triple ("from-security/currency",
     *         "to-currency", "date").
     * @throws InvalidQualifSecCurrIDException
     * @throws InvalidQualifSecCurrTypeException
     */
    KMMPriceID getID() throws InvalidQualifSecCurrIDException;
    
    /**
     * @return Returns the parent price pair object (essentially the pair ("from-security/currency",
     *         "to-currency")
     * @throws InvalidQualifSecCurrIDException
     * @throws InvalidQualifSecCurrTypeException
     * 
     * @see #getParentPricePair()
     */
    KMMPricePairID getParentPricePairID() throws InvalidQualifSecCurrIDException;
	
    /**
     * @return Returns the parent price pair object.
     * 
     * @see #getParentPricePairID()
     */
    KMyMoneyPricePair getParentPricePair();
	
    // ----------------------------

    /**
     * @return Returns the value date of the price.
     * 
     * @see #getDateStr()
     */
    LocalDate getDate();

    /**
     * @return Returns the value date of the price.
     * 
     * @see #getDate()
     */
    String getDateStr();

    /**
     * @return Returns the price source (e.g., "User" or "Transaction")
     */
    Source getSource();

    /**
     * @return Returns the price value (the actual "price" in the narrower sense)
     * 
     * @see #getValueFormatted()
     */
    FixedPointNumber getValue();
    
    /**
     * @return Returns the price value (the actual "price" in the narrower sense)
     *         as a formatted string.
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * 
     * @see #getValue()
     */
    String getValueFormatted() throws InvalidQualifSecCurrIDException;
    
}

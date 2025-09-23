package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class SimpleSecurityQuoteTable implements SimplePriceTable,
                                                 Serializable 
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSecurityQuoteTable.class);

    private static final long serialVersionUID = 3010667150767387238L;

    // -----------------------------------------------------------

    /**
     * maps a currency-name in capital letters(e.g. "GBP") to a factor
     * {@link FixedPointNumber} that is to be multiplied with an amount of that
     * currency to get the value in the base-currency.
     *
     * @see {@link #getConversionFactor(String)}
     */
    private Map<String, FixedPointNumber> mSecID2Factor = null;

    // -----------------------------------------------------------

    public SimpleSecurityQuoteTable() {
	mSecID2Factor = new Hashtable<String, FixedPointNumber>();
    }

    // -----------------------------------------------------------

    /**
     * @param secID a currency-name in capital letters(e.g. "GBP")
     * @return a factor {@link FixedPointNumber} that is to be multiplied with an
     *         amount of that currency to get the value in the base-currency.
     */
    @Override
    public FixedPointNumber getConversionFactor(final String secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

		return mSecID2Factor.get(secID);
    }

    /**
     * @param secID a currency-name in capital letters(e.g. "GBP")
     * @param factor              a factor {@link FixedPointNumber} that is to be
     *                            multiplied with an amount of that currency to get
     *                            the value in the base-currency.
     */
    @Override
    public void setConversionFactor(final String secID, final FixedPointNumber factor) {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

		if ( factor == null ) {
			throw new IllegalArgumentException("null conversion factor given");
		}

		mSecID2Factor.put(secID, factor);
    }

    // ---------------------------------------------------------------

    /**
     * @param value               the value to convert
     * @param secID the currency to convert to
     * @return false if the conversion is not possible
     */
    @Override
    public boolean convertFromBaseCurrency(FixedPointNumber value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

        FixedPointNumber factor = getConversionFactor(secID);
        if (factor == null) {
            return false;
        }
        
        value.divide(factor);
        return true;
    }

    /**
     * @param value           the value to convert
     * @param secID it's currency
     * @return false if the conversion is not possible
     */
    @Override
    public boolean convertToBaseCurrency(FixedPointNumber value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

		FixedPointNumber factor = getConversionFactor(secID);
		if (factor == null) {
			return false;
		}
		
		value.multiply(factor);
		return true;
    }

    // ---------------------------------------------------------------

    /**
     * @return all currency-names
     */
    @Override
    public List<String> getCurrencies() {
	return new ArrayList<String>(mSecID2Factor.keySet());
    }
    
    /**
     * forget all conversion-factors.
     */
    @Override
    public void clear() {
        mSecID2Factor.clear();
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
	String result = "[SimpleSecurityQuoteTable:\n";
	
	result += "No. of entries: " + mSecID2Factor.size() + "\n";
	
	result += "Entries:\n";
	for ( String secID : mSecID2Factor.keySet() ) {
	    // result += " - " + secID + "\n";
	    result += " - " + secID + ";" + mSecID2Factor.get(secID) + "\n";
	}
	
	result += "]";
	
	return result;
    }

}

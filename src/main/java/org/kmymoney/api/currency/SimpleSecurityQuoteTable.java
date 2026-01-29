package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.base.basetypes.simple.KMMSecID;
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

    /*
     * The two objects map a *non-qualified* security ID (e.g. "E000001")
     * (*not* the security code (such as "MBG" or "FR0000120644", as opposed to the sister project)) 
     * to a factor (in two variants: FixedPointNumber and BigFraction).
     * In order to get the value in the base-currency, the factor is to be multiplied with 
     * an amount of that security.
     */
    private Map<KMMSecID, FixedPointNumber> mSecID2Factor    = null; // String, because unqualified
    private Map<KMMSecID, BigFraction>      mSecID2FactorRat = null; // dto.

    // -----------------------------------------------------------

    public SimpleSecurityQuoteTable() {
    	mSecID2Factor    = new Hashtable<KMMSecID, FixedPointNumber>();
    	mSecID2FactorRat = new Hashtable<KMMSecID, BigFraction>();
    }

    // -----------------------------------------------------------

    /**
     * @param secID a security code (e.g. "MBG")
     * @return a factor {@link FixedPointNumber} that is to be multiplied with an
     *         amount of that currency to get the value in the base-currency.
     */
    @Override
    public FixedPointNumber getConversionFactor(final String secID) {
    	return getConversionFactor(new KMMSecID(secID));
    }

    public FixedPointNumber getConversionFactor(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is mot set");
		}

    	return mSecID2Factor.get(secID);
    }

    @Override
    public BigFraction getConversionFactorRat(final String secID) {
    	return getConversionFactorRat(new KMMSecID(secID));
    }
    
    public BigFraction getConversionFactorRat(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

    	return mSecID2FactorRat.get(secID);
    }

    // ----------------------------

    /**
     * @param secID a a security code (e.g. "MBG")
     * @param factor              a factor {@link FixedPointNumber} that is to be
     *                            multiplied with an amount of that currency to get
     *                            the value in the base-currency.
     */
    @Override
    public void setConversionFactor(final String secID, final FixedPointNumber factor) {
    	setConversionFactor(new KMMSecID(secID), factor);
    }

    public void setConversionFactor(final KMMSecID secID, final FixedPointNumber factor) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}

		mSecID2Factor.put(secID, factor);
    }

    // ----------------------------

	@Override
	public void setConversionFactorRat(final String secID, final BigFraction factor) {
		setConversionFactorRat(new KMMSecID(secID), factor);
	}

    public void setConversionFactorRat(final KMMSecID secID, final BigFraction factor) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}

		mSecID2FactorRat.put(secID, factor);
    }

    // ---------------------------------------------------------------

    /**
     * @param value               the value to convert
     * @param secID the currency to convert to
     * @return false if the conversion is not possible
     */
    @Override
    public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("argument <secID> is empty");
		}

        FixedPointNumber factor = getConversionFactor(secID);
        if ( factor == null ) {
        	LOGGER.error("convertFromBaseCurrency: Cannot get conversion factor for value = " + value + " and code = '" + secID + "'");
            return null;
        }
        
        // CAUTION: mutable
        FixedPointNumber result = value.copy();
        result.divide(factor);
        return result;
    }

    public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber value, final KMMSecID secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

        return convertFromBaseCurrency(value, secID);
    }

    // ----------------------------

	@Override
	public BigFraction convertFromBaseCurrencyRat(final BigFraction value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("argument <secID> is empty");
		}

		BigFraction factor = getConversionFactorRat(secID);
        if ( factor == null ) {
        	LOGGER.error("convertFromBaseCurrencyRat: Cannot get conversion factor for value = " + value + " and code = '" + secID + "'");
            return null;
        }
        
        // CAUTION: immutable
        return value.divide(factor);
	}

	public BigFraction convertFromBaseCurrencyRat(final BigFraction value, final KMMSecID secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

        return convertFromBaseCurrencyRat(value, secID);
	}

    // ----------------------------

    /**
     * @param value           the value to convert
     * @param secID the security's ID
     * @return false if the conversion is not possible
     */
    @Override
    public FixedPointNumber convertToBaseCurrency(final FixedPointNumber value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("argument <secID> is empty");
		}

		FixedPointNumber factor = getConversionFactor(secID);
		if ( factor == null ) {
        	LOGGER.error("convertToBaseCurrency: Cannot get conversion factor for value = " + value + " and code = '" + secID + "'");
			return null;
		}
		
        // CAUTION: mutable
        FixedPointNumber result = value.copy();
        result.multiply(factor);
		return result;
    }

    public FixedPointNumber convertToBaseCurrency(final FixedPointNumber value, final KMMSecID secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		return convertToBaseCurrency(value, secID);
    }

    // ----------------------------

	@Override
	public BigFraction convertToBaseCurrencyRat(final BigFraction value, final String secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( secID.trim().equals("") ) {
			throw new IllegalArgumentException("argument <secID> is empty");
		}

		BigFraction factor = getConversionFactorRat(secID);
		if ( factor == null ) {
        	LOGGER.error("convertToBaseCurrencyRat: Cannot get conversion factor for value = " + value + " and code = '" + secID + "'");
            return null;
		}
		
        // CAUTION: immutable
		return value.multiply(factor);
	}

	public BigFraction convertToBaseCurrencyRat(final BigFraction value, final KMMSecID secID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		return convertToBaseCurrencyRat(value, secID);
	}

    // ---------------------------------------------------------------

    /**
     * @return all currency-names
     */
    @Override
    public List<String> getCodes() {
		if ( mSecID2Factor == null ) {
			throw new IllegalStateException("table is not set");
		}

		ArrayList<String> result = new ArrayList<String>();
    	for ( KMMSecID key : mSecID2Factor.keySet() ) {
    		result.add(key.toString());
    	}
    	
    	return result;
    }
    
    /**
     * forget all conversion-factors.
     */
    @Override
    public void clear() {
		if ( mSecID2Factor == null ||
			 mSecID2FactorRat == null ) {
			throw new IllegalStateException("table is not set");
		}

        mSecID2Factor.clear();
        mSecID2FactorRat.clear();
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
    	String result = "SimpleSecurityQuoteTable: [\n";
	
    	result += "No. of entries (FP): " + mSecID2Factor.size() + "\n";
    	result += "No. of entries (BF): " + mSecID2FactorRat.size() + "\n";
	
    	result += "Entries:\n";
    	for ( KMMSecID secID : mSecID2Factor.keySet() ) {
    		result += " - " + secID + ";";
    		result += mSecID2Factor.get(secID) + ";";
    		result += mSecID2FactorRat.get(secID) + "\n";
    	}
	
    	result += "]";
	
    	return result;
    }

}

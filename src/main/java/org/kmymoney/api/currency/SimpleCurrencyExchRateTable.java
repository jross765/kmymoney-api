package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.Const;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class SimpleCurrencyExchRateTable implements SimplePriceTable,
                                                    Serializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCurrencyExchRateTable.class);

    private static final long serialVersionUID = 7619137472265154205L;

    // -----------------------------------------------------------

    /*
     * The two objects map a *non-qualified* ISO 4217 currency code (e.g. "EUR" or "USD") 
     * to a factor (in two variants: FixedPointNumber and BigFraction).
     * In order to get the value in the base-currency, the factor is to be multiplied with 
     * an amount of that currency.
     */
    private Map<String, FixedPointNumber> mIso4217CurrCode2Factor    = null; // String, because unqualified
    private Map<String, BigFraction>      mIso4217CurrCode2FactorRat = null; // dto.

    // -----------------------------------------------------------

    public SimpleCurrencyExchRateTable() {
    	mIso4217CurrCode2Factor    = new Hashtable<String, FixedPointNumber>();
    	mIso4217CurrCode2FactorRat = new Hashtable<String, BigFraction>();
	
    	setConversionFactor( new KMMCurrID( Const.DEFAULT_CURRENCY ), FixedPointNumber.ONE.copy());
    	setConversionFactorRat( new KMMCurrID( Const.DEFAULT_CURRENCY ), BigFraction.ONE);
    }

    // -----------------------------------------------------------

    /**
     * @param currID a currency-name in capital letters(e.g. "GBP")
     * @return a factor {@link FixedPointNumber} that is to be multiplied with an
     *         amount of that currency to get the value in the base-currency.
     */
    public FixedPointNumber getConversionFactor(final KMMQualifCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

    	return mIso4217CurrCode2Factor.get(currID.getCode());
    }

    public FixedPointNumber getConversionFactor(final KMMCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
    	return getConversionFactor(qualifID);
    }

    public FixedPointNumber getConversionFactor(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
    	return getConversionFactor(currID);
    }

    public BigFraction getConversionFactorRat(final KMMQualifCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

    	return mIso4217CurrCode2FactorRat.get(currID.getCode());
    }
    
    public BigFraction getConversionFactorRat(final KMMCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
    	return getConversionFactorRat(qualifID);
    }
    
    public BigFraction getConversionFactorRat(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
    	return getConversionFactorRat(currID);
    }
    
    // ----------------------------

    /**
     * @param currID a currency-name in capital letters(e.g. "GBP")
     * @param factor          a factor {@link FixedPointNumber} that is to be
     *                        multiplied with an amount of that currency to get the
     *                        value in the base-currency.
     */
    public void setConversionFactor(final KMMQualifCurrID currID, final FixedPointNumber factor) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}

    	mIso4217CurrCode2Factor.put(currID.getCode(), factor);
    }

    public void setConversionFactor(final KMMCurrID currID, final FixedPointNumber factor) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
		setConversionFactor(qualifID, factor);
    }

    public void setConversionFactor(final Currency curr, final FixedPointNumber factor) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
		setConversionFactor(currID, factor);
    }

    // ----------------------------

    public void setConversionFactorRat(final KMMQualifCurrID currID, final BigFraction factor) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}

    	mIso4217CurrCode2FactorRat.put(currID.getCode(), factor);
    }

    public void setConversionFactorRat(final KMMCurrID currID, final BigFraction factor) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
		setConversionFactorRat(qualifID, factor);
    }

    public void setConversionFactor(final Currency curr, final BigFraction factor) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
		setConversionFactorRat(currID, factor);
    }

    // ---------------------------------------------------------------

    /**
     * @param value               the value to convert
     * @param currID the currency to convert to
     * @return false if the conversion is not possible
     */
    public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber value, final KMMQualifCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

        FixedPointNumber factor = getConversionFactor(currID);
        if ( factor == null ) {
        	LOGGER.error("convertFromBaseCurrency: Cannot get conversion factor for value = " + value + " and code = '" + currID + "'");
            return null;
        }
        
        // CAUTION: mutable
        FixedPointNumber result = value.copy();
        result.divide(factor);
        return result;
    }

    public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber value, final KMMCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
		return convertFromBaseCurrency(value, qualifID);
    }

    public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber value, final Currency curr) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
		return convertFromBaseCurrency(value, currID);
    }

    // ----------------------------

    public BigFraction convertFromBaseCurrencyRat(final BigFraction value, final KMMQualifCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

    	BigFraction factor = getConversionFactorRat(currID);
        if ( factor == null ) {
        	LOGGER.error("convertFromBaseCurrencyRat: Cannot get conversion factor for value = " + value + " and code = '" + currID + "'");
            return null;
        }
        
        // CAUTION: immutable
        return value.divide(factor);
    }

    public BigFraction convertFromBaseCurrencyRat(final BigFraction value, final KMMCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
		return convertFromBaseCurrencyRat(value, qualifID);
    }

    public BigFraction convertFromBaseCurrencyRat(final BigFraction value, final Currency curr) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
		return convertFromBaseCurrencyRat(value, currID);
    }

    // ----------------------------

    /**
     * @param value               the value to convert
     * @param currID it's currency
     * @return false if the conversion is not possible
     */
    public FixedPointNumber convertToBaseCurrency(final FixedPointNumber value, final KMMQualifCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		FixedPointNumber factor = getConversionFactor(currID);
    	if ( factor == null ) {
        	LOGGER.error("convertToBaseCurrency: Cannot get conversion factor for value = " + value + " and code = '" + currID + "'");
    		return null;
    	}
    	
        // CAUTION: mutable
        FixedPointNumber result = value.copy();
		result.multiply(factor);
		return result;
    }

    public FixedPointNumber convertToBaseCurrency(final FixedPointNumber value, final KMMCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
    	return convertToBaseCurrency(value, qualifID);
    }

    /**
     * @param value     the value to convert
     * @param curr the currency to convert to
     * @return false if the conversion is not possible
     */
    public FixedPointNumber convertToBaseCurrency(final FixedPointNumber value, final Currency curr) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
    	return convertToBaseCurrency(value, currID);
    }

    // ----------------------------

    public BigFraction convertToBaseCurrencyRat(final BigFraction value, final KMMQualifCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

    	BigFraction factor = getConversionFactorRat(currID);
    	if ( factor == null ) {
        	LOGGER.error("convertToBaseCurrencyRat: Cannot get conversion factor for value = " + value + " and code = '" + currID + "'");
            return null;
    	}
    	
    	// CAUTION: immutable
    	return value.multiply(factor);
    }

    public BigFraction convertToBaseCurrencyRat(final BigFraction value, final KMMCurrID currID) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( currID == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifCurrID qualifID = new KMMQualifCurrID(currID);
		return convertToBaseCurrencyRat(value, qualifID);
    }

    public BigFraction convertToBaseCurrencyRat(final BigFraction value, final Currency curr) {
		if ( value == null ) {
			throw new IllegalArgumentException("argument <value> is null");
		}

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMCurrID currID = new KMMCurrID(curr);
		return convertToBaseCurrencyRat(value, currID);
    }

    // ---------------------------------------------------------------

    /**
     * @return all currency-names
     */
    @Override
    public List<String> getCodes() {
		if ( mIso4217CurrCode2Factor == null ) {
			throw new IllegalStateException("table is not set");
		}

    	return new ArrayList<String>(mIso4217CurrCode2Factor.keySet());
    }

    // ---------------------------------------------------------------

    /**
     * forget all conversion-factors.
     */
    @Override
    public void clear() {
		if ( mIso4217CurrCode2Factor == null ||
			 mIso4217CurrCode2FactorRat == null ) {
			throw new IllegalStateException("table is not set");
		}

        mIso4217CurrCode2Factor.clear();
        mIso4217CurrCode2FactorRat.clear();
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
    	String result = "SimpleCurrencyExchRateTable [\n";
	
    	result += "No. of entries (FP): " + mIso4217CurrCode2Factor.size() + "\n";
    	result += "No. of entries (BF): " + mIso4217CurrCode2FactorRat.size() + "\n";
	
    	result += "Entries:\n";
    	for ( String currCode : mIso4217CurrCode2Factor.keySet() ) {
    		result += " - " + currCode + ";";
    		result += mIso4217CurrCode2Factor.get(currCode) + ";";
    		result += mIso4217CurrCode2FactorRat.get(currCode) + "\n";
    	}
	
    	result += "]";
	
    	return result;
    }

}

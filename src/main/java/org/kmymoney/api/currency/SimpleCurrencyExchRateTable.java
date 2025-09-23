package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class SimpleCurrencyExchRateTable implements SimplePriceTable,
                                                    Serializable
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCurrencyExchRateTable.class);

    private static final long serialVersionUID = 7619137472265154205L;

    // -----------------------------------------------------------

    /**
     * maps a currency-name in capital letters(e.g. "GBP") to a factor
     * {@link FixedPointNumber} that is to be multiplied with an amount of that
     * currency to get the value in the base-currency.
     *
     * @see {@link #getConversionFactor(String)}
     */
    private Map<String, FixedPointNumber> mIso4217CurrCodes2Factor = null;

    // -----------------------------------------------------------

    public SimpleCurrencyExchRateTable() {
	// super();
	mIso4217CurrCodes2Factor = new Hashtable<String, FixedPointNumber>();
	
	setConversionFactor("EUR", new FixedPointNumber(1));
	// setConversionFactor("GBP", new FixedPointNumber("769/523"));
    }

    // -----------------------------------------------------------

    /**
     * @param iso4217CurrCode a currency-name in capital letters(e.g. "GBP")
     * @return a factor {@link FixedPointNumber} that is to be multiplied with an
     *         amount of that currency to get the value in the base-currency.
     */
    public FixedPointNumber getConversionFactor(final String iso4217CurrCode) {
	return mIso4217CurrCodes2Factor.get(iso4217CurrCode);
    }

    /**
     * @param iso4217CurrCode a currency-name in capital letters(e.g. "GBP")
     * @param factor          a factor {@link FixedPointNumber} that is to be
     *                        multiplied with an amount of that currency to get the
     *                        value in the base-currency.
     */
    public void setConversionFactor(final String iso4217CurrCode, final FixedPointNumber factor) {
	mIso4217CurrCodes2Factor.put(iso4217CurrCode, factor);
    }

    // ---------------------------------------------------------------

    /**
     * @param value               the value to convert
     * @param iso4217CurrencyCode the currency to convert to
     * @return false if the conversion is not possible
     */
    public boolean convertFromBaseCurrency(FixedPointNumber value, final String iso4217CurrencyCode) {
		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}

		if ( iso4217CurrencyCode == null ) {
			throw new IllegalArgumentException("null ISO code given");
		}

		if ( iso4217CurrencyCode.trim().equals("") ) {
			throw new IllegalArgumentException("empty ISO codce given");
		}

        FixedPointNumber factor = getConversionFactor(iso4217CurrencyCode);
        if (factor == null) {
            return false;
        }
        value.divide(factor);
        return true;
    }

    /**
     * @param value               the value to convert
     * @param iso4217CurrencyCode it's currency
     * @return false if the conversion is not possible
     */
    public boolean convertToBaseCurrency(FixedPointNumber value, final String iso4217CurrencyCode) {
		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}

		if ( iso4217CurrencyCode == null ) {
			throw new IllegalArgumentException("null ISO code given");
		}

		if ( iso4217CurrencyCode.trim().equals("") ) {
			throw new IllegalArgumentException("empty ISO code given");
		}

		FixedPointNumber factor = getConversionFactor(iso4217CurrencyCode);
		if (factor == null) {
			return false;
		}
		
		value.multiply(factor);
		return true;
    }

    /**
     * @param value     the value to convert
     * @param pCurrency the currency to convert to
     * @return false if the conversion is not possible
     */
    public boolean convertToBaseCurrency(FixedPointNumber value, final Currency pCurrency) {
	return convertToBaseCurrency(value, pCurrency.getCurrencyCode());
    }

    // ---------------------------------------------------------------

    /**
     * @return all currency-names
     */
    public List<String> getCurrencies() {
	return new ArrayList<String>(mIso4217CurrCodes2Factor.keySet());
    }

    // ---------------------------------------------------------------

    /**
     * forget all conversion-factors.
     */
    public void clear() {
        mIso4217CurrCodes2Factor.clear();
    }

    @Override
    public String toString() {
	String result = "[SimpleCurrencyExchRateTable:\n";
	
	result += "No. of entries: " + mIso4217CurrCodes2Factor.size() + "\n";
	
	result += "Entries:\n";
	for ( String currCode : mIso4217CurrCodes2Factor.keySet() ) {
	    // result += " - " + currCode + "\n";
	    result += " - " + currCode + ";" + mIso4217CurrCodes2Factor.get(currCode) + "\n";
	}
	
	result += "]";
	
	return result;
    }

}

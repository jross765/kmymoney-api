package org.kmymoney.api.currency;

import java.util.List;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface SimplePriceTable {

    /**
     * @param code
     * @return conversion factor from currency specified by
     *         code to base currency
     */
    FixedPointNumber getConversionFactor(final String code);

    // ::TODO
    // BigFraction getConversionFactorRat(final String code);

    /**
     * @param code
     * @param factor
     */
    void setConversionFactor(final String code, final FixedPointNumber factor);

    // ::TODO
    // void setConversionFactorRat(final String code, final BigFraction factor);

    // ---------------------------------------------------------------

    /**
     * @param value
     * @param code
     * @return
     */
    boolean convertFromBaseCurrency(FixedPointNumber value, final String code);

    // ::TODO
    // boolean convertFromBaseCurrencyRat(BigFraction value, final String code);
    
    // ---

    /**
     * @param value
     * @param code
     * @return
     */
    boolean convertToBaseCurrency(FixedPointNumber value, final String code);

    // ::TODO
    // boolean convertToBaseCurrencyRat(BigFraction value, final String code);

    // ---------------------------------------------------------------

    List<String> getCurrencies();

    void clear();

}

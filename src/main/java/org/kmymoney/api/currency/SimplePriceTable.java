package org.kmymoney.api.currency;

import java.util.List;

import org.apache.commons.numbers.fraction.BigFraction;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface SimplePriceTable {

    /**
     * @param codeOrID
     * @return conversion factor from currency specified by
     *         code to base currency
     */
    FixedPointNumber getConversionFactor(String codeOrID);

    /**
     * @param codeOrID
     * @return
     */
    BigFraction      getConversionFactorRat(String codeOrID);

    /**
     * @param codeOrID
     * @param factor
     */
    void setConversionFactor(String codeOrID, FixedPointNumber factor);

    /**
     * @param codeOrID
     * @param factor
     */
    void setConversionFactorRat(String codeOrID, BigFraction factor);

    // ---------------------------------------------------------------

    /**
     * @param value
     * @param codeOrID
     * @return
     */
    FixedPointNumber convertFromBaseCurrency(FixedPointNumber value, String codeOrID);

    /**
     * @param value
     * @param codeOrID
     * @return
     */
    BigFraction      convertFromBaseCurrencyRat(BigFraction value, String codeOrID);
    
    // ---

    /**
     * @param value
     * @param codeOrID
     * @return
     */
    FixedPointNumber convertToBaseCurrency(FixedPointNumber value, String codeOrID);

    /**
     * @param value
     * @param codeOrID
     * @return
     */
    BigFraction      convertToBaseCurrencyRat(BigFraction value, String codeOrID);

    // ---------------------------------------------------------------

    List<String> getCodes();

    void clear();

}

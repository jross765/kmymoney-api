package org.kmymoney.api.read;

import java.math.BigInteger;
import java.util.List;

import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;

/**
 * A KMyMoney currency is just that, i.e. it satisfies the standard definition.
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/details.currencies.html">KMyMoney handbook</a>
 * <br>
 * Cf. <a href="https://en.wikipedia.org/wiki/Currency">Wikipedia</a>
 */
public interface KMyMoneyCurrency {

    /**
     * @return Returns the technical ID (in this particular case, the ISO currency code) 
     */
    String getID();

    /**
     * @return Returns the fully-qualified ID (inluding the preifx)
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * {@link KMMSecCurr}
     */
    KMMQualifCurrID getQualifID() throws InvalidQualifSecCurrIDException;

    /**
     * @return Returns the currency symbol (e.g., the dollar symbol, the euro symbol, etc.) 
     */
    String getSymbol();

    // ------------------------------------------------------------

    /**
     * @return Returns the currency name (spelled out, such as "Euro" or "US Dollar")
     */
    String getName();
    
    /**
     * @return
     */
    BigInteger getPP();
    
    /**
     * @return Returns the rounding method (we suppose that it is fully redundant)
     * @throws UnknownRoundingMethodException
     */
    KMMSecCurr.RoundingMethod getRoundingMethod() throws UnknownRoundingMethodException;
    
    /**
     * @return
     */
    BigInteger getSAF();
    
    /**
     * @return
     */
    BigInteger getSCF();
    
    // ------------------------------------------------------------

    List<KMyMoneyPrice> getQuotes() throws InvalidQualifSecCurrIDException;
    
    KMyMoneyPrice getYoungestQuote() throws InvalidQualifSecCurrIDException;
    
    // ------------------------------------------------------------

    List<KMyMoneyTransactionSplit> getTransactionSplits();
    
}

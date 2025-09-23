package org.kmymoney.api.read;

import java.math.BigInteger;
import java.util.List;

import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

/**
 * In KMyMoney lingo, "security" is an umbrella term for
 * <ul>
 *   <li>(Real) Securities (shares, bonds, funds, etc.)</li>
 *   <li>Possibly other assets that can be mapped to this KMyMoney entity as pseudo-securities, 
 *   such as crypto-currencies, physical precious metals, etc.</li>
 * </ul>
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/details.investments.ledger.html#idm2349">KMyMoney handbook</a>
 */
public interface KMyMoneySecurity extends Comparable<KMyMoneySecurity>,
										  HasUserDefinedAttributes 
{

    /**
     * @return
     * 
     * @see #getQualifID()
     */
    KMMSecID getID();

    /**
     * @return
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * 
     * @see #getID()
     */
    KMMQualifSecID getQualifID() throws InvalidQualifSecCurrIDException;

    /**
     * @return
     */
    String getSymbol();

    /**
     * ISIN, CUSIP, SEDOL, WKN...
     * @return
     */
    String getCode();

    // ------------------------------------------------------------

    /**
     * @return
     * 
     * @throws UnknownSecurityTypeException
     */
    KMMSecCurr.Type getType();
    
    /**
     * @return
     */
    String getName();
    
    /**
     * @return
     */
    BigInteger getPP();
    
    /**
     * @return
     * @throws UnknownRoundingMethodException
     */
    KMMSecCurr.RoundingMethod getRoundingMethod() throws UnknownRoundingMethodException;
    
    /**
     * @return
     */
    BigInteger getSAF();
    
    KMMQualifCurrID getTradingCurrency() throws InvalidQualifSecCurrIDException;

    /**
     * @return
     */
    String getTradingMarket();

    // ------------------------------------------------------------
    
    List<KMyMoneyAccount> getStockAccounts();

    // ------------------------------------------------------------

    /**
     * @return
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * 
     * @see #getYoungestQuote()
     */
    List<KMyMoneyPrice> getQuotes() throws InvalidQualifSecCurrIDException;
    
    /**
     * @return
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * 
     * @see #getQuotes()
     */
    KMyMoneyPrice getYoungestQuote() throws InvalidQualifSecCurrIDException;
    
    // ------------------------------------------------------------

    List<KMyMoneyTransactionSplit> getTransactionSplits();
    
}

package org.kmymoney.api.write;

import java.math.BigInteger;
import java.util.List;

import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;

/**
 * Security that can be modified.
 * 
 * @see KMyMoneySecurity
 */
public interface KMyMoneyWritableSecurity extends KMyMoneySecurity,
                                                  KMyMoneyWritableObject,
                                                  HasWritableUserDefinedAttributes
{

	void remove() throws ObjectCascadeException;

	// ------------------------------------------------------------
    
    List<KMyMoneyWritableAccount> getWritableStockAccounts();

	// ---------------------------------------------------------------

    /**
     * 
     * @param symb
     * 
     * @see #getSymbol()
     */
	void setSymbol(String symb);

	/**
	 * 
	 * @param code
	 * 
	 * @see #getCode()
	 */
	void setCode(String code);

	// ---------------------------------------------------------------

	/**
	 * 
	 * @param type
	 * 
	 * @see #getType()
	 */
	void setType(KMMSecCurr.Type type);

	/**
	 * 
	 * @param name
	 * 
	 * @see #getName()
	 */
	void setName(String name);

	/**
	 * 
	 * @param pp
	 * 
	 * @see #getPP()
	 */
	void setPP(BigInteger pp);

	/**
	 * 
	 * @param meth
	 * 
	 * @see #getRoundingMethod()
	 */
	void setRoundingMethod(KMMSecCurr.RoundingMethod meth);

	/**
	 * 
	 * @param saf
	 * 
	 * @see #getSAF()
	 */
	void setSAF(BigInteger saf);

	/**
	 * 
	 * @param currID
	 * 
	 * @see #getTradingCurrency()
	 */
	void setTradingCurrency(KMMQualifCurrID currID);

	/**
	 * 
	 * @param mkt
	 * 
	 * @see #getTradingMarket()
	 */
	void setTradingMarket(String mkt);

}

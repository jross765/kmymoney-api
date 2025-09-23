package org.kmymoney.api.write;

import java.math.BigInteger;

import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;

/**
 * Currency that can be modified.
 * 
 * @see KMyMoneyCurrency
 */
public interface KMyMoneyWritableCurrency extends KMyMoneyCurrency,
                                                  KMyMoneyWritableObject 
{

	// CAUTION: No! 
    // void remove();
   
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
     * @param mthd
     * 
     * @see #getRoundingMethod()
     */
    void setRoundingMethod(KMMSecCurr.RoundingMethod mthd);
    
    /**
     * 
     * @param saf
     * 
     * @see #getSAF()
     */
    void setSAF(BigInteger saf);
    
    /**
     * 
     * @param scf
     * 
     * @see #getSCF()
     */
    void getSCF(BigInteger scf);
    
}

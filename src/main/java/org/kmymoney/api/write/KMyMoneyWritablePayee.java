package org.kmymoney.api.write;

import java.math.BigInteger;

import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.write.hlp.HasWritableAddress;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;

/**
 * Payee that can be modified.
 * 
 * @see KMyMoneyPayee
 */
public interface KMyMoneyWritablePayee extends KMyMoneyPayee,
                                               KMyMoneyWritableObject,
                                               HasWritableAddress
{

    void remove();
   
	// ---------------------------------------------------------------

    /**
     * 
     * @param name
     * 
     * @see #getName()
     */
    void setName(String name);

    /**
     * 
     * @param acctID
     * 
     * @see #getDefaultAccountID()
     */
    void setDefaultAccountID(KMMComplAcctID acctID);
    
    /**
     * 
     * @param eml
     * 
     * @see #getEmail()
     */
    void setEmail(String eml);
    
    /**
     * 
     * @param ref
     * 
     * @see #getReference()
     */
    void setReference(String ref);
    
    /**
     * 
     * @param nts
     * 
     * @see #getNotes()
     */
    void setNotes(String nts);
    
    // ---------------------------------------------------------------

    /**
     * 
     * @param enbl
     * 
     * @see #getMatchingEnabled()
     */
    void setMatchingEnabled(BigInteger enbl);

    /**
     * 
     * @param key
     * 
     * @see #getMatchKey()
     */
    void setMatchKey(String key);

    /**
     * 
     * @param key
     * 
     * @see #getUsingMatchKey()
     */
    void setUsingMatchKey(BigInteger key);

    /**
     * 
     * @param val
     * 
     * @see #getMatchIgnoreCase()
     */
    void setMatchIgnoreCase(BigInteger val);

}

package org.kmymoney.api.read;

import java.util.List;

import org.kmymoney.api.read.hlp.HasAddress;
import org.kmymoney.api.read.hlp.HasTransactions;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

/**
 * Person or entity that is being paid in a transaction.
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/makingmostof.mapping.html#makingmostof.mapping.payees">KMyMoney handbook</a>
 */
public interface KMyMoneyPayee extends KMyMoneyObject,
									   HasTransactions,
                                       HasAddress
{
    
    /**
     * @return
     */
    KMMPyeID getID();

    /**
     * @return
     */
    String getName();

    /**
     * @return
     */
    KMMComplAcctID getDefaultAccountID();
    
    /**
     * @return
     */
    String getEmail();
    
    /**
     * @return
     */
    String getReference();
    
    /**
     * @return
     */
    String getNotes();
    
    // ---------------------------------------------------------------
    
    /**
     * @return
     */
    String getIDPattern();

    /**
     * @return
     */
    String getURLTemplate();

    // ---------------------------------------------------------------

    /**
     * @return
     */
    boolean getMatchingEnabled();

    /**
     * @return a list of match keys
     */
    List<String> getMatchKeys();

    /**
     * @return
     */
    boolean getUsingMatchKey();

    /**
     * @return
     */
    boolean getMatchIgnoreCase();

}

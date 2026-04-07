package org.kmymoney.api.read.hlp.fil;

import java.util.Currency;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

public interface KMyMoneyFile_TrxSplt {

    /**
     * @param spltID the unique ID of the transaction split to look for
     * @return the transaction split or null if it's not found
     * 
     * @see #getTransactionSplits()
     */
	KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID);

	KMyMoneyTransactionSplit getTransactionSplitByID(KMMTrxID trxID, KMMSpltID spltID);

	KMyMoneyTransactionSplit getTransactionSplitByAcctIDAndTrxID(KMMAcctID acctID, KMMTrxID trxID);

	// ---------------------------------------------------------------
	
	// ::EMPTY

	// ---------------------------------------------------------------

    /**
     * 
     * @param qualifID
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given security/currency. 
     */
    List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecCurrID(KMMQualifSecCurrID qualifID);
    
    /**
     * 
     * @param qualifID
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given security. 
     */
    List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecID(KMMQualifSecID qualifID);

	/**
	 * 
	 * @param secID
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given security. 
	 */
    List<KMyMoneyTransactionSplit> getTransactionSplitsBySecID(KMMSecID secID);
    
    /**
     * 
     * @param qualifID
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given currency. 
     */
    List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifCurrID(KMMQualifCurrID qualifID);
    
    /**
     * 
     * @param currID
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given currency. 
     */
    List<KMyMoneyTransactionSplit> getTransactionSplitsByCurrID(KMMCurrID currID);

    /**
     * 
     * @param curr
     * @return list of all transaction splits (ro-objects)
     *   denominated in the given currency. 
     */
    List<KMyMoneyTransactionSplit> getTransactionSplitsByCurr(Currency curr);

	// ---------------------------------------------------------------

    /**
     * @return list of all transaction splits (ro-objects)
     */
	List<KMyMoneyTransactionSplit> getTransactionSplits();

}

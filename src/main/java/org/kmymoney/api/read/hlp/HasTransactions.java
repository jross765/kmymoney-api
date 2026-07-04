package org.kmymoney.api.read.hlp;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;

public interface HasTransactions
{
	
    /**
     * The returned list is sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * {@link KMyMoneyTransactionSplit}
     */
    List<KMyMoneyTransactionSplit> getTransactionSplits();

    /**
     * @param spltID the split-id to look for
     * @return the identified split or null
     */
    KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID);
    
    // ---------------------------------------------------------------

    boolean hasTransactions();

    List<KMyMoneyTransaction> getTransactions();

    List<KMyMoneyTransaction> getTransactions(LocalDate fromDate, LocalDate toDate);

}

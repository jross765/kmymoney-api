package org.kmymoney.api.read.hlp;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;

public interface HasTransactions
{
	
    List<KMyMoneyTransactionSplit> getTransactionSplits();

    KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID);
    
    void addTransactionSplit(KMyMoneyTransactionSplit splt);

    // ---------------------------------------------------------------

    boolean hasTransactions();

    List<KMyMoneyTransaction> getTransactions();

    List<KMyMoneyTransaction> getTransactions(LocalDate fromDate, LocalDate toDate);

}

package org.kmymoney.api.write.hlp;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;

public interface HasWritableTransactions
{
	
    List<KMyMoneyWritableTransactionSplit> getWritableTransactionSplits();

    KMyMoneyWritableTransactionSplit getWritableTransactionSplitByID(KMMQualifSpltID spltID);
    
    // ---------------------------------------------------------------

    List<KMyMoneyWritableTransaction> getWritableTransactions();

    List<KMyMoneyWritableTransaction> getWritableTransactions(LocalDate fromDate, LocalDate toDate);

}

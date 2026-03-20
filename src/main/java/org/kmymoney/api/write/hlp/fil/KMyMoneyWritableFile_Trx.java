package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

public interface KMyMoneyWritableFile_Trx {
	
	/**
	 * @param trxID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getTransactionByID(KMMTrxID)
	 */
	KMyMoneyWritableTransaction getWritableTransactionByID(KMMTrxID trxID);
	
	/**
	 * @return writable versions of all transactions in the book.
	 * 
	 * @see #getTransactions()
	 */
	Collection<? extends KMyMoneyWritableTransaction> getWritableTransactions();

	// ----------------------------

	/**
	 * @return a new transaction with no splits that is already added to this file
	 */
	KMyMoneyWritableTransaction createWritableTransaction();

	/**
	 *
	 * @param trx the transaction to remove.
	 */
	void removeTransaction(KMyMoneyWritableTransaction trx);

	// ---------------------------------------------------------------

	/**
	 * 
	 * @param spltID
	 * @return
	 * 
	 * @see #getTransactionSplitByID(KMMQualifSpltID)
	 */
	KMyMoneyWritableTransactionSplit getWritableTransactionSplitByID(KMMQualifSpltID spltID);
	
	// ::TODO
	
}

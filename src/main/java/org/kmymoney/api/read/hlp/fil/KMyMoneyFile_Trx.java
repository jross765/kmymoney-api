package org.kmymoney.api.read.hlp.fil;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

public interface KMyMoneyFile_Trx {

	/**
	 * @param trxID the unique ID of the transaction to look for
	 * @return the transaction or null if it's not found
	 */
	KMyMoneyTransaction getTransactionByID(KMMTrxID trxID);

	/**
	 * @return a (possibly read-only) collection of all transactions Do not modify
	 *         the returned collection!
	 * 
	 * @see #getTransactions(LocalDate, LocalDate)
	 */
	List<? extends KMyMoneyTransaction> getTransactions();

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * 
	 * @see #getTransactions()
	 */
	List<? extends KMyMoneyTransaction> getTransactions(LocalDate fromDate, LocalDate toDate);

}

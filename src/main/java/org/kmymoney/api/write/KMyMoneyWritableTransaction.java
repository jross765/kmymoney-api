package org.kmymoney.api.write;

import java.time.LocalDate;
import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.simple.KMMSpltID;

import xyz.schnorxoborx.base.beanbase.TransactionSplitNotFoundException;

/**
 * Transaction that can be modified.<br/>
 * For PropertyChange-Listeners we support the properties: "description" and
 * "splits".
 * 
 * @see KMyMoneyTransaction
 */
public interface KMyMoneyWritableTransaction extends KMyMoneyTransaction,
                                                     KMyMoneyWritableObject,
                                                     HasWritableUserDefinedAttributes
{

	/**
	 * @param id the new currency
	 * @see #setCurrencyNameSpace(String)
	 * 
	 * @see #getQualifSecCurrID()
	 */
	void setCurrencyID(String id);

	/**
	 * @param id the new name space
	 * 
	 * @see #getQualifSecCurrID()
	 */
	void setCurrencyNameSpace(String id);

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 * 
	 * @return the file we are associated with
	 * 
	 * @see #getKMyMoneyFile()
	 */
	KMyMoneyWritableFile getWritableFile();

	/**
	 * @param dateEntered the day (time is ignored) that this transaction has been
	 *                    entered into the system
	 *                    
	 * @see #getDateEntered()
	 * @see #setDatePosted(LocalDate)
	 */
	void setDateEntered(LocalDate dateEntered);

	/**
	 * @param datePosted the day (time is ignored) that the money was transfered
	 * 
	 * @see #getDatePosted()
	 * @see #setDateEntered(LocalDate)
	 */
	void setDatePosted(LocalDate datePosted);

	/**
	 * 
	 * @param desc
	 * 
	 * @see #getMemo()
	 */
	void setMemo(String desc);

	/**
	 * @return first split of a transaction
	 * @throws TransactionSplitNotFoundException 
	 * @see KMyMoneyTransaction#getFirstSplit()
	 */
	KMyMoneyWritableTransactionSplit getWritableFirstSplit() throws TransactionSplitNotFoundException;

	/**
	 * @return second split of a transaction
	 * @throws TransactionSplitNotFoundException 
	 * @see KMyMoneyTransaction#getSecondSplit()
	 */
	KMyMoneyWritableTransactionSplit getWritableSecondSplit() throws TransactionSplitNotFoundException;

	/**
	 * @param spltID 
	 * @return 
	 * @see KMyMoneyTransaction#getSplitByID(KMMSpltID)
	 */
	KMyMoneyWritableTransactionSplit getWritableSplitByID(KMMSpltID spltID);

	/**
	 *
	 * @return the first split of this transaction or null.
	 */
	KMyMoneyWritableTransactionSplit getFirstSplit() throws TransactionSplitNotFoundException;

	/**
	 * @return the second split of this transaction or null.
	 */
	KMyMoneyWritableTransactionSplit getSecondSplit() throws TransactionSplitNotFoundException;

	/**
	 * @return 
	 * @see KMyMoneyTransaction#getSplits()
	 */
	Collection<? extends KMyMoneyWritableTransactionSplit> getWritableSplits();

	/**
	 * Create a new split, already attached to this transaction.
	 * 
	 * @param account the account for the new split
	 * @return a new split, already attached to this transaction
	 */
	KMyMoneyWritableTransactionSplit createWritableSplit(KMyMoneyAccount account);

	KMyMoneyWritableTransactionSplit createWritableSplit(KMyMoneyAccount account, 
														 KMyMoneyPayee pye,
														 Collection<KMyMoneyTag> tagList);

	/**
	 * Removes the given split from this transaction.
	 * 
	 * @param impl the split to remove from this transaction
	 */
	void remove(KMyMoneyWritableTransactionSplit impl);

	/**
	 * remove this transaction.
	 */
	void remove();

}

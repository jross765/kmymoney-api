package org.kmymoney.api.write;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMTagID;

import xyz.schnorxoborx.base.beanbase.IllegalTransactionSplitActionException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Transaction-Split that can be modified.
 * 
 * @see KMyMoneyTransactionSplit
 */
public interface KMyMoneyWritableTransactionSplit extends KMyMoneyTransactionSplit,
                                                          KMyMoneyWritableObject
{

	/**
	 * @return the transaction this is a split of.
	 */
	KMyMoneyWritableTransaction getTransaction();

	/**
	 * Remove this split from the system.
	 */
	void remove();

	/**
	 * Does not convert the quantity to another currency if the new account has
	 * another one than the old one!
	 * 
	 * @param acctID the new account to give this money to/take it from.
	 * 
	 * @see #getAccount()
	 * @see #getAccountID()
	 * @see #setAccount(KMyMoneyAccount)
	 */
	void setAccountID(KMMComplAcctID acctID);

	void setAccountID(KMMAcctID acctID);

	/**
	 * Does not convert the quantity to another currency if the new account has
	 * another one then the old!
	 * 
	 * @param acct the new account to give this money to/take it from.
	 * 
	 * @see #getAccount()
	 * @see #getAccountID()
	 * @see #setAccountID(KMMComplAcctID)
	 */
	void setAccount(KMyMoneyAccount acct);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setShares(String)}.
	 * 
	 * @param n the new quantity (in the currency of the account)
	 * 
	 * @see #getShares()
	 * @see #setShares(BigFraction)
	 * @see #setShares(String)
	 */
	@Deprecated
	void setShares(FixedPointNumber n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setShares(String)}.
	 * 
	 * @param n the new quantity (in the currency of the account)
	 * 
	 * @see #getShares()
	 * @see #setShares(FixedPointNumber)
	 * @see #setShares(String)
	 */
	void setShares(BigFraction n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setValue(FixedPointNumber)}.
	 * 
	 * @param n the new value (in the currency of the transaction)
	 * 
	 * @see #getValue()
	 */
	@Deprecated
	void setValue(FixedPointNumber n);
	
	void setValue(BigFraction n);
	
	/**
	 * 
	 * @param prc
	 * 
	 * @see #getPrice()
	 */
	@Deprecated
	void setPrice(FixedPointNumber prc);
	
	void setPrice(BigFraction prc);
	
	/**
	 * 
	 * @param pyeID
	 * 
	 * @see #getNumber()
	 */
	void setNumber(String num);
	
	// ----------------------------

	/**
	 * 
	 * @param pyeID
	 * 
	 * @see #getPayee()
	 * @see #getPayeeID()
	 */
	void setPayeeID(KMMPyeID pyeID);

	/**
	 * 
	 * @param pye
	 * 
	 * @see #getPayee()
	 * @see #getPayeeID()
	 */
	void setPayee(KMyMoneyPayee pye);

	// ----------------------------
	
	void clearTags();

	/**
	 * 
	 * @param pyeID
	 * 
	 * @see #getPayee()
	 * @see #getPayeeID()
	 */
	void addTagID(KMMTagID tagID);

	/**
	 * 
	 * @param pye
	 * 
	 * @see #getPayee()
	 * @see #getPayeeID()
	 */
	void addTag(KMyMoneyTag tag);

	// ----------------------------

	/**
	 * Set the description-text.
	 * 
	 * @param desc the new description
	 * 
	 * @see #getMemo()
	 */
	void setMemo(String desc);

	/**
	 * Set the type of association this split has with an invoice's lot.
	 * @param act 
	 * 
	 * @param action null, or one of the ACTION_xyz values defined
	 * @throws IllegalTransactionSplitActionException
	 * 
	 * @see #getAction()
	 */
	void setAction(Action act) throws IllegalTransactionSplitActionException;
	
	void setReconState(ReconState stat);

	@Deprecated
	void setState(ReconState stat);

}

package org.kmymoney.api.write;

import java.time.LocalDate;
import java.util.Currency;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;


/**
 * Account that can be modified.<br/>
 * Supported properties for the propertyChangeListeners:
 * <ul>
 * <li>name</li>
 * <li>currencyID</li>
 * <li>currencyNameSpace</li>
 * <li>description</li>
 * <li>type</li>
 * <li>parentAccount</li>
 * <li>transactionSplits (not giving the old value of the list)</li>
 * </ul>
 * 
 * @see KMyMoneyAccount
 */
public interface KMyMoneyWritableAccount extends KMyMoneyAccount, 
                                                 KMyMoneyWritableObject,
                                                 HasWritableUserDefinedAttributes
{

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	KMyMoneyWritableFile getWritableKMyMoneyFile();

	/**
	 * Change the user-definable name. It should contain no newlines but may contain
	 * non-ascii and non-western characters.
	 *
	 * @param name the new name (not null)
	 * 
	 * @see #getName()
	 */
	void setName(String name);

	/**
	 * @param desc the user-defined description (may contain multiple lines and
	 *             non-ascii-characters)
	 * 
	 * @see #getMemo()
	 */
	void setMemo(String desc);

	/**
	 * Get the sum of all transaction-splits affecting this account in the given
	 * time-frame.
	 *
	 * @param from when to start, inclusive
	 * @param to   when to stop, exlusive.
	 * @return the sum of all transaction-splits affecting this account in the given
	 *         time-frame.
	 *         
	 * @see #getBalanceChange(LocalDate, LocalDate)
	 */
	FixedPointNumber getBalanceChange(LocalDate from, LocalDate to);

	/**
	 * Set the type of the account (income, ...).
	 *
	 * @param type the new type.
	 * 
	 * @see #getType()
	 */
	void setType(KMyMoneyAccount.Type type);

	// ----------------------------

	/**
	 * 
	 * @param secCurrID
	 * 
	 * @see #getQualifSecCurrID()
	 */
	void setQualifSecCurrID(KMMQualifSecCurrID secCurrID);
	
//	void setQualifSecID(KMMQualifSecID secID);
//	
//	void setQualifCurrID(KMMQualifCurrID currID);
	
	/**
	 * 
	 * @param secID
	 * 
	 * @see #getQualifSecCurrID()
	 */
	void setSecID(KMMSecID secID);
	
	/**
	 * 
	 * @param curr
	 * 
	 * @see #getQualifSecCurrID()
	 */
	void setCurrency(Currency curr);

	/**
	 * 
	 * @param currCode
	 * 
	 * @set {@link #getQualifSecCurrID()}
	 */
	void setCurrency(String currCode);
	
	// ----------------------------
	
	/**
	 * 
	 * @param instID
	 * 
	 * @see #getInstitutionID()
	 */
	void setInstitutionID(KMMInstID instID);

	// ----------------------------

	/**
	 * @param newparent the new account or null to make it a top-level-account
	 * 
	 * @see #getParentAccount()
	 */
	void setParentAccount(KMyMoneyAccount newparent);

	/**
	 * If the accountId is invalid, make this a top-level-account.
	 * @param prntAcctID 
	 *
	 * @see #getParentAccountID()
	 */
	void setParentAccountID(KMMComplAcctID prntAcctID);

	/**
	 * Remove this account from the system.<br/>
	 * Throws IllegalStateException if this account has splits or children.
	 */
	void remove();
	
}

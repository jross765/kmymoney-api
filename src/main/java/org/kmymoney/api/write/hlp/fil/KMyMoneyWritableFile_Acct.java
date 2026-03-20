package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;
import java.util.Currency;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyWritableFile_Acct {
	
	/**
	 * @param acctID 
	 * @param id the id of the account to fetch
	 * @return A modifiable version of the account or null of not found.
	 * 
	 * @see #getAccountByID(KMMAcctID)
	 * @see #getAccountByID(KMMComplAcctID)
	 */
	KMyMoneyWritableAccount getWritableAccountByID(KMMComplAcctID acctID);

	KMyMoneyWritableAccount getWritableAccountByNameUniq(String name, boolean qualif)
	    throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @param name the name to look for
	 * @return A modifiable version of the account.
	 * 
	 * @see #getAccountByNameEx(String)
	 */
	Collection<KMyMoneyWritableAccount> getWritableAccountsByName(String name);

	/**
	 * @param type the type to look for
	 * @return A modifiable version of all accounts of the given type.
	 * 
	 * @see #getAccountsByType(org.kmymoney.api.read.KMyMoneyAccount.Type)
	 */
	Collection<KMyMoneyWritableAccount> getWritableAccountsByType(KMyMoneyAccount.Type type);

	/**
	 * 
	 * @param type
	 * @param expr
	 * @param qualif
	 * @param relaxed
	 * @return
	 * 
	 * @see #getAccountsByTypeAndName(org.kmymoney.api.read.KMyMoneyAccount.Type, String, boolean, boolean)
	 */
	Collection<KMyMoneyWritableAccount> getWritableAccountsByTypeAndName(KMyMoneyAccount.Type type, String expr, 
																		 boolean qualif, boolean relaxed);
	
	/**
	 * 
	 * @param acctID
	 * @return
	 * 
	 * @see #getAccountByID(KMMAcctID)
	 * @see #getAccountByID(KMMComplAcctID)
	 */
	KMyMoneyWritableAccount getWritableAccountByID(KMMAcctID acctID);

	/**
	 *
	 * @return a read-only collection of all accounts
	 * 
	 * @see #getAccounts()
	 */
	Collection<? extends KMyMoneyWritableAccount> getWritableAccounts();

	/**
	 *
	 * @return a read-only collection of all accounts that have no parent
	 * 
	 * @see #getRootAccount()
	 */
	Collection<? extends KMyMoneyWritableAccount> getWritableRootAccounts();

	// ----------------------------

	/**
	 * @return a new account that is already added to this file as a top-level
	 *         account
	 */
	KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
												  KMMQualifSecCurrID secCurrID, 
												  KMMComplAcctID parentID, 
												  String name);

	KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  									  KMMQualifSecID secID, 
			  									  KMMComplAcctID parentID, 
			  									  String name);

	KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  									  KMMQualifCurrID currID, 
			  									  KMMComplAcctID parentID, 
			  									  String name);

	KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  									  KMMSecID secID, 
			  									  KMMComplAcctID parentID, 
			  									  String name);

	KMyMoneyWritableAccount createWritableAccount(KMyMoneyAccount.Type type, 
			  									  Currency curr, 
			  									  KMMComplAcctID parentID, 
			  									  String name);

	/**
	 * @param acct the account to remove
	 */
	void removeAccount(KMyMoneyWritableAccount acct);

}

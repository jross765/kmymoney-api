package org.kmymoney.api.read.hlp.fil;

import java.util.List;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Acct {

	/**
	 * @param acctID the unique ID of the account to look for
	 * @return the account or null if it's not found
	 * 
	 * @see #getAccountByID(KMMAcctID)
	 */
	KMyMoneyAccount getAccountByID(KMMComplAcctID acctID);

	/**
	 * 
	 * @param acctID
	 * @return
	 * 
	 * @see #getAccountByID(KMMComplAcctID)
	 */
	KMyMoneyAccount getAccountByID(KMMAcctID acctID);
	
	/**
	 *
	 * @param acctID if null, gives all account that have no parent
	 * @return all accounts with that parent in no particular order
	 */
	List<KMyMoneyAccount> getAccountsByParentID(KMMComplAcctID acctID);

	/**
	 * warning: this function has to traverse all accounts. If it much faster to try
	 * getAccountByID() first and call this method only if the returned account does
	 * not have the right name.
	 * 
	 * @param expr search expression
	 *
	 * @param name the <strong>unqualified</strong> name to look for
	 * @return null if not found
	 * @see #getAccountByID(KMMComplAcctID)
	 * @see #getAccountsByParentID(KMMComplAcctID)
	 * @see #getAccountsByName(String, boolean, boolean)
	 */
	List<KMyMoneyAccount> getAccountsByName(String expr);

	/**
	 * @param expr search expression
	 * @param qualif Whether to search for qualified names of unqualified ones
	 * @param relaxed Whether to ignore upper/lower-case letters or not (true: case-insensitive)
	 * @return the qualified or unqualified name to look for, depending on parameter qualif.
	 */
	List<KMyMoneyAccount> getAccountsByName(String expr, boolean qualif, boolean relaxed);

	/**
	 * @param expr search expression
	 * @param qualif
	 * @return read-only account object whose name uniquely matches the expression
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneyAccount getAccountByNameUniq(String expr, boolean qualif) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * warning: this function has to traverse all accounts. If it much faster to try
	 * getAccountByID() first and call this method only if the returned account does
	 * not have the right name.
	 *
	 * @param name the regular expression of the name to look for
	 * @return null if not found
	 * @throws NoEntryFoundException 
	 * @throws TooManyEntriesFoundException 
	 * @see #getAccountByID(KMMComplAcctID)
	 * @see #getAccountsByName(String)
	 */
	KMyMoneyAccount getAccountByNameEx(String name) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * First try to fetch the account by id, then fall back to traversing all
	 * accounts to get if by it's name.
	 *
	 * @param acctID the ID to look for
	 * @param name   the name to look for if nothing is found for the ID
	 * @return null if not found
	 * @throws NoEntryFoundException 
	 * @throws TooManyEntriesFoundException 
	 */
	KMyMoneyAccount getAccountByIDorName(KMMComplAcctID acctID, String name) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * First try to fetch the account by id, then fall back to traversing all
	 * accounts to get if by it's name.
	 *
	 * @param acctID the id to look for
	 * @param name   the regular expression of the name to look for if nothing is
	 *               found for the id
	 * @return null if not found
	 * @throws NoEntryFoundException 
	 * @throws TooManyEntriesFoundException 
	 */
	KMyMoneyAccount getAccountByIDorNameEx(KMMComplAcctID acctID, String name) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * 
	 * @param type
	 * @return list of read-only account objects of the given type
	 */
	List<KMyMoneyAccount> getAccountsByType(KMyMoneyAccount.Type type);
    
	/**
	 * @param type
	 * @param acctName account name
	 * @param qualif
	 * @param relaxed
	 * @return list of read-only account objects of the given type and
     *   matching the other parameters for the name.
	 */
    List<KMyMoneyAccount> getAccountsByTypeAndName(KMyMoneyAccount.Type type, String acctName, 
    											   boolean qualif, boolean relaxed);


    /**
	 * @return all accounts
	 */
	List<KMyMoneyAccount> getAccounts();
	
    /**
     * THERE IS NO ROOT ACCOUNT!
     * 
     * @return ID of the root account
     */
	KMyMoneyAccount getRootAccount();

    /**
     * @return a read-only collection of all accounts that have no parent (the
     *         result is sorted)
     */
	List<? extends KMyMoneyAccount> getParentlessAccounts();

    /**
     * @return collection of the IDs of all top-level accounts (i.e., 
     * one level under root, if there was a root) 
     */
	List<KMMComplAcctID> getTopAccountIDs();

    /**
     * @return collection of all top-level accounts (ro-objects) (i.e., 
     * one level under root, if there was a root)
     */
	List<KMyMoneyAccount> getTopAccounts();

}

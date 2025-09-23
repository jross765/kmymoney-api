package org.kmymoney.api.write;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of KMyMoneyFile that allows writing. <br/>
 * All the instances for accounts,... it returns can be assumed
 * to implement the respective *Writable-interfaces.
 *
 * @see KMyMoneyFile
 */
public interface KMyMoneyWritableFile extends KMyMoneyFile, 
                                              KMyMoneyWritableObject
                                              // HasWritableUserDefinedAttributes
{
	public enum CompressMode {
		COMPRESS,
		DO_NOT_COMPRESS,
		GUESS_FROM_FILENAME
	}
	
	// ---------------------------------------------------------------

	/**
	 * @return true if this file has been modified.
	 */
	boolean isModified();

	/**
	 * The value is guaranteed not to be later than then the maximum of the current
	 * system-time and the modification-time in the file at the time of the last
	 * (full) read or sucessful write operation.
	 * <br> 
	 * It is thus suitable to detect if the file has been modified outside of this library.
	 * 
	 * @return the time in ms (compatible with File.lastModified) of the last
	 *         write-operation
	 */
	long getLastWriteTime();

	/**
	 * @param pB true if this file has been modified.
	 * @see {@link #isModified()}
	 */
	void setModified(boolean pB);

	/**
	 * Write the data to the given file. That file becomes the new file returned by
	 * {@link KMyMoneyFile#getKMyMoneyFile()}
	 * 
	 * @param file the file to write to
	 * @throws IOException kn io-poblems
	 */
	void writeFile(File file) throws IOException;

	void writeFile(File file, CompressMode compMode) throws IOException;

	/**
	 * @return the underlying JAXB-element
	 */
	@SuppressWarnings("exports")
	KMYMONEYFILE getRootElement();

	// ---------------------------------------------------------------

	/**
	 * @param instID 
	 * @param id the unique id of the institution to look for
	 * @return the customer or null if it's not found
	 * 
	 * @see #getInstitutionByID(KMMInstID)
	 */
	KMyMoneyWritableInstitution getWritableInstitutionByID(KMMInstID instID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new institution with no values that is already added to this file
	 */
	KMyMoneyWritableInstitution createWritableInstitution(String name);

	/**
	 *
	 * @param inst the transaction to remove.
	 */
	void removeInstitution(KMyMoneyWritableInstitution inst);

	// ---------------------------------------------------------------

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
	 * @param acctID 
	 * @param id the id of the account to fetch
	 * @return A modifiable version of the account or null of not found.
	 * 
	 * @see #getAccountByID(KMMAcctID)
	 * @see #getAccountByID(KMMComplAcctID)
	 */
	KMyMoneyWritableAccount getWritableAccountByID(KMMComplAcctID acctID);

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
	@Deprecated
	KMyMoneyWritableAccount createWritableAccount();

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

	// ---------------------------------------------------------------

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
	
	// ---------------------------------------------------------------

	/**
	 * @param pyeID the unique id of the payee to look for
	 * @return the payee or null if it's not found
	 * 
	 * @see #getPayeeByID(KMMPyeID)
	 */
	KMyMoneyWritablePayee getWritablePayeeByID(KMMPyeID pyeID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new payee with no values that is already added to this file
	 */
	KMyMoneyWritablePayee createWritablePayee(String name);

	/**
	 *
	 * @param pye the transaction to remove.
	 */
	void removePayee(KMyMoneyWritablePayee pye);

	// ---------------------------------------------------------------

	/**
	 * @param tagID the unique id of the tag to look for
	 * @return the tag or null if it's not found
	 * 
	 * @see #getTagByID(KMMTagID)
	 */
	KMyMoneyWritableTag getWritableTagByID(KMMTagID tagID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new tag with no values that is already added to this file
	 */
	KMyMoneyWritableTag createWritableTag(String name);

	/**
	 *
	 * @param tag the tag to remove.
	 */
	void removeTag(KMyMoneyWritableTag tag);

	// ---------------------------------------------------------------

	/**
	 * 
	 * @param currCode
	 * @return
	 * 
	 * @see #getCurrencyByID(String)
	 */
	KMyMoneyWritableCurrency getWritableCurrencyByID(String currCode);
	
	/**
	 * 
	 * @param qualifID
	 * @return
	 * 
	 * @see #getCurrencyByQualifID(KMMQualifCurrID)
	 */
	KMyMoneyWritableCurrency getWritableCurrencyByQualifID(KMMQualifCurrID qualifID);
	
//	List<KMyMoneyWritableCurrency> getWritableCurrencyByName(String expr);
//
//    List<KMyMoneyWritableCurrency> getWritableCurenciesByName(String expr, boolean relaxed);
//    
//    KMyMoneyWritableCurrency getWritableCurrencyByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;
    
	/**
	 * 
	 * @return
	 * 
	 * @see #getCurrencies()
	 */
	Collection<KMyMoneyWritableCurrency> getWritableCurrencies();

	// ----------------------------

	/**
	 * @param currID ISO Currency code. 
	 * @param name   Currency name
	 * @return a new transaction with no splits that is already added to this file
	 */
	KMyMoneyWritableCurrency createWritableCurrency(String currID, String name);

	/**
	 * Add a new currency.<br/>
	 * If the currency already exists, add a new price-quote for it.
	 * 
	 * @param pCmdtySpace        the name space (e.g. "GOODS" or "CURRENCY")
	 * @param pCmdtyId           the currency-name
	 * @param conversionFactor   the conversion-factor from the base-currency (EUR).
	 * @param pCmdtyNameFraction number of decimal-places after the comma
	 * @param pCmdtyName         common name of the new currency
	 */
	public void addCurrency(
			String pCmdtySpace,
			String pCmdtyId,
			FixedPointNumber conversionFactor,
			int pCmdtyNameFraction,
			String pCmdtyName);

	/**
	 *
	 * @param curr the transaction to remove.
	 */
	// void removeCurrency(KMyMoneyWritableCurrency curr);

	// ---------------------------------------------------------------

	/**
	 * @param secID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getSecurityByID(KMMSecID)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByID(KMMSecID secID);
	
	/**
	 * 
	 * @param qualifID
	 * @return
	 * 
	 * @see #getSecurityByQualifID(KMMQualifSecID)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByQualifID(KMMQualifSecID qualifID);
	
	/**
	 * 
	 * @param symb
	 * @return
	 * 
	 * @see #getSecurityBySymbol(String)
	 */
	KMyMoneyWritableSecurity getWritableSecurityBySymbol(String symb);
	
	/**
	 * 
	 * @param code
	 * @return
	 * 
	 * @see #getSecurityByCode(String)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByCode(String code);

	/**
	 * 
	 * @param expr
	 * @return
	 * 
	 * @see #getSecurityByNameUniq(String)
	 */
	List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(String expr);

	/**
	 * 
	 * @param expr
	 * @param relaxed
	 * @return
	 * 
	 * @see #getSecuritiesByName(String, boolean)
	 */
    List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(String expr, boolean relaxed);
    
    /**
     * 
     * @param expr
     * @return
     * @throws NoEntryFoundException
     * @throws TooManyEntriesFoundException
     * 
     * @see #getSecurityByNameUniq(String)
     */
    KMyMoneyWritableSecurity getWritableSecurityByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;
    
	/**
	 * @param type the type to look for
	 * @return A modifiable version of all accounts of that type.
	 * 
	 * @see #getSecuritiesByType(org.kmymoney.api.read.KMMSecCurr.Type)
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByType(KMMSecCurr.Type type);

	/**
	 * 
	 * @param type
	 * @param expr
	 * @param relaxed
	 * @return
	 * 
	 * @see #getSecuritiesByTypeAndName(org.kmymoney.api.read.KMMSecCurr.Type, String, boolean)
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr, 
																			boolean relaxed);
	
	/**
	 * @return writable versions of all transactions in the book.
	 * 
	 * @see #getSecurities()
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecurities();

	// ----------------------------

	/**
	 * @param type  Security type
	 * @param code  Security code (<strong>not</strong> the internal technical ID,
	 *              but the business ID, such as ISIN, CUSIP, etc. 
	 *              A ticker will also work, but it is <strong>not</strong> recommended,
	 *              as tickers typically are not unique, and there is a separate field
	 *              for it. 
	 * @param name  Security name
	 * @return a new transaction with no splits that is already added to this file
	 */
	KMyMoneyWritableSecurity createWritableSecurity(KMMSecCurr.Type type, String code, String name);

	/**
	 *
	 * @param sec the transaction to remove.
	 * @throws ObjectCascadeException 
	 */
	void removeSecurity(KMyMoneyWritableSecurity sec) throws ObjectCascadeException;

	// ---------------------------------------------------------------

	/**
	 * @param prcPrID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getPricePairByID(KMMPricePairID)
	 */
	KMyMoneyWritablePricePair getWritablePricePairByID(KMMPricePairID prcPrID);
	
	/**
	 * @see KMyMoneyFile#getPricePairs()
	 * @return writable versions of all prices in the book.
	 * 
	 * @see #getPricePairs()
	 */
	Collection<KMyMoneyWritablePricePair> getWritablePricePairs();

	// ----------------------------

	/**
	 * @param fromSecCurrID 
	 * @param toCurrID 
	 * @return a new price pair with no splits that is already added to this file
	 */
	KMyMoneyWritablePricePair createWritablePricePair(KMMQualifSecCurrID fromSecCurrID,
													  KMMQualifCurrID toCurrID);

	/**
	 * @param prcPrID 
	 * @return a new price pair with no splits that is already added to this file
	 */
	KMyMoneyWritablePricePair createWritablePricePair(KMMPricePairID prcPrID);

	/**
	 *
	 * @param prcPr 
	 */
	void removePricePair(KMyMoneyWritablePricePair prcPr);

	// ---------------------------------------------------------------

	/**
	 * @param prcID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getPriceByID(KMMPriceID)
	 */
	KMyMoneyWritablePrice getWritablePriceByID(KMMPriceID prcID);
	
	/**
	 * 
	 * @param secID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceBySecIDDate(KMMSecID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceBySecIDDate(KMMSecID secID, LocalDate date);
	
	/**
	 * 
	 * @param secID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifSecIDDate(KMMQualifSecID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifSecIDDate(KMMQualifSecID secID, LocalDate date);
	
	/**
	 * 
	 * @param curr
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByCurrDate(Currency, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByCurrDate(Currency curr, LocalDate date);
	
	/**
	 * 
	 * @param currID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifCurrIDDate(KMMQualifCurrID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifCurrIDDate(KMMQualifCurrID currID, LocalDate date);
	
	/**
	 * 
	 * @param secCurrID
	 * @param date
	 * @return
	 * 
	 * @see #getPriceByQualifSecCurrIDDate(KMMQualifSecCurrID, LocalDate)
	 */
	KMyMoneyWritablePrice getWritablePriceByQualifSecCurrIDDate(KMMQualifSecCurrID secCurrID, LocalDate date);
	
    // ---------------------------------------------------------------
	
	/**
	 * @return writable versions of all prices in the book.
	 * 
	 * @see #getPrices()
	 */
	Collection<KMyMoneyWritablePrice> getWritablePrices();

	// ----------------------------

	/**
	 * @param prcPr 
	 * @param date 
	 * @return a new price with no splits that is already added to this file
	 */
	KMyMoneyWritablePrice createWritablePrice(KMyMoneyPricePairImpl prcPr, LocalDate date);

	/**
	 *
	 * @param prc 
	 * @param sec the transaction to remove.
	 */
	void removePrice(KMyMoneyWritablePrice prc);

}

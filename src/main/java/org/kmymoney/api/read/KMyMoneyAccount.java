package org.kmymoney.api.read;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.kmymoney.api.read.aux.KMMAccountReconciliation;
import org.kmymoney.api.read.hlp.HasTransactions;
import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * A KMyMoney account satisfies the "normal" definition of the term in 
 * accounting (<a href="https://en.wikipedia.org/wiki/Account_(bookkeeping)">Wikipedia</a>).
 * <br>
 * You can also see it as a collection of transactions that start or end there. 
 * <br>
 * An account has a balance.  
 * <br>
 * All accounts taken together define the so-called chart of accounts,
 * organized in a tree (the top node of the tree being the root account). 
 * That means that each account may have a parent-account as well as one or 
 * several child-accounts.
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/makingmostof.mapping.html#makingmostof.mapping.accounts">KMyMoney handbook</a>
 */
public interface KMyMoneyAccount extends Comparable<KMyMoneyAccount>,
										 HasTransactions,
                                         HasUserDefinedAttributes
{

    // For the following types cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyaccount.h
    //
    /**
     * The current assignment is as follows:
     *
     * <ul>
     *   <li>Asset
     *     <ul>
     *       <li>Asset</li>
     *       <li>Checkings</li>
     *       <li>Savings</li>
     *       <li>Cash</li>
     *       <li>Currency</li>
     *       <li>Investment</li>
     *       <li>MoneyMarket</li>
     *       <li>CertificateDep</li>
     *       <li>AssetLoan</li>
     *       <li>Stock</li>
     *     </ul>
     *   </li>
     *
     *   <li>Liability
     *     <ul>
     *       <li>Liability</li>
     *       <li>CreditCard</li>
     *       <li>Loan</li>
     *     </ul>
     *   </li>
     *
     *   <li>Income
     *     <ul>
     *       <li>Income</li>
     *     </ul>
     *   </li>
     *
     *   <li>Expense
     *     <ul>
     *       <li>Expense</li>
     *     </ul>
     *   </li>
     *
     *   <li>Equity
     *     <ul>
     *       <li>Equity</li>
     *     </ul>
     *   </li>
     * </ul>
     */
    
    // For the following types cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    /*
     * Checkings,         Standard checking account
     * Savings,              Typical savings account
     * Cash,                 Denotes a shoe-box or pillowcase stuffed with cash
     * CreditCard,           Credit card accounts
     * Loan,                 Loan and mortgage accounts (liability)
     * CertificateDep,       Certificates of Deposit
     * Investment,           Investment account
     * MoneyMarket,          Money Market Account
     * Asset,                Denotes a generic asset account
     * Liability,            Denotes a generic liability account.
     * Currency,             Denotes a currency trading account.
     * Income,               Denotes an income account
     * Expense,              Denotes an expense account
     * AssetLoan,            Denotes a loan (asset of the owner of this object)
     * Stock,                Denotes an security account as sub-account for an investment
     * Equity,               Denotes an equity account e.g. opening/closing balance
    */

    public enum Type {
    	
        // ::MAGIC
    	CHECKING            (  1 ),
    	SAVINGS             (  2 ),
    	CASH                (  3 ),
    	CREDIT_CARD         (  4 ),
    	LOAN                (  5 ),
    	CERTIFICATE_DEPOSIT (  6 ),
    	INVESTMENT          (  7 ),
    	MONEY_MARKET        (  8 ),
    	ASSET               (  9 ),
    	LIABILITY           ( 10 ),
    	CURRENCY            ( 11 ),
    	INCOME              ( 12 ),
    	EXPENSE             ( 13 ),
    	ASSET_LOAN          ( 14 ),
    	STOCK               ( 15 ),
    	EQUITY              ( 16 );

    	// ---
	      
    	private int code = 0;

    	// ---
    	      
    	Type(int code) {
    	    this.code = code;
    	}
    	      
    	// ---
    		
    	public int getCode() {
    	    return code;
    	}
    		
    	public BigInteger getCodeBig() {
    	    return BigInteger.valueOf(getCode());
    	}
    		
    	// no typo!
    	public static Type valueOff(int code) {
    	    for ( Type type : values() ) {
    		if ( type.getCode() == code ) {
    		    return type;
    		}
    	    }
    		    
    	    return null;
    	}
    }
    
    // -----------------------------------------------------------------
    
    public static String SEPARATOR = ":";

    // -----------------------------------------------------------------

    /**
     * @return the unique id for that account (not meaningfull to human users)
     */
    KMMComplAcctID getID();

    /**
     * @return a user-defined description to acompany the name of the account. Can
     *         encompass many lines.
     */
    String getMemo();

    /**
     * @return the account-number
     */
    String getNumber();

    /**
     * @return user-readable name of this account. Does not contain the name of
     *         parent-accounts
     */
    String getName();

    /**
     * get name including the name of the parent.accounts.
     *
     * @return e.g. "Asset::Barvermögen::Bargeld"
     */
    String getQualifiedName();
    
    // ---------------------------------------------------------------

    /**
     * @return null if the institution is below the root
     */
    KMMInstID getInstitutionID();
    
    /**
     * @return the institution this account belongs to
     */
    KMyMoneyInstitution getInstitution();

    // ---------------------------------------------------------------

    /**
     * @return null if the account is below the root
     */
    KMMComplAcctID getParentAccountID();
    
    /**
     * @return the parent-account we are a child of or null if we are a top-level
     *         account
     */
    KMyMoneyAccount getParentAccount();

    boolean isRootAccount();

    // ----------------------------

    /**
     * The returned collection is never null and is sorted by Account-Name.
     *
     * @return all child-accounts (only one level, no grand-children etc.)
     * 
     * @see #getChildrenRecursive()
     * @see #isChildAccountRecursive(KMyMoneyAccount)
     */
    List<KMyMoneyAccount> getChildren();

    /**
     * 
     * @return all child accounts including their children, grand-children etc.
     * 
     * @see #getChildren()
     * @see #isChildAccountRecursive(KMyMoneyAccount)
     */
    List<KMyMoneyAccount> getChildrenRecursive();

    /**
     * @param account the account to test
     * 
     * @return true if this is a child of us or any child's or us.
     * 
     * @see #getChildren()
     * #see #getChildrenRecursive()
     */
    boolean isChildAccountRecursive(KMyMoneyAccount account);

    // ----------------------------

    /**
     * 
     * @return
     */
    Type getType();

    /**
     * 
     * @return fully-qualified security/currency ID
     * 
     * @throws InvalidQualifSecCurrIDException
     */
    KMMQualifSecCurrID getQualifSecCurrID() throws InvalidQualifSecCurrIDException;

    // -----------------------------------------------------------------

    /**
     * The returned list is sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * {@link KMyMoneyTransactionSplit}
     */
    List<KMyMoneyTransactionSplit> getTransactionSplits();

    /**
     * @param spltID the split-id to look for
     * @return the identified split or null
     */
    KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID);

    /**
     * Gets the last transaction-split before the given date.
     *
     * @param date if null, the last split of all time is returned
     * @return the last transaction-split before the given date
     */
    KMyMoneyTransactionSplit getLastSplitBeforeRecursive(LocalDate date);

    /**
     * @param split split to add to this transaction
     */
    void addTransactionSplit(KMyMoneyTransactionSplit split);

    // ----------------------------

    /**
     * @return true if ${@link #getTransactionSplits()}.size()>0
     */
    boolean hasTransactions();

    /**
     * @return true if ${@link #hasTransactions()} is true for this or any
     *         sub-accounts
     */
    boolean hasTransactionsRecursive();

    /**
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * {@link KMyMoneyTransaction}
     */
    List<KMyMoneyTransaction> getTransactions();

    List<KMyMoneyTransaction> getTransactions(LocalDate fromDate, LocalDate toDate);

    // -----------------------------------------------------------------

    /**
     * same as getBalance(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance
     */
    FixedPointNumber getBalance();

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(LocalDate date);

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date  if non-null transactions after this date are ignored in the
     *              calculation
     * @param after splits that are after date are added here.
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(LocalDate date, List<KMyMoneyTransactionSplit> after);

    FixedPointNumber getBalance(LocalDate date, KMMQualifSecCurrID secCurrID);

    FixedPointNumber getBalance(LocalDate date, Currency curr);

    /**
     * @param lastIncludesSplit last split to be included
     * @return the balance up to and including the given split
     */
    FixedPointNumber getBalance(KMyMoneyTransactionSplit lastIncludesSplit);

    // ----------------------------

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @return the balance formatted using the current locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceFormatted() throws InvalidQualifSecCurrIDException;

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @param lcl the locale to use (does not affect the currency)
     * @return the balance formatted using the given locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceFormatted(Locale lcl) throws InvalidQualifSecCurrIDException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance including sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    FixedPointNumber getBalanceRecursive() throws InvalidQualifSecCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    FixedPointNumber getBalanceRecursive(LocalDate date) throws InvalidQualifSecCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date              ignores transactions after the given date
     * @param secCurrID         the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidQualifSecCurrTypeException 
     * @throws InvalidQualifSecCurrIDException 
     * @see {@link KMyMoneyAccount#getBalanceRecursive(LocalDate, Currency)}
     */
    FixedPointNumber getBalanceRecursive(LocalDate date, KMMQualifSecCurrID secCurrID) throws InvalidQualifSecCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date     ignores transactions after the given date
     * @param curr	currency object; the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     * @see KMyMoneyAccount#getBalanceRecursive(LocalDate, KMMQualifSecCurrID)
     */
    FixedPointNumber getBalanceRecursive(LocalDate date, Currency curr) throws InvalidQualifSecCurrIDException;

    /**
     * 
     * @param date
     * @param secID
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidQualifSecCurrTypeException
     * @throws InvalidQualifSecCurrIDException
     * @throws KMMIDNotSetException 
     */
    FixedPointNumber getBalanceRecursive(LocalDate date, KMMSecID secID) throws KMMIDNotSetException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()). ignores transactions after the
     * current date+time
     *
     * @return the balance including sub-accounts formatted using the current locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceRecursiveFormatted() throws InvalidQualifSecCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceRecursiveFormatted(LocalDate date) throws InvalidQualifSecCurrIDException;
    
    // ---------------------------------------------------------------
    
    boolean hasReconciliations();

    List<KMMAccountReconciliation> getReconciliations();

    // ---------------------------------------------------------------

    void printTree(StringBuilder buffer, String prefix, String childrenPrefix);
}

package org.kmymoney.api.read;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import xyz.schnorxoborx.base.beanbase.TransactionSplitNotFoundException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * A financial transaction between two or more accounts.
 * <br>
 * A transaction has two or more transaction splits ({@link KMyMoneyTransactionSplit})
 * whose values normally add up to zero.
 * 
 * @see KMyMoneyTransactionSplit
 */
public interface KMyMoneyTransaction extends Comparable<KMyMoneyTransaction>,
                                             HasUserDefinedAttributes
{

    // For the following enumarations, cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h

//    // ::MAGIC
//    // ::TODO Convert to enum
//    public static final int INVEST_TYPE_UNKNOWN           = -1;
//    public static final int INVEST_TYPE_BUY_SHARES        = 0;
//    public static final int INVEST_TYPE_SELL_SHARES       = 1;
//    public static final int INVEST_TYPE_DIVIVEND          = 2;
//    public static final int INVEST_TYPE_REINVEST_DIVIVEND = 3;
//    public static final int INVEST_TYPE_YIELD             = 4;
//    public static final int INVEST_TYPE_ADD_SHARES        = 5;
//    public static final int INVEST_TYPE_REMOVE_SHARES     = 6;
//    public static final int INVEST_TYPE_SPLIT_SHARES      = 7;
//    public static final int INVEST_TYPE_INTEREST_INCOME   = 8;
//    
//    // ::MAGIC
//    // ::TODO Convert to enum
//    public static final int INVEST_ACTION_NONE              = 0;
//    public static final int INVEST_ACTION_BUY               = 1;
//    public static final int INVEST_ACTION_SELL              = 2;
//    public static final int INVEST_ACTION_REINVEST_DIVIDEND = 3;
//    public static final int INVEST_ACTION_CASH_DIVIDEND     = 4;
//    public static final int INVEST_ACTION_SHARES_IN         = 5;
//    public static final int INVEST_ACTION_SHARES_OUT        = 6;
//    public static final int INVEST_ACTION_STOCK_SPLIT       = 7;
//    public static final int INVEST_ACTION_FEES              = 8;
//    public static final int INVEST_ACTION_INTEREST          = 9;
//    public static final int INVEST_ACTION_INVALID           = 10;
    
    // ---------------------------------------------------------------
	    
    @SuppressWarnings("exports")
    TRANSACTION getJwsdpPeer();

    /**
     * The KMyMoney file is the top-level class to contain everything.
     * 
     * @return the file we are associated with
     */
    KMyMoneyFile getKMyMoneyFile();

    // ----------------------------------------------------------------

    /**
     *
     * @return the unique-id to identify this object with across name- and
     *         hirarchy-changes
     */
    /**
     * @return
     */
    KMMTrxID getID();

    /**
     * @return the user-defined description for this object (may contain multiple
     *         lines and non-ascii-characters)
     */
    String getMemo();

    /**
     *
     * @return the date the transaction was entered into the system
     */
    LocalDate getDateEntered();

    /**
     *
     * @return the date the transaction happened
     */
    LocalDate getDatePosted();

    /**
     *
     * @return date the transaction happened
     */
    String getDatePostedFormatted();

    // ----------------------------------------------------------------

    /**
     * Do not modify the returned collection!
     * 
     * @return all splits of this transaction.
     * 
     * @see #getSplitByID(KMMSpltID)
     * @see #getFirstSplit()
     * @see #getSecondSplit()
     */
    List<KMyMoneyTransactionSplit> getSplits();

    /**
     * Get a split of this transaction it's id.
     * 
     * @param spltID the id to look for
     * @return null if not found
     * 
     * @see #getSplits()
     */
    KMyMoneyTransactionSplit getSplitByID(KMMSpltID spltID);

    /**
     *
     * @return the first split of this transaction or null.
     * <br>
     * <em>Caution</em>: This only makes sense for simple transactions
     * that consist of only two splits. 
     * By no means is that guaranteed or even "normal"!
     *  
     * @throws TransactionSplitNotFoundException
     * 
     * @see #getSecondSplit()
     * @see #getSplits()
     * @see #getSplitsCount()
     */
    KMyMoneyTransactionSplit getFirstSplit() throws TransactionSplitNotFoundException;

    /**
     * @return the second split of this transaction or null.
     * <br>
     * <em>Caution</em>: This only makes sense for simple transactions
     * that consist of only two splits.
     * By no means is that guaranteed or even "normal"!
     * 
     * @throws TransactionSplitNotFoundException
     *
     * @see #getFirstSplit()
     * @see #getSplits()
     * @see #getSplitsCount()
     */
    KMyMoneyTransactionSplit getSecondSplit() throws TransactionSplitNotFoundException;

    /**
     *
     * @return the number of splits in this transaction.
     */
    int getSplitsCount();

    /**
     *
     * @return true if the sum of all splits adds up to zero.
     */
    boolean isBalanced();

    KMMQualifSecCurrID getQualifSecCurrID();

    /**
     * The result is in the currency of the transaction.<br/>
     * if the transaction is unbalanced, get sum of all split-values.
     * 
     * @return the sum of all splits
     * @see #isBalanced()
     */
    FixedPointNumber getBalance();

    /**
     * The result is in the currency of the transaction.
     * @return 
     * 
     * @see KMyMoneyTransaction#getBalance()
     */
    String getBalanceFormatted();

    /**
     * The result is in the currency of the transaction.
     * @param lcl 
     * @return 
     * 
     * @see KMyMoneyTransaction#getBalance()
     */
    String getBalanceFormatted(Locale lcl);

    /**
     * The result is in the currency of the transaction.<br/>
     * if the transaction is unbalanced, get the missing split-value to balance it.
     * 
     * @return the sum of all splits
     * @see #isBalanced()
     */
    FixedPointNumber getNegatedBalance();

    /**
     * The result is in the currency of the transaction.
     * @return 
     * 
     * @see KMyMoneyTransaction#getNegatedBalance()
     */
    String getNegatedBalanceFormatted();

    /**
     * The result is in the currency of the transaction.
     * @param lcl 
     * @return 
     * 
     * @see KMyMoneyTransaction#getNegatedBalance()
     */
    String getNegatedBalanceFormatted(Locale lcl);
}

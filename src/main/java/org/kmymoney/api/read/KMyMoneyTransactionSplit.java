package org.kmymoney.api.read;

import java.util.Collection;
import java.util.Locale;

import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * A single addition or removal of a quantity of an account's accounted-for items 
 * (i.e. currency or security), having a specific value, from that account in a transaction.
 * <br>
 * A transaction split never exists alone, but is always grouped in a transaction
 * together with at least one more split.
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/details.ledgers.split.html">KMyMoney handbook</a>
 * 
 * @see KMyMoneyTransaction
 */
public interface KMyMoneyTransactionSplit extends Comparable<KMyMoneyTransactionSplit>
{

    // For the following states cf.:
    //  - https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    //  - https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneysplit.cpp
    //    (for actual strings)

    // namespace eMyMoney::Split::Action
    public enum Action {
	
        // ::MAGIC
    	CHECK             ( "Check" ),
    	DEPOSIT           ( "Deposit" ),
    	TRANSFER          ( "Transfer" ),
    	WITHDRAWAL        ( "Withdrawal" ),
    	ATM               ( "ATM" ),
    	AMORTIZATION      ( "Amortization" ),
    	INTEREST          ( "Interest" ),
    	BUY_SHARES        ( "Buy" ),
    	SELL_SHARES       ( "Sell" ),   // actually not used
                                    // (instead, BUY_SHARES w/ neg. value)!
    	DIVIDEND          ( "Dividend" ),
    	REINVEST_DIVIDEND ( "Reinvest" ),
    	YIELD             ( "Yield" ),
    	ADD_SHARES        ( "Add" ),
    	REMOVE_SHARES     ( "Remove" ), // actually not used
                                    // (instead, ADD_SHARES w/ neg. value)!
    	SPLIT_SHARES      ( "Split" ),
    	INTEREST_INCOME   ( "IntIncome" );
	
    	// ---
	      
    	private String code = "UNSET";

    	// ---
	      
    	Action(String code) {
    		this.code = code;
    	}
	      
    	// ---
		
    	public String getCode() {
    		return code;
    	}
		
    	// no typo!
    	public static Action valueOff(String code) {
		    for ( Action act : values() ) {
		    	if ( act.getCode().equals(code) ) {
		    		return act;
			}
	    }
		    
	    return null;
    	}
    }
    
    // Also called "ReconFlag"
    public enum ReconState {

    	NOT_RECONCILED ( 0 ),
    	CLEARED        ( 1 ),
    	RECONCILED     ( 2 ),
    	FROZEN         ( 3 );
	
    	// ---
	      
    	private int index = -1;

    	// ---
	      
    	ReconState(int index) {
    		this.index = index;
    	}
	      
    	// ---
		
    	public int getIndex() {
    		return index;
    	}
		
    	// no typo!
    	public static ReconState valueOff(int index) {
    		for ( ReconState stat : values() ) {
				if ( stat.getIndex() == index ) {
					return stat;
				}
    		}
		    
    		return null;
    	}
    }
	
    // ---------------------------------------------------------------
    
    /**
     * 
     * @return the ID identify this object <em>within one transaction</em>.
     * 
     * @see #getQualifID()
     */
    KMMSpltID getID();

    /**
     * @return the fully-qualified and thus unique ID to identify this object
     * (in effect: the pair (transaction ID, split-ID)).
     * 
     * @see #getID()
     */
    KMMQualifSpltID getQualifID();

    // ----------------------------

    /**
     *
     * @return the ID of the account we transfer from/to.
     * 
     * @see #getAccount()
     */
    KMMComplAcctID getAccountID();

    /**
     * @return the account we transfer from/to.
     * 
     * @see #getAccountID()
     */
    KMyMoneyAccount getAccount();

    // ----------------------------
    
    String getNumber();

    // ----------------------------

    /**
     * 
     * @return
     * 
     * @see #getPayee()
     */
    KMMPyeID getPayeeID();
    
    /**
     * 
     * @return
     * 
     * @see #getPayeeID()
     */
    KMyMoneyPayee getPayee();
    
    // ----------------------------

    /**
     * 
     * @return
     * 
     * @see #getTags()
     */
    Collection<KMMTagID> getTagIDs();
    
    /**
     * 
     * @return
     * 
     * @see #getTagIDs()
     */
    Collection<KMyMoneyTag> getTags();
    
    // ----------------------------

    /**
     * @return the ID of the transaction this is a split of.
     * 
     * @see #getTransaction()
     */
    KMMTrxID getTransactionID();

    /**
     * @return the transaction this is a split of.
     * 
     * @see #getTransactionID()
     */
    KMyMoneyTransaction getTransaction();

    // ----------------------------

    /**
     * Get the type of association this split has with
     * an invoice's lot.
     * @return null, or one of the ACTION_xyz values defined
     */
    Action getAction();

    ReconState getReconState();
    
    @Deprecated
    ReconState getState();
    
    // ----------------------------

    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     * 
     * @see #getValueFormatted()
     * @see #getValueFormatted(Locale)
     */
    FixedPointNumber getValue();

    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     * 
     * @see #getValue()
     * @see #getValueFormatted(Locale)
     */
    String getValueFormatted();
    
    /**
     * The value is in the currency of the transaction!
     * @param lcl the locale to use
     * @return the value-transfer this represents
     * 
     * @see #getValue()
     * @see #getValueFormatted()
     */
    String getValueFormatted(Locale lcl);

    // ----------------------------

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     *         
     * @see #getAccountBalanceFormatted()
     * @see #getAccountBalanceFormatted(Locale)
     */
    FixedPointNumber getAccountBalance();

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException
     *  
     * @see #getAccountBalance()
     * @see #getAccountBalanceFormatted(Locale)
     */
    String getAccountBalanceFormatted() throws InvalidQualifSecCurrIDException;

    /**
     * @param lcl 
     * @return 
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     * @see KMyMoneyAccount#getBalanceFormatted()
     * 
     * @see #getAccountBalance()
     * @see #getAccountBalanceFormatted()
     */
    String getAccountBalanceFormatted(Locale lcl) throws InvalidQualifSecCurrIDException;

    // ----------------------------

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     * 
     * @see #getSharesFormatted()
     * @see #getSharesFormatted(Locale)
     */
    FixedPointNumber getShares();

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException
     *  
     * @see #getShares()
     * @see #getSharesFormatted(Locale)
     */
    String getSharesFormatted() throws InvalidQualifSecCurrIDException;

    /**
     * The quantity is in the currency of the account!
     * @param lcl the locale to use
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException
     * 
     * @see #getShares()
     * @see #getSharesFormatted()
     */
    String getSharesFormatted(Locale lcl) throws InvalidQualifSecCurrIDException;

    // ----------------------------

    /**
     * 
     * @return
     * 
     * @see #getPriceFormatted()
     * @see #getPriceFormatted(Locale)
     */
    FixedPointNumber getPrice();

    /**
     * 
     * @return
     * 
     * @see #getPrice()
     * @see #getPriceFormatted(Locale)
     */
    String getPriceFormatted();

    /**
     * 
     * @param lcl
     * @return
     *  
     * @see #getPrice()
     * @see #getPriceFormatted()
    */
    String getPriceFormatted(Locale lcl);

    // ----------------------------

    /**
     * @return the user-defined description for this object
     *         (may contain multiple lines and non-ascii-characters)
     */
    String getMemo();
    
}

package org.kmymoney.api.read.impl.hlp.acct;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/*
 * This is a base-class that helps implementing the KMyMoneyAccount
 * interface with its extensive number of convenience-methods.
 */
public abstract class SimpleAccount extends KMyMoneyObjectImpl 
									implements KMyMoneyAccount 
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAccount.class);

	// ---------------------------------------------------------------

	public SimpleAccount(final KMyMoneyFile kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * The returned list is sorted by the natural order of the Transaction-Splits.
	 */
	@Override
	public List<KMyMoneyTransaction> getTransactions() {
		List<KMyMoneyTransaction> retval = new ArrayList<KMyMoneyTransaction>();

		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			retval.add(splt.getTransaction());
		}

		// retval.sort(Comparator.reverseOrder()); // not necessary 

		return retval;
	}

	@Override
	public List<KMyMoneyTransaction> getTransactions(final LocalDate fromDate, final LocalDate toDate) {
		List<KMyMoneyTransaction> retval = new ArrayList<KMyMoneyTransaction>();

		for ( KMyMoneyTransaction trx : getTransactions() ) {
			 if ( ( trx.getDatePosted().isEqual( fromDate ) ||
				    trx.getDatePosted().isAfter( fromDate ) ) &&
			      ( trx.getDatePosted().isEqual( toDate ) ||
					trx.getDatePosted().isBefore( toDate ) ) ) {
				 retval.add(trx);
			 }
		}

		// retval.sort(Comparator.reverseOrder()); // not necessary 
		
		return retval;
	}

	@Override
	public boolean isChildAccountRecursive(final KMyMoneyAccount account) {
		if ( this == account ) {
			return true;
		}

		for ( KMyMoneyAccount child : getChildren() ) {
			if ( this == child ) {
				return true;
			}
			if ( child.isChildAccountRecursive(account) ) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	/**
	 * Get name including the name of the parent accounts.
	 */
	@Override
	public String getQualifiedName() {
		KMyMoneyAccount acc = getParentAccount();

		if ( acc == null || 
			 acc.getID() == getID() ) {
			KMMComplAcctID parentID = getParentAccountID();
			if ( parentID == null ) {
				return getName();
			} else if ( parentID.toString().isEmpty() ||
						parentID.toString().equals("(unset)") ||
						parentID.toString().equals("(unknown)") ) {
				return "UNKNOWN" + SEPARATOR + getName();
			} else {
				return getName();
			}
		} else {
			return acc.getQualifiedName() + SEPARATOR + getName();
		}
	}

	@Override
	public KMyMoneyAccount getParentAccount() {
		if ( isRootAccount() )
			return null;

		KMMComplAcctID parentID = getParentAccountID();
		if ( parentID == null ) {
			return null;
		} else if ( parentID.toString().isEmpty() || 
				    parentID.toString().equals("(unset)") || 
				    parentID.toString().equals("(unknown)") ) {
			return null;
		}

		return getKMyMoneyFile().getAccountByID(parentID);
	}

	@Override
	public boolean isRootAccount() {
		// Note: KMyMoney does not actually have a root account.
		// Instead, we define all top-level accounts as roots.
		// ==> We have not *one* tree, but five of them.
		if ( getID().equals(KMMComplAcctID.get(KMMComplAcctID.Top.ASSET)) || 
			 getID().equals(KMMComplAcctID.get(KMMComplAcctID.Top.LIABILITY)) || 
			 getID().equals(KMMComplAcctID.get(KMMComplAcctID.Top.INCOME)) || 
			 getID().equals(KMMComplAcctID.get(KMMComplAcctID.Top.EXPENSE)) || 
			 getID().equals(KMMComplAcctID.get(KMMComplAcctID.Top.EQUITY)) ) {
			return true;
		} else {
			return false;
		}
	}

	// ---------------------------------------------------------------

	@Override
	public FixedPointNumber getBalance() {
		return AccountBalanceHelper_FP.getBalance(this);
	}

	@Override
	public BigFraction getBalanceRat() {
		return AccountBalanceHelper_BF.getBalance(this);
	}
	
	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date) {
		return AccountBalanceHelper_FP.getBalance(date, this);
	}

	@Override
	public BigFraction getBalanceRat(final LocalDate date) {
		return AccountBalanceHelper_BF.getBalance(date, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date, List<KMyMoneyTransactionSplit> after) {
		return AccountBalanceHelper_FP.getBalance(date, after, this);
	}

	public BigFraction getBalanceRat(final LocalDate date, List<KMyMoneyTransactionSplit> after) {
		return AccountBalanceHelper_BF.getBalance(date, after, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final KMMQualifSecCurrID secCurrID) {
		return AccountBalanceHelper_FP.getBalance(date, secCurrID, this);
	}

	@Override
	public BigFraction getBalanceRat(final LocalDate date, final KMMQualifSecCurrID secCurrID) {
		return AccountBalanceHelper_BF.getBalance(date, secCurrID, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final KMMSecID secID) {
		return AccountBalanceHelper_FP.getBalance(date, secID, this);
	}

	@Override
	public BigFraction getBalanceRat(final LocalDate date, final KMMSecID secID) {
		return AccountBalanceHelper_BF.getBalance(date, secID, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final KMMCurrID currID) {
		return AccountBalanceHelper_FP.getBalance(date, currID, this);
	}

	@Override
	public BigFraction getBalanceRat(final LocalDate date, final KMMCurrID currID) {
		return AccountBalanceHelper_BF.getBalance(date, currID, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final Currency curr) {
		return AccountBalanceHelper_FP.getBalance(date, curr, this);
	}

	@Override
	public BigFraction getBalanceRat(final LocalDate date, final Currency curr) {
		return AccountBalanceHelper_BF.getBalance(date, curr, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalance(final KMyMoneyTransactionSplit lastSpltIncl) {
		return AccountBalanceHelper_FP.getBalance(lastSpltIncl, this);
	}

	@Override
	public BigFraction getBalanceRat(final KMyMoneyTransactionSplit lastSpltIncl) {
		return AccountBalanceHelper_BF.getBalance(lastSpltIncl, this);
	}

	// ----------------------------

	@Override
	public String getBalanceFormatted() {
		return AccountBalanceHelper_FP.getBalanceFormatted(this);
	}

	@Override
	public String getBalanceFormatted(final Locale lcl) {
		return AccountBalanceHelper_FP.getBalanceFormatted(lcl, this);
	}
	
	// ---------------------------------------------------------------

	@Override
	public FixedPointNumber getBalanceRecursive() {
		return AccountBalanceHelper_FP.getBalanceRecursive(this);
	}

	@Override
	public BigFraction getBalanceRecursiveRat() {
		return AccountBalanceHelper_BF.getBalanceRecursive(this);
	}

	// ---

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date) {
		return AccountBalanceHelper_FP.getBalanceRecursive(date, this);
	}

	@Override
	public BigFraction getBalanceRecursiveRat(final LocalDate date) {
		return AccountBalanceHelper_BF.getBalanceRecursive(date, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMQualifSecCurrID secCurrID) throws InvalidQualifSecCurrIDException {
		return AccountBalanceHelper_FP.getBalanceRecursive(date, secCurrID, this);
	}

	@Override
	public BigFraction getBalanceRecursiveRat(final LocalDate date, final KMMQualifSecCurrID secCurrID) throws InvalidQualifSecCurrIDException {
		return AccountBalanceHelper_BF.getBalanceRecursive(date, secCurrID, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMSecID secID) throws KMMIDNotSetException {
		return AccountBalanceHelper_FP.getBalanceRecursive(date, secID, this);
	}
	
	@Override
	public BigFraction getBalanceRecursiveRat(final LocalDate date, final KMMSecID secID) throws KMMIDNotSetException {
		return AccountBalanceHelper_BF.getBalanceRecursive(date, secID, this);
	}
	
	// ---

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency curr) {
		return AccountBalanceHelper_FP.getBalanceRecursive(date, curr, this);
	}

	@Override
	public BigFraction getBalanceRecursiveRat(final LocalDate date, final Currency curr) {
		return AccountBalanceHelper_BF.getBalanceRecursive(date, curr, this);
	}

	// ---

	@Override
	public FixedPointNumber getBalanceRecursive(final KMyMoneyTransactionSplit lastSpltIncl) {
		return AccountBalanceHelper_FP.getBalanceRecursive(lastSpltIncl, this);
	}

	@Override
	public BigFraction getBalanceRecursiveRat(final KMyMoneyTransactionSplit lastSpltIncl) {
		return AccountBalanceHelper_BF.getBalanceRecursive(lastSpltIncl, this);
	}

	// ----------------------------

	@Override
	public String getBalanceRecursiveFormatted() {
		return AccountBalanceHelper_FP.getBalanceRecursiveFormatted(this);
	}

	@Override
	public String getBalanceRecursiveFormatted(final Locale lcl) {
		return AccountBalanceHelper_FP.getBalanceRecursiveFormatted(lcl, this);
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyTransactionSplit getLastSplitBeforeRecursive(final LocalDate date) {
		if ( date == null ) {
			throw new IllegalArgumentException("argument <date> is null");
		}

		KMyMoneyTransactionSplit lastSplit = null;

		for ( KMyMoneyTransactionSplit split : getTransactionSplits() ) {
			if ( date == null || 
				 split.getTransaction().getDatePosted()
				 	.isBefore(date) ) {
				if ( lastSplit == null ||
					 split.getTransaction().getDatePosted()
						.isAfter(lastSplit.getTransaction().getDatePosted()) ) {
					lastSplit = split;
				}
			}
		}

		for ( KMyMoneyAccount account : getChildren() ) {
			KMyMoneyTransactionSplit split = account.getLastSplitBeforeRecursive(date);
			if ( split != null && 
				 split.getTransaction() != null ) {
				if ( lastSplit == null ||
					 split.getTransaction().getDatePosted()
						.isAfter(lastSplit.getTransaction().getDatePosted()) ) {
					lastSplit = split;
				}
			}
		}

		return lastSplit;
	}
	
	// ----------------------------

	@Override
	public boolean hasTransactions() {
		if ( this.getTransactionSplits() == null ) {
			return false;
		}
		
		return this.getTransactionSplits().size() > 0;
	}

	@Override
	public boolean hasTransactionsRecursive() {
		if ( this.hasTransactions() ) {
			return true;
		}

		for ( KMyMoneyAccount child : getChildren() ) {
			if ( child.hasTransactionsRecursive() ) {
				return true;
			}
		}

		return false;
	}

	// ----------------------------

	/**
	 * @return null if we are no currency but e.g. a fund
	 */
	public Currency getCurrency() {
		if ( getQualifSecCurrID().getType() != KMMQualifSecCurrID.Type.CURRENCY ) {
			throw new IllegalStateException("Account security/currency is not of type " + KMMQualifSecCurrID.Type.CURRENCY);
		}

		String kmmCurrID = getQualifSecCurrID().getCode();
		return Currency.getInstance(kmmCurrID);
	}

	// ---------------------------------------------------------------

	@Override
	public KMyMoneyTransactionSplit getTransactionSplitByID(final KMMQualifSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <spltID> is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("argument <spltID> is not set");
		}

		for ( KMyMoneyTransactionSplit split : getTransactionSplits() ) {
			if ( spltID.equals(split.getQualifID()) ) {
				return split;
			}

		}

		return null;
	}

    // -----------------------------------------------------------------

	@Override
	public int compareTo(final KMyMoneyAccount otherAcct) {
		int i = compareToByQualifiedName(otherAcct);
		if ( i != 0 ) {
			return i;
		}

		i = compareToByID(otherAcct);
		if ( i != 0 ) {
			return i;
		}

		return ("" + hashCode()).compareTo("" + otherAcct.hashCode());
	}

	private int compareToByID(final KMyMoneyAccount otherAcct) {
		return getID().toString().compareTo(otherAcct.getID().toString());
	}

	@SuppressWarnings("unused")
	private int compareToByNumber(final KMyMoneyAccount otherAcct) {
		return getNumber().toString().compareTo(otherAcct.getNumber().toString());
	}

	@SuppressWarnings("unused")
	private int compareToByName(final KMyMoneyAccount otherAcct) {
		return getName().compareTo(otherAcct.getName());
	}

	private int compareToByQualifiedName(final KMyMoneyAccount otherAcct) {
		return getQualifiedName().compareTo(otherAcct.getQualifiedName());
	}

    // -----------------------------------------------------------------

	/*
	 * Helper used in ${@link #compareTo(Object)} to compare names starting with a
	 * number.
	 */
	@SuppressWarnings("unused")
	private Long startsWithNumber(final String str) {
		if ( str == null ) {
			throw new IllegalArgumentException("argument <str> is null");
		}

//		if ( str.isBlank() ) {
//			throw new IllegalArgumentException("argument <str> is blank");
//		}

		int digitCount = 0;
		for ( int i = 0; i < str.length() && Character.isDigit(str.charAt(i)); i++ ) {
			digitCount++;
		}
		
		if ( digitCount == 0 ) {
			return null;
		}
		
		return Long.valueOf(str.substring(0, digitCount));
	}

}

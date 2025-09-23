package org.kmymoney.api.read.impl.hlp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.kmymoney.api.currency.ComplexPriceTable;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/*
 * This is a base-class that helps implementing the KMyMoneyAccount interface
 * with its extensive number of convenience-methods.<br/>
 */
public abstract class SimpleAccount extends KMyMoneyObjectImpl 
									implements KMyMoneyAccount 
{

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAccount.class);

	// ---------------------------------------------------------------

	private static NumberFormat currencyFormat = null;

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
			} else if ( parentID.toString().equals("") ||
						parentID.toString().equals("(unset)") ||
						parentID.toString().equals("(unknown)") ) {
				return getName();
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
		} else if ( parentID.toString().equals("") || 
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

	/**
	 * Same as getBalance(new Date()).<br/>
	 * Ignores transactions after the current date+time<br/>
	 * This implementation caches the result.<br/>
	 * We assume that time does never move backwards.
	 * 
	 * @see #getBalance(LocalDate)
	 */
	@Override
	public FixedPointNumber getBalance() {
		return getBalance(LocalDate.now());
	}

	@Override
	public FixedPointNumber getBalance(final LocalDate date) {
		return getBalance(date, (List<KMyMoneyTransactionSplit>) null);
	}

	/**
	 * The currency will be the one of this account.
	 */
	@Override
	public FixedPointNumber getBalance(final LocalDate date, List<KMyMoneyTransactionSplit> after) {
		FixedPointNumber balance = new FixedPointNumber();
	
		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			if ( date != null && 
				 after != null ) {
				if ( splt.getTransaction().getDatePosted().isAfter(date) ) {
					after.add(splt);
					continue;
				}
			}
	
			// the currency of the quantity is the one of the account
			if ( splt.getAction() == KMyMoneyTransactionSplit.Action.SPLIT_SHARES ) {
				balance.multiply(splt.getShares());
			} else {
				balance.add(splt.getShares());
			}
		}
	
		return balance;
	}

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null security/currency ID given");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given");
		}

		FixedPointNumber retval = getBalance(date);

		if ( retval == null ) {
			LOGGER.error("getBalance: Error creating balance!");
			return null;
		}

		// is conversion needed?
		if ( getQualifSecCurrID().equals(secCurrID) ) {
			return retval;
		}
	
		ComplexPriceTable priceTab = getKMyMoneyFile().getCurrencyTable();

		if ( priceTab == null ) {
			LOGGER.error("getBalance: Cannot transfer "
					+ "to given currency because we have no currency-table!");
			return null;
		}
	
		if ( ! priceTab.convertToBaseCurrency(retval, secCurrID) ) {
			Collection<String> currList = getKMyMoneyFile().getCurrencyTable()
					.getCurrencies(getQualifSecCurrID().getType());
			LOGGER.error("getBalance: Cannot transfer " + "from our currency '"
					+ getQualifSecCurrID().toString() + "' to the base-currency!" + " \n(we know "
					+ getKMyMoneyFile().getCurrencyTable().getNameSpaces().size() + " currency-name-spaces and "
					+ (currList == null ? "no" : "" + currList.size()) + " currencies in our name space)");
			return null;
		}
	
		if ( ! priceTab.convertFromBaseCurrency(retval, secCurrID) ) {
			LOGGER.error("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ secCurrID.toString() + "'!");
			return null;
		}
	
		return retval;
	}

	@Override
	public FixedPointNumber getBalance(final LocalDate date, final Currency curr) {
		FixedPointNumber retval = getBalance(date);

		if ( retval == null ) {
			LOGGER.warn("getBalance: Error creating balance!");
			return null;
		}

		if ( curr == null ||
			 retval.equals(new FixedPointNumber()) ) {
			return retval;
		}

		// is conversion needed?
		if ( getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			if ( getQualifSecCurrID().getCode().equals(curr.getCurrencyCode()) ) {
				return retval;
			}
		}

		ComplexPriceTable priceTab = getKMyMoneyFile().getCurrencyTable();

		if ( priceTab == null ) {
			LOGGER.warn("getBalance: Cannot transfer "
					+ "to given currency because we have no currency-table!");
			return null;
		}

		if ( ! priceTab.convertToBaseCurrency(retval, getQualifSecCurrID()) ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from our currency '"
					+ getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}

		if ( ! priceTab.convertFromBaseCurrency(retval, new KMMQualifCurrID(curr)) ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ curr + "'!");
			return null;
		}

		return retval;
	}

	@Override
	public FixedPointNumber getBalance(final KMyMoneyTransactionSplit lastIncludesSplit) {
		FixedPointNumber balance = new FixedPointNumber();
	
		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			try {
				balance.add(splt.getShares());
	
				if ( splt == lastIncludesSplit ) {
					break;
				}
			} catch ( Exception exc ) {
				// Yes, it does happen!
				LOGGER.error("getBalance: Could not add Split " + splt.getID() + 
						     " of Transaction " + splt.getTransactionID());
			}
		}
	
		return balance;
	}

	@Override
	public String getBalanceFormatted() {
		return getCurrencyFormat().format(getBalance());
	}

	@Override
	public String getBalanceFormatted(final Locale lcl) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(lcl);
		cf.setCurrency(getCurrency());
		return cf.format(getBalance());
	}

	@Override
	public FixedPointNumber getBalanceRecursive() {
		return getBalanceRecursive(LocalDate.now());
	}

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date) {
		return getBalanceRecursive(date, getQualifSecCurrID());
	}

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null security/currency ID given");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given");
		}

		// BEGIN OLD IMPL
//	    FixedPointNumber retval = getBalance(date, secCurrID);
//
//	    if (retval == null) {
//		retval = new FixedPointNumber();
//	    }
//
//	    for ( KMyMoneyAccount child : getChildren() ) {
//		retval.add(child.getBalanceRecursive(date, cmdtyCurrID));
//	    }
//
//	    return retval;
		// END OLD IMPL

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			return getBalanceRecursive(date, new KMMQualifCurrID(secCurrID.getCode()).getCurrency());
		} else {
//			return new FixedPointNumber(999999).copy().negate();
			return getBalance(date, secCurrID); // CAUTION: This assumes that under a stock account,
												// there are no children (which sounds sensible,
												// but there might be special cases)
		}
	}

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency curr)
			{

		FixedPointNumber retval = getBalance(date, curr);

		if ( retval == null ) {
			retval = new FixedPointNumber();
		}

		// CAUTION: As opposed to the sister project JGnuCashLib, the following three lines 
		// work for read-branch (KMyMoneyAccountImpl) but *not* for write-branch 
		// (KMyMoneyWritableAccountImpl). Don'nt know why, can't explain it...
//		for ( KMyMoneyAccount child : getChildren() ) {
//			retval.add(child.getBalanceRecursive(date, curr));
//		}

		// So here is another implementation which works for both read- and write-branch:
		for ( KMyMoneyAccount child : getChildrenRecursive() ) {
			try {
				retval.add( child.getBalance(date, curr) );
			} catch ( Exception exc ) {
				// Yes, it does happen sometimes!
				LOGGER.error("getBalanceRecursive: Error adding balance for child account " + child.getID());
				throw exc;
			}
		}

		return retval;
	}

	@Override
	public FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMSecID secID)
			throws KMMIDNotSetException {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("unset security ID given");
		}

		// CAUTION: This assumes that under a stock account,
		// there are no children (which sounds sensible,
		// but there might be special cases)
		return getBalance(date, new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, secID.get())); 
	}

	@Override
	public String getBalanceRecursiveFormatted() {
		return getCurrencyFormat().format(getBalanceRecursive());
	}

	@Override
	public String getBalanceRecursiveFormatted(final LocalDate date) {
		return getCurrencyFormat().format(getBalanceRecursive(date));
	}

	@Override
	public KMyMoneyTransactionSplit getLastSplitBeforeRecursive(final LocalDate date) {
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

	@Override
	public boolean hasTransactions() {
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

	/**
	 * @return null if we are no currency but e.g. a fund
	 */
	public Currency getCurrency() {
		if ( getQualifSecCurrID().getType() != KMMQualifSecCurrID.Type.CURRENCY ) {
			return null;
		}

		String kmmCurrID = getQualifSecCurrID().getCode();
		return Currency.getInstance(kmmCurrID);
	}

	public NumberFormat getCurrencyFormat() {
		// Do *not* check for null; the currency may have changed
//		if ( currencyFormat == null ) {
			if ( getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
				currencyFormat = NumberFormat.getCurrencyInstance();
				Currency currency = getCurrency();
				currencyFormat.setCurrency(currency);
			} else {
				currencyFormat = NumberFormat.getNumberInstance();
			}
//		}
			
		return currencyFormat;
	}

	@Override
	public KMyMoneyTransactionSplit getTransactionSplitByID(final KMMQualifSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("null split id given");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("unset split ID given");
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
	private Long startsWithNumber(final String s) {
		if ( s == null ) {
			throw new IllegalArgumentException("null string given");
		}

//		if ( s.trim().equals("") ) {
//			throw new IllegalArgumentException("empty string given");
//		}

		int digitCount = 0;
		for ( int i = 0; i < s.length() && Character.isDigit(s.charAt(i)); i++ ) {
			digitCount++;
		}
		
		if ( digitCount == 0 ) {
			return null;
		}
		
		return Long.valueOf(s.substring(0, digitCount));
	}

}

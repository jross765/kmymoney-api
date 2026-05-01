package org.kmymoney.api.read.impl.hlp.acct;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.pricedb.ComplexPriceTable;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.AmountFormatter_BF;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountBalanceHelper_BF
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountBalanceHelper_BF.class);

	// ---------------------------------------------------------------

	public static BigFraction getBalance(final SimpleAccount acct) {
		return getBalance(LocalDate.now(), acct);
	}

	public static BigFraction getBalance(final LocalDate date,
										 final SimpleAccount acct) {
		return getBalance(date, (List<KMyMoneyTransactionSplit>) null, acct);
	}

	// The currency will be the one of this account.
	public static BigFraction getBalance(final LocalDate date, List<KMyMoneyTransactionSplit> after,
											  final SimpleAccount acct) {
		BigFraction balance = BigFraction.ZERO;
		
		for ( KMyMoneyTransactionSplit splt : acct.getTransactionSplits() ) {
			if ( date != null ) {
				if ( splt.getTransaction().getDatePosted().isAfter(date) ) {
					if ( after != null ) {
						after.add(splt);
					}
					continue;
				}
			}
	
			// the currency of the quantity is the one of the account
			// CAUTION: BigFraction is immutable
			if ( splt.getAction() == KMyMoneyTransactionSplit.Action.SPLIT_SHARES ) {
				balance = balance.multiply(splt.getSharesRat());
			} else {
				balance = balance.add(splt.getSharesRat());
			}
		}
	
		return balance;
	}

	public static BigFraction getBalance(final LocalDate date, final KMMQualifSecCurrID secCurrID,
										 final SimpleAccount acct) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID is not set>");
		}

		BigFraction retval = getBalance(date, acct);

		if ( retval == null ) {
			LOGGER.error("getBalance: Could not create balance");
			return null;
		}

		// is conversion needed?
		if ( acct.getQualifSecCurrID().equals(secCurrID) ) {
			return retval;
		}
	
		ComplexPriceTable priceTab = acct.getKMyMoneyFile().getCurrencyTable();

		if ( priceTab == null ) {
			LOGGER.error("getBalance: Cannot transfer to given currency because we have no currency-table!");
			return null;
		}
	
		retval = priceTab.convertToBaseCurrencyRat(retval, secCurrID);
		if ( retval == null ) {
			LOGGER.error("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}
	
		retval = priceTab.convertFromBaseCurrencyRat(retval, secCurrID);
		if ( retval == null ) {
			LOGGER.error("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ secCurrID.toString() + "'!");
			return null;
		}
	
		return retval;
	}

	public static BigFraction getBalance(final LocalDate date, final KMMSecID secID,
										 final SimpleAccount acct) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID is not set>");
		}

		KMMQualifSecCurrID secCurrID;
		try {
			secCurrID = new KMMQualifSecCurrID(secID);
		} catch (KMMIDNotSetException e) {
			return null;
		}
		return getBalance(date, secCurrID, acct);
	}

	public static BigFraction getBalance(final LocalDate date, final KMMCurrID currID,
			 final SimpleAccount acct) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set>");
		}

		KMMQualifSecCurrID secCurrID;
		secCurrID = new KMMQualifSecCurrID(currID);
		return getBalance(date, secCurrID, acct);
	}

	public static BigFraction getBalance(final LocalDate date, final Currency curr,
										 final SimpleAccount acct) {
		BigFraction retval = getBalance(date, acct);

		if ( retval == null ) {
			LOGGER.warn("getBalance: Could not create balance");
			return null;
		}

		if ( curr == null ||
			 retval.equals(BigFraction.ZERO) ) {
			return retval;
		}

		// is conversion needed?
		if ( acct.getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			if ( acct.getQualifSecCurrID().getCode().equals(curr.getCurrencyCode()) ) {
				return retval;
			}
		}

		ComplexPriceTable priceTab = acct.getKMyMoneyFile().getCurrencyTable();

		if ( priceTab == null ) {
			LOGGER.warn("getBalance: Cannot transfer to given currency because we have no currency-table!");
			return null;
		}

		retval = priceTab.convertToBaseCurrencyRat(retval, acct.getQualifSecCurrID());
		if ( retval == null ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}

		retval = priceTab.convertFromBaseCurrencyRat(retval, new KMMQualifCurrID(curr));
		if ( retval == null ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from base-currenty to given currency '" + curr + "'!");
			return null;
		}

		return retval;
	}

	public static BigFraction getBalance(final KMyMoneyTransactionSplit lastSpltIncl,
										 final SimpleAccount acct) {
		BigFraction balance = BigFraction.ZERO;
	
		for ( KMyMoneyTransactionSplit splt : acct.getTransactionSplits() ) {
			try {
				if ( splt.getAction() == KMyMoneyTransactionSplit.Action.SPLIT_SHARES ) {
					// CAUTION: BigFraction is immutable
					balance = balance.multiply(splt.getSharesRat());
				} else {
					// CAUTION: BigFraction is immutable
					balance = balance.add(splt.getSharesRat());
				}
	
				if ( splt.getQualifID().equals( lastSpltIncl.getQualifID() ) ) {
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

	// ----------------------------

	public static String getBalanceFormatted(final SimpleAccount acct) {
		return getBalanceFormatted(Locale.getDefault(), acct);
	}

	public static String getBalanceFormatted(final Locale lcl,
											 final SimpleAccount acct) {
		return formatBalance( acct, getBalance(acct), lcl );
	}

	// ---------------------------------------------------------------

	public static BigFraction getBalanceRecursive(final SimpleAccount acct) {
		return getBalanceRecursive(LocalDate.now(), acct);
	}

	public static BigFraction getBalanceRecursive(final LocalDate date,
												  final SimpleAccount acct) {
		return getBalanceRecursive(date, acct.getQualifSecCurrID(), acct);
	}

	public static BigFraction getBalanceRecursive(final LocalDate date, final KMMQualifSecCurrID secCurrID,
												  final SimpleAccount acct) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			return getBalanceRecursive(date, new KMMQualifCurrID(secCurrID.getCode()).getCurrID().get(), acct);
		} else {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return getBalance(date, secID, acct); // CAUTION: This assumes that under a stock account,
												  // there are no children (which sounds sensible,
												  // but there might be special cases)
		}
	}

	public static BigFraction getBalanceRecursive(final LocalDate date, final Currency curr,
												  final SimpleAccount acct) {
		BigFraction retval = getBalance(date, curr, acct);

		if ( retval == null ) {
			retval = BigFraction.ZERO;
		}

		// CAUTION: As opposed to the sister project JGnuCashLib, the following three lines 
		// work for read-branch (KMyMoneyAccountImpl) but *not* for write-branch 
		// (KMyMoneyWritableAccountImpl). Don'nt know why, can't explain it...
//		for ( KMyMoneyAccount child : getChildren() ) {
//			retval.add(child.getBalanceRecursive(date, curr));
//		}

		// So here is another implementation which works for both read- and write-branch:
		for ( KMyMoneyAccount child : acct.getChildrenRecursive() ) {
			try {
				BigFraction addVal = child.getBalanceRat(date, curr);
				if ( addVal == null ) {
					addVal = BigFraction.ZERO;
				}
				// CAUTION: BigFraction is immutable
				retval = retval.add( addVal );
			} catch ( Exception exc ) {
				// Yes, it does happen sometimes!
				LOGGER.error("getBalanceRecursive: Error adding balance for child account " + child.getID());
				throw exc;
			}
		}

		return retval;
	}

	public static BigFraction getBalanceRecursive(final LocalDate date, final KMMSecID secID,
												  final SimpleAccount acct) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		// CAUTION: This assumes that under a stock account,
		// there are no children (which sounds sensible,
		// but there might be special cases)
		KMMQualifSecCurrID secCurrID;
		try {
			secCurrID = new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, secID.get());
		} catch (KMMIDNotSetException e) {
			return null;
		}
		return getBalance(date, secCurrID, acct); 
	}

	public static BigFraction getBalanceRecursive(final KMyMoneyTransactionSplit lastSpltIncl,
												  final SimpleAccount acct) {
		BigFraction retval = getBalance(lastSpltIncl, acct);

		if ( retval == null ) {
			retval = BigFraction.ZERO;
		}

		for ( KMyMoneyAccount child : acct.getChildren() ) {
			try {
				// CAUTION: BigFraction is immutable
				retval = retval.add( child.getBalanceRecursiveRat(lastSpltIncl) );
			} catch ( Exception exc ) {
				// Yes, it does happen sometimes!
				LOGGER.error("getBalanceRecursive: Error adding balance for child account " + child.getID());
				throw exc;
			}
		}

		return retval;
	}

	// ----------------------------

	public static String getBalanceRecursiveFormatted(final SimpleAccount acct) {
		return getBalanceRecursiveFormatted(Locale.getDefault(), acct);
	}

	public static String getBalanceRecursiveFormatted(final Locale lcl,
													  final SimpleAccount acct) {
		return formatBalance( acct, getBalanceRecursive(acct), lcl );
	}
	
	// ---------------------------------------------------------------
	// Helpers -- balance pre-computed
	
	public static String formatBalance(SimpleAccount acct, BigFraction blnc) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		if ( blnc == null ) {
			throw new IllegalArgumentException("argument <blnc> is null");
		}
		
		return formatBalance(acct, blnc, Locale.getDefault());
	}
	
	public static String formatBalance(SimpleAccount acct, BigFraction blnc, Locale lcl) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		if ( blnc == null ) {
			throw new IllegalArgumentException("argument <blnc> is null");
		}
		
		if ( lcl == null ) {
			throw new IllegalArgumentException("argument <lcl> is null");
		}
		
		return AmountFormatter_BF.formatAmount( acct.getKMyMoneyFile(),
												blnc, acct.getQualifSecCurrID(), lcl );
	}

}

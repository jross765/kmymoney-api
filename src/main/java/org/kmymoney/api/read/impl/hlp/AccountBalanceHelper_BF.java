package org.kmymoney.api.read.impl.hlp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.currency.ComplexPriceTable;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber; // sic

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
			if ( date != null && 
				 after != null ) {
				if ( splt.getTransaction().getDatePosted().isAfter(date) ) {
					after.add(splt);
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
	
		// BEGIN ::TODO: 
		// Works, but is ugly.
		// Have that symmetrical with FP-variant
		FixedPointNumber hlp = FixedPointNumber.of(retval);
		if ( ! priceTab.convertToBaseCurrency(hlp, secCurrID) ) {
			Collection<String> currList = acct.getKMyMoneyFile().getCurrencyTable()
					.getCurrencies(acct.getQualifSecCurrID().getType());
			LOGGER.error("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!" + " \n(we know "
					+ acct.getKMyMoneyFile().getCurrencyTable().getNameSpaces().size() + " currency-name-spaces and "
					+ (currList == null ? "no" : "" + currList.size()) + " currencies in our name space)");
			return null;
		}
	
		if ( ! priceTab.convertFromBaseCurrency(hlp, secCurrID) ) {
			LOGGER.error("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ secCurrID.toString() + "'!");
			return null;
		}
		retval = hlp.toBigFraction();
		// END ::TODO
	
		return retval;
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

		// BEGIN ::TODO: 
		// Works, but is ugly.
		// Have that symmetrical with FP-variant
		FixedPointNumber hlp = FixedPointNumber.of(retval);
		if ( ! priceTab.convertToBaseCurrency(hlp, acct.getQualifSecCurrID()) ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}

		if ( ! priceTab.convertFromBaseCurrency(hlp, new KMMQualifCurrID(curr)) ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ curr + "'!");
			return null;
		}
		retval = hlp.toBigFraction();
		// END ::TODO

		return retval;
	}

	public static BigFraction getBalance(final KMyMoneyTransactionSplit lastIncludesSplit,
											  final SimpleAccount acct) {
		BigFraction balance = BigFraction.ZERO;
		
		for ( KMyMoneyTransactionSplit splt : acct.getTransactionSplits() ) {
			try {
				// CAUTION: BigFraction is immutable
				balance = balance.add(splt.getSharesRat());
	
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

	// ----------------------------

	public static String getBalanceFormatted(final SimpleAccount acct) {
		Locale lcl = Locale.getDefault();
		return getBalanceFormatted(lcl, acct);
	}

	public static String getBalanceFormatted(final Locale lcl,
											 final SimpleAccount acct) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(lcl);
		cf.setCurrency(acct.getCurrency());
		return cf.format(getBalance(acct).bigDecimalValue());
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
			return getBalanceRecursive(date, new KMMQualifCurrID(secCurrID.getCode()).getCurrency(), acct);
		} else {
			return getBalance(date, secCurrID, acct); // CAUTION: This assumes that under a stock account,
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
				// CAUTION: BigFraction is immutable
				retval = retval.add( child.getBalanceRat(date, curr) );
			} catch ( Exception exc ) {
				// Yes, it does happen sometimes!
				LOGGER.error("getBalanceRecursive: Error adding balance for child account " + child.getID());
				throw exc;
			}
		}

		return retval;
	}

	public static BigFraction getBalanceRecursive(final LocalDate date, final KMMSecID secID,
													   SimpleAccount acct)
			throws KMMIDNotSetException {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		// CAUTION: This assumes that under a stock account,
		// there are no children (which sounds sensible,
		// but there might be special cases)
		return getBalance(date, new KMMQualifSecCurrID(KMMQualifSecCurrID.Type.SECURITY, secID.get()), acct); 
	}

	// ----------------------------

	public static String getBalanceRecursiveFormatted(final SimpleAccount acct) {
		Locale lcl = Locale.getDefault();
		return getBalanceRecursiveFormatted(lcl, acct);
	}

	public static String getBalanceRecursiveFormatted(final Locale lcl,
													  final SimpleAccount acct) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(lcl);
		cf.setCurrency(acct.getCurrency());
		return cf.format(getBalanceRecursive(acct).bigDecimalValue());
	}
}

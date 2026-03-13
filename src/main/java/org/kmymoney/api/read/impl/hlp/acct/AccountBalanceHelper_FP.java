package org.kmymoney.api.read.impl.hlp.acct;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.kmymoney.api.pricedb.ComplexPriceTable;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class AccountBalanceHelper_FP
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountBalanceHelper_FP.class);

	// ---------------------------------------------------------------

	public static FixedPointNumber getBalance(final SimpleAccount acct) {
		return getBalance(LocalDate.now(), acct);
	}


	public static FixedPointNumber getBalance(final LocalDate date,
											  final SimpleAccount acct) {
		return getBalance(date, (List<KMyMoneyTransactionSplit>) null, acct);
	}

	// The currency will be the one of this account.
	public static FixedPointNumber getBalance(final LocalDate date, List<KMyMoneyTransactionSplit> after,
											  final SimpleAccount acct) {
		FixedPointNumber balance = FixedPointNumber.ZERO.copy();
		
		for ( KMyMoneyTransactionSplit splt : acct.getTransactionSplits() ) {
			if ( date != null && 
				 after != null ) {
				if ( splt.getTransaction().getDatePosted().isAfter(date) ) {
					after.add(splt);
					continue;
				}
			}
	
			// the currency of the quantity is the one of the account
			// CAUTION: FixedPointNumber is mutable
			if ( splt.getAction() == KMyMoneyTransactionSplit.Action.SPLIT_SHARES ) {
				balance.multiply(splt.getShares());
			} else {
				balance.add(splt.getShares());
			}
		}
	
		return balance;
	}

	public static FixedPointNumber getBalance(final LocalDate date, final KMMQualifSecCurrID secCurrID,
											  final SimpleAccount acct) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null <secCurrID> given");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset <secCurrID> ID given");
		}

		FixedPointNumber retval = getBalance(date, acct);

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
	
		retval = priceTab.convertToBaseCurrency(retval, secCurrID);
		if ( retval == null ) {
			LOGGER.error("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}
	
		retval = priceTab.convertFromBaseCurrency(retval, secCurrID);
		if ( retval == null ) {
			LOGGER.error("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ secCurrID.toString() + "'!");
			return null;
		}
	
		return retval;
	}

	public static FixedPointNumber getBalance(final LocalDate date, final KMMSecID secID,
											  final SimpleAccount acct) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}

		KMMQualifSecCurrID secCurrID;
		try {
			secCurrID = new KMMQualifSecCurrID(secID);
		} catch (KMMIDNotSetException e) {
			return null;
		}
		return getBalance(date, secCurrID, acct);
	}

	public static FixedPointNumber getBalance(final LocalDate date, final KMMCurrID currID,
			  final SimpleAccount acct) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("argument <currID> is not set");
		}

		KMMQualifSecCurrID secCurrID;
		secCurrID = new KMMQualifSecCurrID(currID);
		return getBalance(date, secCurrID, acct);
	}

	public static FixedPointNumber getBalance(final LocalDate date, final Currency curr,
											  final SimpleAccount acct) {
		FixedPointNumber retval = getBalance(date, acct);

		if ( retval == null ) {
			LOGGER.warn("getBalance: Could not create balance");
			return null;
		}

		if ( curr == null ||
			 retval.equals(FixedPointNumber.ZERO.copy()) ) {
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

		retval = priceTab.convertToBaseCurrency(retval, acct.getQualifSecCurrID());
		if ( retval == null ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from our currency '"
					+ acct.getQualifSecCurrID().toString() + "' to the base-currency!");
			return null;
		}

		retval = priceTab.convertFromBaseCurrency(retval, new KMMQualifCurrID(curr));
		if ( retval == null ) {
			LOGGER.warn("getBalance: Cannot transfer " + "from base-currenty to given currency '"
					+ curr + "'!");
			return null;
		}

		return retval;
	}

	public static FixedPointNumber getBalance(final KMyMoneyTransactionSplit lastSpltIncl,
											  final SimpleAccount acct) {
		FixedPointNumber balance = FixedPointNumber.ZERO.copy();
	
		for ( KMyMoneyTransactionSplit splt : acct.getTransactionSplits() ) {
			try {
				if ( splt.getAction() == KMyMoneyTransactionSplit.Action.SPLIT_SHARES ) {
					// CAUTION: FixedPointNumber is mutable
					balance.multiply(splt.getShares());
				} else {
					// CAUTION: FixedPointNumber is mutable
					balance.add(splt.getShares());
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
		Locale lcl = Locale.getDefault();
		return getBalanceFormatted(lcl, acct);
	}

	public static String getBalanceFormatted(final Locale lcl,
											 final SimpleAccount acct) {
		KMMQualifSecCurrID secCurrID = acct.getQualifSecCurrID();
		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			NumberFormat nf = NumberFormat.getCurrencyInstance(lcl);
			nf.setCurrency(acct.getCurrency());
			return nf.format(getBalance(acct).getBigDecimal());
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			KMyMoneySecurity sec = acct.getKMyMoneyFile().getSecurityByID(secID);
			String secSymb = "(sec-symbol)";
			if ( sec.getSymbol() != null ) {
				secSymb = sec.getSymbol();
			} else if ( sec.getCode() != null ) {
				secSymb = sec.getCode();
			} else {
				secSymb = sec.toString();
			}
			NumberFormat nf = NumberFormat.getNumberInstance(lcl);
			return ( nf.format(getBalance(acct).getBigDecimal()) + " " + secSymb );
		}
		
		return "ERROR";
	}

	// ---------------------------------------------------------------

	public static FixedPointNumber getBalanceRecursive(final SimpleAccount acct) {
		return getBalanceRecursive(LocalDate.now(), acct);
	}

	public static FixedPointNumber getBalanceRecursive(final LocalDate date,
													   final SimpleAccount acct) {
		return getBalanceRecursive(date, acct.getQualifSecCurrID(), acct);
	}

	public static FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMQualifSecCurrID secCurrID,
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
			return getBalance(date, secCurrID, acct); // CAUTION: This assumes that under a stock account,
												      // there are no children (which sounds sensible,
												      // but there might be special cases)
		}
	}

	public static FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency curr,
													   final SimpleAccount acct) {
		FixedPointNumber retval = getBalance(date, curr, acct);

		if ( retval == null ) {
			retval = FixedPointNumber.ZERO.copy();
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
				FixedPointNumber addVal = child.getBalance(date, curr);
				if ( addVal == null ) {
					addVal = FixedPointNumber.ZERO.copy();
				}
				// CAUTION: FixedPointNumber is mutable
				retval.add( addVal );
			} catch ( Exception exc ) {
				// Yes, it does happen sometimes!
				LOGGER.error("getBalanceRecursive: Error adding balance for child account " + child.getID());
				throw exc;
			}
		}

		return retval;
	}

	public static FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMSecID secID,
													   SimpleAccount acct) {
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

	public static FixedPointNumber getBalanceRecursive(final KMyMoneyTransactionSplit lastSpltIncl,
													   final SimpleAccount acct) {
		FixedPointNumber retval = getBalance(lastSpltIncl, acct);

		if ( retval == null ) {
			retval = FixedPointNumber.ZERO.copy();
		}

		for ( KMyMoneyAccount child : acct.getChildren() ) {
			try {
				// CAUTION: FixedPointNumber is mutable
				retval.add( child.getBalanceRecursive(lastSpltIncl) );
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
		Locale lcl = Locale.getDefault();
		return getBalanceRecursiveFormatted(lcl, acct);
	}

	public static String getBalanceRecursiveFormatted(final Locale lcl,
													  final SimpleAccount acct) {
		KMMQualifSecCurrID secCurrID = acct.getQualifSecCurrID();
		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			NumberFormat cf = NumberFormat.getCurrencyInstance(lcl);
			cf.setCurrency(acct.getCurrency());
			return cf.format(getBalanceRecursive(acct).getBigDecimal());
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			KMyMoneySecurity sec = acct.getKMyMoneyFile().getSecurityByID(secID);
			String secSymb = "(sec-symbol)";
			if ( sec.getSymbol() != null ) {
				secSymb = sec.getSymbol();
			} else if ( sec.getCode() != null ) {
				secSymb = sec.getCode();
			} else {
				secSymb = sec.toString();
			}
			NumberFormat nf = NumberFormat.getNumberInstance(lcl);
			return nf.format(getBalance(acct).getBigDecimal()) + " " + secSymb;
		}
		
		return "ERROR";
	}
	
	// ---------------------------------------------------------------
	// Helpers -- balance pre-computed
	
	public static String formatBalance(SimpleAccount acct, FixedPointNumber blnc) {
		Locale lcl = Locale.getDefault();
		return formatBalance(acct, blnc, lcl);
	}
	
	public static String formatBalance(SimpleAccount acct, FixedPointNumber blnc, Locale lcl) {
		NumberFormat nf = acct.getCurrencyFormat(lcl);
    	if ( acct.getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
    		nf.setCurrency(Currency.getInstance(acct.getQualifSecCurrID().getCode()));
    		return nf.format(blnc.getBigDecimal());
    	} else {
    		return nf.format(blnc.getBigDecimal()) + " " + acct.getQualifSecCurrID().getCode().toString();
    	}
	}

}

package org.kmymoney.api.write.impl.hlp;

import java.util.Comparator;

import org.kmymoney.api.generated.ACCOUNTS;
import org.kmymoney.api.generated.BUDGETS;
import org.kmymoney.api.generated.PAYEE;
import org.kmymoney.api.generated.PRICES;
import org.kmymoney.api.generated.SCHEDULEDTX;
import org.kmymoney.api.generated.SECURITIES;
import org.kmymoney.api.generated.TRANSACTIONS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Sorter for the elements in a Gnc:Book.
 */
public class BookElementsSorter implements Comparator<Object> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(BookElementsSorter.class);

	// ---------------------------------------------------------------

	@Override
	public int compare(final Object aO1, final Object aO2) {
		// no secondary sorting
		return (Integer.valueOf(getTypeOrder(aO1)).compareTo(Integer.valueOf(getTypeOrder(aO2))));
	}

	// Return an integer for the type of entry. This is the primary ordering used.
	//
	// The order numbers defined in this function do not absolutely have to be
	// defined as they have been -- it just makes things easier as this is
	// how KMyMoney normally stores them (it can handle other variants as well,
	// however).
	private int getTypeOrder(final Object element) {
		if ( element instanceof SECURITIES ) {
			return 1;
		} else if ( element instanceof PRICES ) {
			return 2;
		} else if ( element instanceof ACCOUNTS ) {
			return 3;
		} else if ( element instanceof BUDGETS ) {
			return 4;
		} else if ( element instanceof TRANSACTIONS ) {
			return 5;
		} else if ( element instanceof SCHEDULEDTX ) {
			return 7;
		} else if ( element instanceof PAYEE ) {
			return 15;
		} else {
			throw new IllegalStateException("Unexpected element in found! <" + element.toString() + ">");
		}
	}
}

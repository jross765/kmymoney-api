package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.CURRENCY;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.impl.KMyMoneyCurrencyImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableCurrencyImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCurrencyManager extends org.kmymoney.api.read.impl.hlp.FileCurrencyManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileCurrencyManager.class);

	// ---------------------------------------------------------------

	public FileCurrencyManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyCurrencyImpl createCurrency(final CURRENCY jwsdpCurr) {
		KMyMoneyWritableCurrencyImpl curr = new KMyMoneyWritableCurrencyImpl(jwsdpCurr, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createCurrency: Generated new writable currency: " + curr.getID());
		return curr;
	}

	// ---------------------------------------------------------------

	public void addCurrency(KMyMoneyCurrency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null currency given");
		}

		currMap.put(curr.getID(), curr);
		LOGGER.debug("addCurrency: Added currency to cache: " + curr.getID());
	}

	public void removeCurrency(KMyMoneyCurrency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null currency given");
		}

		currMap.remove(curr.getID());
		LOGGER.debug("removeCurrency: Removed currency from cache: " + curr.getID());
	}

}

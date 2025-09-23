package org.kmymoney.api.read.impl.hlp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kmymoney.api.generated.CURRENCY;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.impl.KMyMoneyCurrencyImpl;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCurrencyManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileCurrencyManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<String, KMyMoneyCurrency> currMap;

	// ---------------------------------------------------------------

	public FileCurrencyManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		currMap = new HashMap<String, KMyMoneyCurrency>();

		for ( CURRENCY jwsdpCurr : pRootElement.getCURRENCIES().getCURRENCY() ) {
			try {
				KMyMoneyCurrencyImpl curr = createCurrency(jwsdpCurr);
				currMap.put(jwsdpCurr.getId(), curr);
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Currency-Entry with id=" + jwsdpCurr.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in currency map: " + currMap.size());
	}

	protected KMyMoneyCurrencyImpl createCurrency(final CURRENCY jwsdpCurr) {
		KMyMoneyCurrencyImpl curr = new KMyMoneyCurrencyImpl(jwsdpCurr, kmmFile);
		LOGGER.debug("Generated new currency: " + curr.getID());
		return curr;
	}

	// ---------------------------------------------------------------

	public KMyMoneyCurrency getCurrencyByID(String currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("null currency code given");
		}

		if ( currID.trim().equals("") ) {
			throw new IllegalArgumentException("empty currency code given");
		}

		if ( currMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyCurrency retval = currMap.get(currID);
		if ( retval == null ) {
			LOGGER.warn("getCurrencyByID: No Currency with ID '" + currID + "'. We know " + currMap.size()
					+ " currencies.");
		}

		return retval;
	}

	public KMyMoneyCurrency getCurrencyByQualifID(KMMQualifCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("null currency ID given");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("unset currency ID given");
		}

		return getCurrencyByID(currID.getCode());
	}

	public Collection<KMyMoneyCurrency> getCurrencies() {
		if ( currMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return Collections.unmodifiableCollection(currMap.values());
	}

	// ---------------------------------------------------------------

	public int getNofEntriesCurrencyMap() {
		return currMap.size();
	}

}

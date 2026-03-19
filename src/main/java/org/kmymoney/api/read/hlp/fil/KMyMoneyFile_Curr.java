package org.kmymoney.api.read.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;

public interface KMyMoneyFile_Curr {

	/**
	 * @param id the unique ID of the currency to look for
	 * @return the currency or null if it's not found
	 */
	KMyMoneyCurrency getCurrencyByID(String id);

	/**
	 * @param currID
	 * @return
	 */
	KMyMoneyCurrency getCurrencyByQualifID(KMMQualifCurrID currID);

	/**
	 * warning: this function has to traverse all securities. If it much faster to
	 * try getCurrencyById first and only call this method if the returned account
	 * does not have the right name.
	 *
	 * @param name the name to look for
	 * @return null if not found
	 * @see #getCurrencyByID(String)
	 */
	// Collection<KMyMoneyCurrency> getCurrenciesByName(String name);

	/**
	 * @return a (read-only) collection of all currencies. Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyCurrency> getCurrencies();

}

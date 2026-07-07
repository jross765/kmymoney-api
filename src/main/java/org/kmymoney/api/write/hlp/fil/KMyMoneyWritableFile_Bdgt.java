package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;
import java.util.List;

import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyWritableFile_Bdgt {

	/**
	 * @param bdgtID 
	 * @param id the unique id of the budget to look for
	 * @return the budget or null if it's not found
	 * 
	 * @see #getBudgetByID(KMMBdgtID)
	 */
	KMyMoneyWritableBudget getWritableBudgetByID(KMMBdgtID bdgtID);

	Collection<KMyMoneyWritableBudget> getWritableBudgetsByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 */
	Collection<KMyMoneyWritableBudget> getWritableBudgetsByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneyWritableBudget getWritableBudgetByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all budgets Do not modify the
	 *         returned collection!
	 */
	List<KMyMoneyWritableBudget> getWritableBudgets();

	// ----------------------------

	/**
	 * @param name 
	 * @return a new budget with no entries that is already added to this file
	 */
	KMyMoneyWritableBudget createWritableBudget(String name);

	/**
	 *
	 * @param bdgt the budget to remove.
	 */
	void removeBudget(KMyMoneyWritableBudget bdgt) throws ObjectCascadeException;

}

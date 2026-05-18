package org.kmymoney.api.read.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Bdgt {

	/**
	 * @param bdgtID the unique ID of the budget to look for
	 * @return the budget or null if it's not found
	 */
	KMyMoneyBudget getBudgetByID(KMMBdgtID bdgtID);

	/**
	 * @param expr search expression
	 * @return
	 */
	Collection<KMyMoneyBudget> getBudgetsByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 */
	Collection<KMyMoneyBudget> getBudgetsByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneyBudget getBudgetByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all institutions Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyBudget> getBudgets();

}

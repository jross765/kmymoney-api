package org.kmymoney.api.read.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Pye {

	/**
	 * @param pyeID the unique ID of the payee to look for
	 * @return the payee or null if it's not found
	 */
	KMyMoneyPayee getPayeeByID(KMMPyeID pyeID);

	/**
	 * @param expr search expression
	 * @return
	 * 
	 * @see #getPayeesByName(String, boolean)
	 */
	Collection<KMyMoneyPayee> getPayeesByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 * 
	 * @see #getPayeesByName(String)
	 */
	Collection<KMyMoneyPayee> getPayeesByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 * 
	 * @see #getPayeesByName(String)
	 */
	KMyMoneyPayee getPayeeByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all payees Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyPayee> getPayees();

}

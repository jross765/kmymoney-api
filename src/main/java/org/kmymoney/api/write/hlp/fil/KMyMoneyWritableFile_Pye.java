package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyWritableFile_Pye {
	
	/**
	 * @param pyeID the unique id of the payee to look for
	 * @return the payee or null if it's not found
	 * 
	 * @see #getPayeeByID(KMMPyeID)
	 */
	KMyMoneyWritablePayee getWritablePayeeByID(KMMPyeID pyeID);

	// ----------------------------

	/**
	 * @param expr search expression
	 * @return
	 * 
	 * @see #getPayeesByName(String, boolean)
	 */
	Collection<KMyMoneyWritablePayee> getWritablePayeesByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 * 
	 * @see #getPayeesByName(String)
	 */
	Collection<KMyMoneyWritablePayee> getWritablePayeesByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 * 
	 * @see #getPayeesByName(String)
	 */
	KMyMoneyWritablePayee getWritablePayeeByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all payees Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyWritablePayee> getWritablePayees();

	// ----------------------------

	/**
	 * @param name 
	 * @return a new payee with no values that is already added to this file
	 */
	KMyMoneyWritablePayee createWritablePayee(String name);

	/**
	 *
	 * @param pye the transaction to remove.
	 */
	void removePayee(KMyMoneyWritablePayee pye);

}

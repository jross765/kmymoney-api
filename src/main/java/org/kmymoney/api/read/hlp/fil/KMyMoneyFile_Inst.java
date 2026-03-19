package org.kmymoney.api.read.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.base.basetypes.simple.KMMInstID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Inst {

	/**
	 * @param instID the unique ID of the institution to look for
	 * @return the institution or null if it's not found
	 */
	KMyMoneyInstitution getInstitutionByID(KMMInstID instID);

	/**
	 * @param expr search expression
	 * @return
	 */
	Collection<KMyMoneyInstitution> getInstitutionsByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 */
	Collection<KMyMoneyInstitution> getInstitutionsByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneyInstitution getInstitutionByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all institutions Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyInstitution> getInstitutions();

}

package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;
import java.util.List;

import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyWritableFile_Sec {
	
	/**
	 * @param secID 
	 * @return A modifiable version of the transaction.
	 * 
	 * @see #getSecurityByID(KMMSecID)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByID(KMMSecID secID);
	
	/**
	 * 
	 * @param qualifID
	 * @return
	 * 
	 * @see #getSecurityByQualifID(KMMQualifSecID)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByQualifID(KMMQualifSecID qualifID);
	
	/**
	 * 
	 * @param symb
	 * @return
	 * 
	 * @see #getSecurityBySymbol(String)
	 */
	KMyMoneyWritableSecurity getWritableSecurityBySymbol(String symb);
	
	/**
	 * 
	 * @param code
	 * @return
	 * 
	 * @see #getSecurityByCode(String)
	 */
	KMyMoneyWritableSecurity getWritableSecurityByCode(String code);

	/**
	 * 
	 * @param expr
	 * @return
	 * 
	 * @see #getSecurityByNameUniq(String)
	 */
	List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(String expr);

	/**
	 * 
	 * @param expr
	 * @param relaxed
	 * @return
	 * 
	 * @see #getSecuritiesByName(String, boolean)
	 */
    List<KMyMoneyWritableSecurity> getWritableSecuritiesByName(String expr, boolean relaxed);
    
    /**
     * 
     * @param expr
     * @return
     * @throws NoEntryFoundException
     * @throws TooManyEntriesFoundException
     * 
     * @see #getSecurityByNameUniq(String)
     */
    KMyMoneyWritableSecurity getWritableSecurityByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;
    
	/**
	 * @param type the type to look for
	 * @return A modifiable version of all accounts of that type.
	 * 
	 * @see #getSecuritiesByType(org.kmymoney.api.read.KMMSecCurr.Type)
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByType(KMMSecCurr.Type type);

	/**
	 * 
	 * @param type
	 * @param expr
	 * @param relaxed
	 * @return
	 * 
	 * @see #getSecuritiesByTypeAndName(org.kmymoney.api.read.KMMSecCurr.Type, String, boolean)
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr, 
																			boolean relaxed);
	
	/**
	 * @return writable versions of all transactions in the book.
	 * 
	 * @see #getSecurities()
	 */
	Collection<KMyMoneyWritableSecurity> getWritableSecurities();

	// ----------------------------

	/**
	 * @param type  Security type
	 * @param code  Security code (<strong>not</strong> the internal technical ID,
	 *              but the business ID, such as ISIN, CUSIP, etc. 
	 *              A ticker will also work, but it is <strong>not</strong> recommended,
	 *              as tickers typically are not unique, and there is a separate field
	 *              for it. 
	 * @param name  Security name
	 * @return a new transaction with no splits that is already added to this file
	 */
	KMyMoneyWritableSecurity createWritableSecurity(KMMSecCurr.Type type, String code, String name);

	/**
	 *
	 * @param sec the transaction to remove.
	 * @throws ObjectCascadeException 
	 */
	void removeSecurity(KMyMoneyWritableSecurity sec) throws ObjectCascadeException;

}

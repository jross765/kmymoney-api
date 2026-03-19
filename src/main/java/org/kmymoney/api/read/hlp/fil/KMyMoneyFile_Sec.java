package org.kmymoney.api.read.hlp.fil;

import java.util.List;

import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Sec {

	/**
	 * @param secID the unique ID of the security to look for
	 * @return the security or null if it's not found
	 */
	KMyMoneySecurity getSecurityByID(KMMSecID secID);

	/**
	 * @param secIDStr
	 * @return
	 */
	@Deprecated
	KMyMoneySecurity getSecurityByID(String secIDStr);

	/**
	 * @param qualifID
	 * @return
	 * @throws InvalidQualifSecCurrIDException
	 * @throws InvalidQualifSecCurrTypeException
	 */
	@Deprecated
	KMyMoneySecurity getSecurityByQualifID(String qualifID)	throws InvalidQualifSecCurrIDException;

    // ------------------------------
	
	// ::EMPTY

    // ------------------------------

	/**
	 * @param secID
	 * @return
	 */
	KMyMoneySecurity getSecurityByQualifID(KMMQualifSecID qualifID);

    // --- CODE ---------------------------

	/**
	 * The symbol is usually the ticker, but need not necessarily be so.
	 * 
	 * @param symb
	 * @return
	 * @throws InvalidQualifSecCurrIDException
	 * @throws InvalidQualifSecCurrTypeException
	 */
	KMyMoneySecurity getSecurityBySymbol(String symb) throws InvalidQualifSecCurrIDException;

	/**
	 * By ISIN/CUSIP/SEDOL/WKN...
	 * 
	 * @param code
	 * @return
	 * @throws InvalidQualifSecCurrIDException
	 * @throws InvalidQualifSecCurrTypeException
	 */
	KMyMoneySecurity getSecurityByCode(String code) throws InvalidQualifSecCurrIDException;

    // --- NAME ---------------------------

	/**
	 * @param expr search expression
	 * @return
	 */
	List<KMyMoneySecurity> getSecuritiesByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 */
	List<KMyMoneySecurity> getSecuritiesByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneySecurity getSecurityByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * 
	 * @param type
	 * @return
	 */
	List<KMyMoneySecurity> getSecuritiesByType(KMMSecCurr.Type type);
    
	/**
	 * @param type
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 */
	List<KMyMoneySecurity> getSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr,
													  boolean relaxed);

	/**
	 * @return
	 */
	List<KMyMoneySecurity> getSecurities();

}

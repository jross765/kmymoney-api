package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.SECURITY;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneySecurityImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public class FileSecurityManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileSecurityManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<KMMSecID, KMyMoneySecurity> secMap;
	protected Map<String, KMMSecID>           symbMap;
	protected Map<String, KMMSecID>           codeMap;

	// ---------------------------------------------------------------

	public FileSecurityManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		secMap = new HashMap<KMMSecID, KMyMoneySecurity>();
		symbMap = new HashMap<String, KMMSecID>();
		codeMap = new HashMap<String, KMMSecID>();

		for ( SECURITY jwsdpSec : pRootElement.getSECURITIES().getSECURITY() ) {
			try {
				KMyMoneySecurityImpl sec = createSecurity(jwsdpSec);
				secMap.put(sec.getID(), sec);
				symbMap.put(sec.getSymbol(), new KMMSecID(jwsdpSec.getId()));
				codeMap.put(sec.getCode(), new KMMSecID(jwsdpSec.getId()));
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Security-Entry with id=" + jwsdpSec.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in security map: " + secMap.size());
	}

	protected KMyMoneySecurityImpl createSecurity(final SECURITY jwsdpSec) {
		KMyMoneySecurityImpl sec = new KMyMoneySecurityImpl(jwsdpSec, kmmFile);
		LOGGER.debug("createSecurity: Generated new security: " + sec.getID());
		return sec;
	}

	// ---------------------------------------------------------------

	public KMyMoneySecurity getSecurityByID(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("unset security ID given");
		}

		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneySecurity retval = secMap.get(secID);
		if ( retval == null ) {
			LOGGER.warn("getSecurityByID: No Security with ID '" + secID + "'. We know " + secMap.size() + " securities.");
		}

		return retval;
	}

	public KMyMoneySecurity getSecurityByID(final String idStr) {
		if ( idStr == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( idStr.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

		KMMSecID secID = new KMMSecID(idStr);
		return getSecurityByID(secID);
	}

	public KMyMoneySecurity getSecurityByQualifID(final KMMQualifSecID secID) {
		return getSecurityByID(secID.getCode());
	}

	public KMyMoneySecurity getSecurityByQualifID(final String qualifIDStr) {
		if ( qualifIDStr == null ) {
			throw new IllegalArgumentException("null security ID given");
		}

		if ( qualifIDStr.trim().equals("") ) {
			throw new IllegalArgumentException("empty security ID given");
		}

		KMMQualifSecID secID = KMMQualifSecID.parse(qualifIDStr);
		return getSecurityByQualifID(secID);
	}

	public KMyMoneySecurity getSecurityBySymbol(final String symb) {
		if ( symb == null ) {
			throw new IllegalArgumentException("null symbol given");
		}

		if ( symb.trim().equals("") ) {
			throw new IllegalArgumentException("empty symbol given");
		}

		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		if ( symbMap.size() != secMap.size() ) {
			// ::CHECK
			// CAUTION: Don't throw an exception, at least not in all cases,
			// because this is not necessarily an error: Only if the KMyMoney
			// file does not contain quotes for foreign currencies (i.e. currency-
			// commodities but only security-commodities is this an error.
			// throw new IllegalStateException("Sizes of root elements are not equal");
			LOGGER.debug("getSecurityBySymbol: Sizes of root elements are not equal.");
		}

		KMMSecID qualifID = symbMap.get(symb);
		if ( qualifID == null ) {
			LOGGER.warn("getSecurityBySymbol: No Security with symbol '" + symb + "'. We know " + symbMap.size()
					+ " securities in map 2.");
		}

		KMyMoneySecurity retval = secMap.get(qualifID);
		if ( retval == null ) {
			LOGGER.warn("getSecurityBySymbol: Security with qualified ID '" + qualifID + "'. We know " + secMap.size()
					+ " securities in map 1.");
		}

		return retval;
	}

	public KMyMoneySecurity getSecurityByCode(final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("null code given");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("empty code given");
		}

		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		if ( codeMap.size() != secMap.size() ) {
			// ::CHECK
			// CAUTION: Don't throw an exception, at least not in all cases,
			// because this is not necessarily an error: Only if the KMyMoney
			// file does not contain quotes for foreign currencies (i.e. currency-
			// commodities but only security-commodities is this an error.
			// throw new IllegalStateException("Sizes of root elements are not equal");
			LOGGER.debug("getSecurityByCode: Sizes of root elements are not equal.");
		}

		KMMSecID qualifID = codeMap.get(code);
		if ( qualifID == null ) {
			LOGGER.warn("getSecurityByCode: No Security with symbol '" + code + "'. We know " + codeMap.size()
					+ " securities in map 2.");
		}

		KMyMoneySecurity retval = secMap.get(qualifID);
		if ( retval == null ) {
			LOGGER.warn("getSecurityByCode: No Security with qualified ID '" + qualifID + "'. We know " + secMap.size()
					+ " securities in map 1.");
		}

		return retval;
	}

	public List<KMyMoneySecurity> getSecuritiesByName(final String expr) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		return getSecuritiesByName(expr, true);
	}

	public List<KMyMoneySecurity> getSecuritiesByName(final String expr, final boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		List<KMyMoneySecurity> result = new ArrayList<KMyMoneySecurity>();

		for ( KMyMoneySecurity sec : getSecurities() ) {
			if ( sec.getName() != null ) // yes, that can actually happen!
			{
				if ( relaxed ) {
					if ( sec.getName().toLowerCase().contains(expr.trim().toLowerCase()) ) {
						result.add(sec);
					}
				} else {
					if ( sec.getName().equals(expr) ) {
						result.add(sec);
					}
				}
			}
		}

		result.sort(Comparator.naturalOrder()); 

		return result;
	}

	public KMyMoneySecurity getSecurityByNameUniq(final String expr)
			throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		List<KMyMoneySecurity> cmdtyList = getSecuritiesByName(expr, false);
		if ( cmdtyList.size() == 0 )
			throw new NoEntryFoundException();
		else if ( cmdtyList.size() > 1 )
			throw new TooManyEntriesFoundException();
		else
			return cmdtyList.get(0);
	}

	public List<KMyMoneySecurity> getSecuritiesByType(KMMSecCurr.Type type) {
		List<KMyMoneySecurity> result = new ArrayList<KMyMoneySecurity>();

		for ( KMyMoneySecurity sec : getSecurities() ) {
			if ( sec.getType() == type ) {
				result.add(sec);
			}
		}

		return result;
	}

	public List<KMyMoneySecurity> getSecuritiesByTypeAndName(KMMSecCurr.Type type, String expr, boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty name given");
		}

		List<KMyMoneySecurity> result = new ArrayList<KMyMoneySecurity>();

		for ( KMyMoneySecurity sec : getSecuritiesByName(expr, relaxed) ) {
			if ( sec.getType() == type ) {
				result.add(sec);
			}
		}

		return result;
	}

	public List<KMyMoneySecurity> getSecurities() {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		ArrayList<KMyMoneySecurity> temp = new ArrayList<KMyMoneySecurity>(secMap.values());
		Collections.sort(temp);
		
		return Collections.unmodifiableList(temp);
	}

	// ---------------------------------------------------------------

	public int getNofEntriesSecurityMap() {
		return secMap.size();
	}

}

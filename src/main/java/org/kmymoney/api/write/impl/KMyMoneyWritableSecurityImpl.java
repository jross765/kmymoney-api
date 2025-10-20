package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.SECURITY;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.KMyMoneySecurityImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.api.write.impl.hlp.HasWritableUserDefinedAttributesImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneySecurityImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableSecurityImpl extends KMyMoneySecurityImpl 
                                          implements KMyMoneyWritableSecurity
{

	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableSecurityImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

	// ---------------------------------------------------------------

	/**
	 * Please use ${@link KMyMoneyWritableFile#createWritableSecurity()}.
	 *
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 */
	@SuppressWarnings("exports")
	public KMyMoneyWritableSecurityImpl(final SECURITY jwsdpPeer, final KMyMoneyWritableFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * Please use ${@link KMyMoneyWritableFile#createWritableSecurity()}.
	 *
	 * @param file the file we belong to
	 * @param id   the ID we shall have
	 */
	protected KMyMoneyWritableSecurityImpl(final KMyMoneyWritableFileImpl file) {
		super(createSecurity_int(file, file.getNewSecurityID()), file);
	}

	public KMyMoneyWritableSecurityImpl(final KMyMoneySecurityImpl sec) {
		super(sec.getJwsdpPeer(), sec.getKMyMoneyFile());
	}

	// ---------------------------------------------------------------

	/**
	 * Delete this security and remove it from the file.
	 * @throws ObjectCascadeException 
	 *
	 * @see KMyMoneyWritableSecurity#remove()
	 */
	public void remove() throws ObjectCascadeException {
		SECURITY peer = jwsdpPeer;
		(getKMyMoneyFile()).getRootElement().getSECURITIES().getSECURITY().remove(peer);
		(getKMyMoneyFile()).removeSecurity(this);
	}

	// ---------------------------------------------------------------

	/**
	 * Creates a new Transaction and add's it to the given KMyMoney file Don't
	 * modify the ID of the new transaction!
	 *
	 * @param file the file we will belong to
	 * @param newID the ID we shall have
	 * @return a new jwsdp-peer already entered into th jwsdp-peer of the file
	 */
	protected static SECURITY createSecurity_int(
			final KMyMoneyWritableFileImpl file, 
			final KMMSecID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}

		SECURITY jwsdpSec = file.createSecurityType();

		jwsdpSec.setId(newID.toString());
		jwsdpSec.setType(Const.SEC_TYPE_DEFAULT.getCode());
		jwsdpSec.setSymbol(Const.SEC_SYMBOL_DEFAULT);
		jwsdpSec.setName("no name given");
		jwsdpSec.setRoundingMethod(Const.SEC_ROUNDMETH_DEFAULT.getCode());
		jwsdpSec.setPp(BigInteger.valueOf(Const.SEC_PP_DEFAULT));
		jwsdpSec.setSaf(BigInteger.valueOf(Const.SEC_SAF_DEFAULT));
		jwsdpSec.setTradingMarket(null);
		jwsdpSec.setTradingCurrency(file.getDefaultCurrencyID());
		
		// ::TODO: Key-value pair for ISIN

        file.getRootElement().getSECURITIES().getSECURITY().add(jwsdpSec);
		file.setModified(true);

		LOGGER.debug("createSecurity_int: Created new security (core):" + jwsdpSec.getId());

		return jwsdpSec;
	}

	// ---------------------------------------------------------------

	@Override
	public List<KMyMoneyWritableAccount> getWritableStockAccounts() {
		List<KMyMoneyWritableAccount> result = new ArrayList<KMyMoneyWritableAccount>();
		
		for ( KMyMoneyAccount acct : getStockAccounts() ) {
			KMyMoneyWritableAccountImpl newAcct = new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) acct, true);
			result.add(newAcct);
		}
		
		return result;
	}

	// ---------------------------------------------------------------

	@Override
	public void setSymbol(final String symb) {
		if ( symb == null ) {
			throw new IllegalArgumentException("null symbol given!");
		}

		if ( symb.trim().length() == 0 ) {
			throw new IllegalArgumentException("empty symbol given!");
		}

		String oldSymb = getSymbol();
		jwsdpPeer.setSymbol(symb);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("symbol", oldSymb, symb);
		}
	}

	@Override
	public void setCode(final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("null code given!");
		}

		if ( code.trim().length() == 0 ) {
			throw new IllegalArgumentException("empty code given!");
		}

		String oldCode = getCode();
		if ( oldCode == null ) {
			addUserDefinedAttribute(Const.KVP_KEY_SEC_SECURITY_ID, code);
		} else {
			setUserDefinedAttribute(Const.KVP_KEY_SEC_SECURITY_ID, code); // sic, no try-catch-block here
		}
		
		// Already done:
		// getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("code", oldCode, code);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setType(final KMMSecCurr.Type type) {
		setTypeBigInt(type.getCode());
	}

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #setType(org.kmymoney.api.read.KMMSecCurr.Type)} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #setType(org.kmymoney.api.read.KMMSecCurr.Type)
     */
	public void setTypeBigInt(final BigInteger type) {
		if ( type == null ) {
			throw new IllegalArgumentException("null type given!");
		}

		if ( type.intValue() < 0 ) {
			throw new IllegalArgumentException("type < 0 given!"); // sic, 0 is allowed!
		}

		if ( type.intValue() == KMMSecCurr.Type.UNSET ) {
			throw new IllegalArgumentException("unset type given!");
		}

		BigInteger oldType = getTypeBigInt();
		jwsdpPeer.setType(type);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("type", oldType, type);
		}
	}

	@Override
	public void setName(String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given!");
		}

		if ( name.trim().length() == 0 ) {
			throw new IllegalArgumentException("empty name given!");
		}

		String oldName = getName();
		jwsdpPeer.setName(name);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("name", oldName, name);
		}
	}

	@Override
	public void setPP(final BigInteger pp) {
		if ( pp == null ) {
			throw new IllegalArgumentException("null PP given!");
		}

		if ( pp.intValue() <= 0 ) {
			throw new IllegalArgumentException("PP is <= 0");
		}
		
		BigInteger oldPP = getPP();
		jwsdpPeer.setPp(pp);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("pp", oldPP, pp);
		}
	}

	@Override
	public void setRoundingMethod(final KMMSecCurr.RoundingMethod meth) {
		if ( meth == null ) {
			throw new IllegalArgumentException("argument <meth> is null");
		}

		if ( meth == KMMSecCurr.RoundingMethod.UNKNOWN ) {
			throw new IllegalArgumentException("argumen <meth> is set to " + KMMSecCurr.RoundingMethod.UNKNOWN);
		}
		
		KMMSecCurr.RoundingMethod oldMeth = getRoundingMethod();
		jwsdpPeer.setRoundingMethod(meth.getCode());
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("roundingMethod", oldMeth, meth);
		}
	}

	@Override
	public void setSAF(final BigInteger saf) {
		if ( saf == null ) {
			throw new IllegalArgumentException("null SAF given!");
		}

		if ( saf.intValue() <= 0 ) {
			throw new IllegalArgumentException("SAF is <= 0");
		}
		
		BigInteger oldSAF = getSAF();
		jwsdpPeer.setSaf(saf);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("saf", oldSAF, saf);
		}
	}

	@Override
	public void setTradingCurrency(final KMMQualifCurrID currID) {
		if ( currID == null ) {
			throw new IllegalArgumentException("unset currency given!");
		}

		if ( ! currID.isSet() ) {
			throw new IllegalArgumentException("unset currency given!");
		}

		KMMQualifCurrID oldCurrID = getTradingCurrency();
		jwsdpPeer.setTradingCurrency(currID.getCurrency().getCurrencyCode());
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("tradingCurrency", oldCurrID, currID);
		}
	}

	@Override
	public void setTradingMarket(final String mkt) {
		if ( mkt == null ) {
			throw new IllegalArgumentException("null trading market given!");
		}

		if ( mkt.trim().length() == 0 ) {
			throw new IllegalArgumentException("empty trading market given!");
		}

		String oldMkt = getTradingMarket();
		jwsdpPeer.setTradingMarket(mkt);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange("tradingMarket", oldMkt, mkt);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

	// ---------------------------------------------------------------

	@Override
	public void addUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			ObjectFactory fact = getKMyMoneyFile().getObjectFactory();
			KEYVALUEPAIRS newKVPs = fact.createKEYVALUEPAIRS();
			jwsdpPeer.setKEYVALUEPAIRS(newKVPs);
		}
		
		HasWritableUserDefinedAttributesImpl
			.addUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	@Override
	public void removeUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.removeUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             	name);
	}

	@Override
	public void setUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.setUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	// -----------------------------------------------------------------

	@Override
    public String toString() {
		String result = "KMyMoneyWritableSecurityImpl ";

		result += "[id=" + getID();
		result += ", symbol='" + getSymbol() + "'";

		try {
			result += ", type=" + getType();
		} catch (Exception e) {
			result += ", type=" + "ERROR";
		}

		result += ", name='" + getName() + "'";
		result += ", pp=" + getPP();

		try {
			result += ", rounding-method=" + getRoundingMethod();
		} catch (Exception e) {
			result += ", rounding-method=" + "ERROR";
		}

		result += ", saf=" + getSAF();

		try {
			result += ", trading-curr=" + getTradingCurrency();
		} catch (Exception e) {
			result += ", trading-curr=" + "ERROR";
		}

		result += ", trading-mkt='" + getTradingMarket() + "'";

		result += "]";

		return result;
    }

}

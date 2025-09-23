package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import java.util.Currency;

import org.kmymoney.api.generated.CURRENCY;
import org.kmymoney.api.read.KMMSecCurr.RoundingMethod;
import org.kmymoney.api.read.UnknownRoundingMethodException;
import org.kmymoney.api.read.impl.KMyMoneyCurrencyImpl;
import org.kmymoney.api.write.KMyMoneyWritableCurrency;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyCurrencyImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableCurrencyImpl extends KMyMoneyCurrencyImpl 
                                          implements KMyMoneyWritableCurrency 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableCurrencyImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableCurrency()}.
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
	public KMyMoneyWritableCurrencyImpl(
			final CURRENCY jwsdpPeer,
			final KMyMoneyWritableFileImpl file) {
    	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableCurrency()}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected KMyMoneyWritableCurrencyImpl(final KMyMoneyWritableFileImpl file, final Currency curr) {
    	super(createCurrency_int(file, curr), file);
    }

//    protected KMyMoneyWritableCurrencyImpl(final KMyMoneyWritableFileImpl file, final String currCode) {
//    	super(createCurrency_int(file, Currency.getInstance(currCode), file);
//    }

    public KMyMoneyWritableCurrencyImpl(final KMyMoneyCurrencyImpl curr) {
    	super(curr.getJwsdpPeer(), curr.getKMyMoneyFile());
    }

    // ---------------------------------------------------------------

	/**
     * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
     * the ID of the new transaction!
     *
     * @param file the file we will belong to
     * @param guid the ID we shall have
     * @return a new jwsdp-peer already entered into th jwsdp-peer of the file
     */
    protected static CURRENCY createCurrency_int(
    		final KMyMoneyWritableFileImpl file, 
    		final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null currency code given");
		}

//        ObjectFactory factory = file.getObjectFactory();
    
        CURRENCY jwsdpCurr = file.createCurrencyType();
    
        jwsdpCurr.setId(curr.getCurrencyCode());
        jwsdpCurr.setSymbol(curr.getSymbol());
        jwsdpCurr.setName(curr.getDisplayName());
    
        file.getRootElement().getCURRENCIES().getCURRENCY().add(jwsdpCurr);
        file.setModified(true);
    
        LOGGER.debug("createCurrency_int: Created new currency (core): " + jwsdpCurr.getId());
        
        return jwsdpCurr;
    }

    // ---------------------------------------------------------------

    /**
     * The KMyMoney file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
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

    /**
     * @see KMyMoneyWritableCurrency#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("argument <name> is null");
    	}

    	if ( name.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <name> is emptys");
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
	public void setSymbol(String symb) {
    	if ( symb == null ) {
    		throw new IllegalArgumentException("argument <symb> is null");
    	}

    	if ( symb.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <symb> is empty");
    	}

    	String oldSymb = getSymbol();
    	jwsdpPeer.setSymbol(symb);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("symbol", oldSymb, symb);
    	}
	}

	@Override
	public void setPP(final BigInteger pp) {
		if ( pp == null ) {
			throw new IllegalArgumentException("argument <pp> is null");
		}

		if ( pp.intValue() <= 0 ) {
			throw new IllegalArgumentException("argument <pp> is <= 0");
		}
		
		BigInteger oldPP = getPP();
		jwsdpPeer.setPp(pp);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
		    propertyChangeSupport.firePropertyChange("pp", oldPP, pp);
		}
	}

	@Override
	public void setRoundingMethod(RoundingMethod mthd) {
		if ( mthd == null ) {
			throw new IllegalArgumentException("argument <mthd> is null");
		}

		RoundingMethod oldMthd;
		try {
			oldMthd = getRoundingMethod();
		} catch (UnknownRoundingMethodException e) {
			LOGGER.error("setRoundingMethod: Could not get old rounding method. Assuming " + RoundingMethod.UNKNOWN);
			oldMthd = RoundingMethod.UNKNOWN;
		}
		jwsdpPeer.setRoundingMethod(mthd.getCode());
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
		    propertyChangeSupport.firePropertyChange("rounding-method", oldMthd, mthd);
		}
	}

	@Override
	public void setSAF(final BigInteger saf) {
		if ( saf == null ) {
			throw new IllegalArgumentException("argument <saf> is null");
		}

		if ( saf.intValue() <= 0 ) {
			throw new IllegalArgumentException("argument <saf> is <= 0");
		}
		
		BigInteger oldSAF = getSAF();
		jwsdpPeer.setSaf(saf);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
		    propertyChangeSupport.firePropertyChange("saf", oldSAF, saf);
		}
	}

	@Override
	public void getSCF(BigInteger scf) {
		if ( scf == null ) {
			throw new IllegalArgumentException("argument <scf> is null");
		}

		if ( scf.intValue() <= 0 ) {
			throw new IllegalArgumentException("argument <scf> is <= 0");
		}
		
		BigInteger oldSCF = getSCF();
		jwsdpPeer.setPp(scf);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
		    propertyChangeSupport.firePropertyChange("scf", oldSCF, scf);
		}
	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
    	String result = "KMyMoneyWritableCurrencyImpl ";
    	
    	result += "[id=" + getID();
    	result += ", symbol='" + getSymbol() + "'";
    	
    	result += ", name='" + getName() + "'";
    	result += ", pp=" + getPP();
    	result += ", saf=" + getSAF();
    	result += ", scf=" + getSCF();
    	
    	try {
    	    result += ", rounding-method=" + getRoundingMethod();
    	} catch (UnknownRoundingMethodException e) {
    	    result += ", rounding-method=" + "ERROR";
    	}
    	
    	result += "]";
    	
    	return result;
    }

}

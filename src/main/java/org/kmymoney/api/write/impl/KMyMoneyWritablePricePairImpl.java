package org.kmymoney.api.write.impl;

import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMMPriceImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritablePricePairImpl extends KMyMoneyPricePairImpl 
                                           implements KMyMoneyWritablePricePair 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritablePricePairImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMyMoneyWritablePricePairImpl(
    		final PRICEPAIR jwsdpPeer,
    		final KMyMoneyWritableFile file) {
    	super(jwsdpPeer, file);
    }

    public KMyMoneyWritablePricePairImpl(
    		final KMMQualifSecCurrID fromSecCurrID, 
    		final KMMQualifCurrID toCurrID,
    		final KMyMoneyWritableFileImpl file) {
    	super(createPricePair_int(fromSecCurrID, toCurrID,
    							  file), 
    		  file);
    }

    public KMyMoneyWritablePricePairImpl(
    		final KMMPricePairID prcPrID,
    		final KMyMoneyWritableFileImpl file) {
    	super(createPricePair_int(prcPrID.getFromSecCurr(), prcPrID.getToCurr(),
    							  file), 
    		  file);
    }

    public KMyMoneyWritablePricePairImpl(final KMyMoneyPricePairImpl prcPr) {
    	super(prcPr.getJwsdpPeer(), prcPr.getKMyMoneyFile());
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
    
    private static PRICEPAIR createPricePair_int(
    		final KMMQualifSecCurrID fromSecCurrID, 
    		final KMMQualifCurrID toCurrID,
    		final KMyMoneyWritableFileImpl file) {
	
        // ObjectFactory fact = file.getObjectFactory();
        
        PRICEPAIR jwsdpPrcPr = file.createPricePairType();

        jwsdpPrcPr.setFrom(fromSecCurrID.getCode());
        jwsdpPrcPr.setTo(toCurrID.getCode());
    
        file.getRootElement().getPRICES().getPRICEPAIR().add(jwsdpPrcPr);
        file.setModified(true);
    
        KMMPricePairID prcPrID = new KMMPricePairID(fromSecCurrID, toCurrID);
        LOGGER.debug("createPricePair_int: Created new price pair (core): " + prcPrID.toString());
        // LOGGER.debug("createPricePair_int: Created new price pair (core): " + jwsdpPrcPr);
    
        return jwsdpPrcPr;
    }

    // ---------------------------------------------------------------

	@Override
	public void set(final KMyMoneyPricePair prcPr) {
		if ( prcPr == null )
			throw new IllegalArgumentException("null price pair given");

		setFromSecCurrQualifID(prcPr.getFromSecCurrQualifID());
		setToCurrencyQualifID(prcPr.getToCurrencyQualifID());
		
		getWritableKMyMoneyFile().setModified(true);
	}
	
	@Override
	public void setID(final KMMPricePairID currPr) {
		if ( currPr == null )
			throw new IllegalArgumentException("null currency pair given");

		if ( ! currPr.isSet() )
			throw new IllegalArgumentException("unset currency pair given");

		setFromSecCurrQualifID(currPr.getFromSecCurr());
		setToCurrencyQualifID(currPr.getToCurr());

		getWritableKMyMoneyFile().setModified(true);
	}
    
    // ---------------------------------------------------------------

	@Override
	public void setFromSecCurrStr(String secCurr) {
		if ( secCurr == null )
			throw new IllegalArgumentException("null security/currency given");

		if ( secCurr.trim().length() == 0 )
			throw new IllegalArgumentException("empty security/currency given");

		setFromCurrencyQualifID(new KMMQualifCurrID(secCurr));
	}

	@Override
	public void setToCurrStr(String curr) {
		if ( curr == null )
			throw new IllegalArgumentException("null currency given");

		if ( curr.trim().length() == 0 )
			throw new IllegalArgumentException("empty currency given");

		setToCurrencyQualifID(new KMMQualifCurrID(curr));
	}

    // ----------------------------
    
	@Override
    public void setFromSecCurrQualifID(final KMMQualifSecCurrID qualifID) {
		if ( qualifID == null )
			throw new IllegalArgumentException("null ID given");

		if ( ! qualifID.isSet() )
			throw new IllegalArgumentException("unset ID given");

    	jwsdpPeer.setFrom(qualifID.getCode());
    	getWritableKMyMoneyFile().setModified(true);
    }

	@Override
	public void setFromSecurityQualifID(final KMMQualifSecID qualifID) {
		if ( qualifID == null )
			throw new IllegalArgumentException("null ID given");

		if ( ! qualifID.isSet() )
			throw new IllegalArgumentException("unset ID given");

		jwsdpPeer.setFrom(qualifID.getCode());
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public void setFromCurrencyQualifID(final KMMQualifCurrID qualifID) {
		if ( qualifID == null )
			throw new IllegalArgumentException("null ID given");

		if ( ! qualifID.isSet() )
			throw new IllegalArgumentException("unset ID given");

		jwsdpPeer.setFrom(qualifID.getCode());
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public void setFromSecurity(final KMyMoneySecurity sec) {
		if ( sec == null )
			throw new IllegalArgumentException("null ID given");

		jwsdpPeer.setFrom(sec.getCode());
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public void setFromCurrencyCode(final String code) {
		if ( code == null )
			throw new IllegalArgumentException("null code given");

		if ( code.trim().length() == 0 )
			throw new IllegalArgumentException("empty code given");

		setFromCurrencyQualifID(new KMMQualifCurrID(code));
	}

	@Override
	public void setFromCurrency(final KMyMoneyCurrency curr) {
		if ( curr == null )
			throw new IllegalArgumentException("null ID given");

		jwsdpPeer.setFrom(curr.getID());
		getWritableKMyMoneyFile().setModified(true);
	}

    // ----------------------------
    
	@Override
	public void setToCurrencyQualifID(KMMQualifCurrID qualifID) {
		if ( qualifID == null )
			throw new IllegalArgumentException("null ID given");

		if ( ! qualifID.isSet() )
			throw new IllegalArgumentException("unset ID given");

		if ( qualifID.getType() != KMMQualifSecCurrID.Type.CURRENCY )
			throw new InvalidQualifSecCurrTypeException("Is not a currency: " + qualifID.toString());

		jwsdpPeer.setTo(qualifID.getCode());
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public void setToCurrencyCode(String code) {
		if ( code == null )
			throw new IllegalArgumentException("null code given");

		if ( code.trim().length() == 0 )
			throw new IllegalArgumentException("empty code given");

		setToCurrencyQualifID(new KMMQualifCurrID(code));
	}

	@Override
	public void setToCurrency(KMyMoneyCurrency curr) {
		if ( curr == null )
			throw new IllegalArgumentException("null ID given");

		jwsdpPeer.setTo(curr.getID());
		setToCurrencyQualifID(curr.getQualifID());
	}

    // ---------------------------------------------------------------
    
//    @Override
//    public String toString() {
//	return toStringShort();
//    }
//
//    public String toStringShort() {
//	return getFromSecCurrStr() + ";" + getToCurrStr();
//    }

    public String toStringLong() {
		String result = "KMMWritablePricePairImpl [";

		try {
			result += "from-sec-curr=" + getFromSecCurrQualifID();
		} catch (Exception e) {
			result += "from-sec-curr=" + "ERROR";
		}

		try {
			result += ", to-curr=" + getToCurrencyQualifID();
		} catch (Exception e) {
			result += ", to-curr=" + "ERROR";
		}

		result += "]";

		return result;
    }

}

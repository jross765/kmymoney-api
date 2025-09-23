package org.kmymoney.api.read.impl;

import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyPricePairImpl extends KMyMoneyObjectImpl 
								   implements KMyMoneyPricePair 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyPricePairImpl.class);

    // -----------------------------------------------------------

    // The JWSDP-object we are wrapping.
    protected final PRICEPAIR jwsdpPeer;

    // -----------------------------------------------------------

    /**
     * @param newPeer the JWSDP-object we are wrapping.
     * @param kmmFile 
     */
    @SuppressWarnings("exports")
    public KMyMoneyPricePairImpl(final PRICEPAIR newPeer, final KMyMoneyFile kmmFile) {
    	super(kmmFile);
		
    	this.jwsdpPeer = newPeer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public PRICEPAIR getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // -----------------------------------------------------------
    
    @Override
    public KMMPricePairID getID() {
    	if ( jwsdpPeer.getFrom() == null ||
    		 jwsdpPeer.getTo() == null ) {
    		throw new IllegalStateException("from-sec-curr and/or to-curr of JWSDP peer is/are null");
    	}
    	
    	return new KMMPricePairID(jwsdpPeer.getFrom(), jwsdpPeer.getTo());
    }

    // -----------------------------------------------------------
    
    @Override
    public String getFromSecCurrStr() {
    	return jwsdpPeer.getFrom();
    }

    @Override
    public String getToCurrStr() {
    	return jwsdpPeer.getTo();
    }

    // ---------------------------------------------------------------
    
    @Override
    public KMMQualifSecCurrID getFromSecCurrQualifID() {
		String secCurrID = getFromSecCurrStr();

		KMMQualifSecCurrID result = null;
		if ( secCurrID.startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) ) {
			result = new KMMQualifSecID(secCurrID);
		} else {
			result = new KMMQualifCurrID(secCurrID);
		}

		return result;
    }

    @Override
    public KMMQualifSecID getFromSecurityQualifID() {
		KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
		if ( secCurrID.getType() != KMMQualifSecCurrID.Type.SECURITY )
			throw new InvalidQualifSecCurrTypeException();

		return new KMMQualifSecID(secCurrID);
    }

    @Override
    public KMMQualifCurrID getFromCurrencyQualifID() {
		KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
		if ( secCurrID.getType() != KMMQualifSecCurrID.Type.CURRENCY )
			throw new InvalidQualifSecCurrTypeException();

		return new KMMQualifCurrID(secCurrID);
    }

    @Override
    public KMyMoneySecurity getFromSecurity() {
		KMMQualifSecID secID = getFromSecurityQualifID();

		KMyMoneySecurity cmdty = getKMyMoneyFile().getSecurityByQualifID(secID);

		return cmdty;
    }
    
    @Override
    public String getFromCurrencyCode() {
		return getFromCurrencyQualifID().getCurrency().getCurrencyCode();
    }

    @Override
    public KMyMoneyCurrency getFromCurrency() {
		KMMQualifCurrID currID = getFromCurrencyQualifID();

		KMyMoneyCurrency curr = getKMyMoneyFile().getCurrencyByQualifID(currID);

		return curr;
    }
    
    // ----------------------------
    
    @Override
    public KMMQualifCurrID getToCurrencyQualifID() {
		String secCurrID = getToCurrStr();

		KMMQualifCurrID result = null;
		if ( secCurrID.startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) ) {
			throw new InvalidQualifSecCurrTypeException();
		} else {
			result = new KMMQualifCurrID(secCurrID);
		}

		return result;
    }

    @Override
    public String getToCurrencyCode() {
    	return getToCurrencyQualifID().getCode();
    }

    @Override
    public KMyMoneyCurrency getToCurrency() {
		KMMQualifCurrID currID = getToCurrencyQualifID();

		KMyMoneyCurrency curr = getKMyMoneyFile().getCurrencyByQualifID(currID);

		return curr;
    }

    // -----------------------------------------------------------
    
    @Override
    public List<KMyMoneyPrice> getPrices() {
		List<KMyMoneyPrice> result = new ArrayList<KMyMoneyPrice>();

		for ( PRICE prc : jwsdpPeer.getPRICE() ) {
			KMyMoneyPrice newPrc = new KMyMoneyPriceImpl(this, prc, getKMyMoneyFile());
			result.add(newPrc);
		}

		try {
			LOGGER.debug("getPrices: Found " + result.size() + " prices for KMMPricePair " + getID());
		} catch (Exception e) {
			LOGGER.debug("getPrices: Found " + result.size() + " prices for KMMPricePair " + "ERROR");
		}

		return result;
    }

    // -----------------------------------------------------------
    
    public boolean equals(KMyMoneyPricePair other) {
		if ( !getFromSecCurrQualifID().toString().equals(other.getFromSecCurrQualifID().toString()) )
			return false;

		if ( !getToCurrencyQualifID().toString().equals(other.getToCurrencyQualifID().toString()) )
			return false;

		return true;
    }
    
    // -----------------------------------------------------------------

    @Override
	public int compareTo(final KMyMoneyPricePair otherPrc) {
		int i = getID().toString().compareTo(otherPrc.getID().toString());
		if ( i != 0 ) {
			return i;
		}

		return ("" + hashCode()).compareTo("" + otherPrc.hashCode());
	}
	
    // -----------------------------------------------------------------
    
    @Override
    public String toString() {
    	return toStringShort();
    }

    public String toStringShort() {
    	return getFromSecCurrStr() + ";" + getToCurrStr();
    }

    public String toStringLong() {
		String result = "KMMPricePairImpl [";

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

package org.kmymoney.api.read.impl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kmymoney.api.generated.CURRENCY;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.UnknownRoundingMethodException;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyCurrencyImpl extends KMyMoneyObjectImpl 
								  implements KMyMoneyCurrency 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyCurrencyImpl.class);

    // ---------------------------------------------------------------
    
    // the JWSDP-object we are facading.
    protected final CURRENCY jwsdpPeer;

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMyMoneyCurrencyImpl(
	    final CURRENCY peer, 
	    final KMyMoneyFile kmmFile) {
    	super(kmmFile);
    	
    	jwsdpPeer = peer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public CURRENCY getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    @Override
    public String getID() {
    	return jwsdpPeer.getId();
    }

    @Override
    public KMMQualifCurrID getQualifID() {
    	return new KMMQualifCurrID(getID());
    }

    @Override
    public String getSymbol() {
    	return jwsdpPeer.getSymbol();
    }

    // ---------------------------------------------------------------

    @Override
    public String getName() {
    	return jwsdpPeer.getName();
    }

    @Override
    public BigInteger getPP() {
    	return jwsdpPeer.getPp();
    }

    @Override
    public KMMSecCurr.RoundingMethod getRoundingMethod() throws UnknownRoundingMethodException {
    	BigInteger methodVal = jwsdpPeer.getRoundingMethod(); 
    	return KMMSecCurr.RoundingMethod.valueOff(methodVal.intValue());
    }

    @Override
    public BigInteger getSAF() {
    	return jwsdpPeer.getSaf();
    }

    @Override
    public BigInteger getSCF() {
    	return jwsdpPeer.getScf();
    }

    // ---------------------------------------------------------------

    @Override
    public List<KMyMoneyPrice> getQuotes() {
		List<KMyMoneyPrice> result = new ArrayList<KMyMoneyPrice>();

		Collection<KMyMoneyPrice> prices = getKMyMoneyFile().getPrices();
		for ( KMyMoneyPrice price : prices ) {
			try {
				if ( price.getFromSecCurrQualifID().toString().equals(getQualifID().toString()) ) {
					result.add(price);
				}
			} catch (Exception exc) {
				LOGGER.error("getQuotes: Could not check price " + price.toString());
			}
		}

		return result;
    }

    @Override
    public KMyMoneyPrice getYoungestQuote() {
		KMyMoneyPrice result = null;

		LocalDate youngestDate = LocalDate.of(1970, 1, 1); // ::MAGIC
		for ( KMyMoneyPrice price : getQuotes() ) {
			if ( price.getDate().isAfter(youngestDate) ) {
				result = price;
				youngestDate = price.getDate();
			}
		}

		return result;
    }

    // -----------------------------------------------------------------

    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplits() {
    	return getKMyMoneyFile().getTransactionSplitsByQualifCurrID(getQualifID());
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneyCurrencyImpl ";

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

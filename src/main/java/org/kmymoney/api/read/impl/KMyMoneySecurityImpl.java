package org.kmymoney.api.read.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.generated.SECURITY;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.UnknownSecurityTypeException;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneySecurityImpl extends KMyMoneyObjectImpl 
								  implements KMyMoneySecurity 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneySecurityImpl.class);

    // ---------------------------------------------------------------
    
    // the JWSDP-object we are facading.
    protected final SECURITY jwsdpPeer;

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMyMoneySecurityImpl(
	    final SECURITY peer, 
	    final KMyMoneyFile kmmFile) {
    	super(kmmFile);

    	jwsdpPeer = peer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public SECURITY getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    @Override
    public KMMSecID getID() {
    	return new KMMSecID(jwsdpPeer.getId());
    }

    @Override
    public KMMQualifSecID getQualifID() {
    	if ( getID() == null ) {
   			return null;
   		}
   	
    	return new KMMQualifSecID(getID());
    }
    
    @Override
    public String getSymbol() {
    	return jwsdpPeer.getSymbol();
    }

    /**
     * {@inheritDoc}
     */
    public String getCode() {
    	try {
    		return getUserDefinedAttribute(Const.KVP_KEY_SEC_SECURITY_ID);
    	} catch (KVPListDoesNotContainKeyException exc) {
    		return null;
    	}
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public KMMSecCurr.Type getType() {
    	if ( getTypeBigInt() == null ) {
    		return null;
    	}
    	
    	BigInteger typeVal = getTypeBigInt(); 
		return KMMSecCurrImpl.getType(typeVal.intValue());
    }

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #getType()} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #getType()
     */
    public BigInteger getTypeBigInt() {
    	return jwsdpPeer.getType(); 
    }

    @Override
    public String getName() {
    	if ( jwsdpPeer.getName() == null ) {
			return ""; // sic, important for compareToByName()
		}
	
    	return jwsdpPeer.getName();
    }

    @Override
    public BigInteger getPP() {
    	return jwsdpPeer.getPp();
    }

    @Override
    public KMMSecCurr.RoundingMethod getRoundingMethod() {
    	if ( jwsdpPeer.getRoundingMethod() == null ) {
    		return null;
    	}
    	
    	BigInteger methodVal = jwsdpPeer.getRoundingMethod(); 
    	return KMMSecCurr.RoundingMethod.valueOff(methodVal.intValue());
    }

    @Override
    public BigInteger getSAF() {
    	return jwsdpPeer.getSaf();
    }

    @Override
    public KMMQualifCurrID getTradingCurrency() {
    	return new KMMQualifCurrID(jwsdpPeer.getTradingCurrency());
    }

    @Override
    public String getTradingMarket() {
    	return jwsdpPeer.getTradingMarket();
    }
    
    // ---------------------------------------------------------------

	@Override
	public List<KMyMoneyAccount> getStockAccounts() {
		List<KMyMoneyAccount> result = new ArrayList<KMyMoneyAccount>();
		
		for ( KMyMoneyAccount acct : getKMyMoneyFile().getAccountsByType(KMyMoneyAccount.Type.STOCK) ) {
			KMMQualifSecCurrID secCurrID = acct.getQualifSecCurrID();
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			if ( this.getID().equals(secID) ) {
				result.add(acct);
			}
		}
		
		return result;
	}

    // ---------------------------------------------------------------

    @Override
    public List<KMyMoneyPrice> getQuotes() {
    	return getKMyMoneyFile().getPricesBySecID(getID());
    }

    @Override
    public KMyMoneyPrice getYoungestQuote() {
    	List<KMyMoneyPrice> qutList = getQuotes();
    	if ( qutList.size() == 0 )
    		return null;
    	
    	return qutList.get(0);
    }

    // -----------------------------------------------------------------

    @Override
    public List<KMyMoneyTransactionSplit> getTransactionSplits() {
    	return getKMyMoneyFile().getTransactionSplitsByQualifSecID(getQualifID());
    }

    // ---------------------------------------------------------------

	/**
	 * @param name the name of the user-defined attribute
	 * @return the value or null if not set
	 */
	public String getUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeCore(kvpList, name);
	}

    /**
     * @return all keys that can be used with
     *         ${@link #getUserDefinedAttribute(String)}}.
     */
	public List<String> getUserDefinedAttributeKeys() {
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeKeysCore(kvpList);
	}

    // -----------------------------------------------------------------

	@Override
	public int compareTo(final KMyMoneySecurity otherSec) {
		int i = compareToByName(otherSec);
		if ( i != 0 ) {
			return i;
		}

		i = compareToByQualifID(otherSec);
		if ( i != 0 ) {
			return i;
		}

		return ("" + hashCode()).compareTo("" + otherSec.hashCode());
	}
	
//	private int compareToByID(final KMyMoneySecurity otherCmdty) {
//		return getID().toString().compareTo(otherCmdty.getID().toString());
//	}

	private int compareToByQualifID(final KMyMoneySecurity otherSec) {
		return getQualifID().toString().compareTo(otherSec.getQualifID().toString());
	}

	private int compareToByName(final KMyMoneySecurity otherSec) {
		return getName().compareTo(otherSec.getName());
	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneySecurityImpl ";

		result += "[id=" + getID();
		result += ", symbol='" + getSymbol() + "'";

		try {
			result += ", type=" + getType();
		} catch (UnknownSecurityTypeException e) {
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

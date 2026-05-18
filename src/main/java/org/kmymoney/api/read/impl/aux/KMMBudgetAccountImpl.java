package org.kmymoney.api.read.impl.aux;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.PERIOD;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMMBudgetAccountImpl extends KMyMoneyObjectImpl 
							      implements KMMBudgetAccount 
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMMBudgetAccountImpl.class);

    // -----------------------------------------------------------

    protected final ACCOUNT jwsdpPeer;

    // -----------------------------------------------------------
    
    protected KMyMoneyBudget parent = null;

    // -----------------------------------------------------------

    /**
     * @param parent 
     * @param newPeer the JWSDP-object we are wrapping.
     * @param kmmFile 
     */
    @SuppressWarnings("exports")
    public KMMBudgetAccountImpl(
    		final KMyMoneyBudget parent, 
    		final ACCOUNT newPeer, 
    		final KMyMoneyFile kmmFile) {
    	super(kmmFile);
		
    	this.parent    = parent;
    	this.jwsdpPeer = newPeer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public ACCOUNT getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // -----------------------------------------------------------------

	@Override
	public KMMAcctID getAcctID() {
		return new KMMAcctID(jwsdpPeer.getId());
	}

	@Override
	public Level getLevel() {
		if ( jwsdpPeer.getBudgetlevel() == null ) {
			return null;
		}
		
		if ( jwsdpPeer.getBudgetlevel().equals("monthly") ) {
			return Level.MONTHLY;
		} else if ( jwsdpPeer.getBudgetlevel().equals("yearly") ) {
			return Level.YEARLY;
		} else if ( jwsdpPeer.getBudgetlevel().equals("monthbymonth") ) {
			return Level.MONTH_BY_MONTH;
		}
		
		return null; // Compiler happy
	}

	@Override
	public boolean includesSubAccounts() {
		if ( jwsdpPeer.getBudgetsubaccounts() == null ) {
			return false;
		}
		
		if ( jwsdpPeer.getBudgetsubaccounts() == BigInteger.ONE ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<KMMBudgetPeriod> getPeriods() {
		List<KMMBudgetPeriod> result = new ArrayList<KMMBudgetPeriod>();

		for ( PERIOD bdgtPrd : jwsdpPeer.getPERIOD() ) {
			KMMBudgetPeriodImpl newBdgtPrd = new KMMBudgetPeriodImpl(this, bdgtPrd, getKMyMoneyFile());
			result.add(newBdgtPrd);
		}

		try {
			LOGGER.debug("getPeriods: Found " + result.size() + " periods for account " + getAcctID());
		} catch (Exception e) {
			LOGGER.debug("getPeriods: Found " + result.size() + " periods for account " + "ERROR");
		}

		return result;
	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMMBudgetAccountImpl [";

		try {
			result += "account-id=" + getAcctID();
		} catch (Exception e) {
			result += "account-id=" + "ERROR";
		}

		try {
			result += ", level='" + getLevel() + "'";
		} catch (Exception e) {
			result += ", level=" + "ERROR";
		}

		try {
			result += ", includes-sub-accounts=" + includesSubAccounts();
		} catch (Exception e) {
			result += ", includes-sub-accounts=" + "ERROR";
		}

		result += "]";

		return result;
    }

}

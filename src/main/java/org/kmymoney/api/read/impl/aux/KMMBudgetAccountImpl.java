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
	private static final Logger LOGGER = LoggerFactory.getLogger(KMMBudgetAccountImpl.class);

    // -----------------------------------------------------------

    protected final ACCOUNT jwsdpPeer;

    // -----------------------------------------------------------
    
    protected final KMyMoneyBudget parent;

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
	public KMyMoneyBudget getParent() {
		return parent;
	}

    // -----------------------------------------------------------------

	@Override
	public KMMAcctID getAcctID() {
		return new KMMAcctID(jwsdpPeer.getId());
	}

    // -----------------------------------------------------------------

	@Override
	public Level getLevel() {
		if ( jwsdpPeer.getBudgetlevel() == null ) {
			return null;
		}
		
		if ( jwsdpPeer.getBudgetlevel().isBlank() ) {
			return null;
		}
		
		return Level.valueOff( jwsdpPeer.getBudgetlevel() );
	}

	@Override
	public boolean includesSubAccounts() {
		if ( jwsdpPeer.getBudgetsubaccounts() == null ) {
			return false;
		}
		
		if ( jwsdpPeer.getBudgetsubaccounts().equals( BigInteger.ONE ) ) {
			return true;
		} else {
			return false;
		}
	}

	// -----------------------------------------------------------------

	@Override
	public boolean hasPeriods() {
		return ( getPeriods().size() > 0 );
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

    protected void addPeriod(final KMMBudgetPeriodImpl bdgtPrd) {
    	if ( ! jwsdpPeer.getPERIOD().contains( bdgtPrd.getJwsdpPeer() ) ) {
    		jwsdpPeer.getPERIOD().add( bdgtPrd.getJwsdpPeer() );
    	}
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

		try {
			result += ", nof-periods=" + getPeriods().size();
		} catch (Exception e) {
			result += ", nof-periods=" + getAcctID();
		}

		result += "]";

		return result;
    }

}

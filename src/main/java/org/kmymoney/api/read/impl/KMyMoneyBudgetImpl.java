package org.kmymoney.api.read.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.BUDGET;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.aux.KMMBudgetAccountImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyBudgetImpl extends KMyMoneyObjectImpl 
								implements KMyMoneyBudget 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyBudgetImpl.class);

    // -----------------------------------------------------------

    // The JWSDP-object we are wrapping.
    protected final BUDGET jwsdpPeer;

    // -----------------------------------------------------------

    /**
     * @param newPeer the JWSDP-object we are wrapping.
     * @param kmmFile 
     */
    @SuppressWarnings("exports")
    public KMyMoneyBudgetImpl(final BUDGET newPeer, final KMyMoneyFile kmmFile) {
    	super(kmmFile);
		
    	this.jwsdpPeer = newPeer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public BUDGET getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // -----------------------------------------------------------
    
    @Override
    public KMMBdgtID getID() {
    	if ( jwsdpPeer.getId() == null ) {
    		throw new IllegalStateException("id of JWSDP peer is null");
    	}
    	
    	return new KMMBdgtID(jwsdpPeer.getId());
    }

    // -----------------------------------------------------------
    
	@Override
	public String getName() {
    	if ( jwsdpPeer.getName() == null ) {
    		return null;
    	}

    	return jwsdpPeer.getName();
	}

	@Override
    public LocalDate getStart() {
		if ( jwsdpPeer.getStart() == null )
			return null;

		XMLGregorianCalendar cal = jwsdpPeer.getStart();
		try {
			return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
		} catch (Exception e) {
			IllegalStateException ex = new IllegalStateException("unparsable date '" + cal + "' in price!");
			ex.initCause(e);
			throw ex;
		}
    }

    @Override
    public String getStartStr() {
		if ( jwsdpPeer.getStart() == null )
			return null;

		return jwsdpPeer.getStart().toString();
    }

    // -----------------------------------------------------------
    
    @Override
    public List<KMMBudgetAccount> getAccounts() {
		List<KMMBudgetAccount> result = new ArrayList<KMMBudgetAccount>();

		for ( ACCOUNT bdgtAcct : jwsdpPeer.getACCOUNT() ) {
			KMMBudgetAccount newBdgtAcct = new KMMBudgetAccountImpl(this, bdgtAcct, getKMyMoneyFile());
			result.add(newBdgtAcct);
		}

		try {
			LOGGER.debug("getEntries: Found " + result.size() + " accounts for budget " + getID());
		} catch (Exception e) {
			LOGGER.debug("getEntries: Found " + result.size() + " accounts for budget " + "ERROR");
		}

		return result;
    }

	@Override
	public List<KMMBudgetPeriod> getPeriods(KMMAcctID acctID) {
		// List<KMMBudgetPeriod> result = new ArrayList<GCshBudgetPeriod>();

		for ( KMMBudgetAccount acct : getAccounts() ) {
			if ( acct.getAcctID().equals( acctID ) ) {
				return acct.getPeriods();
			}
		}
		
		return null;
	}

    // -----------------------------------------------------------------
    
    @Override
    public String toString() {
		String result = "KMMBudgetImpl [";

		result += "id=" + getID() + ", ";
		result += "name='" + getName() + "', ";
		result += "start=" + getStart() + ", ";
		
		// result += "nof-periods=" + getNofPeriods() + ", ";
		result += "nof-accounts=" + getAccounts().size();
		
		result += "]";

		return result;
    }

}

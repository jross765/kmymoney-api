package org.kmymoney.api.write.impl.aux;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.aux.KMMBudgetAccountImpl;
import org.kmymoney.api.read.impl.aux.KMMBudgetPeriodImpl;
import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.aux.KMMWritableBudgetAccount;
import org.kmymoney.api.write.aux.KMMWritableBudgetPeriod;
import org.kmymoney.api.write.impl.KMyMoneyWritableBudgetImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMMBudgetAccountImpl to allow read-write access instead of
 * read-only access.
 */
public class KMMWritableBudgetAccountImpl extends KMMBudgetAccountImpl 
                                          implements KMMWritableBudgetAccount 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMMWritableBudgetAccountImpl.class);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMMWritableBudgetAccountImpl(
    		final KMyMoneyWritableBudget bdgt,
    		final ACCOUNT jwsdpPeer,
    		final KMyMoneyWritableFile kmmFile) {
    	super(bdgt, jwsdpPeer, kmmFile);
    }

    public KMMWritableBudgetAccountImpl(final KMMBudgetAccountImpl bdgtAcct) {
    	super(bdgtAcct.getParent(), bdgtAcct.getJwsdpPeer(), bdgtAcct.getKMyMoneyFile());
    }

	public KMMWritableBudgetAccountImpl(final KMyMoneyWritableBudgetImpl bdgt, 
										final KMMAcctID acctID, final KMMBudgetAccount.Level lvl) {
		super(bdgt,
			  createBudgetAccount_int(bdgt.getWritableKMyMoneyFile(), bdgt,
					  				  acctID, lvl), 
			  bdgt.getKMyMoneyFile());

		bdgt.addAccount(this);
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
//		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
//	}

	// ---------------------------------------------------------------
	
	/**
	 * Creates a new budget account and adds it to the given KMyMoney file.
	 */
	protected static ACCOUNT createBudgetAccount_int(
			final KMyMoneyWritableFileImpl file, 
			final KMyMoneyWritableBudgetImpl bdgt,
			final KMMAcctID acctID,
			final KMMBudgetAccount.Level lvl) {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}

		if ( bdgt == null ) {
			throw new IllegalArgumentException("argument <bdgt> is null");
		}

		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		// This is needed because transaction.addAccount() later
		// must have an already built account.
		// Otherwise, it will create it from the JAXB-Data
		// thus 2 instances of this KMyMoneyWritableAccountImpl
		// will exist. One created in getAccounts() from this JAXB-Data
		// the other is this object.
		bdgt.getAccounts();

		ObjectFactory fact = file.getObjectFactory();

		ACCOUNT jwsdpBdgtAcct = fact.createACCOUNT();
		jwsdpBdgtAcct.setId(acctID.toString());
		jwsdpBdgtAcct.setBudgetlevel(lvl.getCode());
		jwsdpBdgtAcct.setBudgetsubaccounts(BigInteger.ZERO);
		
		// NO, not here but in the calling method:
		// bdgt.addAccount(new KMyMoneyWritableBudgetAccountImpl(jwsdpBdgtAcct, bdgt.getKMyMoneyFile(), bdgt));
		file.setModified(true);
    
        LOGGER.debug("createBudgetAccount_int: Created new budget account (core): " + jwsdpBdgtAcct);
		
        return jwsdpBdgtAcct;
	}

    // ---------------------------------------------------------------

	@Override
	public void setAcctID(final KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}
		
		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}
		
		jwsdpPeer.setId(acctID.toString());
	}

	@Override
	public void setLevel(final Level lvl) {
		if ( lvl == null )
			throw new IllegalArgumentException("argument <lvl> is null");
		
		jwsdpPeer.setBudgetlevel( lvl.getCode() );
	}

	@Override
	public void setIncludesSubAccounts(final boolean val) {
		jwsdpPeer.setBudgetsubaccounts( ( val ? BigInteger.ONE : BigInteger.ZERO ) );
	}

    // ---------------------------------------------------------------

	@Override
	public void clearPeriods() {
		if ( getPeriods() == null )
			return;
		
		if ( getPeriods().size() == 0 )
			return;
		
		// ---
		
		for ( int i = jwsdpPeer.getPERIOD().size(); i >= 0; i-- ) {
			jwsdpPeer.getPERIOD().remove(i);
		}
		
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public void removePeriod(final KMMBudgetPeriod bdgtPrd) {
		if ( bdgtPrd == null ) {
			throw new IllegalArgumentException("argument <bdgtPrd> is null");
		}
		
		if ( getPeriods() == null )
			return;
		
		if ( getPeriods().size() == 0 )
			return;
		
		// ---
		
		jwsdpPeer.getPERIOD()
			.remove( ((KMMBudgetPeriodImpl) bdgtPrd).getJwsdpPeer() );
		
		getWritableKMyMoneyFile().setModified(true);
	}

	@Override
	public KMMWritableBudgetPeriod createWritablePeriod(LocalDate startDate, BigFraction amt) {
		if ( startDate == null ) {
			throw new IllegalArgumentException("argument <startDate> is null");
		}
		
		if ( startDate.compareTo( getParent().getStart() ) < 0 ) {
			throw new IllegalStateException("argument <startDate> is < the budget's start date: " + startDate + " < " + getParent().getStart());
		}
		
		// ::TODO: Check for the upper limit of start date as well
		
		if ( amt == null ) {
			throw new IllegalArgumentException("argument <amt> is null");
		}
		
		if ( amt.compareTo(BigFraction.ZERO) <= 0 ) {
			throw new IllegalArgumentException("argument <amt> is <= 0");
		}
		
		KMMWritableBudgetPeriodImpl bdgtPrd = new KMMWritableBudgetPeriodImpl(this);
		bdgtPrd.setStart(startDate);
		bdgtPrd.setAmount(amt);
		addPeriod(bdgtPrd);
		
		return bdgtPrd;
	}

	@Override
	public List<KMMWritableBudgetPeriod> getWritablePeriods() {
		List<KMMWritableBudgetPeriod> result = new ArrayList<KMMWritableBudgetPeriod>();
		
		for ( KMMBudgetPeriod bdgtPrd : super.getPeriods() ) {
			KMMWritableBudgetPeriod newBdgtPrd = new KMMWritableBudgetPeriodImpl((KMMBudgetPeriodImpl) bdgtPrd);
		    result.add(newBdgtPrd);
		}

		return result;
	}

	public void addPeriod(final KMMWritableBudgetPeriodImpl bdgtPrd) {
		super.addPeriod(bdgtPrd);		
	}

    // ---------------------------------------------------------------

	@Override
	public String toString() {
		String result = "KMMWritableBudgetAccountImpl [";

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

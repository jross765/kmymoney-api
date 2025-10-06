package org.kmymoney.api.read.impl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kmymoney.api.generated.PAYEE;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.aux.KMMAddressImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyPayeeImpl extends KMyMoneyObjectImpl
                               implements KMyMoneyPayee 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyPayeeImpl.class);

	protected static final String MATCHKEYS_SEP = "\n"; // sic, i.e. "&#10;" on XML level

	// ---------------------------------------------------------------

	// the JWSDP-object we are facading.
	protected final PAYEE jwsdpPeer;

    // ---------------------------------------------------------------

    // protected KMyMoneyObjectImpl helper;

    /*
     * The splits of this payee. May not be fully initialized during loading
     * of the KMyMoney-file.
     */
    private final List<KMyMoneyTransactionSplit> mySplits = new ArrayList<KMyMoneyTransactionSplit>();

    /*
     * If {@link #mySplits} needs to be sorted because it was modified. Sorting is
     * done in a lazy way.
     */
    private boolean mySplitsNeedSorting = false;
    
    // ---------------------------------------------------------------

	@SuppressWarnings("exports")
	public KMyMoneyPayeeImpl(final PAYEE peer, final KMyMoneyFile kmmFile) {
		super(kmmFile);
		
		jwsdpPeer = peer;
	}

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public PAYEE getJwsdpPeer() {
    	return jwsdpPeer;
    }

	// ---------------------------------------------------------------

//    protected void setAddress(final KMMAddressImpl addr) {
//		jwsdpPeer.setADDRESS(addr.getJwsdpPeer());
//    }
    
	// ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
	@Override
	public KMMPyeID getID() {
		return new KMMPyeID(jwsdpPeer.getId());
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getName() {
		return jwsdpPeer.getName();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public KMMComplAcctID getDefaultAccountID() {
		if ( jwsdpPeer.getDefaultaccountid() == null )
			return null;

		return new KMMComplAcctID(jwsdpPeer.getDefaultaccountid());
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public KMMAddress getAddress() {
		if ( jwsdpPeer.getADDRESS() == null )
			return null;

		return new KMMAddressImpl(jwsdpPeer.getADDRESS());
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getEmail() {
		return jwsdpPeer.getEmail();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getReference() {
		return jwsdpPeer.getReference();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String getNotes() {
		return jwsdpPeer.getNotes();
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getMatchingEnabled() {
		if ( jwsdpPeer.getMatchingenabled() == null )
			return false;

		if ( jwsdpPeer.getMatchingenabled().equals(BigInteger.ONE) )
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMatchKeys() {
		ArrayList<String> result = new ArrayList<String>();
		
		String keysRaw = getMatchKeysRaw();
		if ( keysRaw == null )
			return result;
		
		for ( String key : keysRaw.split(MATCHKEYS_SEP) ) {
			result.add(new String(key));
		}
				
		return result;
	}

	// internal helper
	protected String getMatchKeysRaw() {
		return jwsdpPeer.getMatchkey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getUsingMatchKey() {
		if ( jwsdpPeer.getUsingmatchkey() == null )
			return false;

		if ( jwsdpPeer.getUsingmatchkey().equals(BigInteger.ONE) )
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getMatchIgnoreCase() {
		if ( jwsdpPeer.getMatchignorecase() == null )
			return false;

		if ( jwsdpPeer.getMatchignorecase().equals(BigInteger.ONE) )
			return true;
		else
			return false;
	}

    // -----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void addTransactionSplit(final KMyMoneyTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null transaction split given");
		}

		KMyMoneyTransactionSplit old = getTransactionSplitByID(splt.getQualifID());
		if ( old != null ) {
			// There already is a split with that ID
			int dummy = 0; // do nothing!
						   // sic, the following error shall *not* be thrown
						   // (as opposed to code in KMyMoneyTransaction)!
//			if ( !old.equals(splt) ) {
//				System.err.println("addTransactionSplit: New Transaction object with same ID, needs to be replaced: "
//						+ splt.getID() + "[" + splt.getClass().getName() + "] and " + old.getID() + "["
//						+ old.getClass().getName() + "]\n" + "new=" + splt.toString() + "\n" + "old=" + old.toString());
//				LOGGER.error("addTransactionSplit: New Transaction object with same ID, needs to be replaced: "
//						+ splt.getID() + "[" + splt.getClass().getName() + "] and " + old.getID() + "["
//						+ old.getClass().getName() + "]\n" + "new=" + splt.toString() + "\n" + "old=" + old.toString());
//				IllegalStateException exc = new IllegalStateException("DEBUG");
//				exc.printStackTrace();
//				replaceTransactionSplit(old, (KMyMoneyTransactionSplitImpl) splt);
//			}
		} else {
			// There is no transaction with that ID yet
			mySplits.add(splt);
			mySplitsNeedSorting = true;
		}
    }

    /**
     * For internal use only.
     *
     * @param splt
     * @param impl 
     */
    public void replaceTransactionSplit(
    		final KMyMoneyTransactionSplit splt,
    		final KMyMoneyTransactionSplitImpl impl) {
    	if ( ! mySplits.remove(splt) ) {
    		throw new IllegalArgumentException("old object not found!");
    	}

    	mySplits.add(impl);
    }

    // ----------------------------

    /**
     * {@inheritDoc}
     */
    public List<KMyMoneyTransactionSplit> getTransactionSplits() {
    	if (mySplitsNeedSorting) {
    		Collections.sort(mySplits);
    		mySplitsNeedSorting = false;
    	}

    	return mySplits;
	}

    /**
     * {@inheritDoc}
     */
    public boolean hasTransactions() {
		return getTransactionSplits().size() > 0;
	}

    /**
     * {@inheritDoc}
     */
    public List<KMyMoneyTransaction> getTransactions() {
		List<KMyMoneyTransaction> retval = new ArrayList<KMyMoneyTransaction>();

		for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
			// Important: Payees are often (always?) assigned to all
			// splits of a transaction, thus a transaction must only be
			// counted once.
			if ( ! retval.contains(splt.getTransaction()) ) {
				retval.add(splt.getTransaction());
			}
		}

		// retval.sort(Comparator.reverseOrder()); // not necessary 
		
		return retval;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<KMyMoneyTransaction> getTransactions(LocalDate fromDate, LocalDate toDate) {
		List<KMyMoneyTransaction> retval = new ArrayList<KMyMoneyTransaction>();

		for ( KMyMoneyTransaction trx : getTransactions() ) {
			 if ( ( trx.getDatePosted().isEqual( fromDate ) ||
				    trx.getDatePosted().isAfter( fromDate ) ) &&
			      ( trx.getDatePosted().isEqual( toDate ) ||
					trx.getDatePosted().isBefore( toDate ) ) ) {
				 retval.add(trx);
			 }
		}

		// retval.sort(Comparator.reverseOrder()); // not necessary 
		
		return retval;
	}

    /**
     * {@inheritDoc}
     */
    public KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID) {
    	for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
    		if ( splt.getID().equals(spltID.getSplitID()) ) {
    			return splt;
    		}
    	}
    	
    	return null;
    }
    
	// ---------------------------------------------------------------

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyPayeeImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", name='");
		buffer.append(getName() + "'");

		buffer.append(", notes='");
		buffer.append(getNotes() + "'");

		buffer.append(", default-account-id='");
		buffer.append(getDefaultAccountID() + "'");

		buffer.append(", email='");
		buffer.append(getEmail() + "'");

		buffer.append(", reference='");
		buffer.append(getReference() + "'");

		buffer.append(", matching-enabled=");
		buffer.append(getMatchingEnabled());

		// ::TODO: some weird behavior here
		// Cf. TestKMyMoneyWritableFileImpl.test04_1_check_3
//		buffer.append(", match-keys='");
//		for ( String key : getMatchKeys() ) {
//			buffer.append(key + "|");
//		}
//		// ::TODO: delete trailing '|'

		buffer.append(", using-match-key=");
		buffer.append(getUsingMatchKey());

		buffer.append(", match-ignore-case=");
		buffer.append(getMatchIgnoreCase());

		buffer.append("]");
		return buffer.toString();
	}

}

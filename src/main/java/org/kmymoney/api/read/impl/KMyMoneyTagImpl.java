package org.kmymoney.api.read.impl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kmymoney.api.generated.TAG;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyTagImpl extends KMyMoneyObjectImpl
                             implements KMyMoneyTag
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyTagImpl.class);

	// ---------------------------------------------------------------

	// the JWSDP-object we are facading.
	protected final TAG jwsdpPeer;

    // ---------------------------------------------------------------

    // protected KMyMoneyObjectImpl helper;

    /*
     * The splits of this transaction. May not be fully initialized during loading
     * of the KMyMoney-file.
     */
    private final List<KMyMoneyTransactionSplit> mySplits = new ArrayList<KMyMoneyTransactionSplit>();

    /**
     * If {@link #mySplits} needs to be sorted because it was modified. Sorting is
     * done in a lazy way.
     */
    private boolean mySplitsNeedSorting = false;
    
	// ---------------------------------------------------------------

	@SuppressWarnings("exports")
	public KMyMoneyTagImpl(final TAG peer, final KMyMoneyFile kmmFile) {
		super(kmmFile);
		
		jwsdpPeer = peer;
	}

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public TAG getJwsdpPeer() {
    	return jwsdpPeer;
    }

	// ---------------------------------------------------------------

//    protected void setAddress(final KMMAddressImpl addr) {
//		jwsdpPeer.setADDRESS(addr.getJwsdpPeer());
//    }
    
	// ---------------------------------------------------------------

	@Override
	public KMMTagID getID() {
		return new KMMTagID(jwsdpPeer.getId());
	}

	@Override
	public String getName() {
		return jwsdpPeer.getName();
	}

	@Override
	public String getColor() {
		return jwsdpPeer.getTagcolor();
	}

	@Override
	public String getNotes() {
		return jwsdpPeer.getNotes();
	}

	@Override
	public boolean isClosed() {
		if ( jwsdpPeer.getClosed() == BigInteger.ONE )
			return true;
		else
			return false;
	}

	// ---------------------------------------------------------------

    /**
     * @see KMyMoneyPayee#addTransactionSplit(KMyMoneyTransactionSplit)
     */
    public void addTransactionSplit(final KMyMoneyTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null transaction split given");
		}

		KMyMoneyTransactionSplit old = getTransactionSplitByID(splt.getQualifID());
		if ( old != null ) {
			// There already is a split with that ID
			if ( !old.equals(splt) ) {
				System.err.println("addTransactionSplit: New Transaction object with same ID, needs to be replaced: "
						+ splt.getID() + "[" + splt.getClass().getName() + "] and " + old.getID() + "["
						+ old.getClass().getName() + "]\n" + "new=" + splt.toString() + "\n" + "old=" + old.toString());
				LOGGER.error("addTransactionSplit: New Transaction object with same ID, needs to be replaced: "
						+ splt.getID() + "[" + splt.getClass().getName() + "] and " + old.getID() + "["
						+ old.getClass().getName() + "]\n" + "new=" + splt.toString() + "\n" + "old=" + old.toString());
				IllegalStateException exc = new IllegalStateException("DEBUG");
				exc.printStackTrace();
				replaceTransactionSplit(old, (KMyMoneyTransactionSplitImpl) splt);
			}
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
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * {@link KMyMoneyTransaction}
     */
    public List<KMyMoneyTransactionSplit> getTransactionSplits() {
    	if (mySplitsNeedSorting) {
    		Collections.sort(mySplits);
    		mySplitsNeedSorting = false;
    	}

    	return mySplits;
	}

    /**
     * @return true if ${@link #getTransactionSplits()}.size()>0
     */
    public boolean hasTransactions() {
		return getTransactionSplits().size() > 0;
	}

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
     * @param spltID the transaction-split-id to look for
     * @return the identified split or null
     */
    public KMyMoneyTransactionSplit getTransactionSplitByID(KMMQualifSpltID spltID) {
    	for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
    		if ( splt.getID().equals(spltID) ) {
    			return splt;
    		}
    	}
    	
    	return null;
    }
    
	// ::TODO: Very inefficient code.
	// On the other hand: How could you possibly make it (much) more efficient 
	// (without dramatically changing larger parts of this lib)?
	// Cannot think of a better way at the moment...
//	@Override
//	public Collection<KMMQualifSpltID> getTransactionSplitIDs() {
//		Collection<KMyMoneyTransactionSplit> spltList = getKMyMoneyFile().getTransactionSplits();
//		
//		if ( spltList == null )
//			return null;
//		
//		ArrayList<KMMQualifSpltID> result = new ArrayList<KMMQualifSpltID>();
//		
//		for ( KMyMoneyTransactionSplit splt : spltList ) {
//			if ( splt.getTagIDs() != null ) {
//				// if ( splt.getTagIDs().contains(getID()) ) { // no, this wont's do because of the specific tag ID needed
//				for ( KMMTagID tagID : splt.getTagIDs() ) {
//					KMyMoneyTag tag = getKMyMoneyFile().getTagByID(tagID);
//					if ( tag.getID().equals(getID()) ) {
//						KMMQualifSpltID newID = new KMMQualifSpltID(splt.getTransaction().getID(), splt.getID());
//						result.add(newID);
//						continue;
//					}
//				}
//			}
//		}
//		
//		return result;
//		// ::TODO
//		return null;
//	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		return "KMyMoneyTagImpl [id=" + getID() + 
							", name='" + getName() + "'" +
						   ", notes='" + getNotes() + "'" +
                           ", color='" + getColor() + "']";
	}

}

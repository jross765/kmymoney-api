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
import org.kmymoney.api.read.impl.hlp.HasTransactionsImplSpec;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyTagImpl extends KMyMoneyObjectImpl
                             implements KMyMoneyTag,
                                        HasTransactionsImplSpec
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
	@Override
    public void addTransactionSplit(final KMyMoneyTransactionSplit splt) {
		addTransactionSplit(splt, true);
	}
	
	@Override
    public void addTransactionSplit(final KMyMoneyTransactionSplit splt, boolean withCheck) {
		if ( splt == null ) {
			throw new IllegalArgumentException("argument <splt> is null");
		}

		if ( ! splt.getTagIDs().contains( getID() ) ) {
			LOGGER.error("addTransactionSplit: Split " + splt.getQualifID() + " does not belong to tag " + getID());
			throw new IllegalArgumentException("Split " + splt.getQualifID() + " does not belong to tag " + getID());
		}
		
		// ---

		if ( withCheck ) {
			KMyMoneyTransactionSplit old = getTransactionSplitByID(splt.getQualifID());
			if ( old != null ) {
				// There already is a split with that ID
				if ( ! old.equals(splt) ) {
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
			}
		}
		
		mySplits.add(splt);
		mySplitsNeedSorting = true;
    }

	@Override
    public void removeTransactionSplit(final KMyMoneyTransactionSplit splt) {
    	removeTransactionSplit(splt, true);
    }
    
	@Override
    public void removeTransactionSplit(final KMyMoneyTransactionSplit splt, boolean strict) {
		if ( splt == null ) {
			throw new IllegalArgumentException("argument <splt> is null");
		}
		
		if ( ! splt.getTagIDs().contains( getID() ) ) {
			LOGGER.error("removeTransactionSplit: Split " + splt.getQualifID() + " does not belong to tag " + getID());
			throw new IllegalArgumentException("Split " + splt.getQualifID() + " does not belong to tag " + getID());
		}

		if ( strict )
		{
			// Does not work:
			// if ( ! mySplits.contains(splt) ) {
			// ...
			// Instead:
			boolean intListContainsSplt = false;
			for ( KMyMoneyTransactionSplit elt : mySplits ) {
				if ( elt.getQualifID().equals( splt.getQualifID() ) ) {
					intListContainsSplt = true;
					break;
				}
			}
			if ( ! intListContainsSplt ) {
				throw new IllegalStateException("internal list of splits does not contain split " + splt.getQualifID() + ", thus this split cannot be removed from the list");
			}
		}
		
//		int listSize = mySplits.size();
		// Does not work:
		// mySplits.remove(splt);
		// Instead:
		for ( int i = mySplits.size() - 1; i >= 0; i-- ) {
			if ( mySplits.get(i).getQualifID().equals( splt.getQualifID() ) ) {
				mySplits.remove(i);
				break;
			}
		}
//		assert( mySplits.size() == listSize - 1 );
		
		// Additionally, this tag's ID will have to be removed
		// from the given split. But this will happen in the 
		// calling function (if at all; not if the split is
		// deleted entirely, of course).
    }

    /**
     * For internal use only.
     *
     * @param splt
     * @param impl 
     */
    public void replaceTransactionSplit(
    		final KMyMoneyTransactionSplit oldSplt,
    		final KMyMoneyTransactionSplitImpl newSplt) {
		if ( oldSplt == null ) {
			throw new IllegalArgumentException("argument <oldSplt> is null");
		}
		
		if ( newSplt == null ) {
			throw new IllegalArgumentException("argument <newSplt> is null");
		}
		
		if ( mySplits.contains(oldSplt) ) {
			throw new IllegalStateException("internal list of splits does not contain old split " + oldSplt.getQualifID() + " from account " + getID());
		}
		
		// ---
		
		if ( ! mySplits.remove(oldSplt) ) {
			LOGGER.error("replaceTransactionSplit: Could not remove old split " + oldSplt.getQualifID() + " from local list (account " + getID() + ")");
			throw new IllegalStateException("Could not remove old split " + oldSplt.getQualifID() + " from local list (account " + getID() + ")");
		}

		mySplits.add(newSplt);
    	mySplitsNeedSorting = true;
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
			// Important: Tags are often (always?) assigned to all
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
		if ( spltID == null ) {
			throw new IllegalArgumentException("argument <spltID> is null");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("argument <spltID> is not set");
		}

    	for ( KMyMoneyTransactionSplit splt : getTransactionSplits() ) {
    		if ( splt.getQualifID().equals(spltID) ) {
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

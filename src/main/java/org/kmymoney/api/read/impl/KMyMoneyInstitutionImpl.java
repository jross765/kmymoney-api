package org.kmymoney.api.read.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.ACCOUNTID;
import org.kmymoney.api.generated.INSTITUTION;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.aux.KMMAddressImpl;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyInstitutionImpl extends KMyMoneyObjectImpl 
								     implements KMyMoneyInstitution 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyInstitutionImpl.class);

    // ---------------------------------------------------------------
    
    // the JWSDP-object we are facading.
    protected final INSTITUTION jwsdpPeer;

    // ---------------------------------------------------------------

    // protected KMyMoneyObjectImpl helper;

    /*
     * The accountsof this institution. May not be fully initialized during loading
     * of the KMyMoney-file.
     */
    private List<KMyMoneyAccount> myAccts = null;

    /*
     * If {@link #myAccts} needs to be sorted because it was modified. Sorting is
     * done in a lazy way.
     */
    private boolean myAcctsNeedSorting = false;
    
    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMyMoneyInstitutionImpl(
	    final INSTITUTION peer, 
	    final KMyMoneyFile kmmFile) {
    	super(kmmFile);

    	jwsdpPeer = peer;
    }

	// ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public INSTITUTION getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    @Override
    public KMMInstID getID() {
    	return new KMMInstID(jwsdpPeer.getId());
    }

    // ---------------------------------------------------------------

    @Override
    public String getName() {
    	return jwsdpPeer.getName();
    }

	@Override
	public String getSortCode() {
		return jwsdpPeer.getSortcode();
	}

	@Override
	public KMMAddress getAddress() {
		if ( jwsdpPeer.getADDRESS() == null )
			return null;

		return new KMMAddressImpl(jwsdpPeer.getADDRESS());
	}

    // ---------------------------------------------------------------

	@Override
	public String getBIC() {
    	try {
    		return getUserDefinedAttribute("bic"); // ::MAGIC
    	} catch (KVPListDoesNotContainKeyException exc) {
    		return null;
    	}
	}

	@Override
	public String getURL() {
    	try {
    		return getUserDefinedAttribute("url"); // ::MAGIC
    	} catch (KVPListDoesNotContainKeyException exc) {
    		return null;
    	}
	}

    // ---------------------------------------------------------------

	@Override
	public boolean hasAccounts() {
		return getAccounts().size() > 0;
	}

	@Override
	public List<KMyMoneyAccount> getAccounts() {
		if (myAccts == null) {
			initAccounts();
		}

		if (myAcctsNeedSorting) {
    		Collections.sort(myAccts);
    		myAcctsNeedSorting = false;
    	}

    	return myAccts;
	}

    private void initAccounts() {
	    List<ACCOUNTID> jwsdpAcctIDs = jwsdpPeer.getACCOUNTIDS().getACCOUNTID();
	    
	    myAccts = new ArrayList<KMyMoneyAccount>();
	    for (ACCOUNTID jwsdpAcctID : jwsdpAcctIDs) {
	    	myAccts.add(createAccount(jwsdpAcctID));
	    }
    }

    protected KMyMoneyAccountImpl createAccount(final ACCOUNTID jwsdpAcctID) {
    	ACCOUNT peer = ((KMyMoneyFileImpl) kmmFile).getAcctMgr().getAccountByID_raw(jwsdpAcctID);
    	return new KMyMoneyAccountImpl(peer, getKMyMoneyFile(), 
    								   true);
    }

    /**
     * @param acctID the account-id to look for
     * @return the identified split or null
     */
	@Override
    public KMyMoneyAccount getAccountByID(KMMComplAcctID acctID) {
    	for ( KMyMoneyAccount acct : getAccounts() ) {
    		if ( acct.getID().equals(acctID) ) {
    			return acct;
    		}
    	}
    	
    	return null;
    }
    
    public void addAccount(final KMyMoneyAccount acct) {
		if ( acct == null ) {
			throw new IllegalArgumentException("null account given");
		}

		KMyMoneyAccount old = getAccountByID(acct.getID());
		if ( old != null ) {
			// There already is a split with that ID
			if ( !old.equals(acct) ) {
				System.err.println("addAccount: New Account object with same ID, needs to be replaced: " + acct.getID()
						+ "[" + acct.getClass().getName() + "] and " + old.getID() + "[" + old.getClass().getName()
						+ "]\n" + "new=" + acct.toString() + "\n" + "old=" + old.toString());
				LOGGER.error("addAccount: New Account object with same ID, needs to be replaced: " + acct.getID() + "["
						+ acct.getClass().getName() + "] and " + old.getID() + "[" + old.getClass().getName() + "]\n"
						+ "new=" + acct.toString() + "\n" + "old=" + old.toString());
				IllegalStateException exc = new IllegalStateException("DEBUG");
				exc.printStackTrace();
				replaceAccount(old, (KMyMoneyAccountImpl) acct);
			}
		} else {
			// There is no account with that ID yet
			myAccts.add(acct);
			myAcctsNeedSorting = true;
		}
    }

    /**
     * For internal use only.
     * @param acct 
     * @param impl 
     */
    public void replaceAccount(
    		final KMyMoneyAccount acct,
    		final KMyMoneyAccountImpl impl) {
    	if ( ! myAccts.remove(acct) ) {
    		throw new IllegalArgumentException("old object not found!");
    	}

    	myAccts.add(impl);
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
	public String toString() {
		String result = "KMyMoneyInstitutionImpl ";

		result += "[id=" + getID();

		result += ", name='" + getName() + "'";
		result += ", sort-code='" + getSortCode() + "'";
		// result += ", address=" + getAddress();
		result += ", bic='" + getBIC() + "'";
		result += ", url='" + getURL() + "'";

		result += "]";

		return result;
	}

}

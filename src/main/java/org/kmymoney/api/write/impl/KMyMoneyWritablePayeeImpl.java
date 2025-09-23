package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.math.BigInteger;

import org.kmymoney.api.generated.ADDRESS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PAYEE;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyPayeeImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.api.write.aux.KMMWritableAddress;
import org.kmymoney.api.write.impl.aux.KMMWritableAddressImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMIDNotSetException;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyPayeeImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritablePayeeImpl extends KMyMoneyPayeeImpl 
                                       implements KMyMoneyWritablePayee 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritablePayeeImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritablePayee()}.
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
	public KMyMoneyWritablePayeeImpl(
			final PAYEE jwsdpPeer,
			final KMyMoneyWritableFileImpl file) {
    	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritablePayee()}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected KMyMoneyWritablePayeeImpl(final KMyMoneyWritableFileImpl file) {
    	super(createPayee_int(file, file.getNewPayeeID()), file);
    }

	public KMyMoneyWritablePayeeImpl(final KMyMoneyPayeeImpl pye, final boolean addSplits) {
		super(pye.getJwsdpPeer(), pye.getKMyMoneyFile());

		if ( addSplits ) {
		    for ( KMyMoneyTransactionSplit splt : ((KMyMoneyFileImpl) pye.getKMyMoneyFile()).getTransactionSplits_readAfresh() ) {
		    	if ( splt.getPayeeID() != null ) { // Caution: Payee is optional for split, as opposed to account
		    									   // Cf. KMyMoneyWritableAccountImpl
		    		if ( splt.getPayeeID().equals(pye.getID()) ) {
			    		super.addTransactionSplit(splt);
				    // NO:
//					    addTransactionSplit(new KMyMoneyTransactionSplitImpl(splt.getJwsdpPeer(), splt.getTransaction(), 
//			                                false, false));
			    	}
		    	}
		    }
		}
	}

    // ---------------------------------------------------------------

    /**
     * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
     * the ID of the new transaction!
     *
     * @param file the file we will belong to
     * @param guid the ID we shall have
     * @return a new jwsdp-peer already entered into th jwsdp-peer of the file
     */
    protected static PAYEE createPayee_int(
    		final KMyMoneyWritableFileImpl file, 
    		final KMMPyeID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}
    
        ObjectFactory factory = file.getObjectFactory();
    
        PAYEE jwsdpPye = file.createPayeeType();
    
        jwsdpPye.setId(newID.toString());
        jwsdpPye.setName("no name given");
    
        {
        	ADDRESS addr = factory.createADDRESS();
            addr.setCity(null);
            addr.setCounty("");
            addr.setPostcode("");
            addr.setState("");
            addr.setStreet("");
            addr.setTelephone("");
            addr.setZip("");
            addr.setZipcode("");
            jwsdpPye.setADDRESS(addr);
        }
    
        file.getRootElement().getPAYEES().getPAYEE().add(jwsdpPye);
        file.setModified(true);
    
        LOGGER.debug("createPayee_int: Created new payee (core): " + jwsdpPye.getId());
        
        return jwsdpPye;
    }

    // ---------------------------------------------------------------

//	protected void setAddress(final KMMWritableAddressImpl addr) {
//		super.setAddress(addr);
//	}
//
    /**
     * Delete this Payee and remove it from the file.
     *
     * @see KMyMoneyWritablePayee#remove()
     */
    @Override
    public void remove() {
    	PAYEE peer = jwsdpPeer;
    	(getKMyMoneyFile()).getRootElement().getPAYEES().getPAYEE().remove(peer);
    	(getKMyMoneyFile()).removePayee(this);
    }

    // ---------------------------------------------------------------

    /**
     * The KMyMoney file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
    	return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
    }

    /**
     * The KMyMoney file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public KMyMoneyWritableFileImpl getKMyMoneyFile() {
    	return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyWritablePayee#getWritableAddress()
     */
    public KMMWritableAddress getWritableAddress() {
    	return new KMMWritableAddressImpl(jwsdpPeer.getADDRESS());
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyWritablePayee#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("argument <name> is null");
    	}

    	if ( name.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <name> is empty");
    	}

    	String oldName = getName();
    	jwsdpPeer.setName(name);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("name", oldName, name);
    	}
    }

    @Override
    public void setAddress(final KMMAddress adr) {
		if ( adr == null ) {
			throw new IllegalArgumentException("argument <adr> is null");
		}

		/*
		 * if (adr instanceof AddressImpl) { AddressImpl adrImpl = (AddressImpl) adr;
		 * jwsdpPeer.setPyeAddr(adrImpl.jwsdpPeer); } else
		 */

		{

			if ( jwsdpPeer.getADDRESS() == null ) {
				jwsdpPeer.setADDRESS(getKMyMoneyFile().getObjectFactory().createADDRESS());
			}

			jwsdpPeer.getADDRESS().setCity(adr.getCity());
			jwsdpPeer.getADDRESS().setCounty(adr.getCounty());
			jwsdpPeer.getADDRESS().setPostcode(adr.getPostCode());
			jwsdpPeer.getADDRESS().setState(adr.getState());
			jwsdpPeer.getADDRESS().setStreet(adr.getStreet());
			jwsdpPeer.getADDRESS().setTelephone(adr.getTelephone());
			jwsdpPeer.getADDRESS().setZip(adr.getZip());
			jwsdpPeer.getADDRESS().setZipcode(adr.getZipCode());
		}

		getKMyMoneyFile().setModified(true);
    }

    /**
     * @param notes user-defined notes about the customer (may be null)
     * @see KMyMoneyWritableCustomer#setNotes(String)
     */
    @Override
    public void setNotes(final String notes) {
		if ( notes == null ) {
			throw new IllegalArgumentException("argument <notes> is null");
		}
		
		if ( notes.trim().length() == 0 ) {
			throw new IllegalArgumentException("argument <notes> is empty");
		}

		// Caution: empty string allowed here
//		if ( notes.trim().length() == 0 ) {
//	   	 throw new IllegalArgumentException("empty notesgiven!");
//		}

		String oldNotes = getNotes();
		jwsdpPeer.setNotes(notes);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("notes", oldNotes, notes);
		}
    }

	@Override
	public void setDefaultAccountID(KMMComplAcctID acctID) {
    	if ( acctID == null ) {
    		throw new IllegalArgumentException("argument <acctID> is null");
    	}

    	if ( ! acctID.isSet() ) {
    		throw new IllegalArgumentException("argument <acctID> is not set");
    	}

    	KMMComplAcctID oldAcctID = getDefaultAccountID();
    	try {
			jwsdpPeer.setDefaultaccountid(acctID.getStdID().get());
		} catch (KMMIDNotSetException e) {
			LOGGER.error("setDefaultAccountID: Could not set new account ID");
			// ::TODO: Don't really want to throw exceptions --
			// I think this branch cannot practically be reached
			e.printStackTrace();
		}
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("default-account-id", oldAcctID, acctID);
    	}
	}

	@Override
	public void setEmail(String eml) {
    	if ( eml == null ) {
    		throw new IllegalArgumentException("argument <eml> is null");
    	}

    	if ( eml.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <eml> is empty");
    	}

    	String oldEml = getEmail();
    	jwsdpPeer.setEmail(eml);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("email", oldEml, eml);
    	}
	}

	@Override
	public void setReference(String ref) {
    	if ( ref == null ) {
    		throw new IllegalArgumentException("argument <ref> is null");
    	}

    	if ( ref.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <ref> is empty");
    	}

    	String oldRef = getReference();
    	jwsdpPeer.setReference(ref);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("reference", oldRef, ref);
    	}
	}

	@Override
	public void setMatchingEnabled(BigInteger enbl) {
    	if ( enbl == null ) {
    		throw new IllegalArgumentException("argument <enbl> is null");
    	}

    	if ( enbl.intValue() < 0 ) {
    		throw new IllegalArgumentException("argument <enbl> is < 0");
    	}

    	BigInteger oldEnbl = getMatchingEnabled();
    	jwsdpPeer.setMatchingenabled(enbl);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("matching-enabled", oldEnbl, enbl);
    	}
	}

	@Override
	public void setMatchKey(String key) {
    	if ( key == null ) {
    		throw new IllegalArgumentException("argument <key> is null");
    	}

    	if ( key.trim().length() == 0 ) {
    		throw new IllegalArgumentException("argument <key> is empty");
    	}

    	String oldKey = getMatchKey();
    	jwsdpPeer.setReference(key);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("matching-key", oldKey, key);
    	}
	}

	@Override
	public void setUsingMatchKey(BigInteger val) {
    	if ( val == null ) {
    		throw new IllegalArgumentException("argument <val> is null");
    	}

    	if ( val.intValue() == 0 ) {
    		throw new IllegalArgumentException("argument <val> is < 0");
    	}

    	BigInteger oldVal = getUsingMatchKey();
    	jwsdpPeer.setUsingmatchkey(val);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("using-match-key", oldVal, val);
    	}
	}

	@Override
	public void setMatchIgnoreCase(BigInteger val) {
    	if ( val == null ) {
    		throw new IllegalArgumentException("argument <val> is null");
    	}

    	if ( val.intValue() == 0 ) {
    		throw new IllegalArgumentException("argument <val> is < 0");
    	}

    	BigInteger oldVal = getUsingMatchKey();
    	jwsdpPeer.setMatchignorecase(val);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null ) {
    		propertyChangeSupport.firePropertyChange("match-ignore-case", oldVal, val);
    	}
	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyWritablePayeeImpl [");

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

		buffer.append(", match-key='");
		buffer.append(getMatchKey() + "'");

		buffer.append(", using-match-key=");
		buffer.append(getUsingMatchKey());

		buffer.append(", match-ignore-case=");
		buffer.append(getMatchIgnoreCase());

		buffer.append("]");
		return buffer.toString();
    }

}

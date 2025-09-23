package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;

import org.kmymoney.api.generated.ADDRESS;
import org.kmymoney.api.generated.INSTITUTION;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.KMyMoneyInstitutionImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.api.write.aux.KMMWritableAddress;
import org.kmymoney.api.write.impl.aux.KMMWritableAddressImpl;
import org.kmymoney.api.write.impl.hlp.HasWritableUserDefinedAttributesImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyInstitutionImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableInstitutionImpl extends KMyMoneyInstitutionImpl 
                                             implements KMyMoneyWritableInstitution 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableInstitutionImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableInstitution()}.
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
	public KMyMoneyWritableInstitutionImpl(
			final INSTITUTION jwsdpPeer,
			final KMyMoneyWritableFileImpl file) {
    	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableInstitution()}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected KMyMoneyWritableInstitutionImpl(final KMyMoneyWritableFileImpl file) {
    	super(createInstitution_int(file, file.getNewInstitutionID()), file);
    }

    public KMyMoneyWritableInstitutionImpl(final KMyMoneyInstitutionImpl inst) {
    	super(inst.getJwsdpPeer(), inst.getKMyMoneyFile());
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
    protected static INSTITUTION createInstitution_int(
    		final KMyMoneyWritableFileImpl file, 
    		final KMMInstID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}
    
        ObjectFactory factory = file.getObjectFactory();
    
        INSTITUTION jwsdpInst = file.createInstitutionType();
    
        jwsdpInst.setId(newID.toString());
        jwsdpInst.setName("no name given");
    
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
            jwsdpInst.setADDRESS(addr);
        }
    
        file.getRootElement().getINSTITUTIONS().getINSTITUTION().add(jwsdpInst);
        file.setModified(true);
    
        LOGGER.debug("createInstitution_int: Created new institution (core): " + jwsdpInst.getId());
        
        return jwsdpInst;
    }

    // ---------------------------------------------------------------

//	protected void setAddress(final KMMWritableAddressImpl addr) {
//		super.setAddress(addr);
//	}

    /**
     * Delete this Institution and remove it from the file.
     *
     * @see KMyMoneyWritableInstitution#remove()
     */
    @Override
    public void remove() {
    	INSTITUTION peer = jwsdpPeer;
    	(getKMyMoneyFile()).getRootElement().getINSTITUTIONS().getINSTITUTION().remove(peer);
    	(getKMyMoneyFile()).removeInstitution(this);
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
     * @see KMyMoneyWritableInstitution#getWritableAddress()
     */
    public KMMWritableAddress getWritableAddress() {
    	return new KMMWritableAddressImpl(jwsdpPeer.getADDRESS());
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyWritableInstitution#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("null name given!");
    	}

    	if ( name.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty name given!");
    	}

    	String oldName = getName();
    	jwsdpPeer.setName(name);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("name", oldName, name);
    	}
    }

    /**
	 * @param notes user-defined notes about the customer (may be null)
	 * @see KMyMoneyWritableCustomer#setNotes(String)
	 */
	@Override
	public void setSortCode(final String sortCode) {
		if ( sortCode == null ) {
			throw new IllegalArgumentException("null sort code given");
		}
		
		if ( sortCode.isEmpty() ) {
			throw new IllegalArgumentException("empty sort code given");
		}
	
		String oldSortCode = getSortCode();
		jwsdpPeer.setSortcode(sortCode);
		getKMyMoneyFile().setModified(true);
	
		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("sortcode", oldSortCode, sortCode);
		}
	}

	@Override
    public void setAddress(final KMMAddress adr) {
		if ( adr == null ) {
			throw new IllegalArgumentException("null address given!");
		}

		/*
		 * if (adr instanceof AddressImpl) { AddressImpl adrImpl = (AddressImpl) adr;
		 * jwsdpPeer.setInstAddr(adrImpl.jwsdpPeer); } else
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

    // ---------------------------------------------------------------

    @Override
	public void setBIC(String bic) {
    	if ( bic == null ) {
    		throw new IllegalArgumentException("null bic given!");
    	}

    	// CAUTION: Empty string allowed here
//    	if ( bic.trim().length() == 0 ) {
//    		throw new IllegalArgumentException("empty bic given!");
//    	}

    	String oldBIC = getBIC();
		setUserDefinedAttribute("bic", bic.toUpperCase()); // sic, no try-catch-block here
														   // note upper case
		
		// Already done:
		// getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("url", oldBIC, bic);
    	}
	}

	@Override
	public void setURL(String url) {
    	if ( url == null ) {
    		throw new IllegalArgumentException("null url given!");
    	}

    	// CAUTION: Empty string allowed here
//    	if ( url.trim().length() == 0 ) {
//    		throw new IllegalArgumentException("empty url given!");
//    	}

    	String oldURL = getURL();
		setUserDefinedAttribute("url", url); // sic, no try-catch-block here
		
		// Already done:
		// getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("url", oldURL, url);
    	}
	}

    // ---------------------------------------------------------------

	@Override
	public void addUserDefinedAttribute(String name, String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			ObjectFactory fact = getKMyMoneyFile().getObjectFactory();
			KEYVALUEPAIRS newKVPs = fact.createKEYVALUEPAIRS();
			jwsdpPeer.setKEYVALUEPAIRS(newKVPs);
		}
		
		HasWritableUserDefinedAttributesImpl
			.addUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

	@Override
	public void removeUserDefinedAttribute(String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.removeUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             	name);
	}

	@Override
	public void setUserDefinedAttribute(String name, String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.setUserDefinedAttributeCore(jwsdpPeer.getKEYVALUEPAIRS(), getWritableKMyMoneyFile(), 
			                             name, value);
	}

    // ---------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneyWritableInstitutionImpl ";

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

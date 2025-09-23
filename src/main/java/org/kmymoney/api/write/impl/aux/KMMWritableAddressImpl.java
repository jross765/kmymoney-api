package org.kmymoney.api.write.impl.aux;

import org.kmymoney.api.generated.ADDRESS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.read.impl.aux.KMMAddressImpl;
import org.kmymoney.api.write.aux.KMMWritableAddress;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableInstitutionImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritablePayeeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMMAddressImpl to allow read-write access instead of
 * read-only access.
 */
public class KMMWritableAddressImpl extends KMMAddressImpl 
                                    implements KMMWritableAddress 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMMWritableAddressImpl.class);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public KMMWritableAddressImpl(final ADDRESS jwsdpPeer) {
    	super(jwsdpPeer);
    }

    public KMMWritableAddressImpl(final KMMAddressImpl addr) {
    	super(addr.getJwsdpPeer());
    }

	public KMMWritableAddressImpl(final KMyMoneyWritableInstitutionImpl inst) {
		super(createAddress_int(inst.getWritableKMyMoneyFile(), inst));

		inst.setAddress(this);
	}

	public KMMWritableAddressImpl(final KMyMoneyWritablePayeeImpl pye) {
		super(createAddress_int(pye.getWritableKMyMoneyFile(), pye));

		pye.setAddress(this);
	}

	// ---------------------------------------------------------------
	
	/**
	 * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
	 * the ID of the new transaction!
	 */
	protected static ADDRESS createAddress_int(
			final KMyMoneyWritableFileImpl file, 
			final KMyMoneyWritableInstitutionImpl inst) {
		if ( inst == null ) {
			throw new IllegalArgumentException("null institution given");
		}

		// This is needed because transaction.addSplit() later
		// must have an already built address.
		// Otherwise, it will create it from the JAXB-Data
		// thus 2 instances of this KMyMoneyWritableAddressImpl
		// will exist. One created in getSplits() from this JAXB-Data
		// the other is this object.
		inst.getAddress();

		ObjectFactory fact = file.getObjectFactory();

		ADDRESS jwsdpAddr = fact.createADDRESS();
		
		// NO, not here but in the calling method:
		// trx.addAddress(new KMyMoneyWritableAddressImpl(jwsdpAddr, pye.getKMyMoneyFile(), pye));
		file.setModified(true);
    
        LOGGER.debug("createAddress_int: Created new address (core)");
		
        return jwsdpAddr;
	}

	/**
	 * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
	 * the ID of the new transaction!
	 */
	protected static ADDRESS createAddress_int(
			final KMyMoneyWritableFileImpl file, 
			final KMyMoneyWritablePayeeImpl pye) {
		if ( pye == null ) {
			throw new IllegalArgumentException("null payee given");
		}

		// This is needed because transaction.addSplit() later
		// must have an already built address.
		// Otherwise, it will create it from the JAXB-Data
		// thus 2 instances of this KMyMoneyWritableAddressImpl
		// will exist. One created in getSplits() from this JAXB-Data
		// the other is this object.
		pye.getAddress();

		ObjectFactory fact = file.getObjectFactory();

		ADDRESS jwsdpAddr = fact.createADDRESS();
		
		// NO, not here but in the calling method:
		// trx.addAddress(new KMyMoneyWritableAddressImpl(jwsdpAddr, pye.getKMyMoneyFile(), pye));
		file.setModified(true);
    
        LOGGER.debug("createAddress_int: Created new address (core)");
		
        return jwsdpAddr;
	}

    // ---------------------------------------------------------------

	@Override
	public void setStreet(String street) {
		getJwsdpPeer().setStreet(street);		
	}

	@Override
	public void setCity(String city) {
		getJwsdpPeer().setCity(city);		
	}

	@Override
	@Deprecated
	public void setCounty(String county) {
		getJwsdpPeer().setCounty(county);		
	}

	@Override
	public void setCountry(String country) {
		getJwsdpPeer().setCountry(country);		
	}

	@Override
	public void setState(String state) {
		getJwsdpPeer().setState(state);		
	}

	@Override
	public void setPostCode(String postCode) {
		getJwsdpPeer().setPostcode(postCode);		
	}

	@Override
	public void setZip(String zip) {
		getJwsdpPeer().setZip(zip);		
	}

	@Override
	public void setZipCode(String zipCode) {
		getJwsdpPeer().setZipcode(zipCode);		
	}

	@Override
	public void setTelephone(String telephone) {
		getJwsdpPeer().setTelephone(telephone);		
	}
    
    // ---------------------------------------------------------------

    @Override
	public String toString() {
		String result = "KMMWritableAddressImpl [";

		result += "street='" + getStreet() + "'";
		result += ", city='" + getCity() + "'";
		result += ", zip='" + getZip() + "'";
		result += ", zip-code='" + getZipCode() + "'";
		result += ", post-code='" + getPostCode() + "'";
		result += ", state='" + getState() + "'";
		result += ", county='" + getCounty() + "'";
		result += ", country='" + getCountry() + "'";
		result += ", telephone='" + getTelephone() + "'";

		result += "]";

		return result;
	}

}

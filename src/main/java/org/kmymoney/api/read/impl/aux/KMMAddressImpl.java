package org.kmymoney.api.read.impl.aux;

import org.kmymoney.api.generated.ADDRESS;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.aux.KMMAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMMAddressImpl implements KMMAddress {
	
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMMAddressImpl.class);

	// ---------------------------------------------------------------

	// The JWSDP-object we are wrapping.
	private final ADDRESS jwsdpPeer;

    protected final KMyMoneyFile myFile;
    
	// ---------------------------------------------------------------

	/**
	 * @param newPeer the JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public KMMAddressImpl(
			final ADDRESS newPeer,
			final KMyMoneyFile kmmFile) {
		super();
		
		this.jwsdpPeer = newPeer;
		this.myFile = kmmFile;
	}

	// ---------------------------------------------------------------

	/**
	 * @return The JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public ADDRESS getJwsdpPeer() {
		return jwsdpPeer;
	}

    public KMyMoneyFile getKMyMoneyFile() {
    	return myFile;
    }

	// -----------------------------------------------------------

	@Override
	public String getStreet() {
		return jwsdpPeer.getStreet();
	}

	@Override
	public String getCity() {
		return jwsdpPeer.getCity();
	}

	@Override
	@Deprecated
	public String getCounty() {
		return jwsdpPeer.getCounty();
	}

	@Override
	public String getCountry() {
		return jwsdpPeer.getCountry();
	}

	@Override
	public String getState() {
		return jwsdpPeer.getState();
	}

	// ----------------------------

	@Override
	@Deprecated
	public String getPostCode() {
		return jwsdpPeer.getPostcode();
	}

	@Override
	@Deprecated
	public String getZip() {
		return jwsdpPeer.getZip();
	}

	@Override
	public String getZipCode() {
		return jwsdpPeer.getZipcode();
	}

	// ----------------------------

	@Override
	public String getTelephone() {
		return jwsdpPeer.getTelephone();
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		String result = "KMMAddressImpl [";

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

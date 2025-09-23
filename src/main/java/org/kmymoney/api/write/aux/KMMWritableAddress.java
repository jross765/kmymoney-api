package org.kmymoney.api.write.aux;

import org.kmymoney.api.read.aux.KMMAddress;

public interface KMMWritableAddress extends KMMAddress {

	/**
	 * 
	 * @param street
	 * 
	 * @see #getStreet()
	 */
	void  setStreet(String street);
    
	/**
	 * 
	 * @param city
	 * 
	 * @see #getCity()c
	 */
    void  setCity(String city);

    /**
     * 
     * @param county
     * 
     * @see #getCounty()
     */
    @Deprecated
    void  setCounty(String county);

    /**
     * 
     * @param county
     * 
     * @see #getCountry()
     */
    void  setCountry(String country);

    /**
     * 
     * @param state
     * 
     * @see #getState()
     */
    void  setState(String state);
    
    // ----------------------------
    
    /**
     * 
     * @param postCode
     * 
     * @see #getPostCode()
     */
    @Deprecated
    void  setPostCode(String postCode);
    
    /**
     * 
     * @param zip
     * 
     * @see #getZip()
     * @see #setZipCode(String)
     */
    @Deprecated
    void  setZip(String zip);
    
    /**
     * 
     * @param zipCode
     * 
     * @see #getZipCode()
     * @see #setZip(String)
     */
    void  setZipCode(String zipCode);
    
    // ----------------------------
    
    /**
     * 
     * @param telephone
     * 
     * @see #getTelephone()
     */
    void setTelephone(String telephone);
    
}

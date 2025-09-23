package org.kmymoney.api.write;

import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.write.hlp.HasWritableAddress;
import org.kmymoney.api.write.hlp.HasWritableAttachment;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;

/**
 * Institution that can be modified.
 * 
 * @see KMyMoneyInstitution
 */
public interface KMyMoneyWritableInstitution extends KMyMoneyInstitution,
                                                     KMyMoneyWritableObject,
                                                     HasWritableUserDefinedAttributes,
                                                     HasWritableAddress,
                                                     HasWritableAttachment
{

    void remove();
   
	// ---------------------------------------------------------------

    void setName(String name);

    void setSortCode(String adr);

    // ---------------------------------------------------------------

    void setBIC(String bic);

}

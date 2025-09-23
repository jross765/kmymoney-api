package org.kmymoney.api.read;

import org.kmymoney.api.read.hlp.HasAccounts;
import org.kmymoney.api.read.hlp.HasAddress;
import org.kmymoney.api.read.hlp.HasAttachment;
import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.simple.KMMInstID;

/**
 * Bank or other entity which one or several accounts are registered with.
 */
public interface KMyMoneyInstitution extends KMyMoneyObject,
										     HasUserDefinedAttributes,
										     HasAddress,
										     HasAccounts,
										     HasAttachment
{

    /**
     * @return
     */
    KMMInstID getID();

    // ------------------------------------------------------------

    /**
     * @return
     */
    String getName();
    
    /**
     * It's called the "rounding number" on the GUI
     * 
     * @return
     */
    String getSortCode(); 

    // ------------------------------------------------------------
    
    /**
     * 
     * @return
     */
    String getBIC();
    
}

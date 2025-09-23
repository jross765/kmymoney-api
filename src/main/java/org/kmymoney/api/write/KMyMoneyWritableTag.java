package org.kmymoney.api.write;

import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;

/**
 * Tag that can be modified.
 * 
 * @see KMyMoneyTag
 */
public interface KMyMoneyWritableTag extends KMyMoneyTag,
                                             KMyMoneyWritableObject
{

    void remove();
   
	// ---------------------------------------------------------------

    /**
     * 
     * @param name
     * 
     * @see #getName()
     */
    void setName(String name);

    /**
     * 
     * @param nts
     * 
     * @see #getNotes()
     */
    void setNotes(String nts);
    
    /**
     * 
     * @param clr
     * 
     * @see #getColor()
     */
    void setColor(String clr);
    
	// ---------------------------------------------------------------
    
    void setClosed(boolean val);

}

package org.kmymoney.api.read;

import org.kmymoney.api.read.hlp.HasTransactions;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.simple.KMMTagID;

/**
 * A short (combination of) word(s) (typically just one)
 * that is used to classify transaction splits and thus
 * define logical groups.
 * <br>
 * There is an m-to-n relationship between tags and 
 * transaction splits, i.e. one tag can be (and typically is) 
 * assigned to several splits and vice versa.  
 */
public interface KMyMoneyTag extends KMyMoneyObject, 
									 HasTransactions
{
    
    /**
     * @return
     */
    KMMTagID getID();

	// ---------------------------------------------------------------

    /**
     * @return
     */
    String getName();

    /**
     * @return
     */
    String getColor();

    /**
     * @return
     */
    String getNotes();
    
	// ---------------------------------------------------------------

    /**
     * @return
     */
    boolean isClosed();

}

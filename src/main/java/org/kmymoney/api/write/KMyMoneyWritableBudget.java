package org.kmymoney.api.write;

import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;

/**
 * Budget that can be modified.
 * 
 * @see KMyMoneyBudget
 */
public interface KMyMoneyWritableBudget extends KMyMoneyBudget,
                                                KMyMoneyWritableObject
{

    void remove();
   
	// ---------------------------------------------------------------

    void setName(String name);

    // void setStart(LocalDate date);

}

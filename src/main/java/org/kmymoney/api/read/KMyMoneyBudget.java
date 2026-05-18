package org.kmymoney.api.read;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

public interface KMyMoneyBudget extends KMyMoneyObject
{

    KMMBdgtID getID() throws InvalidQualifSecCurrIDException;
    
    String getName();
    
    LocalDate getStart();
    
    String getStartStr();
    
    String getVersion();
    
    // ---------------------------------------------------------------
    
    List<KMMBudgetAccount> getAccounts();
	
}

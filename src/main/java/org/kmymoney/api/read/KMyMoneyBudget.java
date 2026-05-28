package org.kmymoney.api.read;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;

public interface KMyMoneyBudget extends KMyMoneyObject
{

    KMMBdgtID getID();
    
    String getName();
    
    LocalDate getStart();
    
    String getStartStr();
    
    // ---------------------------------------------------------------
    
    List<KMMBudgetAccount> getAccounts();
	
    KMMBudgetAccount getAccount(KMMAcctID acctID);
	
    List<KMMBudgetPeriod>  getPeriods(KMMAcctID acctID);
	
}

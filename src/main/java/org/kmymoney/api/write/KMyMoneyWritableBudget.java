package org.kmymoney.api.write;

import java.time.LocalDate;
import java.util.List;

import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.write.aux.KMMWritableBudgetAccount;
import org.kmymoney.api.write.aux.KMMWritableBudgetPeriod;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

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

    void setStart(LocalDate date);

    // ---------------------------------------------------------------
    
    void clearAccounts();
    
    void removeAccount(KMMBudgetAccount bdgtAcct);
    
    KMMWritableBudgetAccount createWritableAccount(KMMAcctID acct, KMMBudgetAccount.Level lvl);
    
	List<KMMWritableBudgetAccount> getWritableAccounts();

	KMMWritableBudgetAccount getWritableAccount(KMMAcctID acctID);

	// ---------------------------------------------------------------
    
    void clearPeriods(KMMAcctID acctID);
    
	List<KMMWritableBudgetPeriod> getWritablePeriods(KMMAcctID acctID);

}

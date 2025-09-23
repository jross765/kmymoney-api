package org.kmymoney.api.read.hlp;

import java.util.List;

import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;

public interface HasAccounts
{
	
    boolean hasAccounts();

    List<KMyMoneyAccount> getAccounts();

    KMyMoneyAccount getAccountByID(KMMComplAcctID acctID);
    
}

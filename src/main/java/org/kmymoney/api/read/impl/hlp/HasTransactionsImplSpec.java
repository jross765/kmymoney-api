package org.kmymoney.api.read.impl.hlp;

import org.kmymoney.api.read.KMyMoneyTransactionSplit;

// *Internal* interface 
// (as opposed to HasTransactionsImplSpec in package "read.hlp")
public interface HasTransactionsImplSpec
{
	
    void addTransactionSplit(KMyMoneyTransactionSplit splt);

    void addTransactionSplit(KMyMoneyTransactionSplit splt, boolean withCheck);

    void removeTransactionSplit(KMyMoneyTransactionSplit splt);

    void removeTransactionSplit(KMyMoneyTransactionSplit splt, boolean strict);

}

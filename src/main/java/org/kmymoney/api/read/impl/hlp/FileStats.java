package org.kmymoney.api.read.impl.hlp;

// File statistics methods
// (primarily, but not exclusively, for test purposes)
public interface FileStats {
    
    public int ERROR = -1; // ::MAGIC
    
    // ---------------------------------------------------------------

    int getNofEntriesInstitutions();

    int getNofEntriesAccounts();

    int getNofEntriesTransactions();

    int getNofEntriesTransactionSplits();

    // ----------------------------
    
    int getNofEntriesPayees();
    
    int getNofEntriesTags();
    
    // ----------------------------
    
    int getNofEntriesSecurities();
    
    int getNofEntriesCurrencies();
    
    // ----------------------------
    
    int getNofEntriesPricePairs();

    int getNofEntriesPrices();

}

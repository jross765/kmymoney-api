package org.kmymoney.api.read;

import java.io.File;
import java.io.PrintStream;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.pricedb.ComplexPriceTable;
import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Acct;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Curr;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Inst;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Prc;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Pye;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Sec;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Tag;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_Trx;
import org.kmymoney.api.read.hlp.fil.KMyMoneyFile_TrxSplt;
import org.kmymoney.base.basetypes.simple.KMMCurrID;

/**
 * Interface of a top-level class that gives access to a KMyMoney file
 * with all its accounts, transactions, etc.
 */
public interface KMyMoneyFile extends KMyMoneyObject,
									  KMyMoneyFile_Inst,
									  KMyMoneyFile_Acct,
									  KMyMoneyFile_Trx,
									  KMyMoneyFile_TrxSplt,
									  KMyMoneyFile_Pye,
									  KMyMoneyFile_Sec,
									  KMyMoneyFile_Curr,
									  KMyMoneyFile_Prc,
									  KMyMoneyFile_Tag,
									  HasUserDefinedAttributes 
{

	/**
	 *
	 * @return the file on disk we are managing
	 */
	File getFile();

	@SuppressWarnings("exports")
	KMYMONEYFILE getRootElement();

	/**
	 * The Currency-Table gets initialized with the latest prices found in the
	 * KMyMoney file.
	 * 
	 * @return Returns the currencyTable.
	 */
	ComplexPriceTable getCurrencyTable();

	/**
	 * Use a heuristic to determine the default currency ID. If we cannot find one,
	 * the method returns the default currency from Const..
	 * 
	 * @return the default-currency's ID to use.
	 */
	KMMCurrID getDefaultCurrencyID();

	@Deprecated
	String getDefaultCurrencyIDStr();

    // ---------------------------------------------------------------
    
    void dump(PrintStream strm);

}

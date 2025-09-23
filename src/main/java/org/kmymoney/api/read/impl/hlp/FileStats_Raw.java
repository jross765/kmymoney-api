package org.kmymoney.api.read.impl.hlp;

import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStats_Raw implements FileStats {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FileStats_Raw.class);

	// ---------------------------------------------------------------

	private KMyMoneyFileImpl kmmFile = null;

	// ---------------------------------------------------------------

	public FileStats_Raw(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
	}

	// ---------------------------------------------------------------

	@Override
	public int getNofEntriesInstitutions() {
		return kmmFile.getRootElement().getINSTITUTIONS().getINSTITUTION().size();
	}

	@Override
	public int getNofEntriesAccounts() {
		return kmmFile.getRootElement().getACCOUNTS().getACCOUNT().size();
	}

	@Override
	public int getNofEntriesTransactions() {
		return kmmFile.getRootElement().getTRANSACTIONS().getTRANSACTION().size();
	}

	@Override
	public int getNofEntriesTransactionSplits() {
		int result = 0;

		for ( TRANSACTION trx : kmmFile.getRootElement().getTRANSACTIONS().getTRANSACTION() ) {
			result += trx.getSPLITS().getSPLIT().size();
		}

		return result;
	}

	// ----------------------------

	@Override
	public int getNofEntriesPayees() {
		return kmmFile.getRootElement().getPAYEES().getPAYEE().size();
	}

	@Override
	public int getNofEntriesTags() {
		return kmmFile.getRootElement().getTAGS().getTAG().size();
	}

	// ----------------------------

	@Override
	public int getNofEntriesSecurities() {
		return kmmFile.getRootElement().getSECURITIES().getSECURITY().size();
	}

	@Override
	public int getNofEntriesCurrencies() {
		return kmmFile.getRootElement().getCURRENCIES().getCURRENCY().size();
	}

	// ----------------------------

	@Override
	public int getNofEntriesPricePairs() {
		return kmmFile.getRootElement().getPRICES().getPRICEPAIR().size();
	}

	@Override
	public int getNofEntriesPrices() {
		int result = 0;

		for ( PRICEPAIR prcPr : kmmFile.getRootElement().getPRICES().getPRICEPAIR() ) {
			result += prcPr.getPRICE().size();
		}

		return result;
	}

}

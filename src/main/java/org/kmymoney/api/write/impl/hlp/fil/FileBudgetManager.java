package org.kmymoney.api.write.impl.hlp.fil;

import org.kmymoney.api.generated.BUDGET;
import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.impl.KMyMoneyBudgetImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableBudgetImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBudgetManager extends org.kmymoney.api.read.impl.hlp.fil.FileBudgetManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileBudgetManager.class);

	// ---------------------------------------------------------------

	public FileBudgetManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyBudgetImpl createBudget(final BUDGET jwsdpBdgt) {
		KMyMoneyWritableBudgetImpl bdgt = new KMyMoneyWritableBudgetImpl(jwsdpBdgt, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createBudget: Generated new writable budget: " + bdgt.getID());
		return bdgt;
	}

	// ---------------------------------------------------------------

	public void addBudget(KMyMoneyBudget bdgt) {
		if ( bdgt == null ) {
			throw new IllegalArgumentException("argument <bdgt> is null");
		}

		bdgtMap.put(bdgt.getID(), bdgt);
		LOGGER.debug("addBudget: Added budget to cache: " + bdgt.getID());
	}

	public void removeBudget(KMyMoneyBudget bdgt) {
		if ( bdgt == null ) {
			throw new IllegalArgumentException("argument <bdgt> is null");
		}

		bdgtMap.remove(bdgt.getID());
		LOGGER.debug("removeBudget: Added budget to cache: " + bdgt.getID());
	}

}

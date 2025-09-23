package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.PAYEE;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.impl.KMyMoneyPayeeImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritablePayeeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePayeeManager extends org.kmymoney.api.read.impl.hlp.FilePayeeManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FilePayeeManager.class);

	// ---------------------------------------------------------------

	public FilePayeeManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyPayeeImpl createPayee(final PAYEE jwsdpPye) {
		KMyMoneyWritablePayeeImpl pye = new KMyMoneyWritablePayeeImpl(jwsdpPye, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createPayee: Generated new writable payee: " + pye.getID());
		return pye;
	}

	// ---------------------------------------------------------------

	public void addPayee(KMyMoneyPayee pye) {
		if ( pye == null ) {
			throw new IllegalArgumentException("null payee given");
		}

		pyeMap.put(pye.getID(), pye);
		LOGGER.debug("addPayee: Added payee to cache: " + pye.getID());
	}

	public void removePayee(KMyMoneyPayee pye) {
		if ( pye == null ) {
			throw new IllegalArgumentException("null payee given");
		}

		pyeMap.remove(pye.getID());
		LOGGER.debug("removePayee: Removed payee from cache: " + pye.getID());
	}

}

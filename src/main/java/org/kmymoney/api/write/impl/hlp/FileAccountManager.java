package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.impl.KMyMoneyAccountImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableAccountImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAccountManager extends org.kmymoney.api.read.impl.hlp.FileAccountManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileAccountManager.class);

	// ---------------------------------------------------------------

	public FileAccountManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyAccountImpl createAccount(final ACCOUNT jwsdpAcct) {
		// ::TODO ::CHECK
		// CAUTION: As opposed to sister project for GnuCash, we have 
		// the situation here that the original, "naive" code *has to be* used,
		// whereas the code analogous to the corrected one in the GnuCash
		// project *does not work* and produces lots of errors in the
		// tests. 
		// Cannot explain this right away, will have to have a closer look
		// at some time in the future.
		// YES, this:
		KMyMoneyWritableAccountImpl wrtblAcct = new KMyMoneyWritableAccountImpl(jwsdpAcct, (KMyMoneyWritableFileImpl) kmmFile);
		// NO; NOT THIS:
		// KMyMoneyAccountImpl roAcct = super.createAccount(jwsdpAcct);
		// KMyMoneyWritableAccountImpl wrtblAcct = new KMyMoneyWritableAccountImpl((KMyMoneyAccountImpl) roAcct, true);
		LOGGER.debug("createAccount: Generated new writable account: " + wrtblAcct.getID());
		return wrtblAcct;
	}

	// ---------------------------------------------------------------

	public void addAccount(KMyMoneyAccount acct) {
		acctMap.put(acct.getID(), acct);
		LOGGER.debug("addAccount: Added account to cache: " + acct.getID());
	}

	public void removeAccount(KMyMoneyAccount acct) {
		acctMap.remove(acct.getID());
		LOGGER.debug("removeAccount: Removed account from cache: " + acct.getID());
	}

}

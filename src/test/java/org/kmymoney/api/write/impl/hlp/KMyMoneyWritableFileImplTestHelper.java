package org.kmymoney.api.write.impl.hlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;

public class KMyMoneyWritableFileImplTestHelper extends KMyMoneyWritableFileImpl
{
	// ---------------------------------------------------------------

	public KMyMoneyWritableFileImplTestHelper(final File pFile) throws IOException {
		super(pFile);
	}
	
	public KMyMoneyWritableFileImplTestHelper(final InputStream is) throws IOException {
		super(is);
	}

	// ---------------------------------------------------------------
	// For test purposes only

	public org.kmymoney.api.write.impl.hlp.fil.FilePayeeManager getPayeeManager() {
		return (org.kmymoney.api.write.impl.hlp.fil.FilePayeeManager) pyeMgr;
	}

	public org.kmymoney.api.write.impl.hlp.fil.FileSecurityManager getSecurityManager() {
		return (org.kmymoney.api.write.impl.hlp.fil.FileSecurityManager) secMgr;
	}

	public org.kmymoney.api.write.impl.hlp.fil.FileCurrencyManager getCurrencyManager() {
		return (org.kmymoney.api.write.impl.hlp.fil.FileCurrencyManager) currMgr;
	}

	public org.kmymoney.api.write.impl.hlp.fil.FilePriceManager getPriceManager() {
		return (org.kmymoney.api.write.impl.hlp.fil.FilePriceManager) prcMgr;
	}

}

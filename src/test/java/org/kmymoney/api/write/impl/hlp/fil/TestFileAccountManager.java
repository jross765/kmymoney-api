package org.kmymoney.api.write.impl.hlp.fil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

import junit.framework.JUnit4TestAdapter;

public class TestFileAccountManager {

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImplTestHelper kmmInFile = null;

	private org.kmymoney.api.write.impl.hlp.fil.FileAccountManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFileAccountManager.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmInFileStream = null;
		try {
			kmmInFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmInFile = new KMyMoneyWritableFileImplTestHelper(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	
	@Test
	public void test01() throws Exception {
		mgr = kmmInFile.getAccountManager();
		
		assertEquals(ConstTest.Stats.NOF_ACCT, mgr.getNofEntriesAccountMap());
		assertEquals(ConstTest.Stats.NOF_ACCT, mgr.getAccounts().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmInFile.getAccountManager();
		
		Collection<KMyMoneyAccount> acctColl = mgr.getAccounts();
		KMMAcctID acctID = new KMMAcctID("A000004");
		KMyMoneyAccount acct = mgr.getAccountByID(acctID);
		assertTrue(acctColl.contains(acct));
	}

	@Test
	public void test03() throws Exception {
		mgr = kmmInFile.getAccountManager();
		
		KMMAcctID acctID = new KMMAcctID("A000045");
		
		assertEquals(ConstTest.Stats.NOF_ACCT, mgr.getAccounts().size());
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(acctID);
		kmmInFile.removeAccount(acct);
		assertEquals(ConstTest.Stats.NOF_ACCT - 1, mgr.getAccounts().size());
		
		// has splits:
		acctID = new KMMAcctID("A000004");
		acct = kmmInFile.getWritableAccountByID(acctID);
		try {
			kmmInFile.removeAccount(acct);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		assertEquals(ConstTest.Stats.NOF_ACCT - 1, mgr.getAccounts().size());
	}

	@Test
	public void test04() throws Exception {
		mgr = kmmInFile.getAccountManager();
		
		KMMAcctID acctID = new KMMAcctID("A000004");
		KMyMoneyAccount acct = mgr.getAccountByID(acctID);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFile.getTransactionSplits().size());
		assertEquals(17, acct.getTransactionSplits().size());
		
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(KMMQualifSpltID.parse("T000000000000000001:S0001"));
		kmmInFile.removeTransactionSplit(splt);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 1, kmmInFile.getTransactionSplits().size());
		assertEquals(16, acct.getTransactionSplits().size());
	}

}

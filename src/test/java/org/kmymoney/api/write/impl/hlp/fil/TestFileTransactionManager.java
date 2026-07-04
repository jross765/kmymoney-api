package org.kmymoney.api.write.impl.hlp.fil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestFileTransactionManager {

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImplTestHelper kmmInFile = null;

	private org.kmymoney.api.write.impl.hlp.fil.FileTransactionManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFileTransactionManager.class);
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
		mgr = kmmInFile.getTransactionManager();
		
		assertEquals(ConstTest.Stats.NOF_TRX, mgr.getNofEntriesTransactionMap());
		assertEquals(ConstTest.Stats.NOF_TRX, mgr.getTransactions().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmInFile.getTransactionManager();
		
		List<? extends KMyMoneyTransaction> trxColl = mgr.getTransactions();
		KMMTrxID trxID = new KMMTrxID("T000000000000000001");
		KMyMoneyTransaction trx = mgr.getTransactionByID(trxID);
		assertTrue(trxColl.contains(trx));
		
		KMMSpltID splt1ID = new KMMSpltID("S0001");
		KMMSpltID splt2ID = new KMMSpltID("S0002");
		KMMQualifSpltID qualifSplt1ID = new KMMQualifSpltID(trxID, splt1ID);
		KMMQualifSpltID qualifSplt2ID = new KMMQualifSpltID(trxID, splt2ID);
		KMyMoneyTransactionSplit splt1 = mgr.getTransactionSplitByID(qualifSplt1ID);
		KMyMoneyTransactionSplit splt2 = mgr.getTransactionSplitByID(qualifSplt2ID);
		
		List<KMyMoneyTransactionSplit> spltColl = mgr.getTransactionSplits();
		assertTrue(spltColl.contains(splt1));
		assertTrue(spltColl.contains(splt2));
	}

	@Test
	public void test03_1() throws Exception {
		mgr = kmmInFile.getTransactionManager();
		
		KMMTrxID trxID = new KMMTrxID("T000000000000000001");

		assertEquals(ConstTest.Stats.NOF_TRX, mgr.getTransactions().size());
		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(trxID);
		kmmInFile.removeTransaction(trx);
		assertEquals(ConstTest.Stats.NOF_TRX - 1, mgr.getTransactions().size());
	}

	@Test
	public void test03_2() throws Exception {
		mgr = kmmInFile.getTransactionManager();
		
		KMMTrxID trxID = new KMMTrxID("T000000000000000001");
		KMMSpltID spltID = new KMMSpltID("S0001");
		KMMQualifSpltID qualifSpltID = new KMMQualifSpltID(trxID, spltID);

		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, mgr.getTransactionSplits().size());
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(qualifSpltID);
		kmmInFile.removeTransactionSplit(splt);
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 1, mgr.getTransactionSplits().size());
	}

}

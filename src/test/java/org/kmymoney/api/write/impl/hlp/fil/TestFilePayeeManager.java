package org.kmymoney.api.write.impl.hlp.fil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import junit.framework.JUnit4TestAdapter;

public class TestFilePayeeManager {

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImplTestHelper kmmInFile = null;

	private org.kmymoney.api.write.impl.hlp.fil.FilePayeeManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFilePayeeManager.class);
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
		mgr = kmmInFile.getPayeeManager();
		
		assertEquals(ConstTest.Stats.NOF_PYE, mgr.getNofEntriesPayeeMap());
		assertEquals(ConstTest.Stats.NOF_PYE, mgr.getPayees().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmInFile.getPayeeManager();
		
		Collection<KMyMoneyPayee> pyeColl = mgr.getPayees();
		KMMPyeID pyeID = new KMMPyeID("P000001");
		KMyMoneyPayee pye = mgr.getPayeeByID(pyeID);
		assertTrue(pyeColl.contains(pye));
	}

	@Test
	public void test03() throws Exception {
		mgr = kmmInFile.getPayeeManager();
		
		// has no splits:
		KMMPyeID pyeID = new KMMPyeID("P000001");
		assertEquals(ConstTest.Stats.NOF_PYE, mgr.getPayees().size());
		KMyMoneyWritablePayee pye = kmmInFile.getWritablePayeeByID(pyeID);
		kmmInFile.removePayee(pye);
		assertEquals(ConstTest.Stats.NOF_PYE - 1, mgr.getPayees().size());

		// has splits:
		pyeID = new KMMPyeID("P000004");
		pye = kmmInFile.getWritablePayeeByID(pyeID);
		try {
			kmmInFile.removePayee(pye);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		assertEquals(ConstTest.Stats.NOF_PYE - 1, mgr.getPayees().size());
	}

	@Test
	public void test04() throws Exception {
		mgr = kmmInFile.getPayeeManager();
		
		KMMPyeID pyeID = new KMMPyeID("P000007");
		KMyMoneyPayee pye = mgr.getPayeeByID(pyeID);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFile.getTransactionSplits().size());
		assertEquals(12, pye.getTransactionSplits().size());
		
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(KMMQualifSpltID.parse("T000000000000000003:S0001"));
		assertEquals(pyeID, splt.getPayeeID());
		kmmInFile.removeTransactionSplit(splt);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 1, kmmInFile.getTransactionSplits().size());
		assertEquals(11, pye.getTransactionSplits().size()); // changed
	}

}

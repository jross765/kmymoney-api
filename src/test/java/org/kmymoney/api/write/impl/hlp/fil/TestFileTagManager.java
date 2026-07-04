package org.kmymoney.api.write.impl.hlp.fil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;

import junit.framework.JUnit4TestAdapter;

public class TestFileTagManager {

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImplTestHelper kmmInFile = null;

	private org.kmymoney.api.write.impl.hlp.fil.FileTagManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFileTagManager.class);
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
		mgr = kmmInFile.getTagManager();
		
		assertEquals(ConstTest.Stats.NOF_TAG, mgr.getNofEntriesTagMap());
		assertEquals(ConstTest.Stats.NOF_TAG, mgr.getTags().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmInFile.getTagManager();
		
		Collection<KMyMoneyTag> tagColl = mgr.getTags();
		KMMTagID tagID = new KMMTagID("G000001");
		KMyMoneyTag tag = mgr.getTagByID(tagID);
		assertTrue(tagColl.contains(tag));
	}

	@Test
	public void test03() throws Exception {
		mgr = kmmInFile.getTagManager();
		
		// has no splits:
		KMMTagID tagID = new KMMTagID("G000003");
		assertEquals(ConstTest.Stats.NOF_TAG, mgr.getTags().size());
		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(tagID);
		kmmInFile.removeTag(tag);
		assertEquals(ConstTest.Stats.NOF_TAG - 1, mgr.getTags().size());
		
		// has splits:
		tagID = new KMMTagID("G000001");
		tag = kmmInFile.getWritableTagByID(tagID);
		try {
			kmmInFile.removeTag(tag);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		assertEquals(ConstTest.Stats.NOF_TAG - 1, mgr.getTags().size());
	}

	@Test
	public void test04() throws Exception {
		mgr = kmmInFile.getTagManager();
		
		KMMTagID tagID = new KMMTagID("G000001");
		KMyMoneyTag pye = mgr.getTagByID(tagID);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT, kmmInFile.getTransactionSplits().size());
		assertEquals(2, pye.getTransactionSplits().size());
		
		KMyMoneyWritableTransactionSplit splt = kmmInFile.getWritableTransactionSplitByID(KMMQualifSpltID.parse("T000000000000000003:S0001"));
		assertEquals(2, splt.getTagIDs().size());
		assertTrue( splt.getTagIDs().contains(tagID) );
		kmmInFile.removeTransactionSplit(splt);
		
		assertEquals(ConstTest.Stats.NOF_TRX_SPLT - 1, kmmInFile.getTransactionSplits().size());
		assertEquals(1, pye.getTransactionSplits().size()); // changed
	}

}

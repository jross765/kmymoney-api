package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.base.basetypes.simple.KMMTagID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyTagImpl {
	public static final KMMTagID TAG_1_ID = new KMMTagID("G000001"); // froufrou
	public static final KMMTagID TAG_2_ID = new KMMTagID("G000002"); // frifri
	public static final KMMTagID TAG_3_ID = new KMMTagID("G000003"); // kiki

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyTag tag = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyTagImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmFileStream = null;
		try {
			kmmFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		tag = kmmFile.getTagByID(TAG_1_ID);
		assertNotEquals(null, tag);

		assertEquals(TAG_1_ID, tag.getID());
		assertEquals("froufrou", tag.getName());
		assertEquals("#ff0000", tag.getColor());
		
		assertEquals(true, tag.hasTransactions());
		assertEquals(2, tag.getTransactions().size());
		assertEquals("T000000000000000003", tag.getTransactions().get(0).getID().toString()); 
		assertEquals("T000000000000000004", tag.getTransactions().get(1).getID().toString()); 
	}

	@Test
	public void test01_2() throws Exception {
		tag = kmmFile.getTagByID(TAG_2_ID);
		assertNotEquals(null, tag);

		assertEquals(TAG_2_ID, tag.getID());
		assertEquals("frifri", tag.getName());
		assertEquals("#00aa00", tag.getColor());
		
		assertEquals(true, tag.hasTransactions());
		assertEquals(2, tag.getTransactions().size());
		assertEquals("T000000000000000002", tag.getTransactions().get(0).getID().toString()); 
		assertEquals("T000000000000000003", tag.getTransactions().get(1).getID().toString()); 
	}

	@Test
	public void test01_3() throws Exception {
		tag = kmmFile.getTagByID(TAG_3_ID);
		assertNotEquals(null, tag);

		assertEquals(TAG_3_ID, tag.getID());
		assertEquals("kiki", tag.getName());
		assertEquals("#000000", tag.getColor());
		
		assertEquals(false, tag.hasTransactions());
	}

}

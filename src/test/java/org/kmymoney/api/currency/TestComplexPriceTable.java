package org.kmymoney.api.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;

import junit.framework.JUnit4TestAdapter;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class TestComplexPriceTable {
	private KMyMoneyFile kmmFile = null;
	private ComplexPriceTable complPriceTab = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestComplexPriceTable.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.GCSH_FILENAME);
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
	public void test01() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		assertEquals(2, complPriceTab.getNameSpaces().size());

		List<KMMQualifSecCurrID.Type> nameSpaceList = complPriceTab.getNameSpaces();
		assertEquals(KMMQualifSecCurrID.Type.CURRENCY, nameSpaceList.get(0));
		assertEquals(KMMQualifSecCurrID.Type.SECURITY, nameSpaceList.get(1));
	}

	@Test
	public void test02() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		assertEquals(119.50,
				complPriceTab.getConversionFactor(KMMQualifSecCurrID.Type.SECURITY, "E000001").doubleValue(),
				ConstTest.DIFF_TOLERANCE);
		assertEquals(58.25,
				complPriceTab.getConversionFactor(KMMQualifSecCurrID.Type.SECURITY, "E000002").doubleValue(),
				ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test03_1() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		FixedPointNumber val = new FixedPointNumber("101.0");
		assertEquals(true, complPriceTab.convertToBaseCurrency(val, new KMMQualifSecID("E000001")));
		assertEquals(12069.50, val.doubleValue(), ConstTest.DIFF_TOLERANCE);

		val = new FixedPointNumber("101.0");
		assertEquals(true, complPriceTab.convertToBaseCurrency(val, new KMMQualifSecID("E000002")));
		assertEquals(5883.25, val.doubleValue(), ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test03_2() throws Exception {
		complPriceTab = kmmFile.getCurrencyTable();
		assertNotEquals(null, complPriceTab);

		FixedPointNumber val = new FixedPointNumber("12069.50");
		assertEquals(true, complPriceTab.convertFromBaseCurrency(val, new KMMQualifSecID("E000001")));
		assertEquals(101.0, val.doubleValue(), ConstTest.DIFF_TOLERANCE);

		val = new FixedPointNumber("5883.25");
		assertEquals(true, complPriceTab.convertFromBaseCurrency(val, new KMMQualifSecID("E000002")));
		assertEquals(101.0, val.doubleValue(), ConstTest.DIFF_TOLERANCE);
	}
}

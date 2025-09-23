package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyCurrencyImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableCurrency;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableCurrencyImpl {
	private static final String CURR_1_ID     = TestKMyMoneyCurrencyImpl.CURR_1_ID;
	private static final String CURR_1_SYMB   = TestKMyMoneyCurrencyImpl.CURR_1_SYMB;
	
	private static final String CURR_2_ID     = TestKMyMoneyCurrencyImpl.CURR_2_ID;
	private static final String CURR_2_SYMB   = TestKMyMoneyCurrencyImpl.CURR_2_SYMB;

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMyMoneyWritableCurrency curr = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMQualifCurrID currID1 = null;
	private KMMQualifCurrID currID2 = null;

	private KMMQualifCurrID newID = new KMMQualifCurrID();

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyWritableCurrencyImpl.class);
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
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}

		// ---

		currID1 = new KMMQualifCurrID(CURR_1_ID);
		currID2 = new KMMQualifCurrID(CURR_2_ID);
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyCurrencyImpl.test01_1/01_4
	//
	// Check whether the KMyMoneyWritableCurrency objects returned by
	// KMyMoneyWritableFileImpl.getWritableCurrencyByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getCurrencyByID().

	@Test
	public void test01_1() throws Exception {
		curr = kmmInFile.getWritableCurrencyByQualifID(new KMMQualifCurrID(CURR_1_ID));
		assertNotEquals(null, curr);

		assertEquals(currID1.toString(), curr.getQualifID().toString());
		assertNotEquals(currID1, curr);
		assertEquals("Euro", curr.getName());
		assertEquals(4, curr.getPP().intValue());
		assertEquals(100, curr.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, curr.getRoundingMethod());
	}

	@Test
	public void test01_2() throws Exception {
		curr = kmmInFile.getWritableCurrencyByID(CURR_2_ID);
		assertNotEquals(null, curr);

		assertEquals(currID2.toString(), curr.getQualifID().toString());
		assertNotEquals(currID2, curr);
		assertEquals(CURR_2_SYMB, curr.getSymbol());
		assertEquals("US-Dollar", curr.getName());
		assertEquals(4, curr.getPP().intValue());
		assertEquals(100, curr.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, curr.getRoundingMethod());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableCurrency objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		KMyMoneyWritableCurrency curr = kmmInFile.getWritableCurrencyByQualifID(currID1);
		assertNotEquals(null, curr);

		assertEquals(currID1, curr.getQualifID());
		assertEquals(CURR_1_ID, curr.getQualifID().getCode());

		// ----------------------------
		// Modify the object

		curr.setName("British Pound");
		curr.setSymbol("£");
		curr.setPP(BigInteger.valueOf(3));
		curr.setSAF(BigInteger.valueOf(1000));
		// ::TODO
		// curr.setRoundingMethod(KMMSecCurr.RoundingMethod.CEIL);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(curr);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test02_1_check_persisted(outFile);
	}

	@Test
	public void test02_2() throws Exception {
		// ::TODO
	}

	private void test02_1_check_memory(KMyMoneyWritableCurrency curr) throws Exception {
		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		assertEquals(currID1, curr.getQualifID()); // unchanged
		assertEquals(CURR_1_ID, curr.getQualifID().getCode()); // unchanged
		assertEquals("British Pound", curr.getName()); // changed
		assertEquals("£", curr.getSymbol()); // changed
		assertEquals(3, curr.getPP().intValue()); // changed
		assertEquals(1000, curr.getSAF().intValue()); // changed
		// ::TODO
		// assertEquals(KMMSecCurr.RoundingMethod.CEIL, curr.getRoundingMethod()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_CURR, kmmOutFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR, kmmOutFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		KMyMoneyCurrency curr = kmmOutFile.getCurrencyByID(CURR_1_ID);
		assertNotEquals(null, curr);

		assertEquals(currID1, curr.getQualifID()); // unchanged
		assertEquals(CURR_1_ID, curr.getQualifID().getCode()); // unchanged
		assertEquals("British Pound", curr.getName()); // changed
		assertEquals("£", curr.getSymbol()); // changed
		assertEquals(3, curr.getPP().intValue()); // changed
		assertEquals(1000, curr.getSAF().intValue()); // changed
		// ::TODO
		// assertEquals(KMMSecCurr.RoundingMethod.CEIL, curr.getRoundingMethod()); // changed
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		KMyMoneyWritableCurrency curr = kmmInFile.createWritableCurrency("BMD", "Bermudian Dollar");
		newID.set(curr.getQualifID());

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(curr);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCurrencyImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMyMoneyWritableCurrency curr) throws Exception {
		assertEquals(ConstTest.Stats.NOF_CURR + 1, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR + 1, kmmInFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		assertEquals(newID.toString(), curr.getQualifID().toString());
		assertEquals("Bermudian Dollar", curr.getName());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_CURR + 1, kmmOutFileStats.getNofEntriesCurrencies(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_CURR + 1, kmmOutFileStats.getNofEntriesCurrencies(KMMFileStats.Type.CACHE));

		KMyMoneyCurrency curr = kmmOutFile.getCurrencyByQualifID(newID);
		assertNotEquals(null, curr);

		assertEquals(newID.toString(), curr.getQualifID().toString());
		assertEquals(newID, curr.getQualifID());
		assertEquals("Bermudian Dollar", curr.getName());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	@Test
	public void test03_2_1() throws Exception {
		KMyMoneyWritableCurrency curr = 
				kmmInFile.createWritableCurrency(
						"BMD", 
						"Bermudian Dollar");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCurrencyImpl.test01_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_1_check_1_xmllint(outFile);
		test03_2_1_check_2(outFile);
	}

	// -----------------------------------------------------------------

	//  @Test
	//  public void test03_2_2() throws Exception
	//  {
	//      assertNotEquals(null, outFileGlob);
	//      assertEquals(true, outFileGlob.exists());
	//
	//      // Check if generated document is valid
	//      // ::TODO: in fact, not even the input document is.
	//      // Build document
	//      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//      DocumentBuilder builder = factory.newDocumentBuilder(); 
	//      Document document = builder.parse(outFileGlob);
	//      System.err.println("xxxx XML parsed");
	//
	//      // https://howtodoinjava.com/java/xml/read-xml-dom-parser-example/
	//      Schema schema = null;
	//      String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	//      SchemaFactory factory1 = SchemaFactory.newInstance(language);
	//      schema = factory1.newSchema(outFileGlob);
	//
	//      Validator validator = schema.newValidator();
	//      DOMResult validResult = null; 
	//      validator.validate(new DOMSource(document), validResult);
	//      System.out.println("yyy: " + validResult);
	//      // assertEquals(validResult);
	//  }

	// Sort of "soft" variant of above function
	// CAUTION: Not platform-independent!
	// Tool "xmllint" must be installed and in path
	private void test03_2_1_check_1_xmllint(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Check if generated document is valid
		ProcessBuilder bld = new ProcessBuilder("xmllint", outFile.getAbsolutePath());
		Process prc = bld.start();

		if ( prc.waitFor() == 0 ) {
			assertEquals(0, 0);
		} else {
			assertEquals(0, 1);
		}
	}

	private void test03_2_1_check_2(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(outFile);
		//      System.err.println("xxxx XML parsed");

		// Normalize the XML structure
		document.getDocumentElement().normalize();
		//      System.err.println("xxxx XML normalized");

		NodeList nList = document.getElementsByTagName("CURRENCY");
		assertEquals(ConstTest.Stats.NOF_CURR + 1, nList.getLength());

		// Last (new) node
		Node lastNode = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());
		Element elt = (Element) lastNode;
		assertEquals("Bermudian Dollar", elt.getAttribute("name"));
		assertEquals("BMD", elt.getAttribute("symbol"));
	}

	//    // -----------------------------------------------------------------

	@Test
	public void test03_2_2() throws Exception {
		KMyMoneyWritableCurrency curr1 = 
				kmmInFile.createWritableCurrency(
						"RUB", 
						"Russia Ruble");
		curr1.setSymbol("₽");

		KMyMoneyWritableCurrency curr2 = 
				kmmInFile.createWritableCurrency(
						"COP", 
						"Colombian Peso");
		curr2.setSymbol("$"); // sic

		KMyMoneyWritableCurrency curr3 = 
				kmmInFile.createWritableCurrency(
						"ILS", 
						"Israeli shekel");
		curr3.setSymbol("₪");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCurrencyImpl.test02_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_2_check(outFile);
	}

	private void test03_2_2_check(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(outFile);
		//      System.err.println("xxxx XML parsed");

		// Normalize the XML structure
		document.getDocumentElement().normalize();
		//      System.err.println("xxxx XML normalized");

		NodeList nList = document.getElementsByTagName("CURRENCY");
		assertEquals(ConstTest.Stats.NOF_CURR + 3, nList.getLength());

		// Last three nodes (the new ones)
		Node node = nList.item(nList.getLength() - 3);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		Element elt = (Element) node;
		assertEquals("RUB", elt.getAttribute("id"));
		assertEquals("Russia Ruble", elt.getAttribute("name"));
		assertEquals("₽", elt.getAttribute("symbol"));

		node = nList.item(nList.getLength() - 2);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("COP", elt.getAttribute("id"));
		assertEquals("Colombian Peso", elt.getAttribute("name"));
		assertEquals("$", elt.getAttribute("symbol"));

		node = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("ILS", elt.getAttribute("id"));
		assertEquals("Israeli shekel", elt.getAttribute("name"));
		assertEquals("₪", elt.getAttribute("symbol"));
	}

}

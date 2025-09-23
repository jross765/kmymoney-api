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
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneySecurityImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableCurrency;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableSecurityImpl {
	
	private static final KMMSecID SEC_1_ID   = TestKMyMoneySecurityImpl.SEC_1_ID;
	private static final String SEC_1_ISIN   = TestKMyMoneySecurityImpl.SEC_1_ISIN;
	private static final String SEC_1_TICKER = TestKMyMoneySecurityImpl.SEC_1_TICKER;

	private static final KMMSecID SEC_2_ID   = TestKMyMoneySecurityImpl.SEC_2_ID;
	private static final String SEC_2_ISIN   = TestKMyMoneySecurityImpl.SEC_2_ISIN;
	private static final String SEC_2_TICKER = TestKMyMoneySecurityImpl.SEC_2_TICKER;

	private static final KMMSecID SEC_4_ID   = TestKMyMoneySecurityImpl.SEC_4_ID;
	private static final String SEC_4_ISIN   = TestKMyMoneySecurityImpl.SEC_4_ISIN;
	private static final String SEC_4_TICKER = TestKMyMoneySecurityImpl.SEC_4_TICKER;

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMyMoneyWritableSecurity sec = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMSecID newID = new KMMSecID();

	private KMMQualifSecID secID1 = null;
	private KMMQualifSecID secID2 = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableSecurityImpl.class);
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

		secID1 = new KMMQualifSecID(SEC_1_ID);
		secID2 = new KMMQualifSecID(SEC_2_ID);
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneySecurityImpl.test01_1/01_4
	//
	// Check whether the KMyMoneyWritableSecurity objects returned by
	// KMyMoneyWritableFileImpl.getWritableSecurityByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getSecurityByID().

	@Test
	public void test01_1() throws Exception {
		sec = kmmInFile.getWritableSecurityByQualifID(secID1);
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		// *Not* equal because of class
		assertNotEquals(secID1, sec.getID());
		// ::TODO: Convert to SecurityID_Exchange, then it should be equal
		//    assertEquals(secCurrID1, sec.getQualifID()); // not trivial!
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_3() throws Exception {
		sec = kmmInFile.getWritableSecurityBySymbol(SEC_1_TICKER);
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_4() throws Exception {
		sec = kmmInFile.getWritableSecurityByCode(SEC_1_ISIN);
		assertNotEquals(null, sec);

		assertEquals(secID1.toString(), sec.getQualifID().toString());
		assertNotEquals(secID1, sec);
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals(SEC_1_TICKER, sec.getSymbol());
		assertEquals(SEC_1_ISIN, sec.getCode());
		assertEquals("Mercedes-Benz Group AG", sec.getName());
		assertEquals(2, sec.getPP().intValue());
		assertEquals(100, sec.getSAF().intValue());
		assertEquals(KMMSecCurr.RoundingMethod.ROUND, sec.getRoundingMethod());
		assertEquals(new KMMQualifCurrID("EUR"), sec.getTradingCurrency());
		assertEquals("XETRA", sec.getTradingMarket());
	}

	@Test
	public void test01_5() throws Exception {
		List<KMyMoneyWritableSecurity> secList = kmmInFile.getWritableSecuritiesByName("mercedes");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());

		assertEquals(secID1.toString(), ((KMyMoneySecurity) secList.toArray()[0]).getQualifID().toString());
		assertEquals(secID1, ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
		// ::TODO: Convert to SecurityID_Exchange, then it should be equal
		//    assertEquals(secCurrID1, 
		//	        ((KMyMoneySecurity) secList.toArray()[0]).getQualifID()); // not trivial!
		// ::TODO
		// assertEquals(SEC_1_ISIN, ((KMyMoneySecurity)
		// secList.toArray()[0]).getSymbol());
		assertEquals(SEC_1_TICKER, ((KMyMoneySecurity) secList.toArray()[0]).getSymbol());
		assertEquals("Mercedes-Benz Group AG", ((KMyMoneySecurity) secList.toArray()[0]).getName());

		secList = kmmInFile.getWritableSecuritiesByName("BENZ");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());
		assertEquals(secID1, ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
		// ::TODO: Convert to SecurityID_Exchange, then it should be equal
		//    assertEquals(secCurrID1, 
		//	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());

		secList = kmmInFile.getWritableSecuritiesByName(" MeRceDeS-bEnZ  ");
		assertNotEquals(null, secList);
		assertEquals(1, secList.size());
		assertEquals(secID1.toString(), ((KMyMoneySecurity) secList.toArray()[0]).getQualifID().toString());
		assertEquals(secID1, ((KMyMoneySecurity) secList.toArray()[0]).getQualifID());
		// ::TODO: Convert to SecurityID_Exchange, then it should be equal
		//    assertEquals(secCurrID1, 
		//	         ((KMyMoneySecurity) secList.toArray()[0]).getQualifID()); // not trivial!
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableSecurity objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByQualifID(secID1);
		assertNotEquals(null, sec);

		assertEquals(secID1, sec.getQualifID());
		assertEquals(SEC_1_ID.toString(), sec.getQualifID().getCode());

		// ----------------------------
		// Modify the object

		sec.setType(KMMSecCurr.Type.MUTUAL_FUND);
		sec.setName("Benzedes Merc");
		sec.setCode("BNZMRC");
		sec.setSymbol("DE00071BNZ00");
		sec.setPP(BigInteger.valueOf(3));
		sec.setSAF(BigInteger.valueOf(1000));
		sec.setTradingCurrency(new KMMQualifCurrID("CZK"));
		sec.setTradingMarket("Kleinkleckersdorf a. d. Lahn");
		// ::TODO
		// sec.setRoundingMethod(KMMSecCurr.RoundingMethod.CEIL);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(sec);

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

	private void test02_1_check_memory(KMyMoneyWritableSecurity sec) throws Exception {
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		assertEquals(secID1, sec.getQualifID()); // unchanged
		assertEquals(SEC_1_ID.toString(), sec.getQualifID().getCode()); // unchanged
		assertEquals(KMMSecCurr.Type.MUTUAL_FUND, sec.getType()); // changed
		assertEquals("Benzedes Merc", sec.getName()); // changed
		assertEquals("BNZMRC", sec.getCode()); // changed
		assertEquals("DE00071BNZ00", sec.getSymbol()); // changed
		assertEquals(3, sec.getPP().intValue()); // changed
		assertEquals(1000, sec.getSAF().intValue()); // changed
		assertEquals("CZK", sec.getTradingCurrency().getCode()); // changed
		assertEquals("Kleinkleckersdorf a. d. Lahn", sec.getTradingMarket()); // changed
		// ::TODO
		// assertEquals(KMMSecCurr.RoundingMethod.CEIL, sec.getRoundingMethod()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_SEC, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneySecurity sec = kmmOutFile.getSecurityByID(SEC_1_ID);
		assertNotEquals(null, sec);

		assertEquals(secID1, sec.getQualifID()); // unchanged
		assertEquals(SEC_1_ID.toString(), sec.getQualifID().getCode()); // unchanged
		assertEquals(KMMSecCurr.Type.MUTUAL_FUND, sec.getType()); // changed
		assertEquals("Benzedes Merc", sec.getName()); // changed
		assertEquals("BNZMRC", sec.getCode()); // changed
		assertEquals("DE00071BNZ00", sec.getSymbol()); // changed
		assertEquals(3, sec.getPP().intValue()); // changed
		assertEquals(1000, sec.getSAF().intValue()); // changed
		assertEquals("CZK", sec.getTradingCurrency().getCode()); // changed
		assertEquals("Kleinkleckersdorf a. d. Lahn", sec.getTradingMarket()); // changed
		// ::TODO
		// assertEquals(KMMSecCurr.RoundingMethod.CEIL, sec.getRoundingMethod()); // changed
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

		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneyWritableSecurity sec = kmmInFile.createWritableSecurity(KMMSecCurr.Type.STOCK, "X11823", "Best Corp Ever");
		newID.set(sec.getID());

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(sec);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableSecurityImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMyMoneyWritableSecurity sec) throws Exception {
		assertEquals(ConstTest.Stats.NOF_SEC + 1, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC + 1, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		assertEquals(newID.toString(), sec.getID().toString());
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals("Best Corp Ever", sec.getName());
		assertEquals("X11823", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_SEC + 1, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_SEC + 1, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneySecurity sec = kmmOutFile.getSecurityByID(newID);
		assertNotEquals(null, sec);

		assertEquals(newID.toString(), sec.getID().toString());
		assertEquals(KMMSecCurr.Type.STOCK, sec.getType());
		assertEquals("Best Corp Ever", sec.getName());
		assertEquals("X11823", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	@Test
	public void test03_2_1() throws Exception {
		KMyMoneyWritableSecurity sec = 
				kmmInFile.createWritableSecurity(
						KMMSecCurr.Type.STOCK, 
						"US0123456789", 
						"Scam and Screw Corp.");
		sec.setSymbol("SCAM");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableSecurityImpl.test01_1: '" + outFile.getPath() + "'");
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

		NodeList nList = document.getElementsByTagName("SECURITY");
		assertEquals(ConstTest.Stats.NOF_SEC + 1, nList.getLength());

		// Last (new) node
		Node lastNode = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());
		Element elt = (Element) lastNode;
		assertEquals("" + KMMSecCurr.Type.STOCK.getCode().intValue(), elt.getAttribute("type"));
		assertEquals("Scam and Screw Corp.", elt.getAttribute("name"));
		assertEquals("SCAM", elt.getAttribute("symbol"));

		Node kvpsNode = elt.getElementsByTagName("KEYVALUEPAIRS").item(0);
		assertNotEquals(null, kvpsNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpsNode.getNodeType());
		Element kvpsElt = (Element) kvpsNode;
		Node kvpNode = kvpsElt.getElementsByTagName("PAIR").item(0);
		assertNotEquals(null, kvpNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpNode.getNodeType());
		Element kvpElt = (Element) kvpNode;
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, kvpElt.getAttribute("key"));
		assertEquals("US0123456789", kvpElt.getAttribute("value"));
	}

	//    // -----------------------------------------------------------------

	@Test
	public void test03_2_2() throws Exception {
		KMyMoneyWritableSecurity sec1 = 
				kmmInFile.createWritableSecurity(
						KMMSecCurr.Type.STOCK, 
						"US0123456789", 
						"Scam and Screw Corp.");
		sec1.setSymbol("SCAM"); // <-- CAUTION: Symbol is a different field than the security ID above

		KMyMoneyWritableSecurity sec2 = 
				kmmInFile.createWritableSecurity(
						KMMSecCurr.Type.BOND, 
						"BE0123456789", 
						"Chocolaterie de la Grande Place");
		sec2.setSymbol("CHOC"); // dto.

		KMyMoneyWritableSecurity sec3 = 
				kmmInFile.createWritableSecurity(
						KMMSecCurr.Type.MUTUAL_FUND, 
						"FR0123456789", 
						"Ils sont fous ces dingos!");
		sec3.setSymbol("FOUS"); // dto.

		KMyMoneyWritableSecurity sec4 = 
				kmmInFile.createWritableSecurity(
						KMMSecCurr.Type.STOCK, 
						"GB10000A2222", 
						"Ye Ole National British Trade Company Ltd.");
		sec4.setSymbol("BTRD"); // dto.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableSecurityImpl.test02_1: '"
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

		NodeList nList = document.getElementsByTagName("SECURITY");
		assertEquals(ConstTest.Stats.NOF_SEC + 4, nList.getLength());

		// Last three nodes (the new ones)
		Node node = nList.item(nList.getLength() - 4);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		Element elt = (Element) node;
		assertEquals("" + KMMSecCurr.Type.STOCK.getCode().intValue(), elt.getAttribute("type"));
		assertEquals("Scam and Screw Corp.", elt.getAttribute("name"));
		assertEquals("SCAM", elt.getAttribute("symbol"));

		Node kvpsNode = elt.getElementsByTagName("KEYVALUEPAIRS").item(0);
		assertNotEquals(null, kvpsNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpsNode.getNodeType());
		Element kvpsElt = (Element) kvpsNode;
		Node kvpNode = kvpsElt.getElementsByTagName("PAIR").item(0);
		assertNotEquals(null, kvpNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpNode.getNodeType());
		Element kvpElt = (Element) kvpNode;
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, kvpElt.getAttribute("key"));
		assertEquals("US0123456789", kvpElt.getAttribute("value"));

		node = nList.item(nList.getLength() - 3);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("" + KMMSecCurr.Type.BOND.getCode().intValue(), elt.getAttribute("type"));
		assertEquals("Chocolaterie de la Grande Place", elt.getAttribute("name"));
		assertEquals("CHOC", elt.getAttribute("symbol"));

		kvpsNode = elt.getElementsByTagName("KEYVALUEPAIRS").item(0);
		assertNotEquals(null, kvpsNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpsNode.getNodeType());
		kvpsElt = (Element) kvpsNode;
		kvpNode = kvpsElt.getElementsByTagName("PAIR").item(0);
		assertNotEquals(null, kvpNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpNode.getNodeType());
		kvpElt = (Element) kvpNode;
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, kvpElt.getAttribute("key"));
		assertEquals("BE0123456789", kvpElt.getAttribute("value"));

		node = nList.item(nList.getLength() - 2);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("" + KMMSecCurr.Type.MUTUAL_FUND.getCode().intValue(), elt.getAttribute("type"));
		assertEquals("Ils sont fous ces dingos!", elt.getAttribute("name"));
		assertEquals("FOUS", elt.getAttribute("symbol"));

		kvpsNode = elt.getElementsByTagName("KEYVALUEPAIRS").item(0);
		assertNotEquals(null, kvpsNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpsNode.getNodeType());
		kvpsElt = (Element) kvpsNode;
		kvpNode = kvpsElt.getElementsByTagName("PAIR").item(0);
		assertNotEquals(null, kvpNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpNode.getNodeType());
		kvpElt = (Element) kvpNode;
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, kvpElt.getAttribute("key"));
		assertEquals("FR0123456789", kvpElt.getAttribute("value"));

		node = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("" + KMMSecCurr.Type.STOCK.getCode().intValue(), elt.getAttribute("type"));
		assertEquals("Ye Ole National British Trade Company Ltd.", elt.getAttribute("name"));
		assertEquals("BTRD", elt.getAttribute("symbol"));

		kvpsNode = elt.getElementsByTagName("KEYVALUEPAIRS").item(0);
		assertNotEquals(null, kvpsNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpsNode.getNodeType());
		kvpsElt = (Element) kvpsNode;
		kvpNode = kvpsElt.getElementsByTagName("PAIR").item(0);
		assertNotEquals(null, kvpNode.getNodeType());
		assertEquals(Node.ELEMENT_NODE, kvpNode.getNodeType());
		kvpElt = (Element) kvpNode;
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, kvpElt.getAttribute("key"));
		assertEquals("GB10000A2222", kvpElt.getAttribute("value"));
	}

	// -----------------------------------------------------------------
	// PART 4: Delete objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 4.1: High-Level
	// ------------------------------

	@Test
	public void test04_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW)); // sic +1 for template
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByID(SEC_1_ID);
		assertNotEquals(null, sec);
		assertEquals(SEC_1_ID.toString(), sec.getID().toString());

		// Objects attached
		assertNotEquals(0, sec.getQuotes().size()); // there are quotes (prices)
		assertNotEquals(0, sec.getTransactionSplits().size()); // there are transactions

		// ----------------------------
		// Delete the object

		try {
			kmmInFile.removeSecurity(sec); // Correctly fails because prices are attached
			assertEquals(1, 0);
		} catch ( ObjectCascadeException exc ) {
			assertEquals(0, 0);
		}
	}
	
	@Test
	public void test04_2() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW)); // sic +1 for template
		assertEquals(ConstTest.Stats.NOF_SEC, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByID(SEC_4_ID);
		assertNotEquals(null, sec);
		assertEquals(SEC_4_ID.toString(), sec.getID().toString());

		// Objects attached
		assertEquals(0, sec.getQuotes().size()); // no quotes (prices)
		assertEquals(0, sec.getTransactionSplits().size()); // no transactions

		// ----------------------------
		// Delete the object

		kmmInFile.removeSecurity(sec);

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test04_2_check_memory(sec);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCommodityImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_2_check_persisted(outFile);
	}
	
	// ---------------------------------------------------------------

	private void test04_2_check_memory(KMyMoneyWritableSecurity sec) throws Exception {
		assertEquals(ConstTest.Stats.NOF_SEC - 1, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW)); // sic +1 for template
		assertEquals(ConstTest.Stats.NOF_SEC - 1, kmmInFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Exception: no splits any more
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(SEC_4_ID.toString(), sec.getID().toString());
		assertEquals("The Coca Cola Co.", sec.getName());
		
		// However, the commodity cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableSecurity secNow1 = kmmInFile.getWritableSecurityByID(SEC_4_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		// Same for a non non-writable instance. 
		// However, due to design asymmetry, no exception is thrown here,
		// but the method just returns null.
		KMyMoneySecurity secNow2 = kmmInFile.getSecurityByID(SEC_4_ID);
		assertEquals(null, secNow2);

		// Attached objects (*not dependent*)
		// Bill terms, however, still exist because they are not
		// customer-specific (not in principle, at least).
		// xxx TODO
//		KMMBillTerms prcNow = kmmInFile.getBillTermsByID(BLLTRM_1_ID);
//		assertNotEquals(null, bllTrmNow);
	}

	private void test04_2_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);
		
		assertEquals(ConstTest.Stats.NOF_SEC - 1, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.RAW)); // sic +1 for template
		assertEquals(ConstTest.Stats.NOF_SEC - 1, kmmOutFileStats.getNofEntriesSecurities(KMMFileStats.Type.CACHE));

		// The transaction does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneySecurity sec = kmmOutFile.getSecurityByID(SEC_4_ID);
		assertEquals(null, sec); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------
	
	// ::EMPTY

}

package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyInstitutionImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableInstitutionImpl {
	private static final KMMInstID INST_1_ID = TestKMyMoneyInstitutionImpl.INST_1_ID;
	private static final KMMInstID INST_2_ID = TestKMyMoneyInstitutionImpl.INST_2_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMInstID newID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableInstitutionImpl.class);
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
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestKMyMoneyInstitutionImpl.test01_1/02_1
	//
	// Check whether the KMyMoneyWritableInstitution objects returned by
	// KMyMoneyWritableFileImpl.getWritableInstitutionByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getInstitutionByID().

	@Test
	public void test01_1() throws Exception {
		KMyMoneyWritableInstitution inst = kmmInFile.getWritableInstitutionByID(INST_1_ID);

		assertEquals(INST_1_ID, inst.getID());
		assertEquals("RaiBa", inst.getName());
	}

	@Test
	public void test01_2() throws Exception {
		KMyMoneyWritableInstitution inst = kmmInFile.getWritableInstitutionByID(INST_2_ID);

		assertEquals(INST_2_ID, inst.getID());
		assertEquals("My Gibberish Bank", inst.getName());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableInstitution objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		KMyMoneyWritableInstitution inst = kmmInFile.getWritableInstitutionByID(INST_1_ID);
		assertNotEquals(null, inst);

		assertEquals(INST_1_ID, inst.getID());

		// ----------------------------
		// Modify the object

		inst.setName("CCC Bank");
		
		// Does not work after conversion V. 5.1.3 -> V. 5.2:
//		inst.setBIC("xxxYYY"); // note mixed case
//		inst.setURL("ccc-bank.com");

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(inst);

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

	private void test02_1_check_memory(KMyMoneyWritableInstitution inst) throws Exception {
		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		assertEquals(INST_1_ID, inst.getID()); // unchanged
		assertEquals("CCC Bank", inst.getName()); // changed
		
		// Does not work after conversion V. 5.1.3 -> V. 5.2:
//		assertEquals("XXXYYY", inst.getBIC()); // changed, not upper case
//		assertEquals("ccc-bank.com", inst.getURL()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_INST, kmmOutFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST, kmmOutFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		KMyMoneyInstitution inst = kmmOutFile.getInstitutionByID(INST_1_ID);
		assertNotEquals(null, inst);

		assertEquals(INST_1_ID, inst.getID()); // unchanged
		assertEquals("CCC Bank", inst.getName()); // changed
		
		// Does not work after conversion V. 5.1.3 -> V. 5.2:
//		assertEquals("XXXYYY", inst.getBIC()); // changed, not upper case
//		assertEquals("ccc-bank.com", inst.getURL()); // changed
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

		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		KMyMoneyWritableInstitution inst = kmmInFile.createWritableInstitution("Wall St Fuckers");

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(inst);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMyMoneyWritableInstitution inst) throws Exception {
		assertEquals(ConstTest.Stats.NOF_INST + 1, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST + 1, kmmInFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		newID = inst.getID();
		assertEquals("Wall St Fuckers", inst.getName());
		assertNotEquals(null, inst.getAddress()); // Cf. TestKMMWritableAddressImpl
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_INST + 1, kmmOutFileStats.getNofEntriesInstitutions(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_INST + 1, kmmOutFileStats.getNofEntriesInstitutions(KMMFileStats.Type.CACHE));

		KMyMoneyInstitution inst = kmmOutFile.getInstitutionByID(newID);
		assertNotEquals(null, inst);

		assertEquals(newID, inst.getID());
		assertEquals("Wall St Fuckers", inst.getName());
		assertNotEquals(null, inst.getAddress()); // Cf. TestKMMWritableAddressImpl
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	@Test
	public void test03_2_1() throws Exception {
		KMyMoneyWritableInstitution inst = kmmInFile.createWritableInstitution("Wall St Fuckers");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableInstitutionImpl.test01_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_1_check_1_xmllint(outFile);
		test03_2_1_check_2(outFile);
	}

	// -----------------------------------------------------------------

	//  @Test
	//  public void test03_2_1_check() throws Exception
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

		NodeList nList = document.getElementsByTagName("INSTITUTION");
		assertEquals(ConstTest.Stats.NOF_INST + 1, nList.getLength());

		// Last (new) node
		Node lastNode = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());
		Element elt = (Element) lastNode;
		assertEquals("Wall St Fuckers", elt.getAttribute("name"));
		assertEquals("I000003", elt.getAttribute("id"));
	}

	// -----------------------------------------------------------------

	@Test
	public void test03_2_4() throws Exception {
		KMyMoneyWritableInstitution inst1 = kmmInFile.createWritableInstitution("Wall St Fuckers");

		KMyMoneyWritableInstitution inst2 = kmmInFile.createWritableInstitution("Robber Barons");

		KMyMoneyWritableInstitution inst3 = kmmInFile.createWritableInstitution("Money Launderers");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		//      System.err.println("Outfile for TestKMyMoneyWritableInstitutionImpl.test02_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_4_check(outFile);
	}

	private void test03_2_4_check(File outFile) throws Exception {
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

		NodeList nList = document.getElementsByTagName("INSTITUTION");
		assertEquals(ConstTest.Stats.NOF_INST + 3, nList.getLength());

		// Last three nodes (the new ones)
		Node node = nList.item(nList.getLength() - 3);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		Element elt = (Element) node;
		assertEquals("Wall St Fuckers", elt.getAttribute("name"));
		assertEquals("I000003", elt.getAttribute("id"));

		node = nList.item(nList.getLength() - 2);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("Robber Barons", elt.getAttribute("name"));
		assertEquals("I000004", elt.getAttribute("id"));

		node = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("Money Launderers", elt.getAttribute("name"));
		assertEquals("I000005", elt.getAttribute("id"));
	}

}

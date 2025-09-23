package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyTagImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableTagImpl {
	private static final KMMTagID TAG_1_ID = TestKMyMoneyTagImpl.TAG_1_ID;
	private static final KMMTagID TAG_2_ID = TestKMyMoneyTagImpl.TAG_2_ID;
	private static final KMMTagID TAG_3_ID = TestKMyMoneyTagImpl.TAG_3_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMTagID newID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableTagImpl.class);
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
	// Cf. TestKMyMoneyTagImpl.test01_1/02_1
	//
	// Check whether the KMyMoneyWritableTag objects returned by
	// KMyMoneyWritableFileImpl.getWritableTagByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getTagByID().

	@Test
	public void test01_1() throws Exception {
		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_1_ID);

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
		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_2_ID);

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
		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_3_ID);

		assertEquals(TAG_3_ID, tag.getID());
		assertEquals("kiki", tag.getName());
		assertEquals("#000000", tag.getColor());
		
		assertEquals(false, tag.hasTransactions());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableTag objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_1_ID);
		assertNotEquals(null, tag);

		assertEquals(TAG_1_ID, tag.getID());

		// ----------------------------
		// Modify the object

		tag.setName("Coucou");
		tag.setNotes("Non, pas la noix de coco");

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(tag);

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

	private void test02_1_check_memory(KMyMoneyWritableTag tag) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		assertEquals(TAG_1_ID, tag.getID()); // unchanged
		assertEquals("Coucou", tag.getName()); // changed
		assertEquals("Non, pas la noix de coco", tag.getNotes()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TAG, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyTag tag = kmmOutFile.getTagByID(TAG_1_ID);
		assertNotEquals(null, tag);

		assertEquals(TAG_1_ID, tag.getID()); // unchanged
		assertEquals("Coucou", tag.getName()); // changed
		assertEquals("Non, pas la noix de coco", tag.getNotes()); // changed
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

		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTag tag = kmmInFile.createWritableTag("Croa-croa");

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(tag);

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

	private void test03_1_1_check_memory(KMyMoneyWritableTag tag) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TAG + 1, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG + 1, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		newID = tag.getID();
		assertEquals("Croa-croa", tag.getName());
		assertEquals(false, tag.hasTransactions());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TAG + 1, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG + 1, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyTag tag = kmmOutFile.getTagByID(newID);
		assertNotEquals(null, tag);

		assertEquals(newID, tag.getID());
		assertEquals("Croa-croa", tag.getName());
		assertEquals(false, tag.hasTransactions());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	@Test
	public void test03_2_1() throws Exception {
		KMyMoneyWritableTag tag = kmmInFile.createWritableTag("Croa-croa");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableTagImpl.test01_1: '" + outFile.getPath() + "'");
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

		// Caution: The following code is a little different from the
		// code in the other test classes, because the "TAG" tag
		// also occurs in the splits.
		NodeList parentsAnchor = document.getElementsByTagName("TAGS");
		Node tagsParentNode = parentsAnchor.item(0);
		NodeList nList = tagsParentNode.getChildNodes();
		ArrayList<Node> nList2 = new ArrayList<Node>();
		for ( int i = 0; i < nList.getLength(); i++ ) {
			if ( nList.item(i).getNodeName().equals("TAG") ) {
				nList2.add(nList.item(i));
			}
		}
		assertEquals(ConstTest.Stats.NOF_TAG + 1, nList2.size());

		// Last (new) node
		Node lastNode = nList2.get(nList2.size() - 1);
		assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());
		Element elt = (Element) lastNode;
		assertEquals("Croa-croa", elt.getAttribute("name"));
		assertEquals("G000004", elt.getAttribute("id"));
	}

	// -----------------------------------------------------------------

	@Test
	public void test03_2_4() throws Exception {
		KMyMoneyWritableTag tag1 = kmmInFile.createWritableTag("Pipo");

		KMyMoneyWritableTag tag2 = kmmInFile.createWritableTag("Picou");

		KMyMoneyWritableTag tag3 = kmmInFile.createWritableTag("Pipi");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		//      System.err.println("Outfile for TestKMyMoneyWritableTagImpl.test02_1: '" + outFile.getPath() + "'");
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

		// Caution: The following code is a little different from the
		// code in the other test classes, because the "TAG" tag
		// also occurs in the splits.
		NodeList parentsAnchor = document.getElementsByTagName("TAGS");
		Node tagsParentNode = parentsAnchor.item(0);
		NodeList nList = tagsParentNode.getChildNodes();
		ArrayList<Node> nList2 = new ArrayList<Node>();
		for ( int i = 0; i < nList.getLength(); i++ ) {
			if ( nList.item(i).getNodeName().equals("TAG") ) {
				nList2.add(nList.item(i));
			}
		}
		assertEquals(ConstTest.Stats.NOF_TAG + 3, nList2.size());

		// Last three nodes (the new ones)
		Node node = nList2.get(nList2.size() - 3);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		Element elt = (Element) node;
		assertEquals("Pipo", elt.getAttribute("name"));
		assertEquals("G000004", elt.getAttribute("id"));

		node = nList2.get(nList2.size() - 2);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("Picou", elt.getAttribute("name"));
		assertEquals("G000005", elt.getAttribute("id"));

		node = nList2.get(nList2.size() - 1);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("Pipi", elt.getAttribute("name"));
		assertEquals("G000006", elt.getAttribute("id"));
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

		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_1_ID);
		assertNotEquals(null, tag);
		assertEquals(TAG_1_ID, tag.getID());

		// Check if modifiable
		assertEquals(true, tag.hasTransactions()); // there are payments

		// Variant 1
		try {
			kmmInFile.removeTag(tag); // Correctly fails because there are transactions/trx splits to it
			assertEquals(1, 0);
		} catch ( IllegalStateException exc ) {
			assertEquals(0, 0);
		}

		// Variant 2
		try {
			tag.remove(); // Correctly fails because there are transactions/trx splits to it
			assertEquals(1, 0);
		} catch ( IllegalStateException exc ) {
			assertEquals(0, 0);
		}
	}
	
	@Test
	public void test04_2_var1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_3_ID);
		assertNotEquals(null, tag);

		// Check if modifiable
		assertEquals(false, tag.hasTransactions()); // there are transactions/trx splits


		// Core (variant-specific):
		kmmInFile.removeTag(tag);

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_2_check_memory(tag);

		// ----------------------------
		// Now, check whether the deletions have been written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_2_check_persisted(outFile);
	}

	@Test
	public void test04_2_var2() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		KMyMoneyWritableTag tag = kmmInFile.getWritableTagByID(TAG_3_ID);
		assertNotEquals(null, tag);

		// Check if modifiable
		assertEquals(false, tag.hasTransactions()); // there are no transactions/trx splits

		// Core (variant-specific):
		tag.remove();

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_2_check_memory(tag);

		// ----------------------------
		// Now, check whether the deletions have been written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_2_check_persisted(outFile);
	}

	// ---------------------------------------------------------------

	private void test04_2_check_memory(KMyMoneyWritableTag tag) throws Exception {
		assertEquals(ConstTest.Stats.NOF_TAG - 1, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG - 1, kmmInFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Exception: no splits any more
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(TAG_3_ID, tag.getID());
		assertEquals("kiki", tag.getName());
		
		// However, the account cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableTag tagNow1 = kmmInFile.getWritableTagByID(TAG_3_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		// Same for a non non-writable instance. 
		// However, due to design asymmetry, no exception is thrown here,
		// but the method just returns null.
		KMyMoneyTag tagNow2 = kmmInFile.getTagByID(TAG_3_ID);
		assertEquals(null, tagNow2);
	}

	private void test04_2_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_TAG - 1, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_TAG - 1, kmmOutFileStats.getNofEntriesTags(KMMFileStats.Type.CACHE));

		// The transaction does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyTag tag = kmmOutFile.getTagByID(TAG_3_ID);
		assertEquals(null, tag); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------
	
	// ::EMPTY

}

package org.kmymoney.api.write.impl.hlp;

import java.util.List;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KVPListAlreadyContainsKeyException;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasWritableUserDefinedAttributesImpl extends HasUserDefinedAttributesImpl 
                                                  // implements HasWritableUserDefinedAttributes
{

	private static final Logger LOGGER = LoggerFactory.getLogger(HasWritableUserDefinedAttributesImpl.class);

	// ---------------------------------------------------------------

	public static void addUserDefinedAttributeCore(KEYVALUEPAIRS kvps,
												   final KMyMoneyWritableFile kmmFile,
												   final String name, final String value) {
		if ( kvps == null )
			throw new IllegalArgumentException("null slot list given");
		
		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");
		
		if ( name == null )
			throw new IllegalArgumentException("null name given");
		
		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		if ( value == null )
			throw new IllegalArgumentException("null value given");
		
		if ( value.trim().equals("") )
			throw new IllegalArgumentException("empty value given");
		
		// CAUTION: Yes, that's valid
//		if ( value.isEmpty() )
//			throw new IllegalArgumentException("empty value given");
		
		// This makes sure that the slots list is initialized
		// in case it had has been null.
		List<PAIR> dummy = kvps.getPAIR();
		
		addUserDefinedAttributeCore(kvps.getPAIR(), kmmFile, 
					                name, value);
	}
	
	public static void removeUserDefinedAttributeCore(KEYVALUEPAIRS kvps, 
													  final KMyMoneyWritableFile kmmFile,
													  final String name) {
		if ( kvps == null )
			throw new IllegalArgumentException("null slot list given");

		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");

		if ( name == null )
			throw new IllegalArgumentException("null name given");

		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		// This makes sure that the slots list is initialized
		// in case it had has been null.
		List<PAIR> dummy = kvps.getPAIR();

		removeUserDefinedAttributeCore(kvps.getPAIR(), kmmFile, name);
	}

	public static void setUserDefinedAttributeCore(KEYVALUEPAIRS kvps,
            									   final KMyMoneyWritableFile kmmFile,
            									   final String name, final String value) {
		if ( kvps == null )
			throw new IllegalArgumentException("null slot list given");
		
		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");
		
		if ( name == null )
			throw new IllegalArgumentException("null name given");
		
		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		if ( value == null )
			throw new IllegalArgumentException("null value given");

		if ( value.trim().equals("") )
			throw new IllegalArgumentException("empty value given");
		
		// This makes sure that the slots list is initialized
		// in case it had has been null.
		List<PAIR> dummy = kvps.getPAIR();
		
		setUserDefinedAttributeCore(kvps.getPAIR(), kmmFile, name, value);
	}
	
	// ---------------------------------------------------------------

	private static void addUserDefinedAttributeCore(List<PAIR> kvpList, 
													final KMyMoneyWritableFile kmmFile,
													final String name, final String value) {
		if ( kvpList == null )
			throw new IllegalArgumentException("null slot list given");

		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");

		if ( name == null )
			throw new IllegalArgumentException("null name given");

		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		if ( value == null )
			throw new IllegalArgumentException("null value given");

		if ( value.trim().equals("") )
			throw new IllegalArgumentException("empty value given");
		
// 		CAUTION: Yes, that's valid
//		if ( value.isEmpty() )
//			throw new IllegalArgumentException("empty value given");

		if ( getUserDefinedAttributeKeysCore(kvpList).contains(name) )
			throw new KVPListAlreadyContainsKeyException();

		ObjectFactory fact = new ObjectFactory();
		PAIR newKVP = fact.createPAIR();
		newKVP.setKey(name);
		newKVP.setValue(value);
		LOGGER.debug("addUserDefinedAttributeCore: (name=" + name + ", value=" + value + ") - adding new slot ");

		kvpList.add(newKVP);

		kmmFile.setModified(true);
	}

	private static void removeUserDefinedAttributeCore(List<PAIR> kvpList, 
													   final KMyMoneyWritableFile kmmFile,
													   final String name) {
		if ( kvpList == null )
			throw new IllegalArgumentException("null slot list given");

		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");

		if ( name == null )
			throw new IllegalArgumentException("null name given");

		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		if ( ! getUserDefinedAttributeKeysCore(kvpList).contains(name) )
			throw new KVPListDoesNotContainKeyException();

		for ( PAIR kvp : kvpList ) {
			if ( kvp.getKey().equals(name) ) {
				LOGGER.debug("removeUserDefinedAttributeCore: (name=" + name 
						+ "') - removing existing slot");

				kvpList.remove(kvp);
				
				kmmFile.setModified(true);
				return;
			}
		}
	}

	private static void setUserDefinedAttributeCore(List<PAIR> kvpList,
			                                       final KMyMoneyWritableFile kmmFile,
			                                       final String name, final String value) {
		if ( kvpList == null )
			throw new IllegalArgumentException("null slot list given");

		if ( kmmFile == null )
			throw new IllegalArgumentException("null KMyMoney file given");

		if ( name == null )
			throw new IllegalArgumentException("null name given");
		
		if ( name.isEmpty() )
			throw new IllegalArgumentException("empty name given");

		if ( value == null )
			throw new IllegalArgumentException("null value given");
		
		if ( value.trim().equals("") )
			throw new IllegalArgumentException("empty value given");
		
		// CAUTION: Yes, that's valid
//		if ( value.isEmpty() )
//			throw new IllegalArgumentException("empty value given");
		if ( ! getUserDefinedAttributeKeysCore(kvpList).contains(name) ) {
			throw new KVPListDoesNotContainKeyException();
		}
		
		for ( PAIR kvp : kvpList ) {
			if ( kvp.getKey().equals(name) ) {
				LOGGER.debug("setUserDefinedAttributeCore: (name=" + name + ", value='" + value
						+ "') - overwriting existing key-value-pair");

				kvp.setValue(value);
				
				kmmFile.setModified(true);
				return;
			}
		}
	}

	// ---------------------------------------------------------------

	// Remove slots with dummy content
	public static void cleanSlots(KEYVALUEPAIRS kvps) {
		if ( kvps == null )
			return;

		for ( PAIR kvp : kvps.getPAIR() ) {
			if ( kvp.getKey().equals(Const.KVP_KEY_DUMMY) ) {
				kvps.getPAIR().remove(kvp);
				break;
			}
		}
	}
}

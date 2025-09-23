package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.List;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.KEYVALUEPAIRS;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.PAIR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasUserDefinedAttributesImpl // implements HasUserDefinedAttributes
{

	private static final Logger LOGGER = LoggerFactory.getLogger(HasUserDefinedAttributesImpl.class);

	// ---------------------------------------------------------------

	public static String getUserDefinedAttributeCore(final KEYVALUEPAIRS kvps, final String name) {
		if ( kvps == null )
			return null;
		
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}

		if ( name.trim().equals("") ) {
			throw new IllegalArgumentException("empty name given");
		}

		return getUserDefinedAttributeCore(kvps.getPAIR(), name);
	}
	
	public static List<String> getUserDefinedAttributeKeysCore(final KEYVALUEPAIRS kvps) {
		if ( kvps == null )
			return null;
		
		return getUserDefinedAttributeKeysCore(kvps.getPAIR());
	}
    
	// ---------------------------------------------------------------

    public static String getUserDefinedAttributeCore(final List<PAIR> kvpList, 
    		                                         final String name) {
		if ( kvpList == null )
			return null;

		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}

//		if ( name.trim().equals("") ) {
//			throw new IllegalArgumentException("empty name given");
//		}

		if ( name.trim().equals("") ) {
			return null;
		}

		// NO:
//		if ( ! getUserDefinedAttributeKeysCore(kvpList).contains(name) ) {
//			throw new KVPListDoesNotContainKeyException();
//		}
		
		for ( PAIR kvp : kvpList ) {
			if ( kvp.getKey().equals(name) ) {
				return kvp.getValue();
			}
		}
		
		return null;
    }

    public static List<String> getUserDefinedAttributeKeysCore(final List<PAIR> kvpList) {
		if ( kvpList == null )
			return null;
		
		List<String> retval = new ArrayList<String>();

		for ( PAIR kvp : kvpList ) {
			retval.add(kvp.getKey());
		}

		return retval;
	}

    
	// ---------------------------------------------------------------

	// Return KVPs without the ones with dummy content
	public static List<PAIR> getKVPListClean(final List<PAIR> kvpList) {
		List<PAIR> retval = new ArrayList<PAIR>();

		for ( PAIR kvp : kvpList ) {
			if ( ! kvp.getKey().equals(Const.KVP_KEY_DUMMY) ) {
				retval.add(kvp);
			}
		}

		return retval;
	}

	// Remove KVPs with dummy content
	public static void cleanKVPs(final List<PAIR> kvpList) {
		for ( PAIR kvp : kvpList ) {
			if ( ! kvp.getKey().equals(Const.KVP_KEY_DUMMY) ) {
				kvpList.remove(kvp);
			}
		}
	}
	
	// ---------------------------------------------------------------
	
	// Sic, not in HasWritableUserDefinedAttributes
	//                ========
	public static void setKVPsInit(
			KEYVALUEPAIRS currKVPs,
			final KEYVALUEPAIRS newKVPs) {
		if ( currKVPs == null ) {
			throw new IllegalArgumentException("null current 'kvps' given");
		}

		if ( newKVPs == null ) {
			throw new IllegalArgumentException("null new 'kvps' given");
		}

		if ( currKVPs == newKVPs ) {
			return; // nothing has changed
		}

		if ( currKVPs.getPAIR().size() == newKVPs.getPAIR().size() ) {
			boolean areEqual = true;
			int size = currKVPs.getPAIR().size();
			for ( int i = 0; i < size; i++ ) {
				if ( ! currKVPs.getPAIR().get(i).equals(newKVPs.getPAIR().get(i)) ) {
					areEqual = false;
					LOGGER.debug("setKVPsInit: current and new KVPs objects are not equal");
				}
			}
			if ( areEqual ) {
				LOGGER.debug("setKVPsInit: current and new KVPs objects are equal");
				return;
			}
		} else {
			// have changed
			LOGGER.debug("setKVPsInit: current and new KVPs objects have different sizes");
		}

		currKVPs = newKVPs;

		// we have an xsd-problem saving empty KVPs so we add a dummy-value
		if ( newKVPs.getPAIR().isEmpty() ) {
			ObjectFactory objectFactory = new ObjectFactory();

			KEYVALUEPAIRS kvps = objectFactory.createKEYVALUEPAIRS();

			PAIR newKVP = objectFactory.createPAIR();
			newKVP.setKey(Const.KVP_KEY_DUMMY);
			newKVP.setValue(Const.KVP_KEY_DUMMY);

			newKVPs.getPAIR().add(newKVP);
		}

		// <<insert code to react further to this change here
//		PropertyChangeSupport ptyChgFirer = myPtyChg;
//		if ( ptyChgFirer != null ) {
//			ptyChgFirer.firePropertyChange("kvps", oldKVPs, newKVPs);
//		}
	}

}

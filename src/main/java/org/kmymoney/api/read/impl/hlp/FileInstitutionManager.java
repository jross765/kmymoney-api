package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.INSTITUTION;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyInstitutionImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public class FileInstitutionManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileInstitutionManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<KMMInstID, KMyMoneyInstitution> instMap;

	// ---------------------------------------------------------------

	public FileInstitutionManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		instMap = new HashMap<KMMInstID, KMyMoneyInstitution>();

		for ( INSTITUTION jwsdpInst : pRootElement.getINSTITUTIONS().getINSTITUTION() ) {
			try {
				KMyMoneyInstitutionImpl inst = createInstitution(jwsdpInst);
				instMap.put(inst.getID(), inst);
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Institution-Entry with id=" + jwsdpInst.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in institution map: " + instMap.size());
	}

	protected KMyMoneyInstitutionImpl createInstitution(final INSTITUTION jwsdpInst) {
		KMyMoneyInstitutionImpl inst = new KMyMoneyInstitutionImpl(jwsdpInst, kmmFile);
		LOGGER.debug("createInstitution: Generated new institution: " + inst.getID());
		return inst;
	}

	// ---------------------------------------------------------------

	public KMyMoneyInstitution getInstitutionByID(final KMMInstID instID) {
		if ( instID == null ) {
			throw new IllegalArgumentException("null institution ID given");
		}

		if ( ! instID.isSet() ) {
			throw new IllegalArgumentException("unset institution ID given");
		}

		if ( instMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyInstitution retval = instMap.get(instID);
		if ( retval == null ) {
			LOGGER.warn("getInstitutionByID: No Institution with ID '" + instID + "'. We know " + instMap.size() + " institutions.");
		}

		return retval;
	}

	public List<KMyMoneyInstitution> getInstitutionsByName(String expr) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		return getInstitutionsByName(expr, true);
	}

	public List<KMyMoneyInstitution> getInstitutionsByName(String expr, boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		if ( instMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		List<KMyMoneyInstitution> result = new ArrayList<KMyMoneyInstitution>();

		for ( KMyMoneyInstitution inst : getInstitutions() ) {
			if ( inst.getName() != null ) {
				if ( relaxed ) {
					if ( inst.getName().toLowerCase().contains(expr.trim().toLowerCase()) ) {
						result.add(inst);
					}
				} else {
					if ( inst.getName().equals(expr) ) {
						result.add(inst);
					}
				}
			}
		}

		return result;
	}

	public KMyMoneyInstitution getInstitutionsByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		List<KMyMoneyInstitution> cmdtyList = getInstitutionsByName(expr, false);
		if ( cmdtyList.size() == 0 )
			throw new NoEntryFoundException();
		else if ( cmdtyList.size() > 1 )
			throw new TooManyEntriesFoundException();
		else
			return cmdtyList.get(0);
	}

	public Collection<KMyMoneyInstitution> getInstitutions() {
		if ( instMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return Collections.unmodifiableCollection(instMap.values());
	}

	// ---------------------------------------------------------------

	public int getNofEntriesInstitutionMap() {
		return instMap.size();
	}

}

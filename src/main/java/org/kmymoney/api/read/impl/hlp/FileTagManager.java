package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.TAG;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyTagImpl;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public class FileTagManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileTagManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<KMMTagID, KMyMoneyTag> tagMap;

	// ---------------------------------------------------------------

	public FileTagManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		tagMap = new HashMap<KMMTagID, KMyMoneyTag>();

		for ( TAG jwsdpTag : pRootElement.getTAGS().getTAG() ) {
			try {
				KMyMoneyTagImpl tag = createTag(jwsdpTag);
				tagMap.put(tag.getID(), tag);
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Tag-Entry with id=" + jwsdpTag.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in tag map: " + tagMap.size());
	}

	protected KMyMoneyTagImpl createTag(final TAG jwsdpTag) {
		KMyMoneyTagImpl tag = new KMyMoneyTagImpl(jwsdpTag, kmmFile);
		LOGGER.debug("createTag: Generated new tag: " + tag.getID());
		return tag;
	}

	// ---------------------------------------------------------------

	public KMyMoneyTag getTagByID(final KMMTagID tagID) {
		if ( tagID == null ) {
			throw new IllegalArgumentException("null tag ID given");
		}

		if ( ! tagID.isSet() ) {
			throw new IllegalArgumentException("unset tag ID given");
		}

		if ( tagMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyTag retval = tagMap.get(tagID);
		if ( retval == null ) {
			LOGGER.warn("getTagByID: No Tag with ID '" + tagID + "'. We know " + tagMap.size() + " tags.");
		}

		return retval;
	}

	public List<KMyMoneyTag> getTagsByName(String expr) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		return getTagsByName(expr, true);
	}

	public List<KMyMoneyTag> getTagsByName(String expr, boolean relaxed) {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		if ( tagMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		List<KMyMoneyTag> result = new ArrayList<KMyMoneyTag>();

		for ( KMyMoneyTag tag : getTags() ) {
			if ( tag.getName() != null ) {
				if ( relaxed ) {
					if ( tag.getName().toLowerCase().contains(expr.trim().toLowerCase()) ) {
						result.add(tag);
					}
				} else {
					if ( tag.getName().equals(expr) ) {
						result.add(tag);
					}
				}
			}
		}

		return result;
	}

	public KMyMoneyTag getTagsByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException {
		if ( expr == null ) {
			throw new IllegalArgumentException("null expression given");
		}

		if ( expr.trim().equals("") ) {
			throw new IllegalArgumentException("empty expression given");
		}

		List<KMyMoneyTag> cmdtyList = getTagsByName(expr, false);
		if ( cmdtyList.size() == 0 )
			throw new NoEntryFoundException();
		else if ( cmdtyList.size() > 1 )
			throw new TooManyEntriesFoundException();
		else
			return cmdtyList.get(0);
	}

	public Collection<KMyMoneyTag> getTags() {
		if ( tagMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return Collections.unmodifiableCollection(tagMap.values());
	}

	// ---------------------------------------------------------------

	public int getNofEntriesTagMap() {
		return tagMap.size();
	}

}

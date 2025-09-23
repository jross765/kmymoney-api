package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.TAG;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.impl.KMyMoneyTagImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableTagImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTagManager extends org.kmymoney.api.read.impl.hlp.FileTagManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileTagManager.class);

	// ---------------------------------------------------------------

	public FileTagManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyTagImpl createTag(final TAG jwsdpTag) {
		KMyMoneyWritableTagImpl tag = new KMyMoneyWritableTagImpl(jwsdpTag, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createTag: Generated new writable tag: " + tag.getID());
		return tag;
	}

	// ---------------------------------------------------------------

	public void addTag(KMyMoneyTag tag) {
		if ( tag == null ) {
			throw new IllegalArgumentException("null tag given");
		}

		tagMap.put(tag.getID(), tag);
		LOGGER.debug("addTag: Added tag to cache: " + tag.getID());
	}

	public void removeTag(KMyMoneyTag tag) {
		if ( tag == null ) {
			throw new IllegalArgumentException("null tag given");
		}

		tagMap.remove(tag.getID());
		LOGGER.debug("removeTag: Removed tag from cache: " + tag.getID());
	}

}

package org.kmymoney.api.read.hlp;

import java.util.List;

public interface HasUserDefinedAttributes {

    /**
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    String getUserDefinedAttribute(String name);
    
    /**
     * 
     * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
     */
    List<String> getUserDefinedAttributeKeys();
    
}

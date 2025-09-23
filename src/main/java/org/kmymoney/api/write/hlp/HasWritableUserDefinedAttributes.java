package org.kmymoney.api.write.hlp;

import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;

public interface HasWritableUserDefinedAttributes extends HasUserDefinedAttributes {

    void addUserDefinedAttribute(String name, String value);
    
    void removeUserDefinedAttribute(String name);
    
    void setUserDefinedAttribute(String name, String value);
    
}

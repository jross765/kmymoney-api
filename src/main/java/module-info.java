module kmymoney.api {
	requires static org.slf4j;
	requires java.desktop;
	requires jakarta.xml.bind;
	requires java.xml;
	
	requires transitive schnorxoborx.schnorxolib;
	
	requires transitive kmymoney.base;
	
	exports org.kmymoney.api.currency;
	
	exports org.kmymoney.api.read;
	exports org.kmymoney.api.read.aux;
	// exports org.kmymoney.api.read.hlp;
	exports org.kmymoney.api.read.impl;
	exports org.kmymoney.api.read.impl.aux;
	// exports org.kmymoney.api.read.impl.hlp;
	
	exports org.kmymoney.api.write;
	exports org.kmymoney.api.write.aux;
	// exports org.kmymoney.api.write.hlp;
	exports org.kmymoney.api.write.impl;
	exports org.kmymoney.api.write.impl.aux;
	// exports org.kmymoney.api.write.impl.hlp;
}

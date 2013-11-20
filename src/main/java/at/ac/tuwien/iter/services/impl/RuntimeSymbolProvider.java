package at.ac.tuwien.iter.services.impl;

import java.util.Map;

import org.apache.tapestry5.ioc.services.SymbolProvider;

/**
 * Not sure about this one..
 * 
 * 
 * @author alessiogambi
 * 
 */
@Deprecated
public interface RuntimeSymbolProvider extends SymbolProvider {
	public void addSymbols(Map<String, String> arg0);

}

package com.googlecode.jmeter.plugins.webdriver.proxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContext;

import com.googlecode.jmeter.plugins.webdriver.config.ChromeDriverConfig;

public class DevThreadArguments extends Arguments  implements TestBean {

	private static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public Map<String, String> getArgumentsAsMap() {
		JMeterContext cont = ChromeDriverConfig.getDevContext();
		if(cont == null)
			return EMPTY_MAP;
		
		HashMap<String,String> args = new HashMap<String,String>();
		Set<Map.Entry<String, Object>> orig = cont.getVariables().entrySet();
		for(Map.Entry<String, Object> entry : orig){
			if(entry.getValue() != null && entry.getValue() instanceof String){
				args.put(entry.getKey(), (String)entry.getValue());
			}
		}
		return args;
	}
}

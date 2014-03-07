package com.googlecode.jmeter.plugins.webdriver.proxy;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.proxy.DefaultSamplerCreator;
import org.apache.jmeter.protocol.http.proxy.HttpRequestHdr;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.googlecode.jmeter.plugins.webdriver.config.ChromeDriverConfig;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

public class DevThreadSamplerCreator extends DefaultSamplerCreator {
	
    private static final Logger log = LoggingManager.getLoggerForClass();
	@Override
	public String[] getManagedContentTypes() {	
		return new String[]{null, "text/html"};
	}
	
	@Override
	protected void computeSamplerName(HTTPSamplerBase sampler,
			HttpRequestHdr request) {
		JMeterContext context = ChromeDriverConfig.getDevContext();
		if(context == null){
			super.computeSamplerName(sampler, request);
			return;
		}
		Sampler currSampler = context.getCurrentSampler();

		if(currSampler == null){
			super.computeSamplerName(sampler, request);
			return;
		}
		sampler.setName(currSampler.getName());				
	}
	
	public HTTPSamplerBase createAndPopulateSampler(HttpRequestHdr request,
			Map<String, String> pageEncodings, Map<String, String> formEncodings)
			throws Exception {
		HTTPSamplerBase sampler = super.createSampler(request, pageEncodings, formEncodings);
		super.populateSampler(sampler, request, pageEncodings, formEncodings);
		pregenerateFutureName(sampler);
		return sampler;
	}
	
    private void pregenerateFutureName(HTTPSamplerBase sampler) {
		String path = sampler.getPath();
		String method = sampler.getMethod();
    	
    	log.info("Sampler path: "  + path);
		log.info("Sampler method: "  + method);
		log.info("Sampler postBodyRaw? : "  + sampler.getPostBodyRaw());
		log.info("Sampler arguments: "  + sampler.getArguments().getArgumentsAsMap());
		
		if("/common/ajsonrpc-uime.jsp".equals(path) || 
				"/common/jsonrpc.jsp".equals(path)){
			if(sampler.getPostBodyRaw()){
				String body = sampler.getArguments().getArgument(0).getValue();
				JsonPath jsonPath = new JsonPath("$.method", new Filter[0]);
				String fullMethodName = jsonPath.<String>read(body);
				int k = fullMethodName.length() - fullMethodName.lastIndexOf(".",fullMethodName.lastIndexOf(".") - 1) - 1;				
				String javaMethod = StringUtils.right(fullMethodName, k);
				sampler.setComment("${stepNumber} AJAX-JSON: " + javaMethod);					
			}else{
				log.warn("Non-raw body case for path " +path+ " isn't supported");
			}
		}
		else if ("/platform/ui/components/referenceselector/controller.jsp".equals(path)){
			if("GET".equals(method)){			
				String pluginName = sampler.getArguments().getArgumentsAsMap().get("plugin");
				String clazzName = StringUtils.substringAfterLast(pluginName, ".");
				sampler.setComment("${stepNumber} ReferenceSelector: plugin " + clazzName);
			}
			else if("POST".equals(method)){
				String action = sampler.getArguments().getArgumentsAsMap().get("who");
				sampler.setComment("${stepNumber} ReferenceSelector: call " + action);
			}
		}else{
			String newName = "${stepNumber} " + StringUtils.substringAfter(sampler.getName(), " ");
			sampler.setComment(newName);
		}
	}

	public boolean isPreferrable() {
    	return true;
    }
	
}

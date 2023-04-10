package com.cvs.pnp.springcloudfunction.azure.generators;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.cvs.pnp.springcloudfunction.azure.responses.Response;

@Component
public class GreetingGenerator implements Function<String, Response<String>> {

	@Override
    public Response<String> apply(String name) {
    	Response<String> response = new Response<String>();
    	String defaultName = "Anonymous";
    	String respMsg = "Hello! %s";
    	if(name == null || name.isEmpty()) {
    		name = defaultName;
    	}
    	respMsg = String.format(respMsg, name);
    	
    	response.setCode("200");
    	response.setMessage("success");
    	response.setPayload(respMsg);
        return response;
    }
}

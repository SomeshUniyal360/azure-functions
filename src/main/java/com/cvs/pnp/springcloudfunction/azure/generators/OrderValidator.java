package com.cvs.pnp.springcloudfunction.azure.generators;

import java.util.function.Function;

import org.springframework.stereotype.Component;


@Component
public class OrderValidator implements Function<String, Boolean>  {
	@Override
    public Boolean apply(String order) {
		Boolean status;
		if(order!=null && !order.isEmpty()) {
			status = true;
		}else {
			status = false;
		}
		return status;
	}
}

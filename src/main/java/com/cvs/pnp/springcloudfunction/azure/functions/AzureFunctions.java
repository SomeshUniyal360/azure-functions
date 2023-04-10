package com.cvs.pnp.springcloudfunction.azure.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cvs.pnp.springcloudfunction.azure.generators.GreetingGenerator;
import com.cvs.pnp.springcloudfunction.azure.generators.OrderValidator;
import com.cvs.pnp.springcloudfunction.azure.responses.Response;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusTopicOutput;

@Component
public class AzureFunctions {

	@Autowired
	private GreetingGenerator greetingGenerator;

	@Autowired
	private OrderValidator orderValidator;

	@FunctionName("greeting")
	public HttpResponseMessage execute(
			@HttpTrigger(name = "request", methods = {
					HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
			ExecutionContext context) {
		String name = request.getQueryParameters().get("name");
		context.getLogger().info("Greeting user name: " + name != null ? name : "null");
		return request.createResponseBuilder(HttpStatus.OK).body(greetingGenerator.apply(name))
				.header("Content-Type", "application/json").build();
	}

	@FunctionName("order")
	public HttpResponseMessage serviceBusTopicOutput(@HttpTrigger(name = "request", methods = {
			HttpMethod.POST }, route = "submit/order", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
			@ServiceBusTopicOutput(name = "message", topicName = "order", subscriptionName = "delhiStore", connection = "AzureWebJobsServiceBus") OutputBinding<String> output,
			final ExecutionContext context) {
		String order = request.getBody();
		boolean status = orderValidator.apply(order);
		Response<String> responce = new Response<String>();
		if (status) {
			responce.setCode("200");
			responce.setMessage("success");
			output.setValue(order);
		} else {
			responce.setCode("400");
			responce.setMessage("failure");
		}
		return request.createResponseBuilder(HttpStatus.OK).body(responce).header("Content-Type", "application/json")
				.build();

	}
}

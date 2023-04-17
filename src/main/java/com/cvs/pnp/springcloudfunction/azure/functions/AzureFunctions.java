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
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusTopicOutput;
import com.microsoft.azure.functions.annotation.ServiceBusTopicTrigger;


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

	@FunctionName("postOrderStatusToServiceBus")
	public HttpResponseMessage postOrderStatusToServiceBus(@HttpTrigger(name = "postOrderStatus", methods = {
			HttpMethod.POST }, route = "submit/order/status", authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
			@ServiceBusTopicOutput(name = "orderStatusUpdate", topicName = "pnporderstatus", subscriptionName = "OrderStatusUpdates", connection = "AzureWebJobsServiceBus") OutputBinding<String> output,
			final ExecutionContext context) {
		context.getLogger().info("running postOrderStatusToServiceBus azure function");
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

	@FunctionName("saveOrderStatusToCosmosDb")
	public void orderStatusUpdate(
			@ServiceBusTopicTrigger(name = "orderStatusUpdate", topicName = "pnporderstatus", subscriptionName = "OrderStatusUpdates", connection = "AzureWebJobsServiceBus") String message,
			@CosmosDBOutput(name = "orderStatusInsert", databaseName = "pnpCentralDb", containerName = "pnpCentralOrderUpdates", connection = "AzureWebJobsCosmosDBConnectionString") OutputBinding<String> output,
			final ExecutionContext context) {
		context.getLogger().info("running saveOrderStatusToCosmosDb azure function");
		output.setValue(message);
		context.getLogger().info("saveOrderStatusToCosmosDb processed a message: " + message);
	}
}

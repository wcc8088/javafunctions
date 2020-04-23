package com.woocc.azure.function;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AzureVerifyFunction {
    /**
     * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/hello
     * 2. curl {your host}/api/hello?name=HTTP%20Query
     */
    @FunctionName("AzureVerify")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");


        // Parse query parameter
        String faceId1 = request.getQueryParameters().get("faceId1");
        String faceId2 = request.getQueryParameters().get("faceId2");
        
        context.getLogger().info(faceId1 + "|"+ faceId2);
        String jsonString = null;
 
        StringBuffer html = new StringBuffer();

        File sourceFile = null;

        final String subscriptionKey = "35f6122e6c324d569c6273e2c2952b50";
        final String uriBase = "https://eastasia.api.cognitive.microsoft.com/face/v1.0/verify";
        final String storageConnectionString ="DefaultEndpointsProtocol=http;" + "AccountName=wooccstorage;" + "AccountKey=qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==";

        HttpClient httpclient = new DefaultHttpClient();

        try
        {
            URIBuilder builder = new URIBuilder(uriBase);

            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost postRequest = new HttpPost(uri);

            // Request headers.
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body.
            String ent = "{\"faceId1\":\"" + faceId1 + "\", \n\"faceId2\":\"" + faceId2 + "\"}";
            context.getLogger().info("ent="+ent);
            StringEntity reqEntity = new StringEntity(ent);
            postRequest.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse postResponse = httpclient.execute(postRequest);
            HttpEntity entity = postResponse.getEntity();

            if (entity != null)
            {
                // Format and display the JSON response.
                context.getLogger().info("REST Response:\n");

                jsonString = EntityUtils.toString(entity).trim();
                if (jsonString.charAt(0) == '[') {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    context.getLogger().info(jsonArray.toString(2));
                    html.append(jsonArray.toString(2));
                }
                else if (jsonString.charAt(0) == '{') {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    context.getLogger().info(jsonObject.toString(2));
                    html.append(jsonObject.toString(2));
                } else {
                    context.getLogger().info(jsonString);
                    html.append(jsonString);
                }
            }
        }
        catch (Exception e)
        {
            // Display error message.
            context.getLogger().info(e.getMessage());
        }

        if (faceId1 == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(html.toString()).build();
        }
    }
}

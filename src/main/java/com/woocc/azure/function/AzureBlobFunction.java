package com.woocc.azure.function;

import java.util.Optional;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BlobOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.StorageAccount;

public class AzureBlobFunction {
    /**
     * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/hello
     * 2. curl {your host}/api/hello?name=HTTP%20Query
     */
    @FunctionName("putTextToBlobHttp")
    @StorageAccount("DefaultEndpointsProtocol=http;AccountName=wooccstorage;AccountKey=qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==;EndpointSuffix=blob.core.windows.net")
    @BlobOutput(name = "$return", path = "www/{name}")
    public String readName(
    @HttpTrigger(name = "req", 
          methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
          final HttpRequestMessage<Optional<String>> request) {
       String name = request.getBody().orElseGet(() -> request.getQueryParameters().get("name"));
       return name == null ?
              "Please pass a name on the query string or in the request body" :
              "Hello " + name;
    }
    
    /*
    @FunctionName("getBlobSizeHttp")
    @StorageAccount("DefaultEndpointsProtocol=http;AccountName=wooccstorage;AccountKey=qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==;EndpointSuffix=blob.core.windows.net")
    public HttpResponseMessage blobSize(
        @HttpTrigger(name = "req", 
        methods = {HttpMethod.GET}, 
        authLevel = AuthorizationLevel.ANONYMOUS) 
        HttpRequestMessage<Optional<String>> request,
        @BlobInput(
        name = "file", 
        dataType = "binary", 
        path = "www/{Query.file}") 
        byte[] content,
        final ExecutionContext context) {
        // build HTTP response with size of requested blob
        return request.createResponseBuilder(HttpStatus.OK)
            .body("The size of \"" + request.getQueryParameters().get("file") + "\" is: " + content.length + " bytes")
            .build();
    }
    */

}

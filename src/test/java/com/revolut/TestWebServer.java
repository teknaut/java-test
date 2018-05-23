package com.revolut;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolut.http.WebServer;
import com.revolut.model.Account;
import com.revolut.model.Responses.Accounts;
import com.revolut.model.Responses.BasicResponse;
import com.revolut.services.AccountService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;


public class TestWebServer {

    private static WebServer webServer;

    private Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @BeforeClass
    public static void setUp()  {

        try {
            webServer = new WebServer(8080);
            long start = System.currentTimeMillis();
            Thread.sleep(100L);
            while (!webServer.wasStarted()) {
                Thread.sleep(100L);
                if (System.currentTimeMillis() - start > 2000) {
                    Assert.fail("could not start server");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
        webServer.stop();
    }

    @Test(priority=1)
    public void testNormalRequest()  {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httphead = new HttpGet("http://localhost:8080/");
            CloseableHttpResponse response = client.execute(httphead);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=2)
    public void testBadRequest() {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httphead = new HttpGet("http://localhost:8080/wrong");
            CloseableHttpResponse response = client.execute(httphead);
            Assert.assertEquals(404, response.getStatusLine().getStatusCode());
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=3)
    public void testCreateAccount() {
        String url = "http://localhost:8080/create";

        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            StringEntity requestEntity = new StringEntity(
                    "{\"account_id\":\"123456789\", \"balance\": \"1000\"}",
                    ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);
            CloseableHttpResponse response = client.execute(post);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=4)
    public void testListAccounts() {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httphead = new HttpGet("http://localhost:8080/list");
            CloseableHttpResponse response = client.execute(httphead);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            Accounts accounts = gson.fromJson(json, Accounts.class);
            Assert.assertTrue(accounts.getAccounts().size() == 1);
            Assert.assertTrue(accounts.getAccounts().get(0) == 123456789l);
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=5)
    public void testGetBalance()  {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httphead = new HttpGet("http://localhost:8080/balance/123456789");
            CloseableHttpResponse response = client.execute(httphead);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            BasicResponse result = gson.fromJson(json, BasicResponse.class);
            Assert.assertTrue(Long.parseLong(result.getResult()) == 1000l);
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=6)
    public void testDoTransfer(){

        try(CloseableHttpClient client = HttpClients.createDefault()) {

            //add a second account
            String url = "http://localhost:8080/create";
            HttpPost post = new HttpPost(url);
            StringEntity postEntity = new StringEntity(
                    "{\"account_id\":\"987654321\", \"balance\": \"1000\"}",
                    ContentType.APPLICATION_JSON);
            post.setEntity(postEntity);
            CloseableHttpResponse response = client.execute(post);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());

            url = "http://localhost:8080/transfer";
            HttpPost transfer = new HttpPost(url);
            StringEntity putEntity = new StringEntity(
                    "{\"account_from\":\"123456789\", \"account_to\": \"987654321\", \"balance_to_transfer\" : \"500\"}",
                    ContentType.APPLICATION_JSON);
            transfer.setEntity(putEntity);
            CloseableHttpResponse transferResult = client.execute(transfer);
            HttpEntity entity = transferResult.getEntity();
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            BasicResponse result = gson.fromJson(json, BasicResponse.class);
            Assert.assertTrue(result.getResult().contains("success"));

            HttpGet httphead = new HttpGet("http://localhost:8080/balance/123456789");
            CloseableHttpResponse balanceResponse1 = client.execute(httphead);
            Assert.assertEquals(balanceResponse1.getStatusLine().getStatusCode(), 200);
            entity = balanceResponse1.getEntity();
            json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            result = gson.fromJson(json, BasicResponse.class);
            Assert.assertTrue(Long.parseLong(result.getResult()) == 500l);

            httphead = new HttpGet("http://localhost:8080/balance/987654321");
            CloseableHttpResponse balanceResponse2 = client.execute(httphead);
            Assert.assertEquals(balanceResponse1.getStatusLine().getStatusCode(), 200);
            entity = balanceResponse2.getEntity();
            json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            result = gson.fromJson(json, BasicResponse.class);
            Assert.assertTrue(Long.parseLong(result.getResult()) == 1500l);
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test(priority=7)
    public void testDoTransferBalanceExceeded(){

        try(CloseableHttpClient client = HttpClients.createDefault()) {

            //add a second account
            String url = "http://localhost:8080/create";
            HttpPost post = new HttpPost(url);
            StringEntity postEntity = new StringEntity(
                    "{\"account_id\":\"98765432\", \"balance\": \"1000\"}",
                    ContentType.APPLICATION_JSON);
            post.setEntity(postEntity);
            CloseableHttpResponse response = client.execute(post);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());

            url = "http://localhost:8080/transfer";
            HttpPost transfer = new HttpPost(url);
            StringEntity putEntity = new StringEntity(
                    "{\"account_from\":\"123456789\", \"account_to\": \"98765432\", \"balance_to_transfer\" : \"5500\"}",
                    ContentType.APPLICATION_JSON);
            transfer.setEntity(putEntity);
            CloseableHttpResponse transferResult = client.execute(transfer);
            Assert.assertEquals(transferResult.getStatusLine().getStatusCode(), 400);
            HttpEntity entity = transferResult.getEntity();
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            BasicResponse result = gson.fromJson(json, BasicResponse.class);
            Assert.assertTrue(result.getResult().contains("insufficient funds"));
            waitForResponse();
        }
        catch(Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testValidateTransfer(){
        AccountService accountService = new AccountService();
        Account account = new Account();
        account.setBalance(10000l);
        account.setAccountId(987654321l);
        Assert.assertFalse(accountService.validateTransfer(account, -1l));
        Assert.assertTrue(accountService.validateTransfer(account, 1l));
        account.setBalance(0l);
        Assert.assertFalse(accountService.validateTransfer(account, 1l));

    }

    /**
     *wait for requests and responses to complete
     */
    private void waitForResponse(){
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

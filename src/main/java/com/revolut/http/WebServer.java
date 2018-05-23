package com.revolut.http;

import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolut.model.Responses.Accounts;
import com.revolut.model.Responses.BasicResponse;
import com.revolut.services.AccountService;
import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebServer extends NanoHTTPD {

    private final AccountService accountService = new AccountService();

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public WebServer(int port) {
        super(port);
        try {
            /**
             * we can uncomment this line to run service with ssl
             * will require confirm browser security exception
             */
            //makeSecure(NanoHTTPD.makeSSLSocketFactory("/keystore.jks", "password".toCharArray()), null);
            setAsyncRunner(new RequestHandler(Executors.newCachedThreadPool()));
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            log.info("Server Running! point your browser to http://127.0.0.1:" + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        log.info("request: " + session.getUri());
        Response response = null;
        try {
            Map<String, String> files = new HashMap<>();
            Method method = session.getMethod();

            if (session.getUri().startsWith("/balance")) {
                if (!Method.GET.equals(method))
                    return handleBadHttpMethod(Method.GET);
                String[] res = session.getUri().split("/");
                Long accountId = 0l;
                try {
                    accountId = Long.parseLong(res[2]);
                } catch (Exception ex) {
                    log.error("invalid path : " + session.getUri());
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST,
                            NanoHTTPD.MIME_PLAINTEXT, "Error 400, invalid path.");
                }
                return setResponse(accountService.getBalance(accountId));
            }

            switch (session.getUri()) {
            case "/transfer":
                //i wanted to use PUT here but found a limitation of NanoHttpd in that request body is not handled
                // correctly
                if (!Method.POST.equals(method))
                    return handleBadHttpMethod(Method.POST);
                session.parseBody(files);
                response = setResponse(accountService.doTransfer(files));
                break;
            case "/create":
                if (!Method.POST.equals(method))
                    return handleBadHttpMethod(Method.POST);
                session.parseBody(files);
                response = setResponse(accountService.createAccount(files));
                break;
            case "/list":
                if (!Method.GET.equals(method))
                    return handleBadHttpMethod(Method.GET);
                Accounts allAccounts = new Accounts(accountService.listAccounts());
                response = newFixedLengthResponse(gson.toJson(allAccounts, Accounts.class));
                break;
            case "/":
                if (!Method.GET.equals(method))
                    return handleBadHttpMethod(Method.GET);
                response = newFixedLengthResponse("OK");
                break;
            default:
                response = newFixedLengthResponse(Response.Status.NOT_FOUND,
                        NanoHTTPD.MIME_PLAINTEXT, "Error 404, path not found.");
            }
        } catch (Exception e) {
            log.error(null, e);
        }
        return response;
    }

    private Response handleBadHttpMethod(Method method) {
        return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
                NanoHTTPD.MIME_PLAINTEXT, "Error 405 bad method not allowed, requires " + method.toString());
    }

    private Response setResponse(BasicResponse response) {
        Response.Status status = (response.getResponseCode() == 200 || response.getResponseCode() == 0) ? OK : BAD_REQUEST;
        return newFixedLengthResponse(status,
                NanoHTTPD.MIME_PLAINTEXT, gson.toJson(response, BasicResponse.class));
    }
}

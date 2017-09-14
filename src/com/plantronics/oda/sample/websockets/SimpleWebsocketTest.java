package com.plantronics.oda.sample.websockets;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A simple test case to demo to connect to PLT ODA Websockets.
 * Created by mramakrishnan on 8/18/17.
 */
@Ignore
public class SimpleWebsocketTest {
    private MyWsHandler myWsHandler = new MyWsHandler();
    private static final int PING_TIME_INTERVAL = (int) TimeUnit.SECONDS.toSeconds(10);
    private static final String APP_ID= "appId";
    private static final String TENANT_API_CODE= "tenantApiCode";


    public static void main(String[] args) {
        try {
            String appId=args[0];
            String tenantId=args[1];
            String webSocketAdd=args[2];
            if(args[0] == null){
                System.out.println("Bad input arg 0! Please enter appId.");
            }
            if(args[1] == null){
                System.out.println("Bad input arg 1! Please enter tenantId.");
            }
            if(args[2] == null){
                System.out.println("Bad input arg 2! Please enter webSocketAdd.");
            }
            System.out.println("Creating websocket client connection in ws URI :..." +webSocketAdd);
            SimpleWebsocketTest simpleWebsocketTest = new SimpleWebsocketTest();
            Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
            List<Transport> transports = Collections.singletonList(webSocketTransport);
            //These headers are mandatory for authenticating on the ODA ws api
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            //localhost
//            headers.set(APP_ID,
//                    "780e186e-cd2e-440b-9618-9cb780ee67f1");
//            //Add tenant api code
//            headers.set(TENANT_API_CODE,
//                    "4df221a5-7956-4e35-9bce-1c257fdde785");
            System.out.println("Setting headers : appId "+ appId + "tenantApiCode "+tenantId);
             headers.set(APP_ID,
                    appId);
            //Add tenant api code
            headers.set(TENANT_API_CODE,
                    tenantId);

            //Add appId
//            headers.set(APP_ID,
//                    "d0f76331-c55e-442a-9f3b-aa86cf157fde");
//            //Add tenant api code
//            headers.set(TENANT_API_CODE,
//                    "b14710bf-d192-466e-aeef-b67f56c9ea9b");

//            headers.set(APP_ID,
//                    "97f17880-507c-4832-b686-1248d53cc9d7");
//            //Add tenant api code
//            headers.set(TENANT_API_CODE,
//                    "6082fda6-fd3f-456c-8acc-56bd36e6fb60");
            WebSocketClient standardWebSocketClient = new StandardWebSocketClient();
//            ListenableFuture<WebSocketSession> sessionListenableFuture = sockJsClient.doHandshake(simpleWebsocketTest.myWsHandler, headers,
//                    URI.create("ws://localhost:8060/start"));
            ListenableFuture<WebSocketSession> sessionListenableFuture = standardWebSocketClient.doHandshake(simpleWebsocketTest.myWsHandler, headers,
                    URI.create(webSocketAdd));
//            ListenableFuture<WebSocketSession> sessionListenableFuture = standardWebSocketClient.doHandshake(simpleWebsocketTest.myWsHandler, headers,
//                    URI.create("wss://test-partner-api.preview.pltzone.org/start"));
//            ListenableFuture<WebSocketSession> sessionListenableFuture = standardWebSocketClient.doHandshake(simpleWebsocketTest.myWsHandler, headers,
//                    URI.create("wss://partner-api.nectar.pltzone.org/start"));
            WebSocketSession session = sessionListenableFuture.get();
            Assert.assertNotNull(session);
            Thread.sleep(600000);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * A websocket session handler.
     */
    public class MyWsHandler implements WebSocketHandler {
        private Logger logger = Logger.getLogger(String.valueOf(MyWsHandler.class));
        private ScheduledExecutorService executorService=Executors.newScheduledThreadPool(1);

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.info("Connection established!");
            //start request for call end notifications
            TextMessage pingMessage=createPingMessage();
            logger.info("Sending ping message...");
            session.sendMessage(pingMessage);
            PingTask pingTask=new PingTask(session);
            logger.info("Starting periodic ping...");
            executorService.scheduleAtFixedRate(pingTask, 0,PING_TIME_INTERVAL , TimeUnit.SECONDS);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            logger.info("Message received!! "+ message);
            org.json.JSONObject jsonObject=new JSONObject(message.getPayload().toString());
            logger.info(message.getPayload().toString());
            String payload=jsonObject.getString("message");
            String metadata=jsonObject.getString("metadata");
            logger.info("payload: "  + payload);
            logger.info("metadata: " + metadata);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }

    private TextMessage createPingMessage() {
        JSONObject pingMessage=new JSONObject();
        try {
            pingMessage.put("messageType",MessageType.PING_MESSAGE.getValue());
        } catch (JSONException e) {
        }
        return new TextMessage(pingMessage.toString());
    }

    /**
     * A runnable to push a ping message from client to keep session alive with server.
     */
    private class PingTask implements Runnable{
        private WebSocketSession session;
        private Logger logger = Logger.getLogger(String.valueOf(PingTask.class));

        public PingTask(WebSocketSession session){
            this.session=session;
        }
        @Override
        public void run() {
            try {
                TextMessage pingMessage = createPingMessage();
                logger.info("Sending ping message...");
                session.sendMessage(pingMessage);
            }
            catch (Exception e){
                logger.info("Exception!! " +e);
            }
        }

        private TextMessage createPingMessage() {
            JSONObject pingMessage=new JSONObject();
            try {
                pingMessage.put("messageType",MessageType.PING_MESSAGE.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new TextMessage(pingMessage.toString());
        }
    }
}


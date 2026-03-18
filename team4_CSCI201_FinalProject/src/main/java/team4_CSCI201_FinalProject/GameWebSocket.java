package team4_CSCI201_FinalProject;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ServerEndpoint("/game")
public class GameWebSocket {
    private static final GameRoomManager roomManager = GameRoomManager.getInstance();
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connection established: " + session.getId());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String action = json.get("action").getAsString();
            JsonObject data = json.getAsJsonObject("data");

            switch (action) {
                case "join_room":
                    handleJoinRoom(data, session);
                    break;
                case "chat":
                    handleChatMessage(data, session);
                    break;
                case "draw":
                    handleDrawing(data, session);
                    break;
                default:
                    sendError(session, "Unknown action: " + action);
            }
        } catch (Exception e) {
            sendError(session, "Invalid message format");
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(JsonObject data, Session session) {
        String roomId = data.get("roomId").getAsString();
        String username = data.get("username").getAsString();

        GameRoom room = roomManager.getOrCreateRoom(roomId);
        Player player = new Player(username, session);
        room.addPlayer(player);
        sendMessage(session, "Joined room " + roomId);
    }

    private void handleChatMessage(JsonObject data, Session session) {
        String message = data.get("message").getAsString();
        String username = data.get("username").getAsString();

        GameRoom room = roomManager.getRoomBySession(session);
        if (room != null) {
            room.broadcastMessage(username + ": " + message);
        } else {
            sendError(session, "You are not in a room.");
        }
    }

    private void handleDrawing(JsonObject data, Session session) {
        GameRoom room = roomManager.getRoomBySession(session);
        if (room != null) {
            room.broadcastDrawing(data.toString());
        } else {
            sendError(session, "You are not in a room.");
        }
    }

    private void sendMessage(Session session, String message) {
        try {
        	JsonObject json = new JsonObject();
            json.addProperty("action", "message");
            JsonObject dataJsonObject = new JsonObject();
			dataJsonObject.addProperty("message", message);
			json.add("data", dataJsonObject);
	        System.out.println(message);
	        session.getBasicRemote().sendText(json.toString());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void sendError(Session session, String error) {
    	try {
    		JsonObject json = new JsonObject();
            json.addProperty("action", "error");
            JsonObject dataJsonObject = new JsonObject();
			dataJsonObject.addProperty("error", error);
			json.add("data", dataJsonObject);
	        session.getBasicRemote().sendText(json.toString());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
}




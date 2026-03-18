package team4_CSCI201_FinalProject;

import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.Session;

public class GameRoomManager {
    private static GameRoomManager instance;
    private ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    private GameRoomManager() {}

    public static synchronized GameRoomManager getInstance() {
        if (instance == null) {
            instance = new GameRoomManager();
        }
        return instance;
    }

    public GameRoom getOrCreateRoom(String roomId) {
        return rooms.computeIfAbsent(roomId, GameRoom::new);
    }

    public GameRoom getRoomBySession(Session session) {
        return rooms.values().stream()
                .filter(room -> room.getPlayers().stream().anyMatch(player -> player.getSession().equals(session)))
                .findFirst()
                .orElse(null);
    }
}



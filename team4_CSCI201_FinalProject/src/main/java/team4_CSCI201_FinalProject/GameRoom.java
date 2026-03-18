package team4_CSCI201_FinalProject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

public class GameRoom implements Runnable {
	private String roomId;
	private List<Player> players = new CopyOnWriteArrayList<>();
	private boolean isGameStarted = false;
	private int currentDrawerIndex = -1; // Tracks the index of the current drawer
	
	private WordBank bank;
    private String currentWord = "";

	public GameRoom(String roomId) {
		this.roomId = roomId;
		this.bank = new WordBank();
	}

	public synchronized void addPlayer(Player player) {
		System.out.println("Attempting to add new player: " + player.getUsername());
		
		if (isGameStarted) {
			sendToPlayer(player, "Game has already started. You cannot join.");
			return;
		}

		if (players.stream().anyMatch(p -> p.getUsername().equals(player.getUsername()))) {
			sendToPlayer(player, "Username already exists in this room.");
			return;
		}

		players.add(player);
		broadcastMessage(player.getUsername() + " has joined the room.");

		if (players.size() == 4) {
			startGame();
		}
	}

	public List<Player> getPlayers() {
		return players;
	}

	private void startGame() {
		isGameStarted = true;
		new Thread(this).start(); // Start the game room thread
		broadcastMessage("Game is starting!");
		
		try
		{
			bank.resetUsedWords();
			currentWord = bank.getNewWord();
			System.out.println("New Word" + currentWord);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void assignDrawer() {
        if (players.isEmpty()) {
            return;
        }

        // Clear the previous drawer flag
        for (Player p : players) {
            p.setDrawer(false);
        }

        if (currentDrawerIndex == -1) {
            // First round: pick a random drawer
            Random rand = new Random();
            currentDrawerIndex = rand.nextInt(players.size());
        } else {
            // Subsequent rounds: rotate to the next player
            currentDrawerIndex = (currentDrawerIndex + 1) % players.size();
        }

        Player drawer = players.get(currentDrawerIndex);
        drawer.setDrawer(true);
        broadcastMessage(drawer.getUsername() + " is the drawer!");
        broadcastDrawerSelection(drawer);
    }

    // New method to inform all clients which player is the drawer
    private void broadcastDrawerSelection(Player drawer) {
        for (Player player : players) {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("action", "drawer_chosen");
                json.addProperty("username", drawer.getUsername());
                player.getSession().getBasicRemote().sendText(json.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public void broadcastMessage(String message) {
		for (Player player : players) {
			try {
				JsonObject json = new JsonObject();
				json.addProperty("action", "chat");
				JsonObject dataJsonObject = new JsonObject();
				dataJsonObject.addProperty("message", message);
				json.add("data", dataJsonObject);
				System.out.println("Sending message: " + json.toString());
				player.getSession().getBasicRemote().sendText(json.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void broadcastDrawing(String drawingData) {
		for (Player player : players) {
			try {
				JsonObject json = new JsonObject();
				json.addProperty("action", "chat");
				JsonObject dataJsonObject = new JsonObject();
				dataJsonObject.addProperty("drawingData", drawingData);
				json.add("data", dataJsonObject);
				System.out.println("Sending message: " + json.toString());
				player.getSession().getBasicRemote().sendText(json.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendToPlayer(Player player, String message) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("action", "message");
            JsonObject dataJsonObject = new JsonObject();
			dataJsonObject.addProperty("message", message);
			json.add("data", dataJsonObject);
            player.getSession().getBasicRemote().sendText(json.toString());
        } catch (IOException e) {
            System.err.println("Error sending message to " + player.getUsername() + ": " + e.getMessage());
        }
    }


	@Override
	public void run() {
		while (!isGameStarted) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("GameRoom thread interrupted: " + e.getMessage());
			}
			// TODO: Add game loop AND termination conditions - round starts scors, any checker
			assignDrawer();
		}

	}
}



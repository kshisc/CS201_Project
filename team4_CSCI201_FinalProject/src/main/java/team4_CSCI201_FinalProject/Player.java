

package team4_CSCI201_FinalProject;

import jakarta.websocket.Session;

public class Player {
    private String username;
    private Session session;
    private boolean isDrawer;

    public Player(String username, Session session) {
        this.username = username;
        this.session = session;
        this.isDrawer = false;
    }

    public String getUsername() {
        return username;
    }

    public Session getSession() {
        return session;
    }

    public boolean isDrawer() {
        return isDrawer;
    }

    public void setDrawer(boolean isDrawer) {
        this.isDrawer = isDrawer;
    }
}



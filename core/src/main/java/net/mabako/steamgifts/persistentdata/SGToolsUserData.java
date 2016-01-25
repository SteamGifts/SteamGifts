package net.mabako.steamgifts.persistentdata;

public class SGToolsUserData {
    private static SGToolsUserData current = new SGToolsUserData();

    private String sessionId;

    public static SGToolsUserData getCurrent() {
        return current;
    }

    public static void clear() {
        current = new SGToolsUserData();
    }

    public boolean isLoggedIn() {
        return sessionId != null && !sessionId.isEmpty();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

package net.mabako.steamgifts.data;

import java.io.Serializable;

public class User implements Serializable {
    private final String name;
    private boolean loaded = false;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return loaded;
    }
}

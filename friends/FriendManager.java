package me.Gui.gui.friends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;

public class FriendManager {
    private final Set<String> friends = new HashSet<String>();
    private final Map<String, String> displayNames = new HashMap<String, String>();

    public boolean add(String name) {
        String n = FriendManager.normalize(name);
        if (n.isEmpty()) {
            return false;
        }
        if (FriendManager.isSelf(n)) {
            return false;
        }
        return this.friends.add(n);
    }

    public boolean remove(String name) {
        String n = FriendManager.normalize(name);
        if (n.isEmpty()) {
            return false;
        }
        if (FriendManager.isSelf(n)) {
            return false;
        }
        this.displayNames.remove(n);
        return this.friends.remove(n);
    }

    public boolean isFriend(String name) {
        String n = FriendManager.normalize(name);
        if (n.isEmpty()) {
            return false;
        }
        if (FriendManager.isSelf(n)) {
            return true;
        }
        return this.friends.contains(n);
    }

    public String getDisplayName(String name) {
        String n = FriendManager.normalize(name);
        if (n.isEmpty()) {
            return "";
        }
        if (!this.friends.contains(n) && !FriendManager.isSelf(n)) {
            return "";
        }
        String value = this.displayNames.get(n);
        return value == null ? "" : value;
    }

    public void setDisplayName(String name, String display) {
        String n = FriendManager.normalize(name);
        if (n.isEmpty()) {
            return;
        }
        if (display == null || display.trim().isEmpty()) {
            this.displayNames.remove(n);
            return;
        }
        this.displayNames.put(n, display.trim());
    }

    public List<String> list() {
        String self = FriendManager.resolveSelfName();
        ArrayList<String> out = new ArrayList<String>();
        for (String f : this.friends) {
            if (f == null || f.isEmpty() || !self.isEmpty() && f.equals(self)) continue;
            out.add(f);
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    public FriendState exportState() {
        FriendState s = new FriendState();
        s.friends = this.list();
        s.displayNames = new HashMap<String, String>();
        for (String f : s.friends) {
            String v = this.displayNames.get(f);
            if (v == null || v.isBlank()) continue;
            s.displayNames.put(f, v);
        }
        return s;
    }

    public void importState(FriendState state) {
        this.friends.clear();
        this.displayNames.clear();
        if (state == null || state.friends == null) {
            return;
        }
        String self = FriendManager.resolveSelfName();
        for (String string : state.friends) {
            String n = FriendManager.normalize(string);
            if (n.isEmpty() || !self.isEmpty() && n.equals(self)) continue;
            this.friends.add(n);
        }
        if (state.displayNames != null) {
            for (Map.Entry entry : state.displayNames.entrySet()) {
                String value;
                String key = FriendManager.normalize((String)entry.getKey());
                if (key.isEmpty() || !self.isEmpty() && key.equals(self) || (value = (String)entry.getValue()) == null || value.isBlank() || !this.friends.contains(key)) continue;
                this.displayNames.put(key, value.trim());
            }
        }
    }

    private static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase();
    }

    private static boolean isSelf(String normalizedName) {
        if (normalizedName == null || normalizedName.isEmpty()) {
            return false;
        }
        String self = FriendManager.resolveSelfName();
        return !self.isEmpty() && self.equals(normalizedName);
    }

    private static String resolveSelfName() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                if (client.player != null) {
                    return FriendManager.normalize(client.player.getName().getString());
                }
                if (client.getSession() != null) {
                    return FriendManager.normalize(client.getSession().getUsername());
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "";
    }

    public static class FriendState {
        public List<String> friends = new ArrayList<String>();
        public Map<String, String> displayNames = new HashMap<String, String>();
    }
}


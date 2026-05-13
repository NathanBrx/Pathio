package fds.hai811i.pathio.model;

public class User {
    private int id;
    private String email;
    private String username;
    private String avatarUrl;

    public User(int id, String username, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // Getters
    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
}
package fds.hai811i.pathio.model.responses;

import fds.hai811i.pathio.model.User;

public class ProfileResponse {
    private String message;
    private User user;

    // Getters
    public String getMessage() {
        return message;
    }
    public User getUser() {
        return user;
    }
}
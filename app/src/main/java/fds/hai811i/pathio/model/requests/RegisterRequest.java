package fds.hai811i.pathio.model.requests;

public class RegisterRequest {
    String email, username, password;

    public RegisterRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
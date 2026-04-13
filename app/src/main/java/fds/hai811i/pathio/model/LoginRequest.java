package fds.hai811i.pathio.model;

public class LoginRequest {
    String identifier, password;

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }
}

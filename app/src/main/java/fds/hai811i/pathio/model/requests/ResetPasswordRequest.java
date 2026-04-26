package fds.hai811i.pathio.model.requests;

public class ResetPasswordRequest {
    String resetToken;
    String newPassword;
    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }
}

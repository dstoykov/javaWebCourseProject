package dst.courseproject.models.binding;

public class UserLoginBindingModel {
    private String email;

    private String password;

    public UserLoginBindingModel() {
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

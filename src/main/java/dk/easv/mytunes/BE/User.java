package dk.easv.mytunes.BE;

public class User {

    private int id = -1;
    private String username = "";
    private String password_hash = "";

    public User (int id, String username, String password_hash){
        setId(id);
        setUsername(username);
        setPassword_hash(password_hash);
    }

    private int getId() {
        return id;
    }

    private void setId(int id) {
        if (id != -1)
            this.id = id;
    }

    private String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        if (username != null && !username.isBlank())
            this.username = username;
    }

    private String getPassword_hash() {
        return password_hash;
    }

    private void setPassword_hash(String password_hash) {
        if (password_hash != null && !password_hash.isBlank())
            this.password_hash = password_hash;
    }
}

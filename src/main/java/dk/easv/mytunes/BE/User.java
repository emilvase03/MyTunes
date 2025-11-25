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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id != -1)
            this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.isBlank())
            this.username = username;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        if (password_hash != null && !password_hash.isBlank())
            this.password_hash = password_hash;
    }
}

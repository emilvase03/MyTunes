package dk.easv.mytunes.BE;

public class CurrentUser {
    private static CurrentUser instance;
    private User currentUser;

    private CurrentUser() {}

    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        if (user != null && currentUser == null)
            this.currentUser = user;
    }
}
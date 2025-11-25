package dk.easv.mytunes.BE;

public class Playlist {

    private int id;
    private int userId;
    private String name;
    private String songFilepaths;
    private String duration;

    public Playlist() {}

    public Playlist(int id, int userId, String name, String songFilepaths, String duration) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.songFilepaths = songFilepaths;
        this.duration = duration;
    }

    public Playlist(int userId, String name, String songFilepaths, String duration) {
        this.userId = userId;
        this.name = name;
        this.songFilepaths = songFilepaths;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSongFilepaths() {
        return songFilepaths;
    }

    public void setSongFilepaths(String songFilepaths) {
        this.songFilepaths = songFilepaths;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return name;
    }
}

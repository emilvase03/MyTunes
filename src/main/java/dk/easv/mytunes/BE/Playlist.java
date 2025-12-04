package dk.easv.mytunes.BE;

// Java imports
import java.util.List;

public class Playlist {

    private int id;
    private int userId;
    private String name;
    private List<Song> allSongs;
    private String duration;

    public Playlist(int id, int userId, String name, List<Song> allSongs, String duration) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.allSongs = allSongs;
        this.duration = duration;
    }

    public Playlist(String name, int userId) {
        this.name = name;
        this.userId = userId;

        this.id = -1;
        this.allSongs = null; // This doesn't seem correct??
        this.duration = "00:00";
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

    public List<Song> getAllSongsFromPlaylist() {
        return allSongs;
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

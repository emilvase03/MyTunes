package dk.easv.mytunes.BE;

public class Playlist {

    private int id;
    private int userId;
    private String name;
    private String songFilepaths;
    private String duration;

    public Playlist(int id, int userId, String name, String songFilepaths, String duration) {
<<<<<<< HEAD
        setId(id);
        setUserId(userId);
        setName(name);
        setSongFilepaths(songFilepaths);
        setDuration(duration);
=======
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.songFilepaths = songFilepaths;
        this.duration = duration;
>>>>>>> 7a3a196 (redid playbackmanager, optimized mainviewcontroller, added support for viewing songs in playlists etc)
    }

    public Playlist(String name, int userId) {
        setName(name);
        setUserId(userId);

        this.id = -1;
        this.songFilepaths = "";
        this.duration = "00:00";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id > 0)
            this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        if (userId > 0)
            this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank())
            this.name = name;
    }

    public String getSongFilepaths() {
        return songFilepaths;
    }

    public void setSongFilepaths(String songFilepaths) {
<<<<<<< HEAD
        if (songFilepaths != null && !songFilepaths.isBlank())
            this.songFilepaths = songFilepaths;
=======
        this.songFilepaths = songFilepaths;
>>>>>>> 7a3a196 (redid playbackmanager, optimized mainviewcontroller, added support for viewing songs in playlists etc)
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (duration != null && !duration.isBlank())
            this.duration = duration;
    }

    @Override
    public String toString() {
        return name;
    }
}
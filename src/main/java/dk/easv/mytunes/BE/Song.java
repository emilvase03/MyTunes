package dk.easv.mytunes.BE;

public class Song {
    private int id=-1;
    private int user_id=-1;
    private String filepath;
    private String title;
    private String artist;
    private String genre;
    private String duration;


    public Song(int id,int user_id,String filepath,String title,String artist,String genre,String duration){
        setId(id);
        setFilepath(filepath);
        setTitle(title);
        setArtist(artist);
        setGenre(genre);
        setDuration(duration);
        setUser_id(user_id);


    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(title!=null && !title.isBlank() )
            this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
       if(artist!=null&&!artist.isBlank())
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        if(genre!=null&&!genre.isBlank())
            this.genre = genre;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (duration !=null&&duration.isBlank())
         this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if(id!=-1)
         this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
       if(filepath!=null&&!filepath.isBlank())
        this.filepath = filepath;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
       if(user_id!=-1)
        this.user_id = user_id;
    }
}

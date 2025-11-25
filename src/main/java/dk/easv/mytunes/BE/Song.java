package dk.easv.mytunes.BE;

public class Song {
    private String title;
    private String artist;
    private String genre;
    private String duration;
    public Song(String title,String artist,String genre,String duration){
        setTitle(title);
        setArtist(artist);
        setGenre(genre);
        setDuration(duration);

    }

    private String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        if(title!=null && !title.isBlank() )
            this.title = title;
    }

    private String getArtist() {
        return artist;
    }

    private void setArtist(String artist) {
       if(artist!=null&&!artist.isBlank())
        this.artist = artist;
    }

    private String getGenre() {
        return genre;
    }

    private void setGenre(String genre) {
        if(genre!=null&&!genre.isBlank())
            this.genre = genre;
    }

    private String getDuration() {
        return duration;
    }

    private void setDuration(String duration) {
        if (duration !=null&&duration.isBlank())
         this.duration = duration;
    }
}

package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.Song;

import java.util.List;

public interface ISongDataAccess {
    List<Song> getAllSongs() throws Exception;

    Song createSong(Song newSong) throws Exception;

    void updateSong(Song song) throws Exception;

    void deleteSong(Song song) throws Exception;
}

package dk.easv.mytunes.BLL.UTIL;

// Project imports
import dk.easv.mytunes.BE.Song;

// Java imports
import java.util.ArrayList;
import java.util.List;

public class SongSearcher {
    public List<Song> search(List<Song> searchBase, String query) {
        List<Song> searchResult = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(searchBase); // return all if query empty
        }

        String q = query.toLowerCase().trim();

        for (Song song : searchBase) {
            if (comparedTo(song.getTitle(), q) ||
                    comparedTo(song.getArtist(), q) ||
                    comparedTo(song.getGenre(), q)) {
                searchResult.add(song);
            }
        }

        return searchResult;
    }

    private boolean comparedTo(String field, String query) {
        return field != null &&
                field.toLowerCase().contains(query);
    }
}

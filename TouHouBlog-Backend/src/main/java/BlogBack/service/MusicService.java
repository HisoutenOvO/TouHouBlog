package BlogBack.service;

import java.util.List;
import java.util.Map;

public interface MusicService {
    public List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception;
}

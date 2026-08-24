package BlogBack.service;

import BlogBack.pojo.vo.LyricLineVO;

import java.util.List;
import java.util.Map;

public interface MusicService {
    List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception;
    String getStreamUrl(String songId);
    List<LyricLineVO> getLyric(String songId) throws Exception;
}
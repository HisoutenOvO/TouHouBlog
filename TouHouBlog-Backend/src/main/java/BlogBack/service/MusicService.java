package BlogBack.service;

import BlogBack.pojo.vo.LyricLineVO;

import java.util.List;
import java.util.Map;

public interface MusicService {
    /**
     * 获取歌单列表
     * @param playlistId
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception;

    /**
     * 获取歌词
     * @param songId
     * @return
     * @throws Exception
     */
    List<LyricLineVO> getLyric(String songId) throws Exception;
}

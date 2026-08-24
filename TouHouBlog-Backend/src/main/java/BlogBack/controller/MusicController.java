package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.vo.LyricLineVO;
import BlogBack.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/music")
@Slf4j
@Tag(name = "音乐播放")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/stream")
    public void stream(@RequestParam String id, HttpServletResponse response) {
        try {
            String realUrl = musicService.getStreamUrl(id);
            if (realUrl == null || realUrl.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(realUrl);
        } catch (Exception e) {
            log.error("获取播放地址失败, songId={}", id, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping("/playlist")
    public Result<List<Map<String, String>>> getPlaylist() {
        try {
            String myPlaylistId = "18231256590";
            List<Map<String, String>> songs = musicService.getPlaylistSongs(myPlaylistId);
            return Result.success(songs);
        } catch (Exception e) {
            log.error("歌单加载失败", e);
            return Result.error("歌单加载失败: " + e.getMessage());
        }
    }

    @GetMapping("/lyric")
    @Operation(summary = "获取歌词")
    public Result<List<LyricLineVO>> getLyric(@RequestParam("songId") String songId) {
        try {
            List<LyricLineVO> lyricLines = musicService.getLyric(songId);
            return Result.success(lyricLines);
        } catch (Exception e) {
            log.error("歌词获取失败, songId={}", songId, e);
            return Result.error("歌词获取失败: " + e.getMessage());
        }
    }


}
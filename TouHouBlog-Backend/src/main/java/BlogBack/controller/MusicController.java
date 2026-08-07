package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.service.MusicService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    // 获取歌单（歌单ID配置在 application.yml 中）
    @GetMapping("/playlist")
    public Result<List<Map<String, String>>> getPlaylist() {
        try {
            // 在这里写死或从配置读取你的歌单 ID
            String myPlaylistId = "6617686504";
            List<Map<String, String>> songs = musicService.getPlaylistSongs(myPlaylistId);
            return Result.success(songs);
        } catch (Exception e) {
            return Result.error("歌单加载失败: " + e.getMessage());
        }
    }

    // 音频代理（同之前）
    @GetMapping("/stream")
    public void stream(@RequestParam String id, HttpServletResponse response) throws IOException {
        String url = "https://music.163.com/song/media/outer/url?id=" + id + ".mp3";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        conn.setRequestProperty("Referer", "https://music.163.com/");
        response.setContentType("audio/mpeg");
        try (InputStream in = conn.getInputStream();
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }
}

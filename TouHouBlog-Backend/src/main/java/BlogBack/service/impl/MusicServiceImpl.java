package BlogBack.service.impl;

import BlogBack.service.MusicService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MusicServiceImpl implements MusicService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception {
        String apiUrl = "https://api.i-meto.com/meting/api?server=netease&type=playlist&id=" + playlistId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();
        JsonNode root = mapper.readTree(body);

        List<Map<String, String>> songs = new ArrayList<>();

        for (JsonNode track : root) {
            // 使用真实字段名
            String title = track.path("title").asText("");
            String author = track.path("author").asText("");
            String pic = track.path("pic").asText("");
            String metingUrl = track.path("url").asText("");

            // 从 Meting URL 中提取歌曲 ID
            String songId = extractIdFromUrl(metingUrl);
            if (songId.isEmpty()) {
                songId = String.valueOf(track.hashCode()); // fallback
            }

            // 自己的代理地址
            String playUrl = "/api/music/stream?id=" + songId;

            Map<String, String> song = new HashMap<>();
            song.put("id", songId);
            song.put("name", title.isEmpty() ? "未知歌曲" : title);
            song.put("artist", author.isEmpty() ? "未知歌手" : author);
            song.put("cover", pic);
            song.put("url", metingUrl);  // 直接使用 Meting 返回的播放链接

            songs.add(song);
        }

        return songs;
    }

    // 从类似 "https://api.i-meto.com/...?id=774719&auth=..." 的链接中提取 id
    private String extractIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            String query = url.contains("?") ? url.split("\\?")[1] : "";
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && "id".equals(pair[0])) {
                    return pair[1];
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}
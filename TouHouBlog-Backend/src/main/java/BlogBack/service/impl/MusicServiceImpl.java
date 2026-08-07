package BlogBack.service.impl;

import BlogBack.service.MusicService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MusicServiceImpl implements MusicService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 从网易云歌单 API 获取歌曲列表，自动过滤 VIP 歌曲
    public List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception {
        String url = "https://music.163.com/api/playlist/detail?id=" + playlistId;
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();
        Response response = client.newCall(request).execute();
        String body = response.body().string();
        JsonNode root = mapper.readTree(body);
        JsonNode tracks = root.path("result").path("tracks");

        List<Map<String, String>> songs = new ArrayList<>();
        for (JsonNode track : tracks) {
            int fee = track.path("fee").asInt(); // 0=免费, 其他=VIP/试听
            if (fee != 0) continue; // 直接跳过 VIP 歌曲

            long id = track.path("id").asLong();
            String name = track.path("name").asText();
            String artist = track.path("ar").get(0).path("name").asText();
            String cover = track.path("al").path("picUrl").asText();

            Map<String, String> song = new HashMap<>();
            song.put("id", String.valueOf(id));
            song.put("name", name);
            song.put("artist", artist);
            song.put("cover", cover);
            songs.add(song);
        }
        return songs;
    }
}

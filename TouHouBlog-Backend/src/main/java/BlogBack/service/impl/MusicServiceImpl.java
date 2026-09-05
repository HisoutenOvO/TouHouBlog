package BlogBack.service.impl;

import BlogBack.service.MusicService;
import BlogBack.pojo.vo.LyricLineVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MusicServiceImpl implements MusicService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Map<String, String>> getPlaylistSongs(String playlistId) throws Exception {
        // 从本地文件读取歌单，不再请求外部 API
        java.nio.file.Path filePath = java.nio.file.Paths.get("/opt/touhoublog/playlist.json");
        byte[] bytes = java.nio.file.Files.readAllBytes(filePath);
        JsonNode root = mapper.readTree(bytes);

        List<Map<String, String>> songs = new ArrayList<>();
        for (JsonNode track : root) {
            String songId = track.path("id").asText("");
            String title = track.path("name").asText("");
            String artist = track.path("artist").asText("");

            if (songId.isEmpty()) continue;

            String playUrl = "https://music.163.com/song/media/outer/url?id=" + songId + ".mp3";
            String coverUrl = "/images/default-cover.jpg";

            Map<String, String> song = new HashMap<>();
            song.put("id", songId);
            song.put("name", title.isEmpty() ? "未知歌曲" : title);
            song.put("artist", artist.isEmpty() ? "未知歌手" : artist);
            song.put("cover", coverUrl);
            song.put("url", playUrl);
            songs.add(song);
        }
        return songs;
    }

    @Override
    public String getStreamUrl(String songId) {
        return "https://music.163.com/song/media/outer/url?id=" + songId + ".mp3";
    }

    @Override
    public List<LyricLineVO> getLyric(String songId) throws Exception {
        String url = "https://music.163.com/api/song/lyric?id=" + songId + "&lv=1&kv=1&tv=-1";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new Exception("歌词接口返回失败");
            }
            String json = response.body().string();
            JsonNode root = mapper.readTree(json);

            String lrc = root.path("lrc").path("lyric").asText("");
            String tlyric = root.path("tlyric").path("lyric").asText("");

            List<LyricLineVO> originalLines = parseLrc(lrc);
            List<LyricLineVO> translatedLines = parseLrc(tlyric);

            for (LyricLineVO line : originalLines) {
                LyricLineVO bestMatch = null;
                double minDiff = 0.1;
                for (LyricLineVO trans : translatedLines) {
                    double diff = Math.abs(trans.getTime() - line.getTime());
                    if (diff < minDiff) {
                        minDiff = diff;
                        bestMatch = trans;
                    }
                }
                if (bestMatch != null) {
                    line.setTranslation(bestMatch.getText());
                }
            }
            return originalLines;
        }
    }

    private List<LyricLineVO> parseLrc(String lrc) {
        List<LyricLineVO> lines = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?\\]");
        String[] rows = lrc.split("\\n");
        for (String row : rows) {
            Matcher matcher = pattern.matcher(row);
            if (matcher.find()) {
                int min = Integer.parseInt(matcher.group(1));
                int sec = Integer.parseInt(matcher.group(2));
                String millisStr = matcher.group(3);
                double time = min * 60 + sec;
                if (millisStr != null) {
                    if (millisStr.length() == 1) {
                        time += Double.parseDouble("0.00" + millisStr);
                    } else if (millisStr.length() == 2) {
                        time += Double.parseDouble("0.0" + millisStr);
                    } else {
                        time += Double.parseDouble("0." + millisStr);
                    }
                }
                String text = row.substring(matcher.end()).trim();
                if (!text.isEmpty()) {
                    lines.add(new LyricLineVO(time, text, ""));
                }
            }
        }
        return lines;
    }

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
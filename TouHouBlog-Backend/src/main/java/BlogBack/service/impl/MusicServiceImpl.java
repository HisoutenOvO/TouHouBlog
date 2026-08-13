package BlogBack.service.impl;

import BlogBack.service.MusicService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import BlogBack.pojo.vo.LyricLineVO;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * 获取歌词
     * @param songId
     * @return
     * @throws Exception
     */
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

            // 合并翻译：按时间最接近匹配
            for (LyricLineVO line : originalLines) {
                LyricLineVO bestMatch = null;
                double minDiff = 0.1; // 允许的时间差阈值
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

    /**
     * 解析歌词
     * @param lrc
     * @return
     */
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
                    // 统一转成三位小数
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
}
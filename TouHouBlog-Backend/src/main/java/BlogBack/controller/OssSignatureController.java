package BlogBack.controller;

import BlogBack.common.result.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oss")          // 接口前缀
public class OssSignatureController {

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.region}")
    private String region;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.endpoint}")
    private String endpoint;

    @GetMapping("/signature")       // 路径为 /api/oss/signature
    public Result<Map<String, Object>> getOssConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("accessKeyId", accessKeyId);
        data.put("accessKeySecret", accessKeySecret);
        data.put("region", region);
        data.put("bucket", bucket);
        data.put("endpoint", endpoint);
        return Result.success(data);
    }
    @PostConstruct
    public void init() {
        System.out.println("OSS Config loaded: " + accessKeyId + ", bucket=" + bucket);
    }
}
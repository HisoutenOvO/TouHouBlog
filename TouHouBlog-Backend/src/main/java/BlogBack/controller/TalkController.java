package BlogBack.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 杂谈接口
 */
@RestController
@RequestMapping("/talks")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "杂谈接口")
public class TalkController {
}

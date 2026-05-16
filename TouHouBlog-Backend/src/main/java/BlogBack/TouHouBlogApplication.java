package BlogBack;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TouHouBlogApplication {
    public static void main(String[] args){
        SpringApplication.run(TouHouBlogApplication.class,args);
        log.info("TouHouBlog项目启动成功!");
    }
}

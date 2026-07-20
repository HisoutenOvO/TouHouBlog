package BlogBack;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement
public class TouHouBlogApplication {
    public static void main(String[] args){
        SpringApplication.run(TouHouBlogApplication.class,args);
        log.info("TouHouBlog项目启动成功!");
    }
}

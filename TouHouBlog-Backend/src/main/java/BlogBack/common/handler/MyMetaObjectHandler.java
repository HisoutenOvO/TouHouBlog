package BlogBack.common.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段填充
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 新增数据
     * @param metaObject
     */
    public void insertFill(MetaObject metaObject){
        strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
        strictInsertFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
    }

    /**
     * 修改数据
     * @param metaObject
     */

    public void updateFill(MetaObject metaObject){
        strictUpdateFill(metaObject,"updateTime",LocalDateTime.class,LocalDateTime.now());
    }
}

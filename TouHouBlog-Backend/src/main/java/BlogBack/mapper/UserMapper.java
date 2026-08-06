package BlogBack.mapper;

import BlogBack.pojo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户id查询其昵称
     * @param id
     * @return
     */
    @Select("select nickname from user where id = #{id}")
    String getNicknameById(Long id);
}

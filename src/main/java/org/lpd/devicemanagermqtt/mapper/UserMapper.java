package org.lpd.devicemanagermqtt.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.lpd.devicemanagermqtt.models.entity.User;
import org.springframework.stereotype.Repository;


@Repository
@Mapper
public interface UserMapper {


    void insert(User user);

    User findByUsername(String username);
}

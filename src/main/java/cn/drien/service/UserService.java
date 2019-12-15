package cn.drien.service;

import cn.drien.entity.User;

public interface UserService extends BaseService<User> {

    User findByName(String name);
}

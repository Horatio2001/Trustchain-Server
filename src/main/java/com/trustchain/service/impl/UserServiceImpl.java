package com.trustchain.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.relation.RelationManager;
import com.trustchain.enums.RegisterStatus;
import com.trustchain.model.convert.UserConvert;
import com.trustchain.mapper.UserMapper;
import com.trustchain.mapper.UserRegisterMapper;
import com.trustchain.model.entity.User;
import com.trustchain.model.entity.UserRegister;
import com.trustchain.model.vo.UserLogin;
import com.trustchain.service.EmailSerivce;
import com.trustchain.service.FabricService;
import com.trustchain.service.MinioService;
import com.trustchain.service.UserService;
import com.trustchain.util.PasswordUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRegisterMapper userRegMapper;
    @Autowired
    private MinioService minioService;
    @Autowired
    private FabricService fabricService;
    @Autowired
    private EmailSerivce emailSerivce;

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    @Override
    public UserLogin login(String orgId, String username, String password) {
        QueryWrapper query = QueryWrapper.create()
                .from(User.class)
                .where(User::getOrganizationId).eq(orgId)
                .and(User::getUsername).eq(username);

        User user = userMapper.selectOneWithRelationsByQuery(query);

        if (user != null && PasswordUtil.match(password, user.getPassword())) {
            // SA-Token登录并缓存数据
            StpUtil.login(user.getId());
            StpUtil.getSession().set("user", user);
            return new UserLogin(UserConvert.INSTANCE.toUserVO(user), StpUtil.getTokenInfo());
        }
        return null;
    }

    @Override
    public void logout(String userId) {
        StpUtil.logout(userId);
    }

    @Override
    public boolean resetPassword(String ordId, String username, String password) {
        QueryWrapper query = QueryWrapper.create()
                .from(User.class)
                .where(User::getOrganizationId).eq(ordId)
                .and(User::getUsername).eq(username);

        User user = userMapper.selectOneByQuery(query);
        user.setPassword(PasswordUtil.encrypt(password));

        return userMapper.update(user) != 0;
    }

    @Override
    public String registerApply(UserRegister userReg) {
        int count = userRegMapper.insert(userReg);

        if (count != 0) {
            emailSerivce.send(userReg.getEmail(), "数据资源可信共享平台 注册申请",
                    "欢迎您注册数据资源可信共享平台, 您的注册申请号如下。<br>" +
                            "<h3>" + userReg.getRegId() + "</h3>");
            return userReg.getRegId();
        } else {
            return null;
        }
    }

    @Override
    public List<UserRegister> registerApplySearch(List<String> regIds) {
        List<UserRegister> userRegs = new ArrayList<UserRegister>();
        regIds.forEach(regId -> {
            userRegs.add(userRegMapper.selectOneWithRelationsById(regId));
        });

        logger.info(userRegs);

        return userRegs;
    }

    @Override
    public boolean registerReply(String regId, RegisterStatus reply, String reason) {
        UserRegister userReg = userRegMapper.selectOneById(regId);
        if (userReg == null) {
            return false;
        }
        if (reply == RegisterStatus.ALLOW) {
            String oldLogoPath = userReg.getLogo();
            String newLogoPath = "user/" + oldLogoPath.substring(oldLogoPath.lastIndexOf("/") + 1);
            minioService.copy(oldLogoPath, newLogoPath);

            User user = UserConvert.INSTANCE.toUser(userReg);
            user.setLogo(newLogoPath);
            int count = userMapper.insert(user);
            if (count != 0) {
                userReg.setId(user.getId());
                userReg.setRegStatus(RegisterStatus.ALLOW);
                userReg.setReplyTime(new Date());

                userRegMapper.update(userReg);

                emailSerivce.send(user.getEmail(), "数据资源可信共享平台 注册成功",
                        "您的用户注册申请已通过, 请点击以下链接进行登录。<br>" +
                                "<a>http://localhost:5173</a>");
                return true;
            }
            return false;
        } else if (reply == RegisterStatus.REJECT) {
            userReg.setRegStatus(RegisterStatus.REJECT);
            userReg.setReplyTime(new Date());
            userReg.setReplyReason(reason);

            userRegMapper.update(userReg);

            emailSerivce.send(userReg.getEmail(), "数据资源可信共享平台 注册失败",
                    "您的用户注册申请未通过, 请点击以下链接查看详情。<br>" +
                            "<a>http://localhost:5173/registerApplySearch</a>");
            return true;
        }
        return false;
    }

    @Override
    public boolean register(User user) {
        int count = userMapper.insert(user);

        return count != 0;
    }

    @Override
    public List<UserRegister> registerList(String orgId) {
        QueryWrapper query = QueryWrapper.create()
                .from(UserRegister.class)
                .where(UserRegister::getOrganizationId).eq(orgId);

        return userRegMapper.selectListByQuery(query);
    }

    @Override
    public UserRegister registerDetail(String regId) {
        RelationManager.setMaxDepth(1);
        return userRegMapper.selectOneWithRelationsById(regId);
    }

    @Override
    public boolean exist(String orgId, String username, String userId) {
        QueryWrapper query = QueryWrapper.create()
                .from(User.class)
                .where(User::getOrganizationId).eq(orgId)
                .and(User::getUsername).eq(username)
                .and(User::getId).ne(userId);

        User user = userMapper.selectOneByQuery(query);

        return user != null;
    }

    @Override
    public List<User> subordinateList(String orgId) {
        QueryWrapper query = QueryWrapper.create()
                .from(User.class)
                .where(User::getOrganizationId).eq(orgId);

        return userMapper.selectListByQuery(query);
    }

    @Override
    public User subordinateDetail(String userId) {
        return userMapper.selectOneById(userId);
    }

    @Override
    public User informationDetail(String userId, String version) {
        return userMapper.selectOneById(userId);
    }
}

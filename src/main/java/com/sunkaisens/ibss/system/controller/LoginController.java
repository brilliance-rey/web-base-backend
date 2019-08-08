package com.sunkaisens.ibss.system.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.lionsoul.ip2region.DbSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunkaisens.ibss.common.annotation.Limit;
import com.sunkaisens.ibss.common.authentication.JWTToken;
import com.sunkaisens.ibss.common.authentication.JWTUtil;
import com.sunkaisens.ibss.common.domain.ActiveUser;
import com.sunkaisens.ibss.common.domain.IBSSConstant;
import com.sunkaisens.ibss.common.domain.SunkResponse;
import com.sunkaisens.ibss.common.exception.SysInnerException;
import com.sunkaisens.ibss.common.properties.IBSSProperties;
import com.sunkaisens.ibss.common.service.RedisService;
import com.sunkaisens.ibss.common.utils.AddressUtil;
import com.sunkaisens.ibss.common.utils.DateUtil;
import com.sunkaisens.ibss.common.utils.IPUtil;
import com.sunkaisens.ibss.common.utils.MD5Util;
import com.sunkaisens.ibss.common.utils.SunkUtil;
import com.sunkaisens.ibss.system.dao.LoginLogMapper;
import com.sunkaisens.ibss.system.domain.LoginLog;
import com.sunkaisens.ibss.system.domain.User;
import com.sunkaisens.ibss.system.domain.UserConfig;
import com.sunkaisens.ibss.system.manager.UserManager;
import com.sunkaisens.ibss.system.service.LoginLogService;
import com.sunkaisens.ibss.system.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.sf.saxon.functions.ConstantFunction.False;


@Validated
@RestController
@Configuration
@Api(description="登陆的实现")
public class LoginController {
	
    @Value("${ibss.ip2region.enabled}")
	private boolean ip2RegionEn;
	    
    @Autowired
    private RedisService redisService;
    @Autowired
    public UserManager userManager;
    @Autowired
    private UserService userService;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private IBSSProperties properties;
    @Autowired
    private ObjectMapper mapper;

    @ApiOperation(value="用户登陆", notes="根据用户名和密码 string类型")
    @PostMapping("/login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public SunkResponse login(
            @NotBlank(message = "{required}")@RequestParam String username,
            @NotBlank(message = "{required}")@RequestParam String password, HttpServletRequest request) throws Exception {
    	username = StringUtils.lowerCase(username);
        password = MD5Util.encrypt(username, password);
        final String errorMessage = "用户名或密码错误";
         //根据用户名查用户信息
         User user = this.userManager.getUser(username);

        if (user == null) 
            throw new SysInnerException(errorMessage);
        if (!StringUtils.equals(user.getPassword(), password)) 
            throw new SysInnerException(errorMessage);
        if (User.STATUS_LOCK.equals(user.getStatus())) 
            throw new SysInnerException("账号已被锁定,请联系管理员！");
            

        // 更新用户登录时间
        this.userService.updateLoginTime(username);
        // 保存登录记录
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(username);
        this.loginLogService.saveLoginLog(loginLog);
        
         //登录生成token 并加密
        String token = SunkUtil.encryptToken(JWTUtil.sign(username, password));
        //获取当前日期时间和秒数
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        //格式化时间
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);
        //生成一个useerID便于登录的使用 存到redis中 
        String userId = this.saveTokenToRedis(user, jwtToken, request);
        user.setId(userId);
        
        // xsh 2019/8/8 生成前端需要的信息 先保留 后期需要用了 再改回来。 合并的
        //Map<String, Object> userInfo = this.generateUserInfo(jwtToken, user);
        
        //xsh 2019/8/8 先保留 后期用了 再改回来。
        //向前端传递token 封装到Map中   
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", jwtToken.getToken());
        userInfo.put("exipreTime", jwtToken.getExipreAt());
        return new SunkResponse().message("认证成功").data(userInfo);
    }

    
    //生成登录的日志文件   xsh 2019/8/1
    @GetMapping("index/{username}")
    public SunkResponse index(@NotBlank(message = "{required}") @PathVariable String username) {
        Map<String, Object> data = new HashMap<>();
        // 获取系统访问记录
        Long totalVisitCount = loginLogMapper.findTotalVisitCount();
        data.put("totalVisitCount", totalVisitCount);
        Long todayVisitCount = loginLogMapper.findTodayVisitCount();
        data.put("todayVisitCount", todayVisitCount);
        Long todayIp = loginLogMapper.findTodayIp();
        data.put("todayIp", todayIp);
        // 获取近期系统访问记录
        List<Map<String, Object>> lastSevenVisitCount = loginLogMapper.findLastSevenDaysVisitCount(null);
        data.put("lastSevenVisitCount", lastSevenVisitCount);
        User param = new User();
        param.setUsername(username); 
        List<Map<String, Object>> lastSevenUserVisitCount = loginLogMapper.findLastSevenDaysVisitCount(param);
        data.put("lastSevenUserVisitCount", lastSevenUserVisitCount);
        return new SunkResponse().data(data);
    }

    @RequiresPermissions("user:online")
    @GetMapping("online")
    public SunkResponse userOnline(String username) throws Exception {
        String now = DateUtil.formatFullTime(LocalDateTime.now());
        Set<String> userOnlineStringSet = redisService.zrangeByScore(IBSSConstant.ACTIVE_USERS_ZSET_PREFIX, now, "+inf");
        List<ActiveUser> activeUsers = new ArrayList<>();
        for (String userOnlineString : userOnlineStringSet) {
            ActiveUser activeUser = mapper.readValue(userOnlineString, ActiveUser.class);
            activeUser.setToken(null);
            if (StringUtils.isNotBlank(username)) {
                if (StringUtils.equalsIgnoreCase(username, activeUser.getUsername()))
                    activeUsers.add(activeUser);
            } else {
                	activeUsers.add(activeUser);
            }
        }
        return new SunkResponse().data(activeUsers);
    }

    @DeleteMapping("kickout/{id}")
    @RequiresPermissions("user:kickout")
    public void kickout(@NotBlank(message = "{required}") @PathVariable String id) throws Exception {
    	//获取最新的时间
        String now = DateUtil.formatFullTime(LocalDateTime.now());
        //获取最新时间的数据 
        Set<String> userOnlineStringSet = redisService.zrangeByScore(IBSSConstant.ACTIVE_USERS_ZSET_PREFIX, now, "+inf");
        ActiveUser kickoutUser = null;
        String kickoutUserString = "";
        for (String userOnlineString : userOnlineStringSet) {
        	//遍历数据的定义到ActiveUser用户上
            ActiveUser activeUser = mapper.readValue(userOnlineString, ActiveUser.class);
            //判断是不是同一个用户id
            if (StringUtils.equals(activeUser.getId(), id)) {
            	//进行赋值
                kickoutUser = activeUser;
                kickoutUserString = userOnlineString;
            }
        }
        
        //判断信息是否为空 如果不为空清楚redies的数据
        if (kickoutUser != null && StringUtils.isNotBlank(kickoutUserString)) {
            // 删除 zset中的记录
            redisService.zrem(IBSSConstant.ACTIVE_USERS_ZSET_PREFIX, kickoutUserString);
            // 删除对应的 token缓存
            redisService.del(IBSSConstant.TOKEN_CACHE_PREFIX + kickoutUser.getToken() + "." + kickoutUser.getIp());
        }
    }

    
    //退出登录     xsh 2019/8/1
    @GetMapping("logout/{id}")
    public void logout(@NotBlank(message = "{required}") @PathVariable String id) throws Exception {
    	System.out.println(id);
        this.kickout(id);
    }
  
    //注册用户
    @PostMapping("regist")
    public void regist(
            @NotBlank(message = "{required}") String username,
            @NotBlank(message = "{required}") String password) throws Exception {
        this.userService.regist(username, password);
    }

    
    
    private String saveTokenToRedis(User user, JWTToken token, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIpAddr(request);

        // 构建在线用户
        ActiveUser activeUser = new ActiveUser();
        activeUser.setUsername(user.getUsername());
        activeUser.setIp(ip);
        activeUser.setToken(token.getToken());
        
        if(ip2RegionEn) {
        	activeUser.setLoginAddress(AddressUtil.getCityInfo(DbSearcher.BTREE_ALGORITHM, ip));
        }

        // zset 存储登录用户，score 为过期时间戳
        this.redisService.zadd(IBSSConstant.ACTIVE_USERS_ZSET_PREFIX, Double.valueOf(token.getExipreAt()), mapper.writeValueAsString(activeUser));
        // redis 中存储这个加密 token，key = 前缀 + 加密 token + .ip
        this.redisService.set(IBSSConstant.TOKEN_CACHE_PREFIX + token.getToken() + StringPool.DOT + ip, token.getToken(), properties.getShiro().getJwtTimeOut() * 1000);

        return activeUser.getId();
    }
    
    
    /**
              * 生成前端需要的用户信息，包括： 先留下后期用了放开。
     * 1. token
     * 2. Vue Router
     * 3. 用户角色
     * 4. 用户权限
     * 5. 前端系统个性化配置信息
     *
     * @param token token
     * @param user  用户信息
     * @return UserInfo
     */
    /*private Map<String, Object> generateUserInfo(JWTToken token, User user) {
        String username = user.getUsername();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token.getToken());
        userInfo.put("exipreTime", token.getExipreAt());

        Set<String> roles = this.userManager.getUserRoles(username);
        userInfo.put("roles", roles);

        Set<String> permissions = this.userManager.getUserPermissions(username);
        userInfo.put("permissions", permissions);

        UserConfig userConfig = this.userManager.getUserConfig(String.valueOf(user.getUserId()));
        userInfo.put("config", userConfig);

        user.setPassword("it's a secret");
        userInfo.put("user", user);
        return userInfo;
    }*/


     /**
     * 
     * xsh  2019/07/18 根据token 获取用户的信息
     */
    @ApiOperation(value="登录根据token 获取用户的信息")
    @GetMapping("login/user-info")
    public SunkResponse generateUser(@NotBlank(message = "{required}") String username) {
    	Map<String, Object> userInfo = new HashMap<>();
    	System.out.println("進來了");
    	if(username !=null && !"".equals(username)) {
    		username = StringUtils.lowerCase(username);
    		System.out.println(username);
    		User user =this.userManager.getUser(username);
    		/**
    		 * 通过用户 ID获取前端系统个性化配置
    		 */
    		UserConfig userConfig = this.userManager.getUserConfig(String.valueOf(user.getUserId()));
    		userInfo.put("config", userConfig);
    		
    		Set<String> roles =this.userManager.getUserRoles(username);
    		userInfo.put("roles", roles);
    		
    		/**
    		 * 通过用户名获取用户权限集合
    		 */
    		Set<String> permissions = this.userManager.getUserPermissions(username);
    		userInfo.put("permissions", permissions);
    		user.setPassword("it's a secret");
    		userInfo.put("user", user);
    		System.out.println(userInfo);
    	}
    	  return new SunkResponse().data(userInfo);
    }
}

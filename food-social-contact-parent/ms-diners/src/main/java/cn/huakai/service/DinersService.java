package cn.huakai.service;

import cn.huakai.config.OAuth2ClientConfiguration;
import cn.huakai.constant.ApiConstant;
import cn.huakai.constant.RedisKeyConstant;
import cn.huakai.dto.OAuthDinerInfo;
import cn.huakai.mapper.DinersMapper;
import cn.huakai.model.domain.ResultInfo;
import cn.huakai.model.dto.DinersDTO;
import cn.huakai.model.pojo.Diners;
import cn.huakai.utils.AssertUtil;
import cn.huakai.utils.ResultInfoUtil;
import cn.huakai.vo.LoginDinerInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: huakaimay
 * @since: 2023-06-29
 */
@Service
@Slf4j
public class DinersService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.name.ms-oauth-server}")
    private String outhName;

    @Autowired
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DinersMapper dinersMapper;

    /**
     * 转发调用outh2服务的登录
     *
     * @param account
     * @param password
     * @param path
     * @return
     */
    public ResultInfo signIn(String account, String password, String path) {
        AssertUtil.isNotEmpty(account, "请输入账户");
        AssertUtil.isNotEmpty(password, "请输入密码");

        // 构建请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", account);
        params.add("password", password);
        params.add("grant_type", oAuth2ClientConfiguration.getGrant_type());
        params.add("scope", oAuth2ClientConfiguration.getScope());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 设置OAuth2的client ID和secret
        String clientId = oAuth2ClientConfiguration.getClientId();
        String clientSecret = oAuth2ClientConfiguration.getSecret();
        String credentials = clientId + ":" + clientSecret;
        byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
        byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
        String base64Credentials = new String(base64CredentialsBytes, StandardCharsets.UTF_8);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // 发送登录请求
        String oauthUrl = outhName + "oauth/token";
        System.out.println("oauthUrl = " + oauthUrl);
        ResponseEntity<ResultInfo> responseEntity = restTemplate.exchange(oauthUrl, HttpMethod.POST, requestEntity, ResultInfo.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return ResultInfoUtil.buildError(0, "Failed to sign in", path);
        }

        ResultInfo result = responseEntity.getBody();
        if (ApiConstant.SUCCESS_CODE != result.getCode()) {
            return ResultInfoUtil.buildError(0, result.getMessage(), path);
        }
        OAuthDinerInfo dinerInfo = BeanUtil.mapToBean((Map<String, Object>) result.getData(), OAuthDinerInfo.class, false);
        LoginDinerInfo loginDinerInfo = new LoginDinerInfo();
        loginDinerInfo.setToken(dinerInfo.getAccessToken());
        loginDinerInfo.setNickname(dinerInfo.getNickname());
        loginDinerInfo.setAvatarUrl(dinerInfo.getAvatarUrl());

        return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
    }


    @Transactional(rollbackFor = Exception.class)
    public ResultInfo register(DinersDTO dinersDTO, String path) {
        // 参数非空校验
        String username = dinersDTO.getUsername();
        AssertUtil.isNotEmpty(username, "请输入用户名");
        String password = dinersDTO.getPassword();
        AssertUtil.isNotEmpty(password, "请输入密码");
        String phone = dinersDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "请输入验证码");
        // 获取验证码
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        String code =   redisTemplate.opsForValue().get(key);
        // 验证是否过期
        AssertUtil.isNotEmpty(code, "验证码已过期，请重新发送");
        // 验证码一致性校验
        AssertUtil.isTrue(!dinersDTO.getVerifyCode().equals(code), "验证码不一致，请重新输入");
        // 验证用户名是否已注册
        Diners diners = dinersMapper.selectByUsername(username.trim());
        AssertUtil.isTrue(diners != null, "用户名已存在，请重新输入");
        // 注册
        // 密码加密
        dinersDTO.setPassword(DigestUtil.md5Hex(password.trim()));
        dinersMapper.save(dinersDTO);
        // 自动登录
        return ResultInfoUtil.buildSuccess(path);
    }

    public void sendVerifyCode(String phone) {
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        // 短信是否在60s内发送过，发送过直接返回
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        if (existCode(key)) {
            return;
        }
        // 生成验证码，模拟发送短信
        String code = RandomUtil.randomNumbers(6);
        log.info("verifyCode: {}", code);
        // 存入redis
        redisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
    }

    /**
     * 验证码是否有效
     */
    private boolean existCode(String key) {
        String codeInRedis = redisTemplate.opsForValue().get(key);
        return !StringUtils.isEmpty(codeInRedis);
    }

    public void checkPhoneIsRegistered(String phone) {
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        Diners diners = dinersMapper.selectByPhone(phone);
        AssertUtil.isTrue(diners != null, "手机号已注册");
    }
}

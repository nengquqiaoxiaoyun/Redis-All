package cn.huakai.service;

import cn.huakai.config.OAuth2ClientConfiguration;
import cn.huakai.config.RestTemplateConfiguration;
import cn.huakai.constant.ApiConstant;
import cn.huakai.dto.OAuthDinerInfo;
import cn.huakai.model.domain.ResultInfo;
import cn.huakai.utils.AssertUtil;
import cn.huakai.utils.ResultInfoUtil;
import cn.huakai.vo.LoginDinerInfo;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author: huakaimay
 * @since: 2023-06-29
 */
@Service
public class DinersService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service.name.ms-oauth-server}")
    private String outhName;

    @Autowired
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;

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

}

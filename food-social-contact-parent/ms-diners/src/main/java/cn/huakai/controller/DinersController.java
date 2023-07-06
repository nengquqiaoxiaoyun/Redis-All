package cn.huakai.controller;

import cn.huakai.model.domain.ResultInfo;
import cn.huakai.model.dto.DinersDTO;
import cn.huakai.service.DinersService;
import cn.huakai.utils.ResultInfoUtil;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 食客服务控制层
 */
@Api(tags = "食客相关接口")
@RestController
public class DinersController {

    @Resource
    private DinersService dinersService;

    @Resource
    private HttpServletRequest request;

    /**
     * 登录
     *
     * @param account
     * @param password
     * @return
     */
    @GetMapping("signin")
    public ResultInfo signIn(String account, String password) {
        return dinersService.signIn(account, password, request.getServletPath());
    }


    @GetMapping("sendVerifyCode")
    public ResultInfo sendVerifyCode(String phone) {
        dinersService.sendVerifyCode(phone);
        return ResultInfoUtil.buildSuccess("发送成功", request.getServletPath());
    }


    @PostMapping("register")
    public ResultInfo register(@RequestBody DinersDTO dinersDTO) {
        return dinersService.register(dinersDTO, request.getServletPath());
    }

    /**
     * 校验手机号是否已注册
     *
     * @param phone
     * @return
     */
    @GetMapping("checkPhone")
    public ResultInfo checkPhone(String phone) {
        dinersService.checkPhoneIsRegistered(phone);
        return ResultInfoUtil.buildSuccess(request.getServletPath());
    }

}

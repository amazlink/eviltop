package io.github.rothschil.war.web.controller.system;

import io.github.rothschil.war.core.domain.SysMenu;
import io.github.rothschil.war.core.domain.SysUser;
import io.github.rothschil.war.core.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import io.github.rothschil.common.utils.DateUtils;
import io.github.rothschil.common.utils.ServletUtils;
import io.github.rothschil.common.utils.StringUtils;
import io.github.rothschil.common.conf.DrunkardConfig;
import io.github.rothschil.common.core.controller.BaseController;
import io.github.rothschil.common.core.domain.AjaxResult;
import io.github.rothschil.common.text.Convert;
import io.github.rothschil.common.utils.CookieUtils;
import io.github.rothschil.common.utils.ShiroUtils;
import io.github.rothschil.framework.shiro.service.SysPasswordService;
import io.github.rothschil.war.constant.ShiroConstants;
import io.github.rothschil.war.core.service.ISysMenuService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 首页 业务处理
 *
 * @author <a href="https://github.com/rothschil">Sam</a>
 * @date 2019/10/9 - 21:09
 * @since 1.0.0
 */
@Controller
public class SysIndexController extends BaseController {
    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysPasswordService passwordService;

    /**
     * 系统首页
     *
     * @param mmap ModelMap
     * @return String None
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @GetMapping("/index")
    public String index(ModelMap mmap) {
        // 取身份信息
        SysUser user = ShiroUtils.getSysUser();
        // 根据用户id取出菜单
        List<SysMenu> menus = menuService.selectMenusByUser(user);
        mmap.put("menus", menus);
        mmap.put("user", user);
        mmap.put("sideTheme", configService.selectConfigByKey("sys.index.sideTheme"));
        mmap.put("skinName", configService.selectConfigByKey("sys.index.skinName"));
        mmap.put("ignoreFooter", configService.selectConfigByKey("sys.index.ignoreFooter"));
        mmap.put("copyrightYear", DrunkardConfig.getCopyrightYear());
        mmap.put("demoEnabled", DrunkardConfig.isDemoEnabled());
        mmap.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
        mmap.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));

        // 菜单导航显示风格
        String menuStyle = configService.selectConfigByKey("sys.index.menuStyle");
        // 移动端，默认使左侧导航菜单，否则取默认配置
        String indexStyle = ServletUtils.checkAgentIsMobile(ServletUtils.getRequest().getHeader("User-Agent")) ? "index" : menuStyle;

        // 优先Cookie配置导航菜单
        Cookie[] cookies = ServletUtils.getRequest().getCookies();
        for (Cookie cookie : cookies) {
            if (StringUtils.isNotEmpty(cookie.getName()) && "nav-style".equalsIgnoreCase(cookie.getName())) {
                indexStyle = cookie.getValue();
                break;
            }
        }
        return "topnav".equalsIgnoreCase(indexStyle) ? "index-topnav" : "index";
    }

    /**
     * 锁定屏幕
     *
     * @param mmap ModelMap
     * @return String None
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @GetMapping("/lockscreen")
    public String lockscreen(ModelMap mmap) {
        mmap.put("user", ShiroUtils.getSysUser());
        ServletUtils.getSession().setAttribute(ShiroConstants.LOCK_SCREEN, true);
        return "lock";
    }

    /**
     * 解锁屏幕
     *
     * @param password String
     * @return AjaxResult None
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @PostMapping("/unlockscreen")
    @ResponseBody
    public AjaxResult unlockscreen(String password) {
        SysUser user = ShiroUtils.getSysUser();
        if (StringUtils.isNull(user)) {
            return AjaxResult.error("服务器超时，请重新登陆");
        }
        if (passwordService.matches(user, password)) {
            ServletUtils.getSession().removeAttribute(ShiroConstants.LOCK_SCREEN);
            return AjaxResult.success();
        }
        return AjaxResult.error("密码不正确，请重新输入。");
    }

    /**
     * 切换主题
     *
     * @return String
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @GetMapping("/system/switchSkin")
    public String switchSkin() {

        return "skin";
    }

    /**
     * 切换菜单
     *
     * @param style    String
     * @param response HttpServletResponse
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @GetMapping("/system/menuStyle/{style}")
    public void menuStyle(@PathVariable String style, HttpServletResponse response) {
        CookieUtils.setCookie(response, "nav-style", style);
    }

    /**
     * 系统介绍
     *
     * @param mmap ModelMap
     * @return String None
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    @GetMapping("/system/main")
    public String main(ModelMap mmap) {
        mmap.put("version", DrunkardConfig.getVersion());
        return "main";
    }

    /**
     * 检查初始密码是否提醒修改
     *
     * @param pwdUpdateDate Date
     * @return boolean true
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    public boolean initPasswordIsModify(Date pwdUpdateDate) {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    /**
     * 检查密码是否过期
     *
     * @param pwdUpdateDate Date
     * @return boolean true
     * @author <a href="https://github.com/rothschil">Sam</a>
     * @date 2021/10/12-16:56
     **/
    public boolean passwordIsExpiration(Date pwdUpdateDate) {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0) {
            if (StringUtils.isNull(pwdUpdateDate)) {
                // 如果从未修改过初始密码，直接提醒过期
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}

package webproject.controller.system;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.security.SecurityUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import webproject.controller.base.BaseController;
import webproject.mapper.MenuMapper;
import webproject.mapper.RoleMapper;
import webproject.mapper.UserMapper;
import webproject.model.system.MenuVo;
import webproject.model.system.Role;
import webproject.model.system.User;
import webproject.utils.MenuUtil;
import webproject.web.config.MyShiroRealm;

/**
 *  
 * 
 * @author hts
 * @version date：2017年4月11日 下午12:31:12 
 * 
 */
@Controller
public class mainController extends BaseController {
	@Autowired
	UserMapper usermapper;
	@Autowired
	MenuMapper menumapper;
	@Autowired
	RoleMapper rolemapper;
	public final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/index")
	public String index(Model model) {
		Session session = SecurityUtils.getSubject().getSession();
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		User user = new User();
		user.setAccount(username);
		// List<MenuVo> parentlist=usermapper.findparentmenu(user);
		// List<MenuVo> sonlist=usermapper.findsonmenu(user);
		List<MenuVo> list = usermapper.findmenus(user);
		for (MenuVo menua : list) {
			for (MenuVo menub : list) {
				if (menua.getParent().equals(menub.getMenuid())) {
					menub.getSonsList().add(menua);
				}
			}
		}
		//采用实体类导致与前端代码冲突，不好的写法
		if (!list.isEmpty()) {
			model.addAttribute("menuHead", list.get(0));
		}
		else model.addAttribute("menuHead", new MenuVo());
		
		//采用实体类导致与前端代码冲突，不好的写法
		Role role = usermapper.findRole(user);
		if(role==null) 
		role=new Role();
		model.addAttribute("role", role);
		
		return "index";
	}

	@RequestMapping("/register")
	public String register(Model model) {
		return "register";
	}

	@RequestMapping("/login")
	public String login(Model model) {
		
		return "login";
	}

	@RequestMapping("/logout")
	public String logout(Model model) {
		logger.info("用户" + SecurityUtils.getSubject().getPrincipal().toString() + "退出系统");
		SecurityUtils.getSubject().logout();
		return "login";
	}

	@RequestMapping("/doregister")
	@ResponseBody
	public Map<String, Object> doregister(Model model, String account, String password) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		User user = new User(null, account, password, null, null);
		if (usermapper.findsameaccount(user) == 0) {
			usermapper.insertUser(user);
			UsernamePasswordToken token = new UsernamePasswordToken(user.getAccount(), user.getPassword());
			SecurityUtils.getSubject().login(token);
			map.put("status", 200);
			map.put("massage", "登陆成功");
		} else {
			map.put("status", 500);
			map.put("message", "账号已被注册");
		}
		return map;
	}

	@RequestMapping("/dologin")
	@ResponseBody
	public Map<String, Object> dologin(Model model, String username, String password) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			SecurityUtils.getSubject().login(token);
			map.put("status", 200);
			map.put("massage", "登陆成功");
			logger.info("用户" + SecurityUtils.getSubject().getPrincipal().toString() + "登陆系统");
		} catch (ExcessiveAttemptsException eae) {

			map.put("status", 500);
			map.put("massage", "登陆次数过多，请稍后再试");
		} catch (AuthenticationException e) {
			map.put("status", 500);
			map.put("message", "账号或密码错误");

		}
		return map;
	}

	@RequestMapping("/menu")
	public String menu(Model model) {
		List list = menumapper.findAllMenus();
		MenuUtil.MenuProcess(list);
		model.addAttribute("menus", list);

		return "menumanage";

	}

	@RequestMapping("/authorize")
	public String authorize(Model model) {

		return "authorizeManage";

	}

}

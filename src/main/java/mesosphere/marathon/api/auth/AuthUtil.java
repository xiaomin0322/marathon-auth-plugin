package mesosphere.marathon.api.auth;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AuthUtil {

	private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);

	private static HashMap<String, User> userMap = getUserMap();
	
	public static final ThreadLocal<User> THREAD_LOCAL_USER = new ThreadLocal<User>();
	
	public static Map<String,String> queryUrlMap = new HashMap<String,String>();
	
	public static final String QUERY_GROUP_URI="/v2/groups?embed=group.groups&embed=group.apps&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.readiness";
	
	public static final String QUERY_QUEUE_URI="/v2/queue";
	
	public static final String QUERY_DEPLOYMENTS_URI="/v2/deployments";
	
	public static final String HTTP_GET = "get";
	public static final String HTTP_POST = "post";
	public static final String HTTP_PUT = "put";
	public static final String HTTP_DELETE = "delete";
	
	
	static
	{
		queryUrlMap.put(QUERY_GROUP_URI, HTTP_GET);
		queryUrlMap.put(QUERY_QUEUE_URI, HTTP_GET);
		queryUrlMap.put(QUERY_DEPLOYMENTS_URI, HTTP_GET);
	}
	
	
	/**   
     * 循环向上转型, 获取对象的 DeclaredField   
     * @param object : 子类对象   
     * @param fieldName : 父类中的属性名   
     * @return 父类中的属性对象   
     */          
    public static Field getDeclaredField(Object object, String fieldName){    
        Field field = null ;              
        Class<?> clazz = object.getClass() ;              
        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {    
            try {    
                field = clazz.getDeclaredField(fieldName) ;    
                return field ;    
            } catch (Exception e) {    
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。    
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了                      
            }     
        }          
        return null;    
    } 
	
	   
    public static Object getFieldVal(Object o,String fieldName){
		try {
			Field field = getDeclaredField(o, fieldName);
			field.setAccessible(true); 
	        return field.get(o); 
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return null;
    }

	public static HashMap<String, User> getUserMap() {
		log.info("init getUserMap >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		try {
			HashMap<String, User> userMap = new HashMap<String, User>();
			Gson gson = new Gson();
			
			String authConfPath = System.getProperty("marathon.auth.conf");

			log.info("marathon.auth.conf : {}",authConfPath);
			
			List<User> users = gson.fromJson(FileUtils
					.readFileToString(new File(authConfPath)),
					new TypeToken<List<User>>() {
					}.getType());

			for (User o : users) {
				userMap.put(o.getUser(), o);
			}
			log.info("init getUserMap ok {}",userMap);
			return userMap;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return null;
	}
	
	public static boolean getAuthFlag(){
		return MapUtils.isEmpty(userMap);
	}

	public static void main(String[] args) throws Exception {

		getUserMap();

	}

	/**
	 * 验证用户名密码
	 * 
	 * @param userName
	 * @param passWrod
	 * @return
	 */
	public static boolean checkUser(String userName, String passWrod) {
		if (!userMap.containsKey(userName)) {
			return false;
		}
		User user = userMap.get(userName);
		return user.getPassword().equals(passWrod);
	}

	public static String getAllowedName(String methodName) {
		if (HTTP_PUT.equalsIgnoreCase(methodName)) {
			return "create";
		} else if (HTTP_POST.equalsIgnoreCase(methodName)) {
			return "update";
		} else if (HTTP_DELETE.equalsIgnoreCase(methodName)) {
			return "delete";
		} else {
			return "view";
		}
	}
	
	public static Permission getQueryPermission(User user){
		if (user.getPermissions() != null) {
			for (Permission permission : user.getPermissions()) {
				// 操作权限动作
				String allowed = permission.getAllowed();
				// 判断操作
				if ("view".equalsIgnoreCase(allowed)) {
					return permission;
				}
			}
		}
		return null;
	}

	/**
	 * 权限验证
	 * 
	 * @param request
	 * @param userName
	 * @return
	 */
	public static boolean checkPermission(HttpServletRequest request,
			String userName) {
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		String path = uri + (StringUtils.isNotBlank(queryString) ? ("?" + queryString) : "");
		
		log.error("method : {} path:{}",method,path);

		String allowedName = getAllowedName(method);

		User user = userMap.get(userName);
		if (user == null) {
			return false;
		}
		
		log.error("user : {}",user);
		
		THREAD_LOCAL_USER.set(user);
		
        if(queryUrlMap.containsKey(path)){
        	return true;
        }

		if (user.getPermissions() != null) {
			for (Permission permission : user.getPermissions()) {
				// 操作路径
				String on = permission.getOn();
				// 权限
				String allowed = permission.getAllowed();
				// 判断操作
				if (allowedName.equalsIgnoreCase(allowed)) {
					// 判断是都有权限
					if (path.contains(on)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * 认证
	 * @param request
	 * @return
	 */
	public static String  checkAuth(HttpServletRequest request) {
		String userName = null;
		try {
			String header = request.getHeader("Authorization");
			if (header != null && header.startsWith("Basic ")) {
				String encoded = header.replaceFirst("Basic ", "");
				String decoded = new String(
						Base64.getDecoder().decode(encoded), "UTF-8");
				//log.error("Authorization Base64.getDecoder {}",decoded);
				String[] userPass = decoded.split(":", 2);
				if (userPass.length == 2) {
					boolean checkpwd = checkUser(userPass[0], userPass[1]);
					if(checkpwd){
						return userPass[0];
					}
				} 
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return userName;
	}

}

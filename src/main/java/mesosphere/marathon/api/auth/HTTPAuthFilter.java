package mesosphere.marathon.api.auth;
import java.io.IOException;
import java.util.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class HTTPAuthFilter implements Filter {
	
	 private static final Logger log = LoggerFactory
				.getLogger(HTTPAuthFilter.class);

	public boolean doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String userName = AuthUtil.checkAuth(request);
			if(StringUtils.isBlank(userName)){
				response.setStatus(401);
				response.setHeader("Cache-Control", "no-store");
				response.setDateHeader("Expires", 0);
				response.setHeader("WWW-authenticate", "Basic Realm=\"pwd\"");
				return false;
			}else if(!AuthUtil.checkPermission(request, userName)){
				response.setStatus(403);
		        response.getWriter().println("{\"problem\": \"Not Authorized to perform this action!\"}");
				return false;
			}		
			return true;
	}
	

	@SuppressWarnings("unused")
	private boolean checkHeaderAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 try {
				String header = request.getHeader("Authorization");
				log.info("request.getHeader(Authorization) " + header);
				if (header != null && header.startsWith("Basic ")) {
					String encoded = header.replaceFirst("Basic ", "");
					String decoded = new String(
							Base64.getDecoder().decode(encoded), "UTF-8");
					String[] userPass = decoded.split(":", 2);
					if (userPass.length == 2) {
						if("admin".equals(userPass[0]) && "admin".equals(userPass[1])){
							return true;
						}
					}else{
						return false;
					}
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(),ex);
			}
		 return false;

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	
	public byte[] filterGroups(HttpServletRequest request, byte[] content) {
		byte[] newContent = content;
		if (ArrayUtils.isEmpty(newContent)) {
			return newContent;
		}
		try {
			String method = request.getMethod();
			String uri = request.getRequestURI();
			String queryString = request.getQueryString();
			String path = uri + "?" + queryString;
			String url = AuthUtil.QUERY_GROUP_URI;
			if (AuthUtil.HTTP_GET.equalsIgnoreCase(method) && path.equalsIgnoreCase(url)) {
				String umContent = MessageGZIP.uncompressToString(newContent);
				

				User user = AuthUtil.THREAD_LOCAL_USER.get();
				if (user == null) {
					log.warn("AuthUtil.THREAD_LOCAL_USER.get()");
					return null;
				}

				JsonObject jsonObject = new JsonParser().parse(umContent)
						.getAsJsonObject();

				// 新的jsonArrayGroups
				JsonArray newJsonArrayGroups = new JsonArray();

				JsonArray jsonArrayGroups = jsonObject.getAsJsonArray("groups");
				
				log.info("jsonArrayGroups.size >>>>>{}", jsonArrayGroups.size());

				if (!jsonArrayGroups.isJsonNull()) {
					for (JsonElement jsonElement : jsonArrayGroups) {
						JsonObject jsonObjectGroup = jsonElement
								.getAsJsonObject();
						String id = jsonObjectGroup.get("id").getAsString();
						Permission permission = AuthUtil
								.getQueryPermission(user);
						log.info(" path >>>>>>  {} permission >>>> {}", id,permission);
						if (permission != null) {
							String queryPath = permission.getOn();
							// 判断分组查询权限
							if ("/".equals(queryPath) || queryPath.contains(id)) {
								newJsonArrayGroups.add(jsonElement);
							}
						}
					}
				}

				jsonObject.remove("groups");
				jsonObject.add("groups", newJsonArrayGroups);

				String newContentStr = jsonObject.toString();

				newContent = MessageGZIP.compressToByte(newContentStr);

				log.info("newJsonArrayGroups.size >>>>>{}", newJsonArrayGroups.size());

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return newContent;
	}
	
	

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		if(AuthUtil.getAuthFlag()){
			 arg2.doFilter(arg0, arg1);
			 return ;
		}
		
		try{
			 HttpServletRequest request  = (HttpServletRequest) arg0;
		    WrapperResponse wrapper = new WrapperResponse((HttpServletResponse)arg1);
			boolean flag =  doGet(request, wrapper);
			log.info("HTTPAuthFilter uri {} doGet flag : {}",request.getRequestURI(),flag);
			if(flag){
				    arg2.doFilter(arg0, wrapper);
				    byte[] content = wrapper.getContent();
				    content = filterGroups(request, content);
                    if(ArrayUtils.isNotEmpty(content)){
                        ServletOutputStream out = arg1.getOutputStream();
     			        out.write(content);
     			        out.flush();
     			        out.close();
                    }
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}finally{
			AuthUtil.THREAD_LOCAL_USER.remove();
		}
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
package mesosphere.marathon.api.auth;

import java.util.ArrayList;
import java.util.List;

public class User {
  
	private String user;
	private String password;
	private List<Permission> permissions = new ArrayList<Permission>();
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<Permission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
	@Override
	public String toString() {
		return "User [user=" + user + ", password=" + password
				+ ", permissions=" + permissions + "]";
	}
	
	
}

package mesosphere.marathon.api.auth;

public class Permission {

	 //增刪該查
	 private String allowed;
	 //對應組名
	 private String on;
	public String getAllowed() {
		return allowed;
	}
	public void setAllowed(String allowed) {
		this.allowed = allowed;
	}
	public String getOn() {
		return on;
	}
	public void setOn(String on) {
		this.on = on;
	}
	@Override
	public String toString() {
		return "Permission [allowed=" + allowed + ", on=" + on + "]";
	}
	 
	 
}

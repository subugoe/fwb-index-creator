package sub.fwb.web;

public class Environment {

	public String getVariable(String name) {
		return System.getenv(name);
	}

}

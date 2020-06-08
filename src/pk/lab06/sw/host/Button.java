package pk.lab06.sw.host;


public class Button {
	private String description;
	private String command;
	
	public Button() {
		description = "empty";
		command = "";
	}
	
	public void setDescription(String desc) {
		if (desc.length() > 14){
			desc = desc.substring(0, 13);
		}
		this.description = desc;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void set(String desc, String command) {
		if (desc.length() > 14){
			desc = desc.substring(0, 13);
		}
		this.description = desc;
		this.command = command;
	}
	
	public String getDescription() {
		return this.description;
	}
	public String getCommand() {
		return this.command;
	}
	
}

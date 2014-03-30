package sympa.extractor;

public class ListDescription {
	String creationDate;
	String lastMessageDate;
	String name;
	String description;
	
	public String tsvDescription() {
		return name + "\t" + description + "\t" + creationDate + "\t" + lastMessageDate;
	}
}

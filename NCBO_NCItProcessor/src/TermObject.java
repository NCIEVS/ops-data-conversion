
public class TermObject {

	private String name;
	private String startTag;
	private String endTag;
	
	public TermObject(String inName, String inStartTag, String inEndTag){
		name = inName;
		startTag = inStartTag;
		endTag = inEndTag;
	}
	
	public String getName(){
		return name;
	}
	
	public String  getStartTag(){
		return startTag;
	}
	
	public String getEndTag(){
		return endTag;
	}
	
	
}

package gov.nih.nci.evs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class Vocabulary {

	private String Name;
	private String stringDate;
	private java.util.Date realDate;
	private boolean needsUpdate = false;
	
	private final static Logger LOGGER = Logger
			.getLogger(Vocabulary.class.getName());
	
	public Vocabulary(String name, String date){
		this.Name=name;
		this.stringDate=date;
		convertStringdateToDate();
	}
	
	private void convertStringdateToDate(){
		//12-Sep-2013 23:02
		try {
			this.realDate = convertStringDateToDate(stringDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			LOGGER.severe("Unable to parse date from vocabulary properties file");
			e.printStackTrace();
		}
	}
	
	public static java.util.Date convertStringDateToDate(String inStringDate) throws ParseException{
		java.util.Date tmpDate = new SimpleDateFormat("dd-MMM-yyyy hh:mm", Locale.ENGLISH).parse(inStringDate.trim());
		return tmpDate;
	}
	
	public String getName(){
		return this.Name;
	}
	
	public String getStringDate() {
		return stringDate;
	}
	
	public java.util.Date getRealDate() {
		return realDate;
	}

	public boolean getNeedsUpdate() {
		return needsUpdate;
	}

	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}
	
	public boolean isAfterDate(Date newDate){
		if(newDate.after(this.realDate)){
			return true;
		}
		return false;
	}
}

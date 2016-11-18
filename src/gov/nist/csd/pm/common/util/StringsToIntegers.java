package gov.nist.csd.pm.common.util;

public class StringsToIntegers {
	public int id;

	public static boolean TryParseInt(int i, String theReturnValue ) {
			theReturnValue = Integer.toString(i);
		return (null != theReturnValue);
	}
	
	public static Integer ParseString2IntSafely(String i, Integer defaultValue) {
		Integer theReturnValue=null;
		if(!TryParseString2Int(i, theReturnValue)){
			theReturnValue = defaultValue;
		}
		return theReturnValue;
	}
	
	
	public static int ParseString2IntSafely(String i) {
		Integer defaultValue = 0;
		return ParseString2IntSafely(i, defaultValue);
	}
	
	public static boolean TryParseString2Int (String theInt, Integer theReturnValue) {
		try{
			theReturnValue = Integer.parseInt(theInt);
		}catch (Exception ex){
			// TODO: do something
		}
		return (null != theReturnValue);
	}
	

}
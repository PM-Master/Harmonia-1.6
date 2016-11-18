package gov.nist.csd.pm.common.constants;

import java.util.ArrayList;

public class MySQL_Parameters {
	private ArrayList<String[]> params;
	private ParamType outParam;
	
	public MySQL_Parameters(){
		params = new ArrayList<String[]>();
		outParam = null;
	}
	
	public ArrayList<String[]> getParams(){
		return params;
	}
	
	public void setOutParamType(ParamType p){
		outParam = p;
	}
	
	public ParamType getOutParamType(){
		return outParam;
	}
	
	public void addParam(ParamType p, Object value){
        String sV = String.valueOf(value);
		try{
			int iV = Integer.valueOf(sV);
		}catch(NumberFormatException ignored){

		}

		params.add(new String[]{p.getName(), value == null ? null :String.valueOf(value)});
	}
	
	public void clearParams(){
		params.clear();
		outParam = null;
	}

	public String toString(){
		String ret = "";
		for(int i = 0; i < params.size(); i++){
			ret += params.get(i)[0] + ", " + params.get(i)[1] + "\n";
		}
		return ret;
	}
}

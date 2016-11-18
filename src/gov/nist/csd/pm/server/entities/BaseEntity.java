package gov.nist.csd.pm.server.entities;

import gov.nist.csd.pm.common.util.StringsToIntegers;

public class BaseEntity {

	String description=null;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	String name=null;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	int id=0;
	
	/// Just a default constructor for Java to keep quiet
	public BaseEntity(){}
	
	public BaseEntity(String id, String name){
		this.id = StringsToIntegers.ParseString2IntSafely(id); // TODO: verify that conversion works whenever it's time to verify it.
		this.name = name;
	}
	
	public BaseEntity(int id, String name){
		this.id = id;
		this.name = name;
	}

}

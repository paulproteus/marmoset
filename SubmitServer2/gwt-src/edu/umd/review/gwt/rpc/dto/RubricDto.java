package edu.umd.review.gwt.rpc.dto;

import java.util.LinkedHashMap;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

import edu.umd.cs.marmoset.modelClasses.Rubric;

public abstract class RubricDto implements IsSerializable, Comparable<RubricDto> {

	public enum Presentation {
		NUMERIC,
		DROPDOWN,
		CHECKBOX,
		;
	}

	private @Rubric.PK
	int id;
	private String name;
	private LinkedHashMap<String,Integer> data;
	private String description;

	@Deprecated
	public RubricDto() {
		this(0, "0xdeadbeef", null, null);
	}

	public RubricDto(@Rubric.PK int id, String name, String description, LinkedHashMap<String,Integer> data) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.data = data;
	}

	public @Rubric.PK
	int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	   public String getDescription() {
	        return description;
	    }

	public LinkedHashMap<String,Integer> getData() {
	  return data;
  }

	@Override
	public int hashCode() {
		final int prime = 31;
	 	int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
  @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RubricDto other = (RubricDto) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public  boolean isValidValue(String value) {
		return data.containsKey(value);
	}

	public void assertIsValidValue(String value) {
		Preconditions.checkArgument(isValidValue(value), "%s is invalid value", value);
	}

	public int getPointsForValue(String value) {
		return data.get(value);
	}


	@Override
	public int compareTo( RubricDto that) {
		return this.id - that.id;
	}


	public  String getDefaultValue() {
		return data.keySet().iterator().next();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", getId())
				.add("name", getName())
				.toString();
	}

	public abstract String getValidationMessage(String invalidValue);
}

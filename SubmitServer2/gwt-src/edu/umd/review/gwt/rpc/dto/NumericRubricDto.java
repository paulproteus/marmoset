package edu.umd.review.gwt.rpc.dto;

import java.util.Collection;
import java.util.LinkedHashMap;

import edu.umd.cs.marmoset.modelClasses.Rubric;

public class NumericRubricDto extends RubricDto {

	private static final String MAX = "max";
	private static final String MIN = "min";

	@Deprecated @SuppressWarnings("unused")
	private NumericRubricDto() {
		super();
	}

	public NumericRubricDto(@Rubric.PK int id, String name, String description, LinkedHashMap<String,Integer> data) {
		super(id, name, description, data);
	}

	@Override
	public boolean isValidValue(String value) {
		try {
		  getPointsForValue(value);
		  return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int getPointsForValue(String value) {
	    if (value.isEmpty()) {
	        if (getData().isEmpty()) 
	            return 0;
	        throw new IllegalArgumentException("can't have an empty value");
	    }
	    try {
            int v = Integer.parseInt(value);
            LinkedHashMap<String, Integer> data = getData();
            if (data.containsKey(MIN) && data.get(MIN) > v)
                throw new IllegalArgumentException(value);
            if (data.containsKey(MAX) && data.get(MAX) < v)
                throw new IllegalArgumentException(value);
            return v;
        } catch (Exception e) {
             throw new IllegalArgumentException(value);
        }

	}

	@Override
    public String getDefaultValue() {
        Collection<Integer> values = getData().values();
        if (values.isEmpty())
            return "";
        return Integer.toString(values.iterator().next());
    }

	@Override
	public String getValidationMessage(String invalidValue) {
	  return "The score " + invalidValue + " is not in the range [" + getData().get("min") +
	  		"," + getData().get("max") + "]";
	}
}

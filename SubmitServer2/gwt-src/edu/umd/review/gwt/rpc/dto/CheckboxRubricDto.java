package edu.umd.review.gwt.rpc.dto;

import java.util.LinkedHashMap;

import edu.umd.cs.marmoset.modelClasses.Rubric;

public class CheckboxRubricDto extends RubricDto {

	@SuppressWarnings("unused")
  @Deprecated
	private CheckboxRubricDto() {
		super();
	}

	public CheckboxRubricDto(@Rubric.PK int id, String name, String description, LinkedHashMap<String, Integer> data) {
		super(id, name, description, data);
	}

	@Override
	public String getValidationMessage(String invalidValue) {
		throw new UnsupportedOperationException();
	}

}

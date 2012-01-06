package edu.umd.review.gwt.rpc.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import edu.umd.cs.marmoset.modelClasses.Rubric;

public class DropdownRubricDto extends RubricDto {

	@Deprecated
	@SuppressWarnings("unused")
	private DropdownRubricDto() {
		super();
	}

	public DropdownRubricDto(@Rubric.PK int id, String name, String description,  LinkedHashMap<String, Integer> data) {
		super(id, name, description, data);
	}

	public List<String> getDropdownChoices() {
		List<String> choices = Lists.newArrayList();
		for (Entry<String, Integer> entry : getData().entrySet()) {
			choices.add(entry.getKey());
		}
		return choices;
	}

	@Override
	public String getValidationMessage(String invalidValue) {
		throw new UnsupportedOperationException();
	}
}

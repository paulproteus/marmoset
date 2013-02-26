package edu.umd.review.common.action;

import net.customware.gwt.dispatch.shared.Action;

public class RateReviewerAction implements Action<VoidResult> {
	private int rating;
	private String reviewerName;

	private RateReviewerAction() {
	}

	public RateReviewerAction(String reviewerName, int rating) {
		this.reviewerName = reviewerName;
		this.rating = rating;
	}

	public int getRating() {
		return rating;
	}

	public String getReviewerName() {
		return reviewerName;
	}

}

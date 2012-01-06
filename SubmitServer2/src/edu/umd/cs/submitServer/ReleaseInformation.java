/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/*
 * Created on Sep 7, 2004
 *
 */
package edu.umd.cs.submitServer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * Given a project and a set of submissions that have requested release testing,
 * this class figures out how many tokens are remaining and the regeneration
 * schedule.
 * 
 * @author jspacco
 */
public class ReleaseInformation {
	private int releaseTokens;
	private int regenerationTime;
	private int tokensRemaining;
	private int tokensUsed = 0;
	private List<Timestamp> regenerationSchedule = new ArrayList<Timestamp>();
	private static final long MILLIS_PER_HOUR = 60 * 60 * 1000L;
	private boolean releaseRequestOK = false;
	private boolean isAfterPublic = false;

	// TODO make this the only constructor
	public ReleaseInformation(Project project, List<Submission> submissionList) {
		try {
			// fetch tokens and regeneration time from the project record
			releaseTokens = project.getReleaseTokens();
			regenerationTime = project.getRegenerationTime();

			isAfterPublic = Project.AFTER_PUBLIC.equals(project
					.getReleasePolicy());

			// create Timestamp for the regeneration cutoff
			long nowMillis = System.currentTimeMillis();
			Timestamp then = new Timestamp(nowMillis
					- (regenerationTime * MILLIS_PER_HOUR));

			tokensUsed = 0;
			for (Submission submission : submissionList) {

				Timestamp requestTime = submission.getReleaseRequest();
				// the SubmissionCollection should be fetched with
				// SubmissionCollection.lookupAllForReleaseTesting()
				// which only includes submissions where the release_request
				// column is NOT NULL
				if (requestTime == null)
					continue;

				if (requestTime.after(then)) {
					tokensUsed++;
					// Debug.print("requstTime is after then, so tokensUsed is now: "
					// +tokensUsed);
					regenerationSchedule.add(new Timestamp(requestTime
							.getTime() + regenerationTime * MILLIS_PER_HOUR));
				}
			}
			// compute remaining tokens
			tokensRemaining = releaseTokens - tokensUsed;
			if (tokensUsed > releaseTokens) {
				Debug.error("Used " + tokensUsed + " when only allowed "
						+ releaseTokens);
				throw new IllegalStateException("Used " + tokensUsed
						+ " when only should be allowed " + releaseTokens);
			}
			// if we can have at least one token remaining, set releaseRequestOK
			// to true
			if (tokensRemaining > 0)
				releaseRequestOK = true;
		} catch (NumberFormatException e) {
			String msg = "Corrupted data in the project table, I can't parse release_tokens into an int: "
					+ e;
			Debug.error(msg);
			e.printStackTrace();
			// re-throw exception
			throw e;
		}
	}

	/**
	 * @return Returns the regenerationSchedule.
	 */
	public List<Timestamp> getRegenerationSchedule() {
		return regenerationSchedule;
	}

	/**
	 * @param regenerationSchedule
	 *            The regenerationSchedule to set.
	 */
	public void setRegenerationSchedule(List<Timestamp> regenerationSchedule) {
		this.regenerationSchedule = regenerationSchedule;
	}

	/**
	 * @return Returns the regenerationTime.
	 */
	public int getRegenerationTime() {
		return regenerationTime;
	}

	/**
	 * @param regenerationTime
	 *            The regenerationTime to set.
	 */
	public void setRegenerationTime(int regenerationTime) {
		this.regenerationTime = regenerationTime;
	}

	/**
	 * @return Returns the releaseRequestOK.
	 */
	public boolean isReleaseRequestOK() {
		return releaseRequestOK;
	}

	/**
	 * @param releaseRequestOK
	 *            The releaseRequestOK to set.
	 */
	public void setReleaseRequestOK(boolean releaseRequestOK) {
		this.releaseRequestOK = releaseRequestOK;
	}

	/**
	 * @return Returns the releaseTokens.
	 */
	public int getReleaseTokens() {
		return releaseTokens;
	}

	/**
	 * @param releaseTokens
	 *            The releaseTokens to set.
	 */
	public void setReleaseTokens(int releaseTokens) {
		this.releaseTokens = releaseTokens;
	}

	/**
	 * @return Returns the tokensRemaining.
	 */
	public int getTokensRemaining() {
		return tokensRemaining;
	}

	/**
	 * @param tokensRemaining
	 *            The tokensRemaining to set.
	 */
	public void setTokensRemaining(int tokensRemaining) {
		this.tokensRemaining = tokensRemaining;
	}

	/**
	 * @return Returns whether this project's release-testing policy is
	 *         "after public"
	 */
	public boolean isAfterPublic() {
		return isAfterPublic;
	}

	/**
	 * @param isAfterPublic
	 *            Set whether this project's release-testing policy is
	 *            "after public"
	 */
	public void setAfterPublic(boolean isAfterPublic) {
		this.isAfterPublic = isAfterPublic;
	}

	/**
	 * @return Returns the tokensUsed.
	 */
	public int getTokensUsed() {
		return tokensUsed;
	}

	/**
	 * @param tokensUsed
	 *            The tokensUsed to set.
	 */
	public void setTokensUsed(int tokensUsed) {
		this.tokensUsed = tokensUsed;
	}
}

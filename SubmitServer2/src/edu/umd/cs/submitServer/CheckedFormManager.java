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
 * Created on Dec 8, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import edu.umd.cs.marmoset.modelClasses.BackgroundData;

/**
 * @author jspacco
 *
 */
/**
 * @author jspacco
 * 
 */
public class CheckedFormManager {
	private BackgroundData backgroundData;

	public CheckedFormManager(BackgroundData backgroundData) {
		this.backgroundData = backgroundData;
	}

	/**
	 * void constructor to make this a bean
	 */
	public CheckedFormManager() {
	}

	private static final String CHECKED = "checked";

	private static String nonNullOrEqual(String field, String key) {
		if (field != null && field.equals(key))
			return CHECKED;
		return "";
	}

	public String getGenderMale() {
		return nonNullOrEqual(backgroundData.getGender(), "male");
	}

	public String getGenderFemale() {
		return nonNullOrEqual(backgroundData.getGender(), "female");
	}

	public String getGenderNA() {
		return nonNullOrEqual(backgroundData.getGender(), "na");
	}

	public String getMajorCS() {
		return nonNullOrEqual(backgroundData.getMajor(), "CS");
	}

	public String getMajorCE() {
		return nonNullOrEqual(backgroundData.getMajor(), "CE");
	}

	public String getMajorMath() {
		return nonNullOrEqual(backgroundData.getMajor(), "Math");
	}

	public String getMajorOther() {
		return nonNullOrEqual(backgroundData.getMajor(), "Other");
	}

	public String getMajorNA() {
		return nonNullOrEqual(backgroundData.getMajor(), "na");
	}

	public String getEthnicRacialAssociationNA() {
		return nonNullOrEqual(backgroundData.getEthnicRacialAssociation(), "na");
	}

	private static String isNonNull(String field) {
		if (field != null)
			return CHECKED;
		return "";
	}

	public String getAmericanIndian() {
		return isNonNull(backgroundData.getAmericanIndian());
	}

	public String getAsian() {
		return isNonNull(backgroundData.getAsian());
	}

	public String getBlack() {
		return isNonNull(backgroundData.getBlack());
	}

	public String getCaucasian() {
		return isNonNull(backgroundData.getCaucasian());
	}

	public String getLatinoLatina() {
		return isNonNull(backgroundData.getLatinoLatina());
	}

	public String getAge18To23() {
		return nonNullOrEqual(backgroundData.getAge(), "18-23");
	}

	public String getAge23To29() {
		return nonNullOrEqual(backgroundData.getAge(), "23-29");
	}

	public String getAge30Plus() {
		return nonNullOrEqual(backgroundData.getAge(), "30+");
	}

	public String getAgeNA() {
		return nonNullOrEqual(backgroundData.getAge(), "na");
	}

	public String getPriorProgrammingNone() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				"none");
	}

	public String getPriorProgrammingNA() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				"na");
	}

	public String getPriorProgrammingCommunityCollege() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				BackgroundData.COMMUNITY_COLLEGE);
	}

	public String getInstitutionCommunityCollege() {
		if (!getPriorProgrammingCommunityCollege().equals(""))
			return backgroundData.getOtherInstitution();
		return "";
	}

	public String getPriorProgrammingOtherUMInstitition() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				BackgroundData.OTHER_UM_INSTITUTION);
	}

	public String getInstititutionOtherUMInstitition() {
		if (!getPriorProgrammingOtherUMInstitition().equals(""))
			return backgroundData.getOtherInstitution();
		return "";
	}

	public String getPriorProgrammingOtherNonUMInstitition() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				BackgroundData.OTHER_NON_UM_INSTITUTION);
	}

	public String getInstitutionOtherNonUMInstitution() {
		if (!getPriorProgrammingOtherNonUMInstitition().equals(""))
			return backgroundData.getOtherInstitution();
		return "";
	}

	public String getPriorProgrammingHighSchoolAP() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				BackgroundData.HIGH_SCHOOL_AP_COURSE);
	}

	public String getPriorProgrammingOtherHighSchool() {
		return nonNullOrEqual(backgroundData.getPriorProgrammingExperience(),
				BackgroundData.OTHER_HIGH_SCHOOL_COURSE);
	}

	private static String getFieldOrEmptyString(String field) {
		if (field == null)
			return "";
		return field;
	}

	// XXX I cannot capitalize this as "getAExamScore" because jsp EL doesn't
	// recognize
	// aExamScore and I don't really care enough to figure out why
	public String getAexamScore() {
		if (!getPriorProgrammingHighSchoolAP().equals(""))
			return getFieldOrEmptyString(backgroundData.getAExamScore());
		return "";
	}

	public String getAbExamScore() {
		if (!getPriorProgrammingHighSchoolAP().equals(""))
			return getFieldOrEmptyString(backgroundData.getAbExamScore());
		return "";
	}

	public String getPlacementExam131() {
		return nonNullOrEqual(backgroundData.getPlacementExam(), "cmsc131");
	}

	public String getPlacementExam132() {
		return nonNullOrEqual(backgroundData.getPlacementExam(), "cmsc132");
	}

	public String getPlacementExam212() {
		return nonNullOrEqual(backgroundData.getPlacementExam(), "cmsc212");
	}

	public String getPlacementExamNA() {
		return nonNullOrEqual(backgroundData.getPlacementExam(), "na");
	}

	public String getPlacementExamNone() {
		return nonNullOrEqual(backgroundData.getPlacementExam(), "none");
	}

	public String getPlacementExamResultPassed() {
		return nonNullOrEqual(backgroundData.getPlacementExamResult(), "passed");
	}

	public String getPlacementExamResultFailed() {
		return nonNullOrEqual(backgroundData.getPlacementExamResult(), "failed");
	}

	public String getPlacementExamResultMarginallyPassed() {
		return nonNullOrEqual(backgroundData.getPlacementExamResult(),
				"marginally passed");
	}

	public boolean isComplete() {
		return backgroundData.isComplete();
	}

	public String getHighSchoolSelector() {
		String select = "<select name=highSchoolCountry>"
				+ "	<option></option>\n"
				+ "	<option value=na>Prefer not to answer</option>\n"
				+ "	<option value=us_en_home>United States </option>\n"
				+ "	<option value=al_en_home_none>Albania </option>\n"
				+ "	<option value=dz_en_home_none>Algeria </option>\n"
				+ "	<option value=as_en_home_none>American Samoa </option>\n"
				+ "	<option value=ao_en_home_none>Angola </option>\n"
				+ "	<option value=ai_en_home>Anguilla </option>\n"
				+ "	<option value=ag_en_home>Antigua </option>\n"
				+ "	<option value=ar_es_home>Argentina </option>\n"
				+ "	<option value=am_en_home_none>Armenia </option>\n"
				+ "	<option value=aw_en_home>Aruba </option>\n"
				+ "	<option value=au_en_home>Australia </option>\n"
				+ "	<option value=at_de_home>Austria </option>\n"
				+ "	<option value=az_en_home_none>Azerbaijan </option>\n"
				+ "	<option value=bs_en_home>Bahamas </option>\n"
				+ "	<option value=bh_en_home>Bahrain </option>\n"
				+ "	<option value=bd_en_home_none>Bangladesh </option>\n"
				+ "	<option value=bb_en_home>Barbados </option>\n"
				+ "	<option value=by_en_home_none>Belarus </option>\n"
				+ "	<option value=be_en_home>Belgium </option>\n"
				+ "	<option value=bz_en_home_none>Belize </option>\n"
				+ "	<option value=bj_en_home_none>Benin </option>\n"
				+ "	<option value=bm_en_home>Bermuda </option>\n"
				+ "	<option value=bt_en_home_none>Bhutan </option>\n"
				+ "	<option value=bo_en_home>Bolivia </option>\n"
				+ "	<option value=bl_en_home_none>Bonaire </option>\n"
				+ "	<option value=ba_en_home_none>Bosnia Herzegovina </option>\n"
				+ "	<option value=bw_en_home_none>Botswana </option>\n"
				+ "	<option value=br_pt_home>Brazil </option>\n"
				+ "	<option value=vg_en_home>British Virgin Islands </option>\n"
				+ "	<option value=bn_en_home_none>Brunei </option>\n"
				+ "	<option value=bg_en_home>Bulgaria </option>\n"
				+ "	<option value=bf_en_home_none>Burkina Faso </option>\n"
				+ "	<option value=bi_en_home_none>Burundi </option>\n"
				+ "	<option value=kh_en_home_none>Cambodia </option>\n"
				+ "	<option value=cm_en_home_none>Cameroon </option>\n"
				+ "	<option value=ca_en_home>Canada </option>\n"
				+ "	<option value=cv_en_home_none>Cape Verde </option>\n"
				+ "	<option value=ky_en_home>Cayman Islands </option>\n"
				+ "	<option value=td_en_home_none>Chad </option>\n"
				+ "	<option value=cl_es_home>Chile </option>\n"
				+ "	<option value=cn_zh_home>China </option>\n"
				+ "	<option value=co_en_home>Colombia </option>\n"
				+ "	<option value=cg_en_home_none>Congo </option>\n"
				+ "	<option value=ck_en_home_none>Cook Islands </option>\n"
				+ "	<option value=cr_es_home>Costa Rica </option>\n"
				+ "	<option value=hr_en_home>Croatia </option>\n"
				+ "	<option value=cb_en_home>Curacao </option>\n"
				+ "	<option value=cy_en_home>Cyprus </option>\n"
				+ "	<option value=cz_en_home>Czech Republic </option>\n"
				+ "	<option value=dk_da_home>Denmark </option>\n"
				+ "	<option value=dj_en_home_none>Djibouti </option>\n"
				+ "	<option value=dm_en_home>Dominica </option>\n"
				+ "	<option value=do_es_home>Dominican Republic </option>\n"
				+ "	<option value=ec_en_home>Ecuador </option>\n"
				+ "	<option value=eg_en_home_none>Egypt </option>\n"
				+ "	<option value=sv_en_home>El Salvador </option>\n"
				+ "	<option value=ee_en_home>Estonia </option>\n"
				+ "	<option value=et_en_home_none>Ethiopia </option>\n"
				+ "	<option value=fj_en_home_none>Fiji </option>\n"
				+ "	<option value=fi_en_home>Finland </option>\n"
				+ "	<option value=fr_fr_home>France </option>\n"
				+ "	<option value=gf_en_home_none>French Guiana </option>\n"
				+ "	<option value=pf_en_home_none>French Polynesia </option>\n"
				+ "	<option value=ga_en_home_none>Gabon </option>\n"
				+ "	<option value=gm_en_home_none>Gambia </option>\n"
				+ "	<option value=ge_en_home_none>Georgia </option>\n"
				+ "<option value=de_de_home>Germany </option>\n"
				+ "<option value=gh_en_home>Ghana </option>\n"
				+ "<option value=gi_en_home_none>Gibraltar </option>\n"
				+ "	<option value=gr_en_home>Greece </option>\n"
				+ "	<option value=gd_en_home>Grenada </option>\n"
				+ "	<option value=gp_en_home>Guadeloupe </option>\n"
				+ "	<option value=gu_en_home_none>Guam </option>\n"
				+ "	<option value=gt_es_home>Guatemala </option>\n"
				+ "	<option value=gn_en_home_none>Guinea </option>\n"
				+ "	<option value=gw_en_home_none>Guinea Bissau </option>\n"
				+ "	<option value=gy_en_home>Guyana </option>\n"
				+ "	<option value=ht_en_home>Haiti </option>\n"
				+ "	<option value=hn_en_home>Honduras </option>\n"
				+ "	<option value=hk_zh_home>Hong Kong </option>\n"
				+ "	<option value=hu_en_home>Hungary </option>\n"
				+ "	<option value=is_en_home_none>Iceland </option>\n"
				+ "	<option value=in_en_home>India </option>\n"
				+ "	<option value=id_en_home>Indonesia </option>\n"
				+ "	<option value=ie_en_home>Ireland (Republic of) </option>\n"
				+ "	<option value=il_en_home>Israel </option>\n"
				+ "	<option value=it_it_home>Italy </option>\n"
				+ "	<option value=ci_en_home>Ivory Coast </option>\n"
				+ "	<option value=jm_en_home>Jamaica </option>\n"
				+ "	<option value=jp_ja_home>Japan </option>\n"
				+ "	<option value=jo_en_home_none>Jordan </option>\n"
				+ "	<option value=kz_en_home_none>Kazakhstan </option>\n"
				+ "	<option value=ke_en_home>Kenya </option>\n"
				+ "	<option value=ki_en_home_none>Kiribati </option>\n"
				+ "	<option value=xk_en_home_none>Kosovo </option>\n"
				+ "	<option value=xe_en_home_none>Kosrae Island </option>\n"
				+ "	<option value=kw_en_home>Kuwait </option>\n"
				+ "	<option value=kg_en_home_none>Kyrgyzstan </option>\n"
				+ "	<option value=la_en_home_none>Laos </option>\n"
				+ "	<option value=lv_en_home>Latvia </option>\n"
				+ "	<option value=lb_en_home>Lebanon </option>\n"
				+ "	<option value=ls_en_home_none>Lesotho </option>\n"
				+ "	<option value=lt_en_home>Lithuania </option>\n"
				+ "	<option value=lu_en_home_none>Luxembourg </option>\n"
				+ "	<option value=mk_en_home_none>Macedonia </option>\n"
				+ "	<option value=mg_en_home_none>Madagascar </option>\n"
				+ "	<option value=mw_en_home_none>Malawi </option>\n"
				+ "	<option value=my_en_home>Malaysia </option>\n"
				+ "	<option value=mv_en_home_none>Maldives </option>\n"
				+ "	<option value=ml_en_home_none>Mali </option>\n"
				+ "	<option value=mt_en_home>Malta </option>\n"
				+ "	<option value=mh_en_home_none>Marshall Islands </option>\n"
				+ "	<option value=mq_en_home_none>Martinique </option>\n"
				+ "	<option value=mr_en_home_none>Mauritania </option>\n"
				+ "	<option value=mu_en_home_none>Mauritius </option>\n"
				+ "	<option value=mx_es_home>Mexico </option>\n"
				+ "	<option value=md_en_home_none>Moldova </option>\n"
				+ "	<option value=mn_en_home_none>Mongolia </option>\n"
				+ "	<option value=ms_en_home>Montserrat </option>\n"
				+ "	<option value=ma_en_home>Morocco </option>\n"
				+ "	<option value=mz_en_home_none>Mozambique </option>\n"
				+ "	<option value=np_en_home_none>Nepal </option>\n"
				+ "	<option value=nl_nl_home>Netherlands </option>\n"
				+ "	<option value=nc_en_home_none>New Caledonia </option>\n"
				+ "	<option value=nz_en_home>New Zealand </option>\n"
				+ "	<option value=ni_en_home>Nicaragua </option>\n"
				+ "	<option value=ne_en_home_none>Niger </option>\n"
				+ "	<option value=ng_en_home>Nigeria </option>\n"
				+ "	<option value=mp_en_home_none>Northern Mariana Islands </option>\n"
				+ "	<option value=no_en_home>Norway </option>\n"
				+ "	<option value=om_en_home_none>Oman </option>\n"
				+ "	<option value=pk_en_home_none>Pakistan </option>\n"
				+ "	<option value=pw_en_home_none>Palau </option>\n"
				+ "	<option value=pa_es_home>Panama </option>\n"
				+ "	<option value=pg_en_home_none>Papua New Guinea </option>\n"
				+ "	<option value=py_en_home>Paraguay </option>\n"
				+ "	<option value=pe_en_home>Peru </option>\n"
				+ "	<option value=ph_en_home>Philippines </option>\n"
				+ "	<option value=pl_en_home>Poland </option>\n"
				+ "	<option value=xp_en_home_none>Ponape </option>\n"
				+ "	<option value=pt_pt_home>Portugal </option>\n"
				+ "	<option value=pr_es_home>Puerto Rico </option>\n"
				+ "	<option value=qa_en_home>Qatar </option>\n"
				+ "	<option value=re_en_home>Reunion </option>\n"
				+ "	<option value=ro_en_home>Romania </option>\n"
				+ "	<option value=xc_en_home_none>Rota </option>\n"
				+ "	<option value=ru_en_home>Russia </option>\n"
				+ "	<option value=rw_en_home_none>Rwanda </option>\n"
				+ "	<option value=xs_en_home_none>Saipan </option>\n"
				+ "	<option value=sa_en_home>Saudi Arabia </option>\n"
				+ "	<option value=sn_en_home_none>Senegal </option>\n"
				+ "	<option value=cs_en_home_none>Serbia and Montenegro </option>\n"
				+ "	<option value=sc_en_home_none>Seychelles </option>\n"
				+ "	<option value=sg_en_home>Singapore </option>\n"
				+ "	<option value=sk_en_home>Slovakia </option>\n"
				+ "	<option value=si_en_home>Slovenia </option>\n"
				+ "	<option value=sb_en_home_none>Solomon Islands </option>\n"
				+ "	<option value=za_en_home>South Africa </option>\n"
				+ "	<option value=kr_ko_home>South Korea </option>\n"
				+ "	<option value=es_es_home>Spain </option>\n"
				+ "	<option value=lk_en_home_none>Sri Lanka </option>\n"
				+ "	<option value=nt_en_home_none>St. Barthelemy </option>\n"
				+ "	<option value=vi_en_home>St. Croix </option>\n"
				+ "	<option value=eu_en_home>St. Eustatius </option>\n"
				+ "	<option value=vi_en_home>St. John </option>\n"
				+ "	<option value=kn_en_home>St. Kitts and Nevis </option>\n"
				+ "	<option value=lc_en_home>St. Lucia </option>\n"
				+ "	<option value=mb_en_home_none>St. Maarten </option>\n"
				+ "	<option value=vi_en_home>St. Thomas </option>\n"
				+ "	<option value=vc_en_home>St. Vincent and the Grenadines </option>\n"
				+ "	<option value=sr_en_home>Suriname </option>\n"
				+ "	<option value=sz_en_home_none>Swaziland </option>\n"
				+ "	<option value=se_sv_home>Sweden </option>\n"
				+ "	<option value=ch_en_home>Switzerland </option>\n"
				+ "	<option value=sy_en_home_none>Syria </option>\n"
				+ "	<option value=tj_en_home_none>Tadjikistan </option>\n"
				+ "	<option value=tw_zh_home>Taiwan </option>\n"
				+ "	<option value=tz_en_home_none>Tanzania </option>\n"
				+ "	<option value=th_en_home>Thailand </option>\n"
				+ "	<option value=xn_en_home_none>Tinian </option>\n"
				+ "	<option value=tg_en_home_none>Togo </option>\n"
				+ "	<option value=to_en_home_none>Tonga </option>\n"
				+ "	<option value=vg_en_home>Tortola </option>\n"
				+ "	<option value=tt_en_home>Trinidad and Tobago </option>\n"
				+ "	<option value=xa_en_home_none>Truk </option>\n"
				+ "	<option value=tn_en_home_none>Tunisia </option>\n"
				+ "	<option value=tr_en_home>Turkey </option>\n"
				+ "	<option value=tm_en_home_none>Turkmenistan </option>\n"
				+ "	<option value=tc_en_home_none>Turks and Caicos </option>\n"
				+ "	<option value=tv_en_home_none>Tuvalu </option>\n"
				+ "	<option value=ug_en_home_none>Uganda </option>\n"
				+ "	<option value=ua_en_home>Ukraine </option>\n"
				+ "	<option value=vc_en_home>Union Island </option>\n"
				+ "	<option value=ae_en_home>United Arab Emirates </option>\n"
				+ "	<option value=gb_en_home>United Kingdom </option>\n"
				+ "	<option value=us_en_home>United States </option>\n"
				+ "	<option value=uy_en_home>Uruguay </option>\n"
				+ "	<option value=vi_en_home>US Virgin Islands </option>\n"
				+ "	<option value=uz_en_home_none>Uzbekistan </option>\n"
				+ "	<option value=vu_en_home_none>Vanuatu </option>\n"
				+ "	<option value=ve_en_home>Venezuela </option>\n"
				+ "	<option value=vn_en_home_none>Vietnam </option>\n"
				+ "	<option value=vg_en_home>Virgin Gorda </option>\n"
				+ "	<option value=wf_en_home_none>Wallis and Futuna </option>\n"
				+ "	<option value=ws_en_home_none>Western Samoa </option>\n"
				+ "	<option value=xy_en_home_none>Yap </option>\n"
				+ "	<option value=ye_en_home_none>Yemen </option>\n"
				+ "	<option value=zm_en_home_none>Zambia </option>\n"
				+ "	<option value=zw_en_home>Zimbabwe </option>\n"
				+ "	</select>";

		String country = backgroundData.getHighSchoolCountry();

		if (country != null && !country.equals("")) {
			select = select.replaceAll(country, country + " selected");
		}
		return select;
	}
}

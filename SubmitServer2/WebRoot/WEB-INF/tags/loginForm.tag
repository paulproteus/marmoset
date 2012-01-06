<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


    <c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >
		<c:if test="${not empty param.target}">
	    <input type="hidden" name="target" value="${ss:urlEncode(param.target)}">
	    </c:if>
	    <table class="form">
	    <tr><th colspan=2>Login with your <a href="http://www.helpdesk.umd.edu/topics/passwords/systems/ldap/4027/" title="help finding your Directory ID and Password">Directory ID and password</a>
	    <tr><td class="label">Directory ID:<td class="input"> <input type="text" name="loginName" autofocus autocorrect="off" autocapitalize="off"/>
		<tr><td class="label">Directory Password: <td class="input"> <input type="password" name="uidPassword"/>
		<tr><td class="label">Keep me logged in:<td class="input"><input type="checkbox" name="keepMeLoggedIn" value="checked" title="Do not check on a shared computer/account"/> 
        
		<tr><td class="label"></td><td class="submit"><input type="submit" value="Login" name="Login"/></td></tr>

							</table>
	</form>


    <p class="alertmessage" style="font-size: smaller;">
                    NOTICE: Unauthorized access to this computer is in violation of
                    Article 27. Sections 45A and 146 of the Annotated Code of MD. The
                    university may monitor use of this system as permitted by state and
                    federal law, including the Electronic Communications Privacy Act,
                    18 U.S.C. sections 2510 et seq. Anyone using this system
                    acknowledges that all use is subject to University of Maryland
                    Acceptable Use Guidelines available at <a
                        href="http://www.inform.umd.edu/aug">http://www.inform.umd.edu/aug</a>.
                </p>

<!-- 
                <p class="indent">
                    <b>Students:</b> Projects for the current semester will remain
                    available for 2 weeks following the end of the semester, at which
                    point they will be archived. Backup any projects you want to keep
                    before then.
                </p>

                <p class="indent">
                    <b>Instructors:</b> Add your courses on <a
                        href="https://grades.cs.umd.edu/">grades.cs.umd.edu</a>.
                </p>
                 -->
                







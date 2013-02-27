<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<html>
<head>
<script
  src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
  type="text/javascript"></script>
  <title>Testing</title>
  </head>
  <body>
  <h2>  ¥ · £ · € · $ · ¢ · ₡ · ₢ · ₣ · ₤ · ₥ · ₦ · ₧ · ₨ · ₩ · ₪ · ₫ · ₭ · ₮ · ₯ · ₹</h2>
  <c:url value="/echo" var="actionUrl" />
  <h3>Form 1</h3>
<form action="${actionUrl}" method="POST" accept-charset="UTF-8">
<input type="text" name="firstname" id="firstname-input" placeholder="First" class="required" required="required" />
<input type="hidden" name="stuff" value=" ¥ · £ · € · $ · ¢ · ₡ · ₢ · ₣ · ₤ · ₥ · ₦ · ₧ · ₨ · ₩ · ₪ · ₫ · ₭ · ₮ · ₯ · ₹"/>
			<button name="action" value="UPDATE" type="submit">Submit</button>
			</form>
			  <h3>Form 2</h3>
<form action="${actionUrl}" method="POST" >
<input type="text" name="lastname" id="firstname-input" placeholder="First" class="required" required="required" />
<input type="hidden" name="stuff" value=" ¥ · £ · € · $ · ¢ · ₡ · ₢ · ₣ · ₤ · ₥ · ₦ · ₧ · ₨ · ₩ · ₪ · ₫ · ₭ · ₮ · ₯ · ₹"/>
			<button name="action" value="UPDATE" type="submit">Submit</button>
			</form>
			
			
   </body>
  </html>

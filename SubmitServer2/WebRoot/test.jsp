<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE html>
<html>
<head>
<script
  src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"
  type="text/javascript"></script>
  
  <script>
  $(document).ready(function() {
 $('#slickbox').hide();
 $('#slick-show').click(function() {
 $('#slickbox').show('slow');
 return false;
 });
$('#slick-hide').click(function() {
 $('#slickbox').hide('fast');
 return false;
});
 $('#slick-toggle').click(function() {
 $('#slickbox').toggle(400);
 return false;
});
});
  </script>
  </head>
  <body>
  <p><a href="#" id="slick-show">Show the box</a>&nbsp;&nbsp;<a href="#" id="slick-hide">Hide the box</a>&nbsp;&nbsp;<a href="#" id="slick-toggle">Toggle the box</a></p>
  
 <div id="slickbox" style="display: block; ">This is the box that will be shown and hidden and toggled at your whim. :)</div>
  </body>
  </html>
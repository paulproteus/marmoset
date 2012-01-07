Subdirectories of WebRoot/authenticate represent different authentication
mechanisms. The submit server will redirect to /authenticate/${type}/ to allow a
user to authenticate.

The desired authentication system can be selected by setting the
authentication.type context parameter in web.xml.

# OtterShare
Welcome to OtterShare<br>
This repo houses both server and native files, in our case Android.

# API Documentation
Below is a brief description on our endpoints <br>
**Note**: Some of our endpoints are still under construction, therefore this documentation will be updated regularly
## GET / <br>
Eventually, when our development team has more time, we plan to serve up an active website to go with our app. As of now we serve up a welcome message at our root.


## POST /createUser <br>
Creates a user with default fields <br>
**JSON to post:** <br>
"name": "name here",
"email": "email-here",
"password": "password-here",
"carMakeModel": "car-make-and-model-here"<br>
**Response**<br>
Entered information returned, as well as instruction to check email.


## POST /login <br>
Takes username and email of registered user for login<br>
**JSON to post:**<br>
"email" : "enter-email-here",<br>
"password" : "password-here" <br>
**Response**<br>
Info on current user along with authentication key for requests.


## GET /verify/{email-key}<br>
Takes verification key sent to user email account, activates user account <br>
**JSON to append** <br>
"key" : "Alpha-numeric key here"<br>
**Response** <br>
Verification message response of true or false


## POST /myProfile <br>
Takes api-key and email of current users account, and returns their profile as seen by themselves
**JSON to post:** <br>
"email" : "csumb-email-here",
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information including schedule


## POST /users <br>
Takes api-key of current user and email of a certain account, and returns their public profile information
**JSON to post:** <br>
"email" : "csumb-email-here",
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information excluding schedule


## POST /myProfile <br>
Takes api-key and email of current users account, and returns their profile as seen by
**JSON to post:** <br>
"email" : "csumb-email-here",
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information including schedule


## POST /myProfile <br>
Takes api-key and email of current users account, and returns their profile as seen by
**JSON to post:** <br>
"email" : "csumb-email-here",
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information including schedule

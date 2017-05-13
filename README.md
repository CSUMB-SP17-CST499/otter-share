# OtterShare
Welcome to OtterShare <br>
This repo houses both server and native files, in our case Android.

# API Documentation
Below is a brief description on our endpoints <br>
**Note**: Some of our endpoints are still under construction, therefore this documentation will be updated regularly


## GET / <br>
Eventually, when our development team has more time, we plan to serve up an active website to go with our app. As of now we serve up a welcome message at our root.


## POST /createUser <br>
Creates a user with default fields <br>
**JSON to post:** <br>
"name": "name here", <br>
"email": "email-here", <br>
"password": "password-here"<br>
**Response**<br>
Creation successful! To continue, check your email to verify account!

## POST /createUser/completeProfile <br>
Completes a user profile with a few more fields <br>
**JSON to post** <br>
"api-key" : "api-key-here", <br>
"schedule" : "schedule-here", <br>
"carMakeModel": "car-make-and-model-here"<br>
**Response** <br>
Success! Profile complete!

## POST /login <br>
Takes username and email of registered user for login <br>
**JSON to post:** <br>
"email" : "enter-email-here",<br>
"password" : "password-here" <br>
**Response** <br>
Info on current user along with authentication key for requests.


## GET /verify/{email-key}<br>
Takes verification key sent to user email account, activates user account <br>
**JSON to append** <br>
"key" : "Alpha-numeric key here"<br>
**Response** <br>
Verification message response of true or false


## POST /myProfile <br>
Takes api-key and email of current users account, and returns their profile as seen by themselves <br>
**JSON to post:** <br>
"email" : "csumb-email-here",<br>
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information including schedule


## POST /users <br>
Takes api-key of current user and email of a certain account, and returns their public profile information <br>
**JSON to post:** <br>
"email" : "csumb-email-here",<br>
"api_key" : "api-key-here"<br>
**Response** <br>
Returns json formatted profile information excluding schedule


## POST /registerPass <br>
Creates a pass for a user from given info (api_key, email-address, lotLocation, price and notes ) <br>
**JSON to post:** <br>
"api_key" : "api-key-here",<br>
"email": "email-here",<br>
"lotLocation" : "lot-location-here",<br>
"price" : "price-here",<br>
"notes" : "notes-here"<br>
**Response** <br>
created pass!


## POST /activeUsers <br>
Returns active passes based on given lot location : api_key, keyword (Either 'all' or a specific lot) <br>
**JSON to post:** <br>
"api_key" : "api-key-here",<br>
"keyword": "keyword-here"<br>

**Response** <br>
Pass(es) information <br>
UPDATE: Now returns the average rating of the user tied to pass, as avgRating.

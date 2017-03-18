/**
    CST 499 - Final Capstone Project
    server.js
    Purpose: Communication between Android App and Neo4j DB, using Express, Node and neo4j drivers.

    @author Mason Lopez
    @version 1.1 3/15/2017
**/

var express = require('express');
var bodyParser = require('body-parser');
var db = require('./db_operations/db');
var bcrypt = require('bcryptjs');
var _ = require('lodash');

var app = express();

const port = process.env.PORT || 3000;

app.use(bodyParser.json()); //uses bodyParser middleware for reading body
app.use(bodyParser.urlencoded({
    extended: false
}));

//routes
app.get('/', (req, res) => {
    //may serve up OtterShare website later.
    res.status(200).send("Welcome to OtterShare, nothing to GET, though");
});

app.get('/verify/:key', (req,res) => {
  //check to see if key is in db, if so, authenticate user it matches with!
  if(req.params.key){
      db.verifyEmail(req.params.key, (err,verify_email_key) => {
        if(err) {
          console.log(err);
        }
        res.send('VERIFIED!' + verify_email_key);
    });
  }
});

app.post('/login', (req, res) => {
    // Search for name in db
    // need to check for CSUMB email
    // will need to check for email & auth key instead, next goal
    if (!!req.body.email && !!req.body.password) {
        //run findByEmailPw callback function, if found w/ matching pw, authenticate
        // Send in hashed pw, for comparison
        db.findByEmailPw(req.body.email, req.body.password, (err, user) => {
            if (err) {
                res.send(err);
            }
            if (!!user) {
                res.send('Connected as : ' + user);
            } else {
                res.send('User not found');
            }
        });

    } else {
        console.log(req.body.email);
    }
});
app.post('/testAuth', (req,res) => {
  if(!!req.body.authKey) {
    db.auth(req.body.authKey, (err, authUser) => {
      let toJson = '';
      _.forEach(authUser.records, (record) => {
        // Prints the password field console.log(record._fields[3]);
        toJson += record._fields;
      });
      if(_.isEmpty(toJson)){
        console.log('Failed to match');
        res.status(401).send('<h3>Failed to Authenticate</h3>');

      }
      else {
        console.log(toJson);
        res.send(toJson);
      }
    });
  } else {
    console.log('Auth Error -> No api key given');
    res.status(401).send('<h2>Looking for something?</h2>');
  }
});
app.post('/createUser', (req, res) => {
    // Search for name in db
    // need to check for CSUMB email
    // app just needs to start here by sending email, right now I have email. No pw yet
    var name = null || req.body.name;
    var email = null || req.body.email;
    var location = null || req.body.location;
    var password = null || req.body.password;

    // need to check for session as well, via auth key, will after testing.
    if (!!name && !!email && !!location && !!password) {
        // hashes password via response from callback, stored in hash!
        bcrypt.hash(password, 10, (err, hash) => {
            // Store hash in password DB, as well as all other fields.
            let hashedPassword = hash;
            db.createUser(email, name, location, hashedPassword, (err, response) => {
                if (err) {
                    res.send(err);
                }
                res.send(response);
            });
        });
    }
});
// Essentially DROPS data from database ! For testing purposes only!!!
// app.get('/reset', (req, res, next) => {
//     db.resetDB((err, succ) => {
//         if (err) {
//             console.log(err);
//         } else {
//             res.send(succ);
//         }
//     });
// });

//begins listening on port 3000 or instance given port .
app.listen(port, () => {
    console.log("Started on port " + port);
});

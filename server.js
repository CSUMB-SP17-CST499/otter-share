/**
    CST 499 - Final Capstone Project
    server.js
    Purpose: Communication between Android App and Neo4j DB, using Express, Node and neo4j drivers.

    @author Mason Lopez
    @version 1.1 3/15/2017
**/
"use strict";
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
app.get('/verify/:key', (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    //check to see if key is in db, if so, authenticate user it matches with!
    if (req.params.key) {
        db.verifyEmail(req.params.key, (err, verify_email_key) => {
            if (err) {
                console.log(err);
            }
            res.send('VERIFIED ! ' + verify_email_key);
            //res.json when serving to our client on Android
        });
    }
});
app.post('/users', (req,res) => {
  res.setHeader('Content-Type', 'application/json');
  // NOTE if account exists, return Profile info, else 'user does not exist'
  // if received strings user and api key, then we set vars equal to said data
  // otherwise they are null and refuse access
  var username = req.body.username.trim() || null;
  var api_key = req.body.api_key.trim() || null;
  if(!!username && !!api_key){
    db.authCheck(api_key, (err, user) => {
      if(err){
        res.send({error:"callback error"});
      }
      db.retrieveUser(username, (err, payload) => {
        if(err){
          res.send({error:"callback error"});
        }
        // console.log(payload);
        res.send(payload);
      });
    });
  } else {
    res.send({error:'Unauthorized'});
  }
});

app.post('/login', (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    // Search for name in db
    if (!!req.body.email && !!req.body.password) {
        //run login callback function, if found w/ matching pw, authenticate
        // Send in pw/email, for comparison, trim any whitespace leading or before email and pw
        db.login(req.body.email.trim(), req.body.password.trim(), (err, user) => {
            if (err) {
                res.send(err);
            }
            if (!!user) {
                // If email is not verified, then we send an error back describing what to do next
                if (!JSON.parse(user).email_verified) {
                    res.json({
                        error: 'Code 0'
                    })
                } else {
                    // If email is verified, we return logged in users info
                    res.send(user);
                }

            }
            // Pw is wrong
            else if (user == false) {
                res.json({
                    error: 'Code 1'
                });
            }
            // User does not exist
            else {
                res.json({
                    error: 'Code 1'
                });
            }
        });

    } else {
        console.log(req.body.email);
    }
});
// takes api auth key, runs function to see if it exists.
app.post('/testAuth', (req, res) => {
    res.setHeader('Content-Type', 'application/json');

    if (!!req.body.authKey) {
        db.authCheck(req.body.authKey, (err, authUser) => {
            let toJson = '';
            _.forEach(authUser.records, (record) => {
                // Prints the password field console.log(record._fields[3]);
                toJson += record._fields;
            });
            if (_.isEmpty(toJson)) {
                console.log('Failed to match');
                res.status(401).send('<h3>Failed to Authenticate</h3>');

            } else {
                console.log(toJson);
                res.send(toJson);
            }
        });
    } else {
        console.log('Auth Error -> No api key given');
        res.status(401).send('<h2>Looking for something?</h2>');
    }
});
// Client creates an account by sending JSON with name, email and password. Creates IF account has Csumb email, and email is not in our system.
app.post('/createUser', (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    var name = null || req.body.name.trim();
    var email = null || req.body.email.trim();
    var password = null || req.body.password.trim();
    var username = null || req.body.username.trim();
    var carMakeModel = null || req.body.carMakeModel.trim();
    var schedule = null || req.body.schedule.trim();

    // if all of these fields are not null, and email is in correct format then continue with creation.
    if (!!name && !!email && !!password && !!username && !!carMakeModel && !!schedule) {
        // Store hash in password DB, as well as all other fields.
        db.createUser(email, name, password, username, carMakeModel, schedule, (err, response) => {
            if (err) {
                res.send(err);
            }
            res.send(response);
        });
    } else {
        res.json({
            error: 'incorrect field format'
        });
    }
});

// Essentially DROPS data from database ! LEAVE commented before spinning up on server! (TESTS ONLY)
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

module.exports.app = app;

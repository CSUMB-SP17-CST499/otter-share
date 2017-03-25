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

app.post('/login', (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    // Search for name in db
    // need to check for CSUMB email
    // will need to check for email & auth key instead, next goal
    if (!!req.body.email && !!req.body.password) {
        //run findByEmailPw callback function, if found w/ matching pw, authenticate
        // Send in pw/email, for comparison, trim any whitespace leading or before email and pw
        res.setHeader('Content-Type', 'application/json');
        db.findByEmailPw(req.body.email.trim(), req.body.password.trim(), (err, user) => {
            if (err) {
                res.send(err);
            }
            if (!!user) {
                // If email is not verified, then we send an error back describing what to do next
                if (!JSON.parse(user).email_verified) {
                    res.json({
                        error: 'Sorry, you need to verify your account by clicking the link in the email!'
                    })
                } else {
                    // If email is verified, we return logged in users info
                    res.send(user);
                }

            }
            // Pw is wrong
            else if (user == false) {
                res.json({
                    error: 'Incorrect password!'
                });
            }
            // User does not exist
            else {
                res.json({
                    error: 'User does not exist, make an account!'
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
        db.auth(req.body.authKey, (err, authUser) => {
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

    // if all of these fields are not null, and email is in correct format then continue with creation.
    if (!!name && !!email && !!password) {
        // Store hash in password DB, as well as all other fields.
        db.createUser(email, name, password, (err, response) => {
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

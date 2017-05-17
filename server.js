/**
    CST 499 - Final Capstone Project
    server.js
    Purpose: Communication between Android App and Neo4j DB, using Express, Node and Neo4j drivers.

    @author Mason Lopez
    @version 1.1 3/15/2017
**/
"use strict";
var express = require('express');
var bodyParser = require('body-parser');
var db = require('./db_operations/db');
var bcrypt = require('bcryptjs');
var _ = require('lodash');

const port = process.env.PORT || 3000;

var app = express();
app.use(bodyParser.json()); //uses bodyParser middleware for reading body
app.use(bodyParser.urlencoded({
  extended: false
}));

function executeOrder66(x) {
  // kills process every 10 minutes, forcing heroku to restart application.
  console.log(x);
  process.exit(0);
}
//routes
app.get('/', (req, res) => {
  //may serve up OtterShare website later.
  res.status(200).send("Welcome to OtterShare, nothing to GET, though");
});

// Verification of email address via email sent to user
app.get('/verify/:key', (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  //check to see if key is in db, if so, authenticate user it matches with!
  if (req.params.key) {
    db.verifyEmail(req.params.key, (err, verify_email_key) => {
      if (err) {
        console.log(err);
      }
      res.send(`Verifified (true\/false): ${verify_email_key}`);
    });
  }
});
// For viewing a users own private profile, generates a little more.
// NOTE: Must have matching API key to view this
app.post('/myProfile', (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  var api_key = req.body.api_key;
  var email = req.body.email;
  if (!!email && !!api_key) {
    db.retrieveMyProfile(email.trim(), api_key.trim(), (err, payload) => {
      res.send(payload);
    });
  }

});

// For receiving public profile of another user, requires an api_key and target email address
app.post('/users', (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  var email = req.body.email;
  var api_key = req.body.api_key;
  if (!!email && !!api_key) {
    db.retrieveUser(email.trim(), api_key.trim(), (err, payload) => {
      if (err) {
        res.send({
          error: "callback error"
        });
      }
      res.send(payload);
    });
  } else {
    res.send({
      error: 'Unauthorized'
    });
  }
});
// Login requires an email from CSUMB and password of a user, password must be minimum of 8 characters
app.post('/login', (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  // Search for name in db
  if (!!req.body.email && !!req.body.password) {
    // Run login callback function, if found w/ matching pw, retrieve login info
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
    // console.log(req.body.email);
    res.json({
      error: 'Incorrect properties POSTed'
    });
  }
});
// Client creates an account by sending JSON with name, email and password. Creates IF account has Csumb email, and email is not in our system.
app.post('/createUser', (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  var name = req.body.name;
  var email = req.body.email;
  var password = req.body.password;
  // if all of these fields are not null, and email is in correct format then continue with creation.
  if (!!name && !!email && !!password) {
    // Store hash in password DB, as well as all other fields.
    db.createUser(email.trim(), name.trim(), password.trim(), (err, response) => {
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
// Completes the account creation process (also updates) by taking in requested properties
app.post('/createUser/completeProfile', (req, res) => {
  var api_key = req.body.api_key;
  var carMakeModel = req.body.carMakeModel;
  var schedule = req.body.schedule;
  if (!!api_key && !!carMakeModel && !!schedule) {
    db.completeProfile(api_key, carMakeModel, schedule, (err, response) => {
      if (response) {
        res.send(response);
      } else {
        res.send(err);
      }
    });
  } else {
    res.send({
      error: 'Please fill out all fields before sending a POST request'
    });
  }
});
// Gets active users in specific parking lots or all parking lots at CSUMB
app.post('/activeUsers', (req, res) => {
  var keyword = req.body.keyword;
  var api_key = req.body.api_key;
  if (!!keyword && !!api_key) {
    db.activeUsers(keyword, api_key, (status, data) => {
      if (status) {
        res.send(data);
        return;
      }
      if (!status) {
        res.send({
          error: "Something went wrong!"
        });
        return;
      }
      if (!!status) {
        res.send({
          error: "Something Went Wrong!!"
        });
        return;
      }
    });
  }
});
// registers a pass to a User, or updates existing pass node.
app.post('/registerPass', (req, res) => {
  // for security purposes
  var api_key = req.body.api_key;
  var email = req.body.email;
  // for creation of the pass
  let lotLocation = req.body.lotLocation;
  let gpsLocation = req.body.gpsLocation;
  let price = req.body.price;
  let notes = req.body.notes;
  if (!!api_key && !!email && !!lotLocation && !!gpsLocation && !!price) {
    db.registerPass(email, api_key, lotLocation, gpsLocation, price, notes, (err, succ) => {
      if (err) {
        res.send({
          error: err
        });
      } else
        res.send(succ);
    });
  } else
    res.send({
      error: 'Please send all required parking pass fields'
    });
});
// Resends verification email
app.post('/resendEmail', (req, res) => {
  var email = req.body.email;
  if (!!email) {
    db.resendVerify(email, (status, response) => {
      //if status true, return success message, if null, return message, if false, return why
      if (status) {
        res.send({
          success: response
        });
        return;
      }
      if (status == null) {
        res.status(500).send({
          error: response
        });
        return;
      }
      if (!status) {
        res.status(500).send({
          error: response
        });
        return;
      }
    })
  }
});
// // Buying a pass from a user, takes buyer's api key, the passes's current owner email and ID
// // This should be invoked after a payment has been processed, changes ownership of parking pass
// app.post('/purchasePass', (req,res) => {
//   var api_key = req.body.api_key;
//   var currentOwnerEmail = req.body.currentOwnerEmail;
//   var passId = req.body.passId;
//   if(!!api_key && !!currentOwnerEmail && !!passId){
//     db.purchasePass(api_key, currentOwnerEmail, passId, (err, response) => {
//       if(err) {
//         return res.send(err);
//       }
//       return res.send(response);
//     });
//   }
//   else {
//     return res.send({error:'error, incorrect parameters received.'});
//   }
// });
//
app.post('/buyerListener', (req, res) => {
  var api_key = req.body.api_key;
  var passId = req.body.passId;
  var requestCount = req.body.requestCount;
  // maybe I can ask if they are buying or selling?
  // On front end, keep count of requests.. if count = 0,  AND custType = Buyer we change the sale to pending
  // if count greater than 0 but less than 300? (1 req a minute, idk what our limit is..) then we should no longer accept them
  if (!!api_key && !!passId && !!requestCount) {
    db.buyerListener(api_key, passId, requestCount, (status, data) => {
      if (status == false) {
        res.status(400).send(data);
      } else
        res.status(200).send(data);

    });
  } else {
    res.status(400).send({
      error: 'Incorrect parameters received'
    });
  }
});
app.post('/sellerListener', (req, res) => {
  var api_key = req.body.api_key;
  var passId = req.body.passId;
  var action = req.body.action;

  if (!!api_key && !!passId) {
    db.sellerListener(api_key, passId, action, (status, data) => {
      if (status == false) {
        res.status(400).json(data);
      } else
        res.status(200).send(data);
    });
  } else {
    res.send({
      error: 'Incorrect parameters received'
    });
  }
});
app.post('/completionListener', (req,res) => {
  var api_key = req.body.api_key;
  var passId = req.body.passId;
  var customerType = req.body.customerType;
  if(!!api_key && !!passId && !!customerType){
    db.completionListener(api_key, passId, customerType, (status, data) => {
      if(status==false){
        res.status(500).send(data);
      } else {
        res.status(200).send(data);
      }
    });
  } else {
    res.status(400).send({error:'Incorrect parameters sent'});
  }

});
// Essentially DROPS data from database ! LEAVE commented before spinning up on server! (TESTS ONLY)
// app.get('/reset', (req, res) => {
//     db.resetDB((err, succ) => {
//         if (err) {
//             console.log(err);
//         } else {
//             res.send(succ);
//         }
//     });
// });
// begins listening on port 3000 or instance given port.
app.listen(port, () => {
  // setTimeout(executeOrder66, 624000, 'Killing Server to prevent session expiration!? (this is temporary)');
  console.log(`Started on port ${port}`);
});

module.exports.app = app;
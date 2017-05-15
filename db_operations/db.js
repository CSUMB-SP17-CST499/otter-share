
/*
  Handles all db operations
*/
"use strict";
var neo4j = require('neo4j-driver').v1;
// ---> Credentials for connecting to GRAPHENEDB with Heroku!
var graphenedbURL = process.env.GRAPHENEDB_BOLT_URL || "bolt://localhost:3001";
var graphenedbUser = process.env.GRAPHENEDB_BOLT_USER || "neo4j";
var graphenedbPass = process.env.GRAPHENEDB_BOLT_PASSWORD || "root";
var driver = neo4j.driver(graphenedbURL, neo4j.auth.basic(graphenedbUser, graphenedbPass));
//
var session = driver.session();
var bcrypt = require('bcryptjs');
var _ = require('lodash');
const cryptoRandomString = require('crypto-random-string');
var shortid = require('shortid');
const env = require('env2')('./.env');
const nodemailer = require('nodemailer');
var fs = require('fs');
var moment = require('moment');

// Takes email and password, searches neo4j db for them, if found, returns user data
const login = (email, password, callback) => {
    let cqlString =
        "MATCH (a:User) WHERE a.email = {email} RETURN " +
        "a.name AS name, a.email AS email, a.password AS password," +
        "a.api_access_key AS api_key, a.verify_email_key AS verify_email_key," +
        "a.carMakeModel AS carMakeModel, a.schedule AS schedule";

    session
        .run(cqlString, {
            email: email
        })
        .then((result) => {
            session.close();
            if (!_.isEmpty(result.records)) {
                var userObj = new Object();
                let stored_pw = '';
                _.forEach(result.records, (record) => {
                    userObj.name = record._fields[0];
                    userObj.email = record._fields[1];
                    stored_pw = record._fields[2];
                    userObj.api_key = record._fields[3];
                    userObj.email_verified = record._fields[4];
                    userObj.carMakeModel = record._fields[5];
                    userObj.schedule = record._fields[6];
                    // Make this false for client, if false, tell user to verify email!
                    if (_.isString(userObj.email_verified)) {
                        userObj.email_verified = false;
                    }
                });
                // converts the object into a Json string for client
                var result_string = JSON.stringify(userObj);
                // compares entered password with stored_pw in database.
                bcrypt.compare(password, stored_pw, (err, res) => {
                    if (res == true) {
                    // sets status to a timestamp, allowing us to find last time user active
                        session
                           .run("MATCH (a:User) WHERE a.email = {email} SET a.status = timestamp()", {email:email})
                           .then(() => {session.close();})
                           .catch((e) => {
                             session.close();
                             console.log(`At Line 61 ${JSON.stringify(e)} `);
                           });
                        return callback(null, result_string);
                    }
                    // return nothing if no match, NOTE: Should return false
                    session.close();
                    return callback(null, false);
                });
            } else {
                // user Does Not exist. NOTE: Should return nothing null, this is ok.
                return callback(null, null);
            }
        })
        .catch((e) => {
            session.close();
            console.log(e);
            return callback('error caught via Login ->: ' + JSON.stringify(e));
        });
}
const completeProfile = (api_key, carMakeModel, schedule, callback) => {
    // NOTE: Can regex to prevent users from making up fake models of car
    session
        .run('MATCH (user:User {api_access_key: {api_access_key}}) RETURN user', {
            api_access_key: api_key
        })
        .then((results) => {
            session.close();
            // accessing model of car by results.records[0]._fields[0].properties.carMakeModel
            // if api_key entered wrong, return error object
            // console.log(results);
            if (_.isEmpty(results.records)) return callback(null, {
                error: 'incorrect api_key'
            });
            // Removed the update once feature, now able to update as much as they want.
            session
                .run('MERGE (user:User {api_access_key:{api_access_key}})' +
                    'SET user.carMakeModel = {carMakeModel}, user.schedule = {schedule}, user.completeProfile = {completeProfile}' +
                    'RETURN user', {
                        api_access_key: api_key,
                        carMakeModel: carMakeModel,
                        schedule: schedule,
                        completeProfile: true
                    })
                .then((results) => {
                    session.close();
                    if (!_.isEmpty(results.records)) {
                        return callback(null, { success: 'Profile creation / update complete!!'});
                    } else {
                        return callback(null, { error: 'error'});
                    }
                })
                .catch((e) => {
                    session.close();
                    return callback(null, { error: e });
                });

        })
        .catch((e) => {
            session.close();
            console.log(e);
            return callback('error caught via completeProfile ->: ' + JSON.stringify(e));
        })

}

const createUser = (email, name, password, callback) => {
    // NOTE if schedule isn't received as an array, we will need to tokenize it and push it into an array before storing
    const emailRegex = /^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(csumb)\.edu$/;
    const nameRegex = /^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$/u;
    const passRegex = /^([a-zA-Z0-9@*#]{8,15})$/;
    if (!emailRegex.test(email)) {
        return callback(null, { error: 'Incorrect email format, it must be from CSUMB!'});
    }
    if (!nameRegex.test(name)) {
        return callback(null, { error: 'Incorrect Name format!'});
    }
    if (!passRegex.test(password)) {
        return callback(null, { error: 'Incorrect password format! Must be minimum 8 characters!' });
    }

    session
        .run('MATCH (user:User {email: {email}}) RETURN user', {
              email: email
        })
        .then((results) => {
          session.close();
            if (!_.isEmpty(results.records)) {
                // if we have records that return, this shows that email is in use, therefore fail with null
                return callback(null, { error: 'This email is in use! Try to login instead'});
            } else {
                // this key will be sent to users emails to verify they are csumb students!
                // it will first store a key, then a boolean value of TRUE when account has
                // been verified, status  has a value of 0, 1, 2, init, verified and logged in
                var verifyEmailKey = cryptoRandomString(60);
                bcrypt.hash(password, 10, (err, hash) => {
                    session
                        .run("CREATE (a:User {name: {name}, email: {email}, password: {password}, completeProfile:{completeProfile}," +
                            "api_access_key: {api_access_key}, status:{status}, emailTime:timestamp(), verify_email_key: {verify_email_key}})", {
                                name: name,
                                email: email,
                                password: hash,
                                api_access_key: cryptoRandomString(60),
                                verify_email_key: verifyEmailKey,
                                status: 0,
                                completeProfile:false,
                                totalStars: 5.0,
                                numberOfRatings: 1.0
                          })
                        .then(() => {
                            session.close();
                            var successObject = new Object();
                            successObject.success = "Creation successful! To continue, check your email to verify account!";
                            // sends email to user with instructions to verify email, without it no access to Ottershare
                            sendEmail(name, email, verifyEmailKey);
                            return callback(null, JSON.stringify(successObject));
                        })
                        .catch((e) => {
                            session.close();
                            return callback(null, { error_output: e });
                        });
                });
            }
        })
        .catch((e) => {
            session.close();
            var out = e;
            return callback(null, { error: out });
        });
}
const resendVerify = (email, callback) => {
  // Resends a verification email.
  const emailRegex = /^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(csumb)\.edu$/;
  if (!emailRegex.test(email)) {
      return callback(null, { error: 'Incorrect email format, it must be from CSUMB!'});
  }
  session
    .run("MATCH (u:User) WHERE u.email = {email} AND u.verify_email_key <> true RETURN u.emailTime, u.verify_email_key, u.name ", {
      email:email
    })
    .then((user) => {
      session.close();
      if (!_.isEmpty(user.records)) {
        let unix_time = '', verify_key = '', name ='';
        _.forEach(user.records, (record) => {
            unix_time += record._fields[0];
            verify_key += record._fields[1];
            name += record._fields[2];
        });
        var past_date = moment(parseInt(unix_time)).format("YYYY-MM-DD HH:mm");
        var now = moment();
        var difference = now.diff(past_date,'minutes');
        if(difference < 30) {
          // console.log(`You cannot resend verifcation for another ${30 - difference} minutes, please try again later.`);
          return callback(null,`You cannot resend verifcation for another ${30 - difference} minutes, please try again later.`);
        }
        // update timestamp in database, send another email with verify_key
        session
          .run("MATCH (u:User) WHERE u.email = {email} SET u.emailTime = timestamp()", {email:email})
          .then(() => { session.close(); sendEmail(name,email,verify_key); return callback(true,"Success, check your email again!"); })
          .catch((e) => { session.close(); console.log(e); return callback(null, {error:e}); });
      } else {
        console.log('Incorrect credentials');
        return callback(null, "Incorrect credentials entered");
      }
    })
    .catch((e) => {
      session.close();
      console.log(e);
      callback(null,e);
    });

}
// send's an email to given user, as well as the email verification key required to activate an account
const sendEmail = (name, email, verifyEmailKey) => {
    // Loads my custom html, converts to String, use Lodash function that inserts user info into html (lodash is freaken awesome!)
    let verifyUrl = process.env.AUTH_URL + verifyEmailKey;
    var html = fs.readFileSync(__dirname + '/email.html', 'utf-8');
    html = _.toString(html);
    var compiled = _.template(html);
    var modifiedHtml = compiled({'user' : name , 'url' : verifyUrl });
    // the following code blocks are what is needed to run nodemailer.
    let transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        },
        tls: {
            rejectUnauthorized: false
        }
    });

    let mailOptions = {
        from: '"Admin" <ottersharemb@gmail.com>', // sender address
        to: email, // list of receivers
        subject: 'Welcome to OtterShare!', // Subject line
        text: 'Welcome!', // plain text body
        html: modifiedHtml // html body
    };

    // send mail with defined transport object
    transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            return console.log('mailer error: '+ error);
        }
        //console.log('Message %s sent: %s', info.messageId, info.response);
        console.log('Sent successfully');
    });
}
// Clears db FOR TESTING PURPOSES ONLY
const resetDB = (callback) => {
    session
        .run("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r")
        .then(() => {
            session.close();
            return callback(null, 'success');
        })
        .catch((e) => {
            session.close();
            return callback(null, e);
        });
}
// Will verify email by searching for it in neo4j instance, if found, we set activated to TRUE,
// which we need to check for when accessing api
const verifyEmail = (verifyString, callback) => {
    session
        .run('MATCH (user:User {verify_email_key: {verify_email_key}}) SET user.verify_email_key = true, user.status = 1 RETURN user.verify_email_key', {
            verify_email_key: verifyString
        })
        .then((verify_email_key) => {
            session.close();
            let result_string = '';
            if (!_.isEmpty(verify_email_key)) {
                _.forEach(verify_email_key.records, (record) => {
                    result_string += record._fields + ' ';
                });
                return callback(null, result_string);
            }
            return callback(null, false)
        })
        .catch((e) => {
            session.close();
            return callback(null, JSON.stringify(e));
        });

}
// Will return profile information of a user (for a public version of a profile )
const retrieveUser = (email, api_key, callback) => {

    session
        .run('MATCH (user:User {api_access_key: { api_key } }),(userProfile:User {email: { email } })'+
        'RETURN userProfile', {
            email: email,
            api_key: api_key
        })
        .then((user) => {
          session.close();
          if(_.isEmpty(user.records)){
            return callback(null, {error:'No match found.'});
          }
            var profileObject = new Object();
            _.forEach(user.records, (record) => {
                // places each part of user properties into Object for jsonification
                profileObject.email = (record._fields[0].properties.email);
                profileObject.name = (record._fields[0].properties.name);
                profileObject.carMakeModel = (record._fields[0].properties.carMakeModel);
            })
            return callback(null, JSON.stringify(profileObject));
        })
        .catch((e) => {
            session.close();
            console.log(e);
            return callback(null, 'error! See logs');
        });
}
const retrieveMyProfile = (email, api_key, callback) => {
    session
        .run('MATCH (user:User) WHERE user.email = {email} AND user.api_access_key = {api_access_key} RETURN user', {
            email: email,
            api_access_key: api_key
        })
        .then((user) => {
            session.close();
            if(_.isEmpty(user.records))
              return callback(null, {error:'No records found!'});
            var myProfileObject = new Object();
            _.forEach(user.records, (record) => {
                myProfileObject.email = (record._fields[0].properties.email);
                myProfileObject.name = (record._fields[0].properties.name);
                myProfileObject.carMakeModel = (record._fields[0].properties.carMakeModel);
                myProfileObject.schedule = (record._fields[0].properties.schedule);
            });
            return callback(null, JSON.stringify(myProfileObject));
        })
        .catch((e) => {
            session.close();
            console.log({ error: e });
        });
}
const registerPass = (email, api_key, lotLocation, gpsLocation, price, notes, callback) => {
    // Regex for currency, tbd, need to speak to team about what is passed for price, regex --> ^\$?([0-9]{1,3},([0-9]{3},)*[0-9]{3}|[0-9]+)(\.[0-9][0-9])?$
    // Check to see if a pass node is in existence
    // If not create it, if so update it
    if(notes ==  null)
      notes = '(empty)';

    session
        .run('MATCH (pass:Pass) WHERE pass.ownerEmail = {ownerEmail} RETURN pass', {
            ownerEmail: email
        })
        .then((pass) => {
            session.close();
            if (!_.isEmpty(pass.records[0])) {
                // Update route
                // Match user with their pass node, update information, including a new pass ID
                session
                    .run('MATCH (pass:Pass), (user:User) WHERE pass.ownerEmail = {ownerEmail} AND user.api_access_key = {api_key}' +
                        'SET pass.price = {price}, pass.lotLocation = {lotLocation}, pass.gpsLocation = {gpsLocation}, pass.notes = {notes}, pass.forSale = {forSale},'+
                        'pass.id = {id} RETURN user.email AS email', {
                            ownerEmail: email,
                            price: price,
                            lotLocation: lotLocation,
                            gpsLocation: gpsLocation,
                            notes: notes,
                            api_key: api_key,
                            id: shortid.generate(),
                            forSale: true,
                            saleState: 0
                     })
                    .then((results) => {
                        session.close();
                        if (typeof results.records[0] == 'undefined')
                            return callback(false, { error: 'Pass failed to update'});
                        else
                            return callback(null, { success: 'Updated pass!'});
                    })
                    .catch((e) => {
                        session.close();
                        console.log(e);
                    })
            } else {
                // creation route
                session
                    .run('MATCH (user:User { email: {email} , api_access_key: {api_key}}) RETURN user.email AS email', {
                            email: email,
                            api_key: api_key
                    })
                    .then ((results) => {
                        session.close();
                        if (typeof results.records[0] == 'undefined')
                          return callback(false, { error: 'Pass failed to create'});
                        // Match email and api_key for finding user node to create relationship with,
                        // Create the pass node with given information, then create a relationship between pass and node, returning relationship
                        session
                            .run('MATCH (user:User { email: {email} , api_access_key: {api_key}}) ' +
                                 'CREATE (pass:Pass {id: {id}, ownerCount: 0, ownerEmail: {ownerEmail}, lotLocation: {lotLocation}, gpsLocation:{gpsLocation}, price: {price}, notes:{notes}, forSale:{forSale}})' +
                                 'CREATE (user)-[r:OWNS]->(pass) RETURN r', {
                                email: email,
                                api_key: api_key,
                                id: shortid.generate(),
                                ownerEmail: email,
                                lotLocation: lotLocation,
                                gpsLocation: gpsLocation,
                                price: price,
                                notes: notes,
                                forSale: true,
                                saleState: 0
                            })
                            .then((result) => {
                                session.close();
                                return callback(null, { success: 'created pass!' });
                            })
                            .catch((e) => {
                                session.close();
                                console.log(JSON.stringify(e));
                            });
                    });
            }
        })
        .catch((e) => {
            session.close();
            console.log({ error: e });
        });
}
const activeUsers = (keyword, api_key, callback) => {
  keyword = keyword.trim();
  // NOTE: keyword 'all' will return active users FROM ALL LOTS!
  // Whereas anything else will return a specific lot (by number) if found, we should probably implement stricter fields here
  if(keyword == 'all'){
    // search for each element of pass(rename each?), return as something else, pickup with 'get' and place into json array!
    session
      .run('MATCH (y:User {api_access_key: { api_key }}) MATCH (user:User), (pass:Pass {forSale: true})'+
            'WHERE user.email=pass.ownerEmail AND EXISTS(user.totalStars) '+
            'RETURN pass.gpsLocation AS gpsLocation, user.totalStars AS totalStars, user.numberOfRatings AS numberOfRatings,' +
            'pass.notes AS notes, pass.forSale AS forSale, pass.price AS price, toFloat(pass.ownerCount) AS ownerCount, pass.lotLocation AS lotLocation,'+
            'pass.id AS passId, pass.ownerEmail AS ownerEmail', {
        forSale: true,
        api_key: api_key
      })
      .then((results) => {
        session.close();

        if(_.isEmpty(results.records)){
          return callback(false, {error: 'No active users at this moment, try again later!'});
        }
        let passArray = new Array();
        // console.log(results.records)
        _.forEach(results.records, (record) => {
          var passObject = new Object();
          passObject.avgRating = parseFloat(record.get('totalStars')) / parseFloat(record.get('numberOfRatings'));
          passObject.gpsLocation = record.get('gpsLocation');
          passObject.notes = record.get('notes');
          passObject.forSale = record.get('forSale');
          passObject.price = record.get('price');
          passObject.lotLocation = record.get('lotLocation');
          passObject.passId = record.get('passId');
          passObject.ownerEmail = record.get('ownerEmail');
          // passObject.ownerCount = record.get('ownerCount');

          passArray.push(passObject);
        });
        return callback(true, {success: passArray});
      })
      .catch((e) => {
        session.close();
        console.log(e);
        return callback(null, {error:'Something went wrong..'});
      })
  } else {
    session
      .run('MATCH (y:User {api_access_key: { api_key }}) MATCH (user:User), (pass:Pass {forSale: true})'+
            'WHERE user.email=pass.ownerEmail AND EXISTS(user.totalStars) AND pass.lotLocation CONTAINS {lotLocation}'+
            'RETURN pass.gpsLocation AS gpsLocation, user.totalStars AS totalStars, user.numberOfRatings AS numberOfRatings,' +
            'pass.notes AS notes, pass.forSale AS forSale, pass.price AS price, toFloat(pass.ownerCount) AS ownerCount, pass.lotLocation AS lotLocation,'+
            'pass.id AS passId, pass.ownerEmail AS ownerEmail, y.email AS api_email', {
            forSale: true,
            api_key: api_key,
            lotLocation: keyword
        })
        .then((results) => {
          session.close();
          if(_.isEmpty(results.records)){
            return callback(false, {error: 'No active users in this lot or no lot match, try again later!'});
          }
          // stores
          let passArray = new Array();
          _.forEach(results.records, (record) => {
            var passObject = new Object();
            passObject.avgRating = parseFloat(record.get('totalStars')) / parseFloat(record.get('numberOfRatings'));
            passObject.gpsLocation = record.get('gpsLocation');
            passObject.notes = record.get('notes');
            passObject.forSale = record.get('forSale');
            passObject.price = record.get('price');
            passObject.lotLocation = record.get('lotLocation');
            passObject.passId = record.get('passId');
            passObject.ownerEmail = record.get('ownerEmail');
            // passObject.ownerCount = record.get('ownerCount');
            passArray.push(passObject);
          });
          return callback(true, {success:passArray});
        })
        .catch((e) => {
          session.close();
          console.log(e);
          return callback(null, {error:'Something went wrong..'});
        });
  }
}
const purchasePass = (api_key, currentOwnerEmail, passId, callback) => {
  // Increments the amount of times the pass has been sold by 1
  // console.log(`Line 505: ${api_key} ${currentOwnerEmail} ${passId}`);
  session
    .run('MATCH (user:User {api_access_key: {api_key} }), (pass:Pass {ownerEmail: {currentOwnerEmail},'+
          'id: {passId} }), (owner:User {email: {currentOwnerEmail} })'+
          'WHERE user.email <> owner.email SET pass.ownerCount = pass.ownerCount + 1 RETURN pass', {
              api_key: api_key,
              currentOwnerEmail: currentOwnerEmail,
              passId: passId
      })
      .then((results) => {
        session.close();
        if(_.isEmpty(results.records[0])){
          return callback(null,{error:'Record not found 503'});
        }
        // Finds the newOwner of the pass, the pass to be sold, and the current owner of the pass (before it is transfered)
        // Deletes the current relationship between the former (currentOwner) of the pass and the pass itself, then creates
        // a new relationship with said pass to newOwner. Finally creates a transaction node that stores relevant sale info
        session
          .run('MATCH (newOwner:User {api_access_key:{api_key} }), (pass:Pass {ownerEmail:{currentOwnerEmail}, id:{passId} }), (owner:User {email: {currentOwnerEmail} })-[r:OWNS]->(pass)'+
               'WHERE pass.id = {passId} AND NOT (newOwner)-[:OWNS]->() DELETE r '+
               'MERGE (newOwner)-[x:OWNS]->(pass) CREATE'+
                '(trans:Transaction {passId: pass.id, buyerEmail: newOwner.email, ownerEmail: pass.ownerEmail, gpsLocation: pass.gpsLocation,'+
                  'transactionTime: timestamp(), notes: pass.notes, price: pass.price}) RETURN trans', {
                    currentOwnerEmail: currentOwnerEmail,
                    api_key: api_key,
                    passId: passId
            })
            .then((results) => {
                session.close();
                // Finds newOwner of pass, updates the soldPass to reflect newOwners email.
                if(_.isEmpty(results.records[0]))
                  return callback(null,{error:'Record not found 524'});
                session
                  .run('MATCH (newOwner:User {api_access_key:{api_key} }),(exchangedPass:Pass {id:{passId} })' +
                        'SET exchangedPass.ownerEmail = newOwner.email RETURN newOwner',
                          {passId:passId, api_key:api_key})
                  .then((results) => {
                    session.close();
                    if(_.isEmpty(results.records[0])){
                        return callback(null, {error:'Record not found 531'});
                    }
                    else {
                        return callback(null, {success:'You\'ve just bought this users pass!'});
                    }
                  })
                  .catch((e) => {
                    session.close();
                    console.log(e);
                    return callback(null,'error!?');
                  });
            })
            .catch((e)=> {
                session.close();
                console.log(e);
                return callback({error:'error!?'});
             });
      })
      .catch((e) => {
        session.close();
        console.log(e);
        return callback(null,'error!?');
      });
}
const passListener = (api_key, passId, customerType, requestCount, action, callback) => {
  // two branches here 1. Buyer branch 2. Seller branches
  if(customerType == 'buyer'){
    if(requestCount == 1) {
      // console.log(`here ${api_key} ${passId} ${customerType} ${requestCount}`);
      // if it is the buyers first visit to this pass, set status of pass to salePending
      // also need to check to see if the user is first one to attempt to buy the pass (buyer will remove property pass.interestedUser once rejected!)
      let saleState = 1;
      session
        .run('MATCH (y:User {api_access_key: { api_key }}) MATCH (pass:Pass {id: {passId}})'+
              'WHERE NOT exists(pass.interestedUser) AND pass.saleState = {zeroCheck}'+
              'SET pass.saleState = {saleState}, pass.interestedUser = y.email RETURN pass.saleState AS saleState', {
               api_key: api_key,
               passId: passId,
               saleState: saleState,
               zeroCheck: 0
        })
        .then((results) => {
          if(_.isEmpty(results.records[0]))
            return callback(false, {error:'Record not found 611(pass probably in interest)'});
          return callback(true, {pending: 'You changed the state of pass, please wait for owner to respond!'});
        })
        .catch((e) => {
          console.log(`Line 608: ${JSON.stringify(e)}`);
          return callback(false, {error:'Sys error 616'});
        });
    }
    // NOTE: should set limitations, maybe 300 requests? (300 minutes)
    // If greater that 1, second or later requests, we constantly check for a change in the status until it is:
    // 0 == Which means the exchange is cancelled, or 2 == Which means that exchange is accepted, then we make exchange (call purchasePass function)
    if(requestCount > 1){
      session
        .run('MATCH (y:User {api_access_key: { api_key }})' +
             'MATCH (pass:Pass {id: {passId}})' +
             'WHERE exists(pass.saleState) AND pass.interestedUser = y.email RETURN pass.ownerEmail AS ownerEmail , pass.saleState AS saleState', {
                  api_key: api_key,
                  passId: passId
        })
        .then((results) => {
          session.close();
          if(_.isEmpty(results.records[0]))
            return callback(false, {error:'Record not found 632'});

          _.forEach(results.records, (record) => {
              if(parseInt(record.get('saleState')) == 1) // if pass owner has yet to respond to request to exchange pass
                return callback(true, {pending:'Pass has not yet updated...'});
              if(parseInt(record.get('saleState')) == 0){ // if pass owner rejects your offer to exchange, remove yourself from their pass property, end search on this pass!
                session
                  .run('MATCH (y:User {api_access_key: { api_key }}) MATCH (pass:Pass {id: {passId}}) REMOVE pass.interestedUser RETURN pass.ownerEmail AS ownerEmail',
                      {passId: passId, api_key: api_key}
                  )
                  .then((results) => {
                    session.close();
                    if(_.isEmpty(results.records[0]))
                      return callback(false, {error:'Record not found 644'});
                    return callback(true, {rejected:'Sorry, looks like your exchange has been rejected by the owner!'});
                  })
                  .catch((e) => {
                    console.log(`Line 649: ${e}`);
                    return callback(false, {error:'Sys error 649'});
                  });
              }
              if(parseInt(record.get('saleState')) == 2){ // if pass owner accepts your offer to exchange, invoke exchange method!
                  // purchasePass(api_key, record.get('ownerEmail'), passId, )
                  // instead of invocation of function that exchange pass, send back a message that tells client to now meet with counter-part
                  // (break out of listening mode)
                  return callback(true, {accepted:'Now make the exchange with the seller!'});
              }
          });
        })
        .catch((e) => {
          session.close();
          console.log(`Line 608: ${e}`);
          return callback(false, {error:'Sys error 636'});
        });
    }
  }
////// Seller section
  else if(customerType == 'seller'){
    if(action == null) {
      // optional match for user information, use interested user
      /* 'OPTIONAL MATCH (potentialBuyer:User) WHERE EXISTS(pass.interestedUser) AND potentialBuyer.email = pass.interestedUser' +
       'RETURN pass.saleState AS saleState, potentialBuyer.email AS buyerEmail, potentialBuyer.totalStars as totalStars, '+*/
        session
          .run('MATCH (x:User {api_access_key: {api_key}}) MATCH (pass:Pass {id: {passId}})' +
               'potentialBuyer.numberOfRatings AS numberOfRatings', {api_key: api_key, passId: passId})
          .then((results) => {
            session.close();
            if(_.isEmpty(results.records[0]))
              return callback(false, {error:'Record not found 681'});
            _.forEach(results.records, (record) => {
              if(parseInt(record.get('saleState')) == 0){
                console.log('Still at initial state, continue requests');
                return callback(true, {pending:'Still no buyers....'});
              }
              if(parseInt(record.get('saleState')) == 1){
                // If accepted by buyer, seller must decide on an action, send back their profile. Return rating of buyer and email
                session
                  .run('MATCH (buyer:User), (pass:Pass) WHERE pass.id = {passId} AND pass.potentialBuyer = buyer.email'+
                       'RETURN buyer.email AS buyerEmail, buyer.totalStars AS totalStars, buyer.numberOfRatings as numberOfRatings')
                  .then((results) => {
                    session.close();
                    if(_.isEmpty(results.records[0]))
                      return callback(false, {error:'Record not found 644 seller'});

                    var passObject = new Object();
                    _.forEach(results.records, (record) => {
                      passObject.avgRating = parseFloat(record.get('totalStars')) / parseFloat(record.get('numberOfRatings'));
                      passObject.email = record.get('buyerEmail');
                    });

                  })
                  .catch((e) => {
                    session.close();
                    console.log(`Line 706: ${e}`);
                    return callback(false, {error:'Sys error 706'});
                  });
                console.log('Buyer must decide on action, buyer info is sent. ');
                return callback(true, JSON.stringify(passObject));
              }
            });

          })
          .catch((e) => {
            session.close();
            console.log(`Line 718: ${e}`);
            return callback(false, {error:'Sys error 718'});
          });
    }
    if(action == 'accept'){
      // Accept the offer, then seller leaves endpoint and moves to new "completion" screen that takes place
      session
        .run('MATCH (x:User {})')
        .then()
        .close()
    }
    if(action == 'reject'){
      // Reject the offer
      session
        .run('MATCH (x:User {})')
        .then()
        .close()
    }
  }
  else {
    callback(false, {error:'Looks like you are sending incorrect parameters'});
  }
}
module.exports = {
    activeUsers,
    login,
    createUser,
    completeProfile,
    verifyEmail,
    resetDB,
    retrieveUser,
    retrieveMyProfile,
    registerPass,
    resendVerify,
    purchasePass,
    passListener
};

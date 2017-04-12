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
                        return callback(null, result_string);
                    }
                    // return nothing if no match, NOTE: Should return false
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
            console.log(results);
            if (_.isEmpty(results.records)) return callback(null, {
                error: 'incorrect api_key'
            });
            // if it exists, exit with callback error, else insert new values to current user node by api_key
            if (typeof results.records[0]._fields[0].properties.carMakeModel == 'undefined') {
                session
                    .run('MATCH (user:User {api_access_key:{api_access_key}})' +
                        'SET user.carMakeModel = {carMakeModel}, user.schedule = {schedule}' +
                        'RETURN user', {
                            api_access_key: api_key,
                            carMakeModel: carMakeModel,
                            schedule: schedule
                        })
                    .then((results) => {
                        session.close();
                        if (!_.isEmpty(results.records)) {
                            return callback(null, { success: 'Profile creation complete!!'});
                        } else {
                            return callback(null, { error: 'error'});
                        }
                    })
                    .catch((e) => {
                        session.close();
                        return callback(null, { error: e });
                    });
            } else
                return callback(null, { error: 'Looks like you have already created a profile...' });
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
            if (!_.isEmpty(results.records)) {
                // if we have records that return, this shows that email is in use, therefore fail with null
                session.close();
                return callback(null, { error: 'This email is in use! Try to login instead'});
            } else {
                // this key will be sent to users emails to verify they are csumb students!
                // it will first store a key, then a boolean value of TRUE when account has been verified
                var verifyEmailKey = cryptoRandomString(60);
                bcrypt.hash(password, 10, (err, hash) => {
                    session
                        .run("CREATE (a:User {name: {name}, email: {email}, password: {password}, " +
                            "api_access_key: {api_access_key}, verify_email_key: {verify_email_key}})", {
                                name: name,
                                email: email,
                                password: hash,
                                api_access_key: cryptoRandomString(60),
                                verify_email_key: verifyEmailKey
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
                            var out = e;
                            return callback(null, { error_output: out });
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
// send's an email to given user, as well as the email verification key required to activate an account
const sendEmail = (name, email, verifyEmailKey) => {
    // loads my custom html, converts to String, use Lodash function that inserts user info into html (lodash is freaken awesome!)
    let verifyUrl = process.env.AUTH_URL + verifyEmailKey;
    var html = fs.readFileSync(__dirname + '/email.html', 'utf-8');
    html = _.toString(html);
    var compiled = _.template(html);
    var modifiedHtml = compiled({
        'user': name,
        'url': verifyUrl
    });
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
            return console.log('mailer error: ' + error);
        }
        console.log('Message %s sent: %s', info.messageId, info.response);
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
// if Authentication key exists, grant access
const authCheck = (api_key, callback) => {
    session
        .run('MATCH (user:User {api_access_key: {api_key}}) RETURN user', {
            api_key: api_key
        })
        .then((user) => {
            session.close();
            if (!_.isEmpty(user)) {
                return callback(null, user);
            }
            return callback(null, false);
        })
        .catch((e) => {
            return callback(null, JSON.stringify(e));
        });
}
// Will verify email by searching for it in neo4j instance, if found, we set activated to TRUE,
// which we need to check for when accessing api
const verifyEmail = (verifyString, callback) => {
    session
        .run('MATCH (user:User {verify_email_key: {verify_email_key}}) SET user.verify_email_key = true RETURN user.verify_email_key', {
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
// Will return profile information of a user
// NOTE: need to specify exactly what to return and update return values for if none found!
const retrieveUser = (email, callback) => {
    session
        .run('MATCH (user:User {email:{email}}) RETURN user', {
            email: email
        })
        .then((user) => {
            var profileObject = new Object();
            session.close();
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
            return callback(null, JSON.stringify(e));
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
const registerPass = (email, api_key, lotLocation, price, notes, callback) => {
    // Regex for currency, tbd, need to speak to team about what is passed for price, regex --> ^\$?([0-9]{1,3},([0-9]{3},)*[0-9]{3}|[0-9]+)(\.[0-9][0-9])?$
    // Check to see if a pass node is in existence
    // If not create it, if so update it
    session
        .run('MATCH (pass:Pass) WHERE pass.ownerEmail = {ownerEmail} RETURN pass', {
            ownerEmail: email
        })
        .then((pass) => {
            session.close();
            if (!_.isEmpty(pass.records[0])) {
                // update route
                // console.log('updating...');
                // Match user with their pass node, update information.
                session
                    .run('MATCH (pass:Pass), (user:User) WHERE pass.ownerEmail = {ownerEmail} AND user.api_access_key = {api_key}' +
                        'SET pass.price = {price}, pass.lotLocation = {lotLocation}, pass.notes = {notes} RETURN user.email AS email', {
                            ownerEmail: email,
                            price: price,
                            lotLocation: lotLocation,
                            notes: notes,
                            api_key: api_key
                        })
                    .then((results) => {
                        session.close();
                        // console.log(results);
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
                // console.log('creating...');
                session
                    .run('MATCH (user:User { email: {email} , api_access_key: {api_key}}) RETURN user.email AS email', {
                            email: email,
                            api_key: api_key
                    }) //record.get("knight_id
                    .then ((results) => {
                        if (typeof results.records[0] == 'undefined')
                          return callback(false, { error: 'Pass failed to create'});
                        // Match email and api_key for finding user node to create relationship with,
                        // Create the pass node with given information, then create a relationship between pass and node, returning relationship
                        session
                            .run('MATCH (user:User { email: {email} , api_access_key: {api_key}}) ' +
                                  'CREATE (pass:Pass {id: {id}, ownerEmail: {ownerEmail}, lotLocation: {lotLocation}, price: {price}, notes:{notes}, forSale:{forSale}})' +
                                    'CREATE (user)-[r:OWNS]->(pass) RETURN r', {
                                email: email,
                                api_key: api_key,
                                id: shortid.generate(),
                                ownerEmail: email,
                                lotLocation: lotLocation,
                                price: price,
                                notes: notes,
                                forSale: false
                            })
                            .then((result) => {
                                // console.log(result);
                                // console.log('Relationship created');
                                return callback(null, { success: 'created pass!' });
                                session.close();
                            })
                            .catch((e) => {
                                console.log(JSON.stringify(e));
                                session.close();
                            });
                    });
            }
        })
        .catch((e) => {
            session.close();
            console.log({ error: e });
        });
}
// if using reset, add variable below!
module.exports = {
    login,
    createUser,
    completeProfile,
    authCheck,
    verifyEmail,
    resetDB,
    retrieveUser,
    retrieveMyProfile,
    registerPass
};

/*
  Handles all db operations
*/
var neo4j = require('neo4j-driver').v1;
//----> Local credentials
// var driver = neo4j.driver("bolt://localhost:3001", neo4j.auth.basic("neo4j", "root"));
//
// ---> Credentials for connecting to GRAPHENEDB with Heroku!
var graphenedbURL = process.env.GRAPHENEDB_BOLT_URL;
var graphenedbUser = process.env.GRAPHENEDB_BOLT_USER;
var graphenedbPass = process.env.GRAPHENEDB_BOLT_PASSWORD;
var driver = neo4j.driver(graphenedbURL, neo4j.auth.basic(graphenedbUser, graphenedbPass));
//
var session = driver.session();
var bcrypt = require('bcryptjs');
var _ = require('lodash');
const cryptoRandomString = require('crypto-random-string');
const env = require('env2')('./.env');
const nodemailer = require('nodemailer');
var fs = require('fs');

// Takes email and password, searches neo4j db for them, if found, returns user data
const findByEmailPw = (email, password, callback) => {
    let cqlString =
      "MATCH (a:User) WHERE a.email = {email} RETURN " +
       "a.name AS name, a.email AS email, a.password AS password," +
        "a.api_access_key AS api_key, a.verify_email_key AS verify_email_key";

    session
        .run(cqlString , {
            email: email
        })
        .then((result) => {
          session.close();
            if(!_.isEmpty(result.records)) {
              var userObj = new Object();
              let stored_pw = '';
            _.forEach(result.records, (record) => {
              // Prints the password field console.log(record._fields[2]);
              userObj.name = record._fields[0];
              userObj.email = record._fields[1];
              stored_pw = record._fields[2];
              userObj.api_key = record._fields[3];
              userObj.email_verified = record._fields[4];
              // Make this false for client, if false, tell user to verify email!
              if(_.isString(userObj.email_verified)){
                userObj.email_verified = false;
              }

            });
            // converts the object into a Json string for client
            var result_string = JSON.stringify(userObj);
            // compares entered password with stored_pw in database.
            bcrypt.compare(password, stored_pw, function(err, res) {
                if (res == true) {
                    return callback(null, result_string);
                }
                // return nothing if no match, NOTE: Should return false
                return callback(null, false);
            });
          } else {
            // user Does Not exist. NOTE: Should return nothing null, this is ok.
            return callback(null,null);
          }
        })
        .catch((e) => {
            session.close();
            console.log(e);
            return callback('error caught via Login ->: ' + JSON.stringify(e));
        });
}

const createUser = (email, name, password, callback) => {
    // next step -> run a match for looking at email/authkey, if it finds it, then we return failure, if not, then we create user.

    const emailRegex = /^[a-zA-Z0-9_.+-]+@(?:(?:[a-zA-Z0-9-]+\.)?[a-zA-Z]+\.)?(csumb)\.edu$/;
    const nameRegex = /^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$/u;
    const passRegex = /^([a-zA-Z0-9@*#]{8,15})$/;
    if(!emailRegex.test(email)){
      return callback(null, {error:'Incorrect email format, it must be from CSUMB!'});
    }
    if(!nameRegex.test(name)){
      return callback(null,{error: 'Incorrect Name format!'});
    }
    if(!passRegex.test(password)){
      return callback(null,{error: 'Incorrect password format! Must be minimum 8 characters!'});
    }

    session
      .run('MATCH (user:User {email: {email}}) RETURN user', {email: email})
      .then((results) => {
        if(!_.isEmpty(results.records)){
          // if we have records that return, this shows that email is in use, therefore fail with null
          session.close();
          return callback(null, {error :'This email is in use! Try to login instead'});
        }
        else {
          // this key will be sent to users emails to verify they are csumb students!
          // it will first store a key, then a boolean value of TRUE when account has been verified
          var verifyEmailKey = cryptoRandomString(60);
          bcrypt.hash(password, 10, (err, hash) => {
            session
                .run("CREATE (a:User {name: {name}, email: {email}, password: {password},"+
                      " api_access_key: {api_access_key}, verify_email_key: {verify_email_key} })", {
                    name: name,
                    email: email,
                    password: hash,
                    api_access_key: cryptoRandomString(60),
                    verify_email_key: verifyEmailKey
                })
                .then(() => {
                    session.close();
                    successObject = new Object();
                    successObject.success = "Creation successful! To continue, check your email to verify account!";
                    // sends email to user with instructions to verify email, without it no access to Ottershare
                    sendEmail(name, email, verifyEmailKey);
                    return callback(null, JSON.stringify(successObject));
                })
                .catch((e) => {
                    session.close();
                    return callback(null, {error:e});
                });

          });

        }
      })
      .catch((e) => {
          session.close();
          return callback(null, {error:e});
      });
}
// send's an email to given user, as well as the email verification key required to activate an account
const sendEmail = (name, email, verifyEmailKey) => {
    // loads my custom html, converts to String, use Lodash function that inserts user info into html (lodash is freaken awesome!)
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
const auth = (api_access_key, callback) => {
  session
    .run('MATCH (user:User {api_access_key: {api_access_key}}) RETURN user', {api_access_key: api_access_key})
    .then((user) => {
      session.close();
      if(!_.isEmpty(user)) {
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
    .run('MATCH (user:User {verify_email_key: {verify_email_key}}) SET user.verify_email_key = true RETURN user.verify_email_key',
        {verify_email_key:verifyString})
    .then((verify_email_key) => {
      session.close();
      let result_string = '';
      if(!_.isEmpty(verify_email_key)) {
        _.forEach(verify_email_key.records, (record) => {
          // Prints the password field console.log(record._fields[3]);
          result_string += record._fields + ' ';
        });
        return callback(null,result_string);
      }
      return callback(null, false)
    })
    .catch((e) => {
      session.close();
      return callback(null, JSON.stringify(e));
    });

}
// if using reset, add variable below!
module.exports = {
    findByEmailPw,
    createUser,
    auth,
    verifyEmail,
    resetDB
};

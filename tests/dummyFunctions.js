/**
    dummyFunctions.js
    Purpose: Create testing objects and quickly dispose of them after tests are finished.
**/
"use strict";
var neo4j = require('neo4j-driver').v1;
// ---> Credentials for connecting to GRAPHENEDB with Heroku!
var graphenedbURL = process.env.GRAPHENEDB_BOLT_URL || "bolt://localhost:3001";
var graphenedbUser = process.env.GRAPHENEDB_BOLT_USER || "neo4j";
var graphenedbPass = process.env.GRAPHENEDB_BOLT_PASSWORD || "root";
var driver = neo4j.driver(graphenedbURL, neo4j.auth.basic(graphenedbUser, graphenedbPass));

const fakeApiKey = 'api-key-yo!';

//
var session = driver.session();
//MATCH (n:User),(p:Pass) WHERE n.email = p.ownerEmail AND n.email STARTS WITH 'TEST' DETACH DELETE n,p;
const createDummyUser = (email, name, password, carMakeModel, schedule, callback) => {
  session
      .run("CREATE (a:User {name: {name}, email: {email}, password: {password}, " +
          "api_access_key: {api_access_key}, verify_email_key: {verify_email_key}, " +
            "carMakeModel: {carMakeModel}, schedule: {schedule}})", {
              name: name,
              email: email,
              password: password,
              api_access_key: fakeApiKey,
              verify_email_key: true,
              carMakeModel: carMakeModel,
              schedule: schedule,
        })
      .then(() => {
          session.close();
          return callback(null, 'worked');
      })
      .catch((e) => {
          session.close();
          var out = e;
          return callback(null, out);
      });
}
const wipeTestData = (callback) => {
  session.
      run()
}
module.exports = {
    createDummyUser,
    wipeTestData
};

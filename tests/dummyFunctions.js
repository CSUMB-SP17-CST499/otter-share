/**
    dummyFunctions.js
    Purpose: Create testing objects and quickly dispose of them after tests are finished.
**/
"use strict";
var neo4j = require('neo4j-driver').v1;
// ---> Credentials for connecting to GRAPHENEDB with Heroku.
var graphenedbURL = process.env.GRAPHENEDB_BOLT_URL || "bolt://localhost:3001";
var graphenedbUser = process.env.GRAPHENEDB_BOLT_USER || "neo4j";
var graphenedbPass = process.env.GRAPHENEDB_BOLT_PASSWORD || "root";
var driver = neo4j.driver(graphenedbURL, neo4j.auth.basic(graphenedbUser, graphenedbPass));

const fakeApiKey = 'api-key-yo!';
var session = driver.session();
const registerDummyPasses = (email, api_key, passId, callback) => {
  session
      .run('MATCH (user:User { email: {email} , api_access_key: {api_key}}) ' +
            'CREATE (pass:Pass {id: {id}, ownerCount: 0, ownerEmail: {ownerEmail}, lotLocation: {lotLocation},'+
            'gpsLocation:{gpsLocation}, price: {price}, notes:{notes}, forSale:{forSale}})' +
            'CREATE (user)-[r:OWNS]->(pass) RETURN r', {
          email: email,
          api_key: api_key,
          id: passId+= passId,
          ownerEmail: email,
          lotLocation: 'random stuff',
          gpsLocation: 'more random stuff',
          price: '900.00',
          notes: 'My pass is better than yours',
          forSale: true
      })
      .then((result) => {
          session.close();
          return callback(null, { success: 'created pass!' });
      })
      .close((e) => {
        session.close();
        return callback(null, { error:e });
      });
}

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
              schedule: schedule
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
  session
      .run("MATCH (n:User),(p:Pass) WHERE n.email = p.ownerEmail AND n.email STARTS WITH 'test' DETACH DELETE n,p ")
      .then(() => {
        session.close();
        session.run("MATCH (x:User) WHERE x.email STARTS WITH 'test' DETACH DELETE x");
        return callback(null, 'Cleaned db');
      })
      .catch((e) => {
          session.close();
          var out = e;
          return callback(null, out);
      });
}
module.exports = {
    createDummyUser,
    registerDummyPasses,
    wipeTestData
};

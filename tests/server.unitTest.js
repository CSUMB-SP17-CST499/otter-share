"use strict";
const request = require('supertest');
const expect = require('expect');
const app = require('../server.js').app;
const cryptoRandomString = require('crypto-random-string');
const env = require('env2')('./.env');
const dummyFunctions = require('./dummyFunctions.js');

var testEmail = 'test' + cryptoRandomString(8) + '@csumb.edu';
var testName = 'Bobby Brown';
var testPassword = cryptoRandomString(10);
var testCarMakeModel = 'Toyota Corolla';
var testSchedule = 'Bunch of data !';
var testLocation = 'Dagobah'
var testPrice = '2.50'
var testNotes = 'My pass is better than yours, bro.'

// Tests root endpoint
it('should return root response returning info about site', (done) => {
    request(app)
        .get('/')
        .expect('Welcome to OtterShare, nothing to GET, though')
        .end(done);
});

// Tests the createUser endpoint with good credentials
it('should return success on creation.', (done) => {
    request(app)
        .post('/createUser')
        .send('name=' + testName)
        .send('email=' + testEmail)
        .send('password=' + testPassword)
        .expect((res) => {
            expect(res.header['content-type']).toEqual('application/json; charset=utf-8');
            expect(res.body).toMatch({
                success: /successful/
            })
        })
        .end(done);
});
// tests createUser with bad email
it('should return fail on creation.', (done) => {
    request(app)
        .post('/createUser')
        .send('name=' + testName)
        .send('email=' + 'cjone45s847728@gmail.com')
        .send('password=' + testPassword)
        .expect((res) => {
            expect(res.header['content-type']).toEqual('application/json; charset=utf-8');
            expect(res.body).toMatch({
                error: /Incorrect email format/
            })
        })
        .end(done);
});
// tests createUser with bad password.
it('should return fail on creation with bad format of password.', (done) => {
    request(app)
        .post('/createUser')
        .send('name=' + testName)
        .send('email=' + 'g' + testEmail)
        .send('password=' + 'bob')
        .expect((res) => {
            expect(res.body).toMatch({
                error: /Incorrect password format/
            })
        })
        .end(done);
});
// Tests login with unverified email
it('should not allow login without email being verified!', (done) => {
    request(app)
        .post('/login')
        .send('email=' + testEmail)
        .send('password=' + testPassword)
        .expect((res) => {
            expect(res.header['content-type']).toEqual('application/json; charset=utf-8');
            expect(res.body).toMatch({
                error: /.*/
            });
        })
        .end(done);
});
// Tests login with verified email address
it('should allow login with verified email address !', (done) => {
    request(app)
        .post('/login')
        .send('email=' + process.env.TEST_EMAIL)
        .send('password=' + process.env.TEST_PASS)
        .expect((res) => {
            expect(res.header['content-type']).toEqual('application/json; charset=utf-8');
            expect(res.body).toMatch({
                email_verified: true
            });
        })
        .end(done);
});
// Tests login with invalid password
it('should not allow login with incorrect password', (done) => {
    request(app)
        .post('/login')
        .send('email=' + process.env.TEST_EMAIL)
        .send('password=' + process.env.TEST_PASS + 'y')
        .expect((res) => {
            expect(res.header['content-type']).toEqual('application/json; charset=utf-8');
            expect(res.body).toMatch({
              error: /Code/
            });
        })
        .end(done);
});
// Tests the fetching of certain public profile NOTE: will need to update this if test user is removed from neo4j instance
it('should retrieve another user\'s profile', (done) => {
  request(app)
    .post('/users')
    .send('email=' + process.env.TEST_EMAIL)
    .send('api_key=' + process.env.TEST_API)
    .expect((res) => {
      expect(res.body).toMatch({
        carMakeModel: /.*/
      });
    })
    .end(done);

});
// Tests the fetching of certain public profile NOTE: will need to update this if test user is removed from neo4j instance
it('should retrieve own personal profile', (done) => {
  request(app)
    .post('/myProfile')
    .send('email=' + process.env.TEST_EMAIL)
    .send('api_key=' + process.env.TEST_API)
    .expect((res) => {
      expect(res.body).toMatch({
        schedule: /.*/
      });
    })
    .end(done);
});
// Updates test users pass-node
it('should update a pass node', (done) => {
  request(app)
    .post('/registerPass')
    .send('email=' + process.env.TEST_EMAIL)
    .send('api_key=' + process.env.TEST_API)
    .send('lotLocation='+ testLocation)
    .send('price='+ testPrice)
    .send('notes='+ testNotes)
    .expect((res) => {
      expect(res.body).toMatch({
        success: /.*/
      });
    })
    .end(done);
});
// need to implement clean up function that deletes all TEST users, test for actual creation of pass, will need a dummy users
it('should create a pass node', (done) => {
  const fakeApiKey = 'api-key-yo!';
  dummyFunctions.createDummyUser(testEmail, testName, testPassword, testCarMakeModel, testSchedule, (succ, err) => {
    request(app)
      .post('/registerPass')
      .send('email=' + testEmail)
      .send('api_key=' + fakeApiKey)
      .send('lotLocation='+ testLocation)
      .send('price='+ testPrice)
      .send('notes='+ testNotes)
      .expect((res) => {
        expect(res.body).toMatch({
          success: /created/
        });
      })
      .end(done);
  });
});
// clean up of test users.
it('should wipe the database of test users', (done) => {
  const fakeApiKey = 'api-key-yo!';
  dummyFunctions.wipeTestData((err, succ) => {
    if(err)
      throw Error('err: ' + err);
    request(app)
      .post('/')
      .end(done);
  });
});

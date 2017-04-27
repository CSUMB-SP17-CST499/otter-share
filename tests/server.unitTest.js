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
var testGPS = "198.367.258.150"
const fakeApiKey = 'api-key-yo!';


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
    .send('lotLocation='+ '201')
    .send('price='+ testPrice)
    .send('notes='+ testNotes)
    .send('gpsLocation=' + testGPS)
    .expect((res) => {
      expect(res.body).toMatch({
        success: /.*/
      });
    })
    .end(done);
});
// need to implement clean up function that deletes all TEST users, test for actual creation of pass, will need a dummy users
it('should create a pass node', (done) => {
  dummyFunctions.createDummyUser(testEmail, testName, testPassword, testCarMakeModel, testSchedule, (succ, err) => {
    request(app)
      .post('/registerPass')
      .send('email=' + testEmail)
      .send('api_key=' + fakeApiKey)
      .send('lotLocation='+ testLocation)
      .send('gpsLocation='+ testGPS)
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
it('should limit user\'s from sending multiple verification emails', (done) => {
  request(app)
    .post('/resendEmail')
    .send('email=' + testEmail)
    .expect((res) => {
      expect(res.body).toMatch({
        error: /later/
      })
    })
    .end(done);
});

// Retrieves all actives sellers of passes
it('should retrieve all active passes', (done) => {
  request(app)
    .post('/activeUsers')
    .send('api_key='+ fakeApiKey)
    .send('keyword=' + 'all')
    .expect((res) => {
      expect(res.body).toMatch({
        success: /.*/
      })
    })
    .end(done);
});
// Retireves sellers from a particular parking lot
it('should retrieve all active passes in a specific lot', (done) => {
  request(app)
    .post('/activeUsers')
    .send('api_key='+ fakeApiKey)
    .send('keyword=' + '201')
    .expect((res) => {
      expect(res.body).toMatch({
        success: /201/
      })
    })
    .end(done);
});
// Tests with random api key
it('should return with an error with incorrect api-key', (done) => {
  request(app)
    .post('/activeUsers')
    .send('api_key='+ fakeApiKey + fakeApiKey)
    .send('keyword=' + '201')
    .expect((res) => {
      expect(res.body).toMatch({
        error: /.*/
      })
    })
    .end(done);
});
// Tests with fake lot location
it('should return with an error with non-existant lot location', (done) => {
  request(app)
    .post('/activeUsers')
    .send('api_key='+ fakeApiKey)
    .send('keyword=' + 'hjgj65')
    .expect((res) => {
      expect(res.body).toMatch({
        error: /.*/
      })
    })
    .end(done);
});
// clean up of test users.
it('should wipe the database of test users', (done) => {
  dummyFunctions.wipeTestData((err, succ) => {
    if(err)
      throw Error('err: ' + err);
    request(app)
      .post('/')
      .end(done);
  });
});

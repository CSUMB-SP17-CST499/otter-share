const request = require('supertest');
const expect = require('expect');
const app = require('../server.js').app;
const cryptoRandomString = require('crypto-random-string');
const env = require('env2')('./.env');

var testEmail = cryptoRandomString(8) + '@csumb.edu';
var testName = 'Bobby Brown';
var testPassword = cryptoRandomString(10);

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
        .send('email=' + 'cjones847728@gmail.com')
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
                error: /Incorrect password/
            });
        })
        .end(done);
});
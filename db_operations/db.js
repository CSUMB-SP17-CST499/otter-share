/*
  Handles all db operations

*/
var neo4j = require('neo4j-driver').v1;
//----> Local credentials
//var driver = neo4j.driver("bolt://localhost:7687", neo4j.auth.basic("neo4j", "root"));
//
// ---> Credentials for connecting to GRAPHENEDB with Heroku!
var graphenedbURL = process.env.GRAPHENEDB_BOLT_URL;
var graphenedbUser = process.env.GRAPHENEDB_BOLT_USER;
var graphenedbPass = process.env.GRAPHENEDB_BOLT_PASSWORD;
var driver = neo4j.driver(graphenedbURL, neo4j.auth.basic(graphenedbUser, graphenedbPass));
//
var session = driver.session();
var bcrypt = require('bcrypt');

//Takes email and password, searches neo4j db for them, if found, returns name, location and hashed pw -> FOR TESTING ONLY! need to remove!
const findByEmailPw = (email, password, callback) => {
    session
        .run("MATCH (a:User) WHERE a.email = {email} RETURN a.name AS name, a.email AS email, a.location AS location, a.password AS password", {
            email: email
        })
        .then((result) => {
            let result_string = '';
            let hash = null;
            result.records.forEach((record) => {
                // Prints the password field console.log(record._fields[3]);
                hash = record._fields[3];
                result_string += record._fields + ' ';
            });
            console.log('then ' + result_string);
            bcrypt.compare(password, hash, function(err, res) {
                if (res == true) {
                    return callback(null, result_string);
                }
                return callback(null, null);
            });
        })
        .catch((e) => {
            console.log(e);
            return callback('error : ' + JSON.stringify(e));
        });
}
// need to develop auth key here, before insertion (creation) of user.
const createUser = (email, name, location, password, callback) => {
    // next step -> run a match for looking at email/authkey, if it finds it, then we return failure, if not, then we create user.
    session
        .run("CREATE (a:User {name: {name}, email: {email}, password: {password} ,location: {location} })", {
            name: name,
            email: email,
            password: password,
            location: location
        })
        .then(() => {
            let succ = ("Success! ---> " + name + ' ' + email + ' ' + location + ' ' + password);
            return callback(null, succ);
        })
        .catch((e) => {
            return callback('error : ' + JSON.stringify(e));
        });
}

const resetDB = (callback) => {
    session
        .run("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r")
        .then(() => {
            return callback(null, 'success');
        })
        .catch((e) => {
            return callback(null, e);
        });
}
module.exports = {
    findByEmailPw,
    createUser,
    resetDB
};

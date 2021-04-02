const mysql = require("mysql2");
const dbConn = mysql.createPool({
    host: "localhost",
    user: "root",
    password: "root",
    database: "User"
}).promise()

module.exports = dbConn;
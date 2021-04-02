const express = require('express');
const path = require('path');
const cookie = require('cookie-session');
const bcrypt = require('bcrypt');
const dbConn = require('./db');
const { body, validationResult } = require('express-validator');

const app = express();
app.use(express.urlencoded({ extended: false}))

app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(cookie({
    name: 'session',
    keys: ['key1', 'key2'],
    maxAge: 3600*1000
}))

const ifNotLoggedIn = (req, res, next) =>{
    if(!req.session.isLoggedIn) {
        return res.render('login-register');
    }
    next();
}

const ifLoggedIn = (req, res, next) =>{
    if(req.session.isLoggedIn) {
        return res.render('home');
    }
    next();
}

app.get('/', ifNotLoggedIn, (req, res, next) => {
    dbConn.execute("SELECT name FROM users WHERE id = ?", [req.session.userID])
    .then(([rows]) => {
        res.render('home', {
            name: rows[0].name
        })
    })
})

app.post('/register', ifLoggedIn, [
    body('user_name', 'Username is empty').trim().not().isEmpty().custom((value) => {
        return dbConn.execute('SELECT name FROM users WHERE name = ?', [value])
        .then(([rows]) => {
            if (rows.length > 0) {
                return Promise.reject('This name already registered');
            }
        })
    }),
    body('user_pass', "Password must be 6-12 characters").trim().isLength({min:6}).isLength({max:12}),
    body('user_type', "Please select one").not().isEmpty(),
    body('user_org', "Please select one").not().isEmpty()
],
    (req, res, next) => {
        const validation_result = validationResult(req);
        const { user_name, user_pass, user_type, user_org } = req.body;

        if (validation_result.isEmpty()){
            bcrypt.hash(user_pass, 12).then((hash_pass) => {
                dbConn.execute("INSERT INTO users (name, password, type, organization) VALUES(?, ?, ?, ?)", [user_name, hash_pass, user_type, user_org])
                .then(result => {
                    res.send('Your account has been create successfully, Now you can go back to <a href="/">login</a>')
                }).catch(err => {
                    if (err) throw err;
                })
            }).catch(err => {
                if (err) throw err;
            })
        } else {
            let allErrors = validation_result.errors.map( error => {
                return error.msg;
            })

            res.render('login-register', {
                register_error: allErrors,
                old_data: req.body
            })
        }
    }
)

app.post('/', ifLoggedIn, [
    body('user_name').custom((value) => {
        return dbConn.execute("SELECT name FROM users WHERE name = ?", [value])
        .then(([rows]) =>{
            if (rows.length == 1) {
                return true;
            }
            return Promise.reject('Invalid Username!');
        })
    }),
    body('user_pass', 'Password is empty!').trim().not().isEmpty()
], (req, res) => {
    const validation_result = validationResult(req);
    const { user_pass, user_name } = req.body;
    if (validation_result.isEmpty()) {
        dbConn.execute("SELECT * FROM users WHERE name = ?", [user_name])
        .then(([rows]) => {
            bcrypt.compare(user_pass, rows[0].password)
            .then(compare_result => {
                if( compare_result == true ) {
                    req.session.isLoggedIn = true;
                    req.session.userID = rows[0].id;
                    res.redirect('/');
                } else {
                    res.render('login-register' , {
                        login_errors: ['Invalid Password']
                    })
                }
            }).catch(err => {
                if (err) throw err;
            })

        }).catch(err => {
            if (err) throw err;
        })
    } else {
        let allErrors = validation_result.errors.map( error => {
            return error.msg;
        })

        res.render('login-register', {
            login_error: allErrors,
        })
    }
})

app.get('/logout', (req, res) =>{
    req.session = null;
    res.redirect('/');
})

app.use('/', (req, res) => {
    res.status(404).send('<h1>404 Page Not Found!</h1>')
})

app.listen(3000, () => console.log("Server is running on localhost:3000..."))

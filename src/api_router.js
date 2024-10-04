const express = require('express');
const api = require('./routes/api');
// const passport = require('passport');
// const authx = require('@appcoe/lib-authx');

const api_router = express.Router();

/* Test route */
api_router.get("/health-check", api.healthCheck)

/* Test route */

module.exports = api_router;

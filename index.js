const express = require('express');
const http = require('http');
const router = express.Router();
const api_router = require('./src/api_router');
const app = express();

router.use('/node-back', api_router);

app.use(router);

app.server = http.createServer(app);

console.log(`Starting listening on port ${process.env.PORT || 5000}`);

app.server.listen(process.env.PORT || 5000);

// authenticationEvents.on('login', ctx.populateUserInfo);

module.exports = app;

// module.exports = {
//   handler: serverless(app)
// };
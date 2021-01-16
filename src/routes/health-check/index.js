// const logger = require('@appcoe/lib-logger')('routes:healthcheck');

module.exports = (req, res) => {
    //   logger.debug('healthcheck controller activated');
    console.log('healthcheck controller activated');
    res.json({
        healthy: true,
        timestamp: new Date().toISOString()
    });
};
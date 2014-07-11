package grails.plugin.runtimelogging

import org.slf4j.LoggerFactory

/**
 * All logging implementation stuff should go in this service or, at least, NOT in the controller.
 *
 * TODO separate into different service implementations that don't need to continually do Class.forName.
 */

class LogAdapterService {
    static transactional = false

    def setLoggerLevel(String loggerName, String levelName) {
        def level
        def logger

        withLoggerClass({
            logger = loggerName ? it.getLogger(loggerName) : it.getRootLogger()
            level = Class.forName('org.apache.log4j.Level').toLevel(levelName)
            logger.level = level

        }, {
            logger = loggerName ? LoggerFactory.getLogger(loggerName) : LoggerFactory.getLogger(it.ROOT_LOGGER_NAME)
            level = Class.forName('ch.qos.logback.classic.Level').toLevel(levelName)
            logger.level = level
        })

        return [logger: loggerName, level: level]
    }

    String getEffectiveLevel(String loggerName) {
        withLoggerClass({
            it.getLogger(loggerName).getEffectiveLevel()
        }, {
            LoggerFactory.getLogger(loggerName).getEffectiveLevel()
        })
    }

    def withLoggerClass(log4jClosure, logBackClosure) {
        def loggerClass, loggerClosure;
        try {
            loggerClass = Class.forName('org.apache.log4j.Logger')
            loggerClosure = log4jClosure
        }
        catch (ClassNotFoundException e1) {
            try {
                loggerClass = Class.forName('ch.qos.logback.classic.Logger')
                loggerClosure = logBackClosure
            }
            catch (e2) { /** yep... just ignore it **/}
        }

        loggerClosure(loggerClass)
    }}

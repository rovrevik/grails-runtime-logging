package grails.plugin.runtimelogging

import grails.util.GrailsUtil
import org.slf4j.LoggerFactory;
import org.codehaus.groovy.grails.commons.GrailsClass

/**
 * Grails Plugin to control log4j logging at runtime, and to simplify persistent
 * configuration though Config.groovy
 *
 * @author Jason Morris (jason.morris@torusit.com)
 * modified by Tom Dean (tom.dean@convergys.com)
 */
class RuntimeLoggingController {

	static final List grailsLogs = [
		[name: 'Grails Application',  logger: 'grails.app'],
		[name: 'Controllers',         logger: 'grails.app.controller'],
		[name: 'Services',            logger: 'grails.app.service'],
		[name: 'Domains',             logger: 'grails.app.domain'],
		[name: 'Filters',             logger: 'grails.app.filters'],
		[name: 'TagLibs',             logger: 'grails.app.taglib'],
		[name: 'Grails Web Requests', logger: 'org.codehaus.groovy.grails.web'],
		[name: 'URL Mappings',        logger: 'org.codehaus.groovy.grails.web.mapping'],
		[name: 'Plugins',             logger: 'org.codehaus.groovy.grails.plugins'],
		[name: 'Apache Commons',      logger: 'org.codehaus.groovy.grails.commons'],
	]

	static {
		if (!GrailsUtil.getGrailsVersion().startsWith('1')) {
			grailsLogs[1].logger += 's' // controller -> controllers in 2.0
			grailsLogs[2].logger += 's' // service -> services in 2.0
		}
	}

	static final List otherLogs = [
		[name: 'Spring',    logger: 'org.springframework'],
		[name: 'SQL',       logger: 'org.hibernate.SQL'],
		[name: 'Hibernate', logger: 'org.hibernate']
	]

	// By default render the standard "chooser" view
	def index = {
		def domainLoggers = buildArtefactLoggerMapList("Domain")
		addCurrentLevelToLoggerMapList(domainLoggers)

		def controllerLoggers = buildArtefactLoggerMapList("Controller")
		addCurrentLevelToLoggerMapList(controllerLoggers)

		def serviceLoggers = buildArtefactLoggerMapList("Service")
		addCurrentLevelToLoggerMapList(serviceLoggers)

		def grailsLoggers = grailsLogs.collect { it.clone() }
		addCurrentLevelToLoggerMapList(grailsLoggers)

		def otherLoggers = otherLogs.collect { it.clone() }
		addCurrentLevelToLoggerMapList(otherLoggers)

		render(view: 'logging',
		       model: [
		          controllerLoggers: controllerLoggers,
		          serviceLoggers: serviceLoggers,
		          domainLoggers: domainLoggers,
		          grailsLoggers: grailsLoggers,
		          otherLoggers: otherLoggers
				 ])
	}

	// Sets the log level based on parameter values
	def setLogLevel = {
        def names = setLoggerLevel(params.logger, params.level)
        def loggerName = names.logger
        def level = names.level

		log.info "Logger $loggerName set to level $level"

		// Produce and render equivalent Config.groovy script
		String tail = loggerName.replaceFirst("[^\\.]*\\.", "")
		String head = loggerName.replaceAll("\\..*", "")

		String loggerConfig = """\
      grails {
         $tail="$level"
      }"""
		render view:'confirm',
		       model: [logger: loggerName, level: level, loggerConfig: loggerConfig]
	}

	private void addCurrentLevelToLoggerMapList(List loggerMapList) {
		loggerMapList.each {
			it.name = "${it.name} - ${getEffectiveLevel(it.logger)}"
		}
	}

	private String classCase(String s) {
		// Effectively ucFirst() i.e. first letter of artefacts must be upper case
		if (!s) {
			return s
		}

		String head = s[0].toUpperCase()
		String tail = (s.length() > 1 ? s.substring(1) : "")

		"$head$tail"
	}

	private String calculateLoggerName(String name, String artefactType) {
    	// Domains just use the artefact name, controllers/services etc need "Controller"/"Service" etc appended
		artefactType.toLowerCase() == "domain" ? "${classCase(name)}" : "${classCase(name)}${artefactType}"
	}

	private List buildArtefactLoggerMapList(String artefactType) {
		def logMapList = []
		boolean grails1 = GrailsUtil.getGrailsVersion().startsWith('1')
		for (GrailsClass gc in grailsApplication.getArtefacts(artefactType).sort { it.fullName }) {
			try {
				String logger = grails1 ?
					"grails.app.${artefactType.toLowerCase()}.${calculateLoggerName(gc.logicalPropertyName, artefactType)}" :
					gc.clazz.log.name
				logMapList << [name: gc.fullName, logger: logger]
			}
			catch (MissingPropertyException e) {
				// ignore it and go on
			}
		}
		return logMapList
	}

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
    }
}

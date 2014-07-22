package grails.plugin.runtimelogging

import grails.converters.JSON
import grails.util.GrailsUtil
import groovy.time.TimeCategory
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsClass
import org.apache.commons.lang.RandomStringUtils


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

    def logTailingService

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

		String loggerName = params.logger
		Level level = Level.toLevel(params.level)

		Logger logger = loggerName ? Logger.getLogger(loggerName) : Logger.getRootLogger()
		logger.level = level

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
			it.name = "${it.name} - ${Logger.getLogger(it.logger).getEffectiveLevel()}"
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

    //
    // log tailing
    //

    def tail = {
        render(view:'tail')
    }

    def tail_getLogData() {
        def logData
        if (params.startFromLines) {
            logData = getLatestLogItemsByLines(params.startFromLines)
        }
        else {
            logData = getCompleteLog()
        }
        def result = [log: logData.output, lastLineNumber: logData.lineAt]
        render result as JSON
    }

    public def getLogPaths() {
        def result = []
        Logger.getRootLogger().loggerRepository.currentLoggers.each {
            (it as Logger).allAppenders.each {
                org.apache.log4j.Appender appender = (it as org.apache.log4j.Appender)
                def name = appender.name
                def path = appender.properties.file
                result.add(name:name, path:path)
            }
        }
        return result
    }

    public def getCompleteLog() {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def logData = logTailingService.getAll(path)
        return logData
    }

    public def getLatestLogItemsByLines(def fromLines) {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def logData = logTailingService.getLastLines(path, fromLines as int)
        return logData
    }

    public def getLatestLogItemsByTime(def fromTime) {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def lastModified = new Date(new File(path).lastModified())
        def logData = ""
        if ((lastModified > fromTime) || fromTime == null) {
            logData = logTailingService.getAll(path)
        }
        return logData
    }

}

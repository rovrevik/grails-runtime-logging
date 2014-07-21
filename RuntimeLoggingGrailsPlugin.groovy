class RuntimeLoggingGrailsPlugin {
    // the plugin version
    def version = "0.4.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails Runtime Logging Plugin" // Headline display name of the plugin
    def author = "Jason Morris"
    def authorEmail = "jason.morris@torusit.com"
    def description = '''\
Allows you to change the logging characteristics (e.g. Level) for common parts of a Grails application at runtime without the need to restart.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/runtime-logging"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "JIRA", url: "https://github.com/rovrevik/grails-runtime-logging/issues"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/rovrevik/grails-runtime-logging"]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
        def bean = applicationContext.getBean('logAdapterService')
        String loggingFrameworkName = application.config.grails.plugins.runtimelogging.loggingFramework ?: null

        if (!bean) {
            log.error('Could not retrieve logAdapterService bean.')
        }
        else if (!loggingFrameworkName) {
            log.error('Could not retrieve logging framework setting. (assuming Log4j)')
            bean.loggingFramwork = grails.plugin.runtimelogging.LoggingFramework.LoggingFramework.LOG4J
        }
        else {
            try {
                bean.loggingFramwork = grails.plugin.runtimelogging.LoggingFramework.LoggingFramework.valueOf(loggingFrameworkName)
            }
            catch (e) {
                log.error('logging framework not set. (assuming Log4j)', e)
                bean.loggingFramwork = grails.plugin.runtimelogging.LoggingFramework.LoggingFramework.LOG4J
            }
        }

        if (bean) {
            try {
                bean.loggingClasses.logger = Class.forName(bean.loggingFramwork.loggingClassNames.logger)
                bean.loggingClasses.level = Class.forName(bean.loggingFramwork.loggingClassNames.level)
            }
            catch (e) {
                log.error("Could not load logging class(es) ${bean.loggingFramwork.loggingClassNames} message: ${e.message}")
            }
        }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}

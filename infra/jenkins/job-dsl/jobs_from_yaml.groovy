import org.yaml.snakeyaml.Yaml

String repoUrl = (binding.hasVariable('repoUrl') ? binding.getVariable('repoUrl') : '').toString().trim()
String repoBranch = (binding.hasVariable('repoBranch') ? binding.getVariable('repoBranch') : 'master').toString().trim()
String credentialsId = (binding.hasVariable('credentialsId') ? binding.getVariable('credentialsId') : '').toString().trim()
String jobConfigGlob = (binding.hasVariable('jobConfigGlob') ? binding.getVariable('jobConfigGlob') : 'infra/jenkins/jobs/*.yml').toString().trim()
String pipelineTemplatePath = (binding.hasVariable('pipelineTemplatePath') ? binding.getVariable('pipelineTemplatePath') : 'infra/jenkins/pipelines/cucumber-job-template.groovy').toString().trim()
String workspacePath = (binding.hasVariable('workspacePath') ? binding.getVariable('workspacePath') : '.').toString().trim()

if (repoUrl.isEmpty()) {
    throw new IllegalArgumentException('repoUrl additional parameter is mandatory for job generation.')
}

File workspaceRoot = new File(workspacePath)
if (!workspaceRoot.exists() || !workspaceRoot.isDirectory()) {
    throw new IllegalStateException("Workspace path is invalid: ${workspacePath}")
}

File resolveWorkspaceFile(String relativePath) {
    return new File(workspaceRoot, relativePath.replace('\\', File.separator))
}

File templateFile = resolveWorkspaceFile(pipelineTemplatePath)
if (!templateFile.exists()) {
    throw new IllegalStateException("Pipeline template not found: ${templateFile.path}")
}

String pipelineTemplate = templateFile.getText('UTF-8')

List<String> resolveConfigFiles(String globPattern) {
    String normalized = globPattern.replace('\\', '/')
    int wildcardIndex = normalized.indexOf('*')
    String baseDirPath = wildcardIndex >= 0 ? normalized.substring(0, wildcardIndex) : normalized
    baseDirPath = baseDirPath.replaceAll('/+$', '')
    if (baseDirPath.isEmpty()) {
        baseDirPath = '.'
    }

    File baseDir = resolveWorkspaceFile(baseDirPath)
    if (!baseDir.exists()) {
        return new ArrayList<String>()
    }

    String regex = '^' + normalized
            .replace('.', '\\.')
            .replace('**/', '(.*/)?')
            .replace('**', '.*')
            .replace('*', '[^/]*') + '$'

    List<String> matches = new ArrayList<String>()
    baseDir.eachFileRecurse { File file ->
        String candidate = workspaceRoot.toPath().relativize(file.toPath()).toString().replace('\\', '/')
        if (file.isFile() && candidate.matches(regex)) {
            matches.add(candidate)
        }
    }
    return matches.sort()
}

List<String> configFiles = resolveConfigFiles(jobConfigGlob)

if (configFiles.isEmpty()) {
    throw new IllegalStateException("No job YAML files found with glob: ${jobConfigGlob}")
}

Yaml yaml = new Yaml()

String requiredString(Map source, String key, String filePath) {
    Object value = source.get(key)
    if (value == null || value.toString().trim().isEmpty()) {
        throw new IllegalArgumentException("Missing required key '${key}' in ${filePath}")
    }
    return value.toString().trim()
}

String optionalString(Map source, String key, String defaultValue) {
    Object value = source.get(key)
    if (value == null || value.toString().trim().isEmpty()) {
        return defaultValue
    }
    return value.toString().trim()
}

boolean optionalBoolean(Map source, String key, boolean defaultValue) {
    Object value = source.get(key)
    if (value == null) {
        return defaultValue
    }
    return Boolean.parseBoolean(value.toString())
}

String escapeForGroovySingleQuote(String value) {
    return value.replace('\\', '\\\\').replace("'", "\\'")
}

configFiles.each { String filePath ->
    File configFile = resolveWorkspaceFile(filePath)
    Map jobConfig = yaml.load(configFile.getText('UTF-8')) as Map

    if (jobConfig == null || jobConfig.isEmpty()) {
        throw new IllegalArgumentException("Empty YAML content in ${filePath}")
    }

    String jobName = requiredString(jobConfig, 'jobName', filePath)
    String description = optionalString(jobConfig, 'description', "Generated from ${filePath}")
    boolean disabled = optionalBoolean(jobConfig, 'disabled', false)

    Map gradle = (jobConfig.get('gradle') instanceof Map) ? (Map) jobConfig.get('gradle') : [:]

    String browser = requiredString(gradle, 'browser', filePath)
    String suiteFile = requiredString(gradle, 'suiteFile', filePath)
    String tags = optionalString(gradle, 'tags', '@FullRun')
    String executionMode = optionalString(gradle, 'executionMode', 'remote')
    String gridUrl = optionalString(gradle, 'gridUrl', 'http://selenium-hub:4444/wd/hub')
    String chromeNodes = optionalString(gradle, 'chromeNodes', '4')

    String renderedPipeline = pipelineTemplate
            .replace('__REPO_URL__', escapeForGroovySingleQuote(repoUrl))
            .replace('__REPO_BRANCH__', escapeForGroovySingleQuote(repoBranch))
            .replace('__CREDENTIALS_ID__', escapeForGroovySingleQuote(credentialsId))
            .replace('__DEFAULT_BROWSER__', escapeForGroovySingleQuote(browser))
            .replace('__DEFAULT_SUITE_FILE__', escapeForGroovySingleQuote(suiteFile))
            .replace('__DEFAULT_TAGS__', escapeForGroovySingleQuote(tags))
            .replace('__DEFAULT_EXECUTION__', escapeForGroovySingleQuote(executionMode))
            .replace('__DEFAULT_GRID_URL__', escapeForGroovySingleQuote(gridUrl))
            .replace('__DEFAULT_CHROME_NODES__', escapeForGroovySingleQuote(chromeNodes))

    pipelineJob(jobName) {
        description(description)
        disabled(disabled)

        logRotator {
            daysToKeep(14)
            numToKeep(30)
        }

        definition {
            cps {
                sandbox(true)
                script(renderedPipeline)
            }
        }
    }
}

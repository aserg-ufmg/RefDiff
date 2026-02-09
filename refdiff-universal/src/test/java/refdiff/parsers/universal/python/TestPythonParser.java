package refdiff.parsers.universal.python;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestPythonParser {
    private static final LanguagePlugin parser = new PythonParser();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/python/syntax";

    private CstNode findNode(List<CstNode> nodes, String name, int line) {
        return nodes.stream()
            .filter(node -> name.equals(node.getSimpleName()) && node.getLocation().getBeginLine() == line)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Node with name '" + name + "' at line " + line + " not found."));
    }

    private record ExpectedNode(
        String name,
        String type,
        int line,
        String localName,
        String namespace,
        String fileName,
        List<String> params
    ) {
        ExpectedNode(String name, String type, int line, String localName, String namespace, String fileName) {
            this(name, type, line, localName, namespace, fileName, List.of());
        }
    }

    @Test
    public void shouldParseFunctionsAndClassesCorrectly() throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH);
        SourceFileSet sources = SourceFolder.from(baseFolderPath, ".py");
        CstRoot cstRoot = parser.parse(sources);

        List<CstNode> nodes = cstRoot.getNodes().stream()
            // .filter(node -> PythonNodeTypes.CLASS.equals(node.getType()) || PythonNodeTypes.FUNCTION.equals(node.getType()))
            .filter(node -> PythonNodeTypes.FUNCTION.equals(node.getType()))
            .collect(Collectors.toList());

        List<ExpectedNode> expectedNodes = Arrays.asList(
            // new ExpectedNode("APIKeyError", PythonNodeTypes.CLASS, 23, "APIKeyError", "api_key", "api_key.py"),
            // new ExpectedNode("APIKeyNotFoundError", PythonNodeTypes.CLASS, 29, "APIKeyNotFoundError", "api_key", "api_key.py"),
            // new ExpectedNode("APIKeyPermissionError", PythonNodeTypes.CLASS, 35, "APIKeyPermissionError", "api_key", "api_key.py"),
            // new ExpectedNode("APIKeyValidationError", PythonNodeTypes.CLASS, 41, "APIKeyValidationError", "api_key", "api_key.py"),
            // new ExpectedNode("APIKey", PythonNodeTypes.CLASS, 47, "APIKey", "api_key", "api_key.py"),
            new ExpectedNode("from_db", PythonNodeTypes.FUNCTION, 61, "from_db(api_key)", "api_key.py/", "api_key.py", List.of("api_key")),
            // new ExpectedNode("APIKeyWithoutHash", PythonNodeTypes.CLASS, 82, "APIKeyWithoutHash", "api_key", "api_key.py"),
            new ExpectedNode("from_db", PythonNodeTypes.FUNCTION, 96, "from_db(api_key)", "api_key.py/", "api_key.py", List.of("api_key")),
            new ExpectedNode("generate_api_key", PythonNodeTypes.FUNCTION, 116, "generate_api_key(name, user_id, permissions, description)", "api_key.py/", "api_key.py", List.of("name", "user_id", "permissions", "description")),
            new ExpectedNode("validate_api_key", PythonNodeTypes.FUNCTION, 153, "validate_api_key(plain_text_key)", "api_key.py/", "api_key.py", List.of("plain_text_key")),
            new ExpectedNode("revoke_api_key", PythonNodeTypes.FUNCTION, 184, "revoke_api_key(key_id, user_id)", "api_key.py/", "api_key.py", List.of("key_id", "user_id")),
            new ExpectedNode("list_user_api_keys", PythonNodeTypes.FUNCTION, 217, "list_user_api_keys(user_id)", "api_key.py/", "api_key.py", List.of("user_id")),
            new ExpectedNode("suspend_api_key", PythonNodeTypes.FUNCTION, 234, "suspend_api_key(key_id, user_id)", "api_key.py/", "api_key.py", List.of("key_id", "user_id")),
            new ExpectedNode("has_permission", PythonNodeTypes.FUNCTION, 265, "has_permission(api_key, required_permission)", "api_key.py/", "api_key.py", List.of("api_key", "required_permission")),
            new ExpectedNode("get_api_key_by_id", PythonNodeTypes.FUNCTION, 273, "get_api_key_by_id(key_id, user_id)", "api_key.py/", "api_key.py", List.of("key_id", "user_id")),
            new ExpectedNode("update_api_key_permissions", PythonNodeTypes.FUNCTION, 291, "update_api_key_permissions(key_id, user_id, permissions)", "api_key.py/", "api_key.py", List.of("key_id", "user_id", "permissions")),

            // new ExpectedNode("GithubWebhookType", PythonNodeTypes.CLASS, 19, "GithubWebhookType", "github", "github.py"),
            // new ExpectedNode("GithubWebhooksManager", PythonNodeTypes.CLASS, 23, "GithubWebhooksManager", "github", "github.py"),
            new ExpectedNode("validate_payload", PythonNodeTypes.FUNCTION, 32, "validate_payload(cls, webhook, request, credentials)", "github.py/", "github.py", List.of("cls", "webhook", "request", "credentials")),
            new ExpectedNode("trigger_ping", PythonNodeTypes.FUNCTION, 65, "trigger_ping(self, webhook, credentials)", "github.py/", "github.py", List.of("self", "webhook", "credentials")),
            new ExpectedNode("_register_webhook", PythonNodeTypes.FUNCTION, 85, "_register_webhook(self, credentials, webhook_type, resource, events, ingress_url, secret)", "github.py/", "github.py", List.of("self", "credentials", "webhook_type", "resource", "events", "ingress_url", "secret")),
            new ExpectedNode("_deregister_webhook", PythonNodeTypes.FUNCTION, 138, "_deregister_webhook(self, webhook, credentials)", "github.py/", "github.py", List.of("self", "webhook", "credentials")),
            new ExpectedNode("extract_github_error_msg", PythonNodeTypes.FUNCTION, 173, "extract_github_error_msg(response)", "github.py/", "github.py", List.of("response")),

            new ExpectedNode("sentry_init", PythonNodeTypes.FUNCTION, 13, "sentry_init()", "metrics.py/", "metrics.py"),
            new ExpectedNode("sentry_capture_error", PythonNodeTypes.FUNCTION, 30, "sentry_capture_error(error)", "metrics.py/", "metrics.py", List.of("error")),
            new ExpectedNode("discord_send_alert", PythonNodeTypes.FUNCTION, 35, "discord_send_alert(content)", "metrics.py/", "metrics.py", List.of("content")),

            new ExpectedNode("_is_ip_blocked", PythonNodeTypes.FUNCTION, 45, "_is_ip_blocked(ip)", "request.py/", "request.py", List.of("ip")),
            new ExpectedNode("_remove_insecure_headers", PythonNodeTypes.FUNCTION, 53, "_remove_insecure_headers(headers, old_url, new_url)", "request.py/", "request.py", List.of("headers", "old_url", "new_url")),
            // new ExpectedNode("HostResolver", PythonNodeTypes.CLASS, 69, "HostResolver", "request", "request.py"),
            new ExpectedNode("__init__", PythonNodeTypes.FUNCTION, 75, "__init__(self, ssl_hostname, ip_addresses)", "request.py/", "request.py", List.of("self", "ssl_hostname", "ip_addresses")),
            new ExpectedNode("resolve", PythonNodeTypes.FUNCTION, 80, "resolve(self, host, port, family)", "request.py/", "request.py", List.of("self", "host", "port", "family")),
            new ExpectedNode("close", PythonNodeTypes.FUNCTION, 97, "close(self)", "request.py/", "request.py", List.of("self")),
            new ExpectedNode("_resolve_host", PythonNodeTypes.FUNCTION, 101, "_resolve_host(hostname)", "request.py/", "request.py", List.of("hostname")),
            new ExpectedNode("validate_url", PythonNodeTypes.FUNCTION, 121, "validate_url(url, trusted_origins)", "request.py/", "request.py", List.of("url", "trusted_origins")),
            new ExpectedNode("pin_url", PythonNodeTypes.FUNCTION, 192, "pin_url(url, ip_addresses)", "request.py/", "request.py", List.of("url", "ip_addresses")),
            // new ExpectedNode("Response", PythonNodeTypes.CLASS, 238, "Response", "request", "request.py"),
            new ExpectedNode("__init__", PythonNodeTypes.FUNCTION, 244, "__init__(self, response, url, body)", "request.py/", "request.py", List.of("self", "response", "url", "body")),
            new ExpectedNode("json", PythonNodeTypes.FUNCTION, 258, "json(self, encoding, **kwargs)", "request.py/", "request.py", List.of("self", "encoding", "**kwargs")),
            new ExpectedNode("text", PythonNodeTypes.FUNCTION, 266, "text(self, encoding)", "request.py/", "request.py", List.of("self", "encoding")),
            // new ExpectedNode("Requests", PythonNodeTypes.CLASS, 283, "Requests", "request", "request.py"),
            new ExpectedNode("__init__", PythonNodeTypes.FUNCTION, 290, "__init__(self, trusted_origins, raise_for_status, extra_url_validator, extra_headers, retry_max_wait)", "request.py/", "request.py", List.of("self", "trusted_origins", "raise_for_status", "extra_url_validator", "extra_headers", "retry_max_wait")),
            new ExpectedNode("request", PythonNodeTypes.FUNCTION, 310, "request(self, method, url, headers, files, data, json, allow_redirects, max_redirects, **kwargs)", "request.py/", "request.py", List.of("self", "method", "url", "headers", "files", "data", "json", "allow_redirects", "max_redirects", "**kwargs")),
            new ExpectedNode("_request", PythonNodeTypes.FUNCTION, 343, "_request(self, method, url, headers, files, data, json, allow_redirects, max_redirects, **kwargs)", "request.py/", "request.py", List.of("self", "method", "url", "headers", "files", "data", "json", "allow_redirects", "max_redirects", "**kwargs")),
            
            // new ExpectedNode("GetWikipediaSummaryBlock", PythonNodeTypes.CLASS, 17, "GetWikipediaSummaryBlock", "search", "search.py"),
            new ExpectedNode("__init__", PythonNodeTypes.FUNCTION, 27, "__init__(self)", "search.py/", "search.py", List.of("self")),
            new ExpectedNode("run", PythonNodeTypes.FUNCTION, 39, "run(self, input_data, **kwargs)", "search.py/", "search.py", List.of("self", "input_data", "**kwargs")),
            // new ExpectedNode("GetWeatherInformationBlock", PythonNodeTypes.CLASS, 63, "GetWeatherInformationBlock", "search", "search.py"),
            new ExpectedNode("__init__", PythonNodeTypes.FUNCTION, 91, "__init__(self)", "search.py/", "search.py", List.of("self")),
            new ExpectedNode("run", PythonNodeTypes.FUNCTION, 116, "run(self, input_data, credentials, **kwargs)", "search.py/", "search.py", List.of("self", "input_data", "credentials", "**kwargs"))
        );

        for (ExpectedNode expected : expectedNodes) {
          try {
            CstNode actualNode = findNode(nodes, expected.name(), expected.line());
            assertThat(actualNode.getType(), is(equalTo(expected.type())));
            assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
            assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
            assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
            Location location = actualNode.getLocation();
            assertThat(location.getFile(), is(equalTo(expected.fileName())));
            assertThat(location.getBeginLine(), is(equalTo(expected.line())));
            List<String> actualParamNames = actualNode.getParameters().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
            assertThat(actualParamNames, is(equalTo(expected.params())));
          } catch (AssertionError | Exception e) {
            CstNode actualNode = findNode(nodes, expected.name(), expected.line());
            System.err.println("Test failed for expected node: name=" + expected.name()
                + ", type=" + expected.type()
                + ", line=" + expected.line()
                + ", localName=" + expected.localName()
                + ", namespace=" + expected.namespace()
                + ", file=" + expected.fileName()
                + ", params=" + expected.params());
            System.err.println("Actual node: line=" + actualNode.getLocation().getBeginLine()
                + ", type=" + actualNode.getType()
                + ", localName=" + actualNode.getLocalName()
                + ", namespace=" + actualNode.getNamespace()
                + ", file=" + actualNode.getLocation().getFile()
                + ", params=" + actualNode.getParameters().stream()
                    .map(Parameter::getName)
                    .collect(Collectors.toList()));
            throw e;
          }
        }
    }
}
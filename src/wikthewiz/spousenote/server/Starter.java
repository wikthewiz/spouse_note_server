package wikthewiz.spousenote.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.security.Constraint;

public class Starter {

    public static void main(String[] args) {
        try {
            ContextHandler secureSmurfContext = new ContextHandler("/smurf");
            secureSmurfContext.setHandler(new ServerRequest("smurf"));

            ContextHandler secureOldSmurfContext = new ContextHandler("/smurf/old");
            secureOldSmurfContext.setHandler(new ServerRequest("old smurf"));

            ContextHandler openContext = new ContextHandler("/open");
            openContext.setHandler(new ServerRequest("open"));

            final String passwordFile = "src/test/resources/spouse-note.properties";
            LoginService loginService = new HashLoginService("spouse-note", passwordFile);

            if (!new File(passwordFile).exists()){
                throw new FileNotFoundException(passwordFile);
            }

            Constraint constraint = new Constraint();
            constraint.setName("auth");
            constraint.setAuthenticate(true);
            constraint.setRoles(new String[] { "user" });

            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec("/smurf/*");
            mapping.setConstraint(constraint);

            ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            security.setConstraintMappings(Collections.singletonList(mapping));
            security.setAuthenticator(new BasicAuthenticator());
            security.setLoginService(loginService);

            ContextHandlerCollection contexts = new ContextHandlerCollection();
            contexts.addHandler(secureSmurfContext);
            contexts.addHandler(openContext);
            contexts.addHandler(secureOldSmurfContext);
            security.setHandler(contexts);

            Server server = new Server(8080);
            server.addBean(loginService);
            server.setHandler(security);
            server.start();
            server.join();
        } catch (Throwable t) {
            StringWriter stacktrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stacktrace));
            System.out.println("ERROR:" + stacktrace.toString());
        }
    }
}

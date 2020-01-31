import initialization.Container;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Todo Pom version management dependency
// Todo remove all singleton code and replace with some dependecy injection framweork
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) throws IOException {
        new Container();
        logger.info("Server started");
    }
}

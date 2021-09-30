import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTTest extends CamelTestSupport {
    Logger log = LoggerFactory.getLogger(getClass());

    int count = 100;
    int threads = 10;
    ExecutorService executorService;

    @Override
    protected void setupResources() throws Exception {
        log.info("Using executor of {} threads", threads);
        executorService = Executors.newFixedThreadPool(threads);
    }

    @Override
    protected void cleanupResources() throws Exception {
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Exception while waiting shutdown of executor", e);
        }
    }

    @Test
    public void testMock() throws Exception {
        String inputString = FileUtils.readFileToString(new File("src/test/resources/input/input.xml"),
                StandardCharsets.UTF_8);

        getMockEndpoint("mock:result").expectedMessageCount(count);

        IntStream.range(0, count).forEach(i ->
                executorService.execute(() ->
                        template.sendBody("direct:start", inputString)
                )
        );

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .pipeline(
                                "xslt:xslt/transform_env_reverse.xslt",
                                "xslt:xslt/transform_env.xslt",
                                "xslt:xslt/transform_env_reverse.xslt",
                                "xslt:xslt/transform_env.xslt",
                                "xslt:xslt/transform_env_reverse.xslt",
                                "xslt:xslt/transform_env.xslt",
                                "xslt:xslt/transform_env_reverse.xslt",
                                "xslt:xslt/transform_env.xslt",
                                "xslt:xslt/transform_env_reverse.xslt"
                        )
                        .to("mock:result");
            }
        };
    }

}

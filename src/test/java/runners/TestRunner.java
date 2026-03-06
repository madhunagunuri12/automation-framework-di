package runners;

import com.automation.base.BaseRunner;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.automation",
        tags = "@user-management",
        monochrome = true
)
public class TestRunner extends BaseRunner {

}

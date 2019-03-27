package base;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.LogStatus;
import com.sun.deploy.util.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import reporting.ExtentManager;
import reporting.ExtentTestManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.DataFormat.URL;

public class CommonAPI {
    //WebDriver Instance
    public static WebDriver driver = null;
    public String browserstack_username= "";
    public String browserstack_accesskey = "";
    public String saucelabs_username = "";
    public String saucelabs_accesskey = "";
    //Extent Report Listener
    public static ExtentReports extent;
    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }
    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }
    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
    @AfterMethod
    public void afterEachTestMethod(ITestResult result) {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));

        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }

        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }
        ExtentTestManager.endTest();
        extent.flush();
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(driver, result.getName());
        }
        driver.quit();
    }
    @AfterSuite
    public void generateReport() {
        extent.close();
    }
    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    @Parameters({"useCloudEnv","cloudEnvName","os","os_version","browserName","browserVersion","url"})
    @BeforeMethod
    public void setUp(@Optional("false") boolean useCloudEnv, @Optional("false")String cloudEnvName,
                      @Optional("OS X") String os, @Optional("10") String os_version, @Optional("chrome-options") String browserName, @Optional("34")
                              String browserVersion, @Optional("http://www.bbc.com") String url)throws IOException {
        if(useCloudEnv==true){
            if(cloudEnvName.equalsIgnoreCase("browserstack")) {
                getCloudDriver(cloudEnvName,browserstack_username,browserstack_accesskey,os,os_version, browserName, browserVersion);
            }else if (cloudEnvName.equalsIgnoreCase("saucelabs")){
                getCloudDriver(cloudEnvName,saucelabs_username, saucelabs_accesskey,os,os_version, browserName, browserVersion);
            }
        }else{
            getLocalDriver(os, browserName);
        }
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(25, TimeUnit.SECONDS);
        driver.get(url);
        // driver.navigate().to(url);
        // driver.navigate().refresh();
        driver.manage().window().maximize();
    }
    public WebDriver getLocalDriver(@Optional("mac") String OS, String browserName){
        if(browserName.equalsIgnoreCase("chrome")){
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\fazli\\IdeaProjects\\lenovoautomation\\Generic\\driver\\chromedriver.exe");
            }else if(OS.equalsIgnoreCase("Windows")){
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\fazli\\IdeaProjects\\lenovoautomation\\Generic\\driver\\chromedriver.exe");
            }
            driver = new ChromeDriver();
        } else if(browserName.equalsIgnoreCase("chrome-options")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-notifications");
            options.addArguments("--incognito");
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\fazli\\IdeaProjects\\lenovoautomation\\Generic\\driver\\chromedriver.exe");
            }else if(OS.equalsIgnoreCase("Windows")){
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\fazli\\IdeaProjects\\lenovoautomation\\Generic\\driver\\chromedriver.exe");
            }
            driver = new ChromeDriver(options);
        }

        else if(browserName.equalsIgnoreCase("firefox")){
            if(OS.equalsIgnoreCase("OS X")){
                System.setProperty("webdriver.gecko.driver", "../generic/browser-driver/geckodriver");
            }else if(OS.equalsIgnoreCase("Windows")) {
                System.setProperty("webdriver.gecko.driver", "../generic/browser-driver/geckodriver.exe");
            }
            driver = new FirefoxDriver();

        } else if(browserName.equalsIgnoreCase("ie")) {
            System.setProperty("webdriver.ie.driver", "../generic/browser-driver/IEDriverServer.exe");
            driver = new InternetExplorerDriver();
        }
        return driver;
    }


    public WebDriver getCloudDriver(String envName,String envUsername, String envAccessKey,String os, String os_version,String browserName,
                                    String browserVersion)throws IOException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("browser",browserName);
        cap.setCapability("browser_version",browserVersion);
        cap.setCapability("os", os);
        cap.setCapability("os_version", os_version);
        if(envName.equalsIgnoreCase("Saucelabs")){
            //resolution for Saucelabs
            driver = new RemoteWebDriver(new URL("http://"+envUsername+":"+envAccessKey+
                    "@ondemand.saucelabs.com:80/wd/hub"), cap);
        }else if(envName.equalsIgnoreCase("Browserstack")) {
            cap.setCapability("resolution", "1024x768");
            driver = new RemoteWebDriver(new URL("http://" + envUsername + ":" + envAccessKey +
                    "@hub-cloud.browserstack.com/wd/hub"), cap);
        }
        return driver;
    }



    @AfterMethod
    public void cleanUp() {
        driver.quit();
    }

    /**
     * Typing value of a general locator. Method will choose what technique to use.
     *
     * @param locator: locator technique: cssSelector, name, xpath and id.
     * @param value: Value being assigned to the locator.
     */
    public void typeOnElem(String locator, String value) {
        try {
            // Check by cssSelector
            driver.findElement(By.cssSelector(locator)).sendKeys(value);
        } catch (Exception e1) {
            try {
                // Check by name
                driver.findElement(By.name(locator)).sendKeys(value);
            } catch (Exception e2) {
                try {
                    // Check by xpath
                    driver.findElement(By.xpath(locator)).sendKeys(value);
                } catch (Exception e3) {
                    // Check by id
                    driver.findElement(By.id(locator)).sendKeys(value);
                }
            }
        }
    }

    /**
     * Typing value of a general locator. Method will choose what technique to use and hit 'ENTER'
     *
     * @param locator: locator technique(cssSelector, name, xpath and id).
     * @param value: Value being assigned to the locator.
     */
    public static void typeOnElementNEnter(String locator, String value) {
        try {
            // Check and enter by cssSelector
            driver.findElement(By.cssSelector(locator)).sendKeys(value, Keys.ENTER);
        } catch (Exception e1) {
            try {
                // Check and enter by name
                driver.findElement(By.name(locator)).sendKeys(value, Keys.ENTER);
            } catch (Exception e2) {
                try {
                    // Check and enter by xpath
                    driver.findElement(By.xpath(locator)).sendKeys(value, Keys.ENTER);
                } catch (Exception e3) {
                    // Check and enter by id
                    driver.findElement(By.id(locator)).sendKeys(value, Keys.ENTER);
                }
            }
        }
    }

    /**
     *  Checking with locator technique and clicking on it.
     *
     * @param locator: locator technique(cssSelector, name, xpath and id)
     */
    public void clickOnElem(String locator) {
        try {
            // Check and click by cssSelector
            driver.findElement(By.cssSelector(locator)).click();
        } catch (Exception e1) {
            try {
                // Check and click by name
                driver.findElement(By.name(locator)).click();
            } catch (Exception e2) {
                try {
                    // Check and click by xpath
                    driver.findElement(By.xpath(locator)).click();
                } catch (Exception e3) {
                    // Check and click by id
                    driver.findElement(By.id(locator)).click();
                }
            }
        }
    }

    /**
     *  Checking with locator technique and hitting 'ENTER' on it.
     *
     * @param locator: locator technique(cssSelector, name, xpath and id)
     */
    public void enterOnElem(String locator) {
        try {
            // Check and enter by cssSelector
            driver.findElement(By.cssSelector(locator)).sendKeys(Keys.ENTER);
        } catch (Exception e1) {
            try {
                // Check and enter by name
                driver.findElement(By.name(locator)).sendKeys(Keys.ENTER);
            } catch (Exception e2) {
                try {
                    // Check and enter by xpath
                    driver.findElement(By.xpath(locator)).sendKeys(Keys.ENTER);
                } catch (Exception e3) {
                    // Check and enter by id
                    driver.findElement(By.id(locator)).sendKeys(Keys.ENTER);
                }
            }
        }
    }

    /**
     *  Checking with locator technique and clearing its value.
     *
     * @param locator: locator technique(cssSelector, name, xpath and id)
     */
    public void clearInputField(String locator) {
        try {
            // Check and clear by cssSelector
            driver.findElement(By.cssSelector(locator)).clear();
        } catch (Exception e1) {
            try {
                // Check and clear by name
                driver.findElement(By.name(locator)).clear();
            } catch (Exception e2) {
                try {
                    // Check and clear by xpath
                    driver.findElement(By.xpath(locator)).clear();
                } catch (Exception e3) {
                    // Check and clear by id
                    driver.findElement(By.id(locator)).clear();
                }
            }
        }
    }

    /**
     * Server sleeps for 2000 milliseconds.
     * @throws InterruptedException
     */
    public static void sleepForTwoSec() throws InterruptedException {
        Thread.sleep(2000);
    }

    /**
     * Server sleeps for user's given seconds.
     *
     * @param sec: Number of seconds for the server to sleep.
     * @throws InterruptedException
     */
    public static void sleepFor(int sec) throws InterruptedException {
        Thread.sleep(sec * 1000);
    }

    /**
     * Returns current page's URL
     *
     * @return: String of the url.
     */
    public String getCurrUrl() {
        return driver.getCurrentUrl();
    }

    public static void captureScreenshot(WebDriver driver, String screenshotName) {
        DateFormat df = new SimpleDateFormat("(MM.dd.yyyy-HH:mma)");
        Date date = new Date();
        df.format(date);

        File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            //FileUtils.copyFile(file, new File(System.getProperty("user.dir") + "/screenshots/" + screenshotName + " " + df.format(date) + ".png"));
            System.out.println("Screenshot captured");
        } catch (Exception e) {
            System.out.println("Exception while taking screenshot " + e.getMessage());
            ;
        }

    }

    public static String convertToString(String st) {
        String splitString = "";
     //   splitString = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(st), ' ');
        return splitString;
    }
}
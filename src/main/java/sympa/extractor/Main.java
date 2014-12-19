package sympa.extractor;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.Select;

public class Main {

    private final static String ML_DOMAIN = "@secondarydomain.com";
    private final static String BASE_URL = "http://sympa.example.com/wws";
    private final static String ADMIN_USER = "adminuser@sympa.example.com";
    private final static String ADMIN_PASSWORD = "***";
    
    private static Logger log = Logger.getLogger("sympa");

    private static WebDriver driver;
    
    public static void main(String[] args) {
        try {
            driver = getWebDriver();
            authenticate();
            writeListsInfoFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        driver.quit();
    }
    
    private static WebDriver getWebDriver() throws IOException {
        FirefoxProfile fp = new FirefoxProfile();
        fp.setPreference("browser.download.folderList", 2);
        fp.setPreference("browser.download.manager.showWhenStarting", false);
        File downloadDir = new File("archives");
        fp.setPreference("browser.download.dir", downloadDir.getCanonicalPath());
        fp.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip");

        return new FirefoxDriver(fp);
    }

    private static void writeListsInfoFiles() throws IOException {
        List<ListDescription> lists = getLists();
        for(ListDescription list : lists) {
            addListDecriptionToListsTSV(list);
            String subscribers = getListInfo(list);
            writeListSubscribersTSV(list, subscribers);
            writeListScripts(list, subscribers);
            downloadArchiveFile(list);
        }
    }

    private static void downloadArchiveFile(ListDescription list) {
        driver.navigate().to(BASE_URL + "/arc_manage/" + list.name);
        WebElement selectElement = driver.findElement(By.name("directories"));
        List<WebElement> optionElements = selectElement.findElements(By.tagName("option"));
        Select select = new Select(selectElement);
        for(WebElement optionElement : optionElements) {
            select.selectByVisibleText(optionElement.getText());
        }
        
        WebElement submitElement = driver.findElement(By.name("action_arc_download"));
        submitElement.click();
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void writeListScripts(ListDescription list,
            String subscribers) throws IOException {
        Writer w = getWriterForFileAtPath("creation_scripts/" + list.name + ".sh");
        String listName = list.name + ML_DOMAIN;
        w.write("#!/bin/sh\n\nset -x\n");
        w.write("gam create group '" + listName + "'\n");
        final String gam_update = "gam update group '" + listName + "' ";
        w.write(gam_update + "name 'ml_" + list.name + "'\n");
        w.write(gam_update + "description '" + list.description + "'\n");
        w.write(gam_update + "settings reply_to reply_to_list\n");
        w.write(gam_update + "settings who_can_post_message anyone_can_post\n");
        w.write(gam_update + "settings allow_external_members true\n");
        w.write(gam_update + "settings primary_language en-GB\n");
        w.write(gam_update + "settings allow_google_communication false\n");
        w.close();
        
        w = getWriterForFileAtPath("update_scripts/" + list.name + ".sh");
        w.write("#!/bin/sh\n\nset -x\n");
        String[] emails = subscribers.split("\n");
        for(String email : emails) {
            w.write(gam_update + "add member " + email + "\n");
        }
        w.close();
    }

    private static void addListDecriptionToListsTSV(ListDescription list) throws IOException {
        Writer listsWriter = getWriterForFileAtPath("lists.tsv");
        listsWriter.write(list.tsvDescription() + "\n");
        listsWriter.close();
    }

    private static void writeListSubscribersTSV(ListDescription list, String users) throws IOException {
        Writer listWriter = getWriterForFileAtPath("lists/" + list.name + ".tsv");
        listWriter.write(users);
        listWriter.close();
    }

    private static Writer getWriterForFileAtPath(String path) throws IOException {
        File f = new File(path);
        f.mkdirs();
        f.delete();
        log.info("creating file at " + f.getCanonicalPath());
        return new BufferedWriter(new FileWriter(f));
    }

    private static String getListInfo(ListDescription list) {
        driver.navigate().to(BASE_URL + "/dump/" + list.name + "/light");
        return driver.findElement(By.xpath("//pre")).getText();
    }

    private static List<ListDescription> getLists() {
        List<ListDescription> lists = new ArrayList<ListDescription>();
        
        driver.navigate().to(BASE_URL + "/get_inactive_lists");
        List<WebElement> listsElements = driver.findElements(By.xpath("//table[@summary='Inactive Lists']/tbody/tr"));
        for(WebElement listInfo : listsElements) {
            List<WebElement> listElements = listInfo.findElements(By.xpath("./td"));
            if(4 == listElements.size()) {
                String lastUsedDateString = listElements.get(1).getText();
                String[] lastUsedDateArray = lastUsedDateString.split(" ");
                assertEquals(3, lastUsedDateArray.length);
                int year = Integer.parseInt(lastUsedDateArray[2]);
                if(year > 2000)
                {
                    ListDescription list = new ListDescription();
                    list.creationDate = listElements.get(0).getText();
                    list.lastMessageDate = listElements.get(1).getText();
                    list.name = listElements.get(2).getText();
                    list.description = listElements.get(3).getText();
                    lists.add(list);
                }
            }
        }
        
        return lists;
    }

    private static void authenticate() {
        log.info("navigating to: " + BASE_URL);
        driver.navigate().to(BASE_URL);
        
        WebElement submitElement = driver.findElement(By.xpath("//input[@type='submit']"));
        driver.findElement(By.id("email_login")).sendKeys(ADMIN_USER);
        driver.findElement(By.id("passwd")).sendKeys(ADMIN_PASSWORD);
        submitElement.click();
    }

}

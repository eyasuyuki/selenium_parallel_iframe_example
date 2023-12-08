package org.javaopen.iframe;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");//重要
        WebDriver driver = new ChromeDriver(options);

        // IFRAMEを同時に探索するリスト
        List<CompletableFuture<List<WebElement>>> iframeSearchFutures = getIframeList(driver).stream()
                .map(iframe -> CompletableFuture.supplyAsync(() -> searchInIframe(driver, iframe)))
                .collect(Collectors.toList());

        // 全体のページでの検索
        CompletableFuture<List<WebElement>> pageSearchFuture = CompletableFuture.supplyAsync(() -> searchOnPage(driver));

        // 最初に見つかった結果を取得
        try {
            List<WebElement> elements = CompletableFuture.anyOf(
                    pageSearchFuture,
                    CompletableFuture.allOf(iframeSearchFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignore -> iframeSearchFutures.stream()
                                    .map(CompletableFuture::join)
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList()))
            ).thenApply(result -> (List<WebElement>) result).get();

            // 要素の処理などを行う

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
    private static List<WebElement> searchOnPage(WebDriver driver) {
        // ページ全体での要素の検索
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("send")));
    }

    private static List<WebElement> searchInIframe(WebDriver driver, WebElement iframe) {
        // IFRAME内での要素の検索
        driver.switchTo().frame(iframe);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("send")));
        driver.switchTo().defaultContent();  // IFRAMEから出る
        return elements;
    }

    private static List<WebElement> getIframeList(WebDriver driver) {
        // IFRAMEのリストを取得するロジックを実装
        return driver.findElements(By.tagName("iframe"));
    }
}

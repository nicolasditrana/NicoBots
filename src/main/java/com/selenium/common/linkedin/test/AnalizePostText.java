package com.selenium.common.linkedin.test;

import com.selenium.common.linkedin.BO.BOPost;
import com.selenium.common.linkedin.util.LinkedinExcel;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class AnalizePostText {

    private static WebDriver driver;
    private static ArrayList<BOPost> xResults;
    private static Properties properties;
    private static boolean reloaded;

    //Mejoras: que calcule cuando se publico y lo valide con una propiedad

    public static void main(String[] args) {
        try {
            init();
            xResults = new ArrayList<>();

            driver.get("https://www.linkedin.com/feed/");

            login();
            changeByMoreRecent();

            execute();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.println("End. good luck!");
            driver.quit();
        }

        LinkedinExcel.generateExcel(xResults);
    }

    private static void init() throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(new File("src/main/resources/application.properties")));

        if (System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver", (String) properties.get("URLDriver"));
        }
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        reloaded = false;
    }

    public static void login() throws Exception {
        driver.findElement(By.className("main__sign-in-link")).click();

        driver.findElement(By.id("username")).sendKeys((String) properties.get("user"));
        driver.findElement(By.id("password")).sendKeys((String) properties.get("passw"));

        try {
            driver.findElement(By.xpath("//*[@aria-label='Iniciar sesión']")).click();
        } catch (Exception e) {
            try {
                driver.findElement(By.xpath("//*[@aria-label='Sign in']")).click();
            } catch (Exception i) {
                throw i;
            }
        }
        Thread.sleep(1500);
    }

    private static void changeByMoreRecent() throws Exception {
        Boolean searchByMoreRecent = Boolean.parseBoolean((String) properties.get("searchByMoreRecent"));
        if (searchByMoreRecent) {
            changeToMoreRecent(false);
        }
    }

    private static void changeToMoreRecent(boolean retry) throws Exception {
        try {
            driver.findElement(By.id("main")).findElement(By.className("mb2")).findElement(By.xpath("./button")).click();
            //Click en Recientes
            Thread.sleep(500);
            driver.findElement(By.id("main")).findElement(By.className("mb2")).findElement(By.className("artdeco-dropdown__content-inner")).findElement(By.xpath(".//ul/li[2]")).click();
            Thread.sleep(1500);
        } catch (Exception e) {
            //Si tarda en cargar, intenta de nuevo
            if (!retry) {
                changeToMoreRecent(true);
            } else {
                throw e;
            }
        }
    }

    private static void execute() {
        List<WebElement> xList;
        Map<String, String> xIds = new HashMap<>();
        Integer errors = 0;

        //Cuantos posteos voy a analizar
        Long loop = Long.parseLong((String) properties.get("loop"));
        while (xIds.size() < loop && errors < loop) {
            try {
                //Leo la lista de posteos en pantalla
                xList = driver.findElement(By.id("main")).findElements(By.xpath("//*[@class='break-words']"));
                for (WebElement xElement : xList) {
                    processAnElement(xIds, xElement);
                }
                afterAnalyze(errors);

            } catch (Exception e) {
                //Si los errores suman la cantidad de posteos a revisar, frena
                errors++;
            }
        }
    }

    private static void processAnElement(Map<String, String> xIds, WebElement xElement) {

        //Para cada posteo consulto si no lo lei
        String xIdElement = ((RemoteWebElement) xElement).getId();

        if (xIds.get(xIdElement) == null) {
            //Guardo los que ya lei.
            xIds.put(xIdElement, "");

            //Muestro el posteo en la pantalla
            //Al moverme, permite que se carguen los siguientes
            new Actions(driver).moveToElement(xElement).perform();

            Integer score = null;
            //Leo la descripcion
            WebElement xElementText = xElement.findElement(By.xpath("./span"));
            if (xElementText != null && xElementText.getText() != null) {

                score = analyzeText(xElementText.getText().toUpperCase());
            }

            if (score != null) {
                BOPost xBOPost = new BOPost();
                xBOPost.setScore(score);
                xBOPost.setText(xElementText.getText());

                searchActorInfo(xBOPost, xElement);

                save(xBOPost);
            }
        }
    }

    private static void save(BOPost xBOPost) {
        boolean exist = false;

        // Si se recargo la pagina valido no estar volviendo a guardar el mismo posteo
        if (!reloaded) {
            xResults.add(xBOPost);
            System.out.println(xBOPost.toString());
        } else {
            for (BOPost post : xResults) {
                if (post.equals(xBOPost)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                xResults.add(xBOPost);
                System.out.println(xBOPost.toString());
            }
        }
    }

    private static Integer analyzeText(String xText) {

        if (!validateObligatoryKeys(xText)) {
            return null;
        }
        return calculateScore(xText);
    }

    private static boolean validateObligatoryKeys(String xText) {
        String obligatoryKeys = (String) properties.get("obligatoryKeys");
        String[] xKeys = (obligatoryKeys).split(";");

        for (String value : xKeys) {
            //Debe contener todas las claves obligatorias
            if (!xText.contains(value)) {
                return false;
            }
        }
        return true;
    }

    private static Integer calculateScore(String xText) {
        Integer score = 0;

        //Cada clave adicional que encuentre suma un punto
        String additionalsKeys = (String) properties.get("additionalsKeys");

        String[] xKeys = additionalsKeys.split(";");
        for (String value : xKeys) {
            if (xText.contains(value)) {
                score++;
            }
        }
        return score;
    }

    private static void searchActorInfo(BOPost xBOPost, WebElement xElement) {
        try {
            WebElement xParentNode = xElement.findElement(By.xpath("./../../../../.."));
            if (xParentNode != null) {
                //Obtengo el contenedor del perfil
                WebElement xActorContainer = null;
                try {
                    xActorContainer = xParentNode.findElement(By.xpath(".//*[@data-control-name='actor_container']"));

                } catch (Exception e) {
                    //EL posteo es un compartir de otro posteo
                    xActorContainer = xParentNode.findElement(By.xpath(".//*[@data-control-name='original_share_actor_container']"));
                }

                //Leo los atributos
                String href = xActorContainer.getAttribute("href");
                String name = xActorContainer.findElement(By.className("feed-shared-actor__name")).getText();

                xBOPost.setPerfilName(name);
                xBOPost.setUrl(href);

                Boolean addNewPeople = Boolean.parseBoolean((String) properties.get("addNewPeople"));
                if (addNewPeople) {
                    connectWithNewPeople(xActorContainer, href, xBOPost);
                } else {
                    xBOPost.setIsConnect(BOPost.NOT_AGGREGATE);
                }
            }
        } catch (Exception e) {
            //Es una publicidad
        }
    }

    private static void connectWithNewPeople(WebElement xActorContainer, String href, BOPost xBOPost) {

        String typeOfContact = xActorContainer.findElement(By.className("feed-shared-actor__supplementary-actor-info")).getText();
        //Si es alguien que no sigo ni tengo como contacto, lo agrego
        if ((typeOfContact).contains("1")) {
            xBOPost.setIsConnect(BOPost.CONNECTED);

        } else if ((typeOfContact).contains("Siguiendo") || (typeOfContact).contains("Following")) {
            xBOPost.setIsConnect(BOPost.FOLLOWING);
        } else {

            if (addPeopleWithFollow(xActorContainer.findElement(By.xpath("./..")))) {
                xBOPost.setIsConnect(BOPost.FOLLOW_SENT);

            } else {
                if (addPeopleWithConnect(href)) {
                    xBOPost.setIsConnect(BOPost.CONNECTED_SENT);
                } else {
                    xBOPost.setIsConnect(BOPost.NOT_AGGREGATE);
                }
            }
        }
    }

    private static boolean addPeopleWithFollow(WebElement xActorContainer) {
        try {
            WebElement xButtonFollow = null;
            try {
                xButtonFollow = xActorContainer.findElement(By.xpath(".//*[@aria-label='Seguir']"));
            } catch (Exception e) {
                //Por si esta en ingles
                xButtonFollow = xActorContainer.findElement(By.xpath(".//*[@aria-label='Follow']"));
            }

            xButtonFollow.click();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean addPeopleWithConnect(String href) {
        try {
            //Abro un nuevo tab con el perfil de la persona
            ((JavascriptExecutor) driver).executeScript("window.open('" + href + "', '_blank');");
            ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(1));
            Thread.sleep(1500);

            //Me conecto al usuario
            WebElement connect = null;
            try {
                connect = driver.findElement(By.xpath("//*[@class='artdeco-button__text' and text() ='Conectar']"));
            } catch (Exception e) {
                //Por si esta en ingles
                connect = driver.findElement(By.xpath("//*[@class='artdeco-button__text' and text() ='Connect']"));
            }
            connect.click();

            try {
                WebElement xModal = driver.findElement(By.id("artdeco-modal-outlet"));
                xModal.findElement(By.xpath(".//button[@aria-label='Send now']")).click();
            } catch (Exception e) {
            }
            //Cierro el tab y vuelvo al principal
            driver.close();

        } catch (Exception e) {
            return false;
        } finally {
            //Cierro los posibles tabs abiertos con error
            ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
            if (tabs.size() > 1) {
                for (int i = 1; i < tabs.size(); i++) {
                    driver.switchTo().window(tabs.get(i));
                    driver.close();
                }
            }
            //Vuelvo al tab principal
            driver.switchTo().window(tabs.get(0));
        }
        return true;
    }

    private static void afterAnalyze(Integer errors) throws Exception {

        //Espera dos segundos de base para que cargue la pagina + 1 segundo cada 10 errores
        Thread.sleep(2000 + (100L * errors));
        //Realizo un scroll hacia abajo
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,150)", "");
        } catch (Exception e) {
        }

        seeNewPost();
    }

    private static void seeNewPost() throws Exception {


        //Si llego al final de los posteos, presiona el boton ver nuevas (lo que actualiza la pagina)
        WebElement xNewPostButton = null;
        try {
            xNewPostButton = driver.findElement(By.id("main")).findElement(By.xpath("//button[text() ='Ver nuevas publicaciones']"));
            xNewPostButton.click();
            reloaded = true;
        } catch (Exception e) {
            try {
                xNewPostButton = driver.findElement(By.id("main")).findElement(By.xpath("//button[text() ='See new posts']"));
                xNewPostButton.click();
                reloaded = true;
            } catch (Exception i) {
            }
        }
    }
}
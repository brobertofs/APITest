/*
CESAR School
Bruno Roberto Florentino da Silva
Professor: Davi Hardman
Testes de Serviços - Exercício 04
*/

package aulara;

import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class exercicio04 {

    Faker faker = new Faker();
    String userName = faker.name().firstName();
    String userEmail = userName + "@qa.com.br";
    String productName = faker.pokemon().name();

    @BeforeClass
    public static void preCondition() {
        baseURI = "http://localhost";
        port = 3000;
    }

    @Test
    public void exercicio04() {
        String userID = cadastrarUsuario();
        String userToken = autenticarUsuario();
        String productID = cadastrarProduto(userToken);
        cadastrarCarrinho(userToken, productID);
        excluirUsuario(userID, 400, "Não é permitido excluir usuário com carrinho cadastrado");
        cancelarCompra(userToken);
        excluirUsuario(userID, 200, "Registro excluído com sucesso");
    }

    public String cadastrarUsuario() {
        String userID = given()
                .body("{\n" +
                        "  \"nome\": \"" + userName + "\",\n" +
                        "  \"email\": \"" + userEmail + "\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");
        return userID;
    }

    public String autenticarUsuario() {
        String userToken = given()
                .body("{\n" +
                        "  \"email\": \"" + userEmail + "\",\n" +
                        "  \"password\": \"teste\"\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is ("Login realizado com sucesso"))
                .extract().path("authorization");
        return userToken;
    }

    public String cadastrarProduto(String authorization) {
        String productID = given()
                .header("authorization", authorization)
                .body("{\n" +
                        "  \"nome\": \""+ productName +"\",\n" +
                        "  \"preco\": 200,\n" +
                        "  \"descricao\": \"Pokemon\",\n" +
                        "  \"quantidade\": 200\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/produtos")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");
        return productID;
    }

    public void cadastrarCarrinho(String authorization, String productID) {
        given()
                .header("authorization", authorization)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \""+ productID + "\",\n" +
                        "      \"quantidade\": 1\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/carrinhos")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"));
    }

    public void cancelarCompra(String userToken) {
        given()
                .header("authorization", userToken)
                .when()
                .delete("/carrinhos/cancelar-compra")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso. Estoque dos produtos reabastecido"));
    }

    public void excluirUsuario(String userID, int statusCode, String message){
        given()
                .pathParam("_id", userID)
                .when()
                .delete("/usuarios/{_id}")
                .then()
                .statusCode(statusCode)
                .body("message", is(message));
    }
}

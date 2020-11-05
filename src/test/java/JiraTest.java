import static io.restassured.RestAssured.*;

import java.io.File;

import org.testng.Assert;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

public class JiraTest { 

	public static void main(String[] args) {
		
		
		RestAssured.baseURI="http://localhost:8080";

		//Login scenario
		
		SessionFilter session = new SessionFilter(); // listen and remember if the session was created
		
		//Login scenario to Jira to create session using Login API

		//relaxedHTTPSValidation(). - workaround HTTPS checks
		String response = given().header("Content-Type", "application/json").body("{\r\n" + 
				"    \"username\" : \"tanyadragan\",\r\n" + 
				"    \"password\" : \"Tanya2011533\"\r\n" + 
				"      } ").log().all()
		.log().all().filter(session).when().post("/rest/auth/1/session")
		.then().log().all().extract().response().asString();
		
		// Update comment  : pathParam("comId", "10019")  post("rest/api/2/issue/{id}/comment/{comId}") code()
		
		/********************************* Add comment to issue *****************************
		String expectedMsg = "My second Updated comment from REST API (Eclipse)";
		String addCommentResponse = given().pathParam("id", "10028").log().all().header("Content-Type", "application/json")
		.body("{\r\n" + 
				"    \"body\": \""+expectedMsg+"\",\r\n" + 
				"    \"visibility\": {\r\n" + 
				"        \"type\": \"role\",\r\n" + 
				"        \"value\": \"Administrators\"\r\n" + 
				"    }\r\n" + 
				"}").filter(session).when().post("rest/api/2/issue/{id}/comment") //{id} - bug id path
			.then().log().all().assertThat().statusCode(201).extract().response().asString();;
		JsonPath js = new JsonPath (addCommentResponse); 
		String commentId = js.get("id");
		/*****************************************************************************************/

						
		/************************* Add attachment to issue **************************************/
		
		String attachData = given().header("X-Atlassian-Token", "no-check").pathParam("id", "10028").filter(session)
		.header("Content-Type", "multipart/form-data")
		.multiPart("file", new File("jira.txt"))  // sending file
		.when().post("rest/api/2/issue/{id}/attachments")
		.then().log().all().assertThat().statusCode(200).extract().response().asString();
		JsonPath js2 = new JsonPath (attachData);
		String attachmentId = js2.getString("id");  
		System.out.println(attachmentId);
		/*****************************************************************************************/

		
		/*************************************** Get issue details *****************************/
	
		String issueDetails = given().filter(session).pathParam("id", "10028")
				.queryParam("fields", "comment") // getting details about comment & attach
				.queryParam("fields", "attachment") 
		.log().all().when().get("rest/api/2/issue/{id}")
		.then().log().all().extract().response().asString();
		JsonPath js1 = new JsonPath (issueDetails);
		int count = js1.getInt("fields.comment.comments.size()");
		//System.out.println(count);
		/*****************************************************************************************/

		
		/********************** Check bug's comment corresponds to bug id *****************
		
		//System.out.println(commentId);
  		for (int i = 0; i < count; i++) {
			if (js1.get("fields.comment.comments[" + i + "].id").equals(commentId)) {
				String actualMsg = js1.get("fields.comment.comments[" + i + "].body");
				Assert.assertEquals(actualMsg, expectedMsg);
			}
		} 
		/*****************************************************************************************/

		
				
		/*********************** Delete few comments ******************************
		 for (int i = 10135; i > 10108; i--) {
			given().filter(session).pathParam("comId", i).log().all().when()
			.delete("rest/api/2/issue/10028/comment/{comId}").then().log().all().assertThat().statusCode(204);
		}
		****************************************************************************/
		
		
		
		/********************************** Delete few attachments (id hard wired ) **********************
		for (int i = 10048; i > 10010; i--) {
			given().filter(session).pathParam("id", i).log().all().when().delete("rest/api/2/attachment/{id}") 
			.then().log().all();
		}
		/****************************************************************************/
		
		/********************************** Delete all attachments (get id of attachment dynamically) ********
		int attachmentsCount = js1.getInt("fields.attachment.size()");
		System.out.println(attachmentsCount);

		for (int i = 0; i < attachmentsCount; i++) {
			String n = js1.get("fields.attachment[" + i + "].id").toString();
			given().filter(session).log().all().when()
			.delete("rest/api/2/attachment/" + n + "") 
			.then().log().all();
		}
		/****************************************************************************/

	}

}

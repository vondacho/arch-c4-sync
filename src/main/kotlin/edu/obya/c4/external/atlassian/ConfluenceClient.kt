package edu.obya.c4.external.atlassian

class ConfluenceClient {

// https://developer.atlassian.com/server/confluence/confluence-rest-api-examples/
// https://developer.atlassian.com/cloud/confluence/basic-auth-for-rest-apis/
// https://id.atlassian.com/manage-profile/security/api-tokens

/*
curl
-D-
-X GET
-H "Authorization: Basic b3ZvbmRhY2hAZWRnZWxhYi5jaDpJc3BSVGZmMFlCMnlnc3Baa1ZXQjdEMjA="
-H "Content-Type: application/json"
"https://edgelab.atlassian.net/wiki/rest/api/content/1041694742?expand=body.storage"
 */

/*
-D-
-X GET
-H "Authorization: Basic b3ZvbmRhY2hAZWRnZWxhYi5jaDpJc3BSVGZmMFlCMnlnc3Baa1ZXQjdEMjA="
-H "Content-Type: application/json"
"https://edgelab.atlassian.net/wiki/rest/api/content?title=myPage%20Title&spaceKey=TST&expand=history"
 */

/*
curl
-D-
-X GET
-H "Authorization: Basic b3ZvbmRhY2hAZWRnZWxhYi5jaDpJc3BSVGZmMFlCMnlnc3Baa1ZXQjdEMjA="
-H "Content-Type: application/json"
-d '{"type":"page","title":"new page","space":{"key":"TST"},"body":{"storage":{"value":"<p>This is <br/> a new page</p>","representation":"storage"}}}'
http://localhost:8080/wiki/rest/api/content/
 */

/*
<p>Context</p>
<ac:structured-macro ac:name=\"structurizr-workspace\" ac:schema-version=\"1\" data-layout=\"default\" ac:macro-id=\"5452cdd8-3f17-418c-a8ff-3cd171d7a979\">
   <ac:parameter ac:name=\"diagram\">el-themis</ac:parameter>
   <ac:parameter ac:name=\"structurizrUrl\">https://www.structurizr.com</ac:parameter>
   <ac:parameter ac:name=\"apiKey\">ddf13468-3843-485f-9a42-f3f20603d857</ac:parameter>
   <ac:parameter ac:name=\"diagramSelector\">true</ac:parameter>
   <ac:parameter ac:name=\"workspaceId\">55008</ac:parameter>
</ac:structured-macro>
 */
}

/*
{
  "id": "1041694742",
  "type": "page",
  "status": "current",
  "title": "Themis",
  "macroRenderedOutput": {},
  "body": {
    "storage": {
      "value": "<p>Context</p><ac:structured-macro ac:name=\"structurizr-workspace\" ac:schema-version=\"1\" data-layout=\"default\" ac:macro-id=\"5452cdd8-3f17-418c-a8ff-3cd171d7a979\"><ac:parameter ac:name=\"diagram\">el-themis</ac:parameter><ac:parameter ac:name=\"structurizrUrl\">https://www.structurizr.com</ac:parameter><ac:parameter ac:name=\"apiKey\">ddf13468-3843-485f-9a42-f3f20603d857</ac:parameter><ac:parameter ac:name=\"diagramSelector\">true</ac:parameter><ac:parameter ac:name=\"workspaceId\">55008</ac:parameter></ac:structured-macro>",
      "representation": "storage",
      "embeddedContent": [],
      "_expandable": {
        "content": "/rest/api/content/1041694742"
      }
    },
    "_expandable": {
      "editor": "",
      "atlas_doc_format": "",
      "view": "",
      "export_view": "",
      "styled_view": "",
      "dynamic": "",
      "editor2": "",
      "anonymous_export_view": ""
    }
  },
  "extensions": {
    "position": 1421
  },
  "_expandable": {
    "childTypes": "",
    "container": "/rest/api/space/~792023391",
    "metadata": "",
    "operations": "",
    "children": "/rest/api/content/1041694742/child",
    "restrictions": "/rest/api/content/1041694742/restriction/byOperation",
    "history": "/rest/api/content/1041694742/history",
    "ancestors": "",
    "version": "",
    "descendants": "/rest/api/content/1041694742/descendant",
    "space": "/rest/api/space/~792023391"
  },
  "_links": {
    "editui": "/pages/resumedraft.action?draftId=1041694742",
    "webui": "/spaces/~792023391/pages/1041694742/Themis",
    "context": "/wiki",
    "self": "https://edgelab.atlassian.net/wiki/rest/api/content/1041694742",
    "tinyui": "/x/FgAXPg",
    "collection": "/rest/api/content",
    "base": "https://edgelab.atlassian.net/wiki"
  }
}
 */

import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import {
  DynamoDBDocumentClient,
  ScanCommand,
  PutCommand,
  GetCommand,
  UpdateCommand,
  DeleteCommand,
} from "@aws-sdk/lib-dynamodb";

// Defining the names of the userbase.
const userDataBase = "userbase";

// Defining the region of the database.
const dynamoRegion = "us-east-1";

// Setting up the dynamo client.
const dynamoDBClient = new DynamoDBClient({region: dynamoRegion});
const dynamo = DynamoDBDocumentClient.from(dynamoDBClient);

// Defining the request methods.
const REQUEST_METHOD = {
  POST: "POST",
  PUT: "PUT",
  PATCH: "PATCH",
  GET: "GET",
  DELETE: "DELETE"
};

// Defining the codes.
const STATUS_CODE = {
  SUCCESS: 200,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  NO_ACCESS: 401,
  NOT_FOUND: 404,
  INTERNAL_ERROR: 500,
};

// Paths (I'll just put it in an object for the sake of ease and consistency).
const accPath = "/account"; // Creating this as its own variable in the off chance we change the layout.

const PATHS = {
  ACCOUNTS: "/accounts",
  ADD_EMPLOYEE: `${accPath}/add_employee`,
  LOGIN_GUEST: `${accPath}/login/guest`,
  LOGIN_REGISTERED: `${accPath}/login/registered`,
  LOGOUT: `${accPath}/{id}/logout`,
  ACCOUNT_ID: `${accPath}/{id}`,
  DELETE_CONFIRM: `${accPath}/{id}/delete-confirmation`,
  USER_PREFS: `${accPath}/{id}/userPreferences`,
  PASSWORD: `${accPath}/{id}/password`,
}

// Creating the handler for the calls.
export const handler = async (event, context) => {
  console.log("Request event method: ", event.httpMethod);
  console.log("EVENT\n" + JSON.stringify(event, null, 2));

  let response;

  switch(true) {
    // Getting all users with params.
    case event.httpMethod == REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == PATHS.ACCOUNTS:
      response = await getUsers(
        Number(event.queryStringParameters.limit), 
        Number(event.queryStringParameters.offset),
        String(event.queryStringParameters.active),
        String(event.queryStringParameters.type)
      );
      break;

    // Adding a new customer to the database.
    case event.httpMethod == REQUEST_METHOD.POST &&
      (event.requestContext.resourcePath == PATHS.ACCOUNTS ||
      event.requestContext.resourcePath == PATHS.ADD_EMPLOYEE):
      response = await addCustomer(JSON.parse(event.body));
      break;

    // Logging out a specific user.
    case event.httpMethod == REQUEST_METHOD.PATCH &&
      event.requestContext.resourcePath == PATHS.LOGOUT:
      response = await logOut(String(event.pathParameters.id));
      break;

    // Updating the list of user preferences.
    case event.httpMethod == REQUEST_METHOD.PUT &&
      event.requestContext.resourcePath == PATHS.USER_PREFS:
      response = await updatePrefs(String(event.pathParameters.id), JSON.parse(event.body));
      break;

    // Getting a user's preferences.
    case event.httpMethod == REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == PATHS.USER_PREFS:
      response = await getUserPrefs(String(event.pathParameters.id));
      break;

    // Deleting a user's preferences.
    case event.httpMethod == REQUEST_METHOD.DELETE &&
      event.requestContext.resourcePath == PATHS.USER_PREFS:
      response = await deleteUserPrefs(String(event.pathParameters.id));
      break;

    // Getting a specific user.
    case event.httpMethod == REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == PATHS.ACCOUNT_ID:
      response = await getUser(String(event.pathParameters.id));
      break;

    // Deleting a specific user.
    case event.httpMethod == REQUEST_METHOD.DELETE &&
      event.requestContext.resourcePath == PATHS.ACCOUNT_ID:
      response = await deleteUser(String(event.pathParameters.id));
      break;

    // Logging in a user or guest.
    case event.httpMethod == REQUEST_METHOD.PATCH &&
        (event.requestContext.resourcePath == PATHS.LOGIN_GUEST ||
        event.requestContext.resourcePath == PATHS.LOGIN_REGISTERED):
        response = await login(JSON.parse(event.body));
        break;

    // Adding deletion confirmation.
    case event.httpMethod == REQUEST_METHOD.POST &&
          event.requestContext.resourcePath == PATHS.DELETE_CONFIRM:
          response = await deleteConfirm(String(event.pathParameters.id), JSON.parse(event.body));
          break;

    // 404, Request is not recognized.
    default:
      response = buildResponse(
        STATUS_CODE.NOT_FOUND, 
        event.requestContext.resourcePath
      );
      break;
  }

  return response;
}

// Function to browse through users.
async function getUsers(limit, offset, active, type) {
  const params = {
    TableName: userDataBase,
    FilterExpression: "dataType = :dataType",
    ExpressionAttributeValues: {
      ":dataType": "user"
    }
  };

  // (Optional) Filtering by active users.
  if (active !== "undefined") {
    params.FilterExpression += " AND active = :active";
    params.ExpressionAttributeValues[":active"] = (active === 'true');
  }

  // (Optional) Filtering by customers or employees.
  if (type === 'employee') {
    params.FilterExpression += " AND attribute_exists(job_position)";
  } else if (type === 'customer') {
    params.FilterExpression += " AND attribute_not_exists(job_position)";
  }

  const command = new ScanCommand(params);

  try {
    const response = await dynamo.send(command);

    // Applying the offset.
    const items = response.Items.slice(offset, offset + limit);

    return buildResponse(STATUS_CODE.SUCCESS, items);
  } catch (error) {
    console.error("Error was encountered: ", error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function to add a customer to the database.
async function addCustomer(requestBody) {
  if (!requestBody || !requestBody.id || !requestBody.name) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request body is missing required fields: 'id' and 'name'.",
    });
  }
  
  requestBody.dataType = "user";

  const commandParams = {
    TableName: userDataBase,
    Item: requestBody
  };

  const command = new PutCommand(commandParams);

  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "SAVE",
      Message: "SUCCESS",
      Item: requestBody
    };

    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    // Error handling.
    console.error("Error adding customer: ", error);
    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "An error occurred while adding the customer.",
      Error: error.message,
    });
  }
}

// Function to log someone out of their account.
async function logOut(userId) {
  const timestamp = new Date().toISOString();
  const commandParams = {
    TableName: userDataBase,
    Key: { 
      id: userId,
      dataType: "user"
    },
    UpdateExpression: "set active = :active, lastActive = :lastActive",
    ExpressionAttributeValues: {
      ":active": false,
      ":lastActive": timestamp
    },
    ReturnValues: "ALL_NEW"
  };

  const command = new UpdateCommand(commandParams);

  try {
    const data = await dynamo.send(command);
    const responseBody = {
      Operation: "LOGOUT",
      Message: "SUCCESS",
      Item: data.Attributes
    };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error was encountered: ", error);
    return buildResponse(STATUS_CODE.ERROR, { Message: "ERROR", Error: error.message });
  }
}

// Function to update / set a list of user prefs to the database.
async function updatePrefs(userId, requestBody) {
  const params = {
    TableName: userDataBase,
    Item: {
      id: userId,
      dataType: "preferences",
      Item: requestBody
    },
    ReturnValues: "ALL_OLD"
  };

  const command = new PutCommand(params);

  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "UPDATE",
      Message: "SUCCESS",
      Item: requestBody
    };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function to get a specific user's preferences.
async function getUserPrefs(userID) {
  const params = {
    TableName: userDataBase,
    Key: {
      id: userID,
      dataType: "preferences"
    }
  };

  const command = new GetCommand(params);

  try {
    const response = await dynamo.send(command);
    return buildResponse(STATUS_CODE.SUCCESS, response.Item);
  } catch (error) {
    console.log(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function to delete a user's preferences.
async function deleteUserPrefs(userID) {
  const params = {
    TableName: userDataBase,
    Key: {
      id: userID,
      dataType: "preferences"
    }
  }

  const command = new DeleteCommand(params);

  try {
    const response = await dynamo.send(command);
    const responseBody = {
      Operation: "DELETE",
      Message: "SUCCESS",
      Item: response
    };
    return buildResponse(STATUS_CODE.NO_CONTENT, responseBody);
  } catch (error) {
    console.log(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function for getting a specifc user.
async function getUser(userID) {
  const params = {
    TableName: userDataBase,
    Key: {
      id: userID,
      dataType: "user"
    }
  }

  const command = new GetCommand(params);

  try {
    const response = await dynamo.send(command);
    return buildResponse(STATUS_CODE.SUCCESS, response.Item);
  } catch (error) {
    console.log(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function for deleting a specific user.
async function deleteUser(userID) {
  const params = {
    TableName: userDataBase,
    Key: {
      id: userID,
      dataType: "user"
    }
  }

  const command = new DeleteCommand(params);

  try {
    const response = await dynamo.send(command);
    const responseBody = {
      Operation: "DELETE",
      Message: "SUCCESS",
      Item: response
    }
    return buildResponse(STATUS_CODE.NO_CONTENT, responseBody);
  } catch (error) {
    console.log(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function for logging in a user / guest.
async function login(requestBody) {
  try {
    // If the user is logging in as a guest.
    if (requestBody.id === "guest") {
      const responseBody = {
        Operation: "GUEST LOGIN",
        Message: "SUCCESS",
        Item: requestBody
      }

      return buildResponse(STATUS_CODE.SUCCESS, responseBody);
    }

    // Getting the user's data.
    const params = {
      TableName: userDataBase,
      Key: {
        id: requestBody.id,
        dataType: "user"
      }
    }

    const command = new GetCommand(params);
    const userData = await dynamo.send(command);

    // Checking to see if the user's login info is good.
    if (requestBody.email === userData.Item.email && requestBody.password === userData.Item.password) {
      const responseBody = {
        Operation: "USER LOGIN",
        Message: "SUCCESS",
        Item: userData.Item
      }

      const timestamp = new Date().toISOString();

      const patchParams = {
        TableName: userDataBase,
        Key: { 
          id: requestBody.id,
          dataType: "user"
        },
        UpdateExpression: "set active = :active, lastActive = :lastActive",
        ExpressionAttributeValues: {
          ":active": true,
          ":lastActive": timestamp
        },
        ReturnValues: "ALL_NEW"
      }

      const updateCommand = new UpdateCommand(patchParams);
      await dynamo.send(updateCommand);

      return buildResponse(STATUS_CODE.SUCCESS, responseBody);
    }

    const responseBody = {
      Operation: "USER LOGIN",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.NO_ACCESS, responseBody);
  } catch (error) {
    console.error(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Function for confirming deletion.
async function deleteConfirm(userID, requestBody) {
  const params = {
    TableName: userDataBase,
    Key: {
      id: userID,
      dataType: "user"
    }
  }

  const command = new DeleteCommand(params);

  try {
    if (requestBody.confirmation == true) {
      const response = await dynamo.send(command);

      const responseBody = {
        Operation: "DELETE",
        Message: "SUCCESS",
        Item: response
      }

      return buildResponse(STATUS_CODE.NO_CONTENT, responseBody);
    }

    const responseBody = {
      Operation: "DELETE",
      Message: "CANCELLED"
    }

    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error(error);

    // Returning an object.
    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "ERROR",
      Item: requestBody
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

// Creating a utility function to build the response.
function buildResponse(statusCode, body) {
  return {
    statusCode: statusCode,
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  };
}

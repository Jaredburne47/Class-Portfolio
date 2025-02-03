import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import {
  DynamoDBDocumentClient,
  ScanCommand,
  PutCommand,
  GetCommand,
  DeleteCommand,
} from "@aws-sdk/lib-dynamodb";

// DynamoDB table names and region.
const notificationTableName = "notificationtable";
const notificationTypesTableName = "notificationtypes";
const dynamoRegion = "us-east-1";

// Setting up the DynamoDB client.
const dynamoDBClient = new DynamoDBClient({ region: dynamoRegion });
const dynamo = DynamoDBDocumentClient.from(dynamoDBClient);

// Defining the request methods.
const REQUEST_METHOD = {
  POST: "POST",
  GET: "GET",
  DELETE: "DELETE",
};

// Defining the status codes.
const STATUS_CODE = {
  SUCCESS: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  NOT_FOUND: 404,
  INTERNAL_ERROR: 500,
};

// Paths for route handling.
const NOTIFICATIONS_PATH = "/notifications";
const NOTIFICATION_TYPES_PATH = "/notification-types";
const NOTIFICATION_PARAM_PATH = `${NOTIFICATIONS_PATH}/{notificationId}`;
const NOTIFICATION_TYPE_PARAM_PATH = `${NOTIFICATION_TYPES_PATH}/{typeId}`;
const NOTIFICATION_HISTORY_PATH = "/notifications/history";

// Main Lambda handler for the API.
export const handler = async (event) => {
  console.log("Request event method:", event.httpMethod);
  console.log("EVENT\n", JSON.stringify(event, null, 2));

  let response;

  switch (true) {
    // Notification Routes
    case event.httpMethod === REQUEST_METHOD.POST &&
      event.requestContext.resourcePath === NOTIFICATIONS_PATH:
      response = await addNotification(JSON.parse(event.body));
      break;

    case event.httpMethod === REQUEST_METHOD.GET &&
      event.requestContext.resourcePath === NOTIFICATIONS_PATH:
      response = await getAllNotifications(event.queryStringParameters);
      break;

    case event.httpMethod === REQUEST_METHOD.GET &&
      event.requestContext.resourcePath === NOTIFICATION_PARAM_PATH:
      response = await getNotificationById(event.pathParameters.notificationId);
      break;

    case event.httpMethod === REQUEST_METHOD.DELETE &&
      event.requestContext.resourcePath === NOTIFICATION_PARAM_PATH:
      response = await deleteNotification(event.pathParameters.notificationId);
      break;

    // Notification Type Routes
    case event.httpMethod === REQUEST_METHOD.POST &&
      event.requestContext.resourcePath === NOTIFICATION_TYPES_PATH:
      response = await addNotificationType(JSON.parse(event.body));
      break;

    case event.httpMethod === REQUEST_METHOD.GET &&
      event.requestContext.resourcePath === NOTIFICATION_TYPES_PATH:
      response = await getAllNotificationTypes();
      break;

    case event.httpMethod === REQUEST_METHOD.GET &&
      event.requestContext.resourcePath === NOTIFICATION_TYPE_PARAM_PATH:
      response = await getNotificationTypeById(event.pathParameters.typeId);
      break;

    case event.httpMethod === REQUEST_METHOD.DELETE &&
      event.requestContext.resourcePath === NOTIFICATION_TYPE_PARAM_PATH:
      response = await deleteNotificationType(event.pathParameters.typeId);
      break;

    case event.httpMethod === REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == NOTIFICATION_HISTORY_PATH:
      response = await getNotificationHistory(event.queryStringParameters)
      break;
    
    default:
      response = buildResponse(STATUS_CODE.NOT_FOUND, { message: "Route not found" });
      break;
  }

  return response;
};

//// Notification Functions ////

// Function to add a notification to the NotificationTable.
async function addNotification(requestBody) {
  if (!requestBody.notificationId || !requestBody.userId || !requestBody.content) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      message: "Invalid input: 'notificationId', 'userId', and 'content' are required fields.",
    });
  }

  // Set default values for optional fields
  const contactMethod = requestBody.contactMethod || "unknown"; // Default to "unknown" if not provided
  const createdAt = requestBody.createdAt || new Date().toISOString(); // Default to current timestamp if not provided
  
  const item = {
    notificationId: requestBody.notificationId,   // Unique identifier for the notification
    userId: requestBody.userId,                  // User receiving the notification
    typeId: requestBody.typeId || "defaultType", // Notification type (optional, default value provided)
    content: requestBody.content,                // Notification content/message
    contactMethod: requestBody.contactMethod || "unknown", // Contact method (email or text, default: "unknown")
    createdAt: requestBody.createdAt || new Date().toISOString(), // Timestamp of creation (default: current time)
  };
  const commandParams = {
    TableName: notificationTableName,
    Item: item,
  };
  const command = new PutCommand(commandParams);

  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "SAVE",
      Message: "SUCCESS",
      Item: requestBody,
    };
    return buildResponse(STATUS_CODE.CREATED, responseBody);
  } catch (error) {
    console.error("Error in addNotification:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to save notification" }
    );
  }
}

// Function to get all notifications in the NotificationTable.
async function getAllNotifications(queryParams) {
  const commandParams = { TableName: notificationTableName };

  // Filter notifications by userId and typeId if provided in the query parameters
  if (queryParams && queryParams.userId) {
    commandParams.FilterExpression = "userId = :userId";
    commandParams.ExpressionAttributeValues = { ":userId": queryParams.userId };

    if (queryParams.typeId) {
      commandParams.FilterExpression += " AND typeId = :typeId";
      commandParams.ExpressionAttributeValues[":typeId"] = queryParams.typeId;
    }
  }

  const command = new ScanCommand(commandParams);

  try {
    const data = await dynamo.send(command);
    const responseBody = { notifications: data.Items };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error in getAllNotifications:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to retrieve notifications" }
    );
  }
}

// Function to get a specific notification by ID.
async function getNotificationById(notificationId) {
  const params = {
    TableName: notificationTableName,
    Key: {notificationId},
  };
  const command = new GetCommand(params);

  try {
    const data = await dynamo.send(command);
    if (!data.Item) {
      return buildResponse(STATUS_CODE.NOT_FOUND, { message: "Notification not found" });
    }
    return buildResponse(STATUS_CODE.SUCCESS, data.Item);
  } catch (error) {
    console.error("Error in getNotificationById:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to retrieve notification" }
    );
  }
}
//Function to retrieve Notification History
async function getNotificationHistory(queryParams) {
  
  // Validate query parameters
  if (!queryParams || (!queryParams.userId && !queryParams.typeId)) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      message: "Invalid query parameters: 'userId' or 'typeId' is required.",
    });
  }
  const commandParams = { TableName: notificationTableName};

// Build filter expressions based on query parameters
  if (queryParams && (queryParams.userId || queryParams.typeId)) {
    const filterExpressions = [];
    const expressionAttributeValues = {};

    if (queryParams.userId) {
      filterExpressions.push("userId = :userId");
      expressionAttributeValues[":userId"] = queryParams.userId;
    }

    if (queryParams.typeId) {
      filterExpressions.push("typeId = :typeId");
      expressionAttributeValues[":typeId"] = queryParams.typeId;
    }

    commandParams.FilterExpression = filterExpressions.join(" AND ");
    commandParams.ExpressionAttributeValues = expressionAttributeValues;
  }

  const command = new ScanCommand(commandParams);

  try {
    const data = await dynamo.send(command);
    
    //Return 404 if no items are found
    if (!data.Items || data.Items.length === 0) {
      return buildResponse(STATUS_CODE.NOT_FOUND, {
        message: "No notification history found matching the provided criteria."
      });
    }
    
    const responseBody = { history: data.Items };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error in getNotificationHistory:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to retrieve notification history" }
    );
  }
}
// Function to delete a specific notification by ID.
async function deleteNotification(notificationId) {
  const params = {
    TableName: notificationTableName,
    Key: {notificationId},
    ReturnValues: "ALL_OLD",
  };
  const command = new DeleteCommand(params);

  try {
    const data = await dynamo.send(command);
    if (!data.Attributes) {
      return buildResponse(STATUS_CODE.NOT_FOUND, { message: "Notification not found" });
    }
    const responseBody = {
      Operation: "DELETE",
      Message: "SUCCESS",
      Item: data.Attributes,
    };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error in deleteNotification:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to delete notification" }
    );
  }
}

//// Notification Type Functions ////

// Function to add a new notification type to the NotificationTypesTable.
async function addNotificationType(requestBody) {
  if (!requestBody.typeId) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      message: "Invalid input: 'typeId' is a required field.",
    });
  }
  
  const commandParams = {
    TableName: notificationTypesTableName,
    Item: requestBody,
  };
  const command = new PutCommand(commandParams);

  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "SAVE",
      Message: "SUCCESS",
      Item: requestBody,
    };
    return buildResponse(STATUS_CODE.CREATED, responseBody);
  } catch (error) {
    console.error("Error in addNotificationType:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to save notification type" }
    );
  }
}

// Function to get all notification types in the NotificationTypesTable.
async function getAllNotificationTypes() {
  const command = new ScanCommand({ TableName: notificationTypesTableName });

  try {
    const data = await dynamo.send(command);
    return buildResponse(STATUS_CODE.SUCCESS, { notificationTypes: data.Items });
  } catch (error) {
    console.error("Error in getAllNotificationTypes:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to retrieve notification types" }
    );
  }
}

// Function to get a specific notification type by ID.
async function getNotificationTypeById(typeId) {
  const params = {
    TableName: notificationTypesTableName,
    Key: { typeId },
  };
  const command = new GetCommand(params);

  try {
    const data = await dynamo.send(command);
    if (!data.Item) {
      return buildResponse(STATUS_CODE.NOT_FOUND, { message: "Notification type not found" });
    }
    return buildResponse(STATUS_CODE.SUCCESS, data.Item);
  } catch (error) {
    console.error("Error in getNotificationTypeById:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to retrieve notification type" }
    );
  }
}

// Function to delete a specific notification type by ID.
async function deleteNotificationType(typeId) {
  const params = {
    TableName: notificationTypesTableName,
    Key: { typeId },
    ReturnValues: "ALL_OLD",
  };
  const command = new DeleteCommand(params);

  try {
    const data = await dynamo.send(command);
    if (!data.Attributes) {
      return buildResponse(STATUS_CODE.NOT_FOUND, { message: "Notification type not found" });
    }
    return buildResponse(STATUS_CODE.SUCCESS, {
      Operation: "DELETE",
      Message: "SUCCESS",
      Item: data.Attributes,
    });
  } catch (error) {
    console.error("Error in deleteNotificationType:", error);
    return buildResponse(
      STATUS_CODE.INTERNAL_ERROR,
      { message: "Failed to delete notification type" }
    );
  }
}

// Utility function to build the response.
function buildResponse(statusCode, body) {
  return {
    statusCode,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

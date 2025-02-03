/*global fetch*/
import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import {
  DynamoDBDocumentClient,
  ScanCommand,
  PutCommand,
  GetCommand,
  UpdateCommand,
  DeleteCommand,
} from "@aws-sdk/lib-dynamodb";

// Defining the names of the orderbase.
const orderDataBase = "orderbase";

// Defining the region of the database.
const dynamoRegion = "us-east-1";

// Setting up the dynamo client.
const dynamoDBClient = new DynamoDBClient({ region: dynamoRegion });
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

// Defining the paths for orders service.
const PATHS = {
  ORDERS: "/orders",
  ORDER_ID: "/orders/{orderId}",
  ORDER_STATUS: "/orders/{orderId}/status",
};

// creating a hangler for all the calls.
export const handler = async (event, context) => {
  console.log("Request event method: ", event.httpMethod);
  console.log("EVENT\n" + JSON.stringify(event, null, 2));

  let response;

  switch (true) {
    // Getting all Orders with params.
    case event.httpMethod == REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == PATHS.ORDERS:
      response = await getOrders(
        Number(event.queryStringParameters.limit),
        Number(event.queryStringParameters.offset),
        String(event.queryStringParameters.accountId),
        String(event.queryStringParameters.orderStatus),
        String(event.queryStringParameters.dateCreated)
      );
      break;
    // creating a new order
    case event.httpMethod == REQUEST_METHOD.POST &&
      event.requestContext.resourcePath == PATHS.ORDERS:
      response = await createOrder(JSON.parse(event.body));
      break;
    // Getting an order by id
    case event.httpMethod == REQUEST_METHOD.GET &&
      event.requestContext.resourcePath == PATHS.ORDER_ID:
      response = await getOrder(String(event.pathParameters.orderId));
      break;

    // Updating an order by id
    case event.httpMethod == REQUEST_METHOD.PATCH &&
      event.requestContext.resourcePath == PATHS.ORDER_ID:
      response = await updateOrder(String(event.pathParameters.orderId), JSON.parse(event.body));
      break;

    // Delete an order by id
    case event.httpMethod == REQUEST_METHOD.DELETE &&
      event.requestContext.resourcePath == PATHS.ORDER_ID:
      response = await deleteOrder(String(event.pathParameters.orderId));
      break;
    
    // Updating an order status by id
    case event.httpMethod == REQUEST_METHOD.PATCH &&
      event.requestContext.resourcePath == PATHS.ORDER_STATUS:
      response = await updateOrderStatus(String(event.pathParameters.orderId), JSON.parse(event.body));
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

async function getOrders(limit, offset, accountId, orderStatus, dateCreated) {
  // Setting up the params. FilterExpression and EAV are blank initially.
  const params = {
    TableName: orderDataBase,
  }

  const filters = [];
  const expressionAttributes = {};

  // Using the query params as needed.
  if (accountId !== 'undefined') {
    filters.push("accountId = :accountId");
    expressionAttributes[":accountId"] = accountId;
  }

  if (orderStatus !== 'undefined') {
    filters.push("orderStatus = :orderStatus");
    expressionAttributes[":orderStatus"] = orderStatus;
  }

  if (dateCreated !== 'undefined') {
    filters.push("dateCreated = :dateCreated");
    expressionAttributes[":dateCreated"] = dateCreated;
  }

  if (filters.length > 0) {
    params.FilterExpression = filters.join(" AND ");
    params.ExpressionAttributeValues = expressionAttributes;
  }

  const command = new ScanCommand(params);

  try {
    const response = await dynamo.send(command);

    // Applying the offset.
    const items = response.Items.slice(offset, offset + limit);

    return buildResponse(STATUS_CODE.SUCCESS, items);
  } catch (error) {
    console.error("Error was encountered: ", error);

    const responseBody = {
      Operation: "INTERNAL ERROR",
      Message: "Internal server error."
    }

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, responseBody);
  }
}

function buildResponse(statusCode, body) {
  return {
    statusCode: statusCode,
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  };
}

//Fucntion that adds a new order to the database.
async function createOrder(requestBody) {
  if (!requestBody || !requestBody.orderId || !requestBody.accountId) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request body is missing required fields: 'orderId' and 'accountId'.",
    });
  }
  requestBody.datatype = "Order";
  // sets the date and time when order was created
  requestBody.dateCreated = new Date().toISOString().split('T')[0];

// sets the total price of the order
  requestBody.totalPrice = 0;
  for (let i = 0; i < requestBody.items.length; i++) {
    requestBody.totalPrice += (requestBody.items[i].price * requestBody.items[i].quantity);
  }
// fromats the total price to 2 decimal places
  requestBody.totalPrice = requestBody.totalPrice.toFixed(2);
  const commandParams = {
    TableName: orderDataBase,
    Item: requestBody,
  
  }
   const command = new PutCommand(commandParams);
  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "SAVE",
      Message: "SUCCESS",
      Item: requestBody,
    };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    //Error Handling
    console.error("Error creating Order: ", error);
    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "Error creating Order: ",
      Error: error.message,
    });

  }
}

// Function to delete an order by id
async function deleteOrder(orderId) {
  console.log("Deleting order:", orderId);

  // Check for missing or invalid orderId
  if (!orderId) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request is missing required field: 'orderId'.",
    });
  }

  const params = {
    TableName: orderDataBase,
    Key: {
      orderId: orderId,
    },
    ReturnValues: "ALL_OLD",
  };

  const command = new DeleteCommand(params);

  try {
    const dbresponse = await dynamo.send(command);

    // Check if the order existed
    if (!dbresponse.Attributes) {
      return buildResponse(STATUS_CODE.NOT_FOUND, {
        Message: `Order with ID '${orderId}' not found.`,
      });
    }

    console.log("Order deleted successfully:", dbresponse.Attributes);

    const responseBody = {
      Message: `Order with ID '${orderId}' has been successfully deleted.`,
      DeletedOrder: dbresponse.Attributes,
    };

    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error deleting order:", error);

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "Error occurred while deleting the order.",
      Error: error.message,
    });
  }
}

// Function that updates the order by id to the database.
async function updateOrder(orderId, requestBody) {
  console.log("Request body: ", requestBody);

  if (!requestBody) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request body is empty",
    });
  }
  if (!orderId) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request body is missing required field: 'orderId' in endpoint.",
    });
  }

  const params = {
    TableName: orderDataBase,
    Item: {
      orderId: orderId,
      dataType: "Order",
      ...requestBody,
    },
    ReturnValues: "ALL_OLD",
  };
  requestBody.dateUpdated = new Date().toISOString().split('T')[0];
  const command = new PutCommand(params);
  try {
    await dynamo.send(command);
    const responseBody = {
      Operation: "UPDATE",
      Message: "SUCCESS",
      requestBody,
    };
    return buildResponse(STATUS_CODE.SUCCESS, responseBody);
  } catch (error) {
    console.error("Error updating order:", error);
    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "Error Updating Order: ",
      Error: error.message,
    });
  }
}

// Function to update the status of an order and send notifications
async function updateOrderStatus(orderId, requestBody) {
  console.log("Updating order status");
  
  if (!orderId || !requestBody || !requestBody.orderStatus) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request body is missing required field: 'orderStatus' or 'orderId'.",
    });
  }

  const params = {
    TableName: orderDataBase,
    Key: {
      orderId: orderId,
    },
    UpdateExpression: "set orderStatus = :status",
    ExpressionAttributeValues: {
      ":status": requestBody.orderStatus,
    },
    ReturnValues: "UPDATED_NEW",
  };

  const command = new UpdateCommand(params);

  try {
    const dbresponse = await dynamo.send(command);
    
    const uniqueNotificationId = `${orderId}-${Date.now()}`;

    // Prepare the notification payload
    const notificationUrl = "https://5dnkq3520d.execute-api.us-east-1.amazonaws.com/Notif/notifications";
    const notificationBody = {
      notificationId: uniqueNotificationId,
      userId: requestBody.accountId,
      typeId: "OrderStatusUpdate",
      content: `Your order status has been updated to: ${requestBody.orderStatus}`,
    };

    console.log("Sending notification to:", notificationUrl);

    console.log("Here is what the body looks like:", notificationBody);

    // Attempt the external API POST call
    const extresponse = await fetch(notificationUrl, {
      method: "POST",
      body: JSON.stringify(notificationBody),
      headers: { "Content-Type": "application/json" },
    });

    if (extresponse.ok) {
      const responseData = await extresponse.json();
      console.log("Notification sent successfully:", responseData);

      const responseBody = {
        Operation: "UPDATE",
        Message: "SUCCESS",
        NotificationSent: true,
        UpdatedAttributes: dbresponse.Attributes,
        NotificationBody: notificationBody,
      };

      return buildResponse(STATUS_CODE.SUCCESS, responseBody);
    } else {
      console.error("Notification API failed:", extresponse.statusText);

      return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
        Message: "Order status updated, but notification failed.",
        NotificationError: extresponse.statusText,
      });
    }
  } catch (error) {
    console.error("Error updating order status or sending notification:", error);

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "Error Updating Order Status or Sending Notification",
      Error: error.message,
    });
  }
}

async function getOrder(orderId) {
  console.log("Fetching order with ID:", orderId);

  // Validate orderId
  if (!orderId) {
    return buildResponse(STATUS_CODE.BAD_REQUEST, {
      Message: "Request is missing required field: 'orderId'.",
    });
  }

  const params = {
    TableName: orderDataBase,
    Key: {
      orderId: orderId,
    },
  };

  const command = new GetCommand(params);

  try {
    const response = await dynamo.send(command);

    // Check if the order exists
    if (!response.Item) {
      return buildResponse(STATUS_CODE.NOT_FOUND, {
        Message: `Order with ID '${orderId}' not found.`,
      });
    }

    return buildResponse(STATUS_CODE.SUCCESS, response.Item);
  } catch (error) {
    console.error("Error fetching order:", error);

    return buildResponse(STATUS_CODE.INTERNAL_ERROR, {
      Message: "Error occurred while fetching the order.",
      Error: error.message,
    });
  }
}
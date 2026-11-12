[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/java-client-generated/src/main/java/org/ergoplatform/restapi/client/Scan.java)

The `Scan` class is part of the Ergo Node API and provides a model for a scanning operation. It contains three properties: `scanName`, `scanId`, and `trackingRule`. 

The `scanName` property is a string that represents the name of the scan. The `scanId` property is an integer that represents the unique identifier of the scan. The `trackingRule` property is an instance of the `ScanningPredicate` class, which represents the predicate used to track the scan.

The `Scan` class provides getter and setter methods for each property, allowing the properties to be accessed and modified as needed. It also provides methods for equality checking, hashing, and string representation.

This class can be used in the larger Ergo Node API project to represent a scanning operation. For example, it could be used to create a new scan by setting the `scanName` and `trackingRule` properties and then sending the `Scan` object to the server. It could also be used to retrieve information about an existing scan by retrieving the `scanId` property and sending a request to the server with that ID.

Example usage:

```java
// Create a new scan
Scan scan = new Scan()
    .scanName("My Scan")
    .trackingRule(new ScanningPredicate());

// Send the scan to the server
Scan createdScan = api.createScan(scan);

// Retrieve information about an existing scan
Scan existingScan = api.getScanById(123);
System.out.println(existingScan.getScanName());
```
## Questions: 
 1. What is the purpose of the `Scan` class?
    
    The `Scan` class is part of the Ergo Node API and represents a scanning operation with a name, ID, and tracking rule.

2. What is the `ScanningPredicate` class and how is it related to `Scan`?
    
    The `ScanningPredicate` class is used as the tracking rule for a `Scan` object. It defines a predicate that is used to filter the results of a scan.

3. Why is there a `toIndentedString` method in the `Scan` class?
    
    The `toIndentedString` method is used to convert an object to a string with each line indented by 4 spaces. It is used in the `toString` method to format the output of the `Scan` object.
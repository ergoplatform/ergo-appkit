[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/appkit/src/main/java/org/ergoplatform/appkit/config/ErgoToolConfig.java)

The `ErgoToolConfig` class is responsible for managing the configuration parameters of the ErgoTool utility. It contains two fields: `node` and `parameters`, which represent the configuration of the Ergo node and the tool parameters, respectively. 

The `getNode()` method returns the `ErgoNodeConfig` object, which contains the configuration parameters of the Ergo node. 

The `getParameters()` method returns a `ToolParameters` object, which is a HashMap of name-value pairs representing the tool parameters. 

The class provides three methods for loading the configuration from a file: `load(Reader reader)`, `load(File file)`, and `load(String fileName)`. The `load(Reader reader)` method takes a `Reader` object as input and returns an `ErgoToolConfig` object created from the file content. The `load(File file)` method takes a `File` object as input and returns an `ErgoToolConfig` object created from the file content. The `load(String fileName)` method takes a `String` object as input, which is the name of the file relative to the current directory. It then resolves the file using `File#getAbsolutePath()` and returns an `ErgoToolConfig` object created from the file content. 

This class can be used in the larger project to manage the configuration parameters of the ErgoTool utility. Developers can use the `load()` methods to load the configuration from a file and then access the configuration parameters using the `getNode()` and `getParameters()` methods. For example, the following code loads the configuration from a file named `config.json` and then gets the node configuration:

```
ErgoToolConfig config = ErgoToolConfig.load("config.json");
ErgoNodeConfig nodeConfig = config.getNode();
```
## Questions: 
 1. What is the purpose of the `ErgoToolConfig` class?
    
    The `ErgoToolConfig` class is responsible for holding configuration parameters for the ErgoTool utility.

2. What is the purpose of the `load` methods?
    
    The `load` methods are used to load configuration data from a file or reader and create an instance of `ErgoToolConfig` with the file content.

3. What is the purpose of the `ToolParameters` class?
    
    The `ToolParameters` class represents a section of the configuration with named parameters in the form of a HashMap of Name-Value pairs.
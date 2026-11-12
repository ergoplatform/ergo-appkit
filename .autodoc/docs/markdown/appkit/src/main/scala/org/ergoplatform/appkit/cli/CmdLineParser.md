[View code on GitHub](https://github.com/ergoplatform/ergo-appkit/appkit/src/main/scala/org/ergoplatform/appkit/cli/CmdLineParser.scala)

# `CmdLineParser` in `ergo-appkit`

`CmdLineParser` is a Scala object that provides a method to parse command line arguments and extract options and parameters. It also provides a method to parse the network type from a string.

## `parseOptions`

`parseOptions` takes a sequence of strings as input, which represents the command line arguments. It returns a tuple of two values: a map of options and their values, and a sequence of parameters.

The method first creates an empty map to store the options and their values. It then creates a mutable buffer and copies the input sequence into it. This buffer is used to extract options and parameters.

The method then iterates over the buffer and checks if each argument starts with the `CmdOption.Prefix` string, which is `"--"`. If an argument starts with this prefix, it is considered an option. The method then looks up the option in the `CmdOption.options` list, which is a list of predefined options. If the option is found, the method checks if it is a flag or not. If it is a flag, the value is set to `"true"`. If it is not a flag, the method checks if there is a value for the option in the next argument. If there is no value, an error is thrown. If there is a value, it is added to the map of options, and the value is removed from the buffer.

If an argument does not start with the `CmdOption.Prefix` string, it is considered a parameter and is left in the buffer.

The method returns the map of options and their values, and the remaining parameters in the buffer.

## `parseNetwork`

`parseNetwork` takes a string as input, which represents the network type. It returns a `NetworkType` object, which is an enumeration that represents the network type.

The method checks if the input string is `"testnet"` or `"mainnet"`. If it is, it returns the corresponding `NetworkType` object. If it is not, an error is thrown.

## Usage

`CmdLineParser` can be used to parse command line arguments in a Scala application. For example, the following code shows how to use `parseOptions` to parse command line arguments and print the options and parameters:

```scala
object MyApp {
  def main(args: Array[String]): Unit = {
    val (options, params) = CmdLineParser.parseOptions(args)
    println("Options:")
    options.foreach { case (name, value) =>
      println(s"$name=$value")
    }
    println("Parameters:")
    params.foreach { param =>
      println(param)
    }
  }
}
```

If the application is run with the following command line arguments:

```
--conf myconf.json --verbose param1 param2
```

The output will be:

```
Options:
conf=myconf.json
verbose=true
Parameters:
param1
param2
```
## Questions: 
 1. What is the purpose of this code?
    
    This code defines a command line parser for extracting options and parameters from the command line arguments passed to the Ergo Appkit CLI.

2. What is the format of the options that can be passed to this command line parser?
    
    The options that can be passed to this command line parser start with `--` and are parsed into name-value pairs. Any option with `CmdOption.isFlag == true` is parsed without a value.

3. What is the purpose of the `parseNetwork` method?
    
    The `parseNetwork` method takes a string argument representing the network type and returns the corresponding `NetworkType` enum value. If the input string is not a valid network type, it raises a `usageError`.
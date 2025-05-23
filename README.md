# Page4J - A Fork of PRImA Core Libs

Page4J is a Java library for working with PAGE (Page Analysis and Ground-Truth Elements) XML files. It is a fork of the original [PRImA Research Lab's prima-core-libs](https://github.com/PRImA-Research-Lab/prima-core-libs) and has been updated to support modern Java versions and build tools, with ongoing development and maintenance.

This library provides a suite of tools for reading, writing, and manipulating PAGE XML files, which are commonly used in document image analysis and digital humanities for representing document layouts and content.

## Modules

The project is divided into several modules:

*   **page4j-basic**: Contains basic interfaces and classes used across the library. This includes fundamental data structures and utilities.
*   **page4j-maths**: Provides mathematical data types and functions, such as geometric primitives (points, lines, polygons) and operations relevant to layout analysis.
*   **page4j-io**: Handles file input/output operations, particularly for reading and writing PAGE XML files. It includes mechanisms for XML parsing and serialization.
*   **page4j-dla**: Focuses on Document Layout Analysis (DLA) specific content. This module includes classes for representing and manipulating the logical and physical structure of a document as defined in the PAGE format (e.g., regions, text lines, glyphs, reading order).

## Building the Project

Page4J uses Apache Maven as its build system. To build the project, you'll need:

*   Java Development Kit (JDK) version 21 or higher.
*   Apache Maven 3.6.0 or higher.

Clone the repository and navigate to the root directory. Then, run the following Maven command to compile the project, run tests, and build the JAR files:

```bash
mvn clean install
```

This will build all modules and install them into your local Maven repository.

## Developing the Library

If you want to contribute to Page4J or develop it further:

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd page4j
    ```
2.  **Import into your IDE:** The project is a standard Maven project and can be imported into most Java IDEs (e.g., IntelliJ IDEA, Eclipse).
3.  **Build the project:** Use the Maven command `mvn clean install` as described above.
4.  **Running Tests:** Tests are located in the `src/test/java` directory of each module. You can run them using your IDE's test runner or via Maven:
    ```bash
    mvn test
    ```

## Using Page4J in Your Project

### Maven Dependency (Placeholder)

Once Page4J is deployed to Maven Central, you will be able to add it to your project by including the following dependency in your `pom.xml`. Replace `LATEST_VERSION` with the desired version.

```xml
<dependency>
    <groupId>de.uniwue.zpd</groupId>
    <artifactId>page4j-dla</artifactId> <!-- Or other modules as needed -->
    <version>LATEST_VERSION</version>
</dependency>
```

For now, you can build the project locally and install the artifacts into your local Maven repository (`~/.m2/repository`) using `mvn clean install`. Then, you can use the snapshot version in your project:

```xml
<dependency>
    <groupId>de.uniwue.zpd</groupId>
    <artifactId>page4j-dla</artifactId> <!-- Or other modules as needed -->
    <version>1.0-SNAPSHOT</version>
</dependency>
```
Make sure your project's `pom.xml` (or your parent `pom.xml`) also includes the `page4j-parent` if you are using multiple modules and want to manage versions centrally, or ensure your local repository has the parent POM.

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvements, please open an issue or submit a pull request on the project's repository.

When contributing, please ensure your code adheres to the existing coding style and that all tests pass.

## License

This project is licensed under the Apache License, Version 2.0. See the `LICENSE` file for more details. The original work by PRImA Research Lab was also distributed under the Apache License, Version 2.0.

## Acknowledgements

This project is a fork and continuation of the work done by the [PRImA Research Lab, University of Salford, United Kingdom](http://www.primaresearch.org/). We acknowledge their significant contributions to the field of document analysis and the development of the original PAGE format and associated libraries.

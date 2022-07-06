To use EXL you can either use the EXL-V1.jar included in the provided code or you can download the code provided and type mvn package in the directory of practice-compiler and it will create a new EXL-V1.jar file in the folder called target. To run an EXL file you need will the jar to be in the directory you are typing the following command into command prompt from

java -cp EXL-V1.jar EXL filename

Note : If you forgot to put .exl at the end it will assume this and do it for you before looking for the file and you can also type -R after the filename and it will attempt to run the code straight after compiling. 

Note : Additional examples of how to run files and programs written in exl be found in the test suite in the folder java located at \practice-compiler\src\main\java as that compiles and runs files using the have Process Builder that uses the same commands as command prompt
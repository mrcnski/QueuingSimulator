run: build
	java -cp bin Driver

build:
	javac -d bin src/*.java

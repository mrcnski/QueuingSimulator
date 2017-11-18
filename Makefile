run: build
	java -cp bin Driver

build:
	mkdir -p bin
	javac -d bin src/*.java

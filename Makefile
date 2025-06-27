# Makefile for Java Kanban project

# Variables
JUNIT_JAR=lib/junit-platform-console-standalone.jar
CLASS_PATH="out/production/java-kanban:out/test/java-kanban:lib/*"

# Targets
.PHONY: compile clean test

compile:
	@echo "Compiling Java files..."
	@javac -d out/production/java-kanban -cp $(CLASS_PATH) src/model/*.java src/taskmanager/*.java src/Main.java
	@javac -d out/test/java-kanban -cp $(CLASS_PATH) src/test/taskmanager/*.java

clean:
	@echo "Cleaning compiled files..."
	@rm -rf out/production/java-kanban/* out/test/java-kanban/*

test:
	@echo "Running tests..."
	@java -jar $(JUNIT_JAR) \
		--class-path $(CLASS_PATH) \
		--scan-class-path

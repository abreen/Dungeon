CLASSES=classes
CLASSPATH=jars/snakeyaml-1.12.jar:jars/lanterna-3.0.0-alpha4.jar:$(CLASSES)

.PHONY: classes

all:
	@test -d classes || mkdir classes
	@javac -Xlint:unchecked -d $(CLASSES) -classpath $(CLASSPATH):. com/abreen/dungeon/*.java com/abreen/dungeon/exceptions/*.java com/abreen/dungeon/worker/*.java com/abreen/dungeon/model/*.java

clean:
	@rm -rf classes

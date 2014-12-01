CLASSES=classes
CLASSPATH=jars/snakeyaml-1.12.jar:$(CLASSES)

.PHONY: classes run_server run_client

.SILENT: classes

all: classes

classes:
	-mkdir classes
	javac -Xlint:unchecked -d $(CLASSES) -classpath $(CLASSPATH):. com/abreen/dungeon/*.java com/abreen/dungeon/exceptions/*.java com/abreen/dungeon/worker/*.java com/abreen/dungeon/model/*.java

run_server: classes
	java -classpath $(CLASSPATH):$(CLASSES) com.abreen.dungeon.DungeonServer

run_client: classes
	java -classpath $(CLASSPATH):$(CLASSES) com.abreen.dungeon.DungeonClient

clean:
	-rm -r classes

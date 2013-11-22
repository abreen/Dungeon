# Dungeon

`Dungeon` is a multiplayer text adventure game engine written in Java. It
is inspired by traditional text adventure games from years ago, with some
new ideas and features.

This project comes with a server and client, though you could probably get
away with connecting to the server over a raw TCP connection once you 
understand how it works. I'll get to writing up the protocol later.

The client is really buggy at the moment. Don't use it.

`Dungeon` is my first exploration into text-based games and network 
programming. You're welcome to help out with development.

There is no "license" for the source code, apart from the requirement that
you don't sue me if it breaks anything. Expect me to find an appropriate
license in due time.


## Extensibility

At the moment, the server automatically constructs a boring universe for
players to roam around in. I am in the process of building in a YAML
parser so people can write their own game universes and load them into the
server. This is what makes `Dungeon` a bit more flexible than any old text
adventure game.

I'll get around to authoring a small universe demoing the features that
`Dungeon` has to offer.

## Dependencies

The server uses [SnakeYAML][snakeyaml], a YAML parser for Java. The JAR
file (version 1.12) is included in the `lib` directory.

I've been using some Java 1.7-only syntax, so you'll need (at least)
Java 1.7.

## Building and running

There's a small `build.xml` file for Apache Ant. As long as you have Ant
installed, just run `ant` in the base directory. The default target should
build all the classes to a directory called `classes`. 

To start the server, change to the top-level directory of the repository
and run `java -cp classes:lib/* com.abreen.dungeon.DungeonServer`. The
server will automatically look for a `config.yml` file under the `yaml`
directory, from which it will take information like hostname, port, etc.
If it finds no configuration file, it will ask for program arguments.

[snakeyaml]: http://code.google.com/p/snakeyaml/

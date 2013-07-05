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
players to roam around in. In the future, I'll integrate a YAML parser into
the server so that authors can write their own universes and play in them.
This is what makes `Dungeon` a bit more flexible than any old text
adventure game.

I'll get around to authoring a small universe demoing the features that
`Dungeon` has to offer. If you poke around, you could probably guess how to
manually construct universes inside the `DungeonServer.java` file.

## Building

There's a small `build.xml` file for Apache Ant. As long as you have Ant
installed, just run `ant` in the base directory. The default target should
build all the classes to a directory called `classes`. Change to the 
directory and start the server with `java DungeonServer`.

